package com.wdcloud.data.neo4j.entity;

/**
 * Created by bigd on 2017/6/28.
 * 顶点-顶点对象
 */
public class VertexV{

    /**
     * 开始顶点唯一标识
     */
    private  String sourceId;

    /**
     * 开始顶点及目标顶点租户值
     */
    private String tenant;

    /**
     * 关系属性
     */
    private Edge edge;

    /**
     * 目标顶点
     */
    private Vertex target;


    /**
     * 用户自定义返回从第几条返回
     */
    private  int skip;

    /**
     * 用户自定义返回多少条数据
     */
    private int limit;


    public Edge getEdge() {
        return edge;
    }

    public void setEdge(Edge edge) {
        this.edge = edge;
    }

    public Vertex getTarget() {
        return target;
    }

    public void setTarget(Vertex target) {
        this.target = target;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
}
