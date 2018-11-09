package cn.jimmiez.pcu.common.graph;

/**
 * a read-only graph interface
 */
public interface GraphStatic {

    /** weight between two unreachable vertices **/
    double N = Double.POSITIVE_INFINITY;

    /**
     * @param i the index of one vertex
     * @param j the index of another vertex
     * @return the weight of edge
     */
    double edgeWeight(int i, int j);

    /**
     * @return number of vertices
     */
    int verticesCount();

    /**
     * @param i ith vertex
     * @return the index of adjacent vertices of ith vertex, i excluded
     */
    Iterable<Integer> adjacentVertices(int i);

}
