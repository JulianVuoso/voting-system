package ar.edu.itba.pod.tpe.stub;

import java.io.Serializable;

public class InspectionVote implements Serializable {
    private static final long serialVersionUID = 4140895505170828845L;

    private final int tableNumber;
    private final String province;
    private final String fptpVote;

    public InspectionVote(int tableNumber, String province, String fptpVote) {
        this.tableNumber = tableNumber;
        this.province = province;
        this.fptpVote = fptpVote;
    }


    public int getTableNumber() {
        return tableNumber;
    }

    public String getProvince() {
        return province;
    }

    public String getFptpVote() {
        return fptpVote;
    }

    @Override
    public String toString() {
        return "InspectionVote{" +
                "tableNumber=" + tableNumber +
                ", province='" + province + '\'' +
                ", fptpVote='" + fptpVote + '\'' +
                '}';
    }
}
