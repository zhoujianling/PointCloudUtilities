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

    public static void reverse(int[] array) {
        if (array.length < 1) return;
        int left = 0, right = array.length - 1;
        while (left < right) {
            int temp = array[left];
            array[left] = array[right];
            array[right] = temp;
            left += 1;
            right -= 1;
        }
    }

}
