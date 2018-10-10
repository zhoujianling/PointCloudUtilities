package cn.jimmiez.pcu.io.ply;

import java.util.HashMap;
import java.util.Map;

public class PlyElement {
    /** eg. ["x", "y", "z", "red", "green", "blue"] **/
    String[] propertiesName;
    /** eg. [float, float, float, uchar, uchar, uchar] **/
    int[] propertiesType;

    PlyElement(int propertiesNum) {
        this.propertiesName = new String [propertiesNum];
        this.propertiesType = new int [propertiesNum];
    }

    Map<String, int[]> listTypes = new HashMap<>();

    public String[] getPropertiesName() {
        return propertiesName;
    }

    public int[] getPropertiesType() {
        return propertiesType;
    }

    public Map<String, int[]> getListTypes() {
        return listTypes;
    }
}
