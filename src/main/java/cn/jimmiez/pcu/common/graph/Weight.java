package cn.jimmiez.pcu.common.graph;

public class Weight implements Comparable<Double>{
    private double val;

    public Weight(double v) {
        this.val = v;
    }

    public double val() {return val;}

    public void set(Double d) {this.val = d;}

    @Override
    public int compareTo(Double o) {
        return - o.compareTo(val);
    }


}
