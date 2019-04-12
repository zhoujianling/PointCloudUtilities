package cn.jimmiez.pcu.io.ply;

import cn.jimmiez.pcu.model.PointCloud3f;
import cn.jimmiez.pcu.model.PolygonMesh3f;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class PlyReaderTest {

    @Test
    public void testReadAsciiPlyData() throws IOException {
        PlyReader reader = new PlyReader();
        File file = new File(PlyReaderTest.class.getClassLoader().getResource("model/ply/simple.ply").getFile());
        PointCloud3f pointCloud = reader.read(file, PointCloud3f.class);
        assertNotNull(pointCloud);
        assertEquals(4770, pointCloud.getPoints().size());
        assertEquals( 3, pointCloud.getPoints().get(1000).length);
    }


    @Test
    public void testReadAsciiPlyData2() throws IOException {
        PlyReader reader = new PlyReader();
        File file = new File(PlyReaderTest.class.getClassLoader().getResource("model/ply/drill_shaft_zip.ply").getFile());
        PolygonMesh3f pointCloud = reader.read(file, PolygonMesh3f.class);
        assertNotNull(pointCloud);
        assertEquals(881, pointCloud.getPoints().size());
        assertEquals(3, pointCloud.getPoints().get(200).length);
        assertEquals(1288, pointCloud.getFaces().size());
    }


    @Test
    public void testReadBinaryLittleEndianPlyData() {
        PlyReader reader = new PlyReader();
        File file = new File(PlyReaderTest.class.getClassLoader().getResource("model/ply/tree_bin.ply").getFile());
        MeshWithColor4b mesh = reader.read(file, MeshWithColor4b.class);
        int expectedVerticesSize = 27788;
        int expectedFaceSize = 52113;
        assertEquals(expectedVerticesSize, mesh.getPoints().size());
        assertEquals(expectedFaceSize, mesh.getFaces().size());
        assertEquals(expectedVerticesSize, mesh.getVertexColors().size());
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 300; i ++) {
            int randomPtr = random.nextInt(expectedVerticesSize);
            byte[] rgba = mesh.getVertexColors().get(randomPtr);
            assertEquals(4, rgba.length);
        }
    }

    public static class MeshWithColor4b extends PolygonMesh3f{
        private List<byte[]> vertexColors;

        public MeshWithColor4b() {
            super();
            vertexColors = new ArrayList<>();
        }

        @ReadFromPly(
                properties = {"red", "green", "blue", "alpha"},
                element = "vertex"
        )
        public List<byte[]> getVertexColors() {
            return vertexColors;
        }
    }

}
