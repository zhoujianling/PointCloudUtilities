package cn.jimmiez.pcu.alg.skel;

import cn.jimmiez.pcu.common.graph.Graph;

import javax.vecmath.Point3d;
import java.util.List;
import java.util.Vector;

public class Skeleton implements Graph {
    private List<Point3d> skeletonNodes;
    private List<List<Integer>> edges;

    @Override
    public double edgeWeight(int i, int j) {
        return skeletonNodes.get(i).distance(skeletonNodes.get(j));
    }

    @Override
    public int verticesCount() {
        return skeletonNodes.size();
    }

    @Override
    public List<Integer> adjacentVertices(int i) {
        return edges.get(i);
    }
}
