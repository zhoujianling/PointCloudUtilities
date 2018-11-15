package cn.jimmiez.pcu.alg.normal;

import Jama.Matrix;
import cn.jimmiez.pcu.alg.normal.NormalEstimator;
import cn.jimmiez.pcu.common.graphics.Octree;
import com.mkobos.pca_transform.covmatrixevd.EVD;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.List;
import java.util.Vector;

/**
 * Use Principal Component Analysis(PCA) to estimate the normal vector of point
 * cloud surface.
 * To get more details please refer to paper:
 * Hoppe, H., DeRose, T., Duchamp, T., McDonald, J., &amp; Stuetzle, W. (1992).
 * Surface reconstruction from unorganized points
 * (Vol. 26, No. 2, pp. 71-78). ACM.
 */
public class HoppeEstimator implements NormalEstimator {

    private final int MINIMAL_POINTS = 5;


    @Override
    public List<Vector3d> estimateNormals(List<Point3d> data) {
        if (data.size() < MINIMAL_POINTS) {
            throw new IllegalArgumentException("Too few points given! The size of point cloud is expected to be larger than " + MINIMAL_POINTS);
        }
        int k = defaultNeighborhoodSize(data);
        List<Vector3d> normals = new Vector<>();
        Octree octree = new Octree();
        octree.buildIndex(data);
        for (int i = 0; i < data.size(); i ++) {
            int[] neighborIndices = octree.searchNearestNeighbors(k, i);
            normals.add(estimateNormal(data, i, neighborIndices));
        }
        return normals;
    }

    /**
     * For a point xi in point cloud X, we use a centroid oi and a unit normal vector ni
     * to represent a tangent plane.
     * @param data point cloud
     * @param index the index of point xi
     * @param neighborIndices the indices of k nearest neighbors of point xi
     * @return the normal vector ni
     */
    private Vector3d estimateNormal(List<Point3d> data, int index, int[] neighborIndices) {
        Vector3d normal = new Vector3d();
        // oi, the centroid of local neighborhood
        Point3d oi = new Point3d(data.get(index));
        for (int neighborIndex : neighborIndices) {
            oi.add(data.get(neighborIndex));
        }
        oi.scale(1.0 / (neighborIndices.length + 1));
        // compute a symmetric 3x3 positive semi-definite matrix CV
        double[][] covMatrixData = new double[][] {
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0},
        };
        for (int neighborIndex : neighborIndices) {
            Point3d y = data.get(neighborIndex);
            Vector3d oiy = new Vector3d(y.x - oi.x, y.y - oi.y, y.z - oi.z);
            covMatrixData[0][0] += oiy.x * oiy.x;
            covMatrixData[0][1] += oiy.x * oiy.y;
            covMatrixData[0][2] += oiy.x * oiy.z;
            covMatrixData[1][0] += oiy.y * oiy.x;
            covMatrixData[1][1] += oiy.y * oiy.y;
            covMatrixData[1][2] += oiy.y * oiy.z;
            covMatrixData[2][0] += oiy.z * oiy.x;
            covMatrixData[2][1] += oiy.z * oiy.y;
            covMatrixData[2][2] += oiy.z * oiy.z;
        }
        for (int row = 0; row < 3; row ++) {
            for (int col = 0; col < 3; col ++) {
                covMatrixData[row][col] /= neighborIndices.length;
            }
        }

        Matrix cv = new Matrix(covMatrixData);
        EVD evd = new EVD(cv);

        Matrix eigenVectorMatrix = evd.v;
        normal.x = eigenVectorMatrix.get(0, 2);
        normal.y = eigenVectorMatrix.get(1, 2);
        normal.z = eigenVectorMatrix.get(2, 2);
        normal.normalize();
        return normal;
    }

    private int defaultNeighborhoodSize(List<Point3d> data) {
        //// TODO: 2018/11/10 Automatic determine k
        return Math.max(MINIMAL_POINTS - 1, 16);
    }
}
