package cn.jimmiez.pcu.io.off;

import cn.jimmiez.pcu.util.PcuReflectUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    private void readImpl(Scanner scanner, OffData data) throws IOException {
        int state = STATE_READY;
        boolean loop = true;
        OffHeader header = new OffHeader();
        String message = null;
        while (loop) {
            switch (state) {
                case STATE_READY: {
                    state = STATE_PARSE_HEADER_KEYWORD;
                    break;
                }
                case STATE_PARSE_HEADER_KEYWORD: {
                    String line = fetchNextLine(scanner);
                    if (line == null) {
                        state = STATE_ERROR;
                        message = "Cannot read first line";
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
                        state = STATE_ERROR;
                        message = "Cannot read the number of dimensions";
                        break;
                    }
                    int dimen = Integer.valueOf(line);
                    if (dimen >= 1) {
                        header.setDimension(dimen);
                        state = STATE_PARSE_ELEMENT_NUM;
                    } else {
                        state = STATE_ERROR;
                        message = "Incorrect vertex dimension.";
                    }
                    break;
                }
                case STATE_PARSE_ELEMENT_NUM: {
                    String line = fetchNextLine(scanner);
                    if (line == null) {
                        state = STATE_ERROR;
                        message = "Cannot read number of vertices and faces.";
                        break;
                    }
                    String[] nums = line.split(" ");
                    int verticesNum = -1, facesNum = -1, edgesNum = -1;
                    if (nums.length < 2) {
                        message = "Too few numbers for parsing element size.";
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
                        message = "Invalid number of vertices.";
                        state = STATE_ERROR;
                        break;
                    }
                    boolean success = true;
                    for (int i = 0; i < header.getVerticesNum(); i ++) {
                        String line = fetchNextLine(scanner);
                        if (line == null) {
                            message = "Fewer vertices than expected.";
                            success = false;
                            break;
                        }
                        int expectedLength = 3;
                        String[] values = line.trim().split("(\\s)+");
                        float[] xyz = new float[3];
                        int j;
                        for (j = 0; j < 3; j ++) xyz[j] = Float.valueOf(values[j]);
                        data.vertices.add(xyz);

                        if (header.hasTextureCoordinates()) {
                            System.out.println("Texture coordinates are currently unsupported.");
                            break;
                        }
                        if (header.hasNormal()) {
                            expectedLength += 3;
                            if (values.length >= expectedLength) {
                                float[] normal = new float[3];
                                for (; j < expectedLength; j ++) normal[j - 3] = Float.valueOf(values[j]);
                                data.vertexNormals.add(normal);
                            } else {
                                System.err.println("Fewer float values than expected.");
                                break;
                            }
                        }
                        if (header.hasColor()) {
                            expectedLength += 4;
                            if (values.length >= expectedLength) {
                                float[] rgba = new float[4];
                                for (; j < expectedLength; j ++) rgba[j - (expectedLength - 4)] = Float.valueOf(values[j]);
                                data.vertexColors.add(rgba);
                            }
                        }

                    }
                    if (success) {
                        state = STATE_READING_FACES;
                    } else {
                        if (message == null) message = "Fail to read vertice information.";
                        state = STATE_ERROR;
                    }
                    break;
                }
                case STATE_READING_FACES: {
                    if (header.getFacesNum() == OffHeader.UNSET) {
                        message = "Invalid number of faces.";
                        state = STATE_ERROR;
                        break;
                    }
                    boolean success = true;
                    for (int i = 0; i < header.getFacesNum(); i ++) {
                        String line = fetchNextLine(scanner);
                        if (line == null) {
                            message = "Fewer faces than expected.";
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
                    throw new IOException(message);
                }
            }

        }
    }

    public OffData read(File file) throws IOException {
        Scanner scanner = new Scanner(file);
        OffData data = new OffData();
        readImpl(scanner, data);
        return data;
    }

    private void injectData(OffData data, Object object) throws InvocationTargetException, IllegalAccessException {
        List<Method> methods = PcuReflectUtil.fetchAllMethods(object);
        for (Method method : methods) {
            ReadFromOff annotation = method.getAnnotation(ReadFromOff.class);
            if (annotation != null) {
                if (annotation.dataType() == ReadFromOff.VERTICES) {
                    injectVerticesData(method, data, object);
                } else if (annotation.dataType() == ReadFromOff.FACES) {
                    injectFacesData(method, data, object);
                } else if (annotation.dataType() == ReadFromOff.VERTEX_COLORS) {
                    injectVertexColorsData(method, data, object);
                } else if (annotation.dataType() == ReadFromOff.VERTEX_NORMALS) {
                    injectVertexNormalsData(method, data, object);
                } else if (annotation.dataType() == ReadFromOff.FACE_COLORS) {
                    injectFaceColorsData(method, data, object);
                }
            }
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
    private void injectVertexNormalsData(Method method, OffData data, Object object) throws InvocationTargetException, IllegalAccessException {
        List<float[]> list = (List<float[]>) method.invoke(object);
        list.addAll(data.vertexNormals);
    }

    @SuppressWarnings("unchecked")
    private void injectFaceColorsData(Method method, OffData data, Object object) throws InvocationTargetException, IllegalAccessException {
        List<float[]> list = (List<float[]>) method.invoke(object);
        list.addAll(data.faceColors);
    }

    public <T> T read(File file, Class<T> clazz) throws IOException {
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
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return object;
    }

}
