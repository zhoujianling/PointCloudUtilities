package cn.jimmiez.pcu.io.ply2;

import cn.jimmiez.pcu.io.ply.PlyPropertyType;

public interface PlyProperties {

    int getPropertyAsInt(String propertyName);

    byte getPropertyAsChar(String propertyName);

    double getPropertyAsDouble(String propertyName);

    float getPropertyAsFloat(String propertyName);

    short getPropertyAsShort(String propertyName);

    long getPropertyAsLong(String propertyName);

    int[] getPropertyAsListI(String propertyName, PlyPropertyType type);

    float[] getPropertyAsListF(String propertyName, PlyPropertyType type);

}
