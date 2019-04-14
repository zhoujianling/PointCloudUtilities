package cn.jimmiez.pcu.io.obj;

import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
        ObjReader.ObjData dataMap = reader.read(file);
        assertNotNull(dataMap);
//        assertTrue(dataMap.size() > 0);

    }
}
