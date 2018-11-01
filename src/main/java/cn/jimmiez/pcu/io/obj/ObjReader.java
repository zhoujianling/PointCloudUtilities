package cn.jimmiez.pcu.io.obj;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class ObjReader {

    public enum ObjDataType {
        // vertex data
        V_GEOMETRIC_VERTICES,
        VT_TEXTURE_VERTICES,
        VN_VERTEX_NORMALS,
        VP_PARAMETER_SPACE_VERTICES,
        // free form curve/surface attributes
        CSTYPE_RATIONAL_FORMS_OF_CURVE_ETC,
        DEG_DEGREE,
        BMAT_BASIS_MATRIX,
        STEP_STEP_SIZE,
        // elements
        P_POINT,
        L_LINE,
        F_FACE,
        CURV_CURVE,
        CURV2_2D_CURVE,
        SURF_SURFACE,
        // free-form curve/surface body statements
        PARM_PARAMETER_VALUES,
        TRIM_OUTER_TRIMMING_LOOP,
        HOLE_INNER_TRIMMING_LOOP,
        SCRV_SEPECIAL_CURVE,
        SP_SPECIAL_POINT,
        END_END_STATEMENT,
        // connectivity between free-form surfaces
        CON_CONNECT,
        // grouping
        G_GROUP_NAME,
        S_SMOOTHING_GROOP,
        MG_MERGING_GROUP,
        O_OBJECT_NAME,
        // display/render attributes
        BEVEL_BEVEL_INTERPOLATION,
        C_INTERP_COLOR_INTERPOLATION,
        D_INTERP_DISSOLVE_INTERPOLATION,
        LOD_LEVEL_OF_DETAIL,
        USEMTL_MATERIAL_NAME,
        MTLLIB_MATERIAL_LIBRARY,
        SHADOW_OBJ_SHADOW_CASTING,
        TRACE_OBJ_RAY_TRACING,
        CTECH_CURVE_APPROXIMATION_TECHNIQUE,
        STECH_SURFACE_APPROXIMATION_TECHNIQUE,
        //
        COMMENT_COMMENT
    }

    private static class ObjParser {

        private Map<ObjDataType, List<float[]>> objData = new HashMap<>();

        private static final int STATE_READY = 0;
        private static final int STATE_READING_LINE = 1;
        private static final int STATE_PARSING_TYPE = 2;
        private static final int STATE_PARSING_DATA = 3;
        private static final int STATE_COMPLETE = 9;
        private static final int STATE_ERROR = 10;
        private static final Map<String, ObjDataType> KEYWORD_MAP = new HashMap<>();

        static {
            KEYWORD_MAP.put("v", ObjDataType.V_GEOMETRIC_VERTICES);
            KEYWORD_MAP.put("vt", ObjDataType.VT_TEXTURE_VERTICES);
            KEYWORD_MAP.put("vn", ObjDataType.VN_VERTEX_NORMALS);
            KEYWORD_MAP.put("vp", ObjDataType.VP_PARAMETER_SPACE_VERTICES);
            KEYWORD_MAP.put("cstype", ObjDataType.CSTYPE_RATIONAL_FORMS_OF_CURVE_ETC);
            KEYWORD_MAP.put("deg", ObjDataType.DEG_DEGREE);
            KEYWORD_MAP.put("bmat", ObjDataType.BMAT_BASIS_MATRIX);
            KEYWORD_MAP.put("step", ObjDataType.STEP_STEP_SIZE);
            KEYWORD_MAP.put("p", ObjDataType.P_POINT);
            KEYWORD_MAP.put("l", ObjDataType.L_LINE);
            KEYWORD_MAP.put("f", ObjDataType.F_FACE);
            KEYWORD_MAP.put("curv", ObjDataType.CURV_CURVE);
            KEYWORD_MAP.put("curv2", ObjDataType.CURV2_2D_CURVE);
            KEYWORD_MAP.put("surf", ObjDataType.SURF_SURFACE);
            KEYWORD_MAP.put("parm", ObjDataType.PARM_PARAMETER_VALUES);
            KEYWORD_MAP.put("trim", ObjDataType.TRIM_OUTER_TRIMMING_LOOP);
            KEYWORD_MAP.put("hole", ObjDataType.HOLE_INNER_TRIMMING_LOOP);
            KEYWORD_MAP.put("scrv", ObjDataType.SCRV_SEPECIAL_CURVE);
            KEYWORD_MAP.put("sp", ObjDataType.SP_SPECIAL_POINT);
            KEYWORD_MAP.put("end", ObjDataType.END_END_STATEMENT);
            KEYWORD_MAP.put("con", ObjDataType.CON_CONNECT);
            KEYWORD_MAP.put("g", ObjDataType.G_GROUP_NAME);
            KEYWORD_MAP.put("s", ObjDataType.S_SMOOTHING_GROOP);
            KEYWORD_MAP.put("mg", ObjDataType.MG_MERGING_GROUP);
            KEYWORD_MAP.put("o", ObjDataType.O_OBJECT_NAME);
            KEYWORD_MAP.put("bevel", ObjDataType.BEVEL_BEVEL_INTERPOLATION);
            KEYWORD_MAP.put("c_interp", ObjDataType.C_INTERP_COLOR_INTERPOLATION);
            KEYWORD_MAP.put("d_interp", ObjDataType.D_INTERP_DISSOLVE_INTERPOLATION);
            KEYWORD_MAP.put("lod", ObjDataType.LOD_LEVEL_OF_DETAIL);
            KEYWORD_MAP.put("usemtl", ObjDataType.USEMTL_MATERIAL_NAME);
            KEYWORD_MAP.put("mtllib", ObjDataType.MTLLIB_MATERIAL_LIBRARY);
            KEYWORD_MAP.put("shadow_obj", ObjDataType.SHADOW_OBJ_SHADOW_CASTING);
            KEYWORD_MAP.put("trace_obj", ObjDataType.TRACE_OBJ_RAY_TRACING);
            KEYWORD_MAP.put("ctech", ObjDataType.CTECH_CURVE_APPROXIMATION_TECHNIQUE);
            KEYWORD_MAP.put("stech", ObjDataType.STECH_SURFACE_APPROXIMATION_TECHNIQUE);
            KEYWORD_MAP.put("#", ObjDataType.COMMENT_COMMENT);

        }

        public void parseObjFile(File file) throws IOException {
            objData.clear();
            byte[] bytes = Files.readAllBytes(file.toPath());
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
            Scanner scanner = new Scanner(stream);
            boolean loop = true;
            int currentState = STATE_READY;
            ObjDataType currentType = null;
            String currentLine = null;

            while (loop) {
                switch (currentState) {
                    case STATE_READY:
                        currentLine = null;
                        currentType = null;
                        currentState = STATE_READING_LINE;
                        break;
                    case STATE_READING_LINE:
                        if (scanner.hasNextLine()) {
                            currentLine = scanner.nextLine();
                            currentState = STATE_PARSING_TYPE;
                        } else {
                            currentState = STATE_COMPLETE;
                        }
                        break;
                    case STATE_PARSING_TYPE: {
                        if (currentLine == null) {
                            currentState = STATE_ERROR;
                            break;
                        }
                        String[] slices = currentLine.split(" ");
                        if (slices.length < 1) {
                            currentState = STATE_ERROR;
                            break;
                        }
                        String typeKeyword = slices[0];
                        currentType = KEYWORD_MAP.get(typeKeyword);
                        if (currentType == null) {
                            System.err.println("Warning: unrecognized data type: " + typeKeyword);
                            currentState = STATE_READING_LINE;
                        } else if (currentType.equals(ObjDataType.COMMENT_COMMENT)) {
                            currentState = STATE_READING_LINE;
                        } else {
                            if (objData.get(currentType) == null) {
                                objData.put(currentType, new ArrayList<float[]>());
                            }
                            currentState = STATE_PARSING_DATA;
                        }
                        break;
                    }
                    case STATE_PARSING_DATA: {
                        String[] slices = currentLine.split(" ");
                        float[] dataArray = new float[slices.length - 1];
                        for (int i = 1; i < slices.length; i ++) {
                            dataArray[i - 1] = Float.valueOf(slices[i]);
                        }
                        objData.get(currentType).add(dataArray);
                        currentState = STATE_READING_LINE;
                        break;
                    }
                    case STATE_COMPLETE:
                        loop = false;
                        break;
                    case STATE_ERROR:
                        loop = false;
                        break;
                }
            }
        }

    }

    private<T> void extractData(ObjParser parser, T object) {

    }

    public Map<ObjDataType, List<float[]>> read(File file) {
        if (!file.exists()) {
            System.err.println("File: " + file.getName() + " does NOT exist.");
            return null;
        }
        ObjParser parser = new ObjParser();
        try {
            parser.parseObjFile(file);
//            extractData(parser, object);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return parser.objData;
    }
}
