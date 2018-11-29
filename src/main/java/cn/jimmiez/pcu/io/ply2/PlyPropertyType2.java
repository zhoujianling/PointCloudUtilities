package cn.jimmiez.pcu.io.ply2;

public interface PlyPropertyType2 {

    interface PlyScalarType extends PlyPropertyType2 {

        PcuDataType dataType();

    }

    interface PlyListType extends PlyPropertyType2 {

        PcuDataType sizeType();

        PcuDataType dataType();

    }

}
