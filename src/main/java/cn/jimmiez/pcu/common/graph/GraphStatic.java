package cn.jimmiez.pcu.common.graph;

import java.util.Collection;

/**
 * a read-only graph interface
 */
public interface GraphStatic {

    /** weight between two unreachable vertices **/
    double N = Double.POSITIVE_INFINITY;

    /**
     * @param i the index of one vertex
     * @param j the index of another vertex
     * @return the weight of edge, if there is no edge between vi and vj, return {@link GraphStatic#N}
     */
    double edgeWeight(int i, int j);

    /**
     * @param i ith vertex
     * @return the index of adjacent vertices of ith vertex, i excluded
     */
    Collection<Integer> adjacentVertices(int i);

    /**
     * the vertices of this graph
     * @return the collection of indices of vertex
     */
    Collection<Integer> vertices();
}
