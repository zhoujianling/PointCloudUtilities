package cn.jimmiez.pcu.io.ply2;

import cn.jimmiez.pcu.io.ply.PlyHeader;
import cn.jimmiez.pcu.io.ply.PlyReader;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Iterator;

public class PlyData implements Iterable<PlyElement2>{

    private Iterator<PlyElement2> iterator = null;

    private PlyHeader header = null;

    byte[] bytes = null;

    public PlyData(File file, PlyHeader header) {
        this.header = header;
        if (header.getPlyFormat() == PlyReader.FORMAT_ASCII) {
            iterator = new AsciiIterator();
        } else {
            iterator = new BinaryIterator();
        }
    }

    @Override
    public Iterator<PlyElement2> iterator() {
        return iterator;
    }

    private class AsciiIterator implements Iterator<PlyElement2> {

        private int bytePointer = header.getHeaderBytes() + 1;

        private int elementPointer = 0;

        @Override
        public boolean hasNext() {
            return elementPointer < header.getElementsNumber().size();
        }

        @Override
        public PlyElement2 next() {
            int[] startPositions = new int[header.getElementsNumber().get(elementPointer).getValue()];
            int linePtr = 0;
            for (; bytePointer < bytes.length; bytePointer ++) {
                if (bytes[bytePointer] != '\n') continue;
                startPositions[linePtr] = bytePointer + 1;
                linePtr += 1;
                if (linePtr >= header.getElementsNumber().get(elementPointer).getValue()) break;
            }
            PlyElement2 element2 = new PlyElement2(bytes, bytePointer, startPositions);
            elementPointer += 1;
            return element2;
        }

        @Override
        public void remove() {
            // do nothing
        }
    }

    private class BinaryIterator implements Iterator<PlyElement2> {

        private int bytePointer = header.getHeaderBytes() + 1;

        private int elementPointer = 0;

        BinaryIterator() {

        }

        @Override
        public boolean hasNext() {
            return elementPointer < header.getElementsNumber().size();
        }

        @Override
        public PlyElement2 next() {
            int[] startPositions = new int[header.getElementsNumber().get(elementPointer).getValue()];
            for (int linePointer = 0; linePointer < header.getElementsNumber().get(elementPointer).getValue(); linePointer ++) {
                startPositions[linePointer] = bytePointer;

            }
            PlyElement2 element2 = new PlyElement2(bytes, bytePointer, startPositions);
            elementPointer += 1;
            return element2;
        }



        @Override
        public void remove() {
            // do nothing
        }
    }
}
