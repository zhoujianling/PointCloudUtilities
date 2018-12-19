package cn.jimmiez.pcu.common.graphics.shape;

import javax.vecmath.Point3d;

/**
 * an Axis-Align Bounding Box
 */
public class Box {

    protected Point3d center;

    protected double xExtent;

    protected double yExtent;

    protected double zExtent;

    public Box() {
        center = new Point3d();
        xExtent = 1d;
        yExtent = 1d;
        zExtent = 1d;
    }

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

    public double maxX() {
        return center.x + xExtent;
    }

    public double minX() {
        return center.x - xExtent;
    }

    public double maxY() {
        return center.y + yExtent;
    }

    public double minY() {
        return center.y - yExtent;
    }

    public double maxZ() {
        return center.z + zExtent;
    }

    public double minZ() {
        return center.z - zExtent;
    }

}
