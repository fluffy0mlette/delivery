package com.springrest.delivery;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.springrest.delivery.resources.Agent;
import com.springrest.delivery.resources.Order;

@RestController
public class DeliveryController {

	ArrayList<Agent> agentList = new ArrayList<Agent>();
	ArrayList<Order> orderList = new ArrayList<Order>();
	PriorityQueue<Integer> pendingOrders = new PriorityQueue<Integer>();
	
	@PostMapping("/addAgent")
	public HttpStatus addAgent(@RequestBody Agent myInput) {
		
		agentList.add(myInput);
		return HttpStatus.CREATED;
		
	}
	
	@GetMapping("/showAgents")
	public ArrayList<Agent> showAgents() {
		return agentList;
	}
	
	@PostMapping("/agentSignIn")
	public HttpStatus agentSignIn(@RequestBody Map<String,Integer> agent) {
		for(Agent myAgent : agentList) {
			if(myAgent.getAgentId().equals(agent.get("agentId"))) {
				if(myAgent.getAgentState() == "signed-out") {
					if(pendingOrders.isEmpty()) { //order pending
						myAgent.setAgentState("available");
					}
					else  { 
						Integer orderId = pendingOrders.poll();
						for(Order myOrder : orderList) {
							if(myOrder.getOrderId() == orderId) {
								myOrder.setStatus("assigned");
								myOrder.setAgentId(agent.get("agentId"));
							}
						}
						myAgent.setAgentState("unavailable");
					}
				}
				break;
			}
		}
		return HttpStatus.CREATED;
	}
	
	@PostMapping("/agentSignOut")
	public HttpStatus agentSignOut(@RequestBody Map<String,Integer> agent) {
		for(Agent myAgent : agentList) {
			System.out.println(myAgent.getAgentId());
			System.out.println(agent.get("agentId"));
			
			if(myAgent.getAgentId().equals(agent.get("agentId"))) {
				System.out.println("hello2");
				if(myAgent.getAgentState().equals("available")) {
					System.out.println("hello3");
					myAgent.setAgentState("signed-out");
				}
			}
		}
		return HttpStatus.CREATED;
	}
	
	@PostMapping("/reInitialize")
	public HttpStatus reInitialize() {
		
		File myFile = new File("/Users/fluffy/Downloads/delivery/initialData.txt");
		Scanner s1 = null;
		try {
			s1 = new Scanner(myFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		while(s1.hasNextLine()) {
			String str = s1.nextLine();
			if(str.charAt(0) == '*') {
				break;
			}
		}	
		
		while(s1.hasNextLine()) {
			String str = s1.nextLine();
			if(str.charAt(0) == '*') {
				break;
			}
			Agent agent = new Agent(Integer.parseInt(str),"signed-out");
			agentList.add(agent);
		}

		return HttpStatus.CREATED;	
	}
	
	@EventListener(ApplicationReadyEvent.class)
	public HttpStatus StartUp() {
			
			File myFile = new File("/Users/fluffy/Downloads/delivery/initialData.txt");
			Scanner s1 = null;
			try {
				s1 = new Scanner(myFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			while(s1.hasNextLine()) {
				String str = s1.nextLine();
				if(str.charAt(0) == '*') {
					break;
				}
			}	
			
			while(s1.hasNextLine()) {
				String str = s1.nextLine();
				if(str.charAt(0) == '*') {
					break;
				}
				Agent agent = new Agent(Integer.parseInt(str),"signed-out");
				agentList.add(agent);
			}
	
			return HttpStatus.CREATED;	
		}
	
}
