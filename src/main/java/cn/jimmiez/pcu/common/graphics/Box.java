package cn.jimmiez.pcu.common.graphics;

import javax.vecmath.Point3d;

/**
 * an Axis-Align Bounding Box
 */
public class Box {

    private Point3d center;

    private double xExtent;

    private double yExtent;

    private double zExtent;

    public Box(Point3d center, double xAxis, double yAxis, double zAxis) {
        this.center = center;
        this.xExtent = xAxis;
        this.yExtent = yAxis;
        this.zExtent = zAxis;
    }

    public Point3d getCenter() {
        return center;
    }

    public void setCenter(Point3d center) {
        this.center = center;
    }

    public double getxExtent() {
        return xExtent;
    }

    public void setxExtent(double xExtent) {
        this.xExtent = xExtent;
    }

    public double getyExtent() {
        return yExtent;
    }

    public void setyExtent(double yExtent) {
        this.yExtent = yExtent;
    }

    public double getzExtent() {
        return zExtent;
    }

    public void setzExtent(double zExtent) {
        this.zExtent = zExtent;
    }
}
