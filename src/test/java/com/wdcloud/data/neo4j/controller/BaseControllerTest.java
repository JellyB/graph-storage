package com.wdcloud.data.neo4j.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wdcloud.data.neo4j.code.Check;
import com.wdcloud.data.neo4j.code.Code;
import com.wdcloud.data.neo4j.code.Validate;
import com.wdcloud.data.neo4j.entity.*;
import com.wdcloud.data.neo4j.services.BaseService;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigInteger;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by bigd on 2017/7/24.
 * 基础服务接口controller测试代码
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class BaseControllerTest {

    private Logger logger = LoggerFactory.getLogger(BaseControllerTest.class);

    final static String APPTENANT = "apptenant";

    final static int OutVertexCount = 20;

    final static int InVertexCount =  3;

    final static int MayBeVertexCount = 5;

    final static String ID = "id";

    final static String PROPERTIES = "properties";

    final static String HEADER_VALUE = "ControllerTest";

    final static String EndVertexIdPre = "VertexTestE00";

    final static String MayBeVertexIdPre = "VertexTestM00";

    final static String Type = "ControllerTest";

    final static String SUCCESS = "SUCCESS";

    final static String SOURCEID = "sourceId";

    final static String TARGETID = "targetId";

    final static String LIMIT = "limit";

    final static String SKIP = "skip";


    @Value("${graph.node.id.length}")
    private  String nodeId;

    @Value("${graph.edge.id.length}")
    private String edgeId;

    @Value("${graph.edge.type.length}")
    private String edgeType;

    @Value("${graph.tenant.length}")
    private String tenant;

    @Value("${graph.key.length}")
    private String keyLength;

    @Value("${graph.value.length}")
    private String valueLength;

    private BigInteger maxBigInteger;

    private BigInteger minBigInteger;

    final static ArrayList<String> SYSTEM_RESERVED = new ArrayList<String>(
            Arrays.asList("id","namespace","createTime","updateTime"));

    final String VertexNameKey = "name";

    final String VertexNameValue = "VertexTest";

    final String VertexTimeKey = "time";

    final long VertexTimeValue = System.currentTimeMillis();

    final String VertexId = "123456";
    /**
     * 注入WebApplicationContext
     */
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private Session session;

    @Autowired
    private BaseService baseService;

    /**
     * 模拟MVC对象，通过MockMvcBuilders.webAppContextSetup(this.wac).build()初始化。
     */
    private MockMvc mockMvc;

    private ArrayList<Long> relationShipIdList;

    @Before
    public void setUp() throws Exception {
        relationShipIdList = new ArrayList<Long>();
        nodeId = getRandomString(Integer.valueOf(nodeId));
        edgeId = getRandomString(Integer.valueOf(edgeId));
        edgeType = getRandomString(Integer.valueOf(edgeType));
        tenant = getRandomString(Integer.valueOf(tenant));
        keyLength = getRandomString(Integer.valueOf(keyLength));
        valueLength = getRandomString(Integer.valueOf(valueLength));
        maxBigInteger = new BigInteger(String.valueOf(Long.MAX_VALUE)).add(new BigInteger("1"));
        minBigInteger = new BigInteger(String.valueOf(Long.MIN_VALUE)).subtract(new BigInteger("1"));
        PropertyConfigurator.configure("src/test/resources/conf/log4j.properties");
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        /**初始化数据
         * 默认初始化顶点-id：123456
         */
        Vertex vertex = new Vertex();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(VertexNameKey, VertexNameValue);
        map.put(VertexTimeKey, VertexTimeValue);
        vertex.setTenant(HEADER_VALUE);
        vertex.setId(VertexId);
        vertex.setProperties(map);
        this.baseService.createVertex(vertex);

        /**
         * 两顶点之间没有关系
         */
        Vertex vertex2 = new Vertex();
        vertex2.setTenant(HEADER_VALUE);
        vertex2.setId("startId0002");
        this.baseService.createVertex(vertex2);

        Vertex vertex3 = new Vertex();
        vertex3.setTenant(HEADER_VALUE);
        vertex3.setId("startId0003");
        this.baseService.createVertex(vertex3);

        /**
         * 初始边
         */
        Path path = new Path();
        Edge edge = new Edge();
        Vertex start = new Vertex();
        Vertex end = new Vertex();
        Vertex mayBe;

        start.setId("startId0001");
        end.setId("endId0001");
        start.setTenant(HEADER_VALUE);
        end.setTenant(HEADER_VALUE);
        edge.setType(Type);
        edge.setTenant(HEADER_VALUE);
        path.setSource(start);
        path.setTarget(end);
        path.setEdge(edge);
        QueryResult queryResult = this.baseService.createEdgeNodeNonExistent(path);
        Map<String,Object> resultMap = (Map<String,Object>)queryResult.getData();        ;
        long relationShipIdLong = Long.valueOf(String.valueOf(resultMap.get(ID)));
        relationShipIdList.add(relationShipIdLong);
        
        /**
         * 两个顶点之间随机关系
         */
        Map<String,Object> properties = new HashMap<String, Object>();
        properties.put("age",19);
        Map<String,Object> endProperties;
        for(int i = 1; i <= OutVertexCount + InVertexCount; i ++){
            end = new Vertex();
            edge = new Edge();
            endProperties = new HashMap<String, Object>();
            endProperties.put(VertexNameKey,VertexNameValue + "**" + i);
            endProperties.put(VertexTimeKey,System.currentTimeMillis());
            end.setTenant(HEADER_VALUE);
            end.setId(EndVertexIdPre + i);
            end.setProperties(endProperties);
            edge.setTenant(HEADER_VALUE);
            edge.setType(Type);
            edge.setProperties(properties);
            if(i <= OutVertexCount){
                path.setSource(vertex);
                path.setTarget(end);
            }else{
                path.setSource(end);
                path.setTarget(vertex);
            }
            path.setEdge(edge);
            QueryResult queryResult1 = baseService.createEdgeNodeNonExistent(path);
            Map<String,Object> resultMap2 = (Map<String,Object>)queryResult1.getData();        ;
            long relationShipIdLong2 = Long.valueOf(String.valueOf(resultMap2.get(ID)));
            relationShipIdList.add(relationShipIdLong2);
        }
        /**
         * 可能认识的人
         */
        HashSet<Integer> intSet =  new HashSet<Integer>();
        for(int i = 1; i <= MayBeVertexCount; i ++){
            end = new Vertex();
            edge = new Edge();
            mayBe = new Vertex();
            Random random = new Random();
            int count = random.nextInt(OutVertexCount) <= 3 ? 3 : random.nextInt(OutVertexCount);
            for(int j = 1 ; j <= count ; j ++){
                intSet.add(new Random().nextInt(OutVertexCount));
            }

            mayBe.setId(MayBeVertexIdPre + i);
            mayBe.setTenant(HEADER_VALUE);
            edge.setTenant(HEADER_VALUE);
            edge.setType(Type);
            for(Integer k : intSet){
                end.setId(EndVertexIdPre + k);
                end.setTenant(HEADER_VALUE);
                path.setEdge(edge);
                path.setSource(mayBe);
                path.setTarget(end);
                baseService.createEdgeNodeNonExistent(path);
            }
            intSet.clear();
        }
    }


    /**
     * 新增顶点正常
     *
     * @throws Exception
     */
    @Test
    public void testCreateVertex() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        List<String> list = new ArrayList<String>();
        list.add("abc");
        list.add("def");
        properties.put("name", "顶点测试-正常");
        properties.put("age", 19);
        properties.put("time", System.currentTimeMillis());
        properties.put("list", list);
        map.put(ID, "1234567");
        map.put(PROPERTIES, properties);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                /**
                 * // 预期返回值的媒体类型text/plain;charset=UTF-8
                 */
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 新增顶点，已经存在
     *
     * @throws Exception
     */
    @Test
    public void testCreateVertexExisted() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("name", "顶点测试-正常");
        properties.put("age", 19);
        properties.put("time", System.currentTimeMillis());
        map.put(ID, "123456");
        map.put(PROPERTIES, properties);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Check.CH_02.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 新增顶点,响应格式不对
     *
     * @throws Exception
     */
    @Test
    public void testCreateVertexMediaType() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("name", "顶点测试-正常");
        properties.put("age", 19);
        properties.put("time", System.currentTimeMillis());
        map.put(ID, "1234567");
        map.put(PROPERTIES, properties);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_ATOM_XML)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 方法不被允许
     *
     * @throws Exception
     */
    @Test
    public void testCreateVertexMethodNotAllowed() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("name", "顶点测试-正常");
        properties.put("age", 19);
        properties.put("time", System.currentTimeMillis());
        map.put(ID, "1234567");
        map.put(PROPERTIES, properties);

        MvcResult result = mockMvc.perform(get("/graph/storage/v1/vertex")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 新增顶点包含关键字 id,createTime,updateTime,namespace
     *
     * @throws Exception
     */
    @Test
    public void testCreateVertexObtainKeyword() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("name", "顶点测试-正常");
        properties.put(SYSTEM_RESERVED.get(0), System.currentTimeMillis());
        properties.put("time", System.currentTimeMillis());
        map.put(ID, "1234567");
        map.put(PROPERTIES, properties);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA05.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 新增顶点不包含ID
     *
     * @throws Exception
     */
    @Test
    public void testCreateVertexNoId() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(PROPERTIES, new HashMap<String, Object>());

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA01.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 新增顶点id长度超过约束条件
     *
     * @throws Exception
     */
    @Test
    public void testCreateVertexIdTooLong() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("name", "顶点测试-正常");
        properties.put("age", 19);
        properties.put("time", System.currentTimeMillis());
        map.put(ID, "123456" + nodeId);
        map.put(PROPERTIES, properties);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA31.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 新增顶点id不合法    20170822id没有限制，测试代码取消
     * @throws Exception
     */
    //@Test
    public void testCreateVertexIdIllegal() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("age", 19);
        properties.put("time", System.currentTimeMillis());
        map.put(ID, "@ABDCD1234");
        map.put(PROPERTIES, properties);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA02.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 新增顶点不包含租户 header,status 为 400
     *
     * @throws Exception
     */
    @Test
    public void testCreateVertexNoHeader() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(PROPERTIES, new HashMap<String, Object>());
        map.put(ID, "123456");

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 新增顶点不包含租户 header 长度超过约束
     *
     * @throws Exception
     */
    @Test
    public void testCreateVertexHeaderTooLong() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(PROPERTIES, new HashMap<String, Object>());
        map.put(ID, "123456");

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex")
                .header(APPTENANT, "Header" + tenant)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA30.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 新增顶点不包含租户 header 不符合规范
     *
     * @throws Exception
     */
    @Test
    public void testCreateVertexHeaderIllegal() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(PROPERTIES, new HashMap<String, Object>());
        map.put(ID, "123456");

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex")
                .header(APPTENANT, "@Header1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA04.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 新增顶点不包含body vertex 对象
     *
     * @throws Exception
     */
    @Test
    public void testCreateVertexNoBody() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(ID, "1234567");

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 新增顶点,属性信息为null
     *
     * @throws Exception
     */
    @Test
    public void testCreateVertexBodyIsNull() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(ID, "1234567");
        map.put(PROPERTIES, null);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 新增顶点,属性信息为为 {}
     *
     * @throws Exception
     */
    @Test
    public void testCreateVertexBodyIsNewObject() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(ID, "1234567");
        map.put(PROPERTIES, new JSONObject());

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 新增顶点,属性信息key 不符合规范
     * @throws Exception
     */
    @Test
    public void testCreateVertexBodyKeyIllegal() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("@Time", "10:20");
        properties.put("location", "beijing");
        properties.put("_Hello", "man");
        map.put(ID, "1234567");
        map.put(PROPERTIES, properties);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA39.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 新增顶点,属性信息key 值太长
     * @throws Exception
     */
    @Test
    public void testCreateVertexBodyKeyIsTooLong() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("time" + keyLength, "10:20");
        properties.put("location", "beijing");
        properties.put("sex" + keyLength, "man");
        map.put(ID, "1234567");
        map.put(PROPERTIES, properties);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA36.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 新增顶点,属性value 值溢出-过大-Number类型
     * @throws Exception
     */
    @Test
    public void testCreateVertexPropertiesValueOverflowBigNumber() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("tooBig", maxBigInteger);
        map.put(ID, "1234567");
        map.put(PROPERTIES, properties);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA41.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 新增顶点,属性value 值溢出-过大-字符类型
     * @throws Exception
     */
    @Test
    public void testCreateVertexPropertiesValueOverflowBigString() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("tooBig", valueLength + valueLength);
        map.put(ID, "1234567");
        map.put(PROPERTIES, properties);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA38.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 新增顶点,属性value 值溢出-过小
     * @throws Exception
     */
    @Test
    public void testCreateVertexPropertiesValueOverflowSmall() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("tooBig", maxBigInteger);
        properties.put("tooSmall", minBigInteger);
        map.put(ID, "1234567");
        map.put(PROPERTIES, properties);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA42.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 更新顶点测试-正常
     *
     * @throws Exception
     */
    @Test
    public void testUpdateVertex() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        final String name  = "顶点测试-正常属性覆盖";
        final int age = 29;
        final long time = System.currentTimeMillis();
        properties.put("name", name);
        properties.put("age", age);
        properties.put("time", time);
        map.put(PROPERTIES, properties);

        MvcResult result = mockMvc.perform(put("/graph/storage/v1/vertex/123456")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                /**
                 * // 预期返回值的媒体类型text/plain;charset=UTF-8
                 */
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
        /**
         *  查看修改结果
         */
        MvcResult resultQuery = mockMvc.perform(get("/graph/storage/v1/vertex/123456")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andExpect(jsonPath("$.data.properties.age").value(age))
                .andExpect(jsonPath("$.data.properties.name").value(name))
                .andExpect(jsonPath("$.data.properties.time").value(time))
                .andExpect(jsonPath("$.data.properties.id").doesNotExist())
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andReturn();
        logger.error(resultQuery.getResponse().getContentAsString());
    }

    /**
     * 更新顶点测试，不存在更新
     *
     * @throws Exception
     */
    @Test
    public void testUpdateVertexNonExist() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("name", "顶点测试-正常属性覆盖");
        properties.put("age", 29);
        properties.put("time", System.currentTimeMillis());
        map.put(PROPERTIES, properties);

        MvcResult result = mockMvc.perform(put("/graph/storage/v1/vertex/AAAAAAAAAA")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Check.CH_01.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 更新顶点测试,Unsupported Media Type 415
     *
     * @throws Exception
     */
    @Test
    public void testUpdateVertexMediaType() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("name", "顶点测试-正常属性覆盖");
        properties.put("age", 29);
        properties.put("time", System.currentTimeMillis());
        map.put(PROPERTIES, properties);

        MvcResult result = mockMvc.perform(put("/graph/storage/v1/vertex/123456")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_ATOM_XML)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 更新顶点测试-方法不被允许
     *
     * @throws Exception
     */
    @Test
    public void testUpdateVertexMethodNotAllowed() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("name", "顶点测试-正常属性覆盖");
        properties.put("age", 29);
        properties.put("time", System.currentTimeMillis());
        map.put(PROPERTIES, properties);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/123456")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    @Test
    public void testUpdateVertexObtainKeyword() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashedMap<String, Object>();
        properties.put(SYSTEM_RESERVED.get(3), System.currentTimeMillis());
        properties.put("namespace", "Test");
        map.put(PROPERTIES, properties);

        mockMvc.perform(put("/graph/storage/v1/vertex/12345")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Validate.VA05.getId()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
    }

    /**
     * 更新顶点测试-没有id
     *
     * @throws Exception
     */
    @Test
    public void testUpdateVertexNoId() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("name", "顶点测试-正常属性覆盖");
        properties.put("age", 29);
        properties.put("time", System.currentTimeMillis());
        map.put(PROPERTIES, properties);

        MvcResult result = mockMvc.perform(put("/graph/storage/v1/vertex/")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(405))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 更新顶点测试-没有header 租户
     *
     * @throws Exception
     */
    @Test
    public void testUpdateVertexNoHeader() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("name", "顶点测试-正常属性覆盖");
        properties.put("age", 29);
        properties.put("time", System.currentTimeMillis());
        map.put(PROPERTIES, properties);

        MvcResult result = mockMvc.perform(put("/graph/storage/v1/vertex/123456")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(400))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 更新顶点 没有body
     *
     * @throws Exception
     */
    @Test
    public void testUpdateVertexNoBody() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();

        MvcResult result = mockMvc.perform(put("/graph/storage/v1/vertex/123456")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 获取顶点-正常
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertex() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();

        MvcResult result = mockMvc.perform(get("/graph/storage/v1/vertex/123456")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andExpect(jsonPath("$.data.properties." + VertexNameKey).value(VertexNameValue))
                .andExpect(jsonPath("$.data.properties." + VertexTimeKey).value(VertexTimeValue))
                .andExpect(jsonPath("$.data.properties.id").doesNotExist())
                .andReturn();
        logger.error(result.getResponse().getContentAsString());

    }

    /**
     * 获取顶点-正常,Unsupported Media Type
     *
     * @throws Exception
     */
    @Test(expected = AssertionError.class)
    public void testObtainVertexMediaType() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();

        MvcResult result = mockMvc.perform(get("/graph/storage/v1;/vertex/123456")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_ATOM_XML))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());

    }


    /**
     * 获取顶点,方法不被允许
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexMethodNotAllowed() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/123456")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());

    }

    /**
     * 查询节点不存在
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexNonExist() throws Exception {

        MvcResult result = mockMvc.perform(get("/graph/storage/v1/vertex/A9999999999999")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Check.CH_01.getId()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 获取顶点-没有id
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexNoId() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();

        MvcResult result = mockMvc.perform(get("/graph/storage/v1/vertex/######")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 获取顶点-no header 没有租户
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexNoHeader() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();

        MvcResult result = mockMvc.perform(get("/graph/storage/v1/vertex/123456")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());

    }

    /**
     * 获取顶点出向顶点-正常
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexOutV() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(SKIP,0);
        map.put(LIMIT,100);
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/"
                + VertexId + "/out")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andExpect(jsonPath("$.data.total").value(OutVertexCount))
                .andExpect(jsonPath("$.data.vertices[1].properties.name").isNotEmpty())
                .andExpect(jsonPath("$.data.vertices[1].properties.id").doesNotExist())
                .andReturn();


        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 获取顶点出向顶点,Unsupported Media Type 415
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexOutVMediaType() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/123456/out")
                .contentType(MediaType.APPLICATION_ATOM_XML)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 获取顶点出向顶点，方法不被允许
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexOutVMethodNotAllowed() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(get("/graph/storage/v1/vertex/123456/out")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 获取顶点出向顶点顶点不存在
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexOutVNonExist() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/------/out")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Code.CO_39.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 获取顶点出向顶点-noId 没有id
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexOutVNoId() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/out")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(405))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 获取顶点出向顶点-noHeader 没有header
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexOutVNoHeader() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/123456/out")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(400))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 调用入向顶点,Unsupported Media Type 415
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexInVMediaType() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/123456/in")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_ATOM_XML)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 调用入向顶点-正常
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexInV() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(SKIP,0);
        map.put(LIMIT,100);
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/"+
                VertexId +"/in")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andExpect(jsonPath("$.data.total").value(InVertexCount))
                .andExpect(jsonPath("$.data.vertices[1].properties.name").isNotEmpty())
                .andExpect(jsonPath("$.data.vertices[1].properties.id").doesNotExist())
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 调用入向顶点,方法不被允许
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexInVMethodNotAllowed() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(get("/graph/storage/v1/vertex/123456/in")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 调用入向顶点顶点不存在
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexInVNonExist() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/------/in")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Code.CO_40.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 获取入向顶点-noId，没有id
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexInVNoId() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/in")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(405))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 获取入向顶点noHeader 没有header信息
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexInVNoHeader() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/123456/in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(400))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 获取双向顶点-both-正常
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexBothV() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(SKIP,0);
        map.put(LIMIT,2);
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/"+
                VertexId + "/both")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andExpect(jsonPath("$.data.total").value(OutVertexCount + InVertexCount))
                .andExpect(jsonPath("$.data.vertices[1].properties.name").isNotEmpty())
                .andExpect(jsonPath("$.data.vertices[1].properties.id").doesNotExist())
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 获取双向顶点-both,Unsupported Media Type 415
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexBothVMediaType() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/123456/both")
                .contentType(MediaType.APPLICATION_ATOM_XML)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 获取双向顶点-both，方法不被允许
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexBothVMethodNotAllowed() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(get("/graph/storage/v1/vertex/123456/both")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 获取双向顶点-both顶点不存在
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexBothVNonExist() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/------/both")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Code.CO_41.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 获取双向顶点，没有id
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexBothVNoId() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/both")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(405))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 获取双向顶点，没有header
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexBothVNoHeader() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/123456/both")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(400))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 获取出向顶点id列表正常
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexOutIds() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(SKIP,0);
        map.put(LIMIT,5);
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/123456/outIds")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andExpect(jsonPath("$.data.total").value(OutVertexCount))
                .andExpect(jsonPath("$.data.vertexIds[0]").isNotEmpty())
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 获取出向顶点id列表正常
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexOutIdsMediaType() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/123456/outIds")
                .contentType(MediaType.APPLICATION_ATOM_XML)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 获取出向顶点id列表,方法不被允许
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexOutIdsMethodNotAllowed() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(get("/graph/storage/v1/vertex/123456/outIds")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 获取出向顶点id列表,顶点不存在
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexOutIdsNonExist() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/------/outIds")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Code.CO_39.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 获取出向顶点id列表没有id
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexOutIdsNoId() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/outIds")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(405))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 获取出向id列表没有header信息
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexOutIdsNoHeader() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/123456/outIds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(400))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 获取入向id顶点列表-正常
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexInIds() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(SKIP,0);
        map.put(LIMIT,10);
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/123456/inIds")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andExpect(jsonPath("$.data.total").value(InVertexCount))
                .andExpect(jsonPath("$.data.vertexIds").isNotEmpty())
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 获取入向id顶点列表,Unsupported Media Type 415
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexInIdsMediaType() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/123456/inIds")
                .contentType(MediaType.APPLICATION_ATOM_XML)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 获取入向id顶点列表，方法不被允许
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexInIdsMethodNotAllowed() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(get("/graph/storage/v1/vertex/123456/inIds")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 获取入向id顶点列表，顶点不存在
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexInIdsNonExist() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/------/inIds")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Code.CO_40.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 获取入向id顶点列表，没有id
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexInIdsNoId() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/inIds")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(405))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 获取入向顶点id列表，没有header
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexInIdsNoHeader() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/123456/inIds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(400))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 获取双向顶点id列表-正常
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexBothIds() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(SKIP,0);
        map.put(LIMIT,5);
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/123456/bothIds")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andExpect(jsonPath("$.data.total").value(OutVertexCount + InVertexCount))
                .andExpect(jsonPath("$.data.vertexIds[1]").isNotEmpty())
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 获取双向顶点id列表,Unsupported Media Type 415
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexBothIdsMediaType() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/123456/bothIds")
                .contentType(MediaType.APPLICATION_ATOM_XML)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 获取双向顶点id列表,方法不被允许
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexBothIdsMethodNotAllowed() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(get("/graph/storage/v1/vertex/123456/bothIds")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 获取双向顶点id列表,顶点不存在
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexBothIdsNonExist() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/------/bothIds")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Code.CO_41.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 获取双向顶点id列表，没有id
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexBothIdsNoId() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/bothIds")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(405))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 获取双向id顶点列表没有header
     *
     * @throws Exception
     */
    @Test
    public void testObtainVertexBothIdsNoHeader() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/123456/bothIds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(400))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 移除顶点-正常
     *
     * @throws Exception
     */
    @Test
    public void testRemoveVertexEdges() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();

        MvcResult result = mockMvc.perform(delete("/graph/storage/v1/vertex/" + VertexId)
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
        /**
         * 查找节点不存在
         */
        MvcResult resultQuery = mockMvc.perform(get("/graph/storage/v1/vertex/" + VertexId)
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Check.CH_01.getId()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        logger.error(resultQuery.getResponse().getContentAsString());
    }

    /**
     * 移除顶点,Unsupported Media Type 415
     *
     * @throws Exception
     */
    @Test(expected = AssertionError.class)
    public void testRemoveVertexEdgesMediaType() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();

        MvcResult result = mockMvc.perform(delete("/graph/storage/v1/vertex/123456")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_ATOM_XML))
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 移除顶点-正常，方法不被允许
     *
     * @throws Exception
     */
    @Test
    public void testRemoveVertexEdgesMethodNotAllowed() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/123456")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 移除顶点-正常,顶点不存在
     *
     * @throws Exception
     */
    @Test
    public void testRemoveVertexEdgesNonExist() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();

        MvcResult result = mockMvc.perform(delete("/graph/storage/v1/vertex/A9999999999")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Check.CH_01.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 移除顶点id，no id
     *
     * @throws Exception
     */
    @Test
    public void testRemoveVertexEdgesNoId() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();

        MvcResult result = mockMvc.perform(delete("/graph/storage/v1/vertex")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(405))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 移除顶点没有header信息
     *
     * @throws Exception
     */
    @Test
    public void testRemoveVertexEdgesNoHeader() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();

        MvcResult result = mockMvc.perform(delete("/graph/storage/v1/vertex/123456")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(400))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 创建关系，正常
     *
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNodeNonExistent() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> start = new HashMap<String, Object>();
        Map<String, Object> end = new HashMap<String, Object>();
        Map<String, Object> edge = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();

        properties.put("location", "beijing");
        properties.put("knowTime", "afternoon");
        start.put(ID, "startId0001");
        end.put(ID, "endId0001");
        edge.put("type", Type);
        edge.put(PROPERTIES, properties);
        map.put("source", start);
        map.put("target", end);
        map.put("edge", edge);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge/non-existent/node")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        JSONObject jsonObject = JSON.parseObject(result.getResponse().getContentAsString());
        JSONObject dataObject  = (JSONObject)jsonObject.get("data");
        String relationShipString  = String.valueOf(dataObject.get(ID));
        long relationShipLong = Long.valueOf(relationShipString);
        logger.error(result.getResponse().getContentAsString());
        /**
         * 获取边
         */
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/graph/storage/v1/edge/" + relationShipLong)
                .header(APPTENANT, HEADER_VALUE)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data.properties.location").value("beijing"))
                .andExpect(jsonPath("$.data.properties.createTime").isNotEmpty())
                .andExpect(jsonPath("$.data.type").value(Type))
                .andExpect(jsonPath("$.data.id").value(Long.valueOf(relationShipLong)))
                .andExpect(jsonPath("$.data.properties.id").doesNotExist())
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 创建关系,Unsupported Media Type 415
     *
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNodeNonExistentMediaType() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> start = new HashMap<String, Object>();
        Map<String, Object> end = new HashMap<String, Object>();
        Map<String, Object> edge = new HashMap<String, Object>();

        start.put(ID, "startId0001");
        end.put(ID, "endId0001");
        edge.put("type", "ControllerTest");
        edge.put(ID, "edge0001");
        map.put("source", start);
        map.put("target", end);
        map.put("edge", edge);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge/non-existent/node")
                .contentType(MediaType.APPLICATION_ATOM_XML)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 创建关系-正常，方法不被允许
     *
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNodeNonExistentMethodNotAllowed() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> start = new HashMap<String, Object>();
        Map<String, Object> end = new HashMap<String, Object>();
        Map<String, Object> edge = new HashMap<String, Object>();

        start.put(ID, "startId0001");
        end.put(ID, "endId0001");
        edge.put("type", "ControllerTest");
        edge.put(ID, "edge0001");
        map.put("source", start);
        map.put("target", end);
        map.put("edge", edge);

        MvcResult result = mockMvc.perform(get("/graph/storage/v1/edge/non-existent/node")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 创建关系包含关键字
     *
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNodeNonExistentObtainKeyword() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> start = new HashMap<String, Object>();
        Map<String, Object> end = new HashMap<String, Object>();
        Map<String, Object> edge = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();

        start.put(ID, "startId0001");
        end.put(ID, "endId0001");
        properties.put("createTime", System.currentTimeMillis());
        edge.put("type", "ControllerTest");
        edge.put(PROPERTIES, properties);
        map.put("source", start);
        map.put("target", end);
        map.put("edge", edge);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge/non-existent/node")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA29.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());

    }

    /**
     * 创建关系没有指定关系类型
     *
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNodeNonExistentNoType() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> start = new HashMap<String, Object>();
        Map<String, Object> end = new HashMap<String, Object>();
        Map<String, Object> edge = new HashMap<String, Object>();

        start.put(ID, "startId0001");
        end.put(ID, "endId0001");
        map.put("source", start);
        map.put("target", end);
        map.put("edge", edge);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge/non-existent/node")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Validate.VA19.getId()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 创建关系，type 太长
     *
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNodeNonExistentTypeIsTooLong() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> start = new HashMap<String, Object>();
        Map<String, Object> end = new HashMap<String, Object>();
        Map<String, Object> edge = new HashMap<String, Object>();

        start.put(ID, "startId0001");
        end.put(ID, "endId0001");
        edge.put("type","ControllerTest" + edgeType);
        map.put("source", start);
        map.put("target", end);
        map.put("edge", edge);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge/non-existent/node")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Validate.VA35.getId()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 创建关系，type 不合法
     *
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNodeNonExistentTypeIllegal() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> start = new HashMap<String, Object>();
        Map<String, Object> end = new HashMap<String, Object>();
        Map<String, Object> edge = new HashMap<String, Object>();

        start.put(ID, "startId0001");
        end.put(ID, "endId0001");
        edge.put("type","**ControllerTest");
        map.put("source", start);
        map.put("target", end);
        map.put("edge", edge);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge/non-existent/node")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Validate.VA20.getId()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 创建关系，没有指定节点
     *
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNodeNonExistentNoSource() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> start = new HashMap<String, Object>();
        Map<String, Object> end = new HashMap<String, Object>();
        Map<String, Object> edge = new HashMap<String, Object>();

        start.put(ID, "startId0001");
        end.put(ID, "endId0001");
        edge.put("type", "ControllerTest");
        map.put("target", end);
        map.put("edge", edge);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge/non-existent/node")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Validate.VA06.getId()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 创建关系，没有结束顶点
     *
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNodeNonExistentNoTarget() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> start = new HashMap<String, Object>();
        Map<String, Object> end = new HashMap<String, Object>();
        Map<String, Object> edge = new HashMap<String, Object>();

        start.put(ID, "startId0001");
        end.put(ID, "endId0001");
        edge.put("type", "ControllerTest");
        map.put("source", start);
        map.put("edge", edge);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge/non-existent/node")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Validate.VA07.getId()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 新增关系，没有租户no header
     *
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNodeNonExistentNoHeader() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> start = new HashMap<String, Object>();
        Map<String, Object> end = new HashMap<String, Object>();
        Map<String, Object> edge = new HashMap<String, Object>();

        start.put(ID, "startId0001");
        end.put(ID, "endId0001");
        edge.put("type", "ControllerTest");
        edge.put(ID, "edge0001");
        map.put("source", start);
        map.put("target", end);
        map.put("edge", edge);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge/non-existent/node")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(400))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 新增关系，header值太长
     *
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNodeNonExistentHeaderIsTooLong() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> start = new HashMap<String, Object>();
        Map<String, Object> end = new HashMap<String, Object>();
        Map<String, Object> edge = new HashMap<String, Object>();

        start.put(ID, "startId0001");
        end.put(ID, "endId0001");
        edge.put("type", "ControllerTest");
        map.put("source", start);
        map.put("target", end);
        map.put("edge", edge);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge/non-existent/node")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT,HEADER_VALUE + tenant)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Validate.VA30.getId()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 新增关系，header值不合法
     *
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNodeNonExistentHeaderIllegal() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> start = new HashMap<String, Object>();
        Map<String, Object> end = new HashMap<String, Object>();
        Map<String, Object> edge = new HashMap<String, Object>();

        start.put(ID, "startId0001");
        end.put(ID, "endId0001");
        edge.put("type", "ControllerTest");
        map.put("source", start);
        map.put("target", end);
        map.put("edge", edge);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge/non-existent/node")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT,"**Header")
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Validate.VA11.getId()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 新增关系key长度过长；
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNodeNonExistentBodyKeyIsTooLong() throws  Exception{

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> start = new HashMap<String, Object>();
        Map<String, Object> end = new HashMap<String, Object>();
        Map<String, Object> edge = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();

        start.put(ID, "startId0001");
        end.put(ID, "endId0001");
        properties.put("locations" + keyLength,"shanghai");
        edge.put("type", "ControllerTest");
        edge.put(PROPERTIES,properties);
        map.put("source", start);
        map.put("target", end);
        map.put("edge", edge);


        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge/non-existent/node")
                .header(APPTENANT,HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Validate.VA37.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());

    }

    /**
     * 新增关系value 值太大 Number类型
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNodeNonExistentPropertiesValueOverflowBigNumber() throws  Exception{

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> start = new HashMap<String, Object>();
        Map<String, Object> end = new HashMap<String, Object>();
        Map<String, Object> edge = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();

        start.put(ID, "startId0001");
        end.put(ID, "endId0001");
        properties.put("tooBig", maxBigInteger);
        edge.put("type", "ControllerTest");
        edge.put(PROPERTIES,properties);
        map.put("source", start);
        map.put("target", end);
        map.put("edge", edge);


        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge/non-existent/node")
                .header(APPTENANT,HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Validate.VA41.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());

    }/**
     * 新增关系value 值太大， 字符类型
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNodeNonExistentPropertiesValueOverflowBigString() throws  Exception{

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> start = new HashMap<String, Object>();
        Map<String, Object> end = new HashMap<String, Object>();
        Map<String, Object> edge = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();

        start.put(ID, "startId0001");
        end.put(ID, "endId0001");
        properties.put("tooBig", valueLength + valueLength);
        edge.put("type", "ControllerTest");
        edge.put(PROPERTIES,properties);
        map.put("source", start);
        map.put("target", end);
        map.put("edge", edge);


        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge/non-existent/node")
                .header(APPTENANT,HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Validate.VA38.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());

    }

    /**
     * 新增关系value 值太小
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNodeNonExistentPropertiesValueOverflowSmall() throws  Exception{

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> start = new HashMap<String, Object>();
        Map<String, Object> end = new HashMap<String, Object>();
        Map<String, Object> edge = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();

        start.put(ID, "startId0001");
        end.put(ID, "endId0001");
        properties.put("tooSmall" , minBigInteger);
        edge.put("type", "ControllerTest");
        edge.put(PROPERTIES,properties);
        map.put("source", start);
        map.put("target", end);
        map.put("edge", edge);


        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge/non-existent/node")
                .header(APPTENANT,HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Validate.VA42.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());

    }


    /**
     * 新增关系,no body properties
     *
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNodeNonExistentNoBody() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> start = new HashMap<String, Object>();
        Map<String, Object> end = new HashMap<String, Object>();
        Map<String, Object> edge = new HashMap<String, Object>();

        start.put(ID, "startId0001");
        end.put(ID, "endId0001");
        edge.put("type", "ControllerTest");
        map.put("source", start);
        map.put("target", end);
        map.put("edge", edge);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge/non-existent/node")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 新增关系,body is null
     *
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNodeNonExistentBodyIsNull() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> start = new HashMap<String, Object>();
        Map<String, Object> end = new HashMap<String, Object>();
        Map<String, Object> edge = new HashMap<String, Object>();

        start.put(ID, "startId0001");
        end.put(ID, "endId0001");
        edge.put("type", "ControllerTest");
        edge.put(PROPERTIES, null);
        map.put("source", start);
        map.put("target", end);
        map.put("edge", edge);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge/non-existent/node")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 新增关系,body is {} new jsonObject
     *
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNodeNonExistentBodyIsNewObject() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> start = new HashMap<String, Object>();
        Map<String, Object> end = new HashMap<String, Object>();
        Map<String, Object> edge = new HashMap<String, Object>();

        start.put(ID, "startId0001");
        end.put(ID, "endId0001");
        edge.put("type", "ControllerTest");
        edge.put(PROPERTIES, new JSONObject());
        map.put("source", start);
        map.put("target", end);
        map.put("edge", edge);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge/non-existent/node")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 新增关系-正常
     * @throws Exception
     */
    @Test
    public void testCreateEdge() throws  Exception{
        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> properties = new HashedMap<String, Object>();
        Map<String,Object> edge = new HashedMap<String, Object>();
        properties.put("name","relationShip test");
        properties.put("location","beijing");
        map.put(SOURCEID,"startId0001");
        map.put(TARGETID,"endId0001");
        edge.put(PROPERTIES,properties);
        edge.put("type",Type);
        map.put("edge",edge);


        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge")
                .header(APPTENANT,HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andExpect(jsonPath("$.data.type").value(Type))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.properties.location").value("beijing"))
                .andExpect(jsonPath("$.data.properties.id").doesNotExist())
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 创建关系，media type 不对
     * throws  Exception
     */
    @Test
    public void testCreateEdgeMediaType() throws  Exception{
        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> properties = new HashedMap<String, Object>();
        properties.put("name","relationShip test");
        properties.put("location","beijing");
        map.put(SOURCEID,"startId0001");
        map.put(TARGETID,"endId0001");
        map.put(PROPERTIES,properties);
        map.put("type",Type);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_ATOM_XML)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()))
                .andReturn();

        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 创建关系，方法不被允许
     * throws  Exception
     */
    @Test
    public void testCreateEdgeMethodNotAllowed() throws  Exception{
        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> properties = new HashedMap<String, Object>();
        properties.put("name","relationShip test");
        properties.put("location","beijing");
        map.put(SOURCEID,"startId0001");
        map.put(TARGETID,"endId0001");
        map.put(PROPERTIES,properties);
        map.put("type",Type);

        MvcResult result = mockMvc.perform(get("/graph/storage/v1/edge")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();

        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 创建关系，包含关键字
     * throws  Exception
     */
    @Test
    public void testCreateEdgeObtainKeyWord() throws  Exception{
        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> properties = new HashedMap<String, Object>();
        Map<String,Object> edge = new HashedMap<String, Object>();
        properties.put("name","relationShip test");
        properties.put("location","beijing");
        properties.put(SYSTEM_RESERVED.get(2),"beijing");
        map.put(SOURCEID,"startId0001");
        map.put(TARGETID,"endId0001");
        edge.put(PROPERTIES,properties);
        edge.put("type",Type);
        map.put("edge",edge);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA29.getId()))
                .andReturn();

        logger.error(result.getResponse().getContentAsString());
    }


    /**
     * 新增关系-没有关系类型
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNoType() throws  Exception{
        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> properties = new HashedMap<String, Object>();
        properties.put("name","relationShip test");
        properties.put("location","beijing");
        map.put(SOURCEID,"startId0001");
        map.put(TARGETID,"endId0001");
        map.put(PROPERTIES,properties);

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge")
                .header(APPTENANT,HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Validate.VA19.getId()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        logger.info(result.getResponse().getContentAsString());
    }


    /**
     * 新增关系-关系字段超出范围
     * @throws Exception
     */
    @Test
    public void testCreateEdgeTypeIsTooLong() throws  Exception{
        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> properties = new HashedMap<String, Object>();
        Map<String,Object> edge = new HashedMap<String, Object>();
        properties.put("name","relationShip test");
        properties.put("location","beijing");
        map.put(SOURCEID,"startId0001");
        map.put(TARGETID,"endId0001");
        edge.put(PROPERTIES,properties);
        edge.put("type",Type + edgeType);
        map.put("edge",edge);


        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge")
                .header(APPTENANT,HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA35.getId()))
                .andReturn();
        logger.info(result.getResponse().getContentAsString());
    }

    /**
     * 新增关系-关系类型不合法
     * @throws Exception
     */
    @Test
    public void testCreateEdgeTypeIllegal() throws  Exception{
        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> properties = new HashedMap<String, Object>();
        Map<String,Object> edge = new HashedMap<String, Object>();
        properties.put("name","relationShip test");
        properties.put("location","beijing");
        map.put(SOURCEID,"startId0001");
        map.put(TARGETID,"endId0001");
        edge.put(PROPERTIES,properties);
        edge.put("type","**" + Type);
        map.put("edge",edge);


        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge")
                .header(APPTENANT,HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA20.getId()))
                .andReturn();
        logger.info(result.getResponse().getContentAsString());
    }


    /**
     * 新增关系-没有指定源顶点
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNoSource() throws  Exception{
        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> properties = new HashedMap<String, Object>();
        properties.put("name","relationShip test");
        properties.put("location","beijing");
        map.put(TARGETID,"endId0001");
        map.put(PROPERTIES,properties);
        map.put("type",Type);


        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge")
                .header(APPTENANT,HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA08.getId()))
                .andReturn();
        logger.info(result.getResponse().getContentAsString());
    }

    /**
     * 新增关系-没有指定目标顶点
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNoTarget() throws  Exception{
        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> properties = new HashedMap<String, Object>();
        properties.put("name","relationShip test");
        properties.put("location","beijing");
        map.put(SOURCEID,"startId0001");
        map.put(PROPERTIES,properties);
        map.put("type",Type);


        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge")
                .header(APPTENANT,HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA12.getId()))
                .andReturn();
        logger.info(result.getResponse().getContentAsString());
    }


    /**
     * 新增关系，没有租户 header
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNoHeader() throws  Exception{
        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> properties = new HashedMap<String, Object>();
        properties.put("name","relationShip test");
        properties.put("location","beijing");
        map.put(SOURCEID,"startId0001");
        map.put(TARGETID,"endId0001");
        map.put(PROPERTIES,properties);
        map.put("type",Type);


        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(map)))
                .andExpect(status().is(400))
                .andReturn();
        logger.info(result.getResponse().getContentAsString());
    }

    /**
     * 新增关系，header 值太长
     * @throws Exception
     */
    @Test
    public void testCreateEdgeHeaderIsTooLong() throws  Exception{
        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> properties = new HashedMap<String, Object>();
        properties.put("name","relationShip test");
        properties.put("location","beijing");
        map.put(SOURCEID,"startId0001");
        map.put(TARGETID,"endId0001");
        map.put(PROPERTIES,properties);
        map.put("type",Type);


        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge")
                .header(APPTENANT,HEADER_VALUE + tenant)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA30.getId()))
                .andReturn();
        logger.info(result.getResponse().getContentAsString());
    }

    /**
     * 新增关系，header值不合法
     * @throws Exception
     */
    @Test
    public void testCreateEdgeHeaderIllegal() throws  Exception{
        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> properties = new HashedMap<String, Object>();
        properties.put("name","relationShip test");
        properties.put("location","beijing");
        map.put(SOURCEID,"startId0001");
        map.put(TARGETID,"endId0001");
        map.put(PROPERTIES,properties);
        map.put("type",Type);


        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge")
                .header(APPTENANT,"**" + HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA28.getId()))
                .andReturn();
        logger.info(result.getResponse().getContentAsString());
    }


    /**
     * 新增关系,properties key 太长
     * @throws Exception
     */
    @Test
    public void testCreateEdgeBodyKeyIsTooLong() throws  Exception{
        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> properties = new HashedMap<String, Object>();
        Map<String,Object> edge = new HashedMap<String, Object>();
        properties.put("name","relationShip test");
        properties.put("location" + keyLength,"beijing");
        map.put(SOURCEID,"startId0001");
        map.put(TARGETID,"endId0001");
        edge.put(PROPERTIES,properties);
        edge.put("type",Type);
        map.put("edge",edge);


        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge")
                .header(APPTENANT,HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA37.getId()))
                .andReturn();
        logger.info(result.getResponse().getContentAsString());
    }

    /**
     * 新增关系，properties value 值太大
     * @throws Exception
     */
    @Test
    public void testCreateEdgePropertiesValueOverflowBigNumber() throws  Exception{
        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> properties = new HashedMap<String, Object>();
        Map<String,Object> edge = new HashedMap<String, Object>();
        properties.put("name","relationShip test");
        properties.put("location","beijing");
        properties.put("tooBig", maxBigInteger);
        map.put(SOURCEID,"startId0001");
        map.put(TARGETID,"endId0001");
        edge.put(PROPERTIES,properties);
        edge.put("type",Type);
        map.put("edge",edge);


        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge")
                .header(APPTENANT,HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA41.getId()))
                .andReturn();
        logger.info(result.getResponse().getContentAsString());
    }


    /**
     * 新增关系，properties value 值太大
     * @throws Exception
     */
    @Test
    public void testCreateEdgePropertiesValueOverflowBigString() throws  Exception{
        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> properties = new HashedMap<String, Object>();
        Map<String,Object> edge = new HashedMap<String, Object>();
        properties.put("name","relationShip test");
        properties.put("location","beijing");
        properties.put("tooBig", valueLength + valueLength);
        map.put(SOURCEID,"startId0001");
        map.put(TARGETID,"endId0001");
        edge.put(PROPERTIES,properties);
        edge.put("type",Type);
        map.put("edge",edge);


        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge")
                .header(APPTENANT,HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA38.getId()))
                .andReturn();
        logger.info(result.getResponse().getContentAsString());
    }

    /**
     * 新增关系，properties value 值太小
     * @throws Exception
     */
    @Test
    public void testCreateEdgePropertiesValueOverflowSmall() throws  Exception{
        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> properties = new HashedMap<String, Object>();
        Map<String,Object> edge = new HashedMap<String, Object>();
        properties.put("name","relationShip test");
        properties.put("location","beijing");
        properties.put("tooSmall" , minBigInteger);
        map.put(SOURCEID,"startId0001");
        map.put(TARGETID,"endId0001");
        edge.put(PROPERTIES,properties);
        edge.put("type",Type);
        map.put("edge",edge);


        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge")
                .header(APPTENANT,HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA42.getId()))
                .andReturn();
        logger.info(result.getResponse().getContentAsString());
    }

    /**
     * 新增关系
     * @throws Exception
     */
    @Test
    public void testCreateEdgeNoBody() throws  Exception{
        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> properties = new HashedMap<String, Object>();
        properties.put("name","relationShip test");
        properties.put("location","beijing");
        map.put(SOURCEID,"startId0001");
        map.put(TARGETID,"endId0001");
        map.put("type",Type);


        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge")
                .header(APPTENANT,HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andReturn();
        logger.info(result.getResponse().getContentAsString());
    }

    /**
     * 新增关系
     * @throws Exception
     */
    @Test
    public void testCreateEdgeBodyIsNull() throws  Exception{
        Map<String,Object> map = new HashedMap<String, Object>();
        map.put(SOURCEID,"startId0001");
        map.put(TARGETID,"endId0001");
        map.put(PROPERTIES,null);
        map.put("type",Type);


        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge")
                .header(APPTENANT,HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        logger.info(result.getResponse().getContentAsString());
    }
    /**
     * 测试移除关系-正常
     *
     * @throws Exception
     */
    @Test
    public void testRemoveEdge() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();

        Random random = new Random();
        long longId = relationShipIdList.get(random.nextInt(relationShipIdList.size()));
        MvcResult result = mockMvc.perform(delete("/graph/storage/v1/edge/" + longId)
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andReturn();

        logger.error(result.getResponse().getContentAsString());
        /**
         * 获取边
         */
        MvcResult resultQuery = mockMvc.perform(MockMvcRequestBuilders.get("/graph/storage/v1/edge/" + longId)
                .header(APPTENANT, HEADER_VALUE)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Code.CO_24.getId()))
                .andExpect(jsonPath("$.data").value(new HashMap<String,Object>()))
                .andReturn();
        logger.error(resultQuery.getResponse().getContentAsString());
    }


    /**
     * 测试移除关系-关系不存在,Unsupported Media Type 415
     *
     * @throws Exception
     */
    @Test(expected = AssertionError.class)
    public void testRemoveEdgeMediaType() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();

        MvcResult result = mockMvc.perform(delete("/graph/storage/v1/edge/123456")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_ATOM_XML))
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andReturn();

        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 测试移除关系-关系不存在-正常,方法不被允许
     *
     * @throws Exception
     */
    @Test
    public void testRemoveEdgeMethodNotAllowed() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();

        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge/123456")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();

        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 测试移除关系-关系不存在-正常
     *
     * @throws Exception
     */
    @Test
    public void testRemoveEdgeNonExist() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();

        MvcResult result = mockMvc.perform(delete("/graph/storage/v1/edge/99999999999999999")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Check.CH_03.getId()))
                .andReturn();

        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 测试移除关系没有id
     *
     * @throws Exception
     */
    @Test
    public void testRemoveEdgeNoId() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();

        MvcResult result = mockMvc.perform(delete("/graph/storage/v1/edge")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT, HEADER_VALUE)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(405))
                .andReturn();

        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 移除关系测试，没有header
     *
     * @throws Exception
     */
    @Test
    public void testRemoveEdgeNoHeader() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();

        MvcResult result = mockMvc.perform(delete("/graph/storage/v1/edge/123456")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(400))
                .andReturn();

        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 测试获取可能认识的人，正常
     * @throws Exception
     */
    @Test
    public void testSameNeighbors() throws  Exception{

        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> edge = new HashedMap<String, Object>();
        edge.put("type",Type);
        edge.put(PROPERTIES,new HashMap<String,Object>());
        map.put("edge",edge);
        map.put(LIMIT,20);
        map.put(SKIP,0);
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId +
        "/same/neighbors")
                .header(APPTENANT,HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andExpect(jsonPath("$.data.total").isNotEmpty())
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 测试获取可能认识的人，媒体类型不对
     * @throws Exception
     */
    @Test
    public void testSameNeighborsMediaType() throws  Exception{

        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> edge = new HashedMap<String, Object>();
        edge.put("type",Type);
        edge.put(PROPERTIES,new HashMap<String,Object>());
        map.put("edge",edge);
        map.put(LIMIT,20);
        map.put(SKIP,0);
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId +
        "/same/neighbors")
                .header(APPTENANT,HEADER_VALUE)
                .contentType(MediaType.APPLICATION_ATOM_XML)
                .content(JSON.toJSONString(map)))
                .andExpect(status().is(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 测试获取可能认识的人，方法不被允许
     * @throws Exception
     */
    @Test
    public void testSameNeighborsMethodNotAllowed() throws  Exception{

        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> edge = new HashedMap<String, Object>();
        edge.put("type",Type);
        edge.put(PROPERTIES,new HashMap<String,Object>());
        map.put("edge",edge);
        map.put(LIMIT,20);
        map.put(SKIP,0);
        MvcResult result = mockMvc.perform(get("/graph/storage/v1/vertex/" + VertexId +
        "/same/neighbors")
                .header(APPTENANT,HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(map)))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 测试获取可能认识的人，正常
     * @throws Exception
     */
    @Test
    public void testSameNeighborsNoHeader() throws  Exception{

        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> edge = new HashedMap<String, Object>();
        edge.put("type",Type);
        edge.put(PROPERTIES,new HashMap<String,Object>());
        map.put("edge",edge);
        map.put(LIMIT,20);
        map.put(SKIP,0);
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId +
        "/same/neighbors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(map)))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 测试获取可能认识的人，没有body
     * @throws Exception
     */
    @Test
    public void testSameNeighborsNoBody() throws  Exception{

        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> edge = new HashedMap<String, Object>();
        edge.put("type",Type);
        edge.put(PROPERTIES,new HashMap<String,Object>());
        map.put("edge",edge);
        map.put(LIMIT,20);
        map.put(SKIP,0);
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId +
                "/same/neighbors")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT,HEADER_VALUE))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 测试获取可能认识的人，顶点不存在
     * @throws Exception
     */
    @Test
    public void testSameNeighborsVertexNonExist() throws  Exception{

        Map<String,Object> map = new HashedMap<String, Object>();
        Map<String,Object> edge = new HashedMap<String, Object>();
        edge.put("type",Type);
        edge.put(PROPERTIES,new HashMap<String,Object>());
        map.put("edge",edge);
        map.put(LIMIT,20);
        map.put(SKIP,0);
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId + "**" +
                "/same/neighbors")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APPTENANT,HEADER_VALUE)
                .content(JSON.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Code.CO_42.getId()))
                .andExpect(jsonPath("$.data.total").value(0))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * tianxy start 2017-08-16
     */

    /**
     * 查询边：
     * 必填项为空（ID）
     */
    @Test
    public void testObtainEdge1() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/graph/storage/v1/edge/")
                .header(APPTENANT, "Dept")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 查询边：
     * 必填项为空（没有apptenant参数）
     */
    @Test
    public void testObtainEdge2() throws Exception {
    	Random random = new Random();
        long id = relationShipIdList.get(random.nextInt(relationShipIdList.size()));
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/graph/storage/v1/edge/" + id)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 查询边：
     * 必填项为空（apptenant参数值为""）
     */
    @Test
    public void testObtainEdge3() throws Exception {
    	Random random = new Random();
        long id = relationShipIdList.get(random.nextInt(relationShipIdList.size()));
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/graph/storage/v1/edge/" + id)
                .header(APPTENANT, "")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA27.getId()))
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 查询边：
     * 正常
     */
    @Test
    public void testObtainEdge5() throws Exception {
    	Random random = new Random();
        long id = relationShipIdList.get(random.nextInt(relationShipIdList.size()));
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/graph/storage/v1/edge/" + id)
                .header(APPTENANT,HEADER_VALUE)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))    // 响应类型
                .andExpect(jsonPath("$.code").value(SUCCESS))    // 查询正确
                .andExpect(jsonPath("$.data.type").value(Type))
                .andExpect(jsonPath("$.data.id").value(id))
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 查询边：
     * 不存在查询
     */
    @Test
    public void testObtainEdge6() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/graph/storage/v1/edge/3395212")
                .header(APPTENANT, HEADER_VALUE)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))    // 响应类型
                .andExpect(jsonPath("$.code").value(Code.CO_24.getId()))    // 边不存在
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 查询边：
     * 请求类型错误
     */
    @Test
    public void testObtainEdge7() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/graph/storage/v1/edge/edge001")
                .header(APPTENANT, "Dept")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 获取任意两节点之间的关系：
     * 参数为空(limit、skip)
     */
    @Test
    public void testObtainEdgeBetweenVertex1() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/graph/storage/v1/edge/between/vertex/startId0001/endId0001")
                .header(APPTENANT, HEADER_VALUE)
//                .param("skip", "1")
                .param("limit", "2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 获取任意两节点之间的关系：
     * 参数为空(v1、v2)
     */
    @Test
    public void testObtainEdgeBetweenVertex2() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/graph/storage/v1/edge/between/vertex/001")
                .header(APPTENANT, "Dept")
                .param("skip", "1")
                .param("limit", "2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 获取任意两节点之间的关系：
     * 必填项为空（没有apptenant参数）
     */
    @Test
    public void testObtainEdgeBetweenVertex3() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/graph/storage/v1/edge/between/vertex/001/002")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 获取任意两节点之间的关系：
     * 参数为空(apptenant)
     */
    @Test
    public void testObtainEdgeBetweenVertex4() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/graph/storage/v1/edge/between/vertex/001/002")
                .header(APPTENANT, "")
                .param("skip", "1")
                .param("limit", "2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA03.getId()))
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 获取任意两节点之间的关系：
     * 顶点不存在查询
     */
    @Test
    public void testObtainEdgeBetweenVertex5() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/graph/storage/v1/edge/between/vertex/0012/003")
                .header(APPTENANT, "Dept")
                .param("skip", "1")
                .param("limit", "2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Code.CO_28.getId()))
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 获取任意两节点之间的关系：
     * 关系不存在查询
     */
    @Test
    public void testObtainEdgeBetweenVertex6() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/graph/storage/v1/edge/between/vertex/startId0002/startId0003")
                .header(APPTENANT,HEADER_VALUE)
                .param("skip", "1")
                .param("limit", "2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Code.CO_28.getId()))
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 获取任意两节点之间的关系：
     * 请求类型错误
     */
    @Test
    public void testObtainEdgeBetweenVertex7() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/graph/storage/v1/edge/between/vertex/001/id_01")
                .header(APPTENANT, "Dept")
                .param("skip", "1")
                .param("limit", "2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 获取任意两节点之间的关系：
     * 正常情况
     */
    @Test
    public void testObtainEdgeBetweenVertex8() throws Exception {
    	
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/graph/storage/v1/edge/between/vertex/startId0001/endId0001")
                .header(APPTENANT,HEADER_VALUE)
                .param("skip", "0")
                .param("limit", "5")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andExpect(jsonPath("$.data.total").isNotEmpty())
                .andExpect(jsonPath("$.data.edges.size()").isNotEmpty())
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 获取任意两节点之间的关系id：
     * 参数为空(limit、skip)
     */
    @Test
    public void testObtainEdgeIdsBetweenVertex1() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/graph/storage/v1/edgeIds/between/vertex/startId0001/endId0001")
                .header(APPTENANT, HEADER_VALUE)
//                .param("skip", "0")
                .param("limit", "2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 获取任意两节点之间的关系id：
     * 参数为空(v1、v2)
     */
    @Test
    public void testObtainEdgeIdsBetweenVertex2() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/graph/storage/v1/edgeIds/between/vertex/001")
                .header(APPTENANT, "Dept")
                .param("skip", "0")
                .param("limit", "2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 获取任意两节点之间的关系id：
     * 必填项为空（没有apptenant参数）
     */
    @Test
    public void testObtainEdgeIdsBetweenVertex3() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/graph/storage/v1/edge/between/vertex/001/002")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 获取任意两节点之间的关系id：
     * 参数为空(apptenant)
     */
    @Test
    public void testObtainEdgeIdsBetweenVertex4() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/graph/storage/v1/edgeIds/between/vertex/001/002")
                .header(APPTENANT, "")
                .param("skip", "0")
                .param("limit", "2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA03.getId()))
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 获取任意两节点之间的关系id：
     * 顶点不存在查询
     */
    @Test
    public void testObtainEdgeIdsBetweenVertex5() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/graph/storage/v1/edgeIds/between/vertex/0010/003")
                .header(APPTENANT, "Dept")
                .param("skip", "0")
                .param("limit", "2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Code.CO_28.getId()))
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 获取任意两节点之间的关系id：
     * 关系不存在查询
     */
    @Test
    public void testObtainEdgeIdsBetweenVertex6() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/graph/storage/v1/edgeIds/between/vertex/startId0002/startId0003")
                .header(APPTENANT, HEADER_VALUE)
                .param("skip", "0")
                .param("limit", "2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Code.CO_28.getId()))
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 获取任意两节点之间的关系id：
     * 请求类型错误
     */
    @Test
    public void testObtainEdgeIdsBetweenVertex7() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/graph/storage/v1/edgeIds/between/vertex/001/002")
                .header(APPTENANT, "Dept")
                .param("skip", "0")
                .param("limit", "2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 获取任意两节点之间的关系id：
     * 正常情况
     */
    @Test
    public void testObtainEdgeIdsBetweenVertex8() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/graph/storage/v1/edgeIds/between/vertex/startId0001/endId0001")
                .header(APPTENANT, HEADER_VALUE)
                .param("skip", "0")
                .param("limit", "4")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andExpect(jsonPath("$.data.total").isNotEmpty())
                .andExpect(jsonPath("$.data.edgeIds.size()").isNotEmpty())
                .andReturn();
        logger.error(mvcResult.getResponse().getContentAsString());
    }

    /**
     * 修改边属性：
     * 正常情况
     */
    @Test
    public void testUpdateEdge() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("age", 29);
        map.put(PROPERTIES, properties);
        Random random = new Random();
        long id = relationShipIdList.get(random.nextInt(relationShipIdList.size()));
        // 修改
        MvcResult result = mockMvc.perform(put("/graph/storage/v1/edge/" + id)
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andReturn();
        // 查询修改是否正确
        MvcResult result1 = mockMvc.perform(get("/graph/storage/v1/edge/" + id)
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andExpect(jsonPath("$.data.properties.age").value(29))
                .andReturn();
        
        logger.error(result.getResponse().getContentAsString());
        logger.error(result1.getResponse().getContentAsString());
    }

    /**
     * 修改边属性：
     * 关键字
     */
    @Test
    public void testUpdateEdge1() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("age", 29);
        properties.put("id", "00001");
        map.put(PROPERTIES, properties);
        Random random = new Random();
        long id = relationShipIdList.get(random.nextInt(relationShipIdList.size()));
        MvcResult result = mockMvc.perform(put("/graph/storage/v1/edge/" + id)
                .header(APPTENANT, "Dept")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA29.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 修改边属性：
     * 不存在更新
     */
    @Test
    public void testUpdateEdge2() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("age", 29);
        map.put(PROPERTIES, properties);

        MvcResult result = mockMvc.perform(put("/graph/storage/v1/edge/33952123")
                .header(APPTENANT, "Dept")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Check.CH_03.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 修改边属性：
     * 请求类型错误
     */
    @Test
    public void testUpdateEdge4() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("age", 29);
        map.put(PROPERTIES, properties);
        Random random = new Random();
        long id = relationShipIdList.get(random.nextInt(relationShipIdList.size()));
        MvcResult result = mockMvc.perform(post("/graph/storage/v1/edge/" + id)
                .header(APPTENANT, "Dept")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 修改边属性：
     * 非必填properties为空 
     */
    @Test
    public void testUpdateEdge5() throws Exception {
    	Random random = new Random();
        long id = relationShipIdList.get(random.nextInt(relationShipIdList.size()));
        MvcResult result = mockMvc.perform(put("/graph/storage/v1/edge/" + id)
                .header(APPTENANT, "Dept")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 修改边属性：
     * apptenant为空
     */
    @Test
    public void testUpdateEdge6() throws Exception {
    	Random random = new Random();
        long id = relationShipIdList.get(random.nextInt(relationShipIdList.size()));
        MvcResult result = mockMvc.perform(put("/graph/storage/v1/edge/" + id)
                .header(APPTENANT, "")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA27.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * 	修改边属性：
     * 		属性的key的定义不符合规定
     */
    @Test
    public void testUpdateEdge7() throws Exception {
    	Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("age", 29);
        properties.put("123", "test");
        map.put(PROPERTIES,properties);

        Random random = new Random();
        long id = relationShipIdList.get(random.nextInt(relationShipIdList.size()));
        MvcResult result = mockMvc.perform(put("/graph/storage/v1/edge/" + id)
                .header(APPTENANT, "Dept")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA40.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 	修改边属性：
     * 		属性的key的长度不符合约定
     */
    @Test
    public void testUpdateEdge8() throws Exception {
    	Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("age", 29);
        properties.put("test" + keyLength, "test");
        map.put(PROPERTIES,properties);
        Random random = new Random();
        long id = relationShipIdList.get(random.nextInt(relationShipIdList.size()));
        MvcResult result = mockMvc.perform(put("/graph/storage/v1/edge/" + id)
                .header(APPTENANT, "Dept")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA37.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 	修改边属性：
     * 		属性的value的长度不符合约定
     */
    @Test
    public void testUpdateEdge9() throws Exception {
    	Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("age", 29);
        properties.put("test", "test" + valueLength);
        map.put(PROPERTIES,properties);

        Random random = new Random();
        long id = relationShipIdList.get(random.nextInt(relationShipIdList.size()));
        MvcResult result = mockMvc.perform(put("/graph/storage/v1/edge/" + id)
                .header(APPTENANT, "Dept")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA38.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 修改边属性：
     * 必填项边的ID为空
     */
    @Test
    public void testUpdateEdge10() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("age", 29);
        properties.put("code", "00001");
        map.put(PROPERTIES, properties);
        MvcResult result = mockMvc.perform(put("/graph/storage/v1/edge/")
                .header(APPTENANT, "Dept")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(map)))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：双向
     * 正常
     */
    @Test
    public void testObtainBothEdgeByVertex() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	Edge edge = new Edge();
//    	edge.setType(Type);
    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	edgeV.setEdge(edge);
    	
    	MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId + "/edge/both")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Code.CO_33.getId()))
                .andExpect(jsonPath("$.data.total").isNotEmpty())
                .andExpect(jsonPath("$.data.edges.size()").isNotEmpty())
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：双向
     * 顶点不存在
     */
    @Test
    public void testObtainBothEdgeByVertex1() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	Edge edge = new Edge();
    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	edgeV.setEdge(edge);
    	
    	MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/11/edge/both")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Code.CO_28.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：双向
     * 租户为空
     */
    @Test
    public void testObtainBothEdgeByVertex2() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	Edge edge = new Edge();
    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	edgeV.setEdge(edge);
    	
    	MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId + "/edge/both")
                .header(APPTENANT, "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA03.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：双向
     * 请求类型错误
     */
    @Test
    public void testObtainBothEdgeByVertex3() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	Edge edge = new Edge();
    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	edgeV.setEdge(edge);
    	
    	MvcResult result = mockMvc.perform(get("/graph/storage/v1/vertex/" + VertexId + "/edge/both")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：双向
     * 边属性值长度不合法
     */
    @Test
    public void testObtainBothEdgeByVertex4() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	Edge edge = new Edge();
    	Map<String, Object> properties = new HashMap<String, Object>();
    	properties.put("namespace", "test" + valueLength);
    	edge.setProperties(properties);
    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	edgeV.setEdge(edge);
    	
    	MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId + "/edge/both")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA38.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：双向
     * 边属性key长度不合法
     */
    @Test
    public void testObtainBothEdgeByVertex5() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	Edge edge = new Edge();
    	Map<String, Object> properties = new HashMap<String, Object>();
    	properties.put("namespace" + keyLength, HEADER_VALUE);
    	edge.setProperties(properties);
    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	edgeV.setEdge(edge);
    	
    	MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId + "/edge/both")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA37.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：双向
     * 非必填项skip为空
     */
    @Test
    public void testObtainBothEdgeByVertex6() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	Edge edge = new Edge();
//    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	edgeV.setEdge(edge);
    	
    	MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId + "/edge/both")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Code.CO_33.getId()))
                .andExpect(jsonPath("$.data.total").isNotEmpty())
                .andExpect(jsonPath("$.data.edges.size()").isNotEmpty())
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：双向
     * 非必填项edge为空
     */
    @Test
    public void testObtainBothEdgeByVertex7() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	
    	MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId + "/edge/both")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Code.CO_35.getId()))
                .andExpect(jsonPath("$.data.total").isNotEmpty())
                .andExpect(jsonPath("$.data.edges.size()").isNotEmpty())
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：入向
     * 正常
     */
    @Test
    public void testObtainInEdgeByVertex() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	Edge edge = new Edge();
    	edge.setType(Type);
    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	edgeV.setEdge(edge);
    	
    	MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId + "/edge/in")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Code.CO_34.getId()))
                .andExpect(jsonPath("$.data.total").isNotEmpty())
                .andExpect(jsonPath("$.data.edges.size()").isNotEmpty())
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：入向
     * 顶点不存在
     */
    @Test
    public void testObtainInEdgeByVertex1() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	Edge edge = new Edge();
    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	edgeV.setEdge(edge);
    	
    	MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/11/edge/in")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Code.CO_28.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：入向
     * 租户为空
     */
    @Test
    public void testObtainInEdgeByVertex2() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	Edge edge = new Edge();
    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	edgeV.setEdge(edge);
    	
    	MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId + "/edge/in")
                .header(APPTENANT, "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA03.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：入向
     * 请求类型错误
     */
    @Test
    public void testObtainInEdgeByVertex3() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	Edge edge = new Edge();
    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	edgeV.setEdge(edge);
    	
    	MvcResult result = mockMvc.perform(get("/graph/storage/v1/vertex/" + VertexId + "/edge/in")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：入向
     * 边属性值长度不合法
     */
    @Test
    public void testObtainInEdgeByVertex4() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	Edge edge = new Edge();
    	Map<String, Object> properties = new HashMap<String, Object>();
    	properties.put("namespace", "test" + valueLength);
    	edge.setProperties(properties);
    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	edgeV.setEdge(edge);
    	
    	MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId + "/edge/in")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA38.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：入向
     * 边属性key长度不合法
     */
    @Test
    public void testObtainInEdgeByVertex5() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	Edge edge = new Edge();
    	Map<String, Object> properties = new HashMap<String, Object>();
    	properties.put("namespace" + keyLength, HEADER_VALUE);
    	edge.setProperties(properties);
    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	edgeV.setEdge(edge);
    	
    	MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId + "/edge/in")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA37.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：入向
     * 非必填项skip为空
     */
    @Test
    public void testObtainInEdgeByVertex6() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	Edge edge = new Edge();
//    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	edgeV.setEdge(edge);
    	
    	MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId + "/edge/in")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Code.CO_34.getId()))
                .andExpect(jsonPath("$.data.total").isNotEmpty())
                .andExpect(jsonPath("$.data.edges.size()").isNotEmpty())
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：入向
     * 非必填项edge为空
     */
    @Test
    public void testObtainInEdgeByVertex7() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	
    	MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId + "/edge/in")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Code.CO_34.getId()))
                .andExpect(jsonPath("$.data.total").isNotEmpty())
                .andExpect(jsonPath("$.data.edges.size()").isNotEmpty())
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：出向
     * 正常
     */
    @Test
    public void testObtainOutEdgeByVertex() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	Edge edge = new Edge();
    	Map<String, Object> properties = new HashMap<String, Object>();
    	properties.put("namespace", HEADER_VALUE);
    	edge.setType(Type);
    	edge.setProperties(properties);
    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	edgeV.setEdge(edge);
    	
    	MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId + "/edge/out")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Code.CO_35.getId()))
                .andExpect(jsonPath("$.data.total").isNotEmpty())
                .andExpect(jsonPath("$.data.edges.size()").isNotEmpty())
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：出向
     * 顶点不存在
     */
    @Test
    public void testObtainOutEdgeByVertex1() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	Edge edge = new Edge();
    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	edgeV.setEdge(edge);
    	
    	MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/11/edge/out")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Code.CO_28.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：出向
     * 租户为空
     */
    @Test
    public void testObtainOutEdgeByVertex2() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	Edge edge = new Edge();
    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	edgeV.setEdge(edge);
    	
    	MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId + "/edge/out")
                .header(APPTENANT, "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA03.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：出向
     * 请求类型错误
     */
    @Test
    public void testObtainOutEdgeByVertex3() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	Edge edge = new Edge();
    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	edgeV.setEdge(edge);
    	
    	MvcResult result = mockMvc.perform(get("/graph/storage/v1/vertex/" + VertexId + "/edge/out")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：出向
     * 边属性值长度不合法
     */
    @Test
    public void testObtainOutEdgeByVertex4() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	Edge edge = new Edge();
    	Map<String, Object> properties = new HashMap<String, Object>();
    	properties.put("namespace", "test" + valueLength);
    	edge.setProperties(properties);
    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	edgeV.setEdge(edge);
    	
    	MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId + "/edge/out")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA38.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：出向
     * 边属性key长度不合法
     */
    @Test
    public void testObtainOutEdgeByVertex5() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	Edge edge = new Edge();
    	Map<String, Object> properties = new HashMap<String, Object>();
    	properties.put("namespace" + keyLength, HEADER_VALUE);
    	edge.setProperties(properties);
    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	edgeV.setEdge(edge);
    	
    	MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId + "/edge/out")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Validate.VA37.getId()))
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：出向
     * 非必填项skip为空
     */
    @Test
    public void testObtainOutEdgeByVertex6() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	Edge edge = new Edge();
        Map<String,Object> properties = new HashMap<String, Object>();
        properties.put("age",19);
        edge.setProperties(properties);
//    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	edgeV.setEdge(edge);
    	
    	MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId + "/edge/out")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Code.CO_35.getId()))
                .andExpect(jsonPath("$.data.total").isNotEmpty())
                .andExpect(jsonPath("$.data.edges.size()").isNotEmpty())
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }
    
    /**
     * 新增根据顶点查询边的接口：出向
     * 非必填项edge为空
     */
    @Test
    public void testObtainOutEdgeByVertex7() throws Exception {
    	EdgeV edgeV = new EdgeV();
    	edgeV.setSkip(0);
    	edgeV.setLimit(3);
    	
    	MvcResult result = mockMvc.perform(post("/graph/storage/v1/vertex/" + VertexId + "/edge/out")
                .header(APPTENANT, HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(edgeV)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code").value(Code.CO_35.getId()))
                .andExpect(jsonPath("$.data.total").isNotEmpty())
                .andExpect(jsonPath("$.data.edges.size()").isNotEmpty())
                .andReturn();
        logger.error(result.getResponse().getContentAsString());
    }

    /**
     * tianxy end 2017-08-16
     */

    @After
    public void tearDown() throws Exception {
        session.purgeDatabase();
    }

    /**
     * 指定长度的字符串
     * @param length 指定长度
     * @return 返回字符串
     */
    public String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < length; ++i) {
            int number = random.nextInt(62);// [0,62)
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
}