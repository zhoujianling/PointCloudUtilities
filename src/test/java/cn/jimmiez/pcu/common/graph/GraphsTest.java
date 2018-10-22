package cn.jimmiez.pcu.common.graph;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class GraphsTest {

    @Test
    public void connectedComponentTest() {
        Graph graph = genData();
        List<List<Integer>> conns = Graphs.connectedComponents(graph);
        Assert.assertTrue(conns.size() == 2);
    }

    private Graph genData() {

        final double N = Double.POSITIVE_INFINITY;
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
        return new Graph() {
            @Override
            public double edgeWeight(int i, int j) {
                return edges[i][j];
            }

            @Override
            public int verticesCount() {
                return 7;
            }

            @Override
            public int[] adjacentVertices(int i) {
                return adjacency[i];
            }
        };
    }
}
