package com.wdcloud.data.neo4j.code;

/**
 * Created by bigd on 2017/6/26.
 * 数据对象校验码
 */
public enum Validate {

    VA00 ("VALIDATE00","Vertex property meet the requirements!"),
    VA01 ("VALIDATE01","Vertex property {id} can not be empty!"),
    VA02 ("VALIDATE02","Vertex property {id} format illegal!"),
    VA03 ("VALIDATE03","Vertex property {tenant} can not be empty!"),
    VA04 ("VALIDATE04","Vertex property {tenant} format illegal!"),
    VA05 ("VALIDATE05","Vertex property {%s} has been occupied by system!"),
    VA06 ("VALIDATE06","Source vertex can not be null!"),
    VA07 ("VALIDATE07","Target vertex can not be null!"),
    VA08 ("VALIDATE08","Source vertex's property {id} can not be empty!"),
    VA09 ("VALIDATE09","Source vertex's property {id} format illegal!"),
    VA10 ("VALIDATE10","Source vertex's property {tenant} can not be empty!"),
    VA11 ("VALIDATE11","Source vertex's property {tenant} format illegal!"),
    VA12 ("VALIDATE12","Target vertex's property {id} can not be empty!"),
    VA13 ("VALIDATE13","Target vertex's property {id} format illegal!"),
    VA14 ("VALIDATE14","Target vertex's property {tenant} can not be empty!"),
    VA15 ("VALIDATE15","Target vertex's property {tenant} format illegal!"),
    VA16 ("VALIDATE16","Edge can not be null!"),
    VA17 ("VALIDATE17","Edge property {id} can not be empty!"),
    VA18 ("VALIDATE18","Edge property {id} format illegal!"),
    VA19 ("VALIDATE19","Edge property {type} can not be empty!"),
    VA20 ("VALIDATE20","Edge property {type} format illegal!"),

    // tianxy start
    VA27 ("VALIDATE27","Edge property {tenant} can not be empty!"),
    VA28 ("VALIDATE28","Edge property {tenant} format illegal!"),
    VA29 ("VALIDATE29","Edge property {%s} has been occupied by system!"),
    // tianxy end

    VA30 ("VALIDATE30","Tenant length > {%s} characters!"),
    VA31 ("VALIDATE31","Vertex property {id} length > {%s} characters!"),
    VA32 ("VALIDATE32","Source vertex's property {id} length > {%s} characters!"),
    VA33 ("VALIDATE33","Target vertex id length > {%s} characters!"),
    VA34 ("VALIDATE34","Edge id length overflow!"),
    VA35 ("VALIDATE35","Edge type length > {%s} characters!"),
    VA36 ("VALIDATE36","Vertex property key:{%s} length > {%s} characters!"),
    VA37 ("VALIDATE37","Edge property key:{%s} length > {%s} characters!"),
    VA38 ("VALIDATE38","Property key:{%s}'s value length > {%s} characters!"),
    VA39 ("VALIDATE39","Vertex property key:{%s} format illegal!"),
    VA40 ("VALIDATE40","Edge property key:{%s} format illegal!"),
    VA41 ("VALIDATE41","Property key:{%s}'s value > Long.MAX_VALUE!"),
    VA42 ("VALIDATE42","Property key:{%s}'s value < Long.MIN_VALUE!"),
    VERTEXVEX ("VertexVException","Validate vertexV caught an exception!");
    private String id;

    private String value;

    Validate(String id, String value) {
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
