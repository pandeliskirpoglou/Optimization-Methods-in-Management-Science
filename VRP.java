package project_mebede;

import java.util.ArrayList;
import java.util.Random;


public class VRP {

	
	ArrayList<Node> customers;
	
	
	public void createAllNodesAndCustomerLists( int numberOfCustomers)  {
		//Create the list with the customers
		customers = new ArrayList();
		int birthday = 18031999;
		Random ran  = new Random(birthday);
		for (int i = 0; i < 100; i++) {
			Node cust = new Node();
			cust.x = ran.nextInt(100);
			cust.y = ran.nextInt(100);
			cust.demand = 100*(1+ran.nextInt(5));
			cust.serviceTime = 0.25;
			customers.add(cust);
		}
		
		
	}
	
	
	
	
	
	
}
