package cn.jimmiez.pcu.alg.sampler;

import cn.jimmiez.pcu.DataUtil;
import cn.jimmiez.pcu.common.graphics.Octree;
import org.junit.Test;

import javax.vecmath.Point3d;
import java.util.List;
import static org.junit.Assert.*;
import static cn.jimmiez.pcu.CommonAssertions.*;

public class OctreeVoxelizerTest {

    @Test
    public void testVoxelize() {
        List<Point3d> points = DataUtil.generateRandomData(3000, 0, 3, -3, 4, 1, 8);
        OctreeVoxelizer voxelizer = new OctreeVoxelizer();
        List<OctreeVoxelizer.OctreeNode> nodes = voxelizer.voxelize(points, 5);
        assertLessThan(nodes.size(), 3000);
        assertGreaterThan(nodes.size(), 0);
        for (Octree.OctreeNode node : nodes) {
            assertNull(node.getChildren());
            assertGreaterThan(node.getIndices().size(), 0);
        }
    }

}
