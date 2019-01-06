package cn.jimmiez.pcu;

import cn.jimmiez.pcu.common.graph.*;

import javax.vecmath.Point3d;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DataUtil {

    public static List<Point3d> generateRandomData(int n, double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
        List<Point3d> data = new Vector<>();
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < n; i ++) {
            data.add(new Point3d(
                    minX + (maxX - minX) * random.nextDouble(),
                    minY + (maxY - minY) * random.nextDouble(),
                    minZ + (maxZ - minZ) * random.nextDouble()
            ));
        }
        return data;
    }

    /**
     * generate graph data for unit tests
     * @param conn the number of connected components
     * @param directed if this graph is directed
     * @return the graph
     */
    public static BaseGraph generateRandomGraph(int conn, boolean directed) {
        if (conn < 1) return Graphs.empty();
        Random random = new Random(System.currentTimeMillis());
        AtomicInteger generator = new AtomicInteger();
        Graph graph = directed ? new DirectedGraph() : new UndirectedGraph();
        List<List<Integer>> vertices = new ArrayList<>();
        for (int i = 0; i < conn; i ++) {
            List<Integer> componentVertices = new ArrayList<>();
            for (int j = 0; j < 15 + random.nextInt(20); j ++) {
                int id = generator.incrementAndGet();
                componentVertices.add(id);
                graph.addVertex(id);
            }
            vertices.add(componentVertices);
        }
        for (List<Integer> componentVertices : vertices) {
            for (int id : componentVertices) {
                for (int i = 0; i < componentVertices.size() / 3; i ++) {
                    int anotherId = componentVertices.get(random.nextInt(componentVertices.size()));
                    graph.addEdge(id, anotherId, 1);
                }
            }
        }
        return graph;
    }


}
