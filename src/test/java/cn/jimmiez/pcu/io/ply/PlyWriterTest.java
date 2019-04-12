package cn.jimmiez.pcu.io.ply;

import cn.jimmiez.pcu.Constants;
import cn.jimmiez.pcu.Constants4Test;
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

    }

    @Test
    public void testWrite() {
        String userDir = System.getProperty("user.home");
        userDir += File.separator;
        userDir += Constants4Test.OUTPUT_DIR;
        File tempPlyFile = new File(userDir.concat(File.separator).concat("temp.ply"));
        PlyEntity entity = new PlyEntity(1000);
        PlyWriter writer = new PlyWriter();
        int code = writer.write(entity, tempPlyFile);

        assertTrue(code == Constants.ERR_CODE_NO_ERROR);
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
    }

    private static class PlyEntity {

        List<Point3d> vertices = new ArrayList<>();
        List<int[]> vertexIndices = new ArrayList<>();

        public PlyEntity(int n) {
            Random r = new Random(System.currentTimeMillis());
            for (int i = 0; i < n; i ++) {
                vertices.add(new Point3d(r.nextDouble(), r.nextDouble(), r.nextDouble()));
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
        public List<double[]> vertices() {
            List<double[]> result = new ArrayList<>();
            for (Point3d p : vertices) {
                result.add(new double[]{p.x, p.y, p.z});
            }
            return result;
        }

        @WriteListToPly(element = "face", property = "vertex_index")
        public List<int[]> faces() {
            return vertexIndices;
        }

    }

}
