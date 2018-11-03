package cn.jimmiez.pcu.alg.skel;


import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.List;
import cn.jimmiez.pcu.common.graph.DirectedGraph;
import cn.jimmiez.pcu.io.ply.WriteToPly;

/**
 * Use a directed acyclic graph for representing a curve skeleton
 */
public class Skeleton extends DirectedGraph{
    private List<Point3d> skeletonNodes = new ArrayList<>();

    public void addNode(Point3d p) {
        skeletonNodes.add(p);
        super.addVertex(skeletonNodes.size() - 1);
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

    @Override
    public final void addVertex(int vi) {
        // do nothing
    }

    @Override
    public final void removeVertex(int vi) {
        // do nothing
    }

    public List<Point3d> getSkeletonNodes() {
        return skeletonNodes;
    }

    @WriteToPly(element = "nodes", properties = {"x", "y", "z"}, typeNames = {"double", "double", "double"})
    public List<double[]> nodes4Ply() {
        List<double[]> result = new ArrayList<>();
        for (Point3d p : skeletonNodes) {
            result.add(new double[] {p.x, p.y, p.z});
        }
        return result;
    }

//    @WriteToPly(element = "edges", properties = {"list"}, typeNames = {"double", "double", "double"})
//    public List<int[]> edges4Ply() {
//        List<int[]> result = new ArrayList<>();
//        for (Point3d p : edges) {
//            result.add(new int[] {p.x, p.y, p.z});
//        }
//        return result;
//
//    }
}
