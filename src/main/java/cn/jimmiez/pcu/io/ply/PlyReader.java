package cn.jimmiez.pcu.io.ply;

import cn.jimmiez.pcu.io.ReadListener;
import cn.jimmiez.pcu.Constants;
import cn.jimmiez.pcu.model.PcuPlyData;
import cn.jimmiez.pcu.util.PcuArrayUtil;
import cn.jimmiez.pcu.util.PcuReflectUtil;
import javafx.util.Pair;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

public class PlyReader {

    /** constant representing ply format **/
    public static final int FORMAT_ASCII = 0x3001;
    public static final int FORMAT_BINARY_BIG_ENDIAN = 0x3002;
    public static final int FORMAT_BINARY_LITTLE_ENDIAN = 0x3003;

    public static final int FORMAT_NON_FORMAT = - 0x3001;

    public static final int TYPE_LIST = 0x1001;

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
    private int ERR_LINE_NO = 0;

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
        header.setHeaderBytes(byteCount);
        ERR_LINE_NO = headerLines.size() + 1;
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
            PlyElement element = new PlyElement();
            for (int i = propertyStartNo; i < lineNo; i ++) {
                String[] propertySlices = headerLines.get(i).split(" ");
                if (propertySlices.length < 3) throw new IOException("Invalid ply file.");
                element.propertiesName.add(propertySlices[propertySlices.length - 1]);
                element.propertiesType.add(recognizeType(propertySlices[1]));
                if (element.propertiesType.get(i - propertyStartNo) == TYPE_LIST) {
                    if (propertySlices.length < 5) throw new IOException("Invalid ply file. Wrong list property.");
                    int[] types = new int[] {recognizeType(propertySlices[2]), recognizeType(propertySlices[3])};
                    element.getListTypes().put(element.propertiesName.get(i - propertyStartNo), types);
                }
            }
            header.getElementTypes().put(pair.getKey(), element);
            header.getElementsNumber().add(pair);
        }
        return header;
    }

    private int recognizeType(String type) {
        switch (type) {
            case "char":
            case "int8":
                return TYPE_CHAR;
            case "uchar":
            case "uint8":
                return TYPE_UCHAR;
            case "int":
            case "int32":
                return TYPE_INT;
            case "uint":
            case "uint32":
                return TYPE_UINT;
            case "short":
            case "int16":
                return TYPE_SHORT;
            case "ushort":
            case "uint16":
                return TYPE_USHORT;
            case "float":
            case "float32":
                return TYPE_FLOAT;
            case "double":
            case "float64":
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
                header.setPlyFormat(FORMAT_ASCII);
            } else if (formatSlices[1].equals("binary_little_endian")) {
                header.setPlyFormat(FORMAT_BINARY_LITTLE_ENDIAN);
            } else if (formatSlices[1].equals("binary_big_endian")) {
                header.setPlyFormat(FORMAT_BINARY_BIG_ENDIAN);
            }
            header.setPlyVersion(Float.valueOf(formatSlices[2]));
        } else {
            throw new IOException("Invalid ply file: Wrong format ply in line");
        }
    }

    public <T> T readPointCloud(File file, Class<T> clazz) {
        T object = null;
        final List<Integer> errorCodes = new ArrayList<>();
        try {
            object = clazz.newInstance();
            readPointCloud(file, object, new ReadListener() {
                @Override
                public void onFail(int code, String message) {
                    errorCodes.add(code);
                    System.err.println("Read point cloud failed, error code: " + code);
                    System.err.println(message);
                }
            });
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (errorCodes.size() > 0) return null;
        return object;
    }

    private List<Method> findAllElementGetter(List<Method> methods) {
        List<Method> getters = new ArrayList<>();
        for (Method m : methods) {
            PcuPlyData pcuEle = m.getAnnotation(PcuPlyData.class);
            if (pcuEle == null) continue;
            getters.add(m);
        }
        return getters;
    }



    /**
     * generated from user-defined annotations
     */
    private interface ParserCallback {

        void gotDoubleVal(String elementName, int pos, double val);

        void gotFloatVal(String elementName, int pos, float val);

        void gotIntVal(String elementName, int pos, int val);

        void gotShortVal(String elementName, int pos, short val);

        void gotByte(String elementName, int pos, byte val);

        void gotDoubleArray(String elementName, double[] array);

        void gotFloatArray(String elementName, float[] array);

        void gotIntArray(String elementName, int[] array);

        void gotShortArray(String elementName, short[] array);

        void gotByteArray(String elementName, byte[] array);

    }

    private abstract class DataContainer implements ParserCallback{
        private PlyHeader header;

        public DataContainer(PlyHeader header) {
            this.header = header;
            this.initArray();
        }

        private void initArray() {
            int doubleDataLength = 0;
            int floatDataLength = 0;
            int intDataLength = 0;
            int shortDataLength = 0;
            int byteDataLength = 0;

            int doubleListDataLength = 0;
            int floatListDataLength = 0;
            int intListDataLength = 0;
            int shortListDataLength = 0;
            int byteListDataLength = 0;
            for (int i = 0; i < header.getElementsNumber().size(); i ++) {
                String plyElementName = header.getElementsNumber().get(i).getKey();
                PlyElement element = header.getElementTypes().get(plyElementName);

                int doubleCnt = 0;
                int floatCnt = 0;
                int intCnt = 0;
                int shortCnt = 0;
                int byteCnt = 0;

                int doublePeriod = 0;
                int floatPeriod = 0;
                int intPeriod = 0;
                int shortPeriod = 0;
                int bytePeriod = 0;

                for (int j = 0; j < element.propertiesName.size(); j ++) {
                    boolean isList = element.propertiesType.get(j) == TYPE_LIST;
                    int type = isList ? element.listTypes.get(element.propertiesName.get(j))[1] : element.propertiesType.get(j);
                    switch (type) {
                        case TYPE_FLOAT:
                            floatPeriod += 1;
                            break;
                        case TYPE_DOUBLE:
                            doublePeriod += 1;
                            break;
                        case TYPE_INT:
                        case TYPE_UINT:
                            intPeriod += 1;
                            break;
                        case TYPE_SHORT:
                        case TYPE_USHORT:
                            shortPeriod += 1;
                            break;
                        case TYPE_CHAR:
                        case TYPE_UCHAR:
                            bytePeriod += 1;
                            break;
                    }
                }

                for (int j = 0; j < element.propertiesName.size(); j ++) {
                    boolean isList = element.propertiesType.get(j) == TYPE_LIST;
                    int type = isList ? element.listTypes.get(element.propertiesName.get(j))[1] : element.propertiesType.get(j);
                    String propertyName = element.propertiesName.get(j);
                    Pair<String, String> eleProNamePair = new Pair<>(plyElementName, propertyName);
                    switch (type) {
                        case TYPE_FLOAT:
                            positionRecord.put(eleProNamePair, new int[] {floatDataLength + floatCnt, floatPeriod});
                            floatCnt += 1;
                            break;
                        case TYPE_DOUBLE:
                            positionRecord.put(eleProNamePair, new int[] {doubleDataLength + doubleCnt, doublePeriod});
                            doubleCnt += 1;
                            break;
                        case TYPE_INT:
                        case TYPE_UINT:
                            positionRecord.put(eleProNamePair, new int[] {intDataLength + intCnt, intPeriod});
                            intCnt += 1;
                            break;
                        case TYPE_SHORT:
                        case TYPE_USHORT:
                            positionRecord.put(eleProNamePair, new int[] {shortDataLength + shortCnt, shortPeriod});
                            shortCnt += 1;
                            break;
                        case TYPE_CHAR:
                        case TYPE_UCHAR:
                            positionRecord.put(eleProNamePair, new int[] {byteDataLength + byteCnt, bytePeriod});
                            byteCnt += 1;
                            break;
                    }
                }
                doubleDataLength += (doublePeriod * header.getElementsNumber().get(i).getValue());
                floatDataLength += (floatPeriod * header.getElementsNumber().get(i).getValue());
                intDataLength += (intPeriod * header.getElementsNumber().get(i).getValue());
                shortDataLength += (shortPeriod * header.getElementsNumber().get(i).getValue());
                byteDataLength += (bytePeriod * header.getElementsNumber().get(i).getValue());

                doubleListDataLength += (doubleCnt * header.getElementsNumber().get(i).getValue());
                floatListDataLength += (floatCnt * header.getElementsNumber().get(i).getValue());
                intListDataLength += (intCnt * header.getElementsNumber().get(i).getValue());
                shortListDataLength += (shortCnt * header.getElementsNumber().get(i).getValue());
                byteListDataLength += (byteCnt * header.getElementsNumber().get(i).getValue());
            }
            this.doubleData = new double[doubleDataLength];
            this.floatData = new float[floatDataLength];
            this.intData = new int[intDataLength];
            this.shortData = new short[shortDataLength];
            this.byteData = new byte[byteDataLength];

            this.doubleListData = new Vector<>(doubleListDataLength);
            this.floatListData = new Vector<>(floatListDataLength);
            this.intListData = new Vector<>(intListDataLength);
            this.shortListData = new Vector<>(shortListDataLength);
            this.byteListData = new Vector<>(byteListDataLength);
        }

        /**
         * ("vertex", "x") -> [10475, 12]
         * means first property "x" is the 10475th element in dataArray
         * and 12 is the period
         **/
        protected Map<Pair<String, String>, int[]> positionRecord = new HashMap<>();
        protected double[] doubleData;
        protected float[] floatData;
        protected int[] intData;
        protected short[] shortData;
        protected byte[] byteData;
        protected List<double[]> doubleListData = null;
        protected List<float[]> floatListData = null;
        protected List<int[]> intListData = null;
        protected List<short[]> shortListData = null;
        protected List<byte[]> byteListData = null;

        private int doublePtr = 0;
        private int floatPtr = 0;
        private int intPtr = 0;
        private int shortPtr = 0;
        private int bytePtr = 0;

        abstract void release() throws IllegalStateException, InvocationTargetException, IllegalAccessException;

        @Override
        public void gotDoubleVal(String elementName, int pos, double val) {
            this.doubleData[doublePtr ++] = val;
        }

        @Override
        public void gotFloatVal(String elementName, int pos, float val) {
            this.floatData[floatPtr ++] = val;
        }

        @Override
        public void gotIntVal(String elementName, int pos, int val) {
            this.intData[intPtr ++] = val;
        }

        @Override
        public void gotShortVal(String elementName, int pos, short val) {
            this.shortData[shortPtr ++] = val;
        }

        @Override
        public void gotByte(String elementName, int pos, byte val) {
            this.byteData[bytePtr ++] = val;
        }

        @Override
        public void gotDoubleArray(String elementName, double[] array) {
            this.doubleListData.add(array);
        }

        @Override
        public void gotFloatArray(String elementName, float[] array) {
            this.floatListData.add(array);
        }

        @Override
        public void gotIntArray(String elementName, int[] array) {
            this.intListData.add(array);
        }

        @Override
        public void gotShortArray(String elementName, short[] array) {
            this.shortListData.add(array);
        }

        @Override
        public void gotByteArray(String elementName, byte[] array) {
            this.byteListData.add(array);
        }
    }

    private <T> DataContainer generateParserCallback(final T userDefinedObject, final PlyHeader header) {
        List<Method> allMethods = PcuReflectUtil.fetchAllMethods(userDefinedObject);
        final List<Method> getters = findAllElementGetter(allMethods);
        return new DataContainer(header) {
            @Override
            void release() throws InvocationTargetException, IllegalAccessException {
                for (int i = 0; i < getters.size(); i ++) {
                    Method method = getters.get(i);
                    PcuPlyData annotation = method.getAnnotation(PcuPlyData.class);
                    String[] elementNames = annotation.element();
                    String elementName = null;
                    int elementNumber = 0;
                    for (String elementNameInPly : header.getElementTypes().keySet()) {
                        if (PcuArrayUtil.find(elementNames, elementNameInPly) >= 0) {
                            elementName = elementNameInPly;
                            break;
                        }
                    }
                    if (elementName == null) {
                        throw new IllegalStateException("Cannot find the element in ply for your getter: " + method.getName());
                    }
                    /** an ugly way to find number of items in current element **/
                    for (Pair<String, Integer> pair : header.getElementsNumber()) {
                        if (pair.getKey().equals(elementName)) {
                            elementNumber = pair.getValue();
                            break;
                        }
                    }

                    String[] properties = annotation.properties();
                    if (properties.length < 1) return;

                    PlyElement plyElement = header.getElementTypes().get(elementName);

                    /** make an assumption that all field has same type **/
                    int propertyPosition = plyElement.propertiesName.indexOf(properties[0]);
                    if (propertyPosition == -1) {
                        throw new IllegalStateException("The property " + properties[0] + " for getter: " + method.getName() + " cannot be found in ply header!");
                    }
                    int firstPropertyType = plyElement.propertiesType.get(propertyPosition);
                    /** user may input "z", "y", "x" **/
                    int[] indices = new int[properties.length];
                    int period = 0;
                    for (int j = 0; j < indices.length; j ++) {
                        String propertyName = properties[j];
                        int[] startIndexAndPeriod = positionRecord.get(new Pair<>(elementName, propertyName));
                        indices[j] = startIndexAndPeriod[0];
                        period = startIndexAndPeriod[1];
                    }
                    boolean isListType = (firstPropertyType == TYPE_LIST);
                    if (isListType) {
                        if (properties.length > 1) {
                            throw new IllegalStateException("You need declare the name of only one list-type property.");
                        } else {
                            firstPropertyType = plyElement.listTypes.get(properties[0])[1];
                        }
                    }
                    switch (firstPropertyType) {
                        case TYPE_FLOAT: {
                            List<float[]> list = (List<float[]>) method.invoke(userDefinedObject);
                            if (isListType) {
                                for (int j = 0; j < elementNumber; j ++) {
                                    list.add(floatListData.get(indices[0] + j * period));
                                }
                            } else {
                                for (int j = 0; j < elementNumber; j++) {
                                    float[] vector = new float[properties.length];
                                    for (int k = 0; k < properties.length; k++) {
                                        vector[k] = floatData[indices[k] + j * period];
                                    }
                                    list.add(vector);
                                }
                            }
                            break;
                        }
                        case TYPE_DOUBLE: {
                            List<double[]> list = (List<double[]>) method.invoke(userDefinedObject);
                            if (isListType) {
                                for (int j = 0; j < elementNumber; j ++) {
                                    list.add(doubleListData.get(indices[0] + j * period));
                                }
                            } else {
                                for (int j = 0; j < elementNumber; j ++) {
                                    double[] vector = new double[properties.length];
                                    for (int k = 0; k < properties.length; k ++) {
                                        vector[k] = doubleData[indices[k] + j * period];
                                    }
                                    list.add(vector);
                                }
                            }
                            break;
                        }
                        case TYPE_INT:
                        case TYPE_UINT: {
                            List<int[]> list = (List<int[]>) method.invoke(userDefinedObject);
                            if (isListType) {
                                for (int j = 0; j < elementNumber; j ++) {
                                    list.add(intListData.get(indices[0] + j * period));
                                }
                            } else {
                                for (int j = 0; j < elementNumber; j++) {
                                    int[] vector = new int[properties.length];
                                    for (int k = 0; k < properties.length; k++) {
                                        vector[k] = intData[indices[k] + j * period];
                                    }
                                    list.add(vector);
                                }
                            }
                            break;
                        }
                        case TYPE_SHORT:
                        case TYPE_USHORT: {
                            List<short[]> list = (List<short[]>) method.invoke(userDefinedObject);
                            if (isListType) {
                                for (int j = 0; j < elementNumber; j ++) {
                                    list.add(shortListData.get(indices[0] + j * period));
                                }
                            } else {
                                for (int j = 0; j < elementNumber; j++) {
                                    short[] vector = new short[properties.length];
                                    for (int k = 0; k < properties.length; k++) {
                                        vector[k] = shortData[indices[k] + j * period];
                                    }
                                    list.add(vector);
                                }
                            }
                            break;
                        }
                        case TYPE_CHAR:
                        case TYPE_UCHAR: {
                            List<byte[]> list = (List<byte[]>) method.invoke(userDefinedObject);
                            if (isListType) {
                                for (int j = 0; j < elementNumber; j ++) {
                                    list.add(byteListData.get(indices[0] + j * period));
                                }
                            } else {
                                for (int j = 0; j < elementNumber; j++) {
                                    byte[] vector = new byte[properties.length];
                                    for (int k = 0; k < properties.length; k++) {
                                        vector[k] = byteData[indices[k] + j * period];
                                    }
                                    list.add(vector);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        };
    }

    private class PlyBodyParser {
        private ParserCallback callback;
        private PlyHeader header;

        public PlyBodyParser(PlyHeader header, ParserCallback callback) {
            this.header = header;
            this.callback = callback;
        }

        private static final int STATE_READY = 0;
        private static final int STATE_READING_SCALAR_VAL = 1;
        private static final int STATE_READING_LIST_SIZE = 2;
        private static final int STATE_READING_LIST_VAL = 3;
        private static final int STATE_TO_READ_NEXT_PROPERTY = 6;
        private static final int STATE_ERROR = 4;
        private static final int STATE_COMPLETE = 5;

        public void readBinaryData(File file, ByteOrder order) throws IOException {
            int state = STATE_READY;

            int expectedType = TYPE_NONTYPE;
            String currentElementName = null;

            int currentElementPtr = 0;
            int currentPropertyPtr = 0;
            int currentItemNumber = 0;

            /** when reading a list property, these two fields are to store current state **/
            int currentListSize = 0;

            byte[] bytes = Files.readAllBytes(file.toPath());
            int currBytePtr = header.getHeaderBytes();

            boolean loop = true;

            while (loop) {
                switch (state) {
                    case STATE_READY: {
                        currentElementPtr = 0;
                        currentPropertyPtr = 0;
                        currentItemNumber = 0;
                        state = STATE_TO_READ_NEXT_PROPERTY;
                        break;
                    }
                    case STATE_TO_READ_NEXT_PROPERTY: {
                        if (currentElementPtr >= header.getElementsNumber().size()) {
                            state = STATE_COMPLETE;
                            break;
                        }
                        if (currentItemNumber >= header.getElementsNumber().get(currentElementPtr).getValue()) {
                            currentItemNumber = 0;
                            currentElementPtr += 1;
                            break;
                        }
                        currentElementName = header.getElementsNumber().get(currentElementPtr).getKey();
                        if (currentPropertyPtr >= header.getElementTypes().get(currentElementName).propertiesType.size()) {
                            currentPropertyPtr = 0;
                            currentItemNumber += 1;
                            break;
                        } else {
                            expectedType = header.getElementTypes().get(currentElementName).propertiesType.get(currentPropertyPtr);
                            if (expectedType != TYPE_LIST) {
                                state = STATE_READING_SCALAR_VAL;
                            } else {
                                state = STATE_READING_LIST_SIZE;
                            }
                        }

                        break;
                    }
                    case STATE_READING_SCALAR_VAL: {
//                        System.out.println("SCALAR VAL STATE");
                        state = STATE_TO_READ_NEXT_PROPERTY;
//                        bytes = new byte[TYPE_SIZE[expectedType]];
//                        stream.read(bytes);
                        ByteBuffer buffer = ByteBuffer.wrap(bytes, currBytePtr, TYPE_SIZE[expectedType]);
                        buffer.order(order);
                        currBytePtr += TYPE_SIZE[expectedType];
                        switch (expectedType) {
                            case TYPE_DOUBLE:
                                callback.gotDoubleVal(currentElementName, currentPropertyPtr, buffer.getDouble());
                                break;
                            case TYPE_FLOAT:
                                callback.gotFloatVal(currentElementName, currentPropertyPtr, buffer.getFloat());
                                break;
                            case TYPE_INT:
                            case TYPE_UINT:
                                callback.gotIntVal(currentElementName, currentPropertyPtr, buffer.getInt());
                                break;
                            case TYPE_SHORT:
                            case TYPE_USHORT:
                                callback.gotShortVal(currentElementName, currentPropertyPtr, buffer.getShort());
                                break;
                            case TYPE_CHAR:
                            case TYPE_UCHAR:
                                callback.gotByte(currentElementName, currentPropertyPtr, buffer.get());
                                break;
                            case TYPE_LIST:
                                state = STATE_ERROR;
                                break;
                        }
                        currentPropertyPtr += 1;
                        break;
                    }
                    case STATE_READING_LIST_SIZE: {
                        state = STATE_READING_LIST_VAL;
                        PlyElement element = header.getElementTypes().get(currentElementName);
                        String currentProperty = element.propertiesName.get(currentPropertyPtr);
                        int [] listTypes = element.listTypes.get(currentProperty);
                        if (listTypes == null || listTypes.length < 2) {
                            System.err.println("PlyReader::readAsciiData(), STATE: READLING_LIST_SIZE. listSize too short.");
                            state = STATE_ERROR;
                            break;
                        }

                        int firstType = listTypes[0];
//                        bytes = new byte[TYPE_SIZE[firstType]];
//                        stream.read(bytes);
                        ByteBuffer buffer = ByteBuffer.wrap(bytes, currBytePtr, TYPE_SIZE[firstType]);
                        currBytePtr += TYPE_SIZE[firstType];
                        buffer.order(order);

                        switch (firstType) {
                            case TYPE_DOUBLE:
                            case TYPE_FLOAT:
                            case TYPE_LIST:
                                System.err.println("PlyReader::readAsciiData(), STATE: READLING_LIST_SIZE.");
                                state = STATE_ERROR;
                                break;
                            case TYPE_INT:
                            case TYPE_UINT:
                                currentListSize = buffer.getInt();
                                break;
                            case TYPE_SHORT:
                            case TYPE_USHORT:
                                currentListSize = buffer.getShort();
                                break;
                            case TYPE_CHAR:
                            case TYPE_UCHAR:
                                currentListSize = buffer.get();
                                break;
                        }
                        break;
                    }
                    case STATE_READING_LIST_VAL: {
                        state = STATE_TO_READ_NEXT_PROPERTY;

                        PlyElement element = header.getElementTypes().get(currentElementName);
                        String currentProperty = element.propertiesName.get(currentPropertyPtr);
                        int [] listTypes = element.listTypes.get(currentProperty);

                        if (listTypes == null || listTypes.length < 2) {
                            System.err.println("PlyReader::readAsciiData(), STATE: READLING_LIST_SIZE. listSize too short.");
                            state = STATE_ERROR;
                            break;
                        }
                        int listItemType = listTypes[1];
//                        bytes = new byte[TYPE_SIZE[listItemType] * currentListSize];
//                        stream.read(bytes);
                        ByteBuffer buffer = ByteBuffer.wrap(bytes, currBytePtr, TYPE_SIZE[listItemType] * currentListSize);
                        currBytePtr += TYPE_SIZE[listItemType] * currentListSize;
                        buffer.order(order);
                        switch (listItemType) {
                            case TYPE_DOUBLE:
                                double[] doubleList = new double[currentListSize];
                                for (int i = 0; i < currentListSize; i++) {
                                    doubleList[i] = buffer.getDouble();
                                }
                                callback.gotDoubleArray(currentElementName, doubleList);
                            case TYPE_FLOAT:
                                float[] floatList = new float[currentListSize];
                                for (int i = 0; i < currentListSize; i++) {
                                    floatList[i] = buffer.getFloat();
                                }
                                callback.gotFloatArray(currentElementName, floatList);
                            case TYPE_INT:
                            case TYPE_UINT:
                                int[] intList = new int[currentListSize];
                                for (int i = 0; i < currentListSize; i++) {
                                    intList[i] = buffer.getInt();
                                }
                                callback.gotIntArray(currentElementName, intList);
                                break;
                            case TYPE_SHORT:
                            case TYPE_USHORT:
                                short[] shortList = new short[currentListSize];
                                for (int i = 0; i < currentListSize; i++) {
                                    shortList[i] = buffer.getShort();
                                }
                                callback.gotShortArray(currentElementName, shortList);
                                break;
                            case TYPE_CHAR:
                            case TYPE_UCHAR:
                                byte[] byteList = new byte[currentListSize];
                                for (int i = 0; i < currentListSize; i++) {
                                    byteList[i] = buffer.get();
                                }
                                callback.gotByteArray(currentElementName, byteList);
                                break;
                            case TYPE_LIST:
                                System.err.println("PlyReader::readAsciiData(), STATE: READLING_LIST_SIZE.");
                                state = STATE_ERROR;
                                break;
                        }
                        currentPropertyPtr += 1;
                        break;
                    }
                    case STATE_COMPLETE: {
                        loop = false;
                        break;
                    }
                    case STATE_ERROR: {
                        loop = false;
                        System.err.println("Parse ply file failed, element: " + currentElementName + ", property: "
                                + header.getElementTypes().get(currentElementName).propertiesName.get(currentPropertyPtr));
                        System.err.println("Parse stop at item: " + currentItemNumber);
                        break;
                    }
                }
            }

        }


        /**
         * I design a state machine to parser ASCII ply file
         * @param file ply file
         * @throws NoSuchElementException if scanner try to read next element, but reach the end of file, throw this exception
         */
        public void readAsciiData(File file) throws NoSuchElementException, IOException {
            byte[] bytes = Files.readAllBytes(file.toPath());
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes, header.getHeaderBytes(), bytes.length - header.getHeaderBytes());
            Scanner scanner = new Scanner(stream);
            int state = STATE_READY;

            int expectedType = TYPE_NONTYPE;
            String currentElementName = null;

            int currentElementPtr = 0;
            int currentPropertyPtr = 0;
            int currentItemNumber = 0;

            /** when reading a list property, these two fields are to store current state **/
            int currentListSize = 0;

            boolean loop = true;

            while (loop) {
                switch (state) {
                    case STATE_READY: {
                        currentElementPtr = 0;
                        currentPropertyPtr = 0;
                        currentItemNumber = 0;
                        state = STATE_TO_READ_NEXT_PROPERTY;
                        break;
                    }
                    case STATE_TO_READ_NEXT_PROPERTY: {
                        if (currentElementPtr >= header.getElementsNumber().size()) {
                            state = STATE_COMPLETE;
                            break;
                        }
                        if (currentItemNumber >= header.getElementsNumber().get(currentElementPtr).getValue()) {
                            currentItemNumber = 0;
                            currentElementPtr += 1;
                            break;
                        }
                        currentElementName = header.getElementsNumber().get(currentElementPtr).getKey();
                        if (currentPropertyPtr >= header.getElementTypes().get(currentElementName).propertiesType.size()) {
                            currentPropertyPtr = 0;
                            currentItemNumber += 1;
                            break;
                        } else {
                            expectedType = header.getElementTypes().get(currentElementName).propertiesType.get(currentPropertyPtr);
                            if (expectedType != TYPE_LIST) {
                                state = STATE_READING_SCALAR_VAL;
                            } else {
                                state = STATE_READING_LIST_SIZE;
                            }
                        }

                        break;
                    }
                    case STATE_READING_SCALAR_VAL: {
//                        System.out.println("SCALAR VAL STATE");
                        state = STATE_TO_READ_NEXT_PROPERTY;
                        switch (expectedType) {
                            case TYPE_DOUBLE:
                                callback.gotDoubleVal(currentElementName, currentPropertyPtr, scanner.nextDouble());
                                break;
                            case TYPE_FLOAT:
                                callback.gotFloatVal(currentElementName, currentPropertyPtr, scanner.nextFloat());
                                break;
                            case TYPE_INT:
                            case TYPE_UINT:
                                callback.gotIntVal(currentElementName, currentPropertyPtr, scanner.nextInt());
                                break;
                            case TYPE_SHORT:
                            case TYPE_USHORT:
                                callback.gotShortVal(currentElementName, currentPropertyPtr, scanner.nextShort());
                                break;
                            case TYPE_CHAR:
                            case TYPE_UCHAR:
                                callback.gotByte(currentElementName, currentPropertyPtr, scanner.nextByte());
                                break;
                            case TYPE_LIST:
                                state = STATE_ERROR;
                                break;
                        }
                        currentPropertyPtr += 1;
                        break;
                    }
                    case STATE_READING_LIST_SIZE: {
                        state = STATE_READING_LIST_VAL;
                        PlyElement element = header.getElementTypes().get(currentElementName);
                        String currentProperty = element.propertiesName.get(currentPropertyPtr);
                        int [] listTypes = element.listTypes.get(currentProperty);
                        if (listTypes == null || listTypes.length < 2) {
                            System.err.println("PlyReader::readAsciiData(), STATE: READLING_LIST_SIZE. listSize too short.");
                            state = STATE_ERROR;
                            break;
                        }

                        int firstType = listTypes[0];

                        switch (firstType) {
                            case TYPE_DOUBLE:
                            case TYPE_FLOAT:
                            case TYPE_LIST:
                                System.err.println("PlyReader::readAsciiData(), STATE: READLING_LIST_SIZE.");
                                state = STATE_ERROR;
                                break;
                            case TYPE_INT:
                            case TYPE_UINT:
                                currentListSize = scanner.nextInt();
                                break;
                            case TYPE_SHORT:
                            case TYPE_USHORT:
                                currentListSize = scanner.nextShort();
                                break;
                            case TYPE_CHAR:
                            case TYPE_UCHAR:
                                currentListSize = scanner.nextByte();
                                break;
                        }
                        break;
                    }
                    case STATE_READING_LIST_VAL: {
                        state = STATE_TO_READ_NEXT_PROPERTY;

                        PlyElement element = header.getElementTypes().get(currentElementName);
                        String currentProperty = element.propertiesName.get(currentPropertyPtr);
                        int [] listTypes = element.listTypes.get(currentProperty);

                        if (listTypes == null || listTypes.length < 2) {
                            System.err.println("PlyReader::readAsciiData(), STATE: READLING_LIST_SIZE. listSize too short.");
                            state = STATE_ERROR;
                            break;
                        }
                        int listItemType = listTypes[1];
                        switch (listItemType) {
                            case TYPE_DOUBLE:
                                double[] doubleList = new double[currentListSize];
                                for (int i = 0; i < currentListSize; i++) {
                                    doubleList[i] = scanner.nextDouble();
                                }
                                callback.gotDoubleArray(currentElementName, doubleList);
                            case TYPE_FLOAT:
                                float[] floatList = new float[currentListSize];
                                for (int i = 0; i < currentListSize; i++) {
                                    floatList[i] = scanner.nextFloat();
                                }
                                callback.gotFloatArray(currentElementName, floatList);
                            case TYPE_INT:
                            case TYPE_UINT:
                                int[] intList = new int[currentListSize];
                                for (int i = 0; i < currentListSize; i++) {
                                    intList[i] = scanner.nextInt();
                                }
                                callback.gotIntArray(currentElementName, intList);
                                break;
                            case TYPE_SHORT:
                            case TYPE_USHORT:
                                short[] shortList = new short[currentListSize];
                                for (int i = 0; i < currentListSize; i++) {
                                    shortList[i] = scanner.nextShort();
                                }
                                callback.gotShortArray(currentElementName, shortList);
                                break;
                            case TYPE_CHAR:
                            case TYPE_UCHAR:
                                byte[] byteList = new byte[currentListSize];
                                for (int i = 0; i < currentListSize; i++) {
                                    byteList[i] = scanner.nextByte();
                                }
                                callback.gotByteArray(currentElementName, byteList);
                                break;
                            case TYPE_LIST:
                                System.err.println("PlyReader::readAsciiData(), STATE: READLING_LIST_SIZE.");
                                state = STATE_ERROR;
                                break;
                        }
                        currentPropertyPtr += 1;
                        break;
                    }
                    case STATE_COMPLETE: {
//                        System.out.println("STATE_COMPLETE, EXIT LOOP.");
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
    }

    /**
     * read a 3d point cloud from a ply file.
     * It is supposed that the properties of vertex is listed in such order:
     * [ x, y, z, other data types ... ]
     * @param file The point cloud file(ply)
     * @param pointCloud The point cloud object (with annotation PcuPlyData)
     */
    public <T> void readPointCloud(File file, T pointCloud, ReadListener listener) {
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
            DataContainer stager = generateParserCallback(pointCloud, header);
            PlyBodyParser parser = new PlyBodyParser(header, stager);
            switch (header.getPlyFormat()) {
                case FORMAT_ASCII:
                    parser.readAsciiData(file);
                    stager.release();
                    break;
                case FORMAT_BINARY_BIG_ENDIAN:
                    parser.readBinaryData(file, ByteOrder.BIG_ENDIAN);
                    stager.release();
                    break;
                case FORMAT_BINARY_LITTLE_ENDIAN:
                    parser.readBinaryData(file, ByteOrder.LITTLE_ENDIAN);
                    stager.release();
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


}
