package cn.jimmiez.pcu.io.off;


public class OffHeader {

    /** default value of number of element, only number of edges can be UNSET in a valid OffHeader  **/
    public static final int UNSET = -1;

    /** first line in OFF file **/
    private String keyword;

    /** if the normal of a vertex is given **/
    private boolean hasNormal = false;

    /** if the color of a vertex is given **/
    private boolean hasColor = false;

    /** if the texture coordinate of a vertex is given **/
    private boolean hasTextureCoordinates = false;

    private boolean has4Components = false;

    /** if the dimension of a vertex is given **/
    private boolean isDimensionSpecified = false;

    /**
     * the dimension of a vertex is 3 by default, this field is valid iff {@link OffHeader#isDimensionSpecified} is true
     **/
    private int dimension = 3;

    /** number of vertices **/
    private int verticesNum = UNSET;

    /** number of faces **/
    private int facesNum = UNSET;

    /** number of edges, can be safely ignored **/
    private int edgesNum = UNSET;

    public OffHeader() {}

    public String getKeyword() {
        return keyword;
    }

    public boolean hasNormal() {
        return hasNormal;
    }

    public boolean hasColor() {
        return hasColor;
    }

    public boolean has4Components() {
        return has4Components;
    }

    public boolean hasTextureCoordinates() {
        return hasTextureCoordinates;
    }

    public boolean isDimensionSpecified() {
        return isDimensionSpecified;
    }

    public int getDimension() {
        return dimension;
    }

    public int getVerticesNum() {
        return verticesNum;
    }

    public int getFacesNum() {
        return facesNum;
    }

    public int getEdgesNum() {
        return edgesNum;
    }

    public void setVerticesNum(int verticesNum) {
        this.verticesNum = verticesNum;
    }

    public void setFacesNum(int facesNum) {
        this.facesNum = facesNum;
    }

    public void setEdgesNum(int edgesNum) {
        this.edgesNum = edgesNum;
    }

    public void setName(String name) {this.keyword = name;}

    public void setHasNormal(boolean hasNormal) {
        this.hasNormal = hasNormal;
    }

    public void setHas4Components(boolean has4Components) {
        this.has4Components = has4Components;
    }

    public void setIfDimensionSpecified(boolean dimensionSpecified) {
        this.isDimensionSpecified = dimensionSpecified;
    }

    public void setHasTextureCoordinates(boolean hasTextureCoordinates) {
        this.hasTextureCoordinates = hasTextureCoordinates;
    }

    public void setHasColor(boolean hasColor) {
        this.hasColor = hasColor;
    }

    public void setDimension(int di) {
        this.dimension = di;
    }

}
