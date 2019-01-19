package cn.jimmiez.pcu.common.graphics;

import cn.jimmiez.pcu.common.graphics.shape.Box;
import cn.jimmiez.pcu.util.VectorUtil;

import javax.vecmath.Point3d;
import java.util.List;

public class BoundingBox extends Box {

    public BoundingBox(Box box) {
        this(box.minX(), box.maxX(), box.minY(), box.maxY(), box.minZ(), box.maxZ());
    }

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
        // use max y - min y, in case that maxY is +Inf, minY is constant
        // do not use maxx - center.x, maxy - center.y ...
        xExtent = (maxx - minx) / 2;
        yExtent = (maxy - miny) / 2;
        zExtent = (maxz - minz) / 2;
    }

    private static BoundingBox empty() {
        return new BoundingBox(0, 0, 0, 0, 0, 0);
    }

    public static BoundingBox of(List<Point3d> data) {
        if (data.size() < 1) return empty();
        double minX =  Double.POSITIVE_INFINITY;
        double maxX =  Double.NEGATIVE_INFINITY;
        double minY =  Double.POSITIVE_INFINITY;
        double maxY =  Double.NEGATIVE_INFINITY;
        double minZ =  Double.POSITIVE_INFINITY;
        double maxZ =  Double.NEGATIVE_INFINITY;
        for (Point3d p : data) {
            if (!VectorUtil.validPoint(p)) continue;
            minX = Math.min(minX, p.x);
            maxX = Math.max(maxX, p.x);
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
            minZ = Math.min(minZ, p.z);
            maxZ = Math.max(maxZ, p.z);
        }
        if (Double.isInfinite(minX)) return empty();
        return new BoundingBox(minX, maxX, minY, maxY, minZ, maxZ);
    }

    public double diagonalLength() {
        return 2 * Math.sqrt(xExtent * xExtent + yExtent * yExtent + zExtent * zExtent);
    }
}
