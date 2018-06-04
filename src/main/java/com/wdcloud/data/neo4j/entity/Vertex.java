package com.wdcloud.data.neo4j.entity;

import java.util.Map;

/**
 * Created by bigd on 2017/6/12.
 * 顶点对象
 */
public class Vertex{


    private String id;
    private String tenant;
    private Map<String,Object> properties;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
}
