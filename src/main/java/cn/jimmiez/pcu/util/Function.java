package cn.jimmiez.pcu.util;

/**
 * the PCU is compatible with JDK 1.7, so Function in JDK 1.8 cannot be used
 */
public interface Function<T, R> {
    R apply(T param);
}
