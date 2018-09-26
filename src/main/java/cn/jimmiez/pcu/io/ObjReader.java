package cn.jimmiez.pcu.io;

import java.io.File;

public class ObjReader {

    public <T> T read(File file, Class<T> clazz) {
        T object = null;
        try {
            object = clazz.newInstance();
            // TODO read from obj file
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return object;
    }
}
