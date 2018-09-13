package cn.jimmiez.pcu.util;

public class PcuArrayUtil {
    public static int find(String[] strs, String target) {
        for (int i = 0; i < strs.length; i ++){
            if (strs[i].equals(target)) {
                return i;
            }
        }
        return -1;
    }
}
