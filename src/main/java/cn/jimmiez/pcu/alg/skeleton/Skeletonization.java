package cn.jimmiez.pcu.alg.skeleton;

import cn.jimmiez.pcu.model.Skeleton;

import javax.vecmath.Point3d;
import java.util.List;

public interface Skeletonization {
    Skeleton skeletonize(List<Point3d> pointCloud);
}
