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
    public void testEdgesCount() {
        Random r = new Random(System.currentTimeMillis());

        // test empty graph
        BaseGraph empty = Graphs.empty();
        assertEquals(0, Graphs.edgesCountOf(empty));

        // test the case that <v0, v0>, <v1, v1>
        BaseGraph graph1 = Graphs.graph(new double[][] {
                {0, N}, {N, 0}},
                new int[][] {{},{}});
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
        // test empty graph
//        BaseGraph empty = Graphs.empty();
//        assertFalse(Graphs.containsCycle(empty));

        // test
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
        final int[][] adjacency = new int[][] {
                {1, 2, 4},
                {0, 2, 3, 4},
                {0, 1, 3},
                {1, 2, 4},
                {0, 1, 3},
                {6},
                {5},
        };
        return Graphs.graph(edges, adjacency);
    }

    private BaseGraph generateGraph2() {
        final double[][] edges = new double[][] {
                {0,   N,  N},
                {N,   0,  N},
                {N,   N,  0},
        };
        final int[][] adjacency = new int[][] {
                {},
                {},
                {},
        };
        return Graphs.graph(edges, adjacency);
    }

    private BaseGraph generateGraph3() {
        Random random = new Random(System.currentTimeMillis());
        List<Point3d> vertices = new Vector<>();
        for (int i = 0; i < 1000; i ++) {
            vertices.add(new Point3d(random.nextDouble(), random.nextDouble(), random.nextDouble()));
        }
        return Graphs.fullConnectedGraph(vertices);
    }

}
