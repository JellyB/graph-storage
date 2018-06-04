package com.wdcloud.data.neo4j.entity;

import java.util.Map;

/**
 * Created by bigd on 2017/7/18.
 */
public class Target {

    private Map<String,Object> properties;

    private Map<String,String> proptype;

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Map<String, String> getProptype() {
        return proptype;
    }

    public void setProptype(Map<String, String> proptype) {
        this.proptype = proptype;
    }
}
