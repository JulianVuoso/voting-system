package ar.edu.itba.pod.tpe.models;

import java.util.Map;

public class FPTP extends Result {

    Map<String, Integer> fptp;

    public FPTP( Map<String, Integer> fptp, boolean partial, Type type) {
        this.fptp = fptp;
        this.partial = partial;
        this.type = type;
    }

    public String getWinner(){
        return "a";
    }

    public Map<String, Integer> getPercenteges(){
        return fptp;
    }

    public Map<String, Integer> getVotes(){
        return fptp;
    }
}


// Map<String = Provincia, Map<Integer = MESA, List<Vote>>>