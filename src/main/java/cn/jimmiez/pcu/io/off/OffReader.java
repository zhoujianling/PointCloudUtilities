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

    private void readImpl(Scanner scanner, OffData data) {

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

        /** number of vertices **/
        private int verticesNum;

        /** number of faces **/
        private int facesNum;

        /** number of edges, can be safely ignored **/
        private int edgesNum;

        /** vertices data, each array is a point in 3d space **/
        private List<float[]> vertices = new ArrayList<>();

        /** face data, each array is composed of indices of vertices **/
        private List<int[]> faces = new ArrayList<>();

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
