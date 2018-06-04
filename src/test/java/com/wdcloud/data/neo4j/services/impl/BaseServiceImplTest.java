package com.wdcloud.data.neo4j.services.impl;

import com.alibaba.fastjson.JSON;
import com.wdcloud.data.neo4j.code.Check;
import com.wdcloud.data.neo4j.code.Code;
import com.wdcloud.data.neo4j.entity.*;
import com.wdcloud.data.neo4j.services.BaseService;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bigd on 2017/9/1.
 * service 接口测试类
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class BaseServiceImplTest {

    private Logger logger = LoggerFactory.getLogger(BaseServiceImplTest.class);

    @Autowired
    private BaseService baseService;

    @Autowired
    private Session session;

    final static int OutVertexCount = 30;

    final static int InVertexCount = 16;

    final static String StartVertexId = "VertexTest001";

    final static String Tenant = "VertexTest";

    final static String EndVertexIdPre = "VertexTestE00";

    final static String EdgeIdPre = "EdgeTest000";

    final static String Type = "EdgeTest";

    final static String SUCCESS = "SUCCESS";

    @Before
    public void setUp() throws Exception {
        PropertyConfigurator.configure("src/test/resources/conf/log4j.properties");
        System.err.println("###########################");
        /**
         * 新增顶点
         */
        Vertex vertex = new Vertex();
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("name","VertexTest");
        map.put("time",System.currentTimeMillis());
        vertex.setTenant(Tenant);
        vertex.setId(StartVertexId);
        vertex.setProperties(map);
        this.baseService.createVertex(vertex);

        Path path = new Path();
        Vertex end;
        Edge edge;
        for(int i = 0; i <= OutVertexCount + InVertexCount; i ++){
            end = new Vertex();
            edge = new Edge();
            end.setTenant(Tenant);
            end.setId(EndVertexIdPre + i);
            edge.setTenant(Tenant);
            edge.setType(Type);
            if(i <= OutVertexCount){
                path.setSource(vertex);
                path.setTarget(end);
            }else{
                path.setSource(end);
                path.setTarget(vertex);
            }
            path.setEdge(edge);
            baseService.createVertex(end);
            baseService.createEdgeNodeNonExistent(path);
        }
    }

    /**
     * 顶点创建测试：
     * 【顶点数据校验】、【唯一性校验】
     */
    @Test
    public void createVertex(){

        Vertex vertex = new Vertex();
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("name","VertexTest");
        map.put("time",System.currentTimeMillis());
        vertex.setTenant(Tenant);
        vertex.setId(StartVertexId);
        vertex.setProperties(map);
        QueryResult queryResult = this.baseService.createVertex(vertex);
        logger.info("※※※※※※※※※※※※RETURN MESSAGE ※※※※※※※※※");
        logger.info(JSON.toJSONString(queryResult.getData()));
        logger.info(queryResult.getMessage());
        Assert.assertEquals(queryResult.getCode(), Check.CH_01.getId());
    }

    /**
     * 顶点更新测试
     * 【顶点数据校验】、【顶点存在性校验】
     */
    @Test
    public void updateVertex(){

        Vertex vertex = new Vertex();
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("location","北京");
        map.put("name","属性覆盖");
        vertex.setTenant("VertexTest");
        vertex.setId("VertexTest001");
        vertex.setProperties(map);
        QueryResult queryResult = this.baseService.updateVertex(vertex);
        logger.info("※※※※※※※※※※※※RETURN MESSAGE ※※※※※※※※※");
        logger.error(JSON.toJSONString(queryResult.getData()));
        logger.info(queryResult.getMessage());
        Assert.assertEquals(queryResult.getCode(), Code.CO_03.getId());
    }

    /**
     * 获取顶点测试
     * 【数据校验】
     */
    @Test
    public void obtainVertex(){

        Vertex vertex = new Vertex();
        vertex.setId(StartVertexId);
        vertex.setTenant(Tenant);
        QueryResult queryResult = this.baseService.obtainVertex(vertex);
        logger.info("※※※※※※※※※※※※RETURN MESSAGE ※※※※※※※※※");
        logger.info(queryResult.getMessage());
        logger.error(JSON.toJSONString(queryResult.getData()));
        Assert.assertEquals(queryResult.getCode(), Code.CO_05.getId());
    }

    /**
     * 测试顶点的出向顶点  OUT
     *
     */
    @Test
    public void obtainVertexOutV(){

        VertexV vertexV = new VertexV();
        vertexV.setSourceId(StartVertexId);
        vertexV.setTenant(Tenant);
        vertexV.setSkip(0);
        vertexV.setLimit(18);
        QueryResult queryResult = this.baseService.obtainVertexOutV(vertexV);
        logger.info("※※※※※※※※※※※※RETURN MESSAGE ※※※※※※※※※");
        logger.info(queryResult.getMessage());
        logger.error(JSON.toJSONString(queryResult.getData()));
        Assert.assertEquals(queryResult.getCode(), Code.CO_09.getId());
    }

    /**
     * 测试顶点的入向顶点  IN
     */
    @Test
    public void obtainVertexInV(){

        VertexV vertexV = new VertexV();
        vertexV.setSourceId(StartVertexId);
        vertexV.setTenant(Tenant);
        vertexV.setSkip(0);
        vertexV.setLimit(5);
        QueryResult queryResult = this.baseService.obtainVertexInV(vertexV);
        logger.info("※※※※※※※※※※※※RETURN MESSAGE ※※※※※※※※※");
        logger.info(queryResult.getMessage());
        logger.error(JSON.toJSONString(queryResult.getData()));
        Assert.assertEquals(queryResult.getCode(), Code.CO_11.getId());
    }

    /**
     * 测试顶点双向顶点 BOTH
     */
    @Test
    public void obtainVertexBothV(){

        VertexV vertexV = new VertexV();
        vertexV.setSourceId(StartVertexId);
        vertexV.setTenant(Tenant);
        vertexV.setSkip(0);
        vertexV.setLimit(40);
        QueryResult queryResult = this.baseService.obtainVertexBothV(vertexV);
        logger.info("※※※※※※※※※※※※RETURN MESSAGE ※※※※※※※※※");
        logger.info(queryResult.getMessage());
        logger.error(JSON.toJSONString(queryResult.getData()));
        Assert.assertEquals(queryResult.getCode(), Code.CO_13.getId());
    }

    /**
     * 测试顶点出向顶点 id 列表  OUT
     */
    @Test
    public void obtainVertexOutIds(){

        VertexV vertexV = new VertexV();
        vertexV.setSourceId(StartVertexId);
        vertexV.setTenant(Tenant);
        vertexV.setSkip(0);
        vertexV.setLimit(40);
        QueryResult queryResult = this.baseService.obtainVertexOutIds(vertexV);
        logger.info("※※※※※※※※※※※※RETURN MESSAGE ※※※※※※※※※");
        logger.info(queryResult.getMessage());
        logger.error(JSON.toJSONString(queryResult.getData()));
        Assert.assertEquals(queryResult.getCode(), Code.CO_15.getId());
    }


    /**
     * 测试顶点出向顶点 id 列表  IN
     */
    @Test
    public void obtainVertexInIds(){

        VertexV vertexV = new VertexV();
        vertexV.setSourceId(StartVertexId);
        vertexV.setTenant(Tenant);
        vertexV.setSkip(0);
        vertexV.setLimit(5);
        QueryResult queryResult = this.baseService.obtainVertexInIds(vertexV);
        logger.info("※※※※※※※※※※※※RETURN MESSAGE ※※※※※※※※※");
        logger.info(queryResult.getMessage());
        logger.error(JSON.toJSONString(queryResult.getData()));
        Assert.assertEquals(queryResult.getCode(), Code.CO_17.getId());
    }

    /**
     * 测试顶点出向顶点 id 列表  BOTH
     */
    @Test
    public void obtainVertexBothIds(){

        VertexV vertexV = new VertexV();
        vertexV.setSourceId(StartVertexId);
        vertexV.setTenant(Tenant);
        vertexV.setSkip(0);
        vertexV.setLimit(40);
        QueryResult queryResult = this.baseService.obtainVertexBothIds(vertexV);
        logger.info("※※※※※※※※※※※※RETURN MESSAGE ※※※※※※※※※");
        logger.info(queryResult.getMessage());
        logger.error(JSON.toJSONString(queryResult.getData()));
        Assert.assertEquals(queryResult.getCode(), Code.CO_19.getId());
    }

    /**
     * 测试移除顶点 & 顶点相关联的边
     */
    @Test
    public void removeVertexEdges(){

        Vertex vertex = new Vertex();
        vertex.setTenant(Tenant);
        vertex.setId(StartVertexId);
        QueryResult queryResult = this.baseService.removeVertexEdges(vertex);
        logger.info("※※※※※※※※※※※※RETURN MESSAGE ※※※※※※※※※");
        logger.info(queryResult.getMessage());
        logger.error(JSON.toJSONString(queryResult.getData()));
        Assert.assertEquals(queryResult.getCode(), Code.CO_07.getId());
    }

    /**
     * 测试创建已存在顶点间的边
     */
    @Test
    public void createEdge(){

        Path path = new Path();

        Vertex start = new Vertex();
        start.setTenant(Tenant);
        start.setId(StartVertexId);

        Vertex end = new Vertex();
        end.setTenant(Tenant);
        end.setId(EndVertexIdPre + 10);

        Edge edge = new Edge();
        Map<String,Object> properties = new HashMap<String,Object>();
        properties.put("location","shanghai");
        edge.setProperties(properties);
        edge.setTenant(Tenant);
        edge.setType(Type);

        path.setEdge(edge);
        path.setSource(start);
        path.setTarget(end);
        QueryResult queryResult = this.baseService.createEdgeNodeNonExistent(path);
        logger.info("※※※※※※※※※※※※RETURN MESSAGE ※※※※※※※※※");
        logger.info(queryResult.getMessage());
        logger.error(JSON.toJSONString(queryResult.getData()));
        Assert.assertEquals(queryResult.getCode(), Code.CO_20.getId());
    }

    @Test
    public void removeEdge(){

        Edge edge = new Edge();
//        edge.setId(EdgeIdPre + 10);
        edge.setTenant(Tenant);
        QueryResult queryResult = this.baseService.removeEdge(edge);
        logger.info("※※※※※※※※※※※※RETURN MESSAGE ※※※※※※※※※");
        logger.info(queryResult.getMessage());
        logger.error(JSON.toJSONString(queryResult.getData()));
        Assert.assertEquals(queryResult.getCode(), Code.CO_23.getId());
    }

    @Test
    public void obtainEdge(){
        Edge edge = new Edge();
//    	edge.setId("edge001");
        edge.setTenant("Dept");
        QueryResult queryResult = this.baseService.obtainEdge(edge);
        logger.info("※※※※※※※※※※※※RETURN MESSAGE ※※※※※※※※※");
        logger.info(queryResult.getMessage());
        logger.error(JSON.toJSONString(queryResult.getData()));
        Assert.assertEquals(queryResult.getCode(), SUCCESS);
    }

    @Test
    public void obtainEdgeBetweenVertex(){
        Vertex start = new Vertex();
        start.setId("002");
        start.setTenant("Dept");

        Vertex end = new Vertex();
        end.setId("001");
        end.setTenant("Dept");

        QueryResult queryResult = this.baseService.obtainEdgeBetweenVertex(start, end, 0, 20);
        logger.info("※※※※※※※※※※※※RETURN MESSAGE ※※※※※※※※※");
        logger.info(queryResult.getMessage());
        logger.error(JSON.toJSONString(queryResult.getData()));
        Assert.assertEquals(queryResult.getCode(), SUCCESS);
    }

    @Test
    public void obtainEdgeIdsBetweenVertex(){
        Vertex start = new Vertex();
        start.setId("002");
        start.setTenant("Dept");

        Vertex end = new Vertex();
        end.setId("001");
        end.setTenant("Dept");

        QueryResult queryResult = this.baseService.obtainEdgeIdsBetweenVertex(start, end, 0, 20);
        logger.info("※※※※※※※※※※※※RETURN MESSAGE ※※※※※※※※※");
        logger.info(queryResult.getMessage());
        logger.error(JSON.toJSONString(queryResult.getData()));
        Assert.assertEquals(queryResult.getCode(), SUCCESS);
    }

    @Test
    public void updateEdge(){
        Edge edge = new Edge();

        Map<String,Object> properties = new HashMap<String,Object>();
        properties.put("location","shanghai");
        properties.put("age", 12);

//        edge.setId("edge001");
        edge.setTenant("Dept");
        edge.setProperties(properties);

        QueryResult queryResult = this.baseService.updateEdge(edge);
        logger.info("※※※※※※※※※※※※RETURN MESSAGE ※※※※※※※※※");
        logger.info(queryResult.getMessage());
        logger.error(JSON.toJSONString(queryResult.getData()));
        Assert.assertEquals(queryResult.getCode(), SUCCESS);
    }

    @After
    public void tearDown() {
        session.purgeDatabase();
    }

    @Test
    public void createEdgeBath(){

        Path path = new Path();
        Vertex start = new Vertex();
        Vertex end = new Vertex();
        Edge edge = new Edge();
        Map<String,Object> properties = new HashMap<String,Object>();
        properties.put("location","shanghai");
        edge.setProperties(properties);


        edge.setType(Type);
        start.setTenant(Tenant);
        end.setTenant(Tenant);
        edge.setTenant(Tenant);
        final int count= 1000000;
        String prexFixStart = "Start";
        String prexFixEnd = "End";
        for(int i = 0 ;i < count; i ++){
            start.setId(prexFixStart + String.format("%09d", i));
            end.setId(prexFixEnd + String.format("%09d", i));
            path.setEdge(edge);
            path.setSource(start);
            path.setTarget(end);
            this.baseService.createEdgeNodeNonExistent(path);
            logger.error("count ※※※※※※※※" + i);
        }
    }


}