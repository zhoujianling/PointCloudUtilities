package cn.jimmiez.pcu.util;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * a util for fetching fields of user-defined class
 */
public class PcuReflectUtil {

    public static List<Field> fetchAllFields(Object object) {
        List<Field> fieldList = new ArrayList<>() ;
        Class tempClass = object.getClass();
        while (tempClass != null) {
            fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            tempClass = tempClass.getSuperclass();
        }
        return fieldList;
    }

    public static List<Method> fetchAllMethods(Object object) {
        List<Method> methodList = new ArrayList<>() ;
        Class tempClass = object.getClass();
        while (tempClass != null) {
            methodList.addAll(Arrays.asList(tempClass.getDeclaredMethods()));
            tempClass = tempClass.getSuperclass();
        }
        return methodList;
    }

}
