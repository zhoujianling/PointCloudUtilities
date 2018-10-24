package cn.jimmiez.pcu.alg.skel;

import javax.vecmath.Point3d;
import java.util.List;

public interface Skeletonization {
    Skeleton skeletonize(List<Point3d> pointCloud);
}
