package cn.jimmiez.pcu.alg.sampler;

import cn.jimmiez.pcu.common.graphics.Octree2;

import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OctreeVoxelizer extends Octree2 {

    public List<OctreeNode> voxelize(List<Point3d> data, int depth) {
        super.buildIndex(data);
        List<OctreeNode> leaves = new ArrayList<>();
        for (Long index : super.octreeIndices.keySet()) {
            leaves.add(super.octreeIndices.get(index));
        }
        return leaves;
    }


}
