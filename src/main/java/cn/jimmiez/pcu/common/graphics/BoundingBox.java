package cn.jimmiez.pcu.common.graphics;

import javax.vecmath.Point3d;
import java.util.List;

public class BoundingBox {

    private double minX = Double.NaN;
    private double maxX = Double.NaN;
    private double minY = Double.NaN;
    private double maxY = Double.NaN;
    private double minZ = Double.NaN;
    private double maxZ = Double.NaN;

    public BoundingBox() {
        this(0, 1, 0, 1, 0, 1);
    }

    public BoundingBox(double minx, double maxx, double miny, double maxy, double minz, double maxz) {
        if (minx > maxx) {
            throw new IllegalArgumentException("minX larger than maxX");
        }
        if (miny > maxy) {
            throw new IllegalArgumentException("minY larger than maxY");
        }
        if (minz > maxz) {
            throw new IllegalArgumentException("minZ larger than maxZ");
        }
        this.minX = minx;
        this.maxX = maxx;
        this.minY = miny;
        this.maxY = maxy;
        this.minZ = minz;
        this.maxZ = maxz;
    }

    private static BoundingBox empty() {
        return new BoundingBox(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }

    public static BoundingBox of(List<Point3d> data) {
        if (data.size() < 1) return empty();
        BoundingBox box = new BoundingBox();
        box.minX =  data.get(0).x;
        box.maxX =  data.get(0).x;
        box.minY =  data.get(0).y;
        box.maxY =  data.get(0).y;
        box.minZ =  data.get(0).z;
        box.maxZ =  data.get(0).z;
        for (Point3d p : data) {
            box.minX = Math.min(box.minX, p.x);
            box.maxX = Math.max(box.maxX, p.x);
            box.minY = Math.min(box.minY, p.y);
            box.maxY = Math.max(box.maxY, p.y);
            box.minZ = Math.min(box.minZ, p.z);
            box.maxZ = Math.max(box.maxZ, p.z);
        }
        return box;
    }

    public double minX() {return minX;}

    public double maxX() {return maxX;}

    public double minY() {return minY;}

    public double maxY() {return maxY;}

    public double minZ() {return minZ;}

    public double maxZ() {return maxZ;}

    public double diagonalLength() {
        return Math.sqrt((maxX - minX) * (maxX - minX) + (maxY - minY) * (maxY - minY) + (maxZ - minZ) * (maxZ - minZ));
    }
}
