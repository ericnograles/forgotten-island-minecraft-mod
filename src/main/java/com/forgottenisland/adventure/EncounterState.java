package com.forgottenisland.adventure;

import java.util.*;

/** Server-owned encounter journal. Views/skipping never advance this state machine. */
public final class EncounterState {
    public enum Stage { READY, ENTRANCE, COMBAT, DEFEAT, COMPLETE, TRANSFORM }
    public record Snapshot(Stage stage,int ticks,int attempt,int phase,float health,float maxHealth,
                           UUID actor,Set<UUID> participants,Set<String> delivered,Set<String> collected) {}
    private Stage stage=Stage.READY;
    private int ticks,attempt,phase=1;
    private float health,maxHealth;
    private UUID actor;
    private final Set<UUID> participants=new HashSet<>();
    private final Set<String> delivered=new HashSet<>(),collected=new HashSet<>();
    public Stage stage(){return stage;}
    public int ticks(){return ticks;}
    public int attempt(){return attempt;}
    public int phase(){return phase;}
    public float health(){return health;}
    public float maxHealth(){return maxHealth;}
    public UUID actor(){return actor;}
    public Set<UUID> participants(){return Set.copyOf(participants);}
    public boolean cinematic(){return stage==Stage.ENTRANCE||stage==Stage.TRANSFORM||stage==Stage.DEFEAT;}
    public boolean start(float baseHealth,Collection<UUID> players,UUID actor) {
        if(stage!=Stage.READY||players.isEmpty()||!Float.isFinite(baseHealth)||baseHealth<=0)return false;
        this.actor=Objects.requireNonNull(actor);participants.addAll(players);
        maxHealth=baseHealth*(1+.5f*Math.min(2,players.size()-1));health=maxHealth;
        attempt++;phase=1;ticks=0;stage=Stage.ENTRANCE;return true;
    }
    public void join(UUID player){if(stage==Stage.ENTRANCE||stage==Stage.COMBAT||stage==Stage.TRANSFORM)participants.add(player);}
    public boolean tick(int entranceLength,int defeatLength) {
        if(stage==Stage.READY||stage==Stage.COMPLETE)return false;
        ticks++;
        if(stage==Stage.TRANSFORM&&ticks>=60){stage=Stage.COMBAT;ticks=0;return true;}
        if(stage==Stage.ENTRANCE&&ticks>=entranceLength){stage=Stage.COMBAT;ticks=0;return true;}
        if(stage==Stage.DEFEAT&&ticks>=defeatLength){stage=Stage.COMPLETE;ticks=0;return true;}
        return false;
    }
    public void directForm(boolean serpent){
        if(stage!=Stage.ENTRANCE)return;
        phase=2;
        if(serpent){maxHealth*=1.25f;health=maxHealth;}
    }
    public boolean recordHealth(float current){return recordHealth(current,false);}
    public boolean recordHealth(float current,boolean serpent) {
        if(stage!=Stage.COMBAT||!Float.isFinite(current))return false;
        health=Math.max(0,Math.min(maxHealth,current));
        if(serpent&&phase==1&&health<=0){
            phase=2;maxHealth*=1.25f;health=maxHealth;stage=Stage.TRANSFORM;ticks=0;return true;
        }
        if(health<=0){stage=Stage.DEFEAT;ticks=0;return true;}
        if(!serpent&&health<=maxHealth*.5f&&phase==1){phase=2;ticks=0;}
        return false;
    }
    public boolean retry() {
        if(stage!=Stage.COMBAT&&stage!=Stage.ENTRANCE&&stage!=Stage.TRANSFORM)return false;
        stage=Stage.READY;ticks=0;health=maxHealth=0;actor=null;participants.clear();phase=1;return true;
    }
    public String rewardToken(String boss,UUID player,String item) {return boss+":"+attempt+":"+player+":"+item;}
    public boolean mayDeliver(UUID player,String token) {return stage==Stage.COMPLETE&&participants.contains(player)&&!delivered.contains(token)&&!collected.contains(token);}
    public boolean delivered(UUID player,String token) {
        if(!mayDeliver(player,token))return false;
        delivered.add(token);return true;
    }
    public boolean collect(String token) {return delivered.contains(token)&&collected.add(token);}
    public Snapshot snapshot() {return new Snapshot(stage,ticks,attempt,phase,health,maxHealth,actor,Set.copyOf(participants),Set.copyOf(delivered),Set.copyOf(collected));}
    public static EncounterState restore(Snapshot s) {
        var state=new EncounterState();state.stage=s.stage;state.ticks=Math.max(0,s.ticks);state.attempt=Math.max(0,s.attempt);
        state.phase=Math.max(1,Math.min(2,s.phase));state.maxHealth=Float.isFinite(s.maxHealth)?Math.max(0,s.maxHealth):0;
        state.health=Float.isFinite(s.health)?Math.max(0,Math.min(state.maxHealth,s.health)):state.maxHealth;state.actor=s.actor;
        state.participants.addAll(s.participants);state.delivered.addAll(s.delivered);state.collected.addAll(s.collected);
        return state;
    }
}
