package com.forgottenisland.adventure;

import java.util.*;

/** Persisted encounter membership and shared crystal objective; no live Entity references. */
public final class StomachState {
    private final Map<UUID,Integer> guests=new HashMap<>();
    private int attempt=-1,crystals;
    public int attempt(){return attempt;}
    public int crystals(){return crystals;}
    public Map<UUID,Integer> guests(){return Map.copyOf(guests);}
    public boolean contains(UUID id){return guests.containsKey(id);}
    public boolean active(UUID id,int encounterAttempt){return guests.getOrDefault(id,-2)==encounterAttempt&&attempt==encounterAttempt&&crystals!=0;}
    /** Returns true only when a new shared interior objective must be built. */
    public boolean swallow(UUID id,int encounterAttempt){
        boolean fresh=attempt!=encounterAttempt||crystals==0;
        if(fresh){attempt=encounterAttempt;crystals=7;}
        guests.put(id,encounterAttempt);return fresh;
    }
    public boolean breakCrystal(UUID id,int index,int encounterAttempt){
        if(index<0||index>=3||!active(id,encounterAttempt)||(crystals&(1<<index))==0)return false;
        crystals&=~(1<<index);
        if(crystals==0)release();return true;
    }
    /** Offline guests retain a recovery marker until they can actually be transported. */
    public void release(){crystals=0;guests.replaceAll((id,a)->-1);}
    public void remove(UUID id){guests.remove(id);}
    public void restore(int attempt,int crystals,Map<UUID,Integer> guests){this.attempt=attempt;this.crystals=crystals&7;this.guests.clear();this.guests.putAll(guests);}
}
