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
    private final Set<BlockPos> necklacePortals = new HashSet<>();
    private final Map<Long, BlockPos> portalCells = new HashMap<>();
    private final Set<Long> builtChunks = new HashSet<>();
    private final Set<Long> expandedChunks = new HashSet<>();
    private final Set<Long> protectedColumns = new HashSet<>();
    public boolean protectedColumn(int x,int z){return protectedColumns.contains(BlockPos.asLong(x,0,z));}
    public void protectColumn(int x,int z){protectedColumns.add(BlockPos.asLong(x,0,z));setDirty();}
    private final Map<String,EncounterState> encounters = new HashMap<>();
    private final Set<String> exits = new HashSet<>();
    private final Map<String,BlockPos> rewardDais=new HashMap<>();
    public BlockPos rewardDais(BossKind kind){return rewardDais.getOrDefault(kind.id,kind.altar());}
    public void rewardDais(BossKind kind,BlockPos pos){rewardDais.put(kind.id,pos.immutable());setDirty();}
    public final StomachState stomach=new StomachState();
    public boolean stomachBuilt;
    public boolean exitBuilt(String boss){return exits.contains(boss);}
    public void markExitBuilt(String boss){exits.add(boss);setDirty();}
    public EncounterState encounter(String boss) {return encounters.computeIfAbsent(boss,k->new EncounterState());}
    void restoreEncounter(String boss,EncounterState.Snapshot snapshot){encounters.put(boss,EncounterState.restore(snapshot));}
    public boolean chunkBuilt(long pos) {return builtChunks.contains(pos);}
    public void markChunkBuilt(long pos) {builtChunks.add(pos);setDirty();}
    public boolean expanded(long pos){return expandedChunks.contains(pos);}
    public void markExpanded(long pos){expandedChunks.add(pos);setDirty();}
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
    public void registerNecklacePortal(BlockPos center) {
        necklacePortals.add(center.immutable());
        for(var c:StarNecklaceItem.CELLS)portalCells.put(center.offset(c.x(),0,c.z()).asLong(),center.immutable());
        setDirty();
    }
    public static IslandSavedData load(CompoundTag tag) {
        var data=new IslandSavedData();
        for(Tag entry:tag.getList("Departures",Tag.TAG_COMPOUND)) {
            var e=(CompoundTag)entry;
            if(e.hasUUID("Player")) data.departures.put(e.getUUID("Player"),BlockPos.of(e.getLong("Center")));
        }
        for(long center:tag.getLongArray("Portals")) data.registerPortal(BlockPos.of(center));
        for(long center:tag.getLongArray("NecklacePortals")) data.registerNecklacePortal(BlockPos.of(center));
        for(long chunk:tag.getLongArray("BuiltChunks")) data.builtChunks.add(chunk);
        for(long chunk:tag.getLongArray("ExpandedChunks")) data.expandedChunks.add(chunk);
        for(long column:tag.getLongArray("ProtectedColumns"))data.protectedColumns.add(column);
        for(Tag entry:tag.getList("Exits",Tag.TAG_STRING))data.exits.add(entry.getAsString());
        var dais=tag.getCompound("RewardDais");for(String key:dais.getAllKeys())data.rewardDais.put(key,BlockPos.of(dais.getLong(key)));
        if(tag.getInt("Version")<2)for(var kind:BossKind.values())if(data.exits.contains(kind.id))data.rewardDais.put(kind.id,(kind==BossKind.MANANG?IslandWorld.MANANG:IslandWorld.BAKUNAWA).offset(0,0,kind==BossKind.MANANG?20:50));
        var stomach=tag.getCompound("Stomach");var guests=new HashMap<UUID,Integer>();
        data.stomachBuilt=tag.getBoolean("StomachBuilt");
        for(Tag entry:stomach.getList("Guests",Tag.TAG_COMPOUND)){var e=(CompoundTag)entry;if(e.hasUUID("Id"))guests.put(e.getUUID("Id"),e.getInt("Attempt"));}
        data.stomach.restore(stomach.getInt("Attempt"),stomach.getInt("Crystals"),guests);
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
        tag.putLongArray("NecklacePortals",necklacePortals.stream().mapToLong(BlockPos::asLong).toArray());
        tag.putLongArray("BuiltChunks",builtChunks.stream().mapToLong(Long::longValue).toArray());
        tag.putLongArray("ExpandedChunks",expandedChunks.stream().mapToLong(Long::longValue).toArray());
        tag.putLongArray("ProtectedColumns",protectedColumns.stream().mapToLong(Long::longValue).toArray());
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
        var dais=new CompoundTag();rewardDais.forEach((boss,pos)->dais.putLong(boss,pos.asLong()));tag.put("RewardDais",dais);
        var stomachTag=new CompoundTag();stomachTag.putInt("Attempt",stomach.attempt());stomachTag.putInt("Crystals",stomach.crystals());
        tag.putBoolean("StomachBuilt",stomachBuilt);
        var guests=new ListTag();stomach.guests().forEach((id,attempt)->{var e=new CompoundTag();e.putUUID("Id",id);e.putInt("Attempt",attempt);guests.add(e);});stomachTag.put("Guests",guests);tag.put("Stomach",stomachTag);
        var exitTags=new ListTag();for(String exit:exits)exitTags.add(StringTag.valueOf(exit));tag.put("Exits",exitTags);
        tag.putInt("Version",2);
        return tag;
    }
}
