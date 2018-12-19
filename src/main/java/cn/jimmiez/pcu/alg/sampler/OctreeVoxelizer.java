package cn.jimmiez.pcu.alg.sampler;

import cn.jimmiez.pcu.common.graphics.Octree;

import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OctreeVoxelizer extends Octree {

    public List<OctreeNode> voxelize(List<Point3d> data, int depth) {
        super.buildIndex(data, depth);
        List<OctreeNode> allNodes = new ArrayList<>();
        List<OctreeNode> leaves = new ArrayList<>();
        allNodes.add(this.root);
        int pointer;
        for (pointer = 0; pointer < allNodes.size(); pointer ++) {
            OctreeNode[] children = allNodes.get(pointer).getChildren();
            if (children == null) break;
            allNodes.addAll(Arrays.asList(children));
        }
        for (int i = pointer; i < allNodes.size(); i ++) {
            OctreeNode node = allNodes.get(i);
            if (node.getIndices().size() > 0) {
                leaves.add(node);
            }
        }
        return leaves;
    }


}
