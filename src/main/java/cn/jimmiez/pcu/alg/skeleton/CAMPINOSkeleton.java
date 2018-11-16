package cn.jimmiez.pcu.alg.skeleton;


import cn.jimmiez.pcu.common.graphics.Octree;

import javax.vecmath.Point3d;
import java.util.List;

public class CAMPINOSkeleton implements Skeletonization {


    private void generateOctree() {

    }

    private void extractGraph() {

    }

    private void graphReduction() {

    }

    @Override
    public Skeleton skeletonize(List<Point3d> pointCloud) {
        generateOctree();
        extractGraph();
        graphReduction();
        return null;
    }

    private class AdaptiveOctree extends Octree {

    }
}
