package cn.jimmiez.pcu.alg.normal;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.List;

public interface NormalEstimator {

    /**
     * Estimate the normals of given point cloud
     * @param data the point cloud, not null
     * @return the list of normals
     */
    List<Vector3d> estimateNormals(List<Point3d> data);
}
