package cn.jimmiez.pcu.common.graphics;

import cn.jimmiez.pcu.DataUtil;
import org.junit.Test;

import static org.junit.Assert.*;

public class BoundingBoxTest {

    @Test
    public void ofTest() {
        BoundingBox box = BoundingBox.of(DataUtil.generateRandomData(500,1, 3, 5, 7, 9, 11));

        assertTrue(box.getCenter().x + box.getxExtent() <= 3 && box.getCenter().x - box.getxExtent() >= 1);
        assertTrue(box.getCenter().y + box.getyExtent() <= 7 && box.getCenter().y - box.getyExtent() >= 5);
        assertTrue(box.getCenter().z + box.getzExtent() <= 11 && box.getCenter().z - box.getzExtent() >= 9);


        box = BoundingBox.of(DataUtil.generateRandomData(0,1, 3, 6, 7, 4, 11));
        assertTrue(Double.isNaN(box.getCenter().x));
        assertTrue(Double.isNaN(box.getCenter().y));
        assertTrue(Double.isNaN(box.getCenter().z));
        assertTrue(Double.isNaN(box.getxExtent()));
        assertTrue(Double.isNaN(box.getyExtent()));
        assertTrue(Double.isNaN(box.getzExtent()));
    }

}
