package cn.jimmiez.pcu.io.obj;

import cn.jimmiez.pcu.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjData {

    Map<ObjDataType, List<double[]>> vectorData;

    /**
     * a hash-map that stores all element data
     * Type -> (2 , [v1,vt1,v2,vt2])
     * Type -> (3 , [v1,vt1,vn1,v2,vt2,vn2])
     **/
    Map<ObjDataType, Pair<Integer, List<double[]>>> elementData;

    Map<ObjDataType, String> textData;

    ObjData() {
        textData = new HashMap<>();
        vectorData = new HashMap<>();
        elementData = new HashMap<>();

        vectorData.put(ObjDataType.V_GEOMETRIC_VERTICES, new ArrayList<double[]>());
        vectorData.put(ObjDataType.VT_TEXTURE_VERTICES, new ArrayList<double[]>());
        vectorData.put(ObjDataType.VN_VERTEX_NORMALS, new ArrayList<double[]>());
        vectorData.put(ObjDataType.VP_PARAMETER_SPACE_VERTICES, new ArrayList<double[]>());

        elementData.put(ObjDataType.P_POINT, new Pair<Integer, List<double[]>>(0, new ArrayList<double[]>()));
        elementData.put(ObjDataType.F_FACE, new Pair<Integer, List<double[]>>(0, new ArrayList<double[]>()));
        elementData.put(ObjDataType.L_LINE, new Pair<Integer, List<double[]>>(0, new ArrayList<double[]>()));
    }

    public void clear() {
        for (List list : vectorData.values()) list.clear();
        textData.clear();
    }

    public List<double[]> vertices() {
        List<double[]> result = vectorData.get(ObjDataType.V_GEOMETRIC_VERTICES);
        if (result != null && result.size() < 1) return null;
        return result;
    }

    public List<double[]> textureCoordinates() {
        List<double[]> result = vectorData.get(ObjDataType.VT_TEXTURE_VERTICES);
        if (result != null && result.size() < 1) return null;
        return result;
    }

    public List<double[]> normals() {
        List<double[]> result = vectorData.get(ObjDataType.VN_VERTEX_NORMALS);
        if (result != null && result.size() < 1) return null;
        return result;
    }

    public String mtllib() {
        return textData.get(ObjDataType.MTLLIB_MATERIAL_LIBRARY);
    }

    public Pair<Integer, List<double[]>> originalFaces() {
        Pair<Integer, List<double[]>> result = elementData.get(ObjDataType.F_FACE);
        if (result != null && result.getKey() == 0) return null;
        return result;
    }

}
