package com.springrest.delivery.resources;

public class Order {
	private Integer orderId;
	private String status;
	private Integer agentId;
	public Order() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Order(Integer orderId, String status, Integer agentId) {
		super();
		this.orderId = orderId;
		this.status = status;
		this.agentId = agentId;
	}
	public Integer getOrderId() {
		return orderId;
	}
	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getAgentId() {
		return agentId;
	}
	public void setAgentId(Integer agentId) {
		this.agentId = agentId;
	}
	@Override
	public String toString() {
		return "Order [orderId=" + orderId + ", status=" + status + ", agentId=" + agentId + "]";
	}
	
	
	
}
