package com.wdcloud.data.neo4j.entity;

/**
 * Created by bigd on 2017/6/26.
 * vertex edge path 等数据检验结果
 */
public class ValidateResult {

    private boolean validate;

    private String code;

    private String msg;

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
