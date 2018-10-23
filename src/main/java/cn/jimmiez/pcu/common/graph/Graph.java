package cn.jimmiez.pcu.common.graph;

public interface Graph {

    /** weight between two unreachable vertices **/
    double N = Double.POSITIVE_INFINITY;

    double edgeWeight(int i, int j);

    int verticesCount();

    int[] adjacentVertices(int i);

}
