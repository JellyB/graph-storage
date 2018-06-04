package com.wdcloud.data.neo4j.service;

import com.alibaba.fastjson.JSON;
import com.wdcloud.data.neo4j.entity.QueryResult;
import com.wdcloud.data.neo4j.entity.Vertex;
import com.wdcloud.data.neo4j.services.BaseService;
import com.wdcloud.data.neo4j.services.impl.BaseServiceImplTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bigd on 2017/9/6.
 * 批量新增测试数据
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BatchDataTest {


    private Logger logger = LoggerFactory.getLogger(BaseServiceImplTest.class);

    static final String VERTEX_START_PRE = "Vertex";

    static final String VERTEX_END_PRE = "end";

    static  final String TENANT = "tenant_A";
    @Autowired
    private BaseService baseService;

    @Autowired
    private Session session;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() {
        session.purgeDatabase();
    }


    @Test
    public void batchStartVertex() throws  Exception{

        int  start = 9999;
        int  end = 1000000;
        Vertex vertex;
        Map<String,Object> properties;
        for(int i = start ; i <= end ; i ++){
            properties = new HashMap<String, Object>();
            properties.put("name","name" + String.format("%09d",i));
            properties.put("age",i);
            vertex = new Vertex();
            vertex.setId(VERTEX_START_PRE + String.format("%09d",i));
            vertex.setTenant(TENANT);
            vertex.setProperties(properties);
            QueryResult result = baseService.createVertex(vertex);
            logger.error(JSON.toJSONString(result));
            logger.error("current vertex id:" + String.format("%09d",i));
        }
    }

}
