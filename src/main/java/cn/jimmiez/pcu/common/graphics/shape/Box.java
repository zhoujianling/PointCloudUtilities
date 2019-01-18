package cn.jimmiez.pcu.common.graphics.shape;

import javax.vecmath.Point3d;

import static java.lang.Math.abs;

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

    /**
     * the constructor of Box
     * @throws IllegalArgumentException x/z/z extent is NaN or negative
     * @param center the center of box, the x/y/z cannot be NaN
     * @param xAxis the x-extent of box, cannot be NaN, must be positive
     * @param yAxis the y-extent of box, cannot be NaN, must be positive
     * @param zAxis the z-extent of box, cannot be NaN, must be positive
     */
    public Box(Point3d center, double xAxis, double yAxis, double zAxis) {
        if (Double.isNaN(center.x)
                || Double.isNaN(center.y)
                || Double.isNaN(center.z)
                || Double.isNaN(xAxis)
                || Double.isNaN(yAxis)
                || Double.isNaN(zAxis)) {
            throw new IllegalArgumentException("The param cannot be Double.NaN");
        }
        if (xAxis < 0 || yAxis < 0 || zAxis < 0) throw new IllegalArgumentException("The x/y/z extent of box should be positive");
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

    private static final double TOLERANCE = 1E-5;

    /**
     * test if this box contains the specified point
     * @param point the user-specified point
     * @return test result
     */
    public boolean contains(Point3d point) {
        return contains(point, TOLERANCE);
    }

    /**
     * test if this box contains the specified point
     * @param point the user-specified point
     * @param tolerance the error within the tolerance is acceptable
     * @return test result
     */
    public boolean contains(Point3d point, double tolerance) {
        return (point.x <= tolerance + xExtent + center.x)
                && (point.x >= center.x - xExtent - tolerance)
                && (point.y <= center.y + yExtent + tolerance)
                && (point.y >= center.y - yExtent - tolerance)
                && (point.z <= center.z + zExtent + tolerance)
                && (point.z >= center.z - zExtent - tolerance);
    }

}
