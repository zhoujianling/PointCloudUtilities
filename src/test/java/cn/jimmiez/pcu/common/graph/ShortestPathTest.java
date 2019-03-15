package cn.jimmiez.pcu.common.graph;

import cn.jimmiez.pcu.util.Pair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static cn.jimmiez.pcu.common.graph.BaseGraph.N;
import static org.junit.Assert.*;

public class ShortestPathTest {

    @Test
    public void testDijkstra() {
        // test null
        try {
            ShortestPath.dijkstra(null, 0);
            fail("Should throw exception.");
        } catch (NullPointerException npe) {}

        // test a graph without vertex
        BaseGraph testCase1 = new UndirectedGraph();
        try {
            ShortestPath.dijkstra(testCase1, 0);
        } catch (IllegalStateException e) {}

        // test a graph with only one vertex
        UndirectedGraph testCase2 = new UndirectedGraph();
        testCase2.addVertex(0);
        Map<Integer, Pair<List<Integer>, Double>> result2 = ShortestPath.dijkstra(testCase2, 0);
        assertEquals(1, result2.size());
        assertEquals(0, result2.get(0).getValue(), 1E-7);
        assertEquals(1, result2.get(0).getKey().size());

        // test a graph with only two vertex
        UndirectedGraph testCase3 = new UndirectedGraph();
        testCase3.addVertex(0);
        testCase3.addVertex(1);
        testCase3.addEdge(0, 1, 1);
        Map<Integer, Pair<List<Integer>, Double>> result3 = ShortestPath.dijkstra(testCase3, 0);
        assertEquals(2, result3.size());
        assertEquals(0, result3.get(0).getValue(), 1E-7);
        assertEquals(1., result3.get(1).getValue(), 1E-7);
        assertEquals(2, result3.get(1).getKey().size());

        // static test case
        BaseGraph graph = staticTestCase1();
        Map<Integer, Pair<List<Integer>, Double>> result = ShortestPath.dijkstra(graph, 0);

        assertEquals(0.0, result.get(0).getValue(), 1e-7);
        assertEquals(2.5, result.get(1).getValue(), 1e-7);
        assertEquals(2.0, result.get(2).getValue(), 1e-7);
        assertEquals(3.0, result.get(3).getValue(), 1e-7);
        assertEquals(3.3, result.get(4).getValue(), 1e-7);
        assertEquals(result.get(5).getValue(), Double.POSITIVE_INFINITY, 0.0);

        // test with generated graph
        int verticeNum = 10;
        List<Double> shortestDistances = new ArrayList<>();
        List<List<Integer>> shortestPaths = new ArrayList<>();
        UndirectedGraph generatedTestCase = generateTestGraphs(verticeNum, 5.0, shortestDistances, shortestPaths);
        Map<Integer, Pair<List<Integer>, Double>> result5 = ShortestPath.dijkstra(generatedTestCase, 0);
        assertEquals(shortestDistances.size(), result5.size());
        for (int i = 0; i < verticeNum; i ++) {
            double distance = shortestDistances.get(i);
            assertEquals(distance, result5.get(i).getValue(), 1E-6);
        }
    }


    private BaseGraph staticTestCase1() {
        final double[][] edges = new double[][] {
                {0,   3,   2,   N,   4,   N},
                {3,   0,   0.5, 2,   0.8, N},
                {2,   0.5, 0,   1,   N,   N},
                {N,   2,   1,   0,   2,   N},
                {4,   0.8, N,   2,   0,   N},
                {N,   N,   N,   N,   N,   0},
        };
        return Graphs.graph(edges, false);
    }

    /**
     * randomly generate a graph with specified number of vertices
     * this method will tell caller the minimal distance from the source vertex(vi = 0) to the
     * other vertices
     * @param verticeNum number of vertices
     * @param maxWeight the max weight of an edge
     * @param shortestDistances expects a non-null empty list, it is used to store the shortest distances
     * @param shortestPaths expects a non-null empty list, it is used to store the shortest paths
     * @return the generated graph
     */
    private UndirectedGraph generateTestGraphs(int verticeNum, double maxWeight,
                                               List<Double> shortestDistances, List<List<Integer>> shortestPaths) {
        UndirectedGraph graph = new UndirectedGraph();
        Random random = new Random(System.currentTimeMillis());
        if (verticeNum < 1) return graph;
        shortestDistances.clear();
        shortestPaths.clear();
        shortestDistances.add(0D);
        graph.addVertex(0);
        for (int vi = 1; vi < verticeNum; vi ++) {
            // vi is newly inserted vertex
            graph.addVertex(vi);
            shortestDistances.add(Graph.N);
            int edgeNum = vi < 3 ? vi : random.nextInt(vi / 2) + 1;
            for (int j = 0; j < edgeNum; j ++) {
                int vj = random.nextInt(vi);
                if (graph.edgeWeight(vi, vj) != Graph.N) continue;
                double edgeWeight = random.nextDouble() * maxWeight;
                graph.addEdge(vi, vj, edgeWeight);
                double minViDistance = shortestDistances.get(vi);
                double minVjDistance = shortestDistances.get(vj);
                if (minVjDistance + edgeWeight < minViDistance) {
                    shortestDistances.set(vi, minVjDistance + edgeWeight);
                }
            }
            for (int vk = 0; vk < vi; vk ++) {
                double minViDistance = shortestDistances.get(vi);
                double minVkDistance = shortestDistances.get(vk);
                double edgeWeight = graph.edgeWeight(vi, vk);
                if (minViDistance + edgeWeight < minVkDistance) {
                    shortestDistances.set(vk, minViDistance + edgeWeight);
                }
            }
        }
        return graph;
    }
}
