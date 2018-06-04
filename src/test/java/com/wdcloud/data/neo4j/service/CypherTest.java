package com.wdcloud.data.neo4j.service;

import com.alibaba.fastjson.JSON;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.values.JcPath;
import iot.jcypher.query.writer.Format;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.ogm.model.Result;
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
 * Created by bigd on 2017/7/14.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class CypherTest {

    private Logger logger = LoggerFactory.getLogger(CypherTest.class);


    @Autowired
    private Session session;


    @Test
    public void testCyper(){
        Map<String,String> parameters = new HashMap<String,String>();
        StringBuilder  strBuilder = new StringBuilder();
        /*strBuilder
                .append(" Match p=(n)-[:")
                .append(type)
                .append("]->(x)<-[:")
                .append(type)
                .append("]-(m) ")
                .append(" Where n.id = '")
                .append(id)
                .append("' And n.account = '")
                .append(account)
                .append("' And size((n)-[:")
                .append(type)
                .append("]->(m)) = 0 ")
                .append("  return m.id as uid, count(x) as count ")
                .append("  ORDER BY count DESC ")
                .append(" SKIP ")
                .append(skip)
                .append(" LIMIT ")
                .append(limit);*/
        /*strBuilder.append("Match p =(n)-[r]->(m) where type(r)={type} return r");*/
        strBuilder.append("Match p =(n)-[r:{type}]->(m) return r");

        parameters.put("type","RENSHI");
       /* parameters.put("type","RENSHI");
        parameters.put("type","RENSHI");
        parameters.put("type","RENSHI");
        parameters.put("type","RENSHI");*/

        Result result = session.query(strBuilder.toString(),parameters);


        logger.error(JSON.toJSONString(result));
    }


    public static void test(){
        JcQuery jcQuery = new JcQuery();
        JcPath jcPath = new JcPath("jcPath");
        jcQuery.setClauses(new IClause[]{
                MATCH.path(jcPath).node().relation().minHops(4).out().type("Friend").node(),
                RETURN.value(jcPath)
        });
        String cypher = iot.jcypher.util.Util.toCypher(jcQuery, Format.NONE);
        System.err.print(cypher);

    }

    public static void main(String[]args){
        test();
    }
}
