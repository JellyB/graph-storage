/************************************************************************
 * Copyright (c) 2014 IoT-Solutions e.U.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ************************************************************************/

package com.wdcloud.data.neo4j.util;

import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrProperty;
import iot.jcypher.graph.GrRelation;
import iot.jcypher.query.result.JcError;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.response.model.RelationshipModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Util {

	static final Logger logger = LoggerFactory.getLogger(Util.class);

	static final  String START_NODE = "startNode";

	static final  String END_NODE = "endNode";

	static final  String ID = "id";

	static final  String PROPERTIES = "properties";

	static final  String TYPE = "type";

	private Util() {
	}

	/**
	 * print errors to System.out
	 */
	public static void printErrors(List<JcError> errors) {
		StringBuilder sb = new StringBuilder();
		sb.append("---------------Errors:");
		iot.jcypher.util.Util.appendErrorList(errors, sb);
		sb.append("\n---------------end Errors:");
		String str = sb.toString();
		logger.error(str);
	}

	/**
	 * 顶点属性转为map
	 * @param grNode
	 * @return
     */
	public static Map<String,Object> properties2Map(GrNode grNode){
		HashMap<String,Object> properties = new HashMap<String, Object>();
		List<GrProperty> jcProperties = grNode.getProperties();
		for(GrProperty property : jcProperties){
			properties.put(property.getName(),property.getValue());
		}
		return properties;
	}


	/**
	 * 获取关系的属性
	 * @param grRelation 边对象
	 * @return 边属性集合
     */
	public static Map<String,Object> properties2Map(GrRelation grRelation){
		HashMap<String,Object> properties = new HashMap<String, Object>();
		List<GrProperty> jcProperties = grRelation.getProperties();
		for(GrProperty property : jcProperties){
			properties.put(property.getName(),property.getValue());
		}
		properties.put(START_NODE,grRelation.getStartNode().getProperty(ID));
		properties.put(END_NODE,grRelation.getEndNode().getProperty(ID));
		properties.put(TYPE,grRelation.getType());
		return properties;
	}

	/**
	 * 多个顶点属性转为list
	 * @param listN 顶点集合
	 * @return
     */
	public static List<Map<String,Object>> properties2List(List<GrNode> listN){
		List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> temp;
		for(GrNode grNode : listN){
			List<GrProperty> jcProperties = grNode.getProperties();
			temp = new HashMap();
			for(GrProperty property : jcProperties){
				temp.put(property.getName(),property.getValue());
			}
			list.add(temp);
		}
		return list;
	}


	public static List<String> properties2List(Result result, String string){
		List<String> list = new ArrayList();
		Iterator iterator = result.iterator();
		while(iterator.hasNext()){
			Map<String,Object> map = (Map<String, Object>) iterator.next();
			if(map.containsKey(string)){
				Object object = map.get(string);
				if(object instanceof String){
					String id = (String) object;
					list.add(id);
				}
			}
		}
		return list;
	}


	/**
	 * 结果集转map 集合
	 * @param result 查询结果
	 * @param string 查询对象string标识
     * @return map 结果集
     */
	public static List<Map<String,Object>> properties2Map(Result result, String string){
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		Map<String,Object> resultMap;
		Map<String,Object> contentMap;
		for(Map<String,Object> temp : result){
			resultMap = new HashMap<String, Object>();
			contentMap = new HashMap<String, Object>();
			if(temp.containsKey(string)){
				Object object = temp.get(string);
				if(object instanceof  NodeModel){
					NodeModel nodeModel = (NodeModel) object;
					List<Property<String, Object>> propertyList = nodeModel.getPropertyList();
					for(Property<String, Object> property : propertyList){
						contentMap.put(property.getKey(),property.getValue());
					}
					resultMap.put(ID,contentMap.get(ID));
					contentMap.remove(ID);
					resultMap.put(PROPERTIES,contentMap);
				}
				if(object instanceof RelationshipModel){
					RelationshipModel relationshipModel = (RelationshipModel) object;
					relationshipModel.getStartNode();
					List<Property<String, Object>> propertyList = relationshipModel.getPropertyList();
					for(Property<String, Object> property : propertyList){
						contentMap.put(property.getKey(),property.getValue());
					}
					resultMap.put(ID,relationshipModel.getId());
					resultMap.put(TYPE,relationshipModel.getType());
					resultMap.put(PROPERTIES,contentMap);
				}
				if(object instanceof Long){
					resultMap.put(string,object);
				}
			}
			list.add(resultMap);
		}
		return list;
	}



	/**
	 * 节点结果集转List<Map>
	 * @param result 查询结果
	 * @param string 查询对象string标识
	 * @param count 查询对象count标识
	 * @return map 结果集
	 */
	public static List<Map<String,Object>> properties2Map(Result result, String string,String count){
		List<Map<String,Object>> list = new ArrayList();
		Map<String,Object> resultMap;
		Map<String,Object> temp;
		Map<String,Object> contentMap;
		for(Iterator iterator = result.iterator();iterator.hasNext();){
			temp = (Map<String,Object>)iterator.next();
			resultMap = new HashMap<String, Object>();
			contentMap = new HashMap<String, Object>();
			if(temp.containsKey(string) && temp.containsKey(count)){
				Object object = temp.get(string);
				Object countObj  = temp.get(count);
				if(object instanceof  NodeModel){
					NodeModel nodeModel = (NodeModel) object;
					List<Property<String, Object>> propertyList = nodeModel.getPropertyList();
					for(Property<String, Object> property : propertyList){
						contentMap.put(property.getKey(),property.getValue());
					}
					resultMap.put(ID,contentMap.get(ID));
					contentMap.remove(ID);
					resultMap.put(count,Integer.valueOf(String.valueOf(countObj)));
					resultMap.put(PROPERTIES,contentMap);
				}
			}
			list.add(resultMap);
		}
		return list;
	}


	/**
	 * 数据类型转换
	 * @param type 源数据类型
	 * @param value 源数据对象
     * @return 转换后数据对象
     */
	public static Object typeConversion(String type, Object value){
		String temString = String.valueOf(value);
		if("int".equals(type)){
			return Integer.valueOf(temString);
		}else if("boolean".equals(type)){
			return new Boolean(temString);
		}else if("string".equals(type)){
			return temString;
		}else {
			return temString;
		}
	}
}
