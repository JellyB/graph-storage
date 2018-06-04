package com.wdcloud.data.neo4j.entity;

/**
 * Created by bigd on 2017/6/16.
 * 新增关系数据绑定对象
 */
public class Path {


    private Vertex source;

    private Vertex target;

    private Edge edge;

    public Vertex getSource() {
        return source;
    }

    public void setSource(Vertex source) {
        this.source = source;
    }

    public Vertex getTarget() {
        return target;
    }

    public void setTarget(Vertex target) {
        this.target = target;
    }

    public Edge getEdge() {
        return edge;
    }

    public void setEdge(Edge edge) {
        this.edge = edge;
    }
}
