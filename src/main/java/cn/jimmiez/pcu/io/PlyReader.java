package cn.jimmiez.pcu.io;

import cn.jimmiez.pcu.model.PcuElement;
import cn.jimmiez.pcu.model.PcuPointCloud;
import cn.jimmiez.pcu.Constants;
import cn.jimmiez.pcu.util.PcuArrayUtil;
import cn.jimmiez.pcu.util.PcuReflectUtil;
import javafx.util.Pair;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.List;

public class PlyReader implements  MeshReader{

    /** constant representing ply format **/
    public static final int FORMAT_ASCII = 0x3001;
    public static final int FORMAT_BINARY_BIG_ENDIAN = 0x3002;
    public static final int FORMAT_BINARY_LITTLE_ENDIAN = 0x3003;

    public static final int FORMAT_NON_FORMAT = - 0x3001;

    private static final int TYPE_LIST = 0x1001;

    private static final int TYPE_LOW_BOUNDS = 0x0000;
    private static final int TYPE_UPPER_BOUNDS = 0x000B;

    /** scalar type **/
    public static final int TYPE_CHAR = 0x0001;
    public static final int TYPE_UCHAR = 0x0002;
    public static final int TYPE_SHORT = 0x0003;
    public static final int TYPE_USHORT = 0x0004;
    public static final int TYPE_INT = 0x0005;
    public static final int TYPE_UINT = 0x0006;
    public static final int TYPE_FLOAT = 0x0007;
    public static final int TYPE_DOUBLE = 0x0008;

    /**
     * The size of type：
     * NON_TYPE, CHAR, UCHAR, SHORT, USHORT, INT, UINT, FLOAT, DOUBLE, RESERVED, RESERVED
     * the index of array TYPE_SIZE is the constant value of TYPE_*
     **/
    private static final int[] TYPE_SIZE = new int[] {
            0, 1, 1, 2, 2, 4, 4, 4, 8, 0, 0
    };

    public static final Integer TYPE_NONTYPE =  0x0000;

    public PlyHeader readHeaderThenCloseFile(File file) throws IOException {
        PlyHeader result;
        FileReader reader = new FileReader(file);
        result = readHeader(reader);
        reader.close();
        return result;
    }
    public PlyHeader readHeader(FileReader reader) throws IOException {
        Scanner scanner = new Scanner(reader);
        return readHeader(scanner);
    }

    private PlyHeader readHeader(Scanner scanner) throws IOException {
        PlyHeader header = new PlyHeader();
        List<String> headerLines = new ArrayList<>();
        try {
            String line = scanner.nextLine();
            while (! line.equals("end_header")) {
                if (! line.startsWith("comment ")) {
                    headerLines.add(line);
                }
                line = scanner.nextLine();
            }
        } catch (NoSuchElementException e) {
            throw new IOException("Invalid ply file: Cannot find end of header.");
        }
        if (headerLines.size() < 1) {
            throw new IOException("Invalid ply file: No data");
        }
        String firstLine = headerLines.get(0);
        if (! firstLine.equals(Constants.MAGIC_STRING)) {
            throw new IOException("Invalid ply file: Ply file does not start with ply.");
        }
        String secondLine = headerLines.get(1);
        readPlyFormat(secondLine, header);
        for (int lineNo = 2; lineNo < headerLines.size();) {
            String elementLine = headerLines.get(lineNo);
            Pair<String, Integer> pair = readPlyElement(elementLine);
            lineNo += 1;
            int propertyStartNo = lineNo;
            while (lineNo < headerLines.size() && headerLines.get(lineNo).startsWith("property ")) lineNo++;
            PlyElement element = new PlyElement(lineNo - propertyStartNo);
            for (int i = propertyStartNo; i < lineNo; i ++) {
                String[] propertySlices = headerLines.get(i).split(" ");
                if (propertySlices.length < 3) throw new IOException("Invalid ply file.");
                element.propertiesName[i - propertyStartNo] = propertySlices[propertySlices.length - 1];
                element.propertiesType[i - propertyStartNo] = recognizeType(propertySlices[1]);
                if (element.propertiesType[i - propertyStartNo] == TYPE_LIST) {
                    if (propertySlices.length < 5) throw new IOException("Invalid ply file. Wrong list property.");
                    element.listType1 = recognizeType(propertySlices[2]);
                    element.listType2 = recognizeType(propertySlices[3]);
                }
            }
            header.elementTypes.put(pair.getKey(), element);
            header.elementsNumber.add(pair);
        }
        return header;
    }

    private int recognizeType(String type) {
        switch (type) {
            case "char":
                return TYPE_CHAR;
            case "uchar":
                return TYPE_UCHAR;
            case "int":
                return TYPE_INT;
            case "uint":
                return TYPE_UINT;
            case "short":
                return TYPE_SHORT;
            case "ushort":
                return TYPE_USHORT;
            case "float":
                return TYPE_FLOAT;
            case "double":
                return TYPE_DOUBLE;
            case "list":
                return TYPE_LIST;
        }
        return TYPE_NONTYPE;
    }

    private Pair<String, Integer> readPlyElement(String line) throws IOException {
        String[] elementSlices = line.split(" ");
        if (! line.startsWith("element ") || elementSlices.length < 3) {
            throw new IOException("Invalid ply file: Invalid format.");
        }
        String elementName = elementSlices[1];
        Integer elementNumber = Integer.valueOf(elementSlices[2]);
        return new Pair<>(elementName, elementNumber);
    }

    private void readPlyFormat(String line, PlyHeader header) throws IOException {
        if (!line.startsWith("format ")) {
            throw new IOException("Invalid ply file: No format information");
        }
        String[] formatSlices = line.split(" ");
        if (formatSlices.length == 3) {
            if (formatSlices[1].equals("ascii")) {
                header.plyFormat = FORMAT_ASCII;
            } else if (formatSlices[1].equals("binary_little_endian")) {
                header.plyFormat = FORMAT_BINARY_LITTLE_ENDIAN;
            } else if (formatSlices[1].equals("binary_big_endian")) {
                header.plyFormat = FORMAT_BINARY_BIG_ENDIAN;
            }
            header.plyVersion = Float.valueOf(formatSlices[2]);
        } else {
            throw new IOException("Invalid ply file: Wrong format ply in line");
        }
    }

    public <T> void readPointCloud(String fileName, T object, ReadListener<T> listener) {
        File file = new File(fileName);
        readPointCloud(file, object, listener);
    }


    public <T> T readPointCloud(String fileName, Class<T> clazz) {
        T object = null;
//        final Boolean readSuccess = false;
        try {
            object = clazz.newInstance();
            readPointCloud(fileName, object, new ReadListener<T>() {
                @Override
                public void onSucceed(T pointCloud, PlyHeader header) {
//                    readSuccess = ;
                }

                @Override
                public void onFail(int code, String message) {
                    System.err.println("Read point cloud failed, error code: " + code);
                    System.err.println(message);
                }
            });
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return object;
    }

    private void readBinaryPointCloud(PlyHeader header, FileInputStream stream, ReadListener listener, ByteOrder order) throws IOException {
        /**
        PcuPointCloud pointCloud = new PcuPointCloud();
        for (int i = 0; i < header.elementsNumber.size(); i ++) {
            String elementName = header.elementsNumber.get(i).getKey();
            int elementNum = header.elementsNumber.get(i).getValue();
            PlyElement element = header.elementTypes.get(elementName);
            boolean isVertex = elementName.equals("vertex") || elementName.equals("vertices");
            int size = sizeOfElement(element);
            if (! isVertex) {
                stream.skip(elementNum * size);
                continue;
            }

            for (int j = 0; j < elementNum; j ++) {
                float[] point = new float[3];
                for (int k = 0; k < element.getPropertiesType().length; k ++) {

                    int type = element.getPropertiesType()[k];
                    if (type > TYPE_LOW_BOUNDS && type < TYPE_UPPER_BOUNDS) {
                        if (k > 2) {
                            stream.skip(TYPE_SIZE[type]);
                        } else {
                            byte[] bytes = new byte[TYPE_SIZE[type]];
                            stream.read(bytes);
                            point[k] = bytes2double(type, bytes, order);
                        }
                    }
                }
                pointCloud.getPoint3ds().add(point);
            }
            listener.onSucceed(pointCloud, header);
            break;
        }
         **/
    }

    private double bytes2double(int type, byte[] bytes, ByteOrder order) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(order);
        switch (type) {
            case TYPE_INT:
            case TYPE_UINT:
                return (double)buffer.getInt();
            case TYPE_SHORT:
            case TYPE_USHORT:
                return (double)buffer.getShort();
            case TYPE_FLOAT:
                return (double)buffer.getFloat();
            case TYPE_DOUBLE:
                return buffer.getDouble();
        }
        return 0;
    }

    /**
     * @param element all data types in the element must be scalar type
     * @return size of a element
     */
    private int sizeOfElement(PlyElement element) {
        int size = 0;

        return size;
    }


    private List<Method> findElementGetter(List<Method> methods, String elementName) {
        List<Method> getters = new ArrayList<>();
        for (Method m : methods) {
//            if (! methodsust.()) continue;
            PcuElement pcuEle = m.getAnnotation(PcuElement.class);
            if (pcuEle == null) continue;
            int position = PcuArrayUtil.find(pcuEle.alternativeNames(), elementName);
            if (position < 0) continue;
            getters.add(m);
        }
        return getters;
    }

    private void skipAsciiElement(int num, Scanner scanner) {
        for (int i = 0; i < num; i ++) {
            scanner.nextLine();
        }
    }

    @SuppressWarnings("unchecked")
    private void parseAsciiElement(String line, List<int[]> indicesList, PlyElement element, List<List> data) throws IOException {
        String[] slices = line.split(" ");
        if (element.listType1 == TYPE_NONTYPE) {
            if (slices.length != element.getPropertiesType().length) throw new IOException("Invalid ply file.");
            for (int i = 0; i < data.size(); i ++) {
                int[] indices = indicesList.get(i);
                if (indices.length < 1) continue;
                switch (element.propertiesType[indices[0]]) {
                    case TYPE_DOUBLE: {
                        double[] values = new double[indices.length];
                        for (int j = 0; j < indices.length; j ++) {
                            values[j] = Double.valueOf(slices[indices[j]]);
                        }
                        data.get(i).add(values);
                    }
                        break;
                    case TYPE_FLOAT: {
                        float[] values = new float[indices.length];
                        for (int j = 0; j < indices.length; j ++) {
                            values[j] = Float.valueOf(slices[indices[j]]);
                        }
                        data.get(i).add(values);
                    }
                        break;
                    case TYPE_INT:
                    case TYPE_UINT:{
                        int[] values = new int[indices.length];
                        for (int j = 0; j < indices.length; j ++) {
                            values[j] = Integer.valueOf(slices[indices[j]]);
                        }
                        data.get(i).add(values);
                    }
                        break;
                    case TYPE_SHORT:
                    case TYPE_USHORT: {
                        short[] values = new short[indices.length];
                        for (int j = 0; j < indices.length; j ++) {
                            values[j] = Short.valueOf(slices[indices[j]]);
                        }
                        data.get(i).add(values);
                    }
                        break;
                    case TYPE_CHAR:
                    case TYPE_UCHAR: {
                        byte[] values = new byte[indices.length];
                        for (int j = 0; j < indices.length; j ++) {
                            values[j] = Byte.valueOf(slices[indices[j]]);
                        }
                        data.get(i).add(values);

                    }
                        break;

                }
            }
        } else if (element.getPropertiesType()[0] == TYPE_LIST){

        } else {
            System.err.println("Invalid ply file.");
            throw new IllegalArgumentException("Invalid entity class, please check the annotation.");
        }
    }

    @SuppressWarnings("unchecked")
    private void readAsciiPly(PlyHeader header, Scanner scanner, Object pointCloud, ReadListener listener) throws InvocationTargetException, IllegalAccessException, IOException {

        List<Method> methods = PcuReflectUtil.fetchAllMethods(pointCloud);
        for (int i = 0; i < header.elementsNumber.size(); i ++) {
            String elementName = header.elementsNumber.get(i).getKey();
            int elementNum = header.elementsNumber.get(i).getValue();
            PlyElement element = header.elementTypes.get(elementName);
            List<Method> getters = findElementGetter(methods, elementName);
            List<List> data = new ArrayList<>();
            List<PcuElement> userDefinedEles = new ArrayList<>();
            List<int[]> indicesList = new ArrayList<>();

            for (Method m : getters) {
                Object listObj = m.invoke(pointCloud);
                if (listObj != null && (listObj instanceof List)) {
                    data.add((List) listObj);
                    userDefinedEles.add(m.getAnnotation(PcuElement.class));
                }
            }

            if (userDefinedEles.size() < 1) {
                skipAsciiElement(elementNum, scanner);
                continue;
            }

            for (PcuElement ele : userDefinedEles) {
               int[] indices = new int[ele.properties().length];
               for (int j = 0; j < ele.properties().length; j ++) {
                   for (int k = 0; k < element.propertiesName.length; k ++) {
                       if (ele.properties()[j].equals(element.propertiesName[k])) {
                            indices[j] = k;
                       }
                   }
               }
               indicesList.add(indices);
            }

            for (int j = 0; j < elementNum; j ++) {
                String line = scanner.nextLine();
                parseAsciiElement(line, indicesList, element, data);
            }
        }
//        PcuPointCloud cloud = new PcuPointCloud();
//        PlyElement element4Point = header.elementTypes.get("vertex") != null ? header.elementTypes.get("vertex") : header.elementTypes.get("vertices");
//        if (header.elementsNumber.size() < 1 || element4Point == null) {
//            throw new IllegalStateException("Not a valid header for 3d point cloud.");
//        }
//        int vertexElementIndex;
//        for (vertexElementIndex = 0; vertexElementIndex < header.elementsNumber.size(); vertexElementIndex ++) {
//            if (header.elementsNumber.get(vertexElementIndex).getKey().equals("vertex")
//                    || header.elementsNumber.get(vertexElementIndex).getKey().equals("vertices")) {
//                break;
//            }
//        }
//
//        int pointsNumber = header.elementsNumber.get(vertexElementIndex).getValue();
//        /** read points iteratively **/
//        for (int j = 0; j < pointsNumber; j ++) {
//            double[] point = new double[3];
//            point[0] = scanner.nextDouble();
//            point[1] = scanner.nextDouble();
//            point[2] = scanner.nextDouble();
//            cloud.getPoint3ds().add(point);
//            for (int k = 3; k < element4Point.getPropertiesType().length; k++) {
//                scanner.nextDouble(); // nextDouble() is able to skip int, float, long ...
//            }
//        }
        listener.onSucceed(pointCloud, header);

    }

    /**
     * read a 3d point cloud from a ply file.
     * It is supposed that the properties of vertex is listed in such order:
     * [ x, y, z, other data types ... ]
     * @param file The point cloud file(ply)
     * @param listener The result of reading point cloud
     */
    public <T> void readPointCloud(File file, T pointCloud, ReadListener<T> listener) {
        if (! file.exists()) {
            listener.onFail(Constants.ERR_CODE_FILE_NOT_FOUND, "File does NOT exist.");
            return;
        }
        try {
            FileInputStream stream = new FileInputStream(file);
            Scanner scanner = new Scanner(stream);
            PlyHeader header = readHeader(scanner);
            switch (header.plyFormat) {
                case FORMAT_ASCII:
                    readAsciiPly(header, scanner, pointCloud, listener);
                    break;
                case FORMAT_BINARY_BIG_ENDIAN:
                    readBinaryPointCloud(header, stream, listener, ByteOrder.BIG_ENDIAN);
                    break;
                case FORMAT_BINARY_LITTLE_ENDIAN:
                    readBinaryPointCloud(header, stream, listener, ByteOrder.LITTLE_ENDIAN);
                    break;
            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            listener.onFail(Constants.ERR_CODE_FILE_FORMAT_ERROR, e.getMessage());
        } catch (IllegalStateException e) {
            e.printStackTrace();
            listener.onFail(Constants.ERR_CODE_NOT_3D_PLY, e.getMessage());
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            listener.onFail(Constants.ERR_CODE_NOT_ENOUGH_POINTS, e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            listener.onFail(Constants.ERR_CODE_PRIVATE_METHOD, e.getMessage());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            listener.onFail(Constants.ERR_CODE_METHOD_NO_LIST, e.getMessage());
        }
    }

    @Override
    public void readMesh(String fileName, ReadMeshListener listener) {
    }

    public static class PlyElement {
        /** eg. ["x", "y", "z", "red", "green", "blue"] **/
        private String[] propertiesName;
        /** eg. [float, float, float, uchar, uchar, uchar] **/
        private int[] propertiesType;

        PlyElement(int propertiesNum) {
            this.propertiesName = new String [propertiesNum];
            this.propertiesType = new int [propertiesNum];
        }

        /**
         * because list will be the only property in a element
         * we can use two type-field to describe list
         **/
        private int listType1 = TYPE_NONTYPE;
        private int listType2 = TYPE_NONTYPE;

        public String[] getPropertiesName() {
            return propertiesName;
        }

        public int[] getPropertiesType() {
            return propertiesType;
        }

        public int getListType1() {
            return listType1;
        }

        public int getListType2() {
            return listType2;
        }

    }

    public static class PlyHeader {
        /** FORMAT_ASCII or FORMAT_BINARY **/
        private int plyFormat = FORMAT_NON_FORMAT;

        private float plyVersion = 0;

        /** [("vertex", 12), ("face", 8)] **/
        private List<Pair<String, Integer>> elementsNumber = new ArrayList<>();

        private Map<String, PlyElement> elementTypes = new HashMap<>();

        private int headerBytes = 0;

        public int getPlyFormat() {
            return plyFormat;
        }

        public float getPlyVersion() {
            return plyVersion;
        }

        public List<Pair<String, Integer>> getElementsNumber() {
            return elementsNumber;
        }

        public Map<String, PlyElement> getElementTypes() {
            return elementTypes;
        }

        public int getHeaderBytes() {
            return headerBytes;
        }
    }

}
