package cn.jimmiez.pcu.io.ply;

import cn.jimmiez.pcu.Constants;

import java.io.File;
import java.util.List;

public class PlyWriter {

    public int write(Object object, File file) {
        int result = Constants.ERR_CODE_NO_ERROR;

        return result;
    }

    public PlyWriterRequest prepare() {
        return new PlyWriterRequest();
    }

    private class PlyWriterRequest {

        File file = null;

        public PlyWriterRequest defineElement(String elementName) {
            return this;
        }

        public PlyWriterRequest defineScalarProperty(String propertyName, int valType) {
            return this;
        }

        public PlyWriterRequest defineScalarProperty(String propertyName, int sizeType, int valType) {
            return this;
        }

        public PlyWriterRequest putData(List data) {
            return this;
        }

        public PlyWriterRequest writeTo(File file) {
            return this;
        }

        public int okay() {
            //todo
            return 0;
        }
    }
}
