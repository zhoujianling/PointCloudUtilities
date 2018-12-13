package cn.jimmiez.pcu.io.off;

import cn.jimmiez.pcu.util.PcuReflectUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Scanner;

/**
 * Object File Format(off / OFF) is used to represent the geometry of polygonal mesh by
 * specifying the vertices and faces.
 */
public class OffReader {

    private static final int STATE_READY = 0;
    private static final int STATE_PARSE_HEADER_KEYWORD = 1;
    private static final int STATE_PARSE_HEADER_N_DIM = 2;
    private static final int STATE_PARSE_ELEMENT_NUM = 3;
    private static final int STATE_READING_VERTICES = 4;
    private static final int STATE_READING_FACES= 5;
    private static final int STATE_COMPLETE = 6;
    private static final int STATE_ERROR = 7;

    private String fetchNextLine(Scanner scanner) {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("#")) continue;
            return line;
        }
        return null;
    }

    private void readImpl(Scanner scanner, OffData data) {
        int state = STATE_READY;
        boolean loop = true;
        OffHeader header = new OffHeader();
        while (loop) {
            switch (state) {
                case STATE_READY: {
                    state = STATE_PARSE_HEADER_KEYWORD;
                    break;
                }
                case STATE_PARSE_HEADER_KEYWORD: {
                    String line = fetchNextLine(scanner);
                    if (line == null) {
                        System.err.println("Cannot read first line.");
                        state = STATE_ERROR;
                        break;
                    }
                    String keyword = line.trim();
                    if (keyword.endsWith("OFF")) {
                        header.setName(keyword);
                        if (keyword.contains("ST")) header.setHasTextureCoordinates(true);
                        if (keyword.contains("N")) header.setHasNormal(true);
                        if (keyword.contains("C")) header.setHasColor(true);
                        if (keyword.contains("4")) header.setHas4Components(true);
                        if (keyword.contains("n")) header.setIfDimensionSpecified(true);
                    }
                    // some files may omit OFF keyword in first line
                    if (header.isDimensionSpecified()) {
                        state = STATE_PARSE_HEADER_N_DIM;
                    } else {
                        state = STATE_PARSE_ELEMENT_NUM;
                    }
                    break;
                }
                case STATE_PARSE_HEADER_N_DIM: {
                    String line = fetchNextLine(scanner);
                    if (line == null) {
                        System.err.println("Cannot read number of dimensions.");
                        state = STATE_ERROR;
                        break;
                    }
                    int dimen = Integer.valueOf(line);
                    header.setDimension(dimen);
                    state = STATE_PARSE_ELEMENT_NUM;
                    break;
                }
                case STATE_PARSE_ELEMENT_NUM: {
                    String line = fetchNextLine(scanner);
                    if (line == null) {
                        System.err.println("Cannot read number of vertices and faces.");
                        state = STATE_ERROR;
                        break;
                    }
                    String[] nums = line.split(" ");
                    int verticesNum = -1, facesNum = -1, edgesNum = -1;
                    if (nums.length < 2) {
                        System.err.println("Too few numbers for parsing element size.");
                        state = STATE_ERROR;
                        break;
                    }
                    verticesNum = Integer.valueOf(nums[0]);
                    facesNum = Integer.valueOf(nums[1]);
                    if (nums.length > 2){
                        edgesNum = Integer.valueOf(nums[2]);
                    }
                    if (verticesNum >= 0) header.setVerticesNum(verticesNum);
                    if (facesNum >= 0) header.setFacesNum(facesNum);
                    if (edgesNum >= 0) header.setEdgesNum(edgesNum);
                    data.setHeader(header);
                    state = STATE_READING_VERTICES;
                    break;
                }
                case STATE_READING_VERTICES: {
                    if (header.getVerticesNum() == OffHeader.UNSET) {
                        System.err.println("Invalid number of vertices.");
                        state = STATE_ERROR;
                        break;
                    }
                    boolean success = true;
                    for (int i = 0; i < header.getVerticesNum(); i ++) {
                        String line = fetchNextLine(scanner);
                        if (line == null) {
                            System.err.println("Fewer vertices than expected.");
                            success = false;
                            break;
                        }

                        String[] values = line.trim().split("(\\s)+");
                        float[] xyz = new float[3];
                        int j;
                        for (j = 0; j < 3; j ++) xyz[j] = Float.valueOf(values[j]);
                        data.vertices.add(xyz);

//                        if (values.length >= 7) {
//                            float[] rgba = new float[4];
//                            for (; j < 7; j ++) rgba[j - 3] = Float.valueOf(values[j]);
//                            data.vertexColors.add(rgba);
//                        }

                    }
                    if (success) {
                        state = STATE_READING_FACES;
                    } else {
                        state = STATE_ERROR;
                    }
                    break;
                }
                case STATE_READING_FACES: {
                    if (header.getFacesNum() == OffHeader.UNSET) {
                        System.err.println("Invalid number of faces.");
                        state = STATE_ERROR;
                        break;
                    }
                    boolean success = true;
                    for (int i = 0; i < header.getFacesNum(); i ++) {
                        String line = fetchNextLine(scanner);
                        if (line == null) {
                            System.err.println("Fewer faces than expected.");
                            success = false;
                            break;
                        }
                        String[] values = line.trim().split("(\\s)+");
                        int arrayLength = Integer.valueOf(values[0]);
                        int[] indices = new int[arrayLength];
                        int j;
                        for (j = 1; j < arrayLength + 1; j ++) indices[j - 1] = Integer.valueOf(values[j]);
                        data.faces.add(indices);

                        if (values.length >= arrayLength + 5) {
                            float[] rgba = new float[4];
                            for (; j < arrayLength + 5; j ++) rgba[j - arrayLength - 1] = Float.valueOf(values[j]);
                            data.faceColors.add(rgba);
                        }
                    }

                    if (success) {
                        state = STATE_COMPLETE;
                    } else {
                        state = STATE_ERROR;
                    }
                    break;
                }
                case STATE_COMPLETE: {
                    loop = false;
                    break;
                }
                case STATE_ERROR: {
                    loop = false;
                    break;
                }
            }

        }
    }

    public OffData read(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        OffData data = new OffData();
        readImpl(scanner, data);
        return data;
    }

    private void injectData(OffData data, Object object) {
        List<Method> methods = PcuReflectUtil.fetchAllMethods(object);
        try {
            for (Method method : methods) {
                ReadFromOff annotation = method.getAnnotation(ReadFromOff.class);
                if (annotation != null) {
                    if (annotation.dataType() == ReadFromOff.VERTICES) {
                        injectVerticesData(method, data, object);
                    } else if (annotation.dataType() == ReadFromOff.FACES) {
                        injectFacesData(method, data, object);
                    } else if (annotation.dataType() == ReadFromOff.VERTEX_COLORS) {
                        injectVertexColorsData(method, data, object);
                    } else if (annotation.dataType() == ReadFromOff.FACE_COLORS) {
                        injectFaceColorsData(method, data, object);
                    }
                }
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            System.err.println("Privater getter with ReadFromOff annotation.");
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void injectVerticesData(Method method, OffData data, Object object) throws InvocationTargetException, IllegalAccessException {
        List<float[]> list = (List<float[]>) method.invoke(object);
        list.addAll(data.vertices);
    }

    @SuppressWarnings("unchecked")
    private void injectFacesData(Method method, OffData data, Object object) throws InvocationTargetException, IllegalAccessException {
        List<int[]> list = (List<int[]>) method.invoke(object);
        list.addAll(data.faces);
    }

    @SuppressWarnings("unchecked")
    private void injectVertexColorsData(Method method, OffData data, Object object) throws InvocationTargetException, IllegalAccessException {
        List<float[]> list = (List<float[]>) method.invoke(object);
        list.addAll(data.vertexColors);
    }

    @SuppressWarnings("unchecked")
    private void injectFaceColorsData(Method method, OffData data, Object object) throws InvocationTargetException, IllegalAccessException {
        List<float[]> list = (List<float[]>) method.invoke(object);
        list.addAll(data.faceColors);
    }

    public <T> T read(File file, Class<T> clazz) throws FileNotFoundException {
        OffData data = read(file);
        // inject into object ....
        T object = null;
        try {
            object= clazz.newInstance();
            injectData(data, object);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return object;
    }

}
