package cn.jimmiez.pcu.io.ply2;

import cn.jimmiez.pcu.io.ply.PlyHeader;
import cn.jimmiez.pcu.io.ply.PlyReader;
import cn.jimmiez.pcu.io.ply2.PlyElement2;

import java.io.File;
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

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public PlyElement2 next() {
            return null;
        }

        @Override
        public void remove() {

        }
    }

    private class BinaryIterator implements Iterator<PlyElement2> {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public PlyElement2 next() {
            return null;
        }

        @Override
        public void remove() {

        }
    }
}
