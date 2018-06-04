package com.wdcloud.data.neo4j.code;

/**
 * Error 状态码标注
 * Created by bigd on 2017/6/20.
 */
public enum Code {

    CO_00("Code00","Create vertex failed, The service may not be available!"),
    CO_01(Assistant.SUCCESS,"Create vertex successfully!"),
    CO_02("Code02","Update vertex failed, The service may not be available!"),
    CO_03(Assistant.SUCCESS,"Update vertex successfully!"),
    CO_04("Code04","Query vertex failed, The service may not be available!"),
    CO_05(Assistant.SUCCESS,"Query vertex successfully!"),
    CO_06("Code06","Remove vertex failed, The service may not be available!"),
    CO_07(Assistant.SUCCESS,"Remove vertex successfully!"),
    CO_08("Code08","Query vertex out vertex failed, The service may not be available!"),
    CO_09(Assistant.SUCCESS,"Query vertex out vertex successfully!"),
    CO_10("Code10","Query vertex in vertex failed, The service may not be available!"),
    CO_11(Assistant.SUCCESS,"Query vertex in vertex successfully!"),
    CO_12("Code12","Query vertex both vertex failed, The service may not be available!"),
    CO_13(Assistant.SUCCESS,"Query vertex both vertex successfully!"),
    CO_14("Code14","Query vertex out vertex'ids failed, The service may not be available!"),
    CO_15(Assistant.SUCCESS,"Query vertex out vertex'ids successfully!"),
    CO_16("Code16","Query vertex in vertex'ids failed, The service may not be available!"),
    CO_17(Assistant.SUCCESS,"Query vertex in vertex'ids successfully!"),
    CO_18("Code18","Query vertex both vertex'ids failed, The service may not be available!"),
    CO_19(Assistant.SUCCESS,"Query vertex both vertex'ids successfully!"),
    CO_20(Assistant.SUCCESS,"Create edge successfully!"),
    CO_21("Code21","Create edge failed, The service may not be available!"),
    CO_22("Code06","Remove edge failed, The service may not be available!"),
    CO_23(Assistant.SUCCESS,"Remove edge successfully!"),
    // tianxy start
    CO_24("Code24","The query edge does not exist!"),
    CO_25(Assistant.SUCCESS,"Query edge successfully!"),
    CO_27(Assistant.SUCCESS,"Query edges between any two nodes successfully!"),
    CO_28("Code28","Vertex does not exist or no edges between vertices!"),
    CO_29(Assistant.SUCCESS,"Query edges id between any two nodes successfully!"),
    CO_30("Code30","The updated edges filed, The service may not be available!"),
    CO_31(Assistant.SUCCESS,"Update edge successfully!"),
    CO_32("Code32","Create edge failed, Vertices may not exists!"),
    CO_33(Assistant.SUCCESS, "Double edge success of query vertices"),
    CO_34(Assistant.SUCCESS, "In edge success of query vertices"),
    CO_35(Assistant.SUCCESS, "Out edge success of query vertices"),
    CO_36("Code36", "Vertex or edge does not exist"),
    // tianxy end


    CO_37(Assistant.SUCCESS,"Query same neighbors successfully!"),
    CO_38("Code38","Query vertex same neighbors failed, The service may not be available!"),
    CO_39("Code39","Vertex does not exist or the out vertex does not exist!"),
    CO_40("Code40","Vertex does not exist or the in vertex does not exist!"),
    CO_41("Code41","Vertex does not exist or no neighbors vertex!"),
    CO_42("Code42","Vertex does not exist or there isn't exist people you might know!"),
    /**
     * 旧接口状态码释义:
     */
    CO_PRE01(Assistant.SUCCESS,"Query successfully!"),

    CO_PREXC("EXCEPTION","Query caught an unexpected exception!"),
    CO_101(Assistant.SUCCESS,"query vertex successfully!"),
    CO_102(Assistant.SUCCESS,"Remove vertex successfully!"),
    EX_00("Error","Query caught an unexpected exception!"),
    NONE_VERTEX("UNEXIST","Vertex id={%s} unexist!"),
    QUERY_ERROR("QUERY_ERROR","Construct query error!"),
    JSON_CONVERT("JSON_CONVERT","Json convert caught an exception!");


    private String id;
    private String value;

    Code(String id, String value) {
        this.id=id;
        this.value = value;
    }

    public String getValue(){
        return this.value;
    }
    public String getId(){
        return this.id;
    }

}
class Assistant{

    private Assistant() {
    }

    public static final String SUCCESS = "SUCCESS";
}
