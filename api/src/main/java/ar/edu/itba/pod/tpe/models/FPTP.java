package ar.edu.itba.pod.tpe.models;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class FPTP extends Result {

    Map<String, Integer> fptp;

    public FPTP( Map<String, Integer> fptp, boolean partial, Type type) {
        this.fptp = fptp;
        this.partial = partial;
        this.type = type;
    }

    public FPTP (){
        this.fptp = new HashMap<>();
        this.partial = true;
        this.type = Type.FPTP;
    }

    public FPTP (String party){
        this.fptp = new HashMap<>();
        this.fptp.put(party,1);

    }

    public Map<String, Integer> getMap(){
        return fptp;
    }

    public void setPartial(boolean partial){
        this.partial = partial;
    }

    public void obtainWinner(){
        this.winner[0] = Collections.max(fptp.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
    }

    public String getWinner(){
        return winner[0];
    }

    public Map<String, Integer> getVotes(){
        return fptp;
    }
}