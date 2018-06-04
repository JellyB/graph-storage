package com.wdcloud.data.neo4j.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.Random;

/**
 * @author CHENYB
 * @since 2016年10月22日
 */
public class BatchRest {

	private static final Log log = LogFactory.getLog(BatchRest.class);
//	String serverUrl = "http://192.168.8.219:9000/ptyhzx-bhv/engine";
	String serverUrl = "http://192.168.6.138:9000/ptyhzx-bhv/engine";
//	String serverUrl = "http://usr.wdcloud.cc/ptyhzx-bhv/engine";//生产环境
//	String serverUrl = "http://enusr.vviton.com/ptyhzx-bhv/engine";//生产环境国际版

	/**
	 * @author CHENYB
	 * @param args
	 * @since 2016年10月22日 下午2:39:18
	 */
	public static void main(String[] args) {
		BatchRest rest = new BatchRest();
		for (int i = 0; i < 1; i++) {
			rest.batchInvok("ptyhzx", 100, 30);
			try {
			} catch (Exception e) {
				log.error(e);
			}
		}
	}

	/**
	 * 跑测试数据数据
	 * @author CHENYB
	 * @param namepace 命名空间 
	 * @param person 人数
	 * @param item 单个物品数
	 * @since 2016年10月22日 下午2:49:10
	 */
	public void batchInvok(String namepace, int person, int item) {
		String[] items = { "person", "book", "article", "circle", "weibo" };
		//String[] actions = { "create", "share", "collect", "uncollect", "like", "dislike", "comment", "reply", "grade", "follow", "unfollow", "view", "join" };
		//String[] actions = { "create", "follow", "join", "comment", "reply" };
		String[] actions = { "08" };
		String action = "";
		for (int i = 0; i < actions.length; i++) {
			action = getRandom(actions);
			JSONObject json = new JSONObject();
			json.put("action", action);
			json.put("user_id", new Random().nextInt(person));
			json.put("item_id", new Random().nextInt(item));
			json.put("category", "02");
			json.put("isrelated", true);
			//properties
			JSONObject props = new JSONObject();
			props.put("key1", action + " property");
			JSONArray arr = new JSONArray();
			arr.add("二十国集团");
			arr.add("b");
			arr.add(100);
			props.put("key2", arr);
			props.put("key3", 12345);
			json.put("properties", props);
			json.put("pos_type", "ll");
			json.put("related", true);
			json.put("position", "longitude:latitude");
			//env
			JSONObject env = new JSONObject();
			env.put("key", "value");
			json.put("env", env);
			json.put("bhv_datetime", System.currentTimeMillis());
			json.put("content", "中华人民共和国");
			JSONObject rating = new JSONObject();
			rating.put("total", 5);
			rating.put("score", new Random().nextInt(5));
			json.put("rating", rating);
			//2、调用
			String params = json.toJSONString();
			System.out.println(params);
//			invokRestVoid(namepace, params);
			invokRestString(namepace, params);
		}
	}

	/**
	 * 随机返回数组中的一项
	 * @author CHENYB
	 * @param array
	 * @return
	 * @since 2016年10月26日 上午10:51:05
	 */
	private String getRandom(String[] array) {
		return array[new Random().nextInt(array.length)];
	}

	/**
	 * 无返回值调用
	 * @author CHENYB
	 * @param params
	 * @since 2016年10月22日 下午2:55:15
	 */
	private void invokRestVoid(String account, String params) {
		String path = "/v1/" + account + "/logtrace";
		AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();
		HttpHeaders headers = new HttpHeaders();
		//MediaType
		MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
		headers.setContentType(type);
		headers.add("Accept", MediaType.APPLICATION_JSON.toString());

		HttpEntity<String> formEntity = new HttpEntity<String>(params, headers);
		ListenableFuture<ResponseEntity<Void>> forEntity = asyncRestTemplate.postForEntity(serverUrl + path, formEntity,
				Void.class);
		//异步调用后的回调函数
		forEntity.addCallback(new ListenableFutureCallback<ResponseEntity<Void>>() {
			//调用失败
			@Override
			public void onFailure(Throwable e) {
				log.error("=====rest response faliure======" + e.getMessage());
				//e.printStackTrace();
			}

			//调用成功
			@Override
			public void onSuccess(ResponseEntity<Void> result) {
				log.info(result.getStatusCode());
			}
		});
	}

	/**
	 * 有返回值调用
	 * @author CHENYB
	 * @param params
	 * @since 2016年10月22日 下午2:55:02
	 */
	private void invokRestString(String account, String params) {
		String path = "/v1/" + account + "/logtrace";
		AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();
		HttpHeaders headers = new HttpHeaders();

		//MediaType
		MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
		headers.setContentType(type);
		headers.add("Accept", MediaType.APPLICATION_JSON.toString());

		HttpEntity<String> formEntity = new HttpEntity<String>(params, headers);
		ListenableFuture<ResponseEntity<String>> forEntity = asyncRestTemplate.postForEntity(serverUrl + path, formEntity,
				String.class);
		//异步调用后的回调函数
		forEntity.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {
			//调用失败
			@Override
			public void onFailure(Throwable e) {
				log.error("=====rest response faliure======" + e.getMessage());
				//e.printStackTrace();
			}

			//调用成功
			@Override
			public void onSuccess(ResponseEntity<String> result) {
				log.info("--->async rest response success----\n" + result);
			}
		});
	}

}
