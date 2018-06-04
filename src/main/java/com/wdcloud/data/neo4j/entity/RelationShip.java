package com.wdcloud.data.neo4j.entity;

/**
 * Created by bigd on 2017/8/25.
 * 俩定点之之间的关系对象
 */
public class RelationShip {

    private String sourceId;

    private String targetId;

    private Edge edge;

    public String getSourceId() {
        return sourceId;
    }

    public Edge getEdge() {
        return edge;
    }

    public void setEdge(Edge edge) {
        this.edge = edge;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

}
