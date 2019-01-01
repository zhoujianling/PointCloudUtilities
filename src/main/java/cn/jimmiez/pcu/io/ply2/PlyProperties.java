package cn.jimmiez.pcu.io.ply2;

public interface PlyProperties {

    int nextPropertyAsInt();

    byte nextPropertyAsChar();

    double nextPropertyAsDouble();

    float nextPropertyAsFloat();

    short nextPropertyAsShort();

    byte[] nextPropertyAsListB(PcuDataType sizeType);

    int[] nextPropertyAsListI(PcuDataType sizeType);

    short[] nextPropertyAsListS(PcuDataType sizeType);

    double[] nextPropertyAsListD(PcuDataType sizeType);

    float[] nextPropertyAsListF(PcuDataType sizeType);

}
