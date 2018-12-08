package cn.jimmiez.pcu.alg.skeleton;

import cn.jimmiez.pcu.io.ply.PlyReader;
import cn.jimmiez.pcu.model.PointCloud3f;
import cn.jimmiez.pcu.util.PcuCommonUtil;
import org.junit.Test;

import javax.vecmath.Point3d;
import java.io.File;
import java.util.List;

public class LevelSetSkeletonTest {

    @Test
    public void skeletonizeTest() {
        PlyReader reader = new PlyReader();
        File file = new File(LevelSetSkeleton.class.getClassLoader().getResource("model/ply/simple.ply").getFile());
        PointCloud3f pointCloud3f = reader.read(file, PointCloud3f.class);
        List<Point3d> vertices = PcuCommonUtil.arrayList2VecList(pointCloud3f.getPoints());
        LevelSetSkeleton skeleton = new LevelSetSkeleton();
        skeleton.setN(10);
        Skeleton skel = skeleton.skeletonize(vertices);
    }
}
