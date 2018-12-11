package cn.jimmiez.pcu.io.ply2;


import cn.jimmiez.pcu.util.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.Iterator;

public class PlyData implements Iterable<PlyElement2>{

    private Iterator<PlyElement2> iterator = null;

    private PlyHeader2 header = null;

    byte[] bytes = null;

    PlyData(File file, PlyHeader2 header) throws IOException {
        this.header = header;
        switch (header.getFormat()) {
            case ASCII:
                iterator = new AsciiIterator();
                break;
            case BINARY_BIG_ENDIAN:
                iterator = new BinaryIterator(ByteOrder.BIG_ENDIAN);
                break;
            case BINARY_LITTLE_ENDIAN:
                iterator = new BinaryIterator(ByteOrder.LITTLE_ENDIAN);
                break;
            default:
                throw new IOException("Unsupported Ply format: " + header.getFormat());
        }
        bytes = Files.readAllBytes(file.toPath());
    }

    @Override
    public Iterator<PlyElement2> iterator() {
        return iterator;
    }

    private class AsciiIterator implements Iterator<PlyElement2> {

        private int bytePointer = header.getBytesCount();

        private int elementPointer = 0;

        @Override
        public boolean hasNext() {
            return elementPointer < header.getElementHeaders().size();
        }

        @Override
        public PlyElement2 next() {
            int[] startPositions = new int[header.getElementHeaders().get(elementPointer).number];
            int linePtr = 0;
            startPositions[linePtr] = bytePointer;
            for (; bytePointer < bytes.length; bytePointer ++) {
                if (bytes[bytePointer] != '\n') continue;
                linePtr += 1;
                if (linePtr >= header.getElementHeaders().get(elementPointer).number) break;
                startPositions[linePtr] = bytePointer + 1;
            }
            PlyElement2 element2 = new PlyElement2(bytes, startPositions, header.getElementHeaders().get(elementPointer), header.getFormat());
            elementPointer += 1;
            if (! hasNext()) bytes = null;
            return element2;
        }

        @Override
        public void remove() {
            // do nothing
        }
    }

    private class BinaryIterator implements Iterator<PlyElement2> {

        private int bytePointer = header.getBytesCount();

        private int elementPointer = 0;

        private ByteOrder order = null;

        BinaryIterator(ByteOrder order) {
            this.order = order;
        }

        @Override
        public boolean hasNext() {
            return elementPointer < header.getElementHeaders().size();
        }

        @Override
        public PlyElement2 next() {
            int[] startPositions = new int[header.getElementHeaders().get(elementPointer).number];
            PlyElement2 element2 = null;
            try {
                for (int linePointer = 0; linePointer < header.getElementHeaders().get(elementPointer).number; linePointer ++) {
                    startPositions[linePointer] = bytePointer;
                    marchOneLine();
                }
                element2 = new PlyElement2(bytes, startPositions, header.getElementHeaders().get(elementPointer), header.getFormat());
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("No enough bytes when marching one line.");
            }
            elementPointer += 1;
            if (! hasNext()) bytes = null;
            return element2;
        }

        private void marchOneLine() throws IOException{
            PlyHeader2.PlyElementHeader elementHeader = header.getElementHeaders().get(elementPointer);
            for (Pair<String, PlyPropertyType2> pair : elementHeader.getProperties()) {
                PlyPropertyType2 type = pair.getValue();
                if (type instanceof PlyPropertyType2.PlyScalarType) {
                    bytePointer += ((PlyPropertyType2.PlyScalarType)type).dataType().size();
                } else if (type instanceof PlyPropertyType2.PlyListType) {
                    PcuDataType sizeType = ((PlyPropertyType2.PlyListType)type).sizeType();
                    PcuDataType dataType = ((PlyPropertyType2.PlyListType)type).dataType();
                    int sizeOfSize = sizeType.size();
                    if (bytePointer + sizeOfSize > bytes.length) throw new IOException("Space not enough, current byte pointer: " + bytePointer);
                    ByteBuffer buffer = ByteBuffer.wrap(bytes, bytePointer, sizeOfSize);
                    buffer.order(order);
                    int size = PlyElement2.parseInt(buffer, sizeType);
                    bytePointer += sizeOfSize;
                    // **********************
                    int sizeOfData = dataType.size() * size;
                    if (bytePointer + sizeOfData > bytes.length) throw new IOException("Space not enough");
                    bytePointer += sizeOfData;
                }
            }
        }

        @Override
        public void remove() {
            // do nothing
        }
    }
}
