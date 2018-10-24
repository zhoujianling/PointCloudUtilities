package cn.jimmiez.pcu.alg.skel;

import cn.jimmiez.pcu.common.graph.Graph;

import javax.vecmath.Point3d;
import java.util.List;

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
    public int[] adjacentVertices(int i) {
        int[] result = new int[edges.get(i).size()];
        for (int ii = 0; ii < result.length; ii ++) {
            int index = edges.get(i).get(ii);
            result[ii] = index;
        }
        return result;
    }
}
