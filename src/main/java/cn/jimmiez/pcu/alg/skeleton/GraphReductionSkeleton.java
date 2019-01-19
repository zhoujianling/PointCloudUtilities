package cn.jimmiez.pcu.alg.skeleton;


import cn.jimmiez.pcu.alg.sampler.OctreeVoxelizer;
import cn.jimmiez.pcu.common.graphics.Octree;
import cn.jimmiez.pcu.model.Skeleton;

import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.List;

public class GraphReductionSkeleton implements Skeletonization {

    /** raw point cloud **/
    private List<Point3d> points = new ArrayList<>();

    /** the list of octree cells **/
    private List<Octree.OctreeNode> cells = null;

    /** the down-sampled points **/
    private List<Point3d> samplePoints = null;

    /** **/
    private double ma = 20;


    private void generateOctree() {
        OctreeVoxelizer voxelizer = new OctreeVoxelizer();
    }

    private void extractGraph() {

    }

    private void graphReduction() {

    }

    private void init(List<Point3d> pointCloud) {
        this.points.clear();
        this.points.addAll(pointCloud);
    }

    @Override
    public Skeleton skeletonize(List<Point3d> pointCloud) {
        init(pointCloud);
        generateOctree();
        extractGraph();
        graphReduction();
        return null;
    }

}
