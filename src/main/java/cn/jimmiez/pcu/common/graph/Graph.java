package cn.jimmiez.pcu.common.graph;

/**
 * an abstract directed graph that support more operations
 */
public interface Graph extends GraphStatic {

    void addEdge(int vi, int vj, double weight);

    void removeEdge(int vi, int vj);

    void updateEdge(int vi, int vj, double weight);

    int addVertex();

    void removeVertex(int vi);

}
