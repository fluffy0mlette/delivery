package com.springrest.delivery.resources;

public class Agent {
	private Integer agentId;
	private String agentState;
	public Agent(Integer agentId, String agentState) {
		super();
		this.agentId = agentId;
		this.agentState = agentState;
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
	public String getAgentState() {
		return agentState;
	}
	public void setAgentState(String agentState) {
		this.agentState = agentState;
	}
	@Override
	public String toString() {
		return "Agent [agentId=" + agentId + ", agentState=" + agentState + "]";
	}
	
	
	
	
	
}
