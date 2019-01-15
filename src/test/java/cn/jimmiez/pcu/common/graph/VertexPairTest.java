package cn.jimmiez.pcu.common.graph;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class VertexPairTest {

    @Test
    public void testEquals() throws Exception {
        VertexPair vp = new VertexPair(0, 1);
        VertexPair vp2 = new VertexPair(1, 0);
        assertEquals(vp, vp2);
        assertEquals(vp2, vp);

        vp = new VertexPair(0, 1);
        vp2 = new VertexPair(0, 1);
        assertEquals(vp, vp2);
        assertEquals(vp2, vp);

        vp = new VertexPair(-1, Integer.MAX_VALUE);
        vp2 = new VertexPair(Integer.MAX_VALUE, -1);
        assertEquals(vp, vp2);
        assertEquals(vp2, vp);
        Set<VertexPair> vpSet = new HashSet<>();
        vpSet.add(vp);
        assertTrue(vpSet.contains(vp));
        assertTrue(vpSet.contains(vp2));

        vpSet.clear();
        vpSet.add(vp2);
        assertTrue(vpSet.contains(vp));

        vp = new VertexPair(-1, Integer.MAX_VALUE);
        vp2 = new VertexPair(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotEquals(vp, vp2);
        assertNotEquals(vp2, vp);

    }


}