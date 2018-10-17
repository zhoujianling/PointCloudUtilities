package cn.jimmiez.pcu.io.ply;

import cn.jimmiez.pcu.Constants;
import cn.jimmiez.pcu.Constants4Test;
import cn.jimmiez.pcu.io.ply.PlyReader;
import cn.jimmiez.pcu.io.ply.PlyWriter;
import org.junit.*;

import java.io.File;
import java.nio.file.Path;
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

    @AfterClass
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
    public void writeBinaryPlyTest() {
        List vertexData = new Vector();
        String userDir = System.getProperty("user.home");
        userDir += File.separator;
        userDir += Constants4Test.OUTPUT_DIR;
        File tempPlyFile = new File(userDir.concat(File.separator).concat("plyAscii.ply"));
//        int code = writer.write(pointCloud, tempPlyFile);
        PlyWriter writer = new PlyWriter();
        int code = writer
                .prepare()
                .format(PlyReader.FORMAT_BINARY_LITTLE_ENDIAN)
                .comment("this is test")
                .comment("for Point Cloud Util v.0.0.3")
                .defineElement("vertex")
                .defineScalarProperty("x", PlyReader.TYPE_FLOAT)
                .defineScalarProperty("y", PlyReader.TYPE_FLOAT)
                .defineScalarProperty("z", PlyReader.TYPE_FLOAT)
                .putData(vertexData)
//                .defineElement("face")
//                .defineListProperty("vertex_indices", PlyReader.TYPE_UCHAR, PlyReader.TYPE_INT)
//                .putData(faceData)
                .writeTo(tempPlyFile)
                .okay();

        assertTrue(code == Constants.ERR_CODE_NO_ERROR);

    }

    @Test
    public void writeAsciiPlyTest() {
        List vertexData = new Vector();
        vertexData.add(Arrays.asList(0.101f, -3f, 0.7f));
        vertexData.add(Arrays.asList(0.301f, +3f, 0.9f));
        vertexData.add(Arrays.asList(2.101f, -3f, 1.1f));
        vertexData.add(Arrays.asList(0.521f, -3f, 1.0f));
        vertexData.add(Arrays.asList(2.104f, -3f, 1.1f));
        PlyWriter writer = new PlyWriter();

        List faceData = new Vector();
        List r1 = new Vector();
        List r2 = new Vector();
        r1.add(new int[] {0, 1, 2});
        r2.add(new int[] {1, 0, 4});
        faceData.add(r1);
        faceData.add(r2);

        String userDir = System.getProperty("user.home");
        userDir += File.separator;
        userDir += Constants4Test.OUTPUT_DIR;
        File tempPlyFile = new File(userDir.concat(File.separator).concat("plyAscii.ply"));
//        int code = writer.write(pointCloud, tempPlyFile);
        int code = writer
                .prepare()
                .format(PlyReader.FORMAT_ASCII)
                .comment("this is test")
                .comment("for Point Cloud Util v.0.0.3")
                .defineElement("vertex")
                .defineScalarProperty("x", PlyReader.TYPE_FLOAT)
                .defineScalarProperty("y", PlyReader.TYPE_FLOAT)
                .defineScalarProperty("z", PlyReader.TYPE_FLOAT)
                .putData(vertexData)
                .defineElement("face")
                .defineListProperty("vertex_indices", PlyReader.TYPE_UCHAR, PlyReader.TYPE_INT)
                .putData(faceData)
                .writeTo(tempPlyFile)
                .okay();

        assertTrue(code == Constants.ERR_CODE_NO_ERROR);
    }


}
