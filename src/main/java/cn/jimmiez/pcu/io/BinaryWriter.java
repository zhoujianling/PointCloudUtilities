package cn.jimmiez.pcu.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;

public class BinaryWriter {

    private FileOutputStream fos = null;
    private ByteOrder order = null;

    public BinaryWriter(File file, ByteOrder order) throws FileNotFoundException {
        this.fos = new FileOutputStream(file);
        this.order = order;
    }

    public void writeString(String str) throws IOException {
        fos.write(str.getBytes());
    }

    public void writeInt(int i) {

    }

    public void writeByte(byte b) {

    }

    public void writeShort(short s) {

    }

    public void writeFloat(float f) {

    }

    public void writeDouble(double d) {

    }

    public void close() {

    }
}
