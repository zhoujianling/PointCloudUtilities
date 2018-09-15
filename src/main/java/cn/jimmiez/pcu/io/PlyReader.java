package cn.jimmiez.pcu.io;

import cn.jimmiez.pcu.model.PcuElement;
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
        int byteCount = 0;
        try {
            String line = null;
            while ((line = scanner.nextLine()) != null) {
                byteCount += (line.getBytes().length + 1);
                if (line.equals("end_header")) break;
                if (line.startsWith("comment ")) continue;
                headerLines.add(line);
            }
        } catch (NoSuchElementException e) {
            throw new IOException("Invalid ply file: Cannot find end of header.");
        }
        if (headerLines.size() < 1) {
            throw new IOException("Invalid ply file: No data");
        }
        header.headerBytes = byteCount;
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

//    public <T> void readPointCloud(String fileName, Class<T> clazz, ReadListener<T> listener) {
//
//
//    }

    public <T> T readPointCloud(String fileName, Class<T> clazz) {
        T object = null;
        try {
            object = clazz.newInstance();
            readPointCloud(fileName, object, new ReadListener<T>() {
                @Override
                public void onSucceed(T pointCloud, PlyHeader header) {
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

    @SuppressWarnings("unchecked")
    private <T> void readBinaryPointCloud(PlyHeader header, FileInputStream stream, T pointCloud, ReadListener<T> listener, ByteOrder order)
            throws IOException, InvocationTargetException, IllegalAccessException {
        List<Method> methods = PcuReflectUtil.fetchAllMethods(pointCloud);
        for (int i = 0; i < header.elementsNumber.size(); i ++) {
            String elementName = header.elementsNumber.get(i).getKey();
            int elementNum = header.elementsNumber.get(i).getValue();
            PlyElement element = header.elementTypes.get(elementName);
            List<Method> getters = findElementGetter(methods, elementName);
            List<List> data = new ArrayList<>();
            List<PcuElement> userDefinedEles = new ArrayList<>();

            for (Method m : getters) {
                Object listObj = m.invoke(pointCloud);
                if (listObj != null && (listObj instanceof List)) {
                    data.add((List) listObj);
                    userDefinedEles.add(m.getAnnotation(PcuElement.class));
                }
            }

            if (userDefinedEles.size() < 1) {
                skipBinaryElement(elementNum, element, stream);
                continue;
            }

            /**key is index for data, value is index for data.get(key)**/
            Pair[] dataIndexList = new Pair[element.propertiesType.length];
            int originalSize = data.get(0).size();

            for (int j = 0; j < element.propertiesType.length ; j ++) {
                boolean jump = false;
                for (int k = 0; k < userDefinedEles.size() && !jump; k ++) {
                    PcuElement pcuEle = userDefinedEles.get(k);
                    for (int l = 0; l < pcuEle.properties().length && !jump; l ++) {
                        if (pcuEle.properties()[l].equals(element.propertiesName[j])) {
                            Pair<Integer, Integer> pair = new Pair<>(k, l);
                            dataIndexList[j] = pair;
                            jump = true;
                        }
                    }
                }
            }
            int lineSize = sizeOfElement(element);
            byte[] bytes = new byte[lineSize];
            for (int j = 0; j < elementNum; j ++) {
                stream.read(bytes);
                parseBinaryElement(bytes, dataIndexList, element, data, order, userDefinedEles);
            }
        }
        listener.onSucceed(pointCloud, header);
    }

    @SuppressWarnings("unchecked")
    private void parseBinaryElement(byte[] bytes, Pair[] dataIndexList, PlyElement element, List<List> data,
                                    ByteOrder order, List<PcuElement> pcuEles) throws IOException {
        int offset = 0;
        int originalSize = data.get(0).size();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(order);
        if (element.listType1 == TYPE_NONTYPE) {
            for (int i = 0; i < element.propertiesType.length; i ++) {
                Pair<Integer, Integer> pair = dataIndexList[i];
                if (pair == null) continue;
                switch (element.propertiesType[i]) {
                    case TYPE_DOUBLE: {
                        if (originalSize == data.get(pair.getKey()).size()) {
                            data.get(pair.getKey()).add(new double[pcuEles.get(pair.getKey()).properties().length]);
                        }
                        List<double[]> vectors = data.get(pair.getKey());
                        double val = buffer.getDouble(offset);
                        vectors.get(vectors.size() - 1)[pair.getValue()] = val;
                        offset += 8;
                    }
                    break;
                    case TYPE_FLOAT: {
                        if (originalSize == data.get(pair.getKey()).size()) {
                            data.get(pair.getKey()).add(new float[pcuEles.get(pair.getKey()).properties().length]);
                        }
                        List<float[]> vector = data.get(pair.getKey());
                        float val = buffer.getFloat(offset);
                        vector.get(vector.size() - 1)[pair.getValue()] = val;
                        offset += 4;
                    }
                    break;
                    case TYPE_INT:
                    case TYPE_UINT:{
                        if (originalSize == data.get(pair.getKey()).size()) {
                            data.get(pair.getKey()).add(new int[pcuEles.get(pair.getKey()).properties().length]);
                        }
                        List<int[]> vector = data.get(pair.getKey());
                        int val = buffer.getInt(offset);
                        vector.get(vector.size() - 1)[pair.getValue()] = val;
                        offset += 4;
                    }
                    break;
                    case TYPE_SHORT:
                    case TYPE_USHORT: {
                        if (originalSize == data.get(pair.getKey()).size()) {
                            data.get(pair.getKey()).add(new short[pcuEles.get(pair.getKey()).properties().length]);
                        }
                        List<short[]> vector = data.get(pair.getKey());
                        short val = buffer.getShort(offset);
                        vector.get(vector.size() - 1)[pair.getValue()] = val;
                        offset += 2;
                    }
                    break;
                    case TYPE_CHAR:
                    case TYPE_UCHAR: {
                        if (originalSize == data.get(pair.getKey()).size()) {
                            data.get(pair.getKey()).add(new byte[pcuEles.get(pair.getKey()).properties().length]);
                        }
                        List<byte[]> vector = data.get(pair.getKey());
                        byte val = buffer.get(offset);
                        vector.get(vector.size() - 1)[pair.getValue()] = val;
                        offset += 1;
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

    private void skipBinaryElement(int elementNum, PlyElement element, FileInputStream stream) throws IOException {
        int totalSize = sizeOfElement(element) * elementNum;
        stream.skip(totalSize);
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
        if (element.listType1 == TYPE_NONTYPE) {
            for (int type : element.propertiesType) {
                size += TYPE_SIZE[type];
            }
        } else {
            // list type
            //???
        }
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
    private void readAsciiPly(PlyHeader header, FileInputStream stream, Object pointCloud, ReadListener listener) throws InvocationTargetException, IllegalAccessException, IOException {
        Scanner scanner = new Scanner(stream);
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
        FileInputStream stream = null;
        PlyHeader header = null;
        try {
            stream = new FileInputStream(file);
            Scanner scanner = new Scanner(stream);
            header = readHeader(scanner);
            stream.close();
            stream = new FileInputStream(file);
            stream.skip(header.headerBytes);
        } catch (IOException e) {
            e.printStackTrace();
            listener.onFail(Constants.ERR_CODE_FILE_HEADER_FORMAT_ERROR, e.getMessage());
        } catch (IllegalStateException e) {
            e.printStackTrace();
            listener.onFail(Constants.ERR_CODE_NOT_3D_PLY, e.getMessage());
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            listener.onFail(Constants.ERR_CODE_NOT_ENOUGH_POINTS, e.getMessage());
        }
        if (header == null) return;

        try {
            switch (header.plyFormat) {
                case FORMAT_ASCII:
                    readAsciiPly(header, stream, pointCloud, listener);
                    break;
                case FORMAT_BINARY_BIG_ENDIAN:
                    readBinaryPointCloud(header, stream, pointCloud, listener, ByteOrder.BIG_ENDIAN);
                    break;
                case FORMAT_BINARY_LITTLE_ENDIAN:
                    readBinaryPointCloud(header, stream, pointCloud, listener, ByteOrder.LITTLE_ENDIAN);
                    break;
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
            listener.onFail(Constants.ERR_CODE_PRIVATE_METHOD, e.getMessage());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            listener.onFail(Constants.ERR_CODE_METHOD_NO_LIST, e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            listener.onFail(Constants.ERR_CODE_FILE_DATA_FORMAT_ERROR, e.getMessage());
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
