package cn.jimmiez.pcu.common.graph;

import cn.jimmiez.pcu.util.Pair;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static cn.jimmiez.pcu.common.graph.BaseGraph.N;
import static org.junit.Assert.*;

public class ShortestPathTest {

    @Test
    public void testDijkstra() {
        BaseGraph graph = genData();
        Map<Integer, Pair<List<Integer>, Double>> result = ShortestPath.dijkstra(graph, 0);

        assertEquals(0.0, result.get(0).getValue(), 1e-7);
        assertEquals(2.5, result.get(1).getValue(), 1e-7);
        assertEquals(2.0, result.get(2).getValue(), 1e-7);
        assertEquals(3.0, result.get(3).getValue(), 1e-7);
        assertEquals(3.3, result.get(4).getValue(), 1e-7);
        assertTrue(result.get(5).getValue() == Double.POSITIVE_INFINITY);

    }


    private BaseGraph genData() {
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
}
