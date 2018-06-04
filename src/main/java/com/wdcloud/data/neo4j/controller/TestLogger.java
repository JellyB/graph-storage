package com.wdcloud.data.neo4j.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.entries;

/**
 * Created by bigd on 2017/11/15.
 */

@Controller
@RequestMapping(value = "/logback/test/")
public class TestLogger {

    private static Logger logger = LoggerFactory.getLogger(BaseController.class);

    @RequestMapping(value ="/self",method = RequestMethod.GET,produces = "application/json")
    @ResponseBody
    public void testLog(){

        logger.error("json log test !!!!");
        Map myMap = new HashMap();
        myMap.put("name1", "value1");
        myMap.put("name2", "value2");
        logger.info("log message {}", entries(myMap));
    }
}
