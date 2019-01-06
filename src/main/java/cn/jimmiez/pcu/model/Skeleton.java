package cn.jimmiez.pcu.model;


import javax.vecmath.Point3d;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import cn.jimmiez.pcu.common.graph.EntityGraph;
import cn.jimmiez.pcu.common.graph.UndirectedGraph;
import cn.jimmiez.pcu.io.ply.PlyPropertyType;
import cn.jimmiez.pcu.io.ply.ReadFromPly;
import cn.jimmiez.pcu.io.ply.WriteListToPly;
import cn.jimmiez.pcu.io.ply.WriteScalarToPly;
import cn.jimmiez.pcu.util.Pair;

/**
 * Use a undirected acyclic graph for representing a curve skeleton
 */
public class Skeleton extends EntityGraph<Point3d> {

    public Skeleton() {
        super(false);
    }

//    @SuppressWarnings("unchecked")
//    @WriteScalarToPly(element = "nodes", properties = {"x", "y", "z"}, type = PlyPropertyType.DOUBLE)
//    @ReadFromPly(element = "nodes", properties = {"x", "y", "z"})
//    public List<double[]> nodes4Ply() {
//        List<Pair<Integer, Point3d>> nodes = new ArrayList<>();
//        for (int key : values.keySet()) {
//            nodes.add(new Pair<>(key, values.get(key)));
//        }
//        Collections.sort(nodes, new Comparator<Pair<Integer, Point3d>>() {
//            @Override
//            public int compare(Pair<Integer, Point3d> o1, Pair<Integer, Point3d> o2) {
//                return o1.getKey().compareTo(o2.getKey());
//            }
//        });
//
//        List<double[]> result = new ArrayList<>();
//        for (Point3d p : values.values()) {
//            result.add(new double[] {p.x, p.y, p.z});
//        }
//        return result;
//    }
//
//    @WriteListToPly(element = "edges", property = "node_index")
//    @ReadFromPly(element = "edges", properties = "node_index")
//    public List<int[]> edges4Ply() {
//        List<Pair<Integer, Point3d>> nodes = new ArrayList<>();
//        for (int key : values.keySet()) {
//            nodes.add(new Pair<>(key, values.get(key)));
//        }
//        Collections.sort(nodes, new Comparator<Pair<Integer, Point3d>>() {
//            @Override
//            public int compare(Pair<Integer, Point3d> o1, Pair<Integer, Point3d> o2) {
//                return o1.getKey().compareTo(o2.getKey());
//            }
//        });
//
//        List<int[]> result = new ArrayList<>();
//        for (int vi = 0; vi < values.size(); vi ++) {
//            Map<Integer, Double> edge = edges.get(vi);
//            int[] edgesVi = new int[edge.size()];
//            int cnt = 0;
//            for (int vj : edge.keySet()) {
//                edgesVi[cnt ++] = vj;
//            }
//            result.add(edgesVi);
//        }
//        return result;
//
//    }

}
