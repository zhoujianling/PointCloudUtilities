package cn.jimmiez.pcu.io.ply;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class PlyData implements Iterable<PlyElement>{

    private List<PlyElement> plyElements = null;

    private PlyHeader header = null;

    PlyData(PlyHeader header) {
        if (header == null) throw new NullPointerException("PlyHeader is null.");
        this.header = header;
        this.plyElements = new ArrayList<>();
    }

    void parse(byte[] bytes) throws IOException {
        PlyElementParser parser = null;
        switch (header.getFormat()) {
            case ASCII:
                parser = new AsciiParser(bytes);
                break;
            case BINARY_BIG_ENDIAN:
                parser = new BinaryParser(bytes, ByteOrder.BIG_ENDIAN);
                break;
            case BINARY_LITTLE_ENDIAN:
                parser = new BinaryParser(bytes, ByteOrder.LITTLE_ENDIAN);
                break;
            default:
                throw new IOException("Unsupported Ply format: " + header.getFormat());
        }
        PlyElement element = null;
        while ((element = parser.next()) != null) {
            this.plyElements.add(element);
        }
    }

    public PlyElement getElement(String elementName) {
        int index = header.findElement(elementName);
        if (index < 0 || index >= plyElements.size()) return null;
        return plyElements.get(index);
    }

    public PlyHeader getHeader() {
        return header;
    }

    @Override
    public Iterator<PlyElement> iterator() {
        return plyElements.iterator();
    }

    private interface PlyElementParser {
        PlyElement next() throws IOException;
    }

    private class AsciiParser implements PlyElementParser {

        private int elementPointer = 0;

        private byte[] bytes = null;

        public AsciiParser(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public PlyElement next() throws IOException {
            if (elementPointer >= header.getElementHeaders().size()) return null;
            PlyElement element = new PlyElement(header.getElementHeaders().get(elementPointer));
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes, header.getBytesCount(), bytes.length - header.getBytesCount());
            int propertiesCount = element.getHeader().properties.size();
            // TODO: 2019/3/20 optimize scanner read()
            Scanner scanner = new Scanner(stream);
            boolean[] isList = new boolean[element.getHeader().properties.size()];
            for (int i = 0; i < isList.length; i ++) {
                isList[i] = false;
                if (element.getHeader().properties.get(i).getValue() instanceof PlyPropertyType.PlyListType) {
                    isList[i] = true;
                }
            }
            for (int lineCount = 0; lineCount < element.getHeader().number; lineCount ++) {
                if (! scanner.hasNextLine()) {
                    throw new IOException("no enough data");
                }
                String nextLine = scanner.nextLine();
                String[] vals = nextLine.split("\\s+");
                if (vals.length < propertiesCount) throw new IllegalStateException("Less properties than expected.");
                for (int i = 0; i < propertiesCount; i ++) {
                    double val = Double.valueOf(vals[i]);
                    if (isList[i]) {
                        int listSize = (int) val;
                        double[] list = new double[listSize];
                        for (int j = 0; j < listSize; j ++) list[j] = Double.valueOf(vals[i + j + 1]);
                        element.elementListData.add(list);
                        val = element.elementListData.size() - 1;
                    }
                    element.elementData[lineCount * propertiesCount + i] = val;
                }
            }
            elementPointer += 1;
            return element;
        }
    }

    private class BinaryParser implements PlyElementParser {

        private int elementPointer = 0;

        private ByteBuffer buffer = null;

        BinaryParser(byte[] bytes, ByteOrder order) {
            buffer = ByteBuffer.wrap(bytes, header.getBytesCount(), bytes.length - header.getBytesCount());
            buffer.order(order);
        }

        @Override
        public PlyElement next() throws IOException {
            if (elementPointer >= header.getElementHeaders().size()) return null;
            PlyElement element = new PlyElement(header.getElementHeaders().get(elementPointer));
            int propertiesCount = element.getHeader().properties.size();
            int minByteCount = 0; // for one
            boolean[] isList = new boolean[element.getHeader().properties.size()];
            PcuDataType[] types = new PcuDataType[element.getHeader().properties.size()];
            PcuDataType[] listDataType = new PcuDataType[element.getHeader().properties.size()];
            for (int i = 0; i < isList.length; i ++) {
                isList[i] = false;
                PlyPropertyType propertyType = element.getHeader().properties.get(i).getValue();
                if (propertyType instanceof PlyPropertyType.PlyListType) {
                    PlyPropertyType.PlyListType listType = (PlyPropertyType.PlyListType) propertyType;
                    isList[i] = true;
                    minByteCount += listType.sizeType().size();
                    types[i] = listType.sizeType();
                    listDataType[i] = listType.dataType();
                } else if (propertyType instanceof PlyPropertyType.PlyScalarType) {
                    PlyPropertyType.PlyScalarType scalarType = (PlyPropertyType.PlyScalarType) propertyType;
                    minByteCount += scalarType.dataType().size();
                    types[i] = scalarType.dataType();
                }
            }
            for (int lineCount = 0; lineCount < element.getHeader().number; lineCount ++) {
                if (buffer.remaining() < minByteCount) {
                    throw new IOException("no enough data");
                }
                for (int i = 0; i < propertiesCount; i ++) {
                    double val = nextDouble(types[i]);
                    if (isList[i]) {
                        int listSize = (int) val;
                        double[] list = new double[listSize];
                        for (int j = 0; j < listSize; j ++) list[j] = nextDouble(listDataType[i]);
                        element.elementListData.add(list);
                        val = element.elementListData.size() - 1;
                    }
                    element.elementData[lineCount * propertiesCount + i] = val;
                }
            }
            elementPointer += 1;
            return element;
        }

        double nextDouble(PcuDataType type) {
            switch (type) {
                case CHAR:
                case UCHAR:
                    return buffer.get();
                case SHORT:
                case USHORT:
                    return buffer.getShort();
                case INT:
                case UINT:
                    return buffer.getInt();
                case FLOAT:
                    return buffer.getFloat();
                case DOUBLE:
                    return buffer.getDouble();
            }
            return 0;
        }

    }

}
