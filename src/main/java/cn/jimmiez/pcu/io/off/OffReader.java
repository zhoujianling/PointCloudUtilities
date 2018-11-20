package cn.jimmiez.pcu.io.off;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Object File Format(off / OFF) is used to represent the geometry of polygonal mesh by
 * specifying the vertices and faces.
 */
public class OffReader {

    private static final int STATE_READY = 0;
    private static final int STATE_CHECK_HEADER = 1;
    private static final int STATE_READING_ELEMENT_NUM = 2;
    private static final int STATE_READING_VERTICES = 3;
    private static final int STATE_READING_FACES= 4;
    private static final int STATE_COMPLETE = 5;
    private static final int STATE_ERROR = 6;

    private String fetchNextLine(Scanner scanner) {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("#")) continue;
            return line;
        }
        return null;
    }

    private void readImpl(Scanner scanner, OffData data) {
        int state = STATE_READY;
        boolean loop = true;
        while (loop) {
            switch (state) {
                case STATE_READY: {
                    state = STATE_CHECK_HEADER;
                    break;
                }
                case STATE_CHECK_HEADER: {
                    String line = fetchNextLine(scanner);
                    if (line == null) {
                        System.err.println("Cannot read first line.");
                        state = STATE_ERROR;
                        break;
                    }
                    if (line.toLowerCase().equals("off")) {
                        state = STATE_READING_ELEMENT_NUM;
                    } else {
                        System.err.println("First line does not start with \"off\"");
                        state = STATE_ERROR;
                    }
                    break;
                }
                case STATE_READING_ELEMENT_NUM: {
                    String line = fetchNextLine(scanner);
                    if (line == null) {
                        System.err.println("Cannot read number of vertices and faces.");
                        state = STATE_ERROR;
                        break;
                    }
                    String[] nums = line.split(" ");
                    if (nums.length != 3) {
                        state = STATE_ERROR;
                    } else {
                        data.verticesNum = Integer.valueOf(nums[0]);
                        data.facesNum = Integer.valueOf(nums[1]);
                        data.edgesNum = Integer.valueOf(nums[2]);
                        state = STATE_READING_VERTICES;
                    }
                    break;
                }
                case STATE_READING_VERTICES: {
                    if (data.verticesNum == OffData.UNSET) {
                        System.err.println("Invalid number of vertices.");
                        state = STATE_ERROR;
                        break;
                    }
                    for (int i = 0; i < data.verticesNum; i ++) {
                        String line = fetchNextLine(scanner);
                        if (line == null) {

                        }

                    }
                    state = STATE_READING_FACES;
                    break;
                }
                case STATE_READING_FACES: {

                    state = STATE_COMPLETE;
                    break;
                }
                case STATE_COMPLETE: {
                    loop = false;
                    break;
                }
                case STATE_ERROR: {
                    loop = false;
                    break;
                }
            }

        }
    }

    public OffData read(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        OffData data = new OffData();
        readImpl(scanner, data);
        return data;
    }

    public void read(File file, Object object) throws FileNotFoundException {
        OffData data = read(file);
        // inject into object ....
    }

    public static class OffData {

        private static final int UNSET = -1;

        private static final String MODE_XYZ_FACE = "xyz_face";
        private static final String MODE_XYZ_RGBA_FACE = "xyz_rgba_face";
        private static final String MODE_XYZ_FACE_RGBA = "xyz_face_rgba";

        /** number of vertices **/
        private int verticesNum = UNSET;

        /** number of faces **/
        private int facesNum = UNSET;

        /** number of edges, can be safely ignored **/
        private int edgesNum = UNSET;

        /** vertices data, each array is a point in 3d space **/
        private List<float[]> vertices = new ArrayList<>();

        /** face data, each array is composed of indices of vertices **/
        private List<int[]> faces = new ArrayList<>();

        private String mode = null;

        public int getVerticesNum() {
            return verticesNum;
        }

        public int getFacesNum() {
            return facesNum;
        }

        public int getEdgesNum() {
            return edgesNum;
        }

        public List<float[]> getVertices() {
            return vertices;
        }

        public List<int[]> getFaces() {
            return faces;
        }
    }

}
