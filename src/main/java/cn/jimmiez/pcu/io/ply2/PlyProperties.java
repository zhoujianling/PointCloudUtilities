package cn.jimmiez.pcu.io.ply2;

import cn.jimmiez.pcu.io.ply.PlyPropertyType;

public interface PlyProperties {

    int parseNextPropertyAsInt();

    byte parseNextPropertyAsChar();

    double parseNextPropertyAsDouble();

    float parseNextPropertyAsFloat();

    short parseNextPropertyAsShort();

    int[] parseNextPropertyAsListI();

    double[] parseNextPropertyAsListF();

}
