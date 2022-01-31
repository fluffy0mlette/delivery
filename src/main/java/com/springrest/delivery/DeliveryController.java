package com.springrest.delivery;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.springrest.delivery.resources.Agent;
import com.springrest.delivery.resources.Order;

@RestController
public class DeliveryController {

	ArrayList<Agent> agentList = new ArrayList<Agent>();
	ArrayList<Order> orderList = new ArrayList<Order>();
	
	HashMap<Integer,HashMap<Integer,Integer>> priceList = new HashMap<Integer,HashMap<Integer,Integer>>();
	
	PriorityQueue<Integer> pendingOrders = new PriorityQueue<Integer>();
	PriorityQueue<Integer> availableAgents = new PriorityQueue<Integer>();
	Integer orderNum = 1000;
	
	
	@PostMapping("/addAgent")
	public HttpStatus addAgent(@RequestBody Agent myInput) {
		
		agentList.add(myInput);
		return HttpStatus.CREATED;
		
	}
	
	@GetMapping("/showAgents")
	public ArrayList<Agent> showAgents() {
		return agentList;
	}
	
	
	@GetMapping("/itemPrice")
	public HashMap<Integer,HashMap<Integer,Integer>> itemPrice() {
		return priceList;
	}
	
	@PostMapping("/agentSignIn")
	public ResponseEntity<String> agentSignIn(@RequestBody Map<String,Integer> agent) {
		for(Agent myAgent : agentList) {
			if(myAgent.getAgentId().equals(agent.get("agentId"))) {
				if(myAgent.getStatus() == "signed-out") {
					if(pendingOrders.isEmpty()) { //order pending
						myAgent.setStatus("available");
						availableAgents.add(myAgent.getAgentId());
					}
					else  { 
						Integer orderId = pendingOrders.poll();
						for(Order myOrder : orderList) {
							if(myOrder.getOrderId() == orderId) {
								myOrder.setStatus("assigned");
								myOrder.setAgentId(agent.get("agentId"));
							}
						}
						myAgent.setStatus("unavailable");
					}
				}
				break;
			}
		}
		return ResponseEntity.status(HttpStatus.CREATED).body("CREATED");
	}
	
	@PostMapping("/agentSignOut")
	public HttpStatus agentSignOut(@RequestBody Map<String,Integer> agent) {
		for(Agent myAgent : agentList) {
			if(myAgent.getAgentId().equals(agent.get("agentId"))) {
				if(myAgent.getStatus().equals("available")) {
					myAgent.setStatus("signed-out");
				}
			}
		}
		return HttpStatus.CREATED;
	}
	
	@PostMapping("/requestOrder") 
	public ResponseEntity<Map<String,Integer>> requestOrder(@RequestBody Map<String,Integer> orderBody){
		
		Integer custId = orderBody.get("custId");
		Integer restId = orderBody.get("restId");
		Integer itemId = orderBody.get("itemId");
		Integer qty = orderBody.get("qty");
		
		Integer price = priceList.get(restId).get(itemId);
		Integer billAmount = price*qty;
		
		Map<String, Integer> walletMap = new HashMap<>();
		walletMap.put("custId", custId);
		walletMap.put("amount", billAmount);
		
		String url1 = "http://localhost:8082/deductBalance";
		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		HttpEntity<Map<String, Integer>> entity1 = new HttpEntity<>(walletMap,headers);
		
		try {
			restTemplate.postForEntity(url1, entity1, String.class);
		}
		catch (HttpClientErrorException e) {
			return ResponseEntity.status(HttpStatus.GONE).body(null);
		}
		
		String url2 = "http://localhost:8080/acceptOrder";
		Map<String, Integer> RestaurantMap = new HashMap<>();
		RestaurantMap.put("restId", restId);
		RestaurantMap.put("itemId", itemId);
		RestaurantMap.put("qty", qty);
		HttpEntity<Map<String, Integer>> entity2 = new HttpEntity<>(RestaurantMap,headers);

		try {
			restTemplate.postForEntity(url2, entity2, String.class);
		}
		catch (HttpClientErrorException e) {
			String url3 = "http://localhost:8082/addBalance"; 
			restTemplate.postForEntity(url3, entity1, String.class);
			return ResponseEntity.status(HttpStatus.GONE).body(null);
		}

		Order order = new Order(orderNum, "unassigned", -1);
		orderNum = orderNum + 1;
		if(availableAgents.isEmpty()) {
			pendingOrders.add(order.getOrderId());
		}
		else {
			Integer agentId = availableAgents.poll();
			order.setStatus("assigned");
			order.setAgentId(agentId);
			
			for(Agent agent: agentList) {
				if(agent.getAgentId().equals(agentId)) {
					agent.setStatus("unavailable");
				}
			}
		}
		orderList.add(order);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonMap("orderId",order.getOrderId()));
	}
	
	@PostMapping("/orderDelivered")
	public ResponseEntity<String> orderDelivered(@RequestBody Map<String, Integer> order) {
		
		for(Order myOrder: orderList) {
			if(myOrder.getOrderId().equals(order.get("orderId"))) {
				if(myOrder.getStatus() == "assigned") {
					myOrder.setStatus("delivered");
					Integer agentId = myOrder.getAgentId();
					if(!pendingOrders.isEmpty()) {
						Integer orderId = pendingOrders.poll();
						for(Order myOrder1: orderList) {
							if(myOrder1.getOrderId().equals(orderId)) {
								myOrder1.setStatus("assigned");
								myOrder1.setAgentId(agentId);
							}
						}
					}
					else {
						for(Agent myAgent: agentList) {
							if(myAgent.getAgentId().equals(agentId)) {
								myAgent.setStatus("available");
								availableAgents.add(myAgent.getAgentId());
							}
						}
					}
					
				}
			}
		}
		
		return ResponseEntity.status(HttpStatus.CREATED).body("CREATED");
	}
	
	@PostMapping("/reInitialize")
	public ResponseEntity<String> reInitialize() {
		
		
		orderNum = 1000;
		orderList.removeAll(orderList);
		pendingOrders.clear();
		availableAgents.clear();
		agentList.removeAll(agentList);
		
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

		return ResponseEntity.status(HttpStatus.CREATED).body("CREATED");	
	}
	
	@EventListener(ApplicationReadyEvent.class)
	public HttpStatus StartUp() {
			
			File myFile = new File("/Users/fluffy/Downloads/delivery/initialData.txt");
			Scanner s = null;
			try {
				s = new Scanner(myFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			while(s.hasNextLine()) {
				Scanner s2 = new Scanner(s.nextLine());
				String str1 = s2.next();
				if(str1.charAt(0) == '*') {
					break;
				}
				String str2 = s2.next();
				Integer restId = Integer.parseInt(str1);
				Integer numItem = Integer.parseInt(str2);
				HashMap<Integer,Integer> innerMap = new HashMap<Integer,Integer>();
				for(int i=0; i<numItem; i++) {
					Scanner s3 = new Scanner(s.nextLine());
					Integer itemId = Integer.parseInt(s3.next());
					Integer price = Integer.parseInt(s3.next()); 
					s3.next(); //skipping over quantity
					innerMap.put(itemId,price);
					System.out.println("Done");
				}
				priceList.put(restId, innerMap);
			}
			
			
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
	
	@GetMapping("/order/{orderId}")
	public ResponseEntity<Order> getOrderDetails(@PathVariable String orderId) {
		Order order = null;
		for(Order myOrder: orderList) {
			if(myOrder.getOrderId().equals(Integer.parseInt(orderId))) {
				order = myOrder;
			}
		}
		
		if(order == null)
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		return ResponseEntity.status(HttpStatus.OK).body(order);
	}
	
	@GetMapping("/agent/{agentId}")
	public ResponseEntity<Agent> getAgentDetails(@PathVariable String agentId) {
		Agent agent = null;
		for(Agent myAgent: agentList) {
			if(myAgent.getAgentId().equals(Integer.parseInt(agentId))) {
				agent = myAgent;
			}
		}
		return ResponseEntity.status(HttpStatus.OK).body(agent);
	}
}
