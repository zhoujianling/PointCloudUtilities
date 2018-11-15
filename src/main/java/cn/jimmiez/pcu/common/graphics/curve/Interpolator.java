package cn.jimmiez.pcu.common.graphics.curve;

import javax.vecmath.Point3d;
import java.util.List;

public interface Interpolator {

    List<Point3d> interpolate(List<Point3d> points);
}
