package cn.jimmiez.pcu.io.off;

import cn.jimmiez.pcu.model.PointCloud3f;
import cn.jimmiez.pcu.model.PolygonMesh3f;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class OffReaderTest {

//    @Test(expected = IOException.class)
//    public void testReadBadData() throws IOException {
//        File file = new File(OffReaderTest.class.getClassLoader().getResource("model/ply/simple.ply").getFile());
//        OffReader reader = new OffReader();
//        reader.read(file);
//    }

    // test OFF
    @Test
    public void testReadOff() throws IOException {
        File file = new File(OffReaderTest.class.getClassLoader().getResource("model/off/cube.off").getFile());
        OffReader reader = new OffReader();
        OffData data =  reader.read(file);
        OffHeader header = data.getHeader();

        // test OffHeader
        assertEquals(8, header.getVerticesNum());
        assertEquals(6, header.getFacesNum());
        assertEquals(12, header.getEdgesNum());
        assertFalse(header.hasColor());
        assertFalse(header.hasNormal());
        assertFalse(header.hasTextureCoordinates());
        assertFalse(header.has4Components());
        assertFalse(header.isDimensionSpecified());
        assertEquals("OFF", header.getKeyword());


        // test OffData
        assertEquals(header.getVerticesNum(), data.getVertices().size());
        assertEquals(header.getFacesNum(), data.getFaces().size());
        assertEquals(header.getFacesNum(), data.getFaceColors().size());
        assertEquals(0, data.getVertexColors().size());

        // test the getter of PointCloud3f
        PointCloud3f pointCloud = reader.read(file, PointCloud3f.class);
        assertNotNull(pointCloud.getPoints());
        assertEquals(header.getVerticesNum(), pointCloud.getPoints().size());
        for (int i = 0; i < header.getVerticesNum(); i ++) {
            float[] xyz = pointCloud.getPoints().get(i);
            assertEquals(3, xyz.length);
        }
    }

    // test COFF
    @Test
    public void testReadCOff() throws IOException {
        File file = new File(OffReaderTest.class.getClassLoader().getResource("model/off/cube2.off").getFile());
        OffReader reader = new OffReader();
        OffData data =  reader.read(file);
        OffHeader header = data.getHeader();

        // test OffHeader
        assertEquals(8, header.getVerticesNum());
        assertEquals(6, header.getFacesNum());
        assertEquals(12, header.getEdgesNum());
        assertTrue(header.hasColor());
        assertFalse(header.hasNormal());
        assertFalse(header.hasTextureCoordinates());
        assertFalse(header.has4Components());
        assertFalse(header.isDimensionSpecified());
        assertEquals("COFF", header.getKeyword());

        assertEquals(header.getVerticesNum(), data.getVertices().size());
        assertEquals(header.getFacesNum(), data.getFaces().size());
        assertEquals(header.getVerticesNum(), data.getVertexColors().size());

        MeshWithColor4f mesh = reader.read(file, MeshWithColor4f.class);
        assertEquals(header.getVerticesNum(), mesh.getPoints().size());
        assertEquals(header.getFacesNum(), mesh.getFaces().size());
        assertEquals(header.getVerticesNum(), mesh.getVertexColors().size());

        for (int i = 0; i < header.getVerticesNum(); i ++) {
            float[] xyz = mesh.getPoints().get(i);
            float[] rgba = mesh.getVertexColors().get(i);
            assertEquals(3, xyz.length);
            assertEquals(4, rgba.length);
        }
        for (int i = 0; i < header.getFacesNum(); i ++) {
            int[] indices = mesh.getFaces().get(i);
            assertTrue(indices.length > 0);
        }
    }

    @Test
    public void testReadNOff() throws IOException {
        File file = new File(OffReaderTest.class.getClassLoader().getResource("model/off/foo.off").getFile());
        OffReader reader = new OffReader();
        OffData data = reader.read(file);
        OffHeader header = data.getHeader();

        // test OffHeader
        assertEquals(16, header.getVerticesNum());
        assertEquals(0, header.getFacesNum());
        assertEquals(OffHeader.UNSET, header.getEdgesNum());
        assertFalse(header.hasColor());
        assertTrue(header.hasNormal());
        assertFalse(header.hasTextureCoordinates());
        assertFalse(header.has4Components());
        assertFalse(header.isDimensionSpecified());
        assertEquals("NOFF", header.getKeyword());

        assertEquals(header.getVerticesNum(), data.getVertices().size());
        assertEquals(header.getFacesNum(), data.getFaces().size());
        assertEquals(0, data.getVertexColors().size());
        assertEquals(header.getVerticesNum(), data.getVertexNormals().size());

        MeshWithNormal3f mesh = reader.read(file, MeshWithNormal3f.class);
        assertEquals(header.getVerticesNum(), mesh.getPoints().size());
        assertEquals(header.getFacesNum(), mesh.getFaces().size());
        assertEquals(header.getVerticesNum(), mesh.getVertexNormals().size());

        for (int i = 0; i < header.getVerticesNum(); i ++) {
            float[] xyz = mesh.getPoints().get(i);
            float[] normal = mesh.getVertexNormals().get(i);
            assertEquals(3, xyz.length);
            assertEquals(3, normal.length);
        }
        for (int i = 0; i < header.getFacesNum(); i ++) {
            int[] indices = mesh.getFaces().get(i);
            assertTrue(indices.length > 0);
        }

    }

    public static class MeshWithColor4f extends PolygonMesh3f{
        private List<float[]> vertexColors;

        public MeshWithColor4f() {
            super();
            vertexColors = new ArrayList<>();
        }

        @ReadFromOff(dataType = ReadFromOff.VERTEX_COLORS)
        public List<float[]> getVertexColors() {
            return vertexColors;
        }

    }

    public static class MeshWithNormal3f extends PolygonMesh3f{

        private List<float[]> vertexNormals;

        public MeshWithNormal3f() {
            super();
            vertexNormals = new ArrayList<>();
        }

        @ReadFromOff(dataType = ReadFromOff.VERTEX_NORMALS)
        public List<float[]> getVertexNormals() {
            return vertexNormals;
        }
    }
}
