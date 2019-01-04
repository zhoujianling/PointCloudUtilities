package cn.jimmiez.pcu.io.ply;

import cn.jimmiez.pcu.util.Pair;
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
    public void testReadAsciiPlyHeader() throws IOException {
        PlyReader reader = new PlyReader();
        File file = new File(PlyReaderTest.class.getClassLoader().getResource("model/ply/simple.ply").getFile());
        PlyHeader header = reader.readHeaderThenCloseFile(file);
        assertTrue(header.getPlyFormat() == PlyReader.FORMAT_ASCII);
        assertEquals(header.getPlyVersion(), 1.0f, 1e-5);
        assertTrue(header.getElementTypes().get("vertex").getPropertiesName().size() == 3);
        assertTrue(header.getElementTypes().get("vertex").getPropertiesType().size() == 3);
        for (Pair<String, Integer> pair : header.getElementsNumber()) {
            if (pair.getKey().equals("vertex")) {
                assertTrue(pair.getValue() == 4770);
            } else if (pair.getKey().equals("face")) {
                assertTrue(pair.getValue() == 0);
            }
        }
    }

    @Test
    public void testReadAsciiPlyData() throws IOException {
        PlyReader reader = new PlyReader();
        File file = new File(PlyReaderTest.class.getClassLoader().getResource("model/ply/simple.ply").getFile());
        PointCloud3f pointCloud = reader.read(file, PointCloud3f.class);
        assertNotNull(pointCloud);
        assertTrue(pointCloud.getPoints().size() == 4770);
        assertTrue(pointCloud.getPoints().get(1000).length == 3);
    }


    @Test
    public void testReadAsciiPlyData2() throws IOException {
        PlyReader reader = new PlyReader();
        File file = new File(PlyReaderTest.class.getClassLoader().getResource("model/ply/drill_shaft_zip.ply").getFile());
        PolygonMesh3f pointCloud = reader.read(file, PolygonMesh3f.class);
        assertNotNull(pointCloud);
        assertTrue(pointCloud.getPoints().size() == 881);
        assertTrue(pointCloud.getPoints().get(200).length == 3);
        assertTrue(pointCloud.getFaces().size() == 1288);
    }


    @Test
    public void testReadBinaryLittleEndianPlyData() {
        PlyReader reader = new PlyReader();
        File file = new File(PlyReaderTest.class.getClassLoader().getResource("model/ply/tree_bin.ply").getFile());
        MeshWithColor4b mesh = reader.read(file, MeshWithColor4b.class);
        int expectedVerticesSize = 27788;
        int expectedFaceSize = 52113;
        assertTrue(mesh.getPoints().size() == expectedVerticesSize);
        assertTrue(mesh.getFaces().size() == expectedFaceSize);
        assertTrue(mesh.getVertexColors().size() == expectedVerticesSize);
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 300; i ++) {
            int randomPtr = random.nextInt(expectedVerticesSize);
            byte[] rgba = mesh.getVertexColors().get(randomPtr);
            assertTrue(rgba.length == 4);
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
