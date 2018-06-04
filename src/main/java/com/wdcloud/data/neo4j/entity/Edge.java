package com.wdcloud.data.neo4j.entity;

import java.util.Map;

/**
 * Created by bigd on 2017/6/13.
 */
public class Edge {


    private Long id;

    private String type;

    private String tenant;

    private Map<String,Object> properties;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
