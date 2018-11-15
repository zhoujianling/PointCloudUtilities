package cn.jimmiez.pcu.alg.normal;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.List;

public interface NormalEstimator {

    List<Vector3d> estimateNormals(List<Point3d> data);
}
