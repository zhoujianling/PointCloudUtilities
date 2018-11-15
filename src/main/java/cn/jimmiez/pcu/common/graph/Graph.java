package cn.jimmiez.pcu.common.graph;

import java.util.Collection;

/**
 * an abstract graph that support more operations
 */
public interface Graph extends GraphStatic {

    void addEdge(int vi, int vj, double weight);

    void removeEdge(int vi, int vj);

    void updateEdge(int vi, int vj, double weight);

    void addVertex(int vi);

    void removeVertex(int vi);

}
