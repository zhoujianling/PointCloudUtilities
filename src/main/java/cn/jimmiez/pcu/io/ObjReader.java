package cn.jimmiez.pcu.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ObjReader {

    public enum ObjDataType {
        // vertex data
        V_GEOMETRIC_VERTICES,
        VT_TEXTURE_VERTICES,
        VN_VERTEX_NORMALS,
        VP_PARAMETER_SPACE_VERTICES,
        // free form curve/surface attributes
        DEG_DEGREE,
        BMAT_BASIS_MATRIX,
        STEP_STEP_SIZE,
        // elements
        P_POINT,
        L_LINE,
        F_FACE,
        CURV_CURVE,
        CURV2_2D_CURVE,
        PARM_PARAMETER_VALUES,
        TRIM_OUTER_TRIMMING_LOOP,
        HOLE_INNER_TRIMMING_LOOP,
        SCRV_SEPECIAL_CURVE,
        SP_SPECIAL_POINT,
        END_END_STATEMENT,
        CON_CONNECT,
        G_GROUP_NAME,
        S_SMOOTHING_GROOP,
        MG_MERGING_GROUP,
        O_OBJECT_NAME,
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

    }

    private class ObjParser {

        private Map<ObjDataType, float[]> objData = new HashMap<>();

        private static final int STATE_READY = 0;
        private static final int STATE_READING_TYPE = 1;
        private static final int STATE_READING_DATA = 2;
        private static final int STATE_COMPLETE = 5;
        private static final int STATE_ERROR = 6;

        public void parseObjFile(File file) throws IOException {
            objData.clear();
            byte[] bytes = Files.readAllBytes(file.toPath());
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
            Scanner scanner = new Scanner(stream);
            boolean loop = true;
            int currentState = STATE_READY;
            ObjDataType currentType;

            while (loop) {
                switch (currentState) {
                    case STATE_READY:
                        currentState = STATE_READING_TYPE;
                        break;
                    case STATE_READING_TYPE:
//                        String
                        break;
                    case STATE_READING_DATA:
                        break;
                    case STATE_COMPLETE:
                        break;
                    case STATE_ERROR:
                        break;
                }
            }
        }

    }

    private<T> void extractData(ObjParser parser, T object) {

    }

    public <T> T read(File file, Class<T> clazz) {
        if (!file.exists()) {
            System.err.println("File: " + file.getName() + " does NOT exist.");
            return null;
        }
        T object = null;
        try {
            object = clazz.newInstance();
            ObjParser parser = new ObjParser();
            parser.parseObjFile(file);
            extractData(parser, object);
            // TODO read from obj file
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return object;
    }
}
