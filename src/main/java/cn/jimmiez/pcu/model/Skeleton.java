package cn.jimmiez.pcu.model;


import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.jimmiez.pcu.common.graph.DirectedGraph;
import cn.jimmiez.pcu.common.graph.UndirectedGraph;
import cn.jimmiez.pcu.io.ply.PlyPropertyType;
import cn.jimmiez.pcu.io.ply.ReadFromPly;
import cn.jimmiez.pcu.io.ply.WriteListToPly;
import cn.jimmiez.pcu.io.ply.WriteScalarToPly;

/**
 * Use a undirected acyclic graph for representing a curve skeleton
 */
public class Skeleton extends DirectedGraph {
    private List<Point3d> skeletonNodes = new ArrayList<>();

    public int addNode(Point3d p) {
        skeletonNodes.add(p);
        super.addVertex(skeletonNodes.size() - 1);
        return skeletonNodes.size() - 1;
    }

    public void removeNode(Point3d p) {
        int index = skeletonNodes.indexOf(p);
        if (index == -1) {
            System.err.println("Skeleton::removeNode() Cannot find the point.");
            return;
        }
        skeletonNodes.remove(index);
        super.removeVertex(index);
    }

    public void clear() {
        skeletonNodes.clear();
        edges.clear();
    }

    public List<Point3d> getSkeletonNodes() {
        return skeletonNodes;
    }


    @SuppressWarnings("unchecked")
    @WriteScalarToPly(element = "nodes", properties = {"x", "y", "z"}, type = PlyPropertyType.DOUBLE)
    @ReadFromPly(element = "nodes", properties = {"x", "y", "z"})
    public List<double[]> nodes4Ply() {
        List<double[]> result = new ArrayList<>();
        for (Point3d p : skeletonNodes) {
            result.add(new double[] {p.x, p.y, p.z});
        }
        return result;
    }

    @WriteListToPly(element = "edges", property = "node_index")
    @ReadFromPly(element = "edges", properties = "node_index")
    public List<int[]> edges4Ply() {
        List<int[]> result = new ArrayList<>();
        for (int vi = 0; vi < skeletonNodes.size(); vi ++) {
            Map<Integer, Double> edge = edges.get(vi);
            int[] edgesVi = new int[edge.size()];
            int cnt = 0;
            for (int vj : edge.keySet()) {
                edgesVi[cnt ++] = vj;
            }
            result.add(edgesVi);
        }
        return result;

    }

}
