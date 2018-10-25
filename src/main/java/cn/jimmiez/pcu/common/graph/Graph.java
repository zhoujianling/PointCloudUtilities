package cn.jimmiez.pcu.common.graph;

import java.util.List;

public interface Graph {

    /** weight between two unreachable vertices **/
    double N = Double.POSITIVE_INFINITY;

    /**
     * @param i the index of one vertex
     * @param j the index of another vertex
     * @return the weight of edge
     */
    double edgeWeight(int i, int j);

    int verticesCount();

    List<Integer> adjacentVertices(int i);

}
