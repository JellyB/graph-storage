package com.wdcloud.data.neo4j.service;

import com.alibaba.fastjson.JSON;
import com.wdcloud.data.neo4j.code.Code;
import com.wdcloud.data.neo4j.entity.QueryResult;
import com.wdcloud.data.neo4j.entity.Vertex;
import com.wdcloud.data.neo4j.services.BaseService;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bigd on 2017/7/21.
 * 元数据测试：
 * 1、支持的基础数据类型
 * 2、事务测试-回滚
 * 3、索引测试
 * 4、条件约束测试
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MetadataTest {

    private Logger logger = LoggerFactory.getLogger(MetadataTest.class);


    @Autowired
    private Session session;

    @Autowired
    private BaseService baseService;

    @Before
    public void setUp() throws Exception {
        PropertyConfigurator.configure("src/test/resources/conf/log4j.properties");
    }

    @Test
    public void testRollBack(){
        String cypher = "Create(n:RollBack) set n.name={name} set n.id={id} " +
                "set n.age = {age} return n";
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("id","Rollback001");
        parameters.put("name","Rollback");
        parameters.put("age",13);
        Result result = session.query(cypher,parameters);
        //session.getTransaction().rollback();
    }

    /**
     * 2、事务测试-RuntimeException 异常回滚
     * link http://blog.csdn.net/catoop/article/details/50595702
     */
    @Test
    public void createVertex(){

        Vertex vertex = new Vertex();
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("name","VertexTest");
        map.put("time",System.currentTimeMillis());
        vertex.setTenant("RollBack");
        vertex.setId("RollBack001");
        vertex.setProperties(map);
        QueryResult queryResult = this.baseService.createVertex(vertex);
        logger.info("※※※※※※※※※※※※RETURN MESSAGE ※※※※※※※※※");
        logger.info(JSON.toJSONString(queryResult.getData()));
        logger.info(queryResult.getMessage());
        Assert.assertEquals(queryResult.getCode(), Code.CO_01.getId());
    }


    @After
    public void tearDown() {
        //session.getTransaction().commit();
    }

}
