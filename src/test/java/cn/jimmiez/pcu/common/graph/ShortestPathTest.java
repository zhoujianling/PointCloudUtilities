package cn.jimmiez.pcu.common.graph;

import javafx.util.Pair;
import org.junit.Test;

import java.util.List;
import static org.junit.Assert.*;

public class ShortestPathTest {

    @Test
    public void dijkstraTest() {
        Graph graph = genData();
        List<Pair<List<Integer>, Weight>> result = ShortestPath.dijkstra(graph, 0);


        assertEquals(0.0, result.get(0).getValue().val(), 1e-7);
        assertEquals(2.5, result.get(1).getValue().val(), 1e-7);
        assertEquals(2.0, result.get(2).getValue().val(), 1e-7);
        assertEquals(3.0, result.get(3).getValue().val(), 1e-7);
        assertEquals(3.3, result.get(4).getValue().val(), 1e-7);

    }

    private Graph genData() {

        final double N = Double.POSITIVE_INFINITY;
        final double[][] edges = new double[][] {
                {0,   3,   2,   N,   4  },
                {3,   0,   0.5, 2,   0.8},
                {2,   0.5, 0,   1,   N  },
                {N,   2,   1,   0,   2  },
                {4,   0.8, N,   2,   0  },
        };
        return new Graph() {
            @Override
            public double edgeWeight(int i, int j) {
                return edges[i][j];
            }

            @Override
            public int verticesCount() {
                return 5;
            }
        };
    }
}
