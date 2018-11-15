package cn.jimmiez.pcu.model;

/**
 * the PCU is compatible with JDK 1.7, so Function in JDK 1.8 cannot be used
 */
public interface PcuFunction<T, R> {
    R apply(T param);
}
