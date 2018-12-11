package cn.jimmiez.pcu.io.ply2;

public interface PlyProperties {

    int nextPropertyAsInt();

    byte nextPropertyAsChar();

    double nextPropertyAsDouble();

    float nextPropertyAsFloat();

    short nextPropertyAsShort();

    int[] nextPropertyAsListI(PcuDataType sizeType, PcuDataType dataType);

    double[] nextPropertyAsListF(PcuDataType sizeType, PcuDataType dataType);

}
