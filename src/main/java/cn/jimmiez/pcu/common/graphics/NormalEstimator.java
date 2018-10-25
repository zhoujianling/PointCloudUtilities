package cn.jimmiez.pcu.common.graphics;

import java.util.List;

public interface NormalEstimator {

    List<double[]> estimateNormal(List<double[]> data);
}
