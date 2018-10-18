package cn.jimmiez.pcu.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;

public class BinaryWriter {

    private FileOutputStream fos = null;
    private ByteOrder order = null;
    private byte[] byteBuffer = new byte[1];
    private byte[] intBuffer = new byte[4];
    private byte[] shortBuffer = new byte[2];
    private byte[] longBuffer = new byte[8];

    public BinaryWriter(File file, ByteOrder order) throws FileNotFoundException {
        this.fos = new FileOutputStream(file);
        this.order = order;
    }

    public void writeString(String str) throws IOException {
        fos.write(str.getBytes());
    }

    public void writeInt(int i) throws IOException {
        if (order == ByteOrder.LITTLE_ENDIAN) {
            intBuffer[3] = (byte) ((i >> 24) & 0x0000_00FF);
            intBuffer[2] = (byte) ((i >> 16) & 0x0000_00FF);
            intBuffer[1] = (byte) ((i >> 8) & 0x0000_00FF);
            intBuffer[0] = (byte) ((i >> 0) & 0x0000_00FF);
        } else {
            intBuffer[0] = (byte) ((i >> 24) & 0x0000_00FF);
            intBuffer[1] = (byte) ((i >> 16) & 0x0000_00FF);
            intBuffer[2] = (byte) ((i >> 8) & 0x0000_00FF);
            intBuffer[3] = (byte) ((i >> 0) & 0x0000_00FF);
        }
        fos.write(intBuffer);
    }

    public void writeLong(long l) throws IOException {
        if (order == ByteOrder.LITTLE_ENDIAN) {
            longBuffer[7] = (byte) ((l >> 56) & 0x0000_00FF);
            longBuffer[6] = (byte) ((l >> 48) & 0x0000_00FF);
            longBuffer[5] = (byte) ((l >> 40) & 0x0000_00FF);
            longBuffer[4] = (byte) ((l >> 32) & 0x0000_00FF);
            longBuffer[3] = (byte) ((l >> 24) & 0x0000_00FF);
            longBuffer[2] = (byte) ((l >> 16) & 0x0000_00FF);
            longBuffer[1] = (byte) ((l >> 8) & 0x0000_00FF);
            longBuffer[0] = (byte) ((l >> 0) & 0x0000_00FF);
        } else {
            longBuffer[0] = (byte) ((l >> 56) & 0x0000_00FF);
            longBuffer[1] = (byte) ((l >> 48) & 0x0000_00FF);
            longBuffer[2] = (byte) ((l >> 40) & 0x0000_00FF);
            longBuffer[3] = (byte) ((l >> 32) & 0x0000_00FF);
            longBuffer[4] = (byte) ((l >> 24) & 0x0000_00FF);
            longBuffer[5] = (byte) ((l >> 16) & 0x0000_00FF);
            longBuffer[6] = (byte) ((l >> 8) & 0x0000_00FF);
            longBuffer[7] = (byte) ((l >> 0) & 0x0000_00FF);
        }
        fos.write(longBuffer);
    }

    public void writeByte(byte b) throws IOException {
        byteBuffer[0] = b;
        fos.write(byteBuffer);
    }

    public void writeShort(short s) throws IOException {
        if (order == ByteOrder.LITTLE_ENDIAN) {
            shortBuffer[1] = (byte) ((s >> 8) & 0x0000_00FF);
            shortBuffer[0] = (byte) ((s >> 0) & 0x0000_00FF);
        } else {
            shortBuffer[0] = (byte) ((s >> 8) & 0x0000_00FF);
            shortBuffer[1] = (byte) ((s >> 0) & 0x0000_00FF);
        }
        fos.write(shortBuffer);
    }

    public void writeFloat(float f) throws IOException {
        writeInt(Float.floatToIntBits(f));
    }

    public void writeDouble(double d) throws IOException {
        writeLong(Double.doubleToLongBits(d));
    }

    public void close() throws IOException {
        fos.close();
    }
}
