package cn.jimmiez.pcu.io.ply;

import cn.jimmiez.pcu.Constants;
import cn.jimmiez.pcu.Constants4Test;
import cn.jimmiez.pcu.io.ply.PlyReader;
import cn.jimmiez.pcu.io.ply.PlyWriter;
import cn.jimmiez.pcu.model.PcuPointCloud3f;
import org.junit.*;

import java.io.File;
import java.nio.file.Files;

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
            fail();
        }

    }

    private static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }


    @Test
    public void writeAsciiPlyTest() {
        PcuPointCloud3f pointCloud = new PcuPointCloud3f();
        pointCloud.getPoint3ds().add(new float[] {0, 1, 2});
        pointCloud.getPoint3ds().add(new float[] {1.4f, 10.2f, -2.1f});
        PlyWriter writer = new PlyWriter();

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
                .putData(pointCloud.getPoint3ds())
                .writeTo(tempPlyFile)
                .okay();

        assertTrue(code == Constants.ERR_CODE_NO_ERROR);
    }
}
