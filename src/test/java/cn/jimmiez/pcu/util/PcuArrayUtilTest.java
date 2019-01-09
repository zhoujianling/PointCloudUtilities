package cn.jimmiez.pcu.util;

import org.junit.Test;

import static cn.jimmiez.pcu.util.PcuArrayUtil.*;
import static org.junit.Assert.*;

public class PcuArrayUtilTest {

    @Test
    public void testReverse() {
        int[] a1 = new int[] {};
        reverse(a1);
        assertEquals(0, a1.length);

        int[] a2 = new int[] {1, 2};
        reverse(a2);
        assertEquals(2, a2[0]);
        assertEquals(1, a2[1]);

        int[] a3 = new int[] {0};
        reverse(a3);
        assertEquals(0, a3[0]);

        int[] a4 = new int[] {0, 1, -4, Integer.MAX_VALUE};
        reverse(a4);
        assertEquals(Integer.MAX_VALUE, a4[0]);
        assertEquals(-4, a4[1]);
        assertEquals(1, a4[2]);
        assertEquals(0, a4[3]);
    }
}
