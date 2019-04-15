package cn.jimmiez.pcu.io.ply;

import cn.jimmiez.pcu.Constants;
import cn.jimmiez.pcu.io.BinaryWriter;
import cn.jimmiez.pcu.util.Pair;
import cn.jimmiez.pcu.util.PcuReflectUtil;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteOrder;
import java.util.*;

public class PlyWriter {

    public int write(Object object, File file) {
        int result = Constants.ERR_CODE_NO_ERROR;
        try {
            writeWithObject(object, file);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            result = Constants.ERR_CODE_METHOD_NO_LIST;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }


    private void writeWithObject(Object object, File file) throws InvocationTargetException, IllegalAccessException {
        List<Method> allMethods = PcuReflectUtil.fetchAllMethods(object);
        PlyWriterRequest request = new PlyWriterRequest();
        request.format(PlyFormat.ASCII);
        request.comment("written by PointCloudUtil.");
        final Map<String, List<Method>> getters = findPropertiesGetters(allMethods);
        for (String key : getters.keySet()) {
            request.defineElement(key);
            for (Method m : getters.get(key)) {
                if (hasWriteScalarToPly(m)) {
                    WriteScalarToPly scalarToPly = m.getAnnotation(WriteScalarToPly.class);
                    if (scalarToPly.properties().length < 1) continue;
                    List data = (List) m.invoke(object);
                    String[] propertyNames = scalarToPly.properties();
                    PcuDataType type = scalarToPly.type();
                    request.defineScalarProperties(propertyNames, type, data);
                }
                if (hasWriteListToPly(m)) {
                    WriteListToPly listToPly = m.getAnnotation(WriteListToPly.class);
                    List data = (List) m.invoke(object);
                    PcuDataType sizeType = listToPly.sizeType();
                    PcuDataType valType = listToPly.valType();
                    request.defineListProperty(listToPly.property(), sizeType, valType, data);
                }

            }
        }

        request.writeTo(file);
        request.okay();
    }

    private boolean hasWriteScalarToPly(Method m) {
        return m.getAnnotation(WriteScalarToPly.class) != null;
    }

    private boolean hasWriteListToPly(Method m) {
        return m.getAnnotation(WriteListToPly.class) != null;
    }

    /**
     * @param allMethods all methods of user-defined object
     * @return element name map to getters
     */
    private Map<String, List<Method>> findPropertiesGetters(List<Method> allMethods) {
        Map<String, List<Method>> map = new HashMap<>();
        for (Method m : allMethods) {
            if (hasWriteScalarToPly(m) ) {
                WriteScalarToPly ply = m.getAnnotation(WriteScalarToPly.class);
                if (map.get(ply.element()) == null) map.put(ply.element(), new ArrayList<Method>());
                map.get(ply.element()).add(m);
            }
        }
        // by default, scalar properties is prior to list properties.
        for (Method m : allMethods) {
            if (hasWriteListToPly(m) ) {
                WriteListToPly ply = m.getAnnotation(WriteListToPly.class);
                if (map.get(ply.element()) == null) map.put(ply.element(), new ArrayList<Method>());
                map.get(ply.element()).add(m);
            }
        }
        return map;
    }

//    private List<Method> findListGetters(List<Method> allMethods) {
//        List<Method> l = new Vector<>();
//        for (Method m : allMethods) {
//            WriteListToPly listToPly = m.getAnnotation(WriteListToPly.class);
//            if (listToPly != null) {
//                l.add(m);
//            }
//        }
//        return l;
//    }


    private String typeString(PlyPropertyType type) {
        if (type instanceof PlyPropertyType.PlyScalarType) {
            PlyPropertyType.PlyScalarType scalarType = (PlyPropertyType.PlyScalarType) type;
            return scalarType.dataType().typeName();
        } else if (type instanceof PlyPropertyType.PlyListType) {
            PlyPropertyType.PlyListType listType = (PlyPropertyType.PlyListType) type;
            return "list " + listType.sizeType().typeName() + " " + listType.dataType().typeName();
        } else {
            throw new IllegalStateException("Unsupported ply property type");
        }
    }

    private void writeImpl(PlyWriterRequest request) throws IOException {
        StringBuffer buffer = generatePlyHeaderString(request);
        switch (request.format) {
            case ASCII:
                // write string
                writeAsciiPlyImpl(buffer, request);
                break;
            case BINARY_BIG_ENDIAN:
                // write bytes
                writeBinaryPlyImpl(buffer, request, ByteOrder.BIG_ENDIAN);
                break;
            case BINARY_LITTLE_ENDIAN:
                writeBinaryPlyImpl(buffer, request, ByteOrder.LITTLE_ENDIAN);
                break;
        }
    }

    private StringBuffer generatePlyHeaderString(PlyWriterRequest request) {
        StringBuffer buffer = new StringBuffer("ply\n");
        buffer.append("format ");
        switch (request.format) {
            case ASCII:
                buffer.append("ascii");
                break;
            case BINARY_BIG_ENDIAN:
                buffer.append("binary_big_endian");
                break;
            case BINARY_LITTLE_ENDIAN:
                buffer.append("binary_little_endian");
                break;
        }
        buffer.append(" 1.0\n");
        for (String comment : request.comments) {
            if (comment.contains("\n")) {
                System.err.println("Warning: Do not use LF(\\n) in your comment.");
                System.err.println("This comment will be neglected. " + comment);
                continue;
            }
            buffer.append("comment ").append(comment).append("\n");
        }
        for (PlyHeader.PlyElementHeader element : request.elements) {
            buffer
                    .append("element ")
                    .append(element.elementName)
                    .append(" ")
                    .append(element.number)
                    .append("\n")
            ;
            for (Pair<String, PlyPropertyType> pair : element.properties) {
                buffer
                        .append("property ")
                        .append(typeString(pair.getValue()))
                        .append(" ")
                        .append(pair.getKey())
                        .append("\n");
            }
        }
        buffer.append("end_header\n");
        return buffer;
    }

    private void writeAsciiPlyImpl(StringBuffer header, PlyWriterRequest pq) throws FileNotFoundException {
        File file = pq.file;
        PrintStream ps = new PrintStream(new FileOutputStream(file));
        ps.print(header.toString());
        for (PlyHeader.PlyElementHeader element : pq.elements) {
            Map<String, List> dataMap = pq.elementData.get(element.elementName);
            if (element.properties.size() < 1) continue;

            for (int i = 0; i < element.number; i ++) {
                int cnt = 0;
                List partOfData = null;
                for (Pair<String, PlyPropertyType> pair : element.properties) {
                    String propertyName = pair.getKey();

                    if (partOfData == null || dataMap.get(propertyName) != partOfData) {
                        partOfData = dataMap.get(propertyName); cnt = 0;
                    }

                    if (pair.getValue() instanceof PlyPropertyType.PlyListType) {
                        PlyPropertyType.PlyListType listType = (PlyPropertyType.PlyListType) pair.getValue();
                        PcuDataType type = listType.dataType();
                        printAsciiList(partOfData.get(i), ps, type);
                        continue;
                    }
                    PlyPropertyType.PlyScalarType scalarType = (PlyPropertyType.PlyScalarType) pair.getValue();
                    PcuDataType type = scalarType.dataType();

                    switch (type) {
                        case CHAR:
                        case UCHAR: {
                            byte[] partOfRow = (byte[]) partOfData.get(i);
                            ps.print(partOfRow[cnt ++]);
                            break;
                        }
                        case SHORT:
                        case USHORT: {
                            short[] partOfRow = (short[]) partOfData.get(i);
                            ps.print(partOfRow[cnt ++]);
                            break;
                        }
                        case INT:
                        case UINT: {
                            int[] partOfRow = (int[]) partOfData.get(i);
                            ps.print(partOfRow[cnt ++]);
                            break;
                        }
                        case FLOAT: {
                            float[] partOfRow = (float[]) partOfData.get(i);
                            ps.print(partOfRow[cnt ++]);
                            break;
                        }
                        case DOUBLE: {
                            double[] partOfRow = (double []) partOfData.get(i);
                            ps.print(partOfRow[cnt ++]);
                            break;
                        }
                    }
                    ps.print(' ');
                }
                ps.print('\n');
            }
        }
        ps.close();
    }

    private void printAsciiList(Object o, PrintStream ps, PcuDataType valType) {
        switch (valType) {
            case CHAR:
            case UCHAR:
                byte[] ba = (byte[]) o;
                ps.print(ba.length);
                ps.print(' ');
                for (byte b : ba) {
                    ps.print(b);
                    ps.print(' ');
                }
                break;
            case SHORT:
            case USHORT:
                short[] sa = (short[]) o;
                ps.print(sa.length);
                ps.print(' ');
                for (short s : sa) {
                    ps.print(s);
                    ps.print(' ');
                }
                break;
            case INT:
            case UINT:
                int[] ia = (int[]) o;
                ps.print(ia.length);
                ps.print(' ');
                for (int i : ia) {
                    ps.print(i);
                    ps.print(' ');
                }
                break;
            case FLOAT:
                float[] fa = (float[]) o;
                ps.print(fa.length);
                ps.print(' ');
                for (float f : fa) {
                    ps.print(f);
                    ps.print(' ');
                }
                break;
            case DOUBLE:
                double[] da = (double[]) o;
                ps.print(da.length);
                ps.print(' ');
                for (double d : da) {
                    ps.print(d);
                    ps.print(' ');
                }
                break;
        }
    }

    private void writeBinaryPlyImpl(StringBuffer buffer, PlyWriterRequest pq, ByteOrder order) throws IOException {
        BinaryWriter writer = new BinaryWriter(pq.file, order);

        writer.writeString(buffer.toString());
        for (PlyHeader.PlyElementHeader element : pq.elements) {
            Map<String, List> dataMap = pq.elementData.get(element.elementName);
            if (element.properties.size() < 1) continue;
            //int dataSize = dataMap.get(element.properties.get(0).getKey()).size();
            for (int i = 0; i < element.number; i ++) {
                int cnt = 0;
                List partOfData = null;
                for (Pair<String, PlyPropertyType> pair : element.properties) {
                    String propertyName = pair.getKey();
                    if (partOfData == null || dataMap.get(propertyName) != partOfData) {
                        partOfData = dataMap.get(propertyName); cnt = 0;
                    }
                    if (pair.getValue() instanceof PlyPropertyType.PlyListType) {
                        PlyPropertyType.PlyListType listType = (PlyPropertyType.PlyListType) pair.getValue();
                        PcuDataType sizeType = listType.sizeType();
                        PcuDataType valType = listType.dataType();
                        writeBinaryList(partOfData.get(i), writer, valType, sizeType);
                        continue;
                    }
                    PlyPropertyType.PlyScalarType scalarType = (PlyPropertyType.PlyScalarType) pair.getValue();
                    PcuDataType type = scalarType.dataType();

                    switch (type) {
                        case CHAR:
                        case UCHAR: {
                            byte[] partOfRow = (byte[]) partOfData.get(i);
                            writer.writeByte(partOfRow[cnt ++]);
                            break;
                        }
                        case SHORT:
                        case USHORT: {
                            short[] partOfRow = (short[]) partOfData.get(i);
                            writer.writeShort(partOfRow[cnt ++]);
                            break;
                        }
                        case INT:
                        case UINT: {
                            int[] partOfRow = (int[]) partOfData.get(i);
                            writer.writeInt(partOfRow[cnt ++]);
                            break;
                        }
                        case FLOAT: {
                            float[] partOfRow = (float []) partOfData.get(i);
                            writer.writeFloat(partOfRow[cnt ++]);
                            break;
                        }
                        case DOUBLE: {
                            double[] partOfRow = (double []) partOfData.get(i);
                            writer.writeDouble(partOfRow[cnt ++]);
                            break;
                        }
                    }
                }
            }
        }
        writer.close();
    }

    private void writeListSize(int len, BinaryWriter writer, PcuDataType sizeType) throws IOException {
        switch (sizeType) {
            case CHAR:
            case UCHAR:
                writer.writeByte((byte) len);
                break;
            case SHORT:
            case USHORT:
                writer.writeShort((short) len);
                break;
            case INT:
            case UINT:
                writer.writeInt(len);
                break;
            case FLOAT:
                writer.writeFloat(len);
                break;
            case DOUBLE:
                writer.writeDouble(len);
                break;
        }

    }

    private void writeBinaryList(Object o, BinaryWriter writer, PcuDataType valType, PcuDataType sizeType) throws IOException {
        switch (valType) {
            case CHAR:
            case UCHAR:
                byte[] ba = (byte[]) o;
                writeListSize(ba.length, writer, sizeType);
                for (byte b : ba) {
                    writer.writeByte(b);
                }
                break;
            case SHORT:
            case USHORT:
                short[] sa = (short[]) o;
                writeListSize(sa.length, writer, sizeType);
                for (short s : sa) {
                    writer.writeShort(s);
                }
                break;
            case INT:
            case UINT:
                int[] ia = (int[]) o;
                writeListSize(ia.length, writer, sizeType);
                for (int i : ia) {
                    writer.writeInt(i);
                }
                break;
            case FLOAT:
                float[] fa = (float[]) o;
                writeListSize(fa.length, writer, sizeType);
                for (float f : fa) {
                    writer.writeFloat(f);
                }
                break;
            case DOUBLE:
                double[] da = (double[]) o;
                writeListSize(da.length, writer, sizeType);
                for (double d : da) {
                    writer.writeDouble(d);
                }
                break;
        }

    }

    public PlyWriterRequest prepare() {
        return new PlyWriterRequest();
    }

    /**
     * Use prepare() of PlyWriter to create a write request
     **/
    public class PlyWriterRequest {

        List<PlyHeader.PlyElementHeader> elements = new ArrayList<>();

        /**
         * {
         *     elementName1: {
         *         propertyName1: data1,
         *         propertyName2: data2,
         *     },
         *     elementName2: {
         *         propertyName3: data3
         *     },
         * }
         * values in List is java array
         */
        Map<String, Map<String, List>> elementData = new HashMap<>();

        /** FORMAT_ASCII, FORMAT_BINARY_BIG_ENDIAN, FORMAT_BINARY_LITTLE_ENDIAN **/
        PlyFormat format = PlyFormat.ASCII;

        List<String> comments = new ArrayList<>();

        File file = null;

        /**
         * Prohibit creating writer request manually
         */
        private PlyWriterRequest() {
            // do nothing
        }

        public PlyWriterRequest defineElement(String elementName) {
            PlyHeader.PlyElementHeader element = new PlyHeader.PlyElementHeader();
            element.elementName = elementName;
            elements.add(element);
            elementData.put(elementName, new HashMap<String, List>());
            return this;
        }

        public PlyWriterRequest defineScalarProperties(String[] propertyNames, final PcuDataType valType, List data) {
            if (elements.size() < 1) {
                throw new IllegalStateException("defineElement() must be called before defineScalarProperties()");
            }
            PlyHeader.PlyElementHeader element = elements.get(elements.size() - 1);
            int dataSize = data.size();
            if (element.number != 0 && element.number != dataSize) {
                throw new IllegalArgumentException("After defining element " + element.elementName + ", you give two list with different sizes.");
            } else {
                element.number = dataSize;
            }
            for (String propertyName : propertyNames) {
                element.properties.add(new Pair<String, PlyPropertyType>(propertyName, new PlyPropertyType.PlyScalarType() {
                    @Override
                    public PcuDataType dataType() {
                        return valType;
                    }
                }));
                elementData.get(element.elementName).put(propertyName, data);
            }
            return this;
        }

        public PlyWriterRequest defineListProperty(String propertyName, final PcuDataType sizeType, final PcuDataType valType, final List data) {
            if (elements.size() < 1) {
                throw new IllegalStateException("defineElement() must be called before defineScalarProperties()");
            }
            PlyHeader.PlyElementHeader element = elements.get(elements.size() - 1);
            int dataSize = data.size();
            if (element.number != 0 && element.number != dataSize) {
                throw new IllegalArgumentException("After defining element " + element.elementName + ", you give two lists with different sizes.");
            } else {
                element.number = dataSize;
            }
            element.properties.add(new Pair<String, PlyPropertyType>(propertyName, new PlyPropertyType.PlyListType() {
                @Override
                public PcuDataType sizeType() {
                    return sizeType;
                }

                @Override
                public PcuDataType dataType() {
                    return valType;
                }
            }));
//            element.listTypes.put(propertyName, new PlyPropertyType[]{sizeType, valType});
            elementData.get(element.elementName).put(propertyName, data);
            return this;
        }

        public PlyWriterRequest format(PlyFormat format) {
            this.format = format;
            return this;
        }

        public PlyWriterRequest comment(String comment) {
            this.comments.add(comment);
            return this;
        }

        public PlyWriterRequest writeTo(File file) {
            this.file = file;
            return this;
        }

        public int okay() {
            if (file == null) {
                throw new IllegalStateException("writeTo() must be called before okay()");
            }
            int result = Constants.ERR_CODE_NO_ERROR;

            try {
                checkData();
                writeImpl(this);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                result = Constants.ERR_CODE_FILE_NOT_FOUND;
            } catch (IOException e) {
                e.printStackTrace();
                result = Constants.ERR_CODE_FILE_NOT_FOUND;
            } catch (IllegalStateException e) {
                e.printStackTrace();
                result = Constants.ERR_CODE_BAD_WRITE_REQUEST;
            }
            return result;
        }

        private void checkData() {
            for (Map<String, List> m : elementData.values()) {
                if (m.size() < 1) continue;
                int dataSize = 0;
                for (List l : m.values()) {
                    if (dataSize == 0) dataSize = l.size();
                    if (l.size() != dataSize) {
                        throw new IllegalStateException("Data lists for one PlyElement should have same size.");
                    }
                }
            }
        }
    }
}

