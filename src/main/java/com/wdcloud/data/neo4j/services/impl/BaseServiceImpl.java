package com.wdcloud.data.neo4j.services.impl;


import com.wdcloud.data.neo4j.code.Check;
import com.wdcloud.data.neo4j.code.Code;
import com.wdcloud.data.neo4j.code.Direction;
import com.wdcloud.data.neo4j.code.Method;
import com.wdcloud.data.neo4j.entity.*;
import com.wdcloud.data.neo4j.services.BaseService;
import com.wdcloud.data.neo4j.util.Util;
import com.wdcloud.data.neo4j.util.ValidateUtil;
import com.wdcloud.exception.CommonException;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.api.pattern.Relation;
import iot.jcypher.query.api.predicate.Concatenator;
import iot.jcypher.query.factories.clause.*;
import iot.jcypher.query.values.*;
import iot.jcypher.query.writer.Format;
import org.apache.commons.collections4.map.HashedMap;
import org.neo4j.ogm.model.QueryStatistics;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by bigd on 2017/6/26.
 * 图存储服务接口api 实现类
 */
@Service(value = "baseServiceImpl")
public class BaseServiceImpl implements BaseService {


    @Autowired
    private Session session;

    @Autowired
    private ValidateUtil validateUtil;

    static final String ID = "id";

    static final  String TOTAL = "total";

    static final  String EDGES = "edges";
    
    static final String EDGEIDS = "edgeIds";

    static final String ICLAUSE = "iClause";

    static final String NODES = "vertices";

    static final String NODE_IDS = "vertexIds";

    static final String CREATE_TIME = "createTime";

    static final  String COUNT = "count";

    static final String JCNUMBER = "jcNumber";

    static final String JCPATH = "jcPath";

    static final String JCNODE = "jcNode";

    static final String CYPHER_PLANNER = "CYPHER planner=rule ";

    /**
     * 关系属性用来存放用户传来的header 租户值
     */
    static final String TENANT = "namespace";

    private final Logger logger = LoggerFactory.getLogger("BaseServiceImpl");


    /**
     * 添加顶点
     * @param vertex 顶点
     * @return 执行结果
     */
    @Override
    public QueryResult createVertex(Vertex vertex) {
        JcQuery jcQuery = new JcQuery();
        JcNode jcNode = new JcNode(JCNODE);
        QueryResult queryResult = new QueryResult();
        queryResult.setData(new HashMap<String,Object>());
        try{
        ValidateResult validateResult = validateUtil.validateVertex(vertex, Method.CREATE);
            /**
             * 如果顶点数据格式校验有问题
             */
            if(!validateResult.isValidate()){
                queryResult.setCode(validateResult.getCode());
                queryResult.setMessage(validateResult.getMsg());
                return  queryResult;
            }
            CheckResult checkResult = checkVertexExist(vertex);
            /**
             * 如果顶点存在的情况下
             */
            if(checkResult.isExist()){
                queryResult.setCode(checkResult.getCode());
                queryResult.setMessage(checkResult.getMsg());
                return queryResult;
            }

            List<IClause> list = new ArrayList<IClause>();
            list.add(CREATE.node(jcNode).label(vertex.getTenant()));
            list.add(DO.SET(jcNode.property(ID)).to(vertex.getId()));
            list.add(DO.SET(jcNode.property(ValidateUtil.CREATE_TIME)).to(System.currentTimeMillis()));
            list.add(DO.SET(jcNode.property(ValidateUtil.LAST_UPDATE_TIME)).to(System.currentTimeMillis()));
            if(null != vertex.getProperties() && vertex.getProperties().size() > 0){
                for(Map.Entry entry : vertex.getProperties().entrySet()){
                    String key = (String)entry.getKey();
                    Object value = entry.getValue();
                    list.add(DO.SET(jcNode.property(key)).to(value));
                }
            }
            list.add(RETURN.value(jcNode));
            IClause[] iClauses = list.toArray(new IClause[list.size()]);
            jcQuery.setClauses(iClauses);

            String cypher = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
            cypher = cypher.replace(CYPHER_PLANNER,"");
            logger.info(cypher);
            Result result = session.query(cypher,new HashMap<String,Object>());
            QueryStatistics queryStatistics = result.queryStatistics();
            if(queryStatistics.getNodesCreated() > 0){
                queryResult.setCode(Code.CO_01.getId());
                queryResult.setMessage(Code.CO_01.getValue());
                return queryResult;
            }else{
                throw new CommonException(Code.CO_00.getValue());
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new CommonException(e.getMessage());
        }
    }

    /**
     * 更新顶点
     * @param vertex 顶点对象
     * @return 执行结果
     */
    @Override
    public QueryResult updateVertex(Vertex vertex) {
        QueryResult queryResult = new QueryResult();
        queryResult.setData(new HashMap<String,Object>());
        JcQuery jcQuery = new JcQuery();
        JcNode jcNode = new JcNode(JCNODE);
        try{
            ValidateResult validateResult = validateUtil.validateVertex(vertex,Method.UPDATE);
            /**
             * 如果顶点数据格式校验有问题
             */
            if(!validateResult.isValidate()){
                queryResult.setCode(validateResult.getCode());
                queryResult.setMessage(validateResult.getMsg());
                return  queryResult;
            }
            List<IClause> list = new ArrayList<IClause>();
            list.add(MATCH.node(jcNode).label(vertex.getTenant()));
            list.add(WHERE.valueOf(jcNode.property(ID)).EQUALS(vertex.getId()));
            list.add(DO.SET(jcNode.property(ValidateUtil.LAST_UPDATE_TIME)).to(System.currentTimeMillis()));
            if(null != vertex.getProperties() && vertex.getProperties().size() > 0){
                for(Map.Entry entry : vertex.getProperties().entrySet()){
                    Object key =  entry.getKey();
                    String keyStr = key.toString();
                    Object value = entry.getValue();
                    list.add(DO.SET(jcNode.property(keyStr)).to(value));
                }
            }
            list.add(RETURN.value(jcNode));
            IClause [] iClauses = list.toArray(new IClause[list.size()]);
            jcQuery.setClauses(iClauses);
            String cypher = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
            cypher = cypher.replace(CYPHER_PLANNER,"");
            logger.info(cypher);
            Result result = session.query(cypher,new HashMap<String,Object>());
            QueryStatistics queryStatistics = result.queryStatistics();
            if(queryStatistics.containsUpdates()){
                queryResult.setCode(Code.CO_03.getId());
                queryResult.setMessage(Code.CO_03.getValue());
                return queryResult;
            }else{
                queryResult.setCode(Check.CH_01.getId());
                queryResult.setMessage(Check.CH_01.getValue());
                return queryResult;
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new CommonException(e.getMessage(),e);
        }
    }

    /**
     *  获取顶点
     * @return 执行结果对象
     */
    @Override
    public QueryResult obtainVertex(Vertex vertex) {
        QueryResult queryResult = new QueryResult();
        JcNode jcNode = new JcNode(JCNODE);
        JcQuery jcQuery = new JcQuery();
        try{
            ValidateResult validateResult = validateUtil.validateVertex(vertex,Method.OBTAIN);
            /**
             * 如果顶点数据格式校验有问题
             */
            if(!validateResult.isValidate()){
                queryResult.setCode(validateResult.getCode());
                queryResult.setMessage(validateResult.getMsg());
                return  queryResult;
            }
            jcQuery.setClauses(new IClause[]{
                    MATCH.node(jcNode).label(vertex.getTenant()),
                    WHERE.valueOf(jcNode.property(ID)).EQUALS(vertex.getId()),
                    RETURN.value(jcNode)
            });
            String cypher = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
            cypher = cypher.replace(CYPHER_PLANNER,"");
            logger.info(cypher);
            Result result = session.query(cypher,new HashMap<String,Object>());
            if(result.iterator().hasNext()){
                List<Map<String,Object>> list = Util.properties2Map(result,JCNODE);
                if(!list.isEmpty()){
                    Map<String,Object> resultMap = list.get(0);
                    queryResult.setCode(Code.CO_05.getId());
                    queryResult.setMessage(Code.CO_05.getValue());
                    queryResult.setData(resultMap);
                    return queryResult;
                }
            }else{
                queryResult.setCode(Check.CH_01.getId());
                queryResult.setMessage(Check.CH_01.getValue());
                queryResult.setData(new HashMap<String,Object>());
                return queryResult;
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            queryResult.setCode(Code.EX_00.getId());
            queryResult.setMessage(Code.EX_00.getValue());
            return queryResult;
        }
        return queryResult;
    }

    /**
     * 获取相邻的出向顶点及顶点属性
     * @return 执行结果对象
     */
    @Override
    public QueryResult obtainVertexOutV(VertexV vertexV) {
        return this.obtainVertexAllV(vertexV,Direction.OUT);
    }


    /**
     * 获取相邻的入向顶点及顶点属性
     * @return 执行结果对象
     */
    @Override
    public QueryResult obtainVertexInV(VertexV vertexV) {
        return this.obtainVertexAllV(vertexV,Direction.IN);
    }

    /**
     * 获取相邻的双向顶点及顶点属性
     * @return 执行结果对象
     */
    @Override
    public QueryResult obtainVertexBothV(VertexV vertexV) {
        return this.obtainVertexAllV(vertexV,Direction.BOTH);
    }

    /**
     * 获取顶点出向、入向、双向顶点方法执行体
     * @param vertexV 顶点对象
     * @param direction 关系方向，out in 或both
     * @return 执行结果对象
     */
    public QueryResult obtainVertexAllV(VertexV vertexV, Direction direction) {
        QueryResult queryResult = new QueryResult();
        queryResult.setData(new HashMap<String,Object>());
        String string = "end";
        JcQuery jcQuery = new JcQuery();
        JcNode end = new JcNode(string);
        JcNumber jcNumber = new JcNumber(COUNT);
        Map<String,Object> resultMap = new HashMap<String, Object>();
        try{
            ValidateResult validateResult = validateUtil.validateVertexV(vertexV,Method.OBTAIN);
            /**
             * 如果顶点数据格式校验有问题
             */
            if(!validateResult.isValidate()){
                queryResult.setCode(validateResult.getCode());
                queryResult.setMessage(validateResult.getMsg());
                return  queryResult;
            }

            Map<String,IClause[]> stringMap = obtainVertexAllVIClause(vertexV,direction,end,jcNumber);
            if(null == stringMap){
                queryResult.setCode(Code.QUERY_ERROR.getId());
                queryResult.setMessage(Code.QUERY_ERROR.getValue());
                return queryResult;
            }
            if(null != stringMap.get(ICLAUSE) && null != stringMap.get(TOTAL)){
                IClause[] iClauses = stringMap.get(ICLAUSE);
                IClause[] total = stringMap.get(TOTAL);
                jcQuery.setClauses(iClauses);
                String cypherIClauses = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
                cypherIClauses = cypherIClauses.replace(CYPHER_PLANNER,"");
                logger.info(cypherIClauses);
                Result obtainResult = session.query(cypherIClauses,new HashedMap<String,Object>());
                if(obtainResult.iterator().hasNext()){
                    List<Map<String,Object>> listMaps = Util.properties2Map(obtainResult,string);
                    if(!listMaps.isEmpty()){
                        resultMap.put(NODES,listMaps);
                        switch (direction){
                            case OUT:
                                queryResult.setCode(Code.CO_09.getId());
                                queryResult.setMessage(Code.CO_09.getValue());
                                break;
                            case IN:
                                queryResult.setCode(Code.CO_11.getId());
                                queryResult.setMessage(Code.CO_11.getValue());
                                break;
                            case BOTH:
                                queryResult.setCode(Code.CO_13.getId());
                                queryResult.setMessage(Code.CO_13.getValue());
                                break;
                            default:
                                break;
                        }
                    }
                }else{
                    resultMap.put(NODES,new ArrayList<Map<String,Object>>());
                    switch (direction){
                        case OUT:
                            queryResult.setCode(Code.CO_39.getId());
                            queryResult.setMessage(Code.CO_39.getValue());
                            break;
                        case IN:
                            queryResult.setCode(Code.CO_40.getId());
                            queryResult.setMessage(Code.CO_40.getValue());
                            break;
                        case BOTH:
                            queryResult.setCode(Code.CO_41.getId());
                            queryResult.setMessage(Code.CO_41.getValue());
                            break;
                        default:
                            break;
                    }
                }
                jcQuery.setClauses(total);
                String cypherTotal = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
                cypherTotal = cypherTotal.replace(CYPHER_PLANNER,"");
                logger.info(cypherTotal);
                Result resulTotal = session.query(cypherTotal,new HashedMap<String,Object>());
                commonTotalOperate(resulTotal,COUNT,resultMap);
                queryResult.setData(resultMap);
            }else{
                switch (direction){
                    case OUT:
                        queryResult.setCode(Code.CO_08.getId());
                        queryResult.setMessage(Code.CO_08.getValue());
                        break;
                    case IN:
                        queryResult.setCode(Code.CO_10.getId());
                        queryResult.setMessage(Code.CO_10.getValue());
                        break;
                    case BOTH:
                        queryResult.setCode(Code.CO_12.getId());
                        queryResult.setMessage(Code.CO_12.getValue());
                        break;
                    default:
                        break;
                }
            }
            return queryResult;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            queryResult.setCode(Code.EX_00.getId());
            queryResult.setMessage(Code.EX_00.getValue());
            return queryResult;
        }
    }

    /**
     * 多条件查询语句方法
     * @param vertexV 顶点start 顶点 end 关系type 对象
     * @param direction 边的方向
     * @param end 查询结束顶点
     * @return query 语句数组；
     */
    private  Map<String,IClause[]>  obtainVertexAllVIClause(VertexV vertexV,Direction direction,JcNode end,JcNumber jcNumber){
        List<IClause> list = new ArrayList<IClause>();
        JcNode start = new JcNode("start");
        JcRelation relation = new JcRelation("jcRelation");
        JcPath jcPath = new JcPath(JCPATH);
        Map<String,IClause[]> stringMap = new HashMap<String, IClause[]>();
        try{
            commonBrach(list,direction,jcPath,relation,start,end,vertexV);
            Concatenator concatenator = WHERE.valueOf(start.property(ID)).EQUALS(vertexV.getSourceId());
            if(null != vertexV.getEdge() && !StringUtils.isEmpty(vertexV.getEdge().getType())){
                concatenator.AND().valueOf(relation.type()).EQUALS(vertexV.getEdge().getType());
            }
            commonFilter(concatenator,vertexV,relation,end);
            list.add(concatenator);
            list.add(RETURN.DISTINCT().value(end)
                    .SKIP(validateUtil.graphDataSkip(vertexV.getSkip()))
                    .LIMIT(validateUtil.graphDataLimit(vertexV.getLimit())));
            /**
             * 查询结果语句
             */
            IClause[] iClauses = list.toArray(new IClause[list.size()]);
            stringMap.put(ICLAUSE,iClauses);
            /**
             * 查询total语句；
             */
            list.remove(list.size()-1);
            list.add(RETURN.count().DISTINCT().value(end).AS(jcNumber));
            IClause[] total = list.toArray(new IClause[list.size()]);
            stringMap.put(TOTAL,total);
            return stringMap;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            return null;
        }
    }

    /**
     * 获取相邻出向顶点的id列表
     * @return 执行结果对象
     */
    @Override
    public QueryResult obtainVertexOutIds(VertexV vertexV) {
        return this.obtainVertexAllIds(vertexV,Direction.OUT);
    }

    /**
     * 获取相邻入向顶点的id列表
     * @return 执行结果对象
     */
    @Override
    public QueryResult obtainVertexInIds(VertexV vertexV) {
        return this.obtainVertexAllIds(vertexV,Direction.IN);
    }


    /**
     * 获取相邻双向顶点的id列表
     * @return 执行结果对象
     */
    @Override
    public QueryResult obtainVertexBothIds(VertexV vertexV) {
        return this.obtainVertexAllIds(vertexV,Direction.BOTH);
    }


    /**
     * 获取顶点出向、入向、双向顶点ids 列表
     * @param vertexV 开始顶点
     * @param direction 方向[out;in;both]
     * @return 查询执行结果
     */
    public QueryResult obtainVertexAllIds(VertexV vertexV, Direction direction){
        QueryResult queryResult = new QueryResult();
        queryResult.setData(new ArrayList<String>());
        String string = "ids";
        JcQuery jcQuery = new JcQuery();
        JcString jcString = new JcString(string);
        JcNumber jcNumber = new JcNumber(COUNT);
        Map<String,Object> resultMap = new HashMap<String, Object>();
        try{
            ValidateResult validateResult = validateUtil.validateVertexV(vertexV,Method.OBTAIN);
            /**
             * 如果顶点数据格式校验有问题
             */
            if(!validateResult.isValidate()){
                queryResult.setCode(validateResult.getCode());
                queryResult.setMessage(validateResult.getMsg());
                return  queryResult;
            }

            Map<String,IClause[]> stringMap = obtainVertexAllIdsIClause(vertexV,direction,jcString,jcNumber);
            if(null == stringMap){
                queryResult.setCode(Code.QUERY_ERROR.getId());
                queryResult.setMessage(Code.QUERY_ERROR.getValue());
                return queryResult;
            }
            if(null != stringMap.get(ICLAUSE) && null != stringMap.get(TOTAL)){
                IClause[] iClauses = stringMap.get(ICLAUSE);
                IClause[] total = stringMap.get(TOTAL);
                jcQuery.setClauses(iClauses);
                String cypherIClauses = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
                cypherIClauses = cypherIClauses.replace(CYPHER_PLANNER,"");
                logger.info(cypherIClauses);
                Result resultIClauses = session.query(cypherIClauses,new HashedMap<String,Object>());
                if(resultIClauses.iterator().hasNext()){
                    List<String> list = Util.properties2List(resultIClauses,string);
                    if(!list.isEmpty()){
                        resultMap.put(NODE_IDS,list);
                        switch (direction){
                            case OUT:
                                queryResult.setCode(Code.CO_15.getId());
                                queryResult.setMessage(Code.CO_15.getValue());
                                break;
                            case IN:
                                queryResult.setCode(Code.CO_17.getId());
                                queryResult.setMessage(Code.CO_17.getValue());
                                break;
                            case BOTH:
                                queryResult.setCode(Code.CO_19.getId());
                                queryResult.setMessage(Code.CO_19.getValue());
                                break;
                            default:
                                break;
                        }
                    }
                }else{
                    resultMap.put(NODE_IDS,new ArrayList<String>());
                    switch (direction){
                        case OUT:
                            queryResult.setCode(Code.CO_39.getId());
                            queryResult.setMessage(Code.CO_39.getValue());
                            break;
                        case IN:
                            queryResult.setCode(Code.CO_40.getId());
                            queryResult.setMessage(Code.CO_40.getValue());
                            break;
                        case BOTH:
                            queryResult.setCode(Code.CO_41.getId());
                            queryResult.setMessage(Code.CO_41.getValue());
                            break;
                        default:
                            break;
                    }
                }
                jcQuery.setClauses(total);
                String cypherTotal = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
                cypherTotal = cypherTotal.replace(CYPHER_PLANNER,"");
                logger.info(cypherTotal);
                Result resulTotal = session.query(cypherTotal,new HashedMap<String,Object>());
                commonTotalOperate(resulTotal,COUNT,resultMap);
                queryResult.setData(resultMap);
            }else{
                switch (direction){
                    case OUT:
                        queryResult.setCode(Code.CO_14.getId());
                        queryResult.setMessage(Code.CO_14.getValue());
                        break;
                    case IN:
                        queryResult.setCode(Code.CO_16.getId());
                        queryResult.setMessage(Code.CO_16.getValue());
                        break;
                    case BOTH:
                        queryResult.setCode(Code.CO_18.getId());
                        queryResult.setMessage(Code.CO_18.getValue());
                        break;
                    default:
                        break;
                }
            }
            return queryResult;

        }catch (Exception e){
            logger.error(e.getMessage(),e);
            queryResult.setCode(Code.EX_00.getId());
            queryResult.setMessage(Code.EX_00.getValue());
            return queryResult;
        }
    }

    public  Map<String,IClause[]>  obtainVertexAllIdsIClause(VertexV vertexV,Direction direction,JcString jcString,JcNumber jcNumber){
        List<IClause> list = new ArrayList<IClause>();
        JcNode start = new JcNode("start");
        JcNode end = new JcNode("end");
        JcRelation relation = new JcRelation("jcRelation");
        JcPath jcPath = new JcPath(JCPATH);
        Map<String,IClause[]> stringMap = new HashMap<String, IClause[]>();
        try{
            commonBrach(list,direction,jcPath,relation,start,end,vertexV);
            Concatenator concatenator = WHERE.valueOf(start.property(ID)).EQUALS(vertexV.getSourceId());
            if(null != vertexV.getEdge() && !StringUtils.isEmpty(vertexV.getEdge().getType())){
                concatenator.AND().valueOf(relation.type()).EQUALS(vertexV.getEdge().getType());
            }
            commonFilter(concatenator,vertexV,relation,end);
            list.add(concatenator);
            list.add(RETURN.DISTINCT().value(end.property(ID)).AS(jcString)
                    .SKIP(validateUtil.graphDataSkip(vertexV.getSkip()))
                    .LIMIT(validateUtil.graphDataLimit(vertexV.getLimit())));
            /**
             * 查询结果语句
             */
            IClause[] iClauses = list.toArray(new IClause[list.size()]);
            stringMap.put(ICLAUSE,iClauses);
            /**
             * 查询total语句；
             */
            list.remove(list.size()-1);
            list.add(RETURN.count().DISTINCT().value(end).AS(jcNumber));
            IClause[] total = list.toArray(new IClause[list.size()]);
            stringMap.put(TOTAL,total);
            return stringMap;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            return null;
        }
    }

    /**
     * 处理查询结果集total操作
     * @param resulTotal total 执行结果
     * @param string total返回key
     * @param resultMap map 存储对象
     */
    public void commonTotalOperate(Result resulTotal,String string,Map<String,Object> resultMap){
        if(resulTotal.iterator().hasNext()){
            List<Map<String,Object>> listMaps = Util.properties2Map(resulTotal,string);
            if(!listMaps.isEmpty()){
                Object object = listMaps.get(0).get(string);
                if(null != object){
                    int nodesCount = Integer.parseInt(object.toString());
                    resultMap.put(TOTAL,nodesCount);
                }else{
                    resultMap.put(TOTAL,0);
                }
            }
        }else{
            resultMap.put(TOTAL,0);
        }
    }

    /**
     * 查询共同分支 [out;in;both]
     * @param list 查询语句集合
     * @param direction 关系方向：out;in;both
     * @param jcPath path
     * @param relation 关系对象
     * @param start 开始顶点
     * @param end 结束顶点
     * @param vertexV 数据封装body
     */
    public void commonBrach(List<IClause> list,Direction direction,JcPath jcPath,
                       JcRelation relation,JcNode start,JcNode end,VertexV vertexV){
        switch (direction){
            case OUT:
                list.add(MATCH.path(jcPath).node(start).label(vertexV.getTenant())
                        .relation(relation).out().node(end));
                break;
            case  IN:
                list.add(MATCH.path(jcPath).node(start).label(vertexV.getTenant())
                        .relation(relation).in().node(end));
                break;
            case BOTH:
                list.add(MATCH.path(jcPath).node(start).label(vertexV.getTenant())
                        .relation(relation).node(end));
                break;
            default:
                list.add(MATCH.path(jcPath).node(start).label(vertexV.getTenant())
                        .relation(relation).out().node(end));
                break;
        }
    }
    /**
     * 共同限定过滤条件 查询节点& id列表
     * @param concatenator 限定条件
     * @param vertexV 参数对象body
     * @param relation 关系对象
     * @param end end 顶点对象
     */
    public void commonFilter(Concatenator concatenator,VertexV vertexV,JcRelation relation,JcNode end){
        if(null != vertexV.getEdge() &&
                null != vertexV.getEdge().getProperties() &&
                vertexV.getEdge().getProperties().size() > 0){
            for(Iterator it = vertexV.getEdge().getProperties().entrySet().iterator(); it.hasNext();){
                Map.Entry e = (Map.Entry) it.next();
                String key = (String)e.getKey();
                Object value = e.getValue();
                concatenator.AND().valueOf(relation.property(key)).EQUALS(value);
            }
        }
        if(null != vertexV.getTarget() &&
                null != vertexV.getTarget().getProperties() &&
                vertexV.getTarget().getProperties().size() > 0){
            for(Iterator it = vertexV.getTarget().getProperties().entrySet().iterator(); it.hasNext();){
                Map.Entry e = (Map.Entry) it.next();
                String key = (String)e.getKey();
                Object value = e.getValue();
                concatenator.AND().valueOf(end.property(key)).EQUALS(value);
            }
        }
    }


    /**
     * 移除顶点及顶点相关联的边
     * @return 执行结果对象
     */
    @Override
    public QueryResult removeVertexEdges(Vertex vertex) {
        QueryResult queryResult = new QueryResult();
        queryResult.setData(new HashMap<String,Object>());
        JcQuery jcQuery = new JcQuery();
        JcNode jcNode = new JcNode(JCNODE);
        try{
            ValidateResult validateResult = validateUtil.validateVertex(vertex,Method.REMOVE);
            /**
             * 如果顶点数据格式校验有问题
             */
            if(!validateResult.isValidate()){
                queryResult.setCode(validateResult.getCode());
                queryResult.setMessage(validateResult.getMsg());
                return  queryResult;
            }
            jcQuery.setClauses(new IClause[]{
                    MATCH.node(jcNode).label(vertex.getTenant()),
                    WHERE.valueOf(jcNode.property(ID)).EQUALS(vertex.getId()),
                    DO.DETACH_DELETE(jcNode)
            });
            String cypher = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
            cypher = cypher.replace(CYPHER_PLANNER,"");
            logger.info(cypher);
            Result result = session.query(cypher,new HashMap<String,Object>());
            QueryStatistics queryStatistics = result.queryStatistics();
            queryResult.setData(new HashedMap<String,Object>());
            if(queryStatistics.getNodesDeleted() > 0){
                queryResult.setCode(Code.CO_07.getId());
                queryResult.setMessage(Code.CO_07.getValue());
                return queryResult;
            }else{
                queryResult.setCode(Check.CH_01.getId());
                queryResult.setMessage(Check.CH_01.getValue());
                queryResult.setData(new HashMap<String,Object>());
                return queryResult;
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new CommonException(e.getMessage(),e);
        }
    }

    /**
     * 创建两个顶点间的边,在顶点存在的情况下创建
     * @param path path 对象包含顶点及边属性
     * @return 执行结果对象
     */
    @Override
    public QueryResult createEdgeNodeNonExistent(Path path) {
        QueryResult queryResult = new QueryResult();
        queryResult.setData(new HashMap<String,Object>());
        try {
            Vertex startV = path.getSource();
            Vertex endV = path.getTarget();
            Edge edge = path.getEdge();
            ValidateResult validateResult = validateUtil.validatePath(path,Method.CREATE);
            /**
             * 如果edge数据格式校验有问题
             */
            if(!validateResult.isValidate()){
                queryResult.setCode(validateResult.getCode());
                queryResult.setMessage(validateResult.getMsg());
                return  queryResult;
            }
            CheckResult checkResult1 = checkVertexExist(path.getSource());
            if (!checkResult1.isExist()) {
                this.createVertex(path.getSource());
            }
            CheckResult checkResult2 = checkVertexExist(path.getTarget());
            if(!checkResult2.isExist()){
                this.createVertex(path.getTarget());
            }
            JcQuery jcQuery = new JcQuery();
            String string = "jcRelation";
            JcRelation jcRelation = new JcRelation(string);
            JcNode start = new JcNode("start");
            JcNode end = new JcNode("end");
            List<IClause> list = new ArrayList<IClause>();
            list.add(MATCH.node(start).label(startV.getTenant()).property(ID).value(startV.getId()));
            list.add(MATCH.node(end).label(endV.getTenant()).property(ID).value(endV.getId()));
            Relation relation = CREATE.node(start).relation(jcRelation);
            if(null != edge.getProperties() && edge.getProperties().size() > 0){
                for(Map.Entry entry : edge.getProperties().entrySet()){
                    String key = (String)entry.getKey();
                    Object value = entry.getValue();
                    relation.property(key).value(value);
                }
            }
            relation.property(TENANT).value(edge.getTenant());
            relation.property(ValidateUtil.CREATE_TIME).value(System.currentTimeMillis());
            relation.property(ValidateUtil.LAST_UPDATE_TIME).value(System.currentTimeMillis());
            list.add(relation.out().type(edge.getType()).node(end));
            list.add(RETURN.value(jcRelation));
            IClause[] iClauses = list.toArray(new IClause[list.size()]);
            jcQuery.setClauses(iClauses);
            String cypher = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
            cypher = cypher.replace(CYPHER_PLANNER,"");
            Result result = session.query(cypher,new HashMap<String,Object>());
            QueryStatistics queryStatistics = result.queryStatistics();
            if(queryStatistics.getRelationshipsCreated() > 0){
                queryResult.setCode(Code.CO_20.getId());
                queryResult.setMessage(Code.CO_20.getValue());
                queryResult.setData(Util.properties2Map(result,string).get(0));
                return queryResult;
            }else{
                throw new CommonException(Code.CO_21.getValue());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
           throw new CommonException(e.getMessage(),e);
        }
    }

    /**
     * 创建两个顶点之间的边
     * @param relationShip
     * @return
     */
    @Override
    public QueryResult createEdge(RelationShip relationShip) {
        QueryResult queryResult  = new QueryResult();
        queryResult.setData(new HashMap<String,Object>());
        ValidateResult validateResult = validateUtil.validateRelationShip(relationShip, Method.CREATE);
        if(!validateResult.isValidate()){
            queryResult.setCode(validateResult.getCode());
            queryResult.setMessage(validateResult.getMsg());
            return queryResult;
        }
        JcQuery jcQuery = new JcQuery();
        String string = "jcRelation";
        JcRelation jcRelation = new JcRelation(string);
        JcNode start = new JcNode("start");
        JcNode end = new JcNode("end");
        List<IClause> list = new ArrayList<IClause>();
        list.add(MATCH.node(start).label(relationShip.getEdge().getTenant()).property(ID).value(relationShip.getSourceId()));
        list.add(MATCH.node(end).label(relationShip.getEdge().getTenant()).property(ID).value(relationShip.getTargetId()));
        Relation relation = CREATE.node(start).relation(jcRelation);
        if(null != relationShip.getEdge().getProperties() && relationShip.getEdge().getProperties().size() > 0){
            for(Map.Entry entry : relationShip.getEdge().getProperties().entrySet()){
                String key = (String)entry.getKey();
                Object value = entry.getValue();
                relation.property(key).value(value);
            }
        }
        relation.property(TENANT).value(relationShip.getEdge().getTenant());
        relation.property(ValidateUtil.CREATE_TIME).value(System.currentTimeMillis());
        relation.property(ValidateUtil.LAST_UPDATE_TIME).value(System.currentTimeMillis());
        list.add(relation.out().type(relationShip.getEdge().getType()).node(end));
        list.add(RETURN.value(jcRelation));
        IClause[] iClauses = list.toArray(new IClause[list.size()]);
        jcQuery.setClauses(iClauses);
        String cypher = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
        cypher = cypher.replace(CYPHER_PLANNER,"");
        Result result = session.query(cypher,new HashMap<String,Object>());
        QueryStatistics queryStatistics = result.queryStatistics();
        if(queryStatistics.getRelationshipsCreated() > 0){
            queryResult.setCode(Code.CO_20.getId());
            queryResult.setMessage(Code.CO_20.getValue());
            Map<String,Object> returnMap = Util.properties2Map(result,string).get(0);
            queryResult.setData(returnMap);
            return queryResult;
        }else{
            queryResult.setCode(Code.CO_32.getId());
            queryResult.setMessage(Code.CO_32.getValue());
            return queryResult;
        }
    }

    /**
     * 移除两个顶点间的边
     * @return 执行结果对象
     */
    @Override
    public QueryResult removeEdge(Edge edge) {
        QueryResult queryResult = new QueryResult();
        queryResult.setData(new HashMap<String,Object>());
        JcQuery jcQuery = new JcQuery();
        JcNode start = new JcNode("start");
        JcNode end = new JcNode("end");
        JcPath jcPath = new JcPath(JCPATH);
        JcRelation jcRelation = new JcRelation("jcRelation");
        try{
            jcQuery.setClauses(new IClause[]{
                    MATCH.path(jcPath).node(start).label(edge.getTenant()).relation(jcRelation).out().node(end).label(edge.getTenant()),
                    WHERE.valueOf(jcRelation.id()).EQUALS(edge.getId()).AND().valueOf(jcRelation.property(TENANT)).EQUALS(edge.getTenant()),
                    DO.DELETE(jcRelation)
            });
            String cypher = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
            cypher = cypher.replace(CYPHER_PLANNER,"");
            Result result = session.query(cypher,new HashMap<String,Object>());
            QueryStatistics queryStatistics = result.queryStatistics();
            if(queryStatistics.getRelationshipsDeleted() > 0){
                queryResult.setCode(Code.CO_23.getId());
                queryResult.setMessage(Code.CO_23.getValue());
                return queryResult;
            }else{
                queryResult.setCode(Check.CH_03.getId());
                queryResult.setMessage(Check.CH_03.getValue());
                queryResult.setData(new HashMap<String,Object>());
                return queryResult;
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new CommonException(e.getMessage(),e);
        }
    }

    /**
     * 检查指定的关系是否存在
     * @param edge 关系对象
     * @return 是否存在对象
     */
    public CheckResult checkEdgeExist(Edge edge){
        CheckResult checkResult = new CheckResult();
        checkResult.setExist(false);
        JcRelation jcRelation = new JcRelation("jcRelation");
        JcNumber jcNumber = new JcNumber(JCNUMBER);

        try{
            JcQuery jcQuery = new JcQuery();
            jcQuery.setClauses(new IClause[]{
                    MATCH.node().relation(jcRelation).out().node(),
                    WHERE.valueOf(jcRelation.id()).EQUALS(edge.getId())
                            .AND().valueOf(jcRelation.property(TENANT)).EQUALS(edge.getTenant()),
                    RETURN.count().value(jcRelation).AS(jcNumber)
            });
            String cypher = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
            cypher = cypher.replace(CYPHER_PLANNER,"");
            logger.info(cypher);
            Result result = session.query(cypher,new HashMap<String,Object>());
            if(result.iterator().hasNext()){
                Map<String,Object> resultMap = result.iterator().next();
                Object object = resultMap.get(JCNUMBER);
                String str = String.valueOf(object);
                int count = Integer.parseInt(str);
                if(count <= 0 ){
                    checkResult.setCode(Check.CH_03.getId());
                    checkResult.setMsg(Check.CH_03.getValue());
                    return checkResult;
                }else{
                    checkResult.setExist(true);
                    checkResult.setCode(Check.CH_04.getId());
                    checkResult.setMsg(Check.CH_04.getValue());
                    return checkResult;
                }
            }else{
                checkResult.setCode(Check.CH_03.getId());
                checkResult.setMsg(Check.CH_03.getValue());
                return checkResult;
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return checkResult;
    }

    /**
     * 检查顶点是否存在
     * @param vertex 顶点对象
     * @return 检查结果，true已经存在，false，不存在
     */
    public CheckResult checkVertexExist(Vertex vertex){
        CheckResult checkResult = new CheckResult();
        JcNumber jcNumber = new JcNumber(JCNUMBER);
        try{
            JcQuery jcQuery = new JcQuery();
            JcNode jcNode = new JcNode(JCNODE);
            jcQuery.setClauses(new IClause[]{
                    MATCH.node(jcNode).label(vertex.getTenant()),
                    WHERE.valueOf(jcNode.property(ID)).EQUALS(vertex.getId()),
                    RETURN.count().value(jcNode).AS(jcNumber)
            });
            String cypher = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
            cypher = cypher.replace(CYPHER_PLANNER,"");
            logger.info(cypher);
            Result result = session.query(cypher,new HashMap<String,Object>());
            if(result.iterator().hasNext()){
                Map<String,Object> resultMap = result.iterator().next();
                Object object = resultMap.get(JCNUMBER);
                String str = String.valueOf(object);
                int count = Integer.parseInt(str);
                if(count <= 0 ){
                    checkResult.setCode(Check.CH_01.getId());
                    checkResult.setMsg(Check.CH_01.getValue());
                    return checkResult;
                }else{
                    checkResult.setExist(true);
                    checkResult.setCode(Check.CH_02.getId());
                    checkResult.setMsg(Check.CH_02.getValue());
                    return checkResult;
                }
            }else{
                checkResult.setCode(Check.CH_01.getId());
                checkResult.setMsg(Check.CH_01.getValue());
                return checkResult;
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return  checkResult;
    }

    /**
     * 查询边
     * @param edge 查询对象
     * @return 执行结果对象
     */
	@Override
	public QueryResult obtainEdge(Edge edge) {
		QueryResult queryResult = new QueryResult();
		queryResult.setData(new HashedMap<String,Object>());
        try {
        	/**
             * 校验edge数据格式
             */
        	ValidateResult validateResult = validateUtil.validateEdge(edge,Method.OBTAIN);
            if(!validateResult.isValidate()){
                queryResult.setCode(validateResult.getCode());
                queryResult.setMessage(validateResult.getMsg());
                return  queryResult;
            }
            JcQuery jcQuery = new JcQuery();
            String string = "jcRelation";
            JcRelation jcRelation = new JcRelation(string);
            jcQuery.setClauses(new IClause[]{
                    MATCH.node().relation(jcRelation).out().node(),
                    WHERE.valueOf(jcRelation.id()).EQUALS(edge.getId())
                            .AND().valueOf(jcRelation.property(TENANT)).EQUALS(edge.getTenant()),
                    RETURN.value(jcRelation)
            });
            String cypher = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
            cypher = cypher.replace(CYPHER_PLANNER, "");
            logger.info(cypher);
            Result result = session.query(cypher,new HashMap<String,Object>());
            if(result.iterator().hasNext()){
                List<Map<String,Object>> list = Util.properties2Map(result,string);
                if(!list.isEmpty()){
                    Map<String,Object> resultMap = list.get(0);
                    queryResult.setCode(Code.CO_25.getId());
                    queryResult.setMessage(Code.CO_25.getValue());
                    queryResult.setData(resultMap);
                    return queryResult;
                }
            }else{
                queryResult.setCode(Code.CO_24.getId());
                queryResult.setMessage(Code.CO_24.getValue());
                return queryResult;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            queryResult.setCode(Code.EX_00.getId());
            queryResult.setMessage(Code.EX_00.getValue());
            return queryResult;
        }
        return queryResult;
	}

	@Override
	public QueryResult obtainEdgeBetweenVertex(Vertex vertex1, Vertex vertex2, int skip, int limit) {
		QueryResult queryResult = new QueryResult();
		queryResult.setData(new HashedMap<String,Object>());
		try{
            /**
             * 校验节点数据格式
             */
			ValidateResult validateResult1 = validateUtil.validateVertex(vertex1,Method.OBTAIN);
            if(!validateResult1.isValidate()){
                queryResult.setCode(validateResult1.getCode());
                queryResult.setMessage(validateResult1.getMsg());
                return  queryResult;
            }
            ValidateResult validateResult2 = validateUtil.validateVertex(vertex2,Method.OBTAIN);
            if(!validateResult2.isValidate()){
                queryResult.setCode(validateResult2.getCode());
                queryResult.setMessage(validateResult2.getMsg());
                return  queryResult;
            }
            /**
             * 分页参数校验
             */
            skip = validateUtil.graphDataSkip(skip);
            limit = validateUtil.graphDataLimit(limit);
            
            JcQuery jcQuery = new JcQuery();
            JcNode start = new JcNode("start");
            JcNode end = new JcNode("end");
            String string = "jcRelation";
            JcRelation jcRelation = new JcRelation(string);
            jcQuery.setClauses(new IClause[]{
                    MATCH.node(start).label(vertex1.getTenant()).property(ID).value(vertex1.getId())
	                    .relation(jcRelation)
	                    .node(end).label(vertex2.getTenant()).property(ID).value(vertex2.getId()),
                    RETURN.value(jcRelation).ORDER_BY_DESC(CREATE_TIME).SKIP(skip).LIMIT(limit)
            });
            String cypher = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
            cypher = cypher.replace(CYPHER_PLANNER, "");
            logger.info(cypher);
            Result result = session.query(cypher, new HashMap<String,Object>());
            /**
             * 查询任意两节点之间边的total
             */
            int count = obtainEdgeTotalBetweenVertex(vertex1, vertex2);
            if(count > 0) {
            	Map<String,Object> objectMap = new HashMap<String, Object>();
            	objectMap.put(TOTAL, count);
            	if(result.iterator().hasNext()){
                    List<Map<String,Object>> list = Util.properties2Map(result,string);
                    if(!list.isEmpty()){
                    	objectMap.put(EDGES, list);
                    }
                }
                queryResult.setCode(Code.CO_27.getId());
                queryResult.setMessage(Code.CO_27.getValue());
                queryResult.setData(objectMap);
                return queryResult;
            } else{
            	queryResult.setCode(Code.CO_28.getId());
                queryResult.setMessage(Code.CO_28.getValue());
                return queryResult;
            }
		} catch (Exception e) {
            logger.error(e.getMessage(), e);
            queryResult.setCode(Code.EX_00.getId());
            queryResult.setMessage(Code.EX_00.getValue());
            return queryResult;
        }
	}

	private int obtainEdgeTotalBetweenVertex(Vertex vertex1, Vertex vertex2) {
		int count = 0;
		JcQuery jcQuery = new JcQuery();
        JcNode start = new JcNode("start");
        JcNode end = new JcNode("end");
        String string = "jcRelation";
        JcRelation jcRelation = new JcRelation(string);
        jcQuery.setClauses(new IClause[]{
                MATCH.node(start).label(vertex1.getTenant()).property(ID).value(vertex1.getId())
                    .relation(jcRelation)
                    .node(end).label(vertex2.getTenant()).property(ID).value(vertex2.getId()),
                RETURN.count().DISTINCT().value(jcRelation).AS(jcRelation)
        });
        String cypher = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
        cypher = cypher.replace(CYPHER_PLANNER, "");
        logger.info(cypher);
        Result result = session.query(cypher, new HashMap<String,Object>());
        if(result.iterator().hasNext()){
            count = Integer.parseInt(result.iterator().next().get("jcRelation").toString());
        }
		return count;
	}

	@Override
	public QueryResult obtainEdgeIdsBetweenVertex(Vertex vertex1, Vertex vertex2, int skip, int limit) {
		QueryResult queryResult = new QueryResult();
		queryResult.setData(new HashedMap<String,Object>());
		try{
            /**
             * 校验节点数据格式
             */
			ValidateResult validateResult1 = validateUtil.validateVertex(vertex1,Method.OBTAIN);
            if(!validateResult1.isValidate()){
                queryResult.setCode(validateResult1.getCode());
                queryResult.setMessage(validateResult1.getMsg());
                return  queryResult;
            }
            ValidateResult validateResult2 = validateUtil.validateVertex(vertex2,Method.OBTAIN);
            if(!validateResult2.isValidate()){
                queryResult.setCode(validateResult2.getCode());
                queryResult.setMessage(validateResult2.getMsg());
                return  queryResult;
            }
            /**
             * 分页参数校验
             */
            skip = validateUtil.graphDataSkip(skip);
            limit = validateUtil.graphDataLimit(limit);
           
            JcQuery jcQuery = new JcQuery();
            JcNode start = new JcNode("start");
            JcNode end = new JcNode("end");
            String string = "jcRelation";
            JcRelation jcRelation = new JcRelation(string);
            jcQuery.setClauses(new IClause[]{
                    MATCH.node(start).label(vertex1.getTenant()).property(ID).value(vertex1.getId())
	                    .relation(jcRelation)
	                    .node(end).label(vertex2.getTenant()).property(ID).value(vertex2.getId()),
                    RETURN.value(jcRelation.id()).ORDER_BY_DESC(CREATE_TIME).SKIP(skip).LIMIT(limit)
            });
            String cypher = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
            cypher = cypher.replace(CYPHER_PLANNER, "");
            logger.info(cypher);
            Result result = session.query(cypher, new HashMap<String,Object>());
            /**
             * 查询任意两节点之间边的total
             */
            int count = obtainEdgeTotalBetweenVertex(vertex1, vertex2);
            if(count > 0) {
            	List<Object> objectList = new ArrayList<Object>();
            	Map<String,Object> objectMap = new HashMap<String, Object>();
            	if(result.iterator().hasNext()){
            		for(Iterator<Map<String, Object>> iterator = result.iterator(); iterator.hasNext();){
            			objectList.add(iterator.next().get("id(jcRelation)"));
            		}
                }
            	objectMap.put(TOTAL, count);
            	objectMap.put(EDGEIDS, objectList);
            	queryResult.setCode(Code.CO_29.getId());
                queryResult.setMessage(Code.CO_29.getValue());
                queryResult.setData(objectMap);
                return queryResult;
            } else{
            	queryResult.setCode(Code.CO_28.getId());
                queryResult.setMessage(Code.CO_28.getValue());
                return queryResult;
            }
		} catch (Exception e) {
            logger.error(e.getMessage(), e);
            queryResult.setCode(Code.EX_00.getId());
            queryResult.setMessage(Code.EX_00.getValue());
            return queryResult;
        }
	}

	/**
	 * 修改边属性
	 * @param edge 边对象
	 */
	@Override
	public QueryResult updateEdge(Edge edge) {
		QueryResult queryResult = new QueryResult();
        queryResult.setData(new HashMap<String,Object>());
        JcQuery jcQuery = new JcQuery();
        try{
        	/**
        	 * 校验边数据是否合法
        	 */
        	ValidateResult validateResult = validateUtil.validateEdge(edge,Method.UPDATE);
        	if(!validateResult.isValidate()){
                queryResult.setCode(validateResult.getCode());
                queryResult.setMessage(validateResult.getMsg());
                return queryResult;
            }
            
            String string = "jcRelation";
            JcRelation jcRelation = new JcRelation(string);
            List<IClause> list = new ArrayList<IClause>();
            list.add(MATCH.node()
            		.relation(jcRelation).property(TENANT).value(edge.getTenant())
            		.node());
            list.add(WHERE.valueOf(jcRelation.id()).EQUALS(edge.getId()));
            list.add(DO.SET(jcRelation.property(ValidateUtil.LAST_UPDATE_TIME)).to(System.currentTimeMillis()));
            if(null != edge.getProperties() && edge.getProperties().size() > 0){
                for(Map.Entry<String, Object> entry : edge.getProperties().entrySet()){
                    Object key =  entry.getKey();
                    String keyStr = key.toString();
                    Object value = entry.getValue();
                    list.add(DO.SET(jcRelation.property(keyStr)).to(value));
                }
            }
            list.add(RETURN.value(jcRelation));
            IClause [] iClauses = list.toArray(new IClause[list.size()]);
            jcQuery.setClauses(iClauses);
            String cypher = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
            cypher = cypher.replace(CYPHER_PLANNER, "");
            logger.info(cypher);
            Result result = session.query(cypher, new HashMap<String,Object>());
            QueryStatistics queryStatistics = result.queryStatistics();
            if(queryStatistics.containsUpdates()){
                queryResult.setCode(Code.CO_31.getId());
                queryResult.setMessage(Code.CO_31.getValue());
                return queryResult;
            } else{
            	queryResult.setCode(Check.CH_03.getId());
                queryResult.setMessage(Check.CH_03.getValue());
                return queryResult;
            }
        } catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new CommonException(e.getMessage());
        }
	}

	/**
	 * 根据顶点查询双向边
	 */
	@Override
	public QueryResult obtainBothEdgeByVertex(Vertex vertex, EdgeV edgeV) {
		QueryResult queryResult = new QueryResult();
		try{
			queryResult = commonObtainEdgeByVertex(vertex, Direction.BOTH, edgeV);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            queryResult.setCode(Code.EX_00.getId());
            queryResult.setMessage(Code.EX_00.getValue());
            return queryResult;
        }
		return queryResult;
	}
	
	private QueryResult commonObtainEdgeByVertex(Vertex vertex, Direction direction, EdgeV edgeV){
		QueryResult queryResult = new QueryResult();
		/**
         * 校验节点数据格式
         */
		ValidateResult validateResult = validateUtil.validateVertex(vertex, Method.OBTAIN);
        if(!validateResult.isValidate()){
            queryResult.setCode(validateResult.getCode());
            queryResult.setMessage(validateResult.getMsg());
            return  queryResult;
        }
        /**
    	 * 校验边数据是否合法
    	 */
        if(null != edgeV.getEdge()) {
        	ValidateResult validateResult1 = validateUtil.validateEdge(edgeV.getEdge(), Method.OBTAIN);
        	if(!validateResult1.isValidate()){
        		queryResult.setCode(validateResult1.getCode());
        		queryResult.setMessage(validateResult1.getMsg());
        		return queryResult;
        	}
        }
		JcQuery jcQuery = new JcQuery();
		List<IClause> list = new ArrayList<IClause>();
		String string = "jcRelation";
		Map<String,IClause[]> stringMap = iClausesObtainEdgeByVertex(list, vertex, direction, edgeV);
		if(stringMap.isEmpty()){
            queryResult.setCode(Code.QUERY_ERROR.getId());
            queryResult.setMessage(Code.QUERY_ERROR.getValue());
            return queryResult;
        }
        if(null != stringMap.get(ICLAUSE) && null != stringMap.get(TOTAL)){
            IClause[] iClauses = stringMap.get(ICLAUSE);
            IClause[] total = stringMap.get(TOTAL);
            int count = 0;
            jcQuery.setClauses(total);
            String cypher = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
            cypher = cypher.replace(CYPHER_PLANNER, "");
            logger.info(cypher);
            Result result = session.query(cypher, new HashMap<String,Object>());
            if(result.iterator().hasNext()){
                count = Integer.parseInt(result.iterator().next().get(JCNUMBER).toString());
            }
            if(count > 0) {
            	Map<String,Object> objectMap = new HashMap<String, Object>();
            	objectMap.put(TOTAL, count);
            	jcQuery.setClauses(iClauses);
                String cypherIClauses = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
                cypherIClauses = cypherIClauses.replace(CYPHER_PLANNER, "");
                logger.info(cypherIClauses);
                Result resultIClauses = session.query(cypherIClauses,new HashedMap<String,Object>());
                if(resultIClauses.iterator().hasNext()){
                	List<Map<String,Object>> listRs = Util.properties2Map(resultIClauses, string);
                    if(!listRs.isEmpty()){
                    	objectMap.put(EDGES, listRs);
                    }
                }
                switch (direction){
                    case OUT:
                        queryResult.setCode(Code.CO_35.getId());
                        queryResult.setMessage(Code.CO_35.getValue());
                        break;
                    case IN:
                        queryResult.setCode(Code.CO_34.getId());
                        queryResult.setMessage(Code.CO_34.getValue());
                        break;
                    case BOTH:
                        queryResult.setCode(Code.CO_33.getId());
                        queryResult.setMessage(Code.CO_33.getValue());
                        break;
                }
            	queryResult.setData(objectMap);
            } else {
                queryResult.setCode(Code.CO_28.getId());
                queryResult.setMessage(Code.CO_28.getValue());
            }
        }else{
        	queryResult.setCode(Code.CO_36.getId());
            queryResult.setMessage(Code.CO_36.getValue());
        }
		return queryResult;
	}

	private Map<String,IClause[]> iClausesObtainEdgeByVertex(List<IClause> list, Vertex vertex,
			Direction direction, EdgeV edgeV) {
		int skip = edgeV.getSkip();
		int limit = edgeV.getLimit();
		/**
         * 分页参数校验
         */
        skip = validateUtil.graphDataSkip(skip);
        limit = validateUtil.graphDataLimit(limit);
        
		Map<String,IClause[]> stringMap = new HashMap<String, IClause[]>();
		JcNode start = new JcNode("start");
        JcNode end = new JcNode("end");
        JcRelation relation = new JcRelation("jcRelation");
        JcNumber jcNumber = new JcNumber(JCNUMBER);
		switch (direction){
	        case OUT:
	            list.add(MATCH.node(start).label(vertex.getTenant()).relation(relation).out().node(end));
	            break;
	        case IN:
	            list.add(MATCH.node(start).label(vertex.getTenant()).relation(relation).in().node(end));
	            break;
	        case BOTH:
	            list.add(MATCH.node(start).label(vertex.getTenant()).relation(relation).node(end));
	            break;
	        default:
	            list.add(MATCH.node(start).label(vertex.getTenant()).relation(relation).out().node(end));
	            break;
	    }
		Concatenator concatenator = conditionObtainEdgeByVertex(start, edgeV, vertex);
		list.add(concatenator);
		list.add(RETURN.value(relation).ORDER_BY_DESC(CREATE_TIME).SKIP(skip).LIMIT(limit));
		/**
         * 查询结果语句
         */
        IClause[] iClauses = list.toArray(new IClause[list.size()]);
        stringMap.put(ICLAUSE,iClauses);
        /**
         * 查询total语句；
         */
        list.remove(list.size()-1);
        list.add(RETURN.count().DISTINCT().value(relation).AS(jcNumber));
        IClause[] total = list.toArray(new IClause[list.size()]);
        stringMap.put(TOTAL,total);
		return stringMap;
	}

	@SuppressWarnings("rawtypes")
	private Concatenator conditionObtainEdgeByVertex(JcNode start, EdgeV edgeV, Vertex vertex) {
		String string = "jcRelation";
        JcRelation jcRelation = new JcRelation(string);
        Concatenator concatenator  = WHERE.valueOf(start.property(ID)).EQUALS(vertex.getId());
        if(null != edgeV.getEdge()) {
        	if(null != edgeV.getEdge().getType()) {
        		concatenator.AND().valueOf(jcRelation.type()).EQUALS(edgeV.getEdge().getType());
        	} else if(null != edgeV.getEdge().getProperties() && edgeV.getEdge().getProperties().size() > 0){
        		for(Map.Entry mapEntry : edgeV.getEdge().getProperties().entrySet()){
        			String key = (String)mapEntry.getKey();
        			Object value = mapEntry.getValue();
        			concatenator.AND().valueOf(jcRelation.property(key)).EQUALS(value);
        		}
        	}
        }
		return concatenator;
	}

	/**
	 * 根据顶点获取边接口 入向：
	 */
	@Override
	public QueryResult obtainInEdgeByVertex(Vertex vertex, EdgeV edgeV) {
		QueryResult queryResult = new QueryResult();
		try{
			queryResult = commonObtainEdgeByVertex(vertex, Direction.IN, edgeV);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            queryResult.setCode(Code.EX_00.getId());
            queryResult.setMessage(Code.EX_00.getValue());
            return queryResult;
        }
		return queryResult;
	}

	/**
	 * 根据顶点获取边接口 出向：
	 */
	@Override
	public QueryResult obtainOutEdgeByVertex(Vertex vertex, EdgeV edgeV) {
		QueryResult queryResult = new QueryResult();
		try{
			queryResult = commonObtainEdgeByVertex(vertex, Direction.OUT, edgeV);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            queryResult.setCode(Code.EX_00.getId());
            queryResult.setMessage(Code.EX_00.getValue());
            return queryResult;
        }
		return queryResult;
	}

    /**
     * 查找顶点可能认识的人
     * @param vertexId 顶点id
     * @param edgeV 关系属性
     * @return 查询结果
     */
    //@Override
    public QueryResult sameNeighbors2(String vertexId, EdgeV edgeV) {

        QueryResult queryResult  = new QueryResult();
        queryResult.setData(new HashedMap<String,Object>());
        /**
         * edge 数据校验
         */
        ValidateResult validateResult = validateUtil.validateEdge(edgeV.getEdge(),Method.OBTAIN);
        if(!validateResult.isValidate()){
            queryResult.setCode(validateResult.getCode());
            queryResult.setMessage(validateResult.getMsg());
        }
        JcPath jcPath = new JcPath(JCPATH);
        JcNode start = new JcNode("start");
        JcNode goal = new JcNode("goal");
        String endStr = "end";
        JcNode end = new JcNode(endStr);
        JcRelation jcRelation1 = new JcRelation("jcRelation1");
        JcRelation jcRelation2 = new JcRelation("jcRelation2");
        JcNumber count = new JcNumber(COUNT);

        JcQuery jcQuery = new JcQuery();
        JcQuery totalQuery = new JcQuery();
        List<IClause> list = new ArrayList<IClause>();

        if(StringUtils.isEmpty(edgeV.getEdge().getType())){
            list.add(MATCH.path(jcPath).node(start).label(edgeV.getEdge().getTenant())
                    .relation(jcRelation1).out().node(goal).label(edgeV.getEdge().getTenant())
                    .relation(jcRelation2).in().node(end).label(edgeV.getEdge().getTenant()));
        }else{
            list.add(MATCH.path(jcPath).node(start).label(edgeV.getEdge().getTenant())
                    .relation(jcRelation1).type(edgeV.getEdge().getType()).out().node(goal).label(edgeV.getEdge().getTenant())
                    .relation(jcRelation2).type(edgeV.getEdge().getType()).in().node(end).label(edgeV.getEdge().getTenant()));
        }
        Concatenator concatenator = WHERE.valueOf(start.property(ID)).EQUALS(vertexId)
                .AND().valueOf(end.property(ID)).NOT_EQUALS(vertexId);
        if(null != edgeV.getEdge().getProperties() && edgeV.getEdge().getProperties().size() > 0){
            for(Iterator iterator = edgeV.getEdge().getProperties().entrySet().iterator(); iterator.hasNext();){
                Map.Entry<String,Object> entry = (Map.Entry<String,Object>)iterator.next();
                String key = entry.getKey();
                Object value = entry.getValue();
                concatenator.AND().valueOf(jcRelation1.property(key)).EQUALS(value);
                concatenator.AND().valueOf(jcRelation2.property(key)).EQUALS(value);
            }
        }
        list.add(concatenator);
        /**
         * 拼接total 语句
         */
        List<IClause> totalIClauses = new ArrayList<IClause>();
        totalIClauses.addAll(list);
        totalIClauses.add(WITH.DISTINCT().value(end));
        totalIClauses.add(WITH.count().value(goal).AS(count));
        totalIClauses.add(RETURN.count().ALL());
        IClause[] tClauses = totalIClauses.toArray(new IClause[totalIClauses.size()]);
        totalQuery.setClauses(tClauses);
        /**
         * 数据查询语句
         */
        list.add(RETURN.DISTINCT().value(end));
        list.add(RETURN.count().value(goal).AS(count));
        IClause[] iClauses = list.toArray(new IClause[list.size()]);
        jcQuery.setClauses(iClauses);

        StringBuilder stringBuilder = new StringBuilder();
        String cypher = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
        cypher = cypher.replace(CYPHER_PLANNER,"");
        String totalCypher = iot.jcypher.util.Util.toCypher(totalQuery, Format.NONE);
        totalCypher = totalCypher.replace(CYPHER_PLANNER,"");

        stringBuilder.append(cypher)
                .append(" ORDER BY " + COUNT + " DESC")
                .append(" SKIP " + validateUtil.graphDataSkip(edgeV.getSkip()))
                .append(" LIMIT " + validateUtil.graphDataLimit(edgeV.getLimit()));
        logger.info(stringBuilder.toString());
        logger.info(totalCypher);
        try{
            Result result = session.query(stringBuilder.toString(),new HashMap<String,Object>());
            Result totalResult = session.query(totalCypher,new HashMap<String,Object>());
            Map<String,Object> dataMap = new HashMap<String,Object>();
            if(totalResult.iterator().hasNext() && result.iterator().hasNext()){
                List<Map<String,Object>> dataList = Util.properties2Map(result,endStr,COUNT);
                Map<String,Object> temp = totalResult.iterator().next();
                dataMap.put(TOTAL,Integer.valueOf(String.valueOf(temp.get("count(*)"))));

                dataMap.put(NODES,dataList);
                queryResult.setData(dataMap);
                queryResult.setCode(Code.CO_37.getId());
                queryResult.setMessage(Code.CO_37.getValue());
                return queryResult;
            }else{
                dataMap.put(TOTAL,0);
                dataMap.put(NODES,new ArrayList());
                queryResult.setCode(Code.CO_37.getId());
                queryResult.setMessage(Code.CO_37.getValue());
                queryResult.setData(dataMap);
                return queryResult;
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            queryResult.setCode(Code.EX_00.getId());
            queryResult.setMessage(Code.EX_00.getValue());
            return queryResult;
        }
    }

    /**
     * 查找顶点可能认识的人
     * @param vertexId 顶点id
     * @param edgeV 关系属性
     * @return 查询结果
     */
    @Override
    public QueryResult sameNeighbors(String vertexId, EdgeV edgeV) {

        QueryResult queryResult  = new QueryResult();
        queryResult.setData(new HashedMap<String,Object>());
        /**
         * edge 数据校验
         */
        ValidateResult validateResult = validateUtil.validateEdge(edgeV.getEdge(),Method.OBTAIN);
        if(!validateResult.isValidate()){
            queryResult.setCode(validateResult.getCode());
            queryResult.setMessage(validateResult.getMsg());
        }
        JcNode n = new JcNode("n");
        JcNode m = new JcNode("m");
        JcNode b = new JcNode("b");
        JcRelation l = new JcRelation("l");
        JcRelation r = new JcRelation("r");

        JcQuery jcQuery = new JcQuery();
        List<IClause> list = new ArrayList<IClause>();

        if(StringUtils.isEmpty(edgeV.getEdge().getType())){
            list.add(MATCH.node(n).label(edgeV.getEdge().getTenant())
                    .relation(l).out().node(m).label(edgeV.getEdge().getTenant())
                    .relation(r).in().node(b).label(edgeV.getEdge().getTenant()));
        }else{
            list.add(MATCH.node(n).label(edgeV.getEdge().getTenant())
                    .relation(l).type(edgeV.getEdge().getType()).out().node(m).label(edgeV.getEdge().getTenant())
                    .relation(r).type(edgeV.getEdge().getType()).in().node(b).label(edgeV.getEdge().getTenant()));
        }
        Concatenator concatenator = WHERE.valueOf(n.property(ID)).EQUALS(vertexId);
        if(null != edgeV.getEdge().getProperties() && edgeV.getEdge().getProperties().size() > 0){
            for(Map.Entry entry :  edgeV.getEdge().getProperties().entrySet()){
                String key = (String)entry.getKey();
                Object value = entry.getValue();
                concatenator.AND().valueOf(l.property(key)).EQUALS(value);
                concatenator.AND().valueOf(r.property(key)).EQUALS(value);
            }
        }
        list.add(concatenator);
        list.add(WITH.value(m));
        list.add(WITH.value(b));
        list.add(WHERE.valueOf(n.property(ID)).NOT_EQUALS(b.property(ID)));

        IClause[] queryIClauses = list.toArray(new IClause[list.size()]);
        jcQuery.setClauses(queryIClauses);

        String cypher = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
        cypher = cypher.replace(CYPHER_PLANNER,"");
        StringBuilder queryBuilder = new StringBuilder(cypher);
        StringBuilder totalBuilder = new StringBuilder(cypher);
        queryBuilder.append(" AND not(n)-[:").
                append(edgeV.getEdge().getType())
                .append("]->(b) return distinct b, count(distinct m) as count order by count desc skip ")
                .append(validateUtil.graphDataSkip(edgeV.getSkip()))
                .append(" limit ")
                .append(validateUtil.graphDataLimit(edgeV.getLimit()));

        totalBuilder.append(" AND not(n)-[:").
                append(edgeV.getEdge().getType())
                .append("]->(b) return count(distinct b) as total;");

        try{
            Result qResult = session.query(queryBuilder.toString(),new HashMap<String,Object>());
            Result tResult = session.query(totalBuilder.toString(),new HashMap<String,Object>());
            Map<String,Object> dataMap = new HashMap<String,Object>();
            if(qResult.iterator().hasNext() && tResult.iterator().hasNext()){
                List<Map<String,Object>> dataList = Util.properties2Map(qResult,"b","count");
                Map<String,Object> temp = tResult.iterator().next();
                dataMap.put(TOTAL,Integer.valueOf(String.valueOf(temp.get("total"))));
                dataMap.put(NODES,dataList);
                queryResult.setData(dataMap);
                queryResult.setCode(Code.CO_37.getId());
                queryResult.setMessage(Code.CO_37.getValue());
                return queryResult;
            }else{
                dataMap.put(TOTAL,0);
                dataMap.put(NODES,new ArrayList<String>());
                queryResult.setData(dataMap);
                queryResult.setCode(Code.CO_42.getId());
                queryResult.setMessage(Code.CO_42.getValue());
                return queryResult;
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            queryResult.setCode(Code.EX_00.getId());
            queryResult.setMessage(Code.EX_00.getValue());
            return queryResult;
        }
    }
}
