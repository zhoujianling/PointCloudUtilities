package cn.jimmiez.pcu.alg.skeleton;

import cn.jimmiez.pcu.alg.skeleton.gr.GraphReductionSkeleton;
import cn.jimmiez.pcu.io.ply.PlyReader;
import cn.jimmiez.pcu.model.PointCloud3f;
import cn.jimmiez.pcu.model.Skeleton;
import cn.jimmiez.pcu.util.PcuCommonUtil;
import org.junit.Test;

import javax.vecmath.Point3d;
import java.io.File;
import java.util.List;


public class GraphReductionSkeletonTest {

    @Test
    public void testSkeletonize() {
        PlyReader reader = new PlyReader();
        File file = new File(LevelSetSkeleton.class.getClassLoader().getResource("model/ply/simple.ply").getFile());
        PointCloud3f pointCloud3f = reader.read(file, PointCloud3f.class);
        List<Point3d> vertices = PcuCommonUtil.arrayList2VecList(pointCloud3f.getPoints());
        GraphReductionSkeleton skeleton = new GraphReductionSkeleton();
        Skeleton skel = skeleton.skeletonize(vertices);
//        int edgeCnt = Graphs.edgesCountOf(skel);
//        assertGreaterThan(edgeCnt, 0);
//        for (int vi : skel.vertices()) {
//            assertLessEqualThan(skel.adjacentVertices(vi).size(), 6);
//        }

    }
}
