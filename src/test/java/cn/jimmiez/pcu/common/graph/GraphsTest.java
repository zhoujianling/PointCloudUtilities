package cn.jimmiez.pcu.common.graph;

import cn.jimmiez.pcu.DataUtil;
import cn.jimmiez.pcu.common.graphics.Octree;
import org.junit.Test;

import javax.vecmath.Point3d;
import java.util.*;

import static cn.jimmiez.pcu.common.graph.BaseGraph.N;

import static org.junit.Assert.*;


public class GraphsTest {

    private static final double COMPARE_DOUBLE_TOLERANCE = 1E-5;

    @Test
    public void testEmpty() {
        BaseGraph empty = Graphs.empty();
        assertEquals(0, empty.vertices().size());
    }

    @Test
    public void testGraph() {
        // test create empty graph
        BaseGraph graph = Graphs.graph(new double[][]{}, false);
        assertEquals(0, graph.vertices().size());
        assertFalse(graph.isDirected());

        BaseGraph graph2 = Graphs.graph(new double[][]{}, true);
        assertEquals(0, graph2.vertices().size());
        assertTrue(graph2.isDirected());

        // test graph with 1 vertex
        BaseGraph graph3 = Graphs.graph(new double[][]{{N}}, false);
        assertEquals(1, graph3.vertices().size());
        assertEquals(0, graph3.adjacentVertices(0).size());
        assertFalse(graph3.isDirected());

        BaseGraph graph4 = Graphs.graph(new double[][]{{N}}, true);
        assertEquals(1, graph4.vertices().size());
        assertTrue(graph4.isDirected());

        // test create directed graph from matrix
        BaseGraph graph5 = Graphs.graph(new double[][]{
                {0,     2,  N,      N,     1},
                {2,     0,  2.3,    N,     0.2},
                {0,     3,  0,      0.2,   1},
                {0.8,   1.2,  N,      0,     1},
                {0.3,     3,  N,      N,     0}
        }, true);
        assertEquals(2, graph5.adjacentVertices(0).size());
        assertEquals(3, graph5.adjacentVertices(1).size());
        assertEquals(4, graph5.adjacentVertices(2).size());
        assertEquals(2.3, graph5.edgeWeight(1, 2), 1E-5);
        assertEquals(3, graph5.edgeWeight(2, 1), 1E-5);

        // test create undirected graph from matrix
        BaseGraph graph6 = Graphs.graph(new double[][]{
                {0,     2,  N,      N,     1},
                {2,     0,  3,      1.2,   0.2},
                {N,     3,  0,      N,   1},
                {N,   1.2,  N,      0,     1},
                {1,   0.2,  1,      1,     0}
        }, false);
        assertEquals(2, graph6.adjacentVertices(0).size());
        assertEquals(4, graph6.adjacentVertices(1).size());
        assertEquals(2, graph6.adjacentVertices(2).size());
        assertEquals(3, graph6.edgeWeight(1, 2), 1E-5);
        assertEquals(3, graph6.edgeWeight(2, 1), 1E-5);

        // test create graph from invalid input
        try {
            Graphs.graph(new double[][]{
                    {1, 2, 3, 4, 5},
                    {1, N, N, 5}
            }, false);
            fail();
        } catch (Exception e) {
        }
    }

    @Test
    public void testKnnGraph() {
        List<Point3d> vertices = new Vector<>();
        vertices.add(new Point3d(0, 0, 0));
        vertices.add(new Point3d(0, 0, 1));
        vertices.add(new Point3d(0, 1, 0));
        vertices.add(new Point3d(0, 1, 1));
        vertices.add(new Point3d(1, 0, 0));
        vertices.add(new Point3d(1, 0, 1));
        vertices.add(new Point3d(1, 1, 0));
        vertices.add(new Point3d(1, 1, 1));
        Octree octree = new Octree();
        octree.buildIndex(vertices);
        List<int[]> nnIndices = new Vector<>();
        for (int i = 0; i < vertices.size(); i ++)
            nnIndices.add(octree.searchNearestNeighbors(3, i));
        BaseGraph knnGraph = Graphs.knnGraph(vertices, nnIndices);
        assertEquals(1, knnGraph.edgeWeight(0, 1), COMPARE_DOUBLE_TOLERANCE);
        assertEquals(Double.POSITIVE_INFINITY, knnGraph.edgeWeight(0, 7), COMPARE_DOUBLE_TOLERANCE);
    }

    @Test
    public void testPrimMst() {
        // test null
        UndirectedGraph graph = null;
        UndirectedGraph mst = Graphs.minimalSpanningTree(graph);
        assertNull(mst);

        // test a graph with one vertex
        graph = new UndirectedGraph();
        graph.addVertex(0);
        mst = Graphs.minimalSpanningTree(graph);
        assertNotNull(mst);
        assertEquals(1, mst.vertices().size());

        // test a graph with two vertices, no edge
        graph = new UndirectedGraph();
        graph.addVertex(0);
        graph.addVertex(1);
        mst = Graphs.minimalSpanningTree(graph);
        assertNotNull(mst);
        assertEquals(1, mst.vertices().size());

        // test a graph with two vertices, one edge
        graph = new UndirectedGraph();
        graph.addVertex(0);
        graph.addVertex(1);
        graph.addEdge(0, 1, 1.0);
        mst = Graphs.minimalSpanningTree(graph);
        assertNotNull(mst);
        assertEquals(2, mst.vertices().size());

        // test a graph with three vertices, three edges
        graph = new UndirectedGraph();
        graph.addVertex(0);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(0, 2, 2.0);
        graph.addEdge(1, 2, 1.5);
        mst = Graphs.minimalSpanningTree(graph);
        assertNotNull(mst);
        assertEquals(3, mst.vertices().size());
        assertEquals(2, Graphs.edgesCountOf(mst) / 2);
        assertEquals(2.5, edgeWeightSum(mst), 1E-5);

        // test big graph
        int verticeNum = 300;
        UndirectedGraph undirectedGraph = generateUndirectedGraph(verticeNum);
        mst = Graphs.minimalSpanningTree(undirectedGraph);
        assertFalse("Minimal spanning tree should not contain cycle.", Graphs.containsCycle(mst));
        assertEquals(verticeNum, mst.vertices().size());
    }

    @Test
    public void testEdgesCount() {
        Random r = new Random(System.currentTimeMillis());

        // test empty graph
        BaseGraph empty = Graphs.empty();
        assertEquals(0, Graphs.edgesCountOf(empty));

        // test the case that <v0, v0>, <v1, v1>
        BaseGraph graph1 = Graphs.graph(new double[][] {{0, N}, {N, 0}}, false);
        assertEquals(0, Graphs.edgesCountOf(graph1));

        // test directed graph
        for (int iter = 0; iter < 3; iter ++) {
            int verticesCnt = 100 + r.nextInt(80);
            int edgesCnt = 0;
            DirectedGraph directedGraph = new DirectedGraph();
            for (int i = 0; i < verticesCnt; i ++) {
                directedGraph.addVertex(i);
            }
            int edgeVertexCnt = verticesCnt / 3 + r.nextInt(verticesCnt / 3);
            for (int j = 0; j < edgeVertexCnt; j ++) {
                int vi = (edgeVertexCnt + j) % verticesCnt;
                int adjacentCnt = 1 + r.nextInt(10);
                for (int k = 0; k < adjacentCnt; k ++) {
                    int vj = (vi + 1 + k) % verticesCnt;
                    directedGraph.addEdge(vi, vj, 1);
                    edgesCnt += 1;
                }
            }
            assertEquals(edgesCnt, Graphs.edgesCountOf(directedGraph));
        }

        // test undirected graph
        for (int iter = 0; iter < 3; iter ++) {
            int verticesCnt = 100 + r.nextInt(80);
            int edgesCnt = 0;
            UndirectedGraph undirectedGraph = new UndirectedGraph();
            for (int i = 0; i < verticesCnt; i ++) {
                undirectedGraph.addVertex(i);
            }
            int edgeVertexCnt = verticesCnt / 3 + r.nextInt(verticesCnt / 3);
            for (int j = 0; j < edgeVertexCnt; j ++) {
                int vi = (edgeVertexCnt + j) % verticesCnt;
                int adjacentCnt = 1 + r.nextInt(15);
                for (int k = 0; k < adjacentCnt; k ++) {
                    int vj = (vi + 1 + k) % verticesCnt;
                    if (undirectedGraph.edgeWeight(vi, vj) != BaseGraph.N) {
                        undirectedGraph.addEdge(vi, vj, 1);
                        edgesCnt += 1;
                    }
                }
            }
            assertEquals(edgesCnt * 2, Graphs.edgesCountOf(undirectedGraph));
        }

        // test static data
        BaseGraph graph = generateGraph();
        assertEquals(18, Graphs.edgesCountOf(graph));
    }

    @Test
    public void testContainsCycle() {
        Random r = new Random(System.currentTimeMillis());

        // test empty graph
        BaseGraph empty = Graphs.empty();
        assertFalse(Graphs.containsCycle(empty));

        // test undirected graph
        UndirectedGraph undirectedGraph = new UndirectedGraph();
        undirectedGraph.addVertex(0);
        undirectedGraph.addVertex(1);
        undirectedGraph.addVertex(2);
        undirectedGraph.addEdge(0, 1, 1);
        undirectedGraph.addEdge(1, 2, 3);
        undirectedGraph.addEdge(2, 0, 2);
        assertTrue(Graphs.containsCycle(undirectedGraph));

        // test undirected graph with two vertices
        UndirectedGraph undirectedGraph2 = new UndirectedGraph();
        undirectedGraph2.addVertex(0);
        undirectedGraph2.addVertex(1);
        undirectedGraph2.addEdge(0, 1, 1);
        assertFalse(Graphs.containsCycle(undirectedGraph2));

        // test un-connected undirected graph
        UndirectedGraph undirectedGraph3 = new UndirectedGraph();
        undirectedGraph3.addVertex(0);
        undirectedGraph3.addVertex(1);
        undirectedGraph3.addVertex(2);
        undirectedGraph3.addVertex(3);
        undirectedGraph3.addEdge(0, 1, 1);
        undirectedGraph3.addEdge(1, 2, 3);
        undirectedGraph3.addEdge(2, 3, 1);
        assertFalse(Graphs.containsCycle(undirectedGraph3));

        // test directed graph
        DirectedGraph directedGraph = new DirectedGraph();
        directedGraph.addVertex(0);
        directedGraph.addVertex(1);
        directedGraph.addVertex(2);
        directedGraph.addEdge(0, 1, 1);
        directedGraph.addEdge(1, 2, 3);
        directedGraph.addEdge(2, 0, 2);
        assertTrue(Graphs.containsCycle(directedGraph));

        // test directed graph with two vertices
        DirectedGraph directedGraph2 = new DirectedGraph();
        directedGraph2.addVertex(0);
        directedGraph2.addVertex(1);
        directedGraph2.addEdge(0, 1, 1);
        directedGraph2.addEdge(1, 0, 3);
        assertTrue(Graphs.containsCycle(directedGraph2));

        // test directed graph with two vertices
        DirectedGraph directedGraph3 = new DirectedGraph();
        directedGraph3.addVertex(0);
        directedGraph3.addVertex(1);
        directedGraph3.addEdge(0, 1, 1);
        assertFalse(Graphs.containsCycle(directedGraph3));

        // test directed graph with four vertices
        DirectedGraph directedGraph4 = new DirectedGraph();
        directedGraph4.addVertex(0);
        directedGraph4.addVertex(1);
        directedGraph4.addVertex(2);
        directedGraph4.addVertex(3);
        directedGraph4.addEdge(0, 1, 1);
        directedGraph4.addEdge(1, 2, 1);
        directedGraph4.addEdge(2, 3, 1);
        directedGraph4.addEdge(0, 3, 1);
        assertFalse(Graphs.containsCycle(directedGraph4));
//
//        // test big data
//        UndirectedGraph bigGraph = new UndirectedGraph();
//        int verticesCnt = 10000;
//        for (int i = 0; i < verticesCnt; i ++) {
//            bigGraph.addVertex(i);
//        }
//        for (int i = 0; i < verticesCnt; i ++) {
//            int adjacent = r.nextInt(verticesCnt);
//            if (adjacent != i
//                    && bigGraph.adjacentVertices(i).size() == 0
//                    && bigGraph.adjacentVertices(adjacent).size() == 0) {
//                bigGraph.addEdge(i, adjacent, 1);
//            }
//        }
//        assertFalse(Graphs.containsCycle(bigGraph));
    }

    @Test
    public void testConnectedComponent() {
        BaseGraph graph = generateGraph();
        List<List<Integer>> conns = Graphs.connectedComponents(graph);
        assertEquals(2,conns.size());

        BaseGraph graph2 = generateGraph2();
        conns = Graphs.connectedComponents(graph2);
        assertEquals(3,conns.size());

        conns = Graphs.connectedComponents(Graphs.empty());
        assertEquals(0,conns.size());

        BaseGraph fcg = generateGraph3();
        conns = Graphs.connectedComponents(fcg);
        assertEquals(1,conns.size());

        for (int i = 0; i < 5; i ++) {
            Random r = new Random(System.currentTimeMillis());
            int num = 1 + r.nextInt(7);
            BaseGraph randomGraph2 = DataUtil.generateRandomGraph(num, false);
            assertEquals(num, Graphs.connectedComponents(randomGraph2).size());
        }
    }


    @Test
    public void testSubGraph() {
        // test an empty graph
        BaseGraph empty = Graphs.empty();
        Set<Integer> vertices = new HashSet<>();
        BaseGraph subGraph1 = Graphs.subGraph(empty, vertices);
        assertEquals(0, subGraph1.vertices().size());

        // test if edgeWeight() method of sub-graph works well
        BaseGraph graph1 = generateGraph();
        vertices.clear();
        vertices.add(0);
        vertices.add(1);
        vertices.add(2);
        BaseGraph subGraph2 = Graphs.subGraph(graph1, vertices);
        assertEquals(3, subGraph2.vertices().size());
        assertEquals(0, subGraph2.edgeWeight(0, 0), COMPARE_DOUBLE_TOLERANCE);
        assertEquals(0, subGraph2.edgeWeight(2, 2), COMPARE_DOUBLE_TOLERANCE);
        assertEquals(0.5, subGraph2.edgeWeight(1, 2), COMPARE_DOUBLE_TOLERANCE);
        try {
            assertEquals(0, subGraph2.edgeWeight(0, 3), COMPARE_DOUBLE_TOLERANCE);
            fail();
        } catch (Exception e) {
        }

        // test input that contains all vertices
        vertices.clear();
        vertices.add(0);
        vertices.add(1);
        vertices.add(2);
        vertices.add(3);
        vertices.add(4);
        vertices.add(5);
        vertices.add(6);
        BaseGraph subGraph3 = Graphs.subGraph(graph1, vertices);
        assertEquals(graph1.vertices().size(), subGraph3.vertices().size());

        // test input that contains unexpected vertex
        vertices.add(7);
        try {
            Graphs.subGraph(graph1, vertices);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    private BaseGraph generateGraph() {
        final double[][] edges = new double[][] {
                {0,   3,   2,   N,   4,   N,   N},
                {3,   0,   0.5, 2,   0.8, N,   N},
                {2,   0.5, 0,   1,   N,   N,   N},
                {N,   2,   1,   0,   2,   N,   N},
                {4,   0.8, N,   2,   0,   N,   N},
                {N,   N,   N,   N,   N,   0,   0.3},
                {N,   N,   N,   N,   N,   0.3, 0},
        };
        return Graphs.graph(edges, false);
    }

    private BaseGraph generateGraph2() {
        final double[][] edges = new double[][] {
                {0,   N,  N},
                {N,   0,  N},
                {N,   N,  0},
        };
        return Graphs.graph(edges, false);
    }

    private BaseGraph generateGraph3() {
        Random random = new Random(System.currentTimeMillis());
        List<Point3d> vertices = new Vector<>();
        for (int i = 0; i < 1000; i ++) {
            vertices.add(new Point3d(random.nextDouble(), random.nextDouble(), random.nextDouble()));
        }
        return Graphs.fullConnectedGraph(vertices, false);
    }

    private UndirectedGraph generateUndirectedGraph(int vn) {
        Random random = new Random(System.currentTimeMillis());
        UndirectedGraph graph = new UndirectedGraph();
        for (int i = 0; i < vn; i ++) {
            graph.addVertex(i);
        }
        for (int i = 0; i < vn; i ++) {
            int vi = i;
            for (int j = 0; j < vn / 5; j ++) {
                double weight = random.nextDouble();
                int vj = random.nextInt(vn);
                if (vi == vj) continue;
                graph.addEdge(vi, vj, weight);
            }
        }
        return graph;
    }

    private double edgeWeightSum(UndirectedGraph graph) {
        double sum = 0;
        for (int vi : graph.vertices()) {
            for (int ai : graph.adjacentVertices(vi)) {
                sum += graph.edgeWeight(vi, ai);
            }
        }
        return sum / 2.0;
    }

}
