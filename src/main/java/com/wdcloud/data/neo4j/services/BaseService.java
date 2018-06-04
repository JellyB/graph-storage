package com.wdcloud.data.neo4j.services;


import com.wdcloud.data.neo4j.entity.*;

/**
 * Created by bigd on 2017/6/26.
 * 图存储服务基础api接口
 */
public interface BaseService {


    QueryResult createVertex(Vertex vertex);

    QueryResult updateVertex(Vertex vertex);

    QueryResult obtainVertex(Vertex vertex);

    QueryResult obtainVertexOutV(VertexV vertex);

    QueryResult obtainVertexInV(VertexV vertex);

    QueryResult obtainVertexBothV(VertexV vertex);

    QueryResult obtainVertexOutIds(VertexV vertex);

    QueryResult obtainVertexInIds(VertexV vertex);

    QueryResult obtainVertexBothIds(VertexV vertex);

    QueryResult removeVertexEdges(Vertex vertex);

    QueryResult createEdgeNodeNonExistent(Path path);

    QueryResult createEdge(RelationShip relationShip);

    QueryResult removeEdge(Edge edge);

	QueryResult obtainEdge(Edge edge);

	QueryResult obtainEdgeBetweenVertex(Vertex vertex1, Vertex vertex2, int skip, int limit);

	QueryResult obtainEdgeIdsBetweenVertex(Vertex vertex1, Vertex vertex2, int skip, int limit);

	QueryResult updateEdge(Edge edge);

	QueryResult obtainBothEdgeByVertex(Vertex vertex, EdgeV edgeV);

	QueryResult obtainInEdgeByVertex(Vertex vertex, EdgeV edgeV);

	QueryResult obtainOutEdgeByVertex(Vertex vertex, EdgeV edgeV);

    QueryResult sameNeighbors(String vertexId, EdgeV edgeV);
}
