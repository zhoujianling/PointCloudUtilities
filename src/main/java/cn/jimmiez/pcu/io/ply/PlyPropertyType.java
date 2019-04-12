package cn.jimmiez.pcu.io.ply;

public interface PlyPropertyType {

    interface PlyScalarType extends PlyPropertyType {

        PcuDataType dataType();

    }

    interface PlyListType extends PlyPropertyType {

        PcuDataType sizeType();

        PcuDataType dataType();

    }

}
