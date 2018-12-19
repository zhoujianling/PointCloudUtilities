package cn.jimmiez.pcu.common.graphics.shape;

import javax.vecmath.Point3d;

public class Sphere {

    private Point3d center;

    private double radius;

    public Sphere(Point3d center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    public Point3d getCenter() {
        return center;
    }

    public void setCenter(Point3d center) {
        this.center = center;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
