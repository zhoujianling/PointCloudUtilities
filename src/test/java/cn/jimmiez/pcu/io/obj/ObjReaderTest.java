package cn.jimmiez.pcu.io.obj;

import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ObjReaderTest {

    @Test
    public void testObjDataType() {
        ObjDataType[] enumValues = ObjDataType.class.getEnumConstants();
        Set<String> keywords = new HashSet<>();
        // test if the keyword is unique
        for (ObjDataType type : enumValues) {
            assertNotNull(type.getKeyword());
            assertFalse(keywords.contains(type.getKeyword()));
            keywords.add(type.getKeyword());
        }
    }

    @Test
    public void testReadObjData() {
        ObjReader reader = new ObjReader();
        File file = new File(ObjReaderTest.class.getClassLoader().getResource("model/obj/bunny.obj").getFile());
        ObjData data = reader.read(file);
        assertNotNull(data);
        assertNotNull(data.vertices());
        assertNotNull(data.originalFaces());

        assertEquals(2503, data.vertices().size());
        assertEquals(4968, data.originalFaces().getValue().size());
        assertEquals(1, (int)data.originalFaces().getKey());
        assertNull(data.vertexTextures());
        assertNull(data.normals());
        assertNull(data.mtllib());

    }
}
