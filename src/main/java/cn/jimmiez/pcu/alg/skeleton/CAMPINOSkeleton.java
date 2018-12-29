package cn.jimmiez.pcu.alg.skeleton;


import javax.vecmath.Point3d;
import java.util.List;

public class CAMPINOSkeleton implements Skeletonization {


    private void generateOctree() {
        // // TODO: 2018/11/20 a particular octree need to be implemented ...
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

}
