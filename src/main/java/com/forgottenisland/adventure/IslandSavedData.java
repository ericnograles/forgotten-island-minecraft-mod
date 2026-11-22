package com.forgottenisland.adventure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

/** Stored in the Overworld, never in player inventories or a transient loaded entity. */
public final class IslandSavedData extends SavedData {
    private final Map<UUID, BlockPos> departures = new HashMap<>();
    private final Set<BlockPos> portals = new HashSet<>();
    private final Map<Long, BlockPos> portalCells = new HashMap<>();
    private final Set<Long> builtChunks = new HashSet<>();
    private final Map<String,EncounterState> encounters = new HashMap<>();
    private final Set<String> exits = new HashSet<>();
    public boolean exitBuilt(String boss){return exits.contains(boss);}
    public void markExitBuilt(String boss){exits.add(boss);setDirty();}
    public EncounterState encounter(String boss) {return encounters.computeIfAbsent(boss,k->new EncounterState());}
    void restoreEncounter(String boss,EncounterState.Snapshot snapshot){encounters.put(boss,EncounterState.restore(snapshot));}
    public boolean chunkBuilt(long pos) {return builtChunks.contains(pos);}
    public void markChunkBuilt(long pos) {builtChunks.add(pos);setDirty();}
    public static IslandSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(IslandSavedData::load, IslandSavedData::new, "forgottenisland_adventure");
    }
    public void link(UUID player, BlockPos center) { departures.put(player, center.immutable()); setDirty(); }
    public Optional<BlockPos> departure(UUID player) { return Optional.ofNullable(departures.get(player)); }
    public void registerPortal(BlockPos center) {
        portals.add(center.immutable());
        for(var c:StarPattern.OPENING) portalCells.put(center.offset(c.x(),0,c.z()).asLong(),center.immutable());
        setDirty();
    }
    public Optional<BlockPos> portalAt(BlockPos cell) { return Optional.ofNullable(portalCells.get(cell.asLong())); }
    public static IslandSavedData load(CompoundTag tag) {
        var data=new IslandSavedData();
        for(Tag entry:tag.getList("Departures",Tag.TAG_COMPOUND)) {
            var e=(CompoundTag)entry;
            if(e.hasUUID("Player")) data.departures.put(e.getUUID("Player"),BlockPos.of(e.getLong("Center")));
        }
        for(long center:tag.getLongArray("Portals")) data.registerPortal(BlockPos.of(center));
        for(long chunk:tag.getLongArray("BuiltChunks")) data.builtChunks.add(chunk);
        for(Tag entry:tag.getList("Exits",Tag.TAG_STRING))data.exits.add(entry.getAsString());
        var encounters=tag.getCompound("Encounters");
        for(String boss:encounters.getAllKeys()) {
            var e=encounters.getCompound(boss);
            EncounterState.Stage stage;
            try {stage=EncounterState.Stage.valueOf(e.getString("Stage"));} catch(IllegalArgumentException ex) {stage=EncounterState.Stage.READY;}
            var players=new HashSet<UUID>();
            for(Tag p:e.getList("Participants",Tag.TAG_COMPOUND))if(((CompoundTag)p).hasUUID("Id"))players.add(((CompoundTag)p).getUUID("Id"));
            var delivered=new HashSet<String>();for(Tag t:e.getList("Delivered",Tag.TAG_STRING))delivered.add(t.getAsString());
            var collected=new HashSet<String>();for(Tag t:e.getList("Collected",Tag.TAG_STRING))collected.add(t.getAsString());
            data.restoreEncounter(boss,new EncounterState.Snapshot(stage,e.getInt("Ticks"),e.getInt("Attempt"),e.getInt("Phase"),e.getFloat("Health"),e.getFloat("MaxHealth"),e.hasUUID("Actor")?e.getUUID("Actor"):null,players,delivered,collected));
        }
        data.setDirty(false);
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        var links=new ListTag();
        departures.forEach((id,pos)->{var e=new CompoundTag();e.putUUID("Player",id);e.putLong("Center",pos.asLong());links.add(e);});
        tag.put("Departures",links);
        tag.putLongArray("Portals",portals.stream().mapToLong(BlockPos::asLong).toArray());
        tag.putLongArray("BuiltChunks",builtChunks.stream().mapToLong(Long::longValue).toArray());
        var savedEncounters=new CompoundTag();
        encounters.forEach((boss,state)->{
            var s=state.snapshot();var e=new CompoundTag();
            e.putString("Stage",s.stage().name());e.putInt("Ticks",s.ticks());e.putInt("Attempt",s.attempt());e.putInt("Phase",s.phase());
            e.putFloat("Health",s.health());e.putFloat("MaxHealth",s.maxHealth());if(s.actor()!=null)e.putUUID("Actor",s.actor());
            var players=new ListTag();for(UUID id:s.participants()){var p=new CompoundTag();p.putUUID("Id",id);players.add(p);}e.put("Participants",players);
            var delivered=new ListTag();for(String token:s.delivered())delivered.add(StringTag.valueOf(token));e.put("Delivered",delivered);
            var collected=new ListTag();for(String token:s.collected())collected.add(StringTag.valueOf(token));e.put("Collected",collected);
            savedEncounters.put(boss,e);
        });
        tag.put("Encounters",savedEncounters);
        var exitTags=new ListTag();for(String exit:exits)exitTags.add(StringTag.valueOf(exit));tag.put("Exits",exitTags);
        tag.putInt("Version",1);
        return tag;
    }
}
