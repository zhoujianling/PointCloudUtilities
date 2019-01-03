package cn.jimmiez.pcu.common.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DirectedGraph implements Graph {

    protected Map<Integer, Map<Integer, Double>> edges = new HashMap<>();


    /**
     * {@inheritDoc}
     */
    @Override
    public void addEdge(int vi, int vj, double weight) {
        if (vi >= edges.size() || vj >= edges.size()) {
            throw new IllegalArgumentException("Invalid vertex index.");
        }
        edges.get(vi).put(vj, weight);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeEdge(int vi, int vj) {
        if (vi >= edges.size() || vj >= edges.size()) {
            throw new IllegalArgumentException("Invalid vertex index.");
        }
        edges.get(vi).remove(vj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateEdge(int vi, int vj, double weight) {
        addEdge(vi, vj, weight);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addVertex(int vi) {
        edges.put(vi, new HashMap<Integer, Double>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeVertex(int vi) {
        for (int v : edges.keySet()) {
            if (edges.get(v).keySet().contains(vi)) {
                edges.get(v).remove(vi);
            }
        }
        edges.remove(vi);
    }

    @Override
    public Collection<Integer> vertices() {
        return edges.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double edgeWeight(int i, int j) {
        Double weight = edges.get(i).get(j);
        if (weight == null) {
            return N;
        }
        return weight;
    }

    @Override
    public Collection<Integer> adjacentVertices(int i) {
        return edges.get(i).keySet();
    }
}
