package cn.jimmiez.pcu.common.graph;

import java.util.*;

/**
 * A undirected graph in which the edges is store in a HashMap.
 */
public class UndirectedGraph implements Graph {

    /** the edges of this graph, weight is represented using a double value **/
    protected Map<Integer, Map<Integer, Double>> edges = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEdge(int vi, int vj, double weight) {
        if (edges.get(vi) == null)
            throw new IllegalArgumentException("Invalid index of vi");
        if (edges.get(vj) == null)
            throw new IllegalArgumentException("Invalid index of vj");
        if (weight < 0)
            throw new IllegalArgumentException("Weight must be non-negative.");
        if (vi == vj) return;
        edges.get(vi).put(vj, weight);
        edges.get(vj).put(vi, weight);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeEdge(int vi, int vj) {
        if (edges.get(vi) == null)
            throw new IllegalArgumentException("Invalid index of vi");
        if (edges.get(vj) == null)
            throw new IllegalArgumentException("Invalid index of vj");
        if (vi == vj) return;
        edges.get(vi).remove(vj);
        edges.get(vj).remove(vi);
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
    public void clear() {
        edges.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Integer> vertices() {
        return edges.keySet();
    }

    @Override
    public boolean isDirected() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double edgeWeight(int i, int j) {
        if (i == j) return 0;
        if (edges.get(i) == null) throw new IllegalArgumentException("Invalid index of i");
        if (edges.get(j) == null) throw new IllegalArgumentException("Invalid index of j");
        Double weight = edges.get(i).get(j);
        if (weight == null) {
            return N;
        }
        return weight;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Integer> adjacentVertices(int i) {
        if (edges.get(i) == null) throw new IllegalArgumentException("Invalid index of vi");
        return edges.get(i).keySet();
    }

    /**
     * determine if this graph has vertex vi
     * @param vi the index of vertex
     * @return if the graph contains vertex vi
     */
    public boolean hasVertex(int vi) {
        return edges.containsKey(vi);
    }

}
