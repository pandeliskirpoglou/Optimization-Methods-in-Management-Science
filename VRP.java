package project_mebede;

import java.util.ArrayList;
import java.util.Random;






public class VRP {

	
	ArrayList<Node> customers;
	ArrayList<Node> allNodes;
	Node depot;
	double[][] distanceMatrix;
	double[][] timeMatrix;
	
    int numberOfCustomers;
    int capacity1;
    int capacity2;
	
    Random ran;
	
	public VRP(int totalCustomers, int cap1, int cap2) {
        numberOfCustomers = totalCustomers;
        capacity1 = cap1;
        capacity2 = cap2;
        ran = new Random(1);
    }

    void generateNetworkRandomly() {
        createAllNodesAndCustomerLists(numberOfCustomers);
       // CalculateDistanceMatrix();
    }
	
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
		
		allNodes = new ArrayList();

		depot = new Node();
		depot.x = 50;
		depot.y = 50;
		depot.demand = 0;
		allNodes.add(depot);
		for (int i = 0; i < customers.size(); i++) {
			Node cust = customers.get(i);
			allNodes.add(cust);
		}

		for (int i = 0; i < allNodes.size(); i++) {
			Node nd = allNodes.get(i);
			nd.ID = i;
		}
		
		
	}
	
	
	public void calculateDistanceMatrix() {

		distanceMatrix = new double[allNodes.size()][allNodes.size()];
		for (int i = 0; i < allNodes.size(); i++) {
			Node from = allNodes.get(i);

			for (int j = 0; j < allNodes.size(); j++) {
				Node to = allNodes.get(j);

				double Delta_x = (from.x - to.x);
				double Delta_y = (from.y - to.y);
				double distance = Math.sqrt((Delta_x * Delta_x) + (Delta_y * Delta_y));

				distance = Math.round(distance);

				distanceMatrix[i][j] = distance;
			}
		}
		
		for (int i = 0; i < allNodes.size(); i++) {

			for (int j = 0; j < allNodes.size(); j++) {
				Node node = allNodes.get(j);
				timeMatrix[i][j] = distanceMatrix[i][j]/35 + 0.25; 
				

				
			}
			
		}
		
	}
	
	
	
	
	
}
