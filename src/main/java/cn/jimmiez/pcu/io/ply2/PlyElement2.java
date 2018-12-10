package cn.jimmiez.pcu.io.ply2;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Scanner;

public class PlyElement2 implements Iterable<PlyProperties>{

    private byte[] bytes = null;

    private int[] startPositions = null;

    private PlyHeader2.PlyElementHeader header = null;

    public PlyElement2 (byte[] bs, int[] startPositions, PlyHeader2.PlyElementHeader header, PlyFormat format) {
        this.bytes = bs;
        this.startPositions = startPositions;
        this.header = header;
        if (format == PlyFormat.ASCII) {
            iterator = new AsciiIterator();
        } else if (format == PlyFormat.BINARY_BIG_ENDIAN) {
            iterator = new BinaryIterator();
        } else if (format == PlyFormat.BINARY_LITTLE_ENDIAN) {
            iterator = new BinaryIterator();
        }
    }

    @Override
    public Iterator<PlyProperties> iterator() {
        return iterator;
    }

    private Iterator<PlyProperties> iterator = null;

    public static int parseSizeOfList(ByteBuffer buffer, PcuDataType dataType) throws IOException {
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

    private class AsciiIterator implements Iterator<PlyProperties>{

        private int linePointer = 0;

        private Scanner scanner = null;

        public AsciiIterator() {
            try {
                InputStream is = new ByteArrayInputStream(bytes);
                is.skip(startPositions[0]);
                scanner = new Scanner(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean hasNext() {
            return linePointer < header.number;
        }

        @Override
        public PlyProperties next() {

            PlyProperties properties = new PlyProperties() {
                @Override
                public int parseNextPropertyAsInt() {
                    return scanner.nextInt();
                }

                @Override
                public byte parseNextPropertyAsChar() {
                    return scanner.nextByte();
                }

                @Override
                public double parseNextPropertyAsDouble() {
                    return scanner.nextDouble();
                }

                @Override
                public float parseNextPropertyAsFloat() {
                    return scanner.nextFloat();
                }

                @Override
                public short parseNextPropertyAsShort() {
                    return scanner.nextShort();
                }

                @Override
                public int[] parseNextPropertyAsListI() {
                    int length = scanner.nextInt();
                    int[] array = new int[length];
                    for (int i = 0; i < length; i ++) {
                        array[i] = scanner.nextInt();
                    }
                    return array;
                }

                @Override
                public double[] parseNextPropertyAsListF() {
                    int length = scanner.nextInt();
                    double[] array = new double[length];
                    for (int i = 0; i < length; i ++) {
                        array[i] = scanner.nextDouble();
                    }
                    return array;
                }
            };
            linePointer += 1;
            if (! hasNext()) bytes = null;
            return properties;
        }

        @Override
        public void remove() {
             // do nothing
        }
    }

    private class BinaryIterator implements Iterator<PlyProperties> {

        private int linePointer = 0;

        private ByteBuffer buffer = null;

        public BinaryIterator() {

        }

        @Override
        public boolean hasNext() {
            return linePointer < header.number;
        }

        @Override
        public PlyProperties next() {
            PlyProperties properties = new PlyProperties() {
                @Override
                public int parseNextPropertyAsInt() {
                    return 0;
                }

                @Override
                public byte parseNextPropertyAsChar() {
                    return 0;
                }

                @Override
                public double parseNextPropertyAsDouble() {
                    return 0;
                }

                @Override
                public float parseNextPropertyAsFloat() {
                    return 0;
                }

                @Override
                public short parseNextPropertyAsShort() {
                    return 0;
                }

                @Override
                public int[] parseNextPropertyAsListI() {
                    return new int[0];
                }

                @Override
                public double[] parseNextPropertyAsListF() {
                    return new double[0];
                }
            };
            linePointer += 1;
            if (! hasNext()) bytes = null;
            return properties;
        }

        @Override
        public void remove() {
            // do nothing
        }
    }

}
