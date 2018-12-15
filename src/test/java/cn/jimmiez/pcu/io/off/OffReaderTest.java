package cn.jimmiez.pcu.io.off;

import cn.jimmiez.pcu.model.PointCloud3f;
import cn.jimmiez.pcu.model.PolygonMesh3f;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class OffReaderTest {

    @Test
    public void readTest1() throws FileNotFoundException {
        File file = new File(OffReaderTest.class.getClassLoader().getResource("model/off/cube.off").getFile());
        OffReader reader = new OffReader();
        OffData data =  reader.read(file);
        OffHeader header = data.getHeader();
        assertTrue(data.getVertices().size() == header.getVerticesNum());
        assertTrue(data.getFaces().size() == header.getFacesNum());
        assertTrue(data.getFaceColors().size() == 0 || data.getFaceColors().size() == header.getFacesNum());
        assertTrue(data.getVertexColors().size() == 0 || data.getVertexColors().size() == header.getVerticesNum());

        PointCloud3f pointCloud = reader.read(file, PointCloud3f.class);
        assertTrue(pointCloud.getPoints().size() == 8);
    }

    @Test
    public void readTest2() throws FileNotFoundException {
        File file = new File(OffReaderTest.class.getClassLoader().getResource("model/off/cube2.off").getFile());
        OffReader reader = new OffReader();
        OffData data =  reader.read(file);
        OffHeader header = data.getHeader();
        assertTrue(data.getVertices().size() == header.getVerticesNum());
        assertTrue(data.getFaces().size() == header.getFacesNum());
        assertTrue(data.getVertexColors().size() == 0 || data.getVertexColors().size() == header.getVerticesNum());

        MeshWithColor4f mesh = reader.read(file, MeshWithColor4f.class);
        assertTrue(mesh.getPoints().size() == 8);
        assertTrue(mesh.getFaces().size() == 6);
        assertTrue(mesh.getVertexColors().size() == 8);
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
}
