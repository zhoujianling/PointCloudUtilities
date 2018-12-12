package cn.jimmiez.pcu.io.off;

import java.util.ArrayList;
import java.util.List;

public class OffData {

    public static final int UNSET = -1;

    /** the format of OFF file, e.g. OFF, NOFF **/
    OffFormat format = null;

    /** number of vertices **/
    int verticesNum = UNSET;

    /** number of faces **/
    int facesNum = UNSET;

    /** number of edges, can be safely ignored **/
    int edgesNum = UNSET;

    /** vertices data, each array is a point in 3d space **/
    List<float[]> vertices = new ArrayList<>();

    /** face data, each array is composed of indices of vertices **/
    List<int[]> faces = new ArrayList<>();

    /** optional face colors **/
    List<float[]> faceColors = new ArrayList<>();

    /** optional vertex colors **/
    List<float[]> vertexColors = new ArrayList<>();

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

    public List<float[]> getFaceColors() {
        return faceColors;
    }

    public List<float[]> getVertexColors() {
        return vertexColors;
    }
}
