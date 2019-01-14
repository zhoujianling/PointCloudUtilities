package cn.jimmiez.pcu.io.off;

import java.util.ArrayList;
import java.util.List;

public class OffData {

    /** the header of OFF file, e.g. OFF, NOFF **/
    private OffHeader header = null;

    /** vertices data, each array is a point in 3d space **/
    List<float[]> vertices = new ArrayList<>();

    /** normals of vertices data, each array is a 3d-vector **/
    List<float[]> vertexNormals = new ArrayList<>();

    /** face data, each array is composed of indices of vertices **/
    List<int[]> faces = new ArrayList<>();

    /** optional face colors **/
    List<float[]> faceColors = new ArrayList<>();

    /** optional vertex colors **/
    List<float[]> vertexColors = new ArrayList<>();

    public List<float[]> getVertices() {
        return vertices;
    }

    public List<int[]> getFaces() {
        return faces;
    }

    public List<float[]> getFaceColors() {
        return faceColors;
    }

    public List<float[]> getVertexColors() {
        return vertexColors;
    }

    public List<float[]> getVertexNormals() {
        return vertexNormals;
    }

    public OffHeader getHeader() {
        return header;
    }

    public void setHeader(OffHeader header) {
        this.header = header;
    }
}
