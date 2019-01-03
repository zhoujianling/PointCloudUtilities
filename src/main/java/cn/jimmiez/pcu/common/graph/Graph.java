package cn.jimmiez.pcu.common.graph;

import java.util.Collection;

/**
 * an abstract graph that support more operations
 */
public interface Graph extends GraphStatic {

    /**
     * add an edge to the graph
     * @param vi the index of vertex i
     * @param vj the index of vertex j
     * @param weight the weight of the edge to be added
     */
    void addEdge(int vi, int vj, double weight);

    /**
     * add the edge that is specified by ordered index pair {@literal <i, j>}
     * @param vi the index of vertex i
     * @param vj the index of vertex j
     */
    void removeEdge(int vi, int vj);

    /**
     * update the weight of edge that is specified by ordered index pair {@literal <i, j>}
     * @param vi the index of vertex i
     * @param vj the index of vertex j
     */
    void updateEdge(int vi, int vj, double weight);

    /**
     * add a vertex to the graph
     * @param vi the index of vertex vi
     */
    void addVertex(int vi);

    /**
     * remove the vertex
     * @param vi the index of vertex which is to be removed
     */
    void removeVertex(int vi);

}
