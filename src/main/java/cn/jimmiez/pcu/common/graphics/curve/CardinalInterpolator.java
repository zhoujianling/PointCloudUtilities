package cn.jimmiez.pcu.common.graphics.curve;

import cn.jimmiez.pcu.common.graph.EntityGraph;
import cn.jimmiez.pcu.util.Function;

import javax.vecmath.Point3d;
import java.util.List;

public class CardinalInterpolator implements Interpolator{

    private double t = 0.1;

    public CardinalInterpolator() {
    }

    private Function<Double, Point3d> cardinalSplineFit(final Point3d p1, final Point3d p2, final Point3d p3, final Point3d p4) {
        return new Function<Double, Point3d>() {
            @Override
            public Point3d apply(Double u) {
                double s = (1 - t) / 2;
                double coef1 = (-s * Math.pow(u, 3) + 2 * s * Math.pow(u, 2) - s * u);
                double coef2 = ((2 - s) * Math.pow(u, 3) + (s - 3) * Math.pow(u, 2) + 1);
                double coef3 = ((s - 2) * Math.pow(u, 3) + (3 - 2 * s) * Math.pow(u, 2) + s * u);
                double coef4 = s * Math.pow(u, 3) - s * Math.pow(u, 2);
                float x = (float) (p1.x * coef1 + p2.x * coef2 + p3.x * coef3 + p4.x * coef4);
                float y = (float) (p1.y * coef1 + p2.y * coef2 + p3.y * coef3 + p4.y * coef4);
                float z = (float) (p1.z * coef1 + p2.z * coef2 + p3.z * coef3 + p4.z * coef4);
                return new Point3d(x, y, z);
            }
        };
    }

    @Override
    public void interpolate(EntityGraph<Point3d> points) {
        // // TODO: 2018/11/15
    }

    public void setT(double t) {
        this.t = t;
    }

    public double getT() {return t;}
}
