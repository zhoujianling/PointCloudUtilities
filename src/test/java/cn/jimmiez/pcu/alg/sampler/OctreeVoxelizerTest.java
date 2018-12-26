package cn.jimmiez.pcu.alg.sampler;

import cn.jimmiez.pcu.DataUtil;
import cn.jimmiez.pcu.common.graphics.Octree;
import org.junit.Test;

import javax.vecmath.Point3d;
import java.util.List;
import static org.junit.Assert.*;

public class OctreeVoxelizerTest {

    @Test
    public void voxelizeTest() {
        List<Point3d> points = DataUtil.generateRandData(3000, 0, 3, -3, 4, 1, 8);
        OctreeVoxelizer voxelizer = new OctreeVoxelizer();
        List<OctreeVoxelizer.OctreeNode> nodes = voxelizer.voxelize(points, 5);
        assertTrue(nodes.size() > 0 && nodes.size() < 3000);
        for (Octree.OctreeNode node : nodes) {
            assertTrue(node.getChildren() == null);
            assertTrue(node.getIndices().size() > 0);
        }
    }
}
