package cn.jimmiez.pcu.common.graph;

public interface Graph {

    double edgeWeight(int i, int j);

    int verticesCount();

    int[] adjacentVertices(int i);

}
