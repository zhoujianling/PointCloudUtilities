package cn.jimmiez.pcu.common.graph;

import org.junit.Test;
import static org.junit.Assert.*;

public class UndirectedGraphTest {

    @Test
    public void testAddVertex() {
        UndirectedGraph graph = new UndirectedGraph();
        assertEquals(0, graph.vertices().size());
        graph.addVertex(0);
        assertEquals(1, graph.vertices().size());
        // test add duplicate vertex
        graph.addVertex(0);
        assertEquals(1, graph.vertices().size());

        // test add more vertices
        graph = new UndirectedGraph();
        int vn = 300;
        for (int i = 0; i < vn; i ++) {
            graph.addVertex(i);
        }
        assertEquals(vn, graph.vertices().size());
    }

    @Test
    public void testAddEdge() {
        UndirectedGraph graph = new UndirectedGraph();
        graph.addVertex(0);
        graph.addVertex(1);
        graph.addEdge(0, 1, 2);
        assertEquals(2, graph.edgeWeight(0, 1), 1E-5);
        assertEquals(2, graph.edgeWeight(1, 0), 1E-5);

        try {
            graph.addEdge(0, 1, -2);
            fail();
        } catch (IllegalArgumentException e){}
        try {
            graph.addEdge(0, 11, 2);
            fail();
        } catch (IllegalArgumentException e){}
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

    @Test
    public void testIsDirected() {
        UndirectedGraph graph = new UndirectedGraph();
        assertFalse(graph.isDirected());
    }

    @Test
    public void testClear() {
        Graph graph = new UndirectedGraph();
        // test clear a graph with no vertex
        graph.clear();
        assertEquals(0, graph.vertices().size());

        graph = new UndirectedGraph();
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addEdge(1, 2, 1);
        graph.clear();
        assertEquals(0, graph.vertices().size());
        graph.addVertex(3);
        assertEquals(0, graph.adjacentVertices(3).size());

    }

    @Test
    public void testVertices() {
        UndirectedGraph graph = new UndirectedGraph();
        assertNotNull(graph.vertices());
        assertEquals(0, graph.vertices().size());

        graph.addVertex(0);
        assertEquals(1, graph.vertices().size());
    }

    @Test
    public void testUpdateEdge() {
        UndirectedGraph graph = new UndirectedGraph();

        // test a graph with two vertices
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addEdge(3, 4, 2);
        assertEquals(2, graph.edgeWeight(3, 4), 1E-5);
        graph.updateEdge(3, 4, 2.5);
        assertNotEquals(2, graph.edgeWeight(3, 4), 1E-5);
        assertEquals(2.5, graph.edgeWeight(3, 4), 1E-5);

        try {
            // test a graph with no vertex
            graph.updateEdge(3, 5, 3);
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            // test a graph with negative edge weight
            graph.updateEdge(3, 4, -33);
            fail();
        } catch (IllegalArgumentException e) {}
    }

    @Test
    public void testHasVertex() {
        UndirectedGraph graph = new UndirectedGraph();

        // test empty graph
        assertFalse(graph.hasVertex(0));

        // test graph with one vertex
        graph.addVertex(0);
        assertTrue(graph.hasVertex(0));
        assertFalse(graph.hasVertex(1));

        // test graph with many vertices
        graph = new UndirectedGraph();
        int vn = 1024;
        for (int i = 0; i < vn; i ++) {
            graph.addVertex(i);
            // test add duplicate vertices
            graph.addVertex(i);
        }
        for (int i = 0; i < vn; i ++) {
            assertTrue(graph.hasVertex(i));
            assertFalse(graph.hasVertex(-(i + 1)));
        }
        assertFalse(graph.hasVertex(vn + 39482093));
    }

}
