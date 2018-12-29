package cn.jimmiez.pcu.alg.projector;

import cn.jimmiez.pcu.common.graphics.BoundingBox;
import cn.jimmiez.pcu.common.graphics.Octree;
import cn.jimmiez.pcu.common.graphics.Octree2;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

/**
 * The iterative implementation of Weighted Locally Optimal Projector, see paper:
 * {@literal Huang, H., Li, D., Zhang, H., Ascher, U., & Cohen-Or, D. (2009).
 * Consolidation of unorganized point clouds for surface reconstruction.
 * ACM transactions on graphics (TOG), 28(5), 176.}
 *
 */
public class WeightedLocallyOptimalProjector {

    /** the points set representing original surface **/
    private List<Point3d> originals = null;

    /** the sample points which will be projected onto original surface **/
    private List<Point3d> samples = null;

    /** the indices of neighbors of sample points on ORIGINAL surfaces **/
    private List<List<Integer>> sampleOriginalNeighbors = new ArrayList<>();

    /** the indices of neighbors of sample points on SAMPLE surfaces **/
    private List<List<Integer>> sampleSelfNeighbors = new ArrayList<>();

    /** the indices of neighbors of original points on ORIGINAL surfaces **/
    private List<List<Integer>> originalSelfNeighbors = new ArrayList<>();

    /** the local density of sample points **/
    private List<Double> sampleDensity = new ArrayList<>();

    /** the local density of original points **/
    private List<Double> originalDensity = new ArrayList<>();

    /**
     * the local support radius
     * h is set to 4 * sqrt(d_bb / m) by default, the d_bb is the diagonal length
     * of the bounding box of original data set
     **/
    private double h = 0.1;

    /**
     * the factor that controls the repulsion force, the possible value of mu is
     * between 0 and 0.5
     */
    private double repulsionMu = 0.5;

    /**
     * the octree used to search nearest neighbors in original data set
     */
    private Octree2 originalOctree;

    /**
     * The ctor of WLOP.
     * @param originals The original points set, which might have noise, outliers and missing data.
     */
    public WeightedLocallyOptimalProjector(List<Point3d> originals) {
        this.originals = originals;
        originalOctree = new Octree2();
        originalOctree.buildIndex(originals);
        for (int i = 0; i < originals.size(); i ++) originalDensity.add(1.0);
        BoundingBox box = BoundingBox.of(originals);
        h = 4 * Math.sqrt(box.diagonalLength() / originals.size());
    }

    /**
     * The Gaussian weighting function
     * @param distance the distance between two points
     * @return the result of Gaussian function
     */
    private double theta(double distance) {
        return Math.pow(Math.E, - 4 * distance * distance / h / h);
    }

    private void run(int iterNum) {
        for (int i = 0; i < iterNum; i ++) {
            System.out.print("\rh:" + h);
            iterate(i);
        }
    }

    private void computeAverageTerm(List<Vector3d> averageVectors, List<Double> averageWeightSums) {
        averageVectors.clear();
        averageWeightSums.clear();
        for (int i = 0; i < samples.size(); i ++) {
            Point3d sample = samples.get(i);
            double averageWeightSum = 0;
            Vector3d delta = new Vector3d(); // delta = sampleNeighbor * averageWeight
            for (int originalNeighborIndex : sampleOriginalNeighbors.get(i)) {
                double averageWeight;
                Vector3d deltaj;
                Point3d neighborPoint = originals.get(originalNeighborIndex);
                double distance = sample.distance(neighborPoint);

                // in case distance is near zero, resulting in INFINITY weight
                distance = Math.max(h * 0.01 , distance);
                averageWeight = (theta(distance) / distance);

                // density based weighted
                averageWeight *= originalDensity.get(originalNeighborIndex);

                deltaj = new Vector3d(neighborPoint);
                deltaj.scale(averageWeight);
                delta.add(deltaj);
                averageWeightSum += averageWeight;
            }
            averageWeightSums.add(averageWeightSum);
            averageVectors.add(delta);
        }
    }

    private void computeRepulsionTerm(List<Vector3d> repulsionVectors, List<Double> repulsionWeightSums) {
        repulsionVectors.clear();
        repulsionWeightSums.clear();

        for (int i = 0; i < samples.size(); i ++) {
            Point3d sample = samples.get(i);
            Vector3d delta = new Vector3d();
            double repulsionWeightSum = 0;
            for (int neighborIndex : sampleSelfNeighbors.get(i)) {
                if (i == neighborIndex) continue;
                Point3d neighborPoint = samples.get(neighborIndex);

                double distance = neighborPoint.distance(sample);

                // in case distance is near zero, resulting in INFINITY weight
                distance = Math.max(h * 0.01 , distance);
                // the eta function is - 1 / r^3
                double repulsionWeight = theta(distance) / Math.pow(distance, 4);

                Vector3d neighborSampleVec = new Vector3d(sample.x - neighborPoint.x, sample.y - neighborPoint.y, sample.z - neighborPoint.z);

                // density-based weighted
                repulsionWeight *= sampleDensity.get(neighborIndex);

                neighborSampleVec.scale(repulsionWeight);
                repulsionWeightSum += repulsionWeight;
                delta.add(neighborSampleVec);
            }
            repulsionWeightSums.add(repulsionWeightSum);
            repulsionVectors.add(delta);
        }
    }

    private void computeDensity(List<Point3d> points, List<Double> densities, List<List<Integer>> neighborList, double h, boolean original) {
        for (int i = 0; i < points.size(); i ++) {
            double density = 1.0;
            Point3d point = points.get(i);
            List<Integer> neighbors = neighborList.get(i);

            for (int neighborIndex : neighbors) {
                Point3d neighbor = points.get(neighborIndex);
                double distance = point.distance(neighbor);
                double den = theta(distance);
                density += den;
            }
            if (original) {
                density = 1.0 / density;
            } else {
                density = Math.sqrt(density);
            }
            densities.set(i, density);
        }
    }

    private void iterate(int iter) {
        Octree sampleOctree = new Octree();
        sampleOctree.buildIndex(samples);
        sampleSelfNeighbors.clear();
        sampleOriginalNeighbors.clear();
        originalSelfNeighbors.clear();

        for (int i = 0; i < samples.size(); i ++) {
            sampleSelfNeighbors.add(sampleOctree.searchNeighborsInSphere(i, h));
            sampleOriginalNeighbors.add(originalOctree.searchNeighborsInSphere(samples.get(i), h));
        }

        final double MAGIC_RATIO = 0.95;

        if (iter == 0) {
            // compute density of original points
            for (int i = 0; i < originals.size(); i ++) {
                originalSelfNeighbors.add(originalOctree.searchNeighborsInSphere(i, h));
            }
            computeDensity(originals, originalDensity, originalSelfNeighbors, h * MAGIC_RATIO, true);
        }

        computeDensity(samples, sampleDensity, sampleSelfNeighbors, h, false);
        List<Double> averageWeightSums = new ArrayList<>();
        List<Double> repulsionWeightSums = new ArrayList<>();
        List<Vector3d> averageVectors = new ArrayList<>();
        List<Vector3d> repulsionVectors = new ArrayList<>();
        computeAverageTerm(averageVectors, averageWeightSums);
        computeRepulsionTerm(repulsionVectors, repulsionWeightSums);

        for (int i = 0; i < samples.size(); i ++) {
            Point3d sample = samples.get(i);
            double averageWeightSum = averageWeightSums.get(i);
            double repulsionWeightSum = repulsionWeightSums.get(i);
            Vector3d averageVector = averageVectors.get(i);
            Vector3d repulsionVector = repulsionVectors.get(i);
            if (averageWeightSum  > 1e-6) {
                averageVector.scale(1.0 / averageWeightSum);
                sample.set(averageVector);
            }
            if (repulsionWeightSum > 1e-6 && repulsionMu >= 0) {
                repulsionVector.scale(repulsionMu / repulsionWeightSum);
                sample.add(repulsionVector);
            }
        }

    }

    /**
     * Project the sample points onto the original points set.
     * Note that the coordinates of sample points can be same as original points set,
     * but these two lists should not share same objects.
     * @param samples The sample points,
     * @param iterNum the number of iterations
     */
    public void project(List<Point3d> samples, int iterNum) {
        this.samples = samples;
        this.sampleDensity.clear();
        for (int i = 0; i < samples.size(); i ++) this.sampleDensity.add(1.0);
        run(iterNum);
    }

    public void setH(double h) {
        this.h = h;
    }

    public void setRepulsionMu(double repulsionMu) {
        this.repulsionMu = repulsionMu;
    }
}

