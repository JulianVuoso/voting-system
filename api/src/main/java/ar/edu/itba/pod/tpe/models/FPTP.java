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

    public boolean getPartial(){
        return partial;
    }

    public Map<String, Integer> getMap(){
        return fptp;
    }

    public void setPartial(boolean partial){
        this.partial = partial;
    }

    /** Check **/
    public void obtainWinner(){
       // this.winner[0] = Collections.max(fptp.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
        this.winner[0] = Collections.max(fptp.entrySet(), (o1, o2) -> o1.getValue() > o2.getValue()? 1:-1).getKey();
    }

    public String getWinner(){
        winner[0] = Collections.max(fptp.entrySet(), (o1, o2) -> o1.getValue() > o2.getValue()? 1:-1).getKey();
        return winner[0];
    }

    public Map<String, Integer> getVotes(){
        return fptp;
    }
}