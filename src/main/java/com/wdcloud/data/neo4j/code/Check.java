package com.wdcloud.data.neo4j.code;

/**
 * Created by bigd on 2017/6/26.
 * 对象校验状态码
 */
public enum Check {

    CH_01("Check01","Vertex does not exist!"),
    CH_02("Check02","Vertex existed already!"),
    CH_03("Check03","Edge does't exist!"),
    CH_04("Check04","Edge has existed already!");

    private String id;

    private String value;

    Check(String id, String value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

}
