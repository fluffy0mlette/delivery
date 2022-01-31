package com.springrest.delivery.resources;

public class Agent {
	private Integer agentId;
	private String status;
	public Agent(Integer agentId, String status) {
		super();
		this.agentId = agentId;
		this.status = status;
	}
	public Agent() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Integer getAgentId() {
		return agentId;
	}
	public void setAgentId(Integer agentId) {
		this.agentId = agentId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	@Override
	public String toString() {
		return "Agent [agentId=" + agentId + ", status=" + status + "]";
	}
	
	
	
	
	
}
