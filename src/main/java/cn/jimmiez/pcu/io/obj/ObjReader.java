package cn.jimmiez.pcu.io.obj;

import cn.jimmiez.pcu.util.Pair;
import cn.jimmiez.pcu.util.PcuReflectUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;

public class ObjReader {


    private static class ObjParser {

        //private Map<ObjDataType, List<float[]>> objData = new HashMap<>();
        private ObjData objData = new ObjData();

        private static final int STATE_READY = 0;
        private static final int STATE_READING_LINE = 1;
        private static final int STATE_PARSING_TYPE = 2;
        private static final int STATE_PARSING_DATA = 3;
        private static final int STATE_COMPLETE = 9;
        private static final int STATE_ERROR = 10;
        private Map<String, ObjDataType> keywordMap = new HashMap<>();

        ObjParser() {
            ObjDataType[] enumValues = ObjDataType.class.getEnumConstants();
            for (ObjDataType type : enumValues) {
                keywordMap.put(type.getKeyword(), type);
            }
        }

        void parseObjFile(File file) throws IOException {
            objData.clear();
            byte[] bytes = Files.readAllBytes(file.toPath());
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
            Scanner scanner = new Scanner(stream);
            boolean loop = true;
            int currentState = STATE_READY;
            ObjDataType currentType = null;
            String currentLine = null;
            String errorMessage = null;
            String[] slices = null;

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
                            // '\' may occur in the matrix definition
                            while (currentLine.endsWith("\\") && scanner.hasNextLine()) {
                                currentLine = currentLine + scanner.nextLine();
                            }
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
                        slices = currentLine.split("(\\s)+");
                        if (slices.length < 1) {
                            currentState = STATE_ERROR;
                            break;
                        }
                        String typeKeyword = slices[0];
                        currentType = keywordMap.get(typeKeyword);
                        if (currentType == null) {
                            System.err.println("Warning: unrecognized data type: " + typeKeyword);
                            currentState = STATE_READING_LINE;
                        } else {
                            currentState = STATE_PARSING_DATA;
                        }
                        break;
                    }
                    case STATE_PARSING_DATA: {
                        try {
                            parseObjData(currentType, slices);
                        } catch (IOException e) {
                            currentState = STATE_ERROR;
                            errorMessage = e.getMessage();
                            break;
                        }
                        currentState = STATE_READING_LINE;
                        break;
                    }
                    case STATE_COMPLETE:
                        loop = false;
                        break;
                    case STATE_ERROR:
                        System.err.println(errorMessage);
                        loop = false;
                        break;
                }
            }
        }

        private void parseObjData(ObjDataType type, String[] slices) throws IOException {
            if (slices.length < 2) throw new IOException("Expect the value of " + type.getKeyword() + ".");
            switch (type) {
                /* vertex data */
                case V_GEOMETRIC_VERTICES:
                case VT_TEXTURE_VERTICES:
                case VN_VERTEX_NORMALS:
                case VP_PARAMETER_SPACE_VERTICES: {
                    double[] dataArray = new double[slices.length - 1];
                    for (int i = 1; i < slices.length; i++) {
                        dataArray[i - 1] = Double.valueOf(slices[i]);
                    }
                    objData.vectorData.get(type).add(dataArray);
                }
                    break;

                /* free-form curve/surface attributes */
                case CSTYPE_RATIONAL_FORMS_OF_CURVE_ETC:
                    // spec p.11
                    // cstype rat type
                case DEG_DEGREE:
                case BMAT_BASIS_MATRIX:
                case STEP_STEP_SIZE:
                    System.err.println("Warning: unsupported data type: " + type.getKeyword());
                    break;

                /* elements */
                case P_POINT:
                case L_LINE:
                case F_FACE: {
                    // spec p.17
                    int cnt = 0;
                    Pair<Integer, List<double[]>> pair = objData.elementData.get(type);
                    int indicesCntPerVertex = pair.getKey();
                    if (indicesCntPerVertex == 0) {
                        indicesCntPerVertex = slices[1].split("/").length;
                        pair.setKey(indicesCntPerVertex);
                    }
                    double[] dataArray = new double[(slices.length - 1) * indicesCntPerVertex];
                    for (int i = 1; i < slices.length; i++) {
                        String[] subArrays = slices[i].split("/");
                        for (String subArray : subArrays) {
                            dataArray[cnt ++] = Double.valueOf(subArray);
                        }
                    }
                    pair.getValue().add(dataArray);
                }
                    break;
                case CURV_CURVE:
                case CURV2_2D_CURVE:
                case SURF_SURFACE:
                    System.err.println("Warning: unsupported data type: " + type.getKeyword());
                    break;

                /* free-form curve/surface body statements */
                case PARM_PARAMETER_VALUES:
                case TRIM_OUTER_TRIMMING_LOOP:
                case HOLE_INNER_TRIMMING_LOOP:
                case SCRV_SEPECIAL_CURVE:
                case SP_SPECIAL_POINT:
                case END_END_STATEMENT:
                    System.err.println("Warning: unsupported data type: " + type.getKeyword());
                    break;

                /* connection */
                case CON_CONNECT:
                    System.err.println("Warning: unsupported data type: " + type.getKeyword());
                    break;

                /* grouping */
                case G_GROUP_NAME:
                case S_SMOOTHING_GROOP:
                case MG_MERGING_GROUP:
                case O_OBJECT_NAME:
                    System.err.println("Warning: unsupported data type: " + type.getKeyword());
                    break;

                /* display / render attributes */
                case BEVEL_BEVEL_INTERPOLATION:
                case C_INTERP_COLOR_INTERPOLATION:
                case D_INTERP_DISSOLVE_INTERPOLATION:
                case LOD_LEVEL_OF_DETAIL:
                    System.err.println("Warning: unsupported data type: " + type.getKeyword());
                    break;
                case USEMTL_MATERIAL_NAME:
                case MTLLIB_MATERIAL_LIBRARY:
                    objData.textData.put(type, slices[1]);
                    break;
                case SHADOW_OBJ_SHADOW_CASTING:
                case TRACE_OBJ_RAY_TRACING:
                case CTECH_CURVE_APPROXIMATION_TECHNIQUE:
                case STECH_SURFACE_APPROXIMATION_TECHNIQUE:
                    break;
                case COMMENT_COMMENT:
                    // do nothing
                    default:
                    break;
            }

        }

    }

    public ObjData read(File file) {
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
