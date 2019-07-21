package cn.jimmiez.pcu.util;

import java.io.Serializable;

/**
 * This class is a substitute for javafx.util.Pair coz OpenJFX is not included
 * in OpenJDK.
 * @param <K> type of key in a k-v pair
 * @param <V> type of value in a k-v pair
 */
public class Pair<K, V> implements Serializable{

    private K key;

    private V value;

    public Pair(K k, V v) {
        this.key = k;
        this.value = v;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K k) {
        this.key = k;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V v) {
        this.value = v;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;

        if (key != null ? !key.equals(pair.key) : pair.key != null) return false;
        return value != null ? value.equals(pair.value) : pair.value == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
