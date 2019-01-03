package cn.jimmiez.pcu.common.graph;

/**
 * A unordered pair of two vertices.
 * {@literal <vi, vj> == <vj, vi>}
 */
public class VertexPair {

    private int vi;

    private int vj;

    public VertexPair(int vi, int vj) {
        this.vi = vi;
        this.vj = vj;
    }

    public int getVi() {
        return vi;
    }

    public int getVj() {
        return vj;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        VertexPair that = (VertexPair) object;

        if (this.vi == that.vi && this.vj == that.vj) return true;
        if (this.vi == that.vj && this.vj == that.vi) return true;
        return false;
    }

    @Override
    public int hashCode() {
        int result1 = vi;
        result1 = 31 * result1 + vj;
        int result2 = vj;
        result2 = 31 * result2 + vi;
        return result1 + result2;
    }
}
