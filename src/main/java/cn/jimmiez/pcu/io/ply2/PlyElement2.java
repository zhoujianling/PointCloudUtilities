package cn.jimmiez.pcu.io.ply2;

import cn.jimmiez.pcu.io.ply2.PlyProperties;

import java.util.Iterator;

public class PlyElement2 implements Iterable<PlyProperties>{

    byte[] bytes = null;
    int cnt = 0;
    int[] startPositions = null;

    public PlyElement2 (byte[] bs, int skipCount, int[] startPositions) {
        this.bytes = bs;
        this.cnt = skipCount;
        this.startPositions = startPositions;
    }

    @Override
    public Iterator<PlyProperties> iterator() {
        return iterator;
    }

    private Iterator<PlyProperties> iterator = null;

    private class AsciiIterator implements Iterator<PlyProperties>{


        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public PlyProperties next() {
            return null;
        }

        @Override
        public void remove() {

        }
    }

    private class BinaryIterator implements Iterator<PlyProperties> {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public PlyProperties next() {
            return null;
        }

        @Override
        public void remove() {
            // do nothing
        }
    }

}
