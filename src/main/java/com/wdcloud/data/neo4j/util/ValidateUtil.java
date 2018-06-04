package com.wdcloud.data.neo4j.util;


import com.wdcloud.data.neo4j.code.Method;
import com.wdcloud.data.neo4j.code.Validate;
import com.wdcloud.data.neo4j.code.WhichVertex;
import com.wdcloud.data.neo4j.entity.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bigd on 2017/6/29.
 * 顶点、边、path 数据格式校验
 */
@Service(value = "validateUtil")
public class ValidateUtil {

    public static final  String CREATE_TIME = "createTime";

    public static final  String LAST_UPDATE_TIME = "updateTime";

    @Value("${graph.node.id.length}")
    private  String nodeId;

    @Value("${graph.edge.id.length}")
    private String edgeId;

    @Value("${graph.id.format}")
    private String idFormat;

    @Value("${graph.edge.type.length}")
    private String edgeType;

    @Value("${graph.tenant.length}")
    private String tenant;

    @Value("${graph.key.format}")
    private String keyFormat;

    @Value("${graph.key.length}")
    private String keyLength;

    @Value("${graph.value.length}")
    private String valueLength;

    @Value("${graph.tenant.format}")
    private String tenantFormat;

    @Value("${graph.data.max.limit}")
    private String maxLimit;

    @Value("${graph.data.default.limit}")
    private String defaultLimit;

    @Value("${graph.data.default.skip}")
    private String defaultSkip;



    static final ArrayList<String> SYSTEM_RESERVED = new ArrayList<String>(
            Arrays.asList("id","namespace","createTime","updateTime"));

    /**
     * 返回系统graph数据返回条数限定
     * @param limit 用户传递的limit值
     * @return 比较后返回的limit值
     */
    public int graphDataLimit(int limit){
        int max = Integer.parseInt(maxLimit);
        int def = Integer.parseInt(defaultLimit);
        if(limit <= 0){
            return def;
        }
        if(limit >= max){
            return max;
        }else{
            return limit;
        }
    }
    
    /**
     * 返回limit的默认值
     * @return
     */
    public int getDefaultLimit(){
    	int defLimit = Integer.parseInt(defaultLimit);
    	return defLimit;
    }

    /**
     * 返回系统graph数据返回数据的起始值
     * @param skip 用户传递的skip值
     * @return 比较后返回的skip值
     */
    public int graphDataSkip(int skip){
        int def = Integer.parseInt(defaultSkip);
        if(skip <= 0){
            return def;
        }else{
            return skip;
        }
    }
    
    /**
     * 返回skip的默认值
     * @return
     */
    public int getDefaultSkip(){
    	int defSkip = Integer.parseInt(defaultSkip);
    	return defSkip;
    }

    /**
     * 新增修改顶点数据格式校验
     * @param vertex 顶点对象
     * @return 顶点数据格式校验结果，如果数据格式符合指定要求：true，其余均返回false
     */
    public  ValidateResult validateVertex(Vertex vertex,Method method){
        ValidateResult  validateResult = new ValidateResult();
        /**
         * Vertex id 校验
         */

        validateVertexId(validateResult,vertex,method);
        if(!validateResult.isValidate())
            return validateResult;

        /**
         * Vertex tenant 校验
         */
        validateVertexTenant(validateResult,vertex,method);
        if(!validateResult.isValidate())
            return validateResult;

        /**
         * properties 校验
         */
        if(method.equals(Method.CREATE) || method.equals(Method.UPDATE)){
            validateVertexProperties(validateResult,vertex);
            if(!validateResult.isValidate())
                return validateResult;
        }

        validateResult.setValidate(true);
        validateResult.setCode(Validate.VA00.getId());
        validateResult.setMsg(Validate.VA00.getValue());
        return validateResult;
    }


    /**
     * 获取指定顶点出向、入向、双向、顶点方法 Obtain
     * @param vertexV source 顶点 & 关系 & target 顶点属性对象
     * @return 校验结果
     */
    public  ValidateResult validateVertexV(VertexV vertexV,Method method){
        ValidateResult  validateResult = new ValidateResult();
        try{
            /**
             * vertexV sourceId 非空校验
             */
            if(StringUtils.isEmpty(vertexV.getSourceId())){
                validateResult.setValidate(false);
                validateResult.setCode(Validate.VA01.getId());
                validateResult.setMsg(Validate.VA01.getValue());
                return validateResult;
            }

            /**
             * vertexV tenant 非空 校验
             */
            if(StringUtils.isEmpty(vertexV.getTenant())){
                validateResult.setValidate(false);
                validateResult.setCode(Validate.VA10.getId());
                validateResult.setMsg(Validate.VA10.getValue());
                return validateResult;
            }
        }catch (Exception e){
            validateResult.setValidate(false);
            validateResult.setCode(Validate.VERTEXVEX.getId());
            validateResult.setMsg(Validate.VERTEXVEX.getValue());
            return validateResult;
        }
        validateResult.setValidate(true);
        return validateResult;
    }

    /**
     * 边属性校验【开始顶点】、【结束顶点】、【关系对象】
     * @param path 边对象
     * @return 校验结果
     */
    public  ValidateResult validatePath(Path path,Method method){
        ValidateResult validateResult = new ValidateResult();
        /**
         * 开始顶点非空校验
         */
        if(null == path.getSource()){
            validateResult.setValidate(false);
            validateResult.setCode(Validate.VA06.getId());
            validateResult.setMsg(Validate.VA06.getValue());
            return validateResult;
        }

        /**
         * 指向顶点非空校验
         */
        if(null == path.getTarget()){
            validateResult.setValidate(false);
            validateResult.setCode(Validate.VA07.getId());
            validateResult.setMsg(Validate.VA07.getValue());
            return validateResult;
        }

        /**
         * 关系对象数据校验
         */
        if(null == path.getEdge()){
            validateResult.setValidate(false);
            validateResult.setCode(Validate.VA16.getId());
            validateResult.setMsg(Validate.VA16.getValue());
            return validateResult;
        }

        /**
         * 开始顶点 id 数据校验
         */
        validateVertexId(validateResult,path.getSource().getId() == null ? "" : path.getSource().getId(),WhichVertex.SOURCE);
        if(!validateResult.isValidate())
            return validateResult;

        /**
         * 目标顶点 id 数据校验
         */
        validateVertexId(validateResult,path.getTarget().getId() == null ? "":path.getTarget().getId(),WhichVertex.TARGET);
        if(!validateResult.isValidate())
            return validateResult;

        /**
         * 开始顶点租户 tenant 数据校验
         */
        validateVertexTenant(validateResult,path.getSource().getTenant() == null ? "": path.getSource().getTenant(),WhichVertex.SOURCE);
        if(!validateResult.isValidate())
            return validateResult;

        /**
         * 目标顶点租户 tenant 数据校验
         */
        validateVertexTenant(validateResult,path.getTarget().getTenant() == null ? "" : path.getTarget().getTenant(),WhichVertex.TARGET);
        if(!validateResult.isValidate())
            return validateResult;

        /**
         * 边租户 tenant 数据校验
         */
        validateEdgeTenant(validateResult,path.getEdge().getTenant() == null ? "": path.getEdge().getTenant());
        if(!validateResult.isValidate())
            return validateResult;

        validateEdgeType(validateResult,path.getEdge().getType() == null ? "": path.getEdge().getType());
        if(!validateResult.isValidate())
            return validateResult;

        validateEdgeProperties(validateResult,path.getEdge().getProperties(), method);
        if(!validateResult.isValidate()){
            return validateResult;
        }

        validateResult.setValidate(true);
        return validateResult;
    }


    public ValidateResult validateRelationShip(RelationShip relationShip, Method method){

        ValidateResult validateResult = new ValidateResult();
        /**
         * 源顶点 id 数据校验
         */
        validateVertexId(validateResult,relationShip.getSourceId() == null ? "" : relationShip.getSourceId(), WhichVertex.SOURCE);
        if(!validateResult.isValidate())
            return validateResult;
        /**
         * 目标顶点id 数据校验
         */
        validateVertexId(validateResult,relationShip.getTargetId()== null ? "": relationShip.getTargetId(), WhichVertex.TARGET);
        if(!validateResult.isValidate())
            return validateResult;
        /**
         * 关系租户数据校验
         */
        validateEdgeTenant(validateResult,relationShip.getEdge().getTenant() == null ? "": relationShip.getEdge().getTenant());
        if(!validateResult.isValidate())
            return validateResult;
        /**
         * 关系type 数据校验
         */
        validateEdgeType(validateResult,relationShip.getEdge().getType() == null ? "" : relationShip.getEdge().getType());
        if(!validateResult.isValidate())
            return validateResult;
        /**
         * 关系属性校验
         */
        validateEdgeProperties(validateResult,relationShip.getEdge().getProperties(), method);
        if(!validateResult.isValidate())
            return validateResult;
        return validateResult;
    }


    /**
     * Vertex id 长度非空及格式校验
     * @param validateResult 校验对象
     * @param vertex vertex对象
     * @return 校验结果
     */
    public ValidateResult validateVertexId(ValidateResult validateResult,Vertex vertex,Method method){
        /**
         * vertex id 非空校验
         */
        if(StringUtils.isEmpty(vertex.getId())){
            validateResult.setValidate(false);
            validateResult.setCode(Validate.VA01.getId());
            validateResult.setMsg(Validate.VA01.getValue());
            return validateResult;
        }
        /**
         * vertex id 长度校验
         */
        if(method.equals(Method.CREATE) && (vertex.getId().length() > Integer.valueOf(nodeId))){
            validateResult.setValidate(false);
            validateResult.setCode(Validate.VA31.getId());
            validateResult.setMsg(String.format(Validate.VA31.getValue(),Integer.valueOf(nodeId)));
            return validateResult;
        }

        validateResult.setValidate(true);
        return validateResult;
    }

    /**
     * tenant 租户格式及长度校验
     * @param validateResult 校验对象
     * @param vertex 顶点对象
     * @return 校验对象
     */
    public ValidateResult validateVertexTenant(ValidateResult validateResult, Vertex vertex,Method method){
        /**
         * tenant 非空校验
         */
        if(StringUtils.isEmpty(vertex.getTenant())){
            validateResult.setValidate(false);
            validateResult.setCode(Validate.VA03.getId());
            validateResult.setMsg(Validate.VA03.getValue());
            return validateResult;
        }

        /**
         * tenant 长度校验
         */
        if(method.equals(Method.CREATE) && (vertex.getTenant().length() > Integer.valueOf(tenant))){
            validateResult.setValidate(false);
            validateResult.setCode(Validate.VA30.getId());
            validateResult.setMsg(String.format(Validate.VA30.getValue(),Integer.valueOf(nodeId)));
            return validateResult;
        }


        /**
         * tenant 格式校验
         */
        Pattern pattern = Pattern.compile(tenantFormat);
        Matcher matcher = pattern.matcher(vertex.getTenant());
        if(!matcher.find()){
            validateResult.setValidate(false);
            validateResult.setCode(Validate.VA04.getId());
            validateResult.setMsg(Validate.VA04.getValue());
            return validateResult;
        }

        validateResult.setValidate(true);
        return validateResult;
    }

    /**
     * vertex properties 属性校验
     * @param validateResult 校验对象
     * @param vertex 顶点对象
     * @return 校验结果
     */
    public ValidateResult validateVertexProperties(ValidateResult validateResult, Vertex vertex){
        if(null != vertex.getProperties() && vertex.getProperties().size() > 0){
            for(Map.Entry mapEntry : vertex.getProperties().entrySet()){
                String key = (String)mapEntry.getKey();
                Object value = mapEntry.getValue();

                /**
                 * vertex key 保留校验
                 */
                if(SYSTEM_RESERVED.contains(key)){
                    validateResult.setValidate(false);
                    validateResult.setCode(Validate.VA05.getId());
                    String msg = String.format(Validate.VA05.getValue(),key);
                    validateResult.setMsg(msg);
                    return validateResult;
                }

                /**
                 * vertex key 长度校验
                 */

                if(key.length() > Integer.valueOf(keyLength)){
                    validateResult.setValidate(false);
                    validateResult.setCode(Validate.VA36.getId());
                    validateResult.setMsg(String.format(Validate.VA36.getValue(),key,keyLength));
                    return validateResult;
                }

                /**
                 * vertex key 格式校验
                 */
                Pattern pattern = Pattern.compile(keyFormat);
                Matcher matcher = pattern.matcher(key);
                if(!matcher.find()){
                    validateResult.setValidate(false);
                    validateResult.setCode(Validate.VA39.getId());
                    validateResult.setMsg(Validate.VA39.getValue());
                    return validateResult;
                }
                this.validateValue(validateResult,key,value);
                if(!validateResult.isValidate()){
                    return validateResult;
                }
            }
        }

        validateResult.setValidate(true);
        return validateResult;
    }

    /**
     * 顶点id 校验
     * @param validateResult 校验结果对象
     * @param id 顶点id，
     * @param whichVertex 开始顶点或结束顶点
     * @return 校验结果
     */
    public ValidateResult validateVertexId(ValidateResult validateResult, String id, WhichVertex whichVertex ){

        /**
         * source id 非空校验
         */
        if(StringUtils.isEmpty(id)){
            validateResult.setValidate(false);
            if(whichVertex.equals(WhichVertex.SOURCE)){
                validateResult.setCode(Validate.VA08.getId());
                validateResult.setMsg(Validate.VA08.getValue());
            }
            if(whichVertex.equals(WhichVertex.TARGET)){
                validateResult.setCode(Validate.VA12.getId());
                validateResult.setMsg(Validate.VA12.getValue());
            }
            return validateResult;
        }

        /**
         * source id 长度校验
         */
        if(id.length() > Integer.valueOf(nodeId)){
            validateResult.setValidate(false);
            if(whichVertex.equals(WhichVertex.SOURCE)){
                validateResult.setCode(Validate.VA32.getId());
                validateResult.setMsg(String.format(Validate.VA32.getValue(),Integer.valueOf(nodeId)));
            }
            if(whichVertex.equals(WhichVertex.TARGET)){
                validateResult.setCode(Validate.VA33.getId());
                validateResult.setMsg(String.format(Validate.VA33.getValue(),Integer.valueOf(nodeId)));
            }
            return validateResult;
        }
        validateResult.setValidate(true);
        return validateResult;
    }

    /**
     * 顶点 tenant 数据校验
     * @param validateResult 校验结果对象
     * @param apptenant 顶点租户
     * @param whichVertex 源、目标顶点
     * @return 校验结果
     */
    public ValidateResult validateVertexTenant(ValidateResult validateResult, String apptenant,WhichVertex whichVertex) {
        /**
         * tenant 非空校验
         */
        if(StringUtils.isEmpty(apptenant)){
            validateResult.setValidate(false);
            if(whichVertex.equals(WhichVertex.SOURCE)){
                validateResult.setCode(Validate.VA10.getId());
                validateResult.setMsg(Validate.VA10.getValue());
            }
            if(whichVertex.equals(WhichVertex.TARGET)){
                validateResult.setCode(Validate.VA14.getId());
                validateResult.setMsg(Validate.VA14.getValue());
            }
            return validateResult;
        }

        /**
         * 顶点 tenant 长度校验
         */
        if(apptenant.length() > Integer.valueOf(tenant)){
            validateResult.setValidate(false);
            if(whichVertex.equals(WhichVertex.SOURCE)){
                validateResult.setCode(Validate.VA30.getId());
                validateResult.setMsg(String.format(Validate.VA30.getValue(),Integer.valueOf(tenant)));
            }
            if(whichVertex.equals(WhichVertex.TARGET)){
                validateResult.setCode(Validate.VA30.getId());
                validateResult.setMsg(String.format(Validate.VA30.getValue(),Integer.valueOf(tenant)));
            }
            return validateResult;
        }

        /**
         * 顶点 tenant 格式校验
         */
        Pattern pattern = Pattern.compile(tenantFormat);
        Matcher matcher = pattern.matcher(apptenant);
        if(!matcher.find()){
            validateResult.setValidate(false);
            if(whichVertex.equals(WhichVertex.SOURCE)){
                validateResult.setCode(Validate.VA11.getId());
                validateResult.setMsg(Validate.VA11.getValue());
            }
            if(whichVertex.equals(WhichVertex.TARGET)){
                validateResult.setCode(Validate.VA15.getId());
                validateResult.setMsg(Validate.VA15.getValue());
            }
            return validateResult;
        }

        validateResult.setValidate(true);
        return validateResult;
    }

    /**
     * 边 tenant 数据校验
     * @param validateResult 校验结果对象
     * @param apptenant 边对象
     * @return 校验结果
     */
    public ValidateResult validateEdgeTenant(ValidateResult validateResult, String apptenant) {

        /**
         * tenant 非空校验
         */
        if(StringUtils.isEmpty(apptenant)){
            validateResult.setValidate(false);
            validateResult.setCode(Validate.VA27.getId());
            validateResult.setMsg(Validate.VA27.getValue());
            return validateResult;
        }

        /**
         * tenant 长度校验
         */
        if(apptenant.length() > Integer.valueOf(tenant)){
            validateResult.setValidate(false);
            validateResult.setCode(Validate.VA30.getId());
            validateResult.setMsg(String.format(Validate.VA30.getValue(),Integer.valueOf(nodeId)));
            return validateResult;
        }

        /**
         * tenant 格式校验
         */
        Pattern pattern = Pattern.compile(tenantFormat);
        Matcher matcher = pattern.matcher(apptenant);
        if(!matcher.find()){
            validateResult.setValidate(false);
            validateResult.setCode(Validate.VA28.getId());
            validateResult.setMsg(Validate.VA28.getValue());
            return validateResult;
        }

        validateResult.setValidate(true);
        return validateResult;
    }

    /**
     * edge type 数据校验
     * @param validateResult 数据校验对象
     * @param type 关系类型
     * @return 数据校验结果
     */
    public ValidateResult validateEdgeType(ValidateResult validateResult, String type){
        /**
         * edge type 非空校验
         */
        if(StringUtils.isEmpty(type)){
            validateResult.setValidate(false);
            validateResult.setCode(Validate.VA19.getId());
            validateResult.setMsg(Validate.VA19.getValue());
            return validateResult;
        }

        /**
         * edge type 长度校验
         */
        if(type.length() > Integer.valueOf(edgeType)){
            validateResult.setValidate(false);
            validateResult.setCode(Validate.VA35.getId());
            validateResult.setMsg(String.format(Validate.VA35.getValue(),Integer.valueOf(edgeType)));
            return validateResult;
        }

        /**
         * type 格式校验
         */
        Pattern pattern = Pattern.compile(idFormat);
        Matcher matcher = pattern.matcher(type);
        if(!matcher.find()){
            validateResult.setValidate(false);
            validateResult.setCode(Validate.VA20.getId());
            validateResult.setMsg(Validate.VA20.getValue());
            return validateResult;
        }

        validateResult.setValidate(true);
        return validateResult;
    }

    /**
     * edge properties 属性校验
     * @param validateResult 校验对象
     * @param properties 关系属性
     * @return 校验结果
     */
    public ValidateResult validateEdgeProperties(ValidateResult validateResult, Map<String,Object> properties, Method method){
        if(null != properties && properties.size() > 0){
            for(Map.Entry mapEntry : properties.entrySet()){
                String key = (String)mapEntry.getKey();
                Object value = mapEntry.getValue();

                /**
                 * vertex key 保留校验
                 */
                if((method.equals(Method.CREATE) || method.equals(Method.UPDATE)) && SYSTEM_RESERVED.contains(key)){
                    validateResult.setValidate(false);
                    validateResult.setCode(Validate.VA29.getId());
                    String msg = String.format(Validate.VA29.getValue(),key);
                    validateResult.setMsg(msg);
                    return validateResult;
                }

                /**
                 * edge key 长度校验
                 */
                if(key.length() > Integer.valueOf(keyLength)){
                    validateResult.setValidate(false);
                    validateResult.setCode(Validate.VA37.getId());
                    validateResult.setMsg(String.format(Validate.VA37.getValue(),key,keyLength));
                    return validateResult;
                }

                /**
                 * edge key 格式校验
                 */
                Pattern pattern = Pattern.compile(keyFormat);
                Matcher matcher = pattern.matcher(key);
                if(!matcher.find()){
                    validateResult.setValidate(false);
                    validateResult.setCode(Validate.VA40.getId());
                    validateResult.setMsg(Validate.VA40.getValue());
                    return validateResult;
                }


                /**
                 * edge value 长度校验
                 */
                this.validateValue(validateResult,key,value);
                if(!validateResult.isValidate()){
                    return validateResult;
                }
            }
        }

        validateResult.setValidate(true);
        return validateResult;
    }

    /**
     * 查询边数据校验
     * @param edge 边对象
     * @return 如果数据格式符合指定要求：true，其余均返回false
     */
	public  ValidateResult validateEdge(Edge edge,Method method) {
		ValidateResult  validateResult = new ValidateResult();
        /**
         * tenant 数据校验
         */
        validateEdgeTenant(validateResult,edge.getTenant() == null ? "" : edge.getTenant());
        if(!validateResult.isValidate())
            return validateResult;

        /**
         * type 数据校验
         */
        if(method.equals(Method.CREATE)){
            validateEdgeType(validateResult,edge.getType() == null ? "":edge.getType());
            if(!validateResult.isValidate())
                return validateResult;
        }

        /**
         * properties 数据校验
         */
        validateEdgeProperties(validateResult,edge.getProperties(), method);
        if(!validateResult.isValidate())
            return validateResult;

        validateResult.setValidate(true);
        return validateResult;
	}

    /**
     * value 长度校验
     * @param validateResult 校验结果
     * @param value value
     * @return 校验结果
     */
    public ValidateResult validateValue(ValidateResult validateResult,String key, Object value){
        if(value instanceof  Long || value instanceof Integer || value instanceof BigInteger){
            String valueStr = String.valueOf(value);
            BigInteger bigInteger =  new BigInteger(valueStr);
            BigInteger maxBigInteger = BigInteger.valueOf(Long.MAX_VALUE);
            BigInteger minBigInteger = BigInteger.valueOf(Long.MIN_VALUE);
            if(bigInteger.compareTo(maxBigInteger) > 0){
                validateResult.setValidate(false);
                validateResult.setCode(Validate.VA41.getId());
                validateResult.setMsg(String.format(Validate.VA41.getValue(),key));
                return validateResult;
            }
            if(bigInteger.compareTo(minBigInteger) < 0){
                validateResult.setValidate(false);
                validateResult.setCode(Validate.VA42.getId());
                validateResult.setMsg(String.format(Validate.VA42.getValue(),key));
                return validateResult;
            }
        }
        if(value instanceof String){
            String valueStr = String.valueOf(value);
            if(valueStr.length() > Integer.valueOf(valueLength)){
                validateResult.setValidate(false);
                validateResult.setCode(Validate.VA38.getId());
                validateResult.setMsg(String.format(Validate.VA38.getValue(),key,valueLength));
                return validateResult;
            }
        }
        return validateResult;
    }
}
