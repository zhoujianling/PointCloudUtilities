package cn.jimmiez.pcu.alg.skeleton;

import cn.jimmiez.pcu.model.Skeleton;

import javax.vecmath.Point3d;
import java.util.List;

/**
 * The interface for curve skeleton extraction algorithms.
 */
public interface Skeletonization {

    /**
     * Extract a curve skeleton from a set of scattered points(unorganized point cloud).
     * @param pointCloud the point cloud
     * @return the curve skeleton of the point cloud, represented as a acyclic graph
     */
    Skeleton skeletonize(List<Point3d> pointCloud);

}
