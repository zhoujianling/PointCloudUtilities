package cn.jimmiez.pcu.alg.sampler;

import cn.jimmiez.pcu.common.graphics.Octree;

import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.List;

public class OctreeVoxelizer extends Octree {


    /**
     * The constructor of OctreeVoxelizer.
     */
    public OctreeVoxelizer() { }

    /**
     *
     * @param data the point cloud
     * @param maxPointsPerNode the maximum number of points in an octree node
     * @return the list of octree cells
     */
    public List<OctreeNode> voxelize(List<Point3d> data, int maxPointsPerNode) {
        super.setMaxPointsPerNode(maxPointsPerNode);
        super.buildIndex(data);
        List<OctreeNode> leaves = new ArrayList<>();
        for (Long index : super.octreeIndices.keySet()) {
            leaves.add(super.octreeIndices.get(index));
        }
        return leaves;
    }


}
