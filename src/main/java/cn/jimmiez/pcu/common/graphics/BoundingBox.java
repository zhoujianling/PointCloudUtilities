package cn.jimmiez.pcu.common.graphics;

import javax.vecmath.Point3d;
import java.util.List;

public class BoundingBox extends Box {

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
        center = new Point3d((minx + maxx) / 2, (miny + maxy) / 2, (minz + maxz) / 2);
        xExtent = maxx - center.x;
        yExtent = maxy - center.y;
        zExtent = maxz - center.z;
    }

    private static BoundingBox empty() {
        return new BoundingBox(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }

    public static BoundingBox of(List<Point3d> data) {
        if (data.size() < 1) return empty();
        double minX =  data.get(0).x;
        double maxX =  data.get(0).x;
        double minY =  data.get(0).y;
        double maxY =  data.get(0).y;
        double minZ =  data.get(0).z;
        double maxZ =  data.get(0).z;
        for (Point3d p : data) {
            minX = Math.min(minX, p.x);
            maxX = Math.max(maxX, p.x);
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
            minZ = Math.min(minZ, p.z);
            maxZ = Math.max(maxZ, p.z);
        }
        return new BoundingBox(minX, maxX, minY, maxY, minZ, maxZ);
    }

    public double diagonalLength() {
        return 2 * Math.sqrt(xExtent * xExtent + yExtent * yExtent + zExtent * zExtent);
    }
}
