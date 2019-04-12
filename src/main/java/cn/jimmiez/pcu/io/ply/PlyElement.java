package cn.jimmiez.pcu.io.ply;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class PlyElement {

    //private byte[] bytes = null;
    // store int,char,double,float using DOUBLE
    // if the properties is a list, store a key,
    double[] elementData = null;

    List<double[]> elementListData = null;

    private PlyHeader.PlyElementHeader header = null;

    public PlyElement(PlyHeader.PlyElementHeader header) {
        this.elementData = new double[header.number * header.properties.size()];
        // TODO: 2019/3/20 set appropriate initial size
        this.elementListData = new ArrayList<>();
        this.header = header;
    }


    public PlyHeader.PlyElementHeader getHeader() {
        return header;
    }

    public static int parseInt(ByteBuffer buffer, PcuDataType dataType) throws IOException {
        switch (dataType) {
            case CHAR:
            case UCHAR:
                return (int)buffer.get();
            case SHORT:
            case USHORT:
                return (int)buffer.getShort();
            case INT:
            case UINT:
                return buffer.getInt();
            case FLOAT:
                return (int)buffer.getFloat();
            case DOUBLE:
                return (int)buffer.getDouble();
            default:
                throw new IOException("Unsupported data type: " + dataType);
        }
    }

//    public static double parseDouble(ByteBuffer buffer, PcuDataType dataType) throws IOException {
//        switch (dataType) {
//            case CHAR:
//            case UCHAR:
//                return (double) buffer.get();
//            case SHORT:
//            case USHORT:
//                return (double) buffer.getShort();
//            case INT:
//            case UINT:
//                return (double) buffer.getInt();
//            case FLOAT:
//                return (double) buffer.getFloat();
//            case DOUBLE:
//                return buffer.getDouble();
//            default:
//                throw new IOException("Unsupported data type: " + dataType);
//        }
//    }
//
//    private class AsciiIterator implements Iterator<PlyProperties>{
//
//        private int linePointer = 0;
//
////        private Scanner scanner = null;
//
//        public AsciiIterator() {
////            try {
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
//        }
//
//        @Override
//        public boolean hasNext() {
//            return linePointer < header.number;
//        }
//
//        @Override
//        public PlyProperties next() {
//            InputStream is = new ByteArrayInputStream(bytes);
//            try {
//                is.skip(startPositions[0]);
//            } catch (IOException e) {
//                e.printStackTrace();
//                return null;
//            }
//            final Scanner scanner = new Scanner(is);
//            PlyProperties properties = new PlyProperties() {
//                @Override
//                public int nextPropertyAsInt() {
//                    return scanner.nextInt();
//                }
//
//                @Override
//                public byte nextPropertyAsChar() {
//                    return scanner.nextByte();
//                }
//
//                @Override
//                public double nextPropertyAsDouble() {
//                    return scanner.nextDouble();
//                }
//
//                @Override
//                public float nextPropertyAsFloat() {
//                    return scanner.nextFloat();
//                }
//
//                @Override
//                public short nextPropertyAsShort() {
//                    return scanner.nextShort();
//                }
//
//                @Override
//                public byte[] nextPropertyAsListB(PcuDataType sizeType) {
//                    int length = scanner.nextInt();
//                    byte[] array = new byte[length];
//                    for (int i = 0; i < length; i ++) {
//                        array[i] = scanner.nextByte();
//                    }
//                    return array;
//                }
//
//                @Override
//                public int[] nextPropertyAsListI(PcuDataType sizeType) {
//                    int length = scanner.nextInt();
//                    int[] array = new int[length];
//                    for (int i = 0; i < length; i ++) {
//                        array[i] = scanner.nextInt();
//                    }
//                    return array;
//                }
//
//                @Override
//                public short[] nextPropertyAsListS(PcuDataType sizeType) {
//                    int length = scanner.nextInt();
//                    short[] array = new short[length];
//                    for (int i = 0; i < length; i ++) {
//                        array[i] = scanner.nextShort();
//                    }
//                    return array;
//                }
//
//                @Override
//                public double[] nextPropertyAsListD(PcuDataType sizeType) {
//                    int length = scanner.nextInt();
//                    double[] array = new double[length];
//                    for (int i = 0; i < length; i ++) {
//                        array[i] = scanner.nextDouble();
//                    }
//                    return array;
//                }
//
//                @Override
//                public float[] nextPropertyAsListF(PcuDataType sizeType) {
//                    int length = scanner.nextInt();
//                    float[] array = new float[length];
//                    for (int i = 0; i < length; i ++) {
//                        array[i] = scanner.nextFloat();
//                    }
//                    return array;
//                }
//            };
//            linePointer += 1;
//            if (! hasNext()) bytes = null;
//            return properties;
//        }
//
//        @Override
//        public void remove() {
//             // do nothing
//        }
//    }
//
//    private class BinaryIterator implements Iterator<PlyProperties> {
//
//        private int linePointer = 0;
//
//        private ByteOrder order = null;
//
//        public BinaryIterator(ByteOrder order) {
//            this.order = order;
//        }
//
//        @Override
//        public boolean hasNext() {
//            return linePointer < header.number;
//        }
//
//        @Override
//        public PlyProperties next() {
//            final ByteBuffer buffer = ByteBuffer.wrap(bytes, startPositions[linePointer], bytes.length - startPositions[linePointer]);
//            buffer.order(order);
//            PlyProperties properties = new PlyProperties() {
//                @Override
//                public int nextPropertyAsInt() {
//                    return buffer.getInt();
//                }
//
//                @Override
//                public byte nextPropertyAsChar() {
//                    return buffer.get();
//                }
//
//                @Override
//                public double nextPropertyAsDouble() {
//                    return buffer.getDouble();
//                }
//
//                @Override
//                public float nextPropertyAsFloat() {
//                    return buffer.getFloat();
//                }
//
//                @Override
//                public short nextPropertyAsShort() {
//                    return buffer.getShort();
//                }
//
//                @Override
//                public byte[] nextPropertyAsListB(PcuDataType sizeType) {
//                    try {
//                        int size = parseInt(buffer, sizeType);
//                        byte[] list = new byte[size];
//                        for (int i = 0; i < size; i ++)
//                            list[i] = buffer.get();
//                        return list;
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return new byte[0];
//                }
//
//                @Override
//                public int[] nextPropertyAsListI(PcuDataType sizeType) {
//                    try {
//                        int size = parseInt(buffer, sizeType);
//                        int[] list = new int[size];
//                        for (int i = 0; i < size; i ++)
//                            list[i] = buffer.getInt();
//                        return list;
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return new int[0];
//                }
//
//                @Override
//                public short[] nextPropertyAsListS(PcuDataType sizeType) {
//                    try {
//                        int size = parseInt(buffer, sizeType);
//                        short[] list = new short[size];
//                        for (int i = 0; i < size; i ++)
//                            list[i] = buffer.getShort();
//                        return list;
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return new short[0];
//                }
//
//                @Override
//                public double[] nextPropertyAsListD(PcuDataType sizeType) {
//                    try {
//                        int size = parseInt(buffer, sizeType);
//                        double[] list = new double[size];
//                        for (int i = 0; i < size; i ++)
//                            list[i] = buffer.getDouble();
//                        return list;
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return new double[0];
//                }
//
//                @Override
//                public float[] nextPropertyAsListF(PcuDataType sizeType) {
//                    try {
//                        int size = parseInt(buffer, sizeType);
//                        float[] list = new float[size];
//                        for (int i = 0; i < size; i ++)
//                            list[i] = buffer.getFloat();
//                        return list;
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return new float[0];
//                }
//            };
//            linePointer += 1;
//            if (! hasNext()) bytes = null;
//            return properties;
//        }
//
//        @Override
//        public void remove() {
//            // do nothing
//        }
//    }

}
