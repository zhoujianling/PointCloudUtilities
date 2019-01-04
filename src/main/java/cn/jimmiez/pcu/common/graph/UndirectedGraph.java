package cn.jimmiez.pcu.common.graph;

import java.util.*;

public class UndirectedGraph implements Graph {

    private Map<VertexPair, Double> edges = new HashMap<>();

    private Map<Integer, Set<Integer>> adjacentVertices = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEdge(int vi, int vj, double weight) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeEdge(int vi, int vj) {

    }

    @Override
    public void updateEdge(int vi, int vj, double weight) {

    }

    @Override
    public void addVertex(int vi) {
        adjacentVertices.put(vi, new HashSet<Integer>());
    }

    @Override
    public void removeVertex(int vi) {

    }

    @Override
    public double edgeWeight(int i, int j) {
        return 0;
    }

    @Override
    public Collection<Integer> adjacentVertices(int i) {
        return null;
    }

    @Override
    public Collection<Integer> vertices() {
        return adjacentVertices.keySet();
    }

    public void clear() {
        edges.clear();
        adjacentVertices.clear();
    }
}
