package cn.jimmiez.pcu.io.ply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlyElement {
    /** eg. ["x", "y", "z", "red", "green", "blue"] **/
    List<String> propertiesName;
    /** eg. [float, float, float, uchar, uchar, uchar] **/
    List<PlyPropertyType> propertiesType;

    String elementName;

    public PlyElement() {
        this.propertiesName = new ArrayList<>();
        this.propertiesType = new ArrayList<>();
    }

    Map<String, PlyPropertyType[]> listTypes = new HashMap<>();

    public List<String> getPropertiesName() {
        return propertiesName;
    }

    public List<PlyPropertyType> getPropertiesType() {
        return propertiesType;
    }

    public Map<String, PlyPropertyType[]> getListTypes() {
        return listTypes;
    }
}
