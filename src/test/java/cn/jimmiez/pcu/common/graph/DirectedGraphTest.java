package cn.jimmiez.pcu.common.graph;

import org.junit.Test;
import static org.junit.Assert.*;

public class DirectedGraphTest {

    @Test
    public void testAddVertex() {
        Graph directedGraph = new DirectedGraph();
        assertEquals(0,directedGraph.vertices().size());
        directedGraph.addVertex(0);
        assertEquals(0, directedGraph.edgeWeight(0, 0),1E-5);
        assertEquals(1,directedGraph.vertices().size());

        directedGraph.addVertex(0);
        assertEquals(1,directedGraph.vertices().size());

        directedGraph.addVertex(1);
        assertEquals(2,directedGraph.vertices().size());
        assertEquals(Graph.N, directedGraph.edgeWeight(0, 1),1E-5);

        directedGraph.addVertex(-1);
        assertEquals(3,directedGraph.vertices().size());
        assertEquals(0, directedGraph.edgeWeight(0, 0),1E-5);
        assertEquals(Graph.N, directedGraph.edgeWeight(0, -1), 1E-5);
        assertEquals(0, directedGraph.edgeWeight(1, 1),1E-5);
        assertEquals(0, directedGraph.edgeWeight(-1, -1), 1E-5);

        directedGraph.addVertex(Integer.MAX_VALUE);
        assertEquals(4,directedGraph.vertices().size());
        assertEquals(0, directedGraph.edgeWeight(Integer.MAX_VALUE, Integer.MAX_VALUE), 1E-5);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testAddEdge1() {
        Graph directedGraph = new DirectedGraph();
        directedGraph.addEdge(0, 1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEdge2() {
        Graph directedGraph = new DirectedGraph();
        directedGraph.addVertex(0);
        directedGraph.addEdge(0, 1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEdge3() {
        Graph directedGraph = new DirectedGraph();
        directedGraph.addVertex(0);
        directedGraph.addVertex(1);
        directedGraph.addEdge(0, 1, -2);
    }

    @Test
    public void testAddEdge4() {
        Graph directedGraph = new DirectedGraph();
        directedGraph.addVertex(0);
        directedGraph.addVertex(1);
        directedGraph.addVertex(5);
        directedGraph.addVertex(6);
        directedGraph.addVertex(- 10000);

        assertEquals(0, directedGraph.adjacentVertices(0).size());
        assertEquals(0, directedGraph.adjacentVertices(1).size());
        assertEquals(0, directedGraph.adjacentVertices(5).size());
        assertEquals(0, directedGraph.adjacentVertices(6).size());

        directedGraph.addEdge(0, 0, 3);
        assertEquals(0, directedGraph.edgeWeight(0, 0),1E-5);
        directedGraph.addEdge(0, 1, 8);
        assertEquals(8, directedGraph.edgeWeight(0, 1),1E-5);
        assertEquals(1, directedGraph.adjacentVertices(0).size());
        assertEquals(0, directedGraph.adjacentVertices(1).size());
        assertEquals(Graph.N, directedGraph.edgeWeight(1, 0),1E-5);
        directedGraph.addEdge(1, 0, 4);
        assertEquals(4, directedGraph.edgeWeight(1, 0),1E-5);

        directedGraph.addEdge(0, 5, Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, directedGraph.edgeWeight(0, 5),1E-5);
        assertEquals(Graph.N, directedGraph.edgeWeight(5, 0),1E-5);
        directedGraph.addEdge(5, 0, Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, directedGraph.edgeWeight(5, 0),1E-5);

        directedGraph.addEdge(5, 6, 3);
        directedGraph.addEdge(6, 5, 3);
        directedGraph.addEdge(5, -10000, 8);
        assertEquals(3, directedGraph.edgeWeight(5, 6),1E-5);
        assertEquals(3, directedGraph.edgeWeight(6, 5),1E-5);
        assertEquals(8, directedGraph.edgeWeight(5, -10000),1E-5);

        assertEquals(3, directedGraph.adjacentVertices(5).size());
        assertEquals(1, directedGraph.adjacentVertices(6).size());

    }

    @Test(expected = IllegalArgumentException.class)
    public void testEdgeWeight1() {
        Graph directedGraph = new DirectedGraph();
        directedGraph.edgeWeight(1, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEdgeWeight2() {
        Graph directedGraph = new DirectedGraph();
        directedGraph.addVertex(3);
        directedGraph.edgeWeight(3, 4);
    }

    @Test
    public void testEdgeWeight3() {
        Graph directedGraph = new DirectedGraph();
        directedGraph.addVertex(0);
        directedGraph.addVertex(1);
        assertEquals(0, directedGraph.edgeWeight(0, 0),1E-5);
        assertEquals(0, directedGraph.edgeWeight(1, 1),1E-5);
        assertEquals(Graph.N, directedGraph.edgeWeight(0, 1),1E-5);
        assertEquals(Graph.N, directedGraph.edgeWeight(1, 0),1E-5);

        directedGraph.addEdge(1, 0, 3);
        assertEquals(3, directedGraph.edgeWeight(1, 0),1E-5);
    }

    @Test
    public void testRemoveEdge() {
        Graph directedGraph = new DirectedGraph();
        assertEquals(0, directedGraph.vertices().size());

        directedGraph.addVertex(-1);
        directedGraph.addVertex(0);
        directedGraph.addVertex(1);
        directedGraph.addVertex(2);
        directedGraph.addEdge(-1, 0, 2);
        directedGraph.addEdge(0, -1, 2);
        assertEquals(2, directedGraph.edgeWeight(0, -1), 1E-5);
        assertEquals(2, directedGraph.edgeWeight(-1, 0), 1E-5);
        directedGraph.removeEdge(0, -1);
        assertEquals(Graph.N, directedGraph.edgeWeight(0, -1), 1E-5);
        directedGraph.removeEdge(0, -1);
        assertEquals(Graph.N, directedGraph.edgeWeight(0, -1), 1E-5);
        directedGraph.removeEdge(-1, 0);
        assertEquals(Graph.N, directedGraph.edgeWeight(-1, 0), 1E-5);

        assertEquals(0, directedGraph.edgeWeight(0, 0), 1E-5);
        directedGraph.removeEdge(0, 0);
        assertEquals(0, directedGraph.edgeWeight(0, 0), 1E-5);
    }

    @Test
    public void testRemoveVertex() {
        Graph directedGraph = new DirectedGraph();
        assertEquals(0, directedGraph.vertices().size());
        directedGraph.removeVertex(-1);
        assertEquals(0, directedGraph.vertices().size());

        directedGraph.addVertex(5);
        directedGraph.addVertex(5);
        assertEquals(1, directedGraph.vertices().size());
        directedGraph.removeVertex(5);
        assertEquals(0, directedGraph.vertices().size());

        directedGraph.addVertex(1);
        directedGraph.addVertex(2);
        directedGraph.addEdge(1, 2, 3);
        directedGraph.addEdge(2, 1, 1);
        assertEquals(3, directedGraph.edgeWeight(1, 2), 1E-5);
        assertEquals(1, directedGraph.edgeWeight(2, 1), 1E-5);
        assertEquals(2, directedGraph.vertices().size());

        directedGraph.removeVertex(1);
        assertEquals(1, directedGraph.vertices().size());
        directedGraph.removeVertex(2);
        assertEquals(0, directedGraph.vertices().size());
        directedGraph.removeVertex(2);
        assertEquals(0, directedGraph.vertices().size());

        directedGraph.addVertex(1);
        directedGraph.addVertex(3);
        directedGraph.addVertex(4);
        directedGraph.addEdge(1, 3, 3);
        directedGraph.addEdge(1, 4, 4);
        directedGraph.addEdge(3, 4, 5);
        assertEquals(1, directedGraph.adjacentVertices(3).size());
        directedGraph.removeVertex(4);
        assertEquals(2, directedGraph.vertices().size());
        assertEquals(1, directedGraph.adjacentVertices(1).size());
        assertEquals(0, directedGraph.adjacentVertices(3).size());
    }

    @Test
    public void testClear() {
        Graph graph = new DirectedGraph();
        assertEquals(0, graph.vertices().size());
        // test clear a graph with no vertex
        graph.clear();
        assertEquals(0, graph.vertices().size());

        graph.addVertex(1);
        graph.clear();
        assertEquals(0, graph.vertices().size());
    }

}
