package com.wdcloud.data.neo4j.entity;

public class EdgeV {

	/**
	 * 用户自定义
	 */
	private int skip;
	private int limit;
	private Edge edge;

	public int getSkip() {
		return skip;
	}

	public void setSkip(int skip) {
		this.skip = skip;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public Edge getEdge() {
		return edge;
	}

	public void setEdge(Edge edge) {
		this.edge = edge;
	}

}
