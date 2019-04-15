package cn.jimmiez.pcu.io.ply;

import cn.jimmiez.pcu.Constants;
import cn.jimmiez.pcu.Constants4Test;
import cn.jimmiez.pcu.model.PointCloud3f;
import cn.jimmiez.pcu.model.PolygonMesh3f;
import org.junit.*;

import javax.vecmath.Point3d;
import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

public class PlyWriterTest {



    @BeforeClass
    public static void createTempDir() {
        String userDir = System.getProperty("user.home");
        userDir += File.separator;
        userDir += Constants4Test.OUTPUT_DIR;
        File file = new File(userDir);
        if (!file.exists()) {
            if (!file.mkdir()) {
                fail();
            }
        }
    }

//    @AfterClass
    public static void cleanTempDir() {
        String userDir = System.getProperty("user.home");
        userDir += File.separator;
        userDir += Constants4Test.OUTPUT_DIR;
        File file = new File(userDir);
        assertTrue(file.exists() && file.isDirectory());
        if(!deleteDirectory(file)) {
            System.err.println("Warning: Cannot clean up temporary files for unit test.");
            System.err.flush();
//            fail();
        }

    }

    private static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                if (!deleteDirectory(file)) {
                    return false;
                }
            }
        }
        return directoryToBeDeleted.delete();
    }

    @Test
    public void testPrepareChainBinary() {
        List<float[]> vertexData = new Vector<>();
        vertexData.add(new float[] {0.101f, -3f, 0.7f});
        vertexData.add(new float[] {0.301f, +3f, 0.9f});
        vertexData.add(new float[] {2.101f, -3f, 1.1f});
        vertexData.add(new float[] {0.521f, -3f, 1.0f});
        vertexData.add(new float[] {2.104f, -3f, 1.1f});

        List<int[]> faceData = new Vector<>();
        faceData.add(new int[] {0, 1, 2});
        faceData.add(new int[] {1, 0, 4});
        String userDir = System.getProperty("user.home");
        userDir += File.separator;
        userDir += Constants4Test.OUTPUT_DIR;
        File tempPlyFile = new File(userDir.concat(File.separator).concat("plyBinLittle.ply"));
        File tempPlyFile2 = new File(userDir.concat(File.separator).concat("plyBinBig.ply"));

        PlyWriter writer = new PlyWriter();
        int code = writer
                .prepare()
                .format(PlyFormat.BINARY_LITTLE_ENDIAN)
                .comment("this is test")
                .comment("for Point Cloud Util v.0.0.3")
                .defineElement("vertex")
                .defineScalarProperties(new String[] {"x", "y", "z"}, PcuDataType.FLOAT, vertexData)
                .defineElement("face")
                .defineListProperty("vertex_indices", PcuDataType.UCHAR, PcuDataType.INT, faceData)
                .writeTo(tempPlyFile)
                .okay();

        assertEquals(code, (int) Constants.ERR_CODE_NO_ERROR);

        PlyReader reader = new PlyReader();
        PolygonMesh3f mesh = reader.read(tempPlyFile, PolygonMesh3f.class);
        assertNotNull(mesh);
        assertEquals(vertexData.size(), mesh.getPoints().size());
        assertEquals(vertexData.get(0)[0], mesh.getPoints().get(0)[0], 1E-6);
        assertEquals(vertexData.get(0)[1], mesh.getPoints().get(0)[1], 1E-6);
        assertEquals(vertexData.get(0)[2], mesh.getPoints().get(0)[2], 1E-6);
        assertEquals(faceData.size(), mesh.getFaces().size());

        int code2 = writer
                .prepare()
                .format(PlyFormat.BINARY_BIG_ENDIAN)
                .comment("this is test")
                .comment("for Point Cloud Util v.0.0.3")
                .defineElement("vertex")
                .defineScalarProperties(new String[] {"x", "y", "z"}, PcuDataType.FLOAT, vertexData)
                .defineElement("face")
                .defineListProperty("vertex_indices", PcuDataType.UCHAR, PcuDataType.INT, faceData)
                .writeTo(tempPlyFile2)
                .okay();

        assertEquals(code2, (int) Constants.ERR_CODE_NO_ERROR);

        reader = new PlyReader();
        mesh = reader.read(tempPlyFile, PolygonMesh3f.class);
        assertNotNull(mesh);
        assertEquals(vertexData.size(), mesh.getPoints().size());
        for (int i = 0; i < vertexData.size(); i ++) {
            assertEquals(vertexData.get(i)[0], mesh.getPoints().get(i)[0], 1E-6);
            assertEquals(vertexData.get(i)[1], mesh.getPoints().get(i)[1], 1E-6);
            assertEquals(vertexData.get(i)[2], mesh.getPoints().get(i)[2], 1E-6);
        }
        assertEquals(faceData.size(), mesh.getFaces().size());
        for (int i = 0; i < faceData.size(); i++) {
            assertEquals(faceData.get(i).length, mesh.getFaces().get(i).length);
        }
    }

    @Test
    public void testWrite() {
        String userDir = System.getProperty("user.home");
        userDir += File.separator;
        userDir += Constants4Test.OUTPUT_DIR;
        File tempPlyFile = new File(userDir.concat(File.separator).concat("temp.ply"));
        int vertexCnt = 1000;
        PlyEntity entity = new PlyEntity(vertexCnt);
        PlyWriter writer = new PlyWriter();
        int code = writer.write(entity, tempPlyFile);

        assertEquals(code, (int) Constants.ERR_CODE_NO_ERROR);
        PlyReader reader = new PlyReader();
        PlyEntity object4Read = reader.read(tempPlyFile, PlyEntity.class);
        assertNotNull(object4Read);
        assertEquals(vertexCnt, object4Read.vertices().size());
        for (int i = 0; i < vertexCnt; i ++) {
            assertEquals(3, object4Read.vertices().get(i).length);
        }
    }

    @Test
    public void testPrepareChainAscii() {
        List<float[]> vertexData = new Vector<>();
        vertexData.add(new float[] {0.101f, -3f, 0.7f});
        vertexData.add(new float[] {0.301f, +3f, 0.9f});
        vertexData.add(new float[] {2.101f, -3f, 1.1f});
        vertexData.add(new float[] {0.521f, -3f, 1.0f});
        vertexData.add(new float[] {2.104f, -3f, 1.1f});
        PlyWriter writer = new PlyWriter();

        List<int[]> faceData = new Vector<>();
        faceData.add(new int[] {0, 1, 2});
        faceData.add(new int[] {1, 0, 4});

        String userDir = System.getProperty("user.home");
        userDir += File.separator;
        userDir += Constants4Test.OUTPUT_DIR;
        File tempPlyFile = new File(userDir.concat(File.separator).concat("plyAscii.ply"));
//        int code = writer.write(pointCloud, tempPlyFile);
        int code = writer
                .prepare()
                .format(PlyFormat.ASCII)
                .comment("this is test")
                .comment("for Point Cloud Util.")
                .defineElement("vertex")
                .defineScalarProperties(new String[] {"x", "y", "z"}, PcuDataType.FLOAT, vertexData)
                .defineElement("face")
                .defineListProperty("vertex_indices", PcuDataType.UCHAR, PcuDataType.INT, faceData)
                .writeTo(tempPlyFile)
                .okay();

        assertEquals(code, (int) Constants.ERR_CODE_NO_ERROR);

        PlyReader reader = new PlyReader();
        PolygonMesh3f mesh = reader.read(tempPlyFile, PolygonMesh3f.class);
        assertNotNull(mesh);
        assertEquals(vertexData.size(), mesh.getPoints().size());
        assertEquals(vertexData.get(0)[0], mesh.getPoints().get(0)[0], 1E-6);
        assertEquals(vertexData.get(0)[1], mesh.getPoints().get(0)[1], 1E-6);
        assertEquals(vertexData.get(0)[2], mesh.getPoints().get(0)[2], 1E-6);
        assertEquals(faceData.size(), mesh.getFaces().size());
    }

    public static class PlyEntity {

        List<double[]> vertices = new ArrayList<>();
        List<int[]> vertexIndices = new ArrayList<>();

        public PlyEntity() {}

        public PlyEntity(int n) {
            Random r = new Random(System.currentTimeMillis());
            for (int i = 0; i < n; i ++) {
                vertices.add(new double[]{r.nextDouble(), r.nextDouble(), r.nextDouble()});
            }
            int l = Math.min(3, n / 2);
            for (int i = 0; i < n; i ++) {
                int[] face = new int[l];
                for (int j = 0; j < l; j ++) {
                    face[j] = (i + j) % n;
                }
                vertexIndices.add(face);
            }
        }

        @WriteScalarToPly(element = "vertex", properties = {"x", "y", "z"}, type = PcuDataType.DOUBLE)
        @ReadFromPly(element = "vertex", properties = {"x", "y", "z"})
        public List<double[]> vertices() {
            return vertices;
        }

        @WriteListToPly(element = "face", property = "vertex_indices")
        @ReadFromPly(element = "face", properties = {"vertex_indices"})
        public List<int[]> faces() {
            return vertexIndices;
        }

    }

}
