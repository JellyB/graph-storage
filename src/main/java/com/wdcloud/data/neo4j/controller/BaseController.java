package com.wdcloud.data.neo4j.controller;

import com.wdcloud.data.neo4j.code.Code;
import com.wdcloud.data.neo4j.entity.*;
import com.wdcloud.data.neo4j.services.BaseService;
import com.wdcloud.data.neo4j.util.ValidateUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * Created by bigd on 2017/6/26.
 * 图存储服务基础api
 */

@Controller
@RequestMapping(value = "/graph/storage/v1")
public class BaseController {

    static final String RES_TENANT = "apptenant";

    @Autowired
    private BaseService baseService;

    @Autowired
    private ValidateUtil validateUtil;
    
    /**
     * 添加顶点
     * @param vertex 顶点
     * @param tenant 租户
     * @return 执行结果
     */
    @RequestMapping(value ="/vertex",method = RequestMethod.POST,produces = "application/json")
    @ResponseBody
    public QueryResult createVertex(@RequestBody(required = false) Vertex vertex, @RequestHeader(value = RES_TENANT) String tenant){
        QueryResult queryResult = new QueryResult();
        if(null == vertex){
            vertex = new Vertex();
        }
        try{
            vertex.setTenant(tenant);
            queryResult = baseService.createVertex(vertex);
            return queryResult;
        }catch (Exception e){
            queryResult.setCode(Code.CO_00.getId());
            queryResult.setMessage(Code.CO_00.getValue());
            queryResult.setData(new HashMap<String,Object>());
            return queryResult;
        }
    }


    /**
     * 更新顶点
     * @param id 指定租户下顶点唯一标识
     * @param vertex 顶点对象
     * @param tenant 租户
     * @return 执行结果
     */
    @RequestMapping(value = "/vertex/{v0}",method = RequestMethod.PUT,produces = "application/json")
    @ResponseBody
    public QueryResult updateVertex(@PathVariable("v0") String id, @RequestBody Vertex vertex, @RequestHeader(RES_TENANT) String tenant){
        QueryResult queryResult = new QueryResult();
        if(null == vertex){
            vertex = new Vertex();
        }
        try{
            vertex.setId(id);
            vertex.setTenant(tenant);
            queryResult = baseService.updateVertex(vertex);
            return queryResult;
        }catch (Exception e){
            queryResult.setCode(Code.CO_02.getId());
            queryResult.setMessage(Code.CO_02.getValue());
            queryResult.setData(new HashMap<String,Object>());
            return queryResult;
        }
    }


    /**
     *  获取顶点
     * @param id 指定租户下顶点唯一标识
     * @param tenant 租户
     * @return 执行结果对象
     */
    @RequestMapping(value = "/vertex/{v0}",method = RequestMethod.GET,produces = "application/json")
    @ResponseBody
    public QueryResult obtainVertex(@PathVariable(value = "v0") String id,@RequestHeader(RES_TENANT) String tenant){
        Vertex vertex = new Vertex();
        vertex.setId(id);
        vertex.setTenant(tenant);
        return baseService.obtainVertex(vertex);
    }


    /**
     * 获取相邻的出向顶点及顶点属性
     * @param id 指定租户下唯一标识
     * @param tenant 租户
     * @return 执行结果对象
     */
    @RequestMapping(value = "/vertex/{v0}/out",method = RequestMethod.POST,produces = "application/json")
    @ResponseBody
    public QueryResult obtainVertexOutV(@PathVariable("v0") String id, @RequestBody(required = false) VertexV vertexV, @RequestHeader(RES_TENANT) String tenant){
        if(null == vertexV){
            vertexV = new VertexV();
        }
        vertexV.setSourceId(id);
        vertexV.setTenant(tenant);
        return baseService.obtainVertexOutV(vertexV);
    }

    /**
     * 获取相邻的入向顶点及顶点属性
     * @param id 指定租户下顶点唯一标识
     * @param tenant 租户
     * @return 执行结果对象
     */
    @RequestMapping(value = "/vertex/{v0}/in",method = RequestMethod.POST,produces = "application/json")
    @ResponseBody
    public QueryResult obtainVertexInV(@PathVariable("v0") String id, @RequestBody(required = false) VertexV vertexV, @RequestHeader(RES_TENANT) String tenant){
        if(null == vertexV){
            vertexV = new VertexV();
        }
        vertexV.setSourceId(id);
        vertexV.setTenant(tenant);
        return baseService.obtainVertexInV(vertexV);
    }

    /**
     * 获取相邻的双向顶点及顶点属性
     * @param id 指定租户下顶点唯一标识
     * @param tenant 租户
     * @return 执行结果对象
     */
    @RequestMapping(value = "/vertex/{v0}/both",method = RequestMethod.POST,produces = "application/json")
    @ResponseBody
    public QueryResult obtainVertexBothV(@PathVariable("v0") String id,  @RequestBody VertexV vertexV, @RequestHeader(RES_TENANT) String tenant){
        if(null == vertexV){
            vertexV = new VertexV();
        }
        vertexV.setSourceId(id);
        vertexV.setTenant(tenant);
        return baseService.obtainVertexBothV(vertexV);
    }


    /**
     * 获取相邻出向顶点的id列表
     * @param id 指定租户下顶点唯一标识
     * @param tenant 租户
     * @return 执行结果对象
     */
    @RequestMapping(value = "/vertex/{v0}/outIds",method = RequestMethod.POST,produces = "application/json")
    @ResponseBody
    public QueryResult obtainVertexOutIds(@PathVariable("v0") String id, @RequestBody VertexV vertexV, @RequestHeader(RES_TENANT) String tenant){
        if(null == vertexV){
            vertexV = new VertexV();
        }
        vertexV.setSourceId(id);
        vertexV.setTenant(tenant);
        return baseService.obtainVertexOutIds(vertexV);
    }


    /**
     * 获取相邻入向顶点的id列表
     * @param id 指定租户下顶点的唯一标识
     * @param tenant 租户
     * @return 执行结果对象
     */
    @RequestMapping(value = "/vertex/{v0}/inIds",method = RequestMethod.POST,produces = "application/json")
    @ResponseBody
    public QueryResult obtainVertexInIds(@PathVariable("v0") String id,  @RequestBody VertexV vertexV,@RequestHeader(RES_TENANT) String tenant){
        if(null == vertexV){
            vertexV = new VertexV();
        }
        vertexV.setSourceId(id);
        vertexV.setTenant(tenant);
        return baseService.obtainVertexInIds(vertexV);
    }


    /**
     * 获取相邻双向顶点的id列表
     * @param id 指定租户下顶点的唯一标识
     * @param tenant 租户
     * @return 执行结果对象
     */
    @RequestMapping(value = "/vertex/{v0}/bothIds",method = RequestMethod.POST,produces = "application/json")
    @ResponseBody
    public QueryResult obtainVertexBothIds(@PathVariable("v0") String id, @RequestBody VertexV vertexV, @RequestHeader(RES_TENANT) String tenant){
        if(null == vertexV){
            vertexV = new VertexV();
        }
        vertexV.setSourceId(id);
        vertexV.setTenant(tenant);
        return baseService.obtainVertexBothIds(vertexV);
    }


    /**
     * 移除顶点及顶点相关联的边
     * @param id 指定租户下顶点的唯一标识
     * @param tenant 租户
     * @return 执行结果对象
     */
    @RequestMapping(value = "/vertex/{v0}",method = RequestMethod.DELETE,produces = "application/json")
    @ResponseBody
    public QueryResult removeVertexEdges(@PathVariable("v0") String id, @RequestHeader(RES_TENANT) String tenant){
        QueryResult queryResult = new QueryResult();
        try{
            Vertex vertex = new Vertex();
            vertex.setId(id);
            vertex.setTenant(tenant);
            queryResult = baseService.removeVertexEdges(vertex);
            return queryResult;
        }catch (Exception e){
            queryResult.setCode(Code.CO_06.getId());
            queryResult.setMessage(Code.CO_06.getValue());
            queryResult.setData(new HashMap<String,Object>());
            return queryResult;
        }
    }


    /**
     * 创建两个顶点间的边,边上的顶点可能不存在
     * @param path path 对象包含顶点及边属性
     * @param tenant 租户
     * @return 执行结果对象
     */
    @RequestMapping(value = "/edge/non-existent/node",method = RequestMethod.POST,produces = "application/json")
    @ResponseBody
    public QueryResult createEdgeNodeNonExistent(@RequestBody Path path, @RequestHeader(RES_TENANT) String tenant){
        QueryResult queryResult = new QueryResult();
        try{
            if(null != path.getSource()){
                path.getSource().setTenant(tenant);
            }
            if(null != path.getTarget()){
                path.getTarget().setTenant(tenant);
            }
            if(null != path.getEdge()){
                path.getEdge().setTenant(tenant);
            }
            queryResult = baseService.createEdgeNodeNonExistent(path);
            return queryResult;
        }catch (Exception e){
            queryResult.setCode(Code.CO_21.getId());
            queryResult.setMessage(Code.CO_21.getValue());
            queryResult.setData(new HashMap<String,Object>());
            return queryResult;
        }
    }

    /**
     * 创建两个顶点间的边
     * @param relationShip 关系对象
     * @param tenant 租户
     * @return 执行结果对象
     */
    @RequestMapping(value = "/edge",method = RequestMethod.POST,produces = "application/json")
    @ResponseBody
    public QueryResult createEdge(@RequestBody RelationShip relationShip, @RequestHeader(RES_TENANT) String tenant){
        QueryResult queryResult = new QueryResult();
        try{
            if(null == relationShip.getEdge())
                relationShip.setEdge(new Edge());
            relationShip.getEdge().setTenant(tenant);
            queryResult = baseService.createEdge(relationShip);
            return queryResult;
        }catch (Exception e){
            queryResult.setCode(Code.CO_21.getId());
            queryResult.setMessage(Code.CO_21.getValue());
            queryResult.setData(new HashMap<String,Object>());
            return queryResult;
        }
    }


    /**
     * 移除两个顶点间的边
     * @param id 指定租户下顶点的唯一标识
     * @param tenant 租户
     * @return 执行结果对象
     */
    @RequestMapping(value = "/edge/{e0}",method = RequestMethod.DELETE,produces = "application/json")
    @ResponseBody
    public QueryResult removeEdge(@PathVariable("e0") long id,@RequestHeader(RES_TENANT) String tenant){
        QueryResult queryResult = new QueryResult();
        try{
            Edge edge = new Edge();
            edge.setId(id);
            edge.setTenant(tenant);
            queryResult =  baseService.removeEdge(edge);
            return queryResult;
        }catch (Exception e){
            queryResult.setCode(Code.CO_22.getId());
            queryResult.setMessage(Code.CO_22.getValue());
            queryResult.setData(new HashMap<String,Object>());
            return queryResult;
        }
    }
    
    /**
     *  获取边
     * @param id 指定租户下顶点唯一标识
     * @param tenant 租户
     * @return 执行结果对象
     */
    @RequestMapping(value = "/edge/{e0}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public QueryResult obtainEdge(@PathVariable(value = "e0") long id, @RequestHeader(RES_TENANT) String tenant){
    	Edge edge = new Edge();
    	edge.setId(id);
    	edge.setTenant(tenant);
        return baseService.obtainEdge(edge);
    }
    
    /**
     * 获取任意两节点之间的边
     * @param id1 节点1的ID
     * @param id2 节点2的ID
     * @param tenant 租户
     * @return 执行结果对象
     */
    @RequestMapping(value = "/edge/between/vertex/{v1}/{v2}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public QueryResult obtainEdgeBetweenVertex(@PathVariable(value = "v1") String id1, @PathVariable(value = "v2") String id2, @RequestHeader(RES_TENANT) String tenant, Integer skip, Integer limit){
    	Vertex vertex1 = new Vertex();
    	vertex1.setId(id1);
    	vertex1.setTenant(tenant);
    	Vertex vertex2 = new Vertex();
    	vertex2.setId(id2);
    	vertex2.setTenant(tenant);
    	if(null == skip){
    		skip = validateUtil.getDefaultSkip();
    	}
    	if(null == limit) {
    		limit = validateUtil.getDefaultLimit();
    	}
    	return baseService.obtainEdgeBetweenVertex(vertex1, vertex2, skip, limit);
    }
    
    /**
     * 获取任意两节点之间的边IDS
     * @param id1 节点1的ID
     * @param id2 节点2的ID
     * @param tenant 租户
     * @return 执行结果对象
     */
    @RequestMapping(value = "/edgeIds/between/vertex/{v1}/{v2}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public QueryResult obtainEdgeIdsBetweenVertex(@PathVariable(value = "v1") String id1, @PathVariable(value = "v2") String id2, @RequestHeader(RES_TENANT) String tenant, Integer skip, Integer limit){
    	Vertex vertex1 = new Vertex();
    	vertex1.setId(id1);
    	vertex1.setTenant(tenant);
    	Vertex vertex2 = new Vertex();
    	vertex2.setId(id2);
    	vertex2.setTenant(tenant);
    	if(null == skip){
    		skip = validateUtil.getDefaultSkip();
    	}
    	if(null == limit) {
    		limit = validateUtil.getDefaultLimit();
    	}
    	return baseService.obtainEdgeIdsBetweenVertex(vertex1, vertex2, skip, limit);
    }
    
    /**
     * 修改边属性
     * @param id 边的唯一标识
     * @param tenant 租户
     * @param edge 边对象
     * @return 执行结果对象
     */
    @RequestMapping(value = "/edge/{e0}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public QueryResult updateEdge(@PathVariable(value = "e0") long id, @RequestHeader(RES_TENANT) String tenant, @RequestBody(required=false) Edge edge) {
    	QueryResult queryResult = new QueryResult();
        try{
        	if(null == edge) {
        		edge = new Edge();
        	}
        	edge.setId(id);
        	edge.setTenant(tenant);
            queryResult = baseService.updateEdge(edge);
            return queryResult;
        }catch (Exception e){
            queryResult.setCode(Code.CO_30.getId());
            queryResult.setMessage(Code.CO_30.getValue());
            return queryResult;
        }
    }
    
    
    /**
     * 根据顶点查询双向边
     * @param id	顶点的唯一标识
     * @param tenant	租户
     * @return
     */
    @RequestMapping(value = "/vertex/{v0}/edge/both", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public QueryResult obtainBothEdgeByVertex(@PathVariable(value = "v0") String id, @RequestHeader(RES_TENANT) String tenant, @RequestBody(required=false) EdgeV edgeV) {
    	QueryResult queryResult = new QueryResult();
    	Vertex vertex = new Vertex();
    	vertex.setId(id);
    	vertex.setTenant(tenant);
    	if(null == edgeV) {
    		edgeV = new EdgeV();
    	}
    	if(null != edgeV.getEdge()) {
    		edgeV.getEdge().setTenant(tenant);
    	}
    	queryResult = baseService.obtainBothEdgeByVertex(vertex, edgeV);
    	return queryResult;
    }
    
    /**
     * 根据顶点查询双入边
     * @param id	顶点的唯一标识
     * @param tenant	租户
     * @return
     */
    @RequestMapping(value = "/vertex/{v0}/edge/in", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public QueryResult obtainInEdgeByVertex(@PathVariable(value = "v0") String id, @RequestHeader(RES_TENANT) String tenant, @RequestBody(required=false) EdgeV edgeV) {
    	QueryResult queryResult = new QueryResult();
    	Vertex vertex = new Vertex();
    	vertex.setId(id);
    	vertex.setTenant(tenant);
    	if(null == edgeV) {
    		edgeV = new EdgeV();
    	}
    	if(null != edgeV.getEdge()) {
    		edgeV.getEdge().setTenant(tenant);
    	}
    	queryResult = baseService.obtainInEdgeByVertex(vertex, edgeV);
    	return queryResult;
    }
    
    /**
     * 根据顶点查询双出边
     * @param id	顶点的唯一标识
     * @param tenant	租户
     * @return
     */
    @RequestMapping(value = "/vertex/{v0}/edge/out", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public QueryResult obtainOutEdgeByVertex(@PathVariable(value = "v0") String id, @RequestHeader(RES_TENANT) String tenant, @RequestBody(required=false) EdgeV edgeV) {
    	QueryResult queryResult = new QueryResult();
    	Vertex vertex = new Vertex();
    	vertex.setId(id);
    	vertex.setTenant(tenant);
    	if(null == edgeV) {
    		edgeV = new EdgeV();
    	}
    	if(null != edgeV.getEdge()) {
    		edgeV.getEdge().setTenant(tenant);
    	}
    	queryResult = baseService.obtainOutEdgeByVertex(vertex, edgeV);
    	return queryResult;
    }

    @RequestMapping(value = "/vertex/{v0}/same/neighbors",method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public QueryResult sameNeighbors(@PathVariable(value = "v0") String vertexId, @RequestHeader(RES_TENANT) String tenant, @RequestBody EdgeV edgeV){
        QueryResult queryResult = new QueryResult();
        try{
            if(null == edgeV.getEdge()){
                edgeV.setEdge(new Edge());
            }
            edgeV.getEdge().setTenant(tenant);
            queryResult = baseService.sameNeighbors(vertexId, edgeV);
        }catch (Exception e){
            queryResult.setCode(Code.CO_38.getId());
            queryResult.setMessage(Code.CO_38.getValue());
            queryResult.setData(new HashMap<String,Object>());
            return queryResult;
        }
        return queryResult;
    }
}
