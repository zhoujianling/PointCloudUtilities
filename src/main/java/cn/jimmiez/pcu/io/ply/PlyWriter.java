package cn.jimmiez.pcu.io.ply;

import cn.jimmiez.pcu.Constants;
import cn.jimmiez.pcu.io.BinaryWriter;

import java.io.*;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlyWriter {



    private String typeString(PlyElement element, int position) {
        PlyPropertyType type = element.getPropertiesType().get(position);
        String typeStr = type.typeName();
        if (type == PlyPropertyType.LIST) {
            PlyPropertyType[] typePair = element.listTypes.get(element.getPropertiesName().get(position));
            typeStr += String.format(" %s %s", typePair[0].typeName(), typePair[1].typeName());
        }
        return typeStr;
    }

    private void writeImpl(PlyWriterRequest request) throws IOException {
        StringBuffer buffer = generatePlyHeaderString(request);
        if (request.format == PlyReader.FORMAT_ASCII) {
           // write string
            writeAsciiPlyImpl(buffer, request);
        } else if (request.format == PlyReader.FORMAT_BINARY_BIG_ENDIAN) {
           // write bytes
            writeBinaryPlyImpl(buffer, request, ByteOrder.BIG_ENDIAN);
        } else if (request.format == PlyReader.FORMAT_BINARY_LITTLE_ENDIAN) {
            writeBinaryPlyImpl(buffer, request, ByteOrder.LITTLE_ENDIAN);
        } else {
            System.err.println("Warning: unsupported ply format.");
        }
    }

    private StringBuffer generatePlyHeaderString(PlyWriterRequest request) {
        StringBuffer buffer = new StringBuffer("ply\n");
        buffer.append("format ");
        switch (request.format) {
            case PlyReader.FORMAT_ASCII:
                buffer.append("ascii");
                break;
            case PlyReader.FORMAT_BINARY_BIG_ENDIAN:
                buffer.append("binary_big_endian");
                break;
            case PlyReader.FORMAT_BINARY_LITTLE_ENDIAN:
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
        for (PlyElement element : request.elements) {
            buffer.append("element ").append(element.elementName);
            buffer.append(" ").append(request.elementData.get(element).size()).append("\n");
            for (int i = 0; i < element.propertiesName.size(); i ++) {
                buffer.append("property ").append(typeString(element, i)).append(" ").append(element.propertiesName.get(i)).append("\n");
            }
        }
        buffer.append("end_header\n");
        return buffer;
    }

    private void writeAsciiPlyImpl(StringBuffer header, PlyWriterRequest pq) throws FileNotFoundException {
        File file = pq.file;
        PrintStream ps = new PrintStream(new FileOutputStream(file));
        ps.print(header.toString());
        for (PlyElement element : pq.elements) {
            List data = pq.elementData.get(element);
            for (int i = 0; i < data.size(); i ++) {
                List row = (List) data.get(i);
                for (int j = 0; j < element.propertiesType.size(); j ++) {
                    PlyPropertyType type = element.propertiesType.get(j);
                    String propertyName = element.propertiesName.get(j);
                    switch (type) {
                        case CHAR:
                        case UCHAR:
                        case SHORT:
                        case USHORT:
                        case INT:
                        case UINT:
                        case FLOAT:
                        case DOUBLE:
                            ps.print(row.get(j));
                            break;
                        case LIST:
                            printAsciiList(row.get(j), ps, element.listTypes.get(propertyName)[1]);
                            break;
                    }
                    ps.print(' ');
                }
                ps.print('\n');
            }
        }
        ps.close();
    }

    private void printAsciiList(Object o, PrintStream ps, PlyPropertyType valType) {
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
        for (PlyElement element : pq.elements) {
            List data = pq.elementData.get(element);
            for (int i = 0; i < data.size(); i ++) {
                List row = (List) data.get(i);
                for (int j = 0; j < element.propertiesType.size(); j ++) {
                    PlyPropertyType type = element.propertiesType.get(j);
                    String propertyName = element.propertiesName.get(j);
                    switch (type) {
                        case CHAR:
                        case UCHAR: {
                            byte c = (byte) row.get(j);
                            writer.writeByte(c);
//                            writer.write;
                            break;
                        }
                        case SHORT:
                        case USHORT: {
                            short s = (short) row.get(j);
                            writer.writeShort(s);
//                            writer.write(s);
                            break;
                        }
                        case INT:
                        case UINT: {
                            int intVal = (int) row.get(j);
                            writer.writeInt(intVal);
                            break;
                        }
                        case FLOAT: {
                            float floatVal = (float) row.get(j);
                            writer.writeFloat(floatVal);
                            break;
                        }
                        case DOUBLE: {
                            double doubleVal = (double) row.get(j);
                            writer.writeDouble(doubleVal);
                            break;
                        }
                        case LIST: {
                            PlyPropertyType sizeType = element.listTypes.get(propertyName)[0];
                            PlyPropertyType valType = element.listTypes.get(propertyName)[1];
                            writeBinaryList(row.get(j), writer, valType, sizeType);
                            break;
                        }
                    }
                }
            }
        }
        writer.close();
    }

    private void writeListSize(int len, BinaryWriter writer, PlyPropertyType sizeType) throws IOException {
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

    private void writeBinaryList(Object o, BinaryWriter writer, PlyPropertyType valType, PlyPropertyType sizeType) throws IOException {
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

    public class PlyWriterRequest {

        List<PlyElement> elements = new ArrayList<>();

        Map<PlyElement, List> elementData = new HashMap<>();

        /** FORMAT_ASCII, FORMAT_BINARY_BIG_ENDIAN, FORMAT_BINARY_LITTLE_ENDIAN **/
        int format = PlyReader.FORMAT_ASCII;

        List<String> comments = new ArrayList<>();

        File file = null;

        public PlyWriterRequest defineElement(String elementName) {
            PlyElement element = new PlyElement();
            element.elementName = elementName;
            elements.add(element);
            return this;
        }

        public PlyWriterRequest defineScalarProperty(String propertyName, PlyPropertyType valType) {
            if (elements.size() < 1) {
                throw new IllegalStateException("defineElement() must be called before defineScalarProperty()");
            }
            PlyElement element = elements.get(elements.size() - 1);
            element.propertiesName.add(propertyName);
            element.propertiesType.add(valType);
            return this;
        }

        public PlyWriterRequest defineListProperty(String propertyName, PlyPropertyType sizeType, PlyPropertyType valType) {
            if (elements.size() < 1) {
                throw new IllegalStateException("defineElement() must be called before defineScalarProperty()");
            }
            PlyElement element = elements.get(elements.size() - 1);
            element.propertiesName.add(propertyName);
            element.propertiesType.add(PlyPropertyType.LIST);
            element.listTypes.put(propertyName, new PlyPropertyType[]{sizeType, valType});
            return this;
        }

        public PlyWriterRequest putData(List data) {
            if (elements.size() < 1) {
                throw new IllegalStateException("defineElement() must be called before putData()");
            }
            PlyElement element = elements.get(elements.size() - 1);
            if (element.getPropertiesName().size() < 1) {
                throw new IllegalStateException("defineProperty() must be called before putData()");
            }
            // check type
            // put data
            elementData.put(element, data);
            return this;
        }

        public PlyWriterRequest format(int format) {
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
                writeImpl(this);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                result = Constants.ERR_CODE_FILE_NOT_FOUND;
            } catch (IOException e) {
                e.printStackTrace();
                result = Constants.ERR_CODE_FILE_NOT_FOUND;
            }
            return result;
        }
    }
}
