package com.wdcloud.data.neo4j.entity;

/**
 * Created by bigd on 2017/6/26.
 * 检查结果
 * true 对象已经存在
 * false 对象可以被创建
 */
public class CheckResult {

    private boolean isExist;

    private String msg;

    private String code;

    public boolean isExist() {
        return isExist;
    }

    public void setExist(boolean exist) {
        isExist = exist;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
