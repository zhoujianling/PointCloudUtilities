package cn.jimmiez.pcu.alg.skel;

import cn.jimmiez.pcu.common.Octree;
import cn.jimmiez.pcu.common.graph.Graph;
import cn.jimmiez.pcu.common.graph.Graphs;

import javax.vecmath.Point3d;
import java.util.List;
import java.util.Vector;

public class LevelSetSkeleton implements Skeletonization{

    private List<Point3d> data;
    private Octree octree = null;
    private Graph neighborhoodGraph = null;
    private Graph geodesicGraph = null;
    private List<LevelSet> levelSets = null;
    private Skeleton skeleton = null;
    private Point3d root = null;

    public LevelSetSkeleton(List<Point3d> pointCloud) {
        this.data = pointCloud;
    }

    private void init() {
        skeleton = new Skeleton();
        octree = new Octree();
        octree.buildIndex(data);
    }

    private void clean() {
        octree = null;
    }

    private void buildNeighborhoodGraph(int k) {
        List<int[]> nnIndices = new Vector<>();
        for (int i = 0; i < data.size(); i ++) {
            int[] neighborIndices = octree.searchNearestNeighbors(k, i);
            nnIndices.add(neighborIndices);
        }
        neighborhoodGraph = Graphs.knnGraph(data, nnIndices);
    }
    
    private void buildGeodesicGraph() {

    }

    private void divideLevelSets() {

    }

    private void generateCurveSkeleton() {

    }

    private void determineRoot() {

    }

    @Override
    public Skeleton skeletonize(List<Point3d> pointCloud) {
        init();
        buildNeighborhoodGraph(3);
        determineRoot();
        buildGeodesicGraph();
        divideLevelSets();
        generateCurveSkeleton();
        clean();
        return skeleton;
    }

    private static class LevelSet {
        List<List<Integer>> subgraphs;
    }

    public void setRoot(Point3d p) {
        this.root = p;
    }

    public Point3d getRoot() {
        return root;
    }
}
