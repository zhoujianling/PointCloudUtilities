package cn.jimmiez.pcu.alg.projector;

import cn.jimmiez.pcu.common.graphics.BoundingBox;
import cn.jimmiez.pcu.common.graphics.Octree;

import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.List;

/**
 * The iterative implementation of Locally Optimal Projector which can be used
 * to re-sample point cloud, see paper:
 * Lipman, Y., Cohen-Or, D., Levin, D., &amp; Tal-Ezer, H. (2007).
 * Parameterization-free projection for geometry reconstruction.
 * ACM Transactions on Graphics (TOG), 26(3), 22.
 *
 */
public class LocallyOptimalProjector {

    private List<Point3d> data;

    /** number of iterations **/
    private int k;

    /** the size of local neighborhood **/
    private double h;

    /** range of mu: [0, 0.5] **/
    private double miu = 0.4;

    /** dataOctree that helps find nearest neighbors **/
    private Octree dataOctree = new Octree();

    /**
     * Construct the locally optimal projector
     * @param data the points surface on which the projected points will be uniformly distributed
     * @param iterNum the number of iterations
     */
    public LocallyOptimalProjector(List<Point3d> data, int iterNum) {
        if (data == null) {
            throw new NullPointerException("Points list should not be null.");
        }
        if (iterNum < 1) {
            throw new IllegalArgumentException("Param iterNum should be larger than 0");
        }
        this.data = data;
        this.k = iterNum;
        this.h = defaultH();
    }

    /**
     * compute default h, see paper:
     * Huang, H., Li, D., Zhang, H., Ascher, U., &amp; Cohen-Or, D. (2009).
     * Consolidation of unorganized point clouds for surface reconstruction.
     * ACM transactions on graphics (TOG), 28(5), 176.
     * @return default h
     */
    private double defaultH() {
        if (data == null || data.size() < 1) return 1;
        BoundingBox box = BoundingBox.of(data);
        return box.diagonalLength() / data.size() * 4;
    }

    private double theta(double distance) {
        return Math.pow(Math.E, - 4 * distance * distance / h / h);
    }

    private void firstIteration(List<Point3d> projectedResults, List<Point3d> tobeProjected) {
        projectedResults.clear();
        for (int i1 = 0; i1 < tobeProjected.size(); i1 ++) {
            Point3d pi1 = tobeProjected.get(i1);
            double x = 0;
            double y = 0;
            double z = 0;
            double denominator = 0;
//            for (int j : dataOctree.searchAllNeighborsWithinDistance(pi1, h)) {
            for (int j = 0; j < data.size(); j ++) {
                double distance = data.get(j).distance(pi1);
                double thetaValue = theta(distance);
                x += data.get(j).x * thetaValue;
                y += data.get(j).y * thetaValue;
                z += data.get(j).z * thetaValue;
                denominator += thetaValue;
            }
            x /= denominator;
            y /= denominator;
            z /= denominator;
            projectedResults.add(new Point3d(x, y, z));
        }
    }

    private double alpha(Point3d xi1k, Point3d pj) {
        double normXi1Pj = xi1k.distance(pj);
        if (Math.abs(normXi1Pj) < 1e-7) return 0;
        return theta(normXi1Pj) / normXi1Pj;
    }

    private double beta(Point3d xi1k, Point3d xik) {
        double normXi1Xi = xi1k.distance(xik);
        if (Math.abs(normXi1Xi) < 1e-7) return 0;
        return theta(normXi1Xi) / Math.pow(normXi1Xi, 5);
    }

    private void successiveIteration(List<Point3d> projectedResults) {
        List<Point3d> nextIterPoints = new ArrayList<>();
        // this octree helps search nearest neighbors in projected points
//        Octree octree = new Octree();
//        octree.buildIndex(projectedResults);
        for (int i1 = 0; i1 < projectedResults.size(); i1 ++) {
            Point3d xi1k = projectedResults.get(i1);
            double x = 0;
            double y = 0;
            double z = 0;
            double[] alphas = new double[data.size()];
            double alphaSum = 0;
//            for (int j : dataOctree.searchAllNeighborsWithinDistance(xi1k, h)) {
            for (int j = 0; j < data.size(); j ++) {
                if (i1 == j) continue;
                Point3d pj = data.get(j);
                alphas[j] = alpha(xi1k, pj);
                alphaSum += alphas[j];
            }
//            for (int j : dataOctree.searchAllNeighborsWithinDistance(xi1k, h)) {
            for (int j = 0; j < data.size(); j ++) {
                Point3d pj = data.get(j);
                x += pj.x * alphas[j] / alphaSum;
                y += pj.y * alphas[j] / alphaSum;
                z += pj.z * alphas[j] / alphaSum;
            }

            double[] betas = new double[projectedResults.size()];
            double betaSum = 0;
//            for (int i : octree.searchAllNeighborsWithinDistance(xi1k, h)) {
            for (int i = 0; i < projectedResults.size(); i ++) {
                if (i == i1) continue;
                Point3d xik = projectedResults.get(i);
                betas[i] = beta(xik, xi1k);
                betaSum += betas[i];
            }
//            for (int i : octree.searchAllNeighborsWithinDistance(xi1k, h)) {
            for (int i = 0; i < projectedResults.size(); i ++) {
                if (i == i1) continue;
                Point3d xik = projectedResults.get(i);
                x += miu * (xi1k.x - xik.x) * betas[i] / betaSum;
                y += miu * (xi1k.y - xik.y) * betas[i] / betaSum;
                z += miu * (xi1k.z - xik.z) * betas[i] / betaSum;
            }
            nextIterPoints.add(new Point3d(x, y, z));
        }

        projectedResults.clear();
        projectedResults.addAll(nextIterPoints);
        nextIterPoints.clear();
    }

    /**
     * project tobeProjected onto data
     * @param tobeProjected the list of points that is to be projected
     * @return result
     */
    public List<Point3d> project(List<Point3d> tobeProjected) {
        if (tobeProjected == null) throw new NullPointerException("The input point set should not be null");
        dataOctree.buildIndex(data);
        List<Point3d> projectedPoints = new ArrayList<>();
        firstIteration(projectedPoints, tobeProjected);
        for (int kIter = 0; kIter < k; kIter ++) {
//            System.out.println("kiter: " + kIter);
            successiveIteration(projectedPoints);
        }
        return projectedPoints;
    }
}
