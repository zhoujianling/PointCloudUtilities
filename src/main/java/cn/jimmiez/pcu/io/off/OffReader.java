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
    private static final int STATE_CHECK_HEADER = 1;
    private static final int STATE_READING_ELEMENT_NUM = 2;
    private static final int STATE_READING_VERTICES = 3;
    private static final int STATE_READING_FACES= 4;
    private static final int STATE_COMPLETE = 5;
    private static final int STATE_ERROR = 6;

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
        while (loop) {
            switch (state) {
                case STATE_READY: {
                    state = STATE_CHECK_HEADER;
                    break;
                }
                case STATE_CHECK_HEADER: {
                    String line = fetchNextLine(scanner);
                    if (line == null) {
                        System.err.println("Cannot read first line.");
                        state = STATE_ERROR;
                        break;
                    }
                    if (line.toLowerCase().equals("off")) {
                        state = STATE_READING_ELEMENT_NUM;
                    } else {
                        System.err.println("Cannot recognize the format of this file.");
                        state = STATE_ERROR;
                    }
                    break;
                }
                case STATE_READING_ELEMENT_NUM: {
                    String line = fetchNextLine(scanner);
                    if (line == null) {
                        System.err.println("Cannot read number of vertices and faces.");
                        state = STATE_ERROR;
                        break;
                    }
                    String[] nums = line.split(" ");
                    if (nums.length != 3) {
                        state = STATE_ERROR;
                    } else {
                        data.verticesNum = Integer.valueOf(nums[0]);
                        data.facesNum = Integer.valueOf(nums[1]);
                        data.edgesNum = Integer.valueOf(nums[2]);
                        state = STATE_READING_VERTICES;
                    }
                    break;
                }
                case STATE_READING_VERTICES: {
                    if (data.verticesNum == OffData.UNSET) {
                        System.err.println("Invalid number of vertices.");
                        state = STATE_ERROR;
                        break;
                    }
                    boolean success = true;
                    for (int i = 0; i < data.verticesNum; i ++) {

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

                        if (values.length >= 7) {
                            float[] rgba = new float[4];
                            for (; j < 7; j ++) rgba[j - 3] = Float.valueOf(values[j]);
                            data.vertexColors.add(rgba);
                        }

                    }
                    if (success) {
                        state = STATE_READING_FACES;
                    } else {
                        state = STATE_ERROR;
                    }
                    break;
                }
                case STATE_READING_FACES: {
                    if (data.facesNum == OffData.UNSET) {
                        System.err.println("Invalid number of faces.");
                        state = STATE_ERROR;
                        break;
                    }
                    boolean success = true;
                    for (int i = 0; i < data.facesNum; i ++) {
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
