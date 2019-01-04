package cn.jimmiez.pcu.common.graph;

import org.junit.Test;
import static org.junit.Assert.*;

public class UndirectedGraphTest {

    @Test
    public void testAddEdge() {
        UndirectedGraph graph = new UndirectedGraph();
        graph.addVertex(0);
        graph.addVertex(1);
        graph.addEdge(0, 1, 2);
        assertEquals(2, graph.edgeWeight(0, 1), 1E-5);
        assertEquals(2, graph.edgeWeight(1, 0), 1E-5);
    }

    @Test
    public void testRemoveEdge() {
        UndirectedGraph graph = new UndirectedGraph();
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addEdge(2, 3, 2);
        assertEquals(2, graph.edgeWeight(2, 3), 1E-5);
        assertEquals(2, graph.edgeWeight(3, 2), 1E-5);

        graph.removeEdge(3, 2);
        assertEquals(Graph.N, graph.edgeWeight(2, 3), 1E-5);
        assertEquals(Graph.N, graph.edgeWeight(3, 2), 1E-5);
    }

    @Test
    public void testRemoveVertex() {
        UndirectedGraph graph = new UndirectedGraph();
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addEdge(1, 2, 1);
        graph.addEdge(1, 3, 2);
        graph.addEdge(2, 3, 6);
        assertEquals(6, graph.edgeWeight(2, 3), 1E-5);
        assertEquals(6, graph.edgeWeight(3, 2), 1E-5);
        assertEquals(2, graph.edgeWeight(1, 3), 1E-5);

        graph.removeVertex(1);
        assertEquals(1, graph.adjacentVertices(2).size());
        assertEquals(1, graph.adjacentVertices(3).size());
    }
}
