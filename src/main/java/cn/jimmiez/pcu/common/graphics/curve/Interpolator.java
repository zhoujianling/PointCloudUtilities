package cn.jimmiez.pcu.common.graphics.curve;

import cn.jimmiez.pcu.common.graph.EntityGraph;

import javax.vecmath.Point3d;
import java.util.List;

public interface Interpolator {

    void interpolate(EntityGraph<Point3d> points);
}
