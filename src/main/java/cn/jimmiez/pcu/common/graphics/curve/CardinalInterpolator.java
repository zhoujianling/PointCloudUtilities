package cn.jimmiez.pcu.common.graphics.curve;

import cn.jimmiez.pcu.common.graph.EntityGraph;
import cn.jimmiez.pcu.common.graph.Graphs;
import cn.jimmiez.pcu.common.graph.VertexPair;
import cn.jimmiez.pcu.util.Function;
import cn.jimmiez.pcu.util.VectorUtil;

import javax.vecmath.Point3d;
import java.util.*;

public class CardinalInterpolator implements Interpolator{

    /**
     * the factor that controls how tight the resulting spline is.
     * If tension < 0, the spline is loose.
     * If tension > 0, the spline is tight.
     */
    private double tension = 0.1;

    /** the number of points that are interpolated between every two points **/
    private int num;

    /**
     * Construct a default cardinal interpolator
     */
    public CardinalInterpolator() {
        this(5);
    }

    /**
     * Construct cardinal interpolator
     * @param n the number of interpolated points between every two points, should be larger than 0
     */
    public CardinalInterpolator(int n) {
        if (n < 1) throw new IllegalArgumentException("n should be larger than 0");
        this.num = n;
    }

    private Function<Double, Point3d> cardinalSplineFit(final Point3d p1, final Point3d p2, final Point3d p3, final Point3d p4) {
        return new Function<Double, Point3d>() {
            @Override
            public Point3d apply(Double u) {
                double s = (1 - tension) / 2;
                double coef1 = (-s * Math.pow(u, 3) + 2 * s * Math.pow(u, 2) - s * u);
                double coef2 = ((2 - s) * Math.pow(u, 3) + (s - 3) * Math.pow(u, 2) + 1);
                double coef3 = ((s - 2) * Math.pow(u, 3) + (3 - 2 * s) * Math.pow(u, 2) + s * u);
                double coef4 = s * Math.pow(u, 3) - s * Math.pow(u, 2);
                float x = (float) (p1.x * coef1 + p2.x * coef2 + p3.x * coef3 + p4.x * coef4);
                float y = (float) (p1.y * coef1 + p2.y * coef2 + p3.y * coef3 + p4.y * coef4);
                float z = (float) (p1.z * coef1 + p2.z * coef2 + p3.z * coef3 + p4.z * coef4);
                return new Point3d(x, y, z);
            }
        };
    }

    // may return a NaN or INF
    private Point3d adjacentPoint(EntityGraph<Point3d> graph, int index, int vj) {
        Collection<Integer> adjacentIndices = graph.adjacentVertices(index);
        if (adjacentIndices.size() < 1) return null;
        if (adjacentIndices.size() == 1)
            return graph.getVertex(index);
        double x = .0;
        double y = .0;
        double z = .0;
        int cnt = 0;
        for (int adjacentIndex : adjacentIndices) {
            if (adjacentIndex == vj) continue;
            x += graph.getVertex(index).x;
            y += graph.getVertex(index).y;
            z += graph.getVertex(index).z;
            cnt += 1;
        }
        return new Point3d(x / cnt, y / cnt, z / cnt);
    }

    // insert the points between every two adjacent graph vertices
    private void insertPoints(EntityGraph<Point3d> graph, Map<VertexPair, List<Point3d>> inserted) {
        for (VertexPair vp : inserted.keySet()) {
            int endIndexI = vp.getVi();
            int endIndexJ = vp.getVj();
            graph.removeEdge(endIndexI, endIndexJ);
            int vi = endIndexI;
            int vj;
            List<Point3d> ps = inserted.get(vp);
            for (Point3d p : ps) {
                vj = graph.addVertex(p);
                graph.addEdge(vi, vj, graph.getVertex(vi).distance(p));
                vi = vj;
            }
            graph.addEdge(vi, endIndexJ, graph.getVertex(vi).distance(graph.getVertex(endIndexJ)));
        }

    }

    private void insertPointsIntoVertexPair(EntityGraph<Point3d> graph, Set<Integer> validIndices, VertexPair vp, List<Point3d> ps) {
        int vi = vp.getVi();
        int vj = vp.getVj();
        Point3d p1 = adjacentPoint(graph, vi, vj);
        Point3d p2 = graph.getVertex(vi);
        Point3d p3 = graph.getVertex(vj);
        Point3d p4 = adjacentPoint(graph, vj, vi);
        if (! VectorUtil.validPoint(p2) || ! VectorUtil.validPoint(p3)) return;
        if (! VectorUtil.validPoint(p1)) p1 = new Point3d(p2);
        if (! VectorUtil.validPoint(p4)) p4 = new Point3d(p3);
        for (int i = 0; i < num; i ++) {
            Function<Double, Point3d> function = cardinalSplineFit(p1, p2, p3, p4);
            ps.add(function.apply((i + 1) * 1.0 / (num + 1)));
        }
    }

    private void interpolateImpl(EntityGraph<Point3d> graph, List<Integer> vertexIndices) {
        Set<Integer> validVertices = new HashSet<>();
        Set<VertexPair> visitedEdges = new HashSet<>();
        for (Integer index : vertexIndices) {
            Point3d point = graph.getVertex(index);
            if (VectorUtil.validPoint(point)) validVertices.add(index);
        }
        if (validVertices.size() < 1) return;
        int startIndex = validVertices.iterator().next();
        List<VertexPair> edgesQueue = new ArrayList<>();
        Map<VertexPair, List<Point3d>> insertedMap = new HashMap<>();
        int nextIndex = graph.adjacentVertices(startIndex).iterator().next();
        edgesQueue.add(new VertexPair(startIndex, nextIndex));
        for (int i = 0; i < edgesQueue.size(); i ++) {
            VertexPair vp = edgesQueue.get(i);
            // perform interpolation
            List<Point3d> ps = new ArrayList<>();
            insertedMap.put(vp, ps);
            insertPointsIntoVertexPair(graph, validVertices, vp, ps);
            // ===========
            visitedEdges.add(vp);
            int vi = vp.getVi();
            int vj = vp.getVj();
            for (int vii : graph.adjacentVertices(vi)) {
                VertexPair vpi = new VertexPair(vi, vii);
                if (! visitedEdges.contains(vpi)) {
                    edgesQueue.add(vpi);
                }
            }
            for (int vjj : graph.adjacentVertices(vj)) {
                VertexPair vpj = new VertexPair(vj, vjj);
                if (! visitedEdges.contains(vpj)) {
                    edgesQueue.add(vpj);
                }
            }
        }

        insertPoints(graph, insertedMap);
    }


    @Override
    public void interpolate(EntityGraph<Point3d> graph) {
        if (graph.isDirected()) {
            throw new UnsupportedOperationException("Directed graph is currently unsupported");
        }
        List<List<Integer>> conns = Graphs.connectedComponents(graph);
        for (List<Integer> conn : conns) {
            if (conn.size() < 2) continue;
            interpolateImpl(graph, conn);
        }
    }

    public void setTension(double tension) {
        this.tension = tension;
    }

    public double getTension() {return tension;}

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
