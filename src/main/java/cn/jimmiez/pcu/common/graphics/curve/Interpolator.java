package cn.jimmiez.pcu.common.graphics.curve;

import cn.jimmiez.pcu.common.graph.EntityGraph;

import javax.vecmath.Point3d;
import java.util.List;

public interface Interpolator {

    /**
     * Give a skeleton, represented as an acyclic graph, this method will interpolate
     * several points between every two points.
     * @param points the skeleton
     */
    void interpolate(EntityGraph<Point3d> points);
}
