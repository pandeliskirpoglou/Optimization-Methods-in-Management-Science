package MEBEDE;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author mzaxa
 */
public class VRP {

    double[][] distanceMatrix;
    ArrayList<Node> allNodes;
    ArrayList<Node> customers;
    Random ran;
    Node depot;
    int numberOfCustomers;
    int capacity1;
    int capacity2;
    Solution bestSolutionThroughTabuSearch;

    public VRP(int totalCustomers, int cap1, int cap2) {
        numberOfCustomers = totalCustomers;
        capacity1 = cap1;
        capacity2 = cap2;
        ran = new Random(1);
    }

    void GenerateNetworkRandomly() {
        CreateAllNodesAndCustomerLists(numberOfCustomers);
        CalculateDistanceMatrix();
    }

    public void CreateAllNodesAndCustomerLists(int numberOfCustomers) {
        //Create the list with the customers
        customers = new ArrayList();
        int birthday = 18031999; 
        Random ran = new Random(birthday);
        for (int i = 0; i < numberOfCustomers; i++) {
            Node cust = new Node();

            cust.x = ran.nextInt(100);
            cust.y = ran.nextInt(100);
            cust.demand = 100*(1 + ran.nextInt(5));
            cust.serviceTime = 0.25;

            customers.add(cust);
        }

        //Build the allNodes array and the corresponding distance matrix
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

    public void CalculateDistanceMatrix() {

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
    }

    void Solve() {

        Solution s = new Solution();

        ApplyNearestNeighborMethod(s);
        printSolution(s);
        System.out.println("The cost is:" + s.cost );
       // TabuSearch(s);
    }

    private void SetRoutedFlagToFalseForAllCustomers() {
        for (int i = 0; i < customers.size(); i++) {
            customers.get(i).isRouted = false;
        }
    }

    private void ApplyNearestNeighborMethod(Solution solution) {

        boolean modelIsFeasible = true;
        ArrayList<Route> routeList1 = solution.routes1500;
        ArrayList<Route> routeList2 = solution.routes1200;

        SetRoutedFlagToFalseForAllCustomers();

        //Q - How many insertions? A - Equal to the number of customers! Thus for i = 0 -> customers.size() 
        for (int insertions = 0; insertions < customers.size();) /* the insertions will be updated in the for loop */ {
            if (routeList1.size() < 15) {
            	int num = 1;
            	//A. Insertion Identification
            	CustomerInsertion bestInsertion = new CustomerInsertion();
                bestInsertion.cost = Double.MAX_VALUE;
                Route lastRoute = GetLastRoute(routeList1);
                if (lastRoute != null) {
                    IdentifyBestInsertion_NN(bestInsertion, lastRoute);
                }
                //B. Insertion Application
                //Feasible insertion was identified
                if (bestInsertion.cost < Double.MAX_VALUE) {
                    ApplyCustomerInsertion(bestInsertion, solution);
                    insertions++;
                } //C. If no insertion was feasible
                else {
                    //C1. There is a customer with demand larger than capacity -> Infeasibility
                    if (lastRoute != null && lastRoute.nodes.size() == 1) {
                        modelIsFeasible = false;
                        break;
                    } else {
                        CreateAndPushAnEmptyRouteInTheSolution(solution, capacity1, num);
              
                    }
                }
            } else if (routeList2.size() < 15) { 
            	int num = 2;
            	//A. Insertion Identification
            	CustomerInsertion bestInsertion = new CustomerInsertion();
                bestInsertion.cost = Double.MAX_VALUE;
                Route lastRoute = GetLastRoute(routeList2);
                if (lastRoute != null) {
                    IdentifyBestInsertion_NN(bestInsertion, lastRoute);
                }
                //B. Insertion Application
                //Feasible insertion was identified
                if (bestInsertion.cost < Double.MAX_VALUE) {
                    ApplyCustomerInsertion(bestInsertion, solution);
                    insertions++;
                } //C. If no insertion was feasible
                else {
                    //C1. There is a customer with demand larger than capacity -> Infeasibility
                    if (lastRoute != null && lastRoute.nodes.size() == 2) {
                        modelIsFeasible = false;
                        break;
                    } else {
                        CreateAndPushAnEmptyRouteInTheSolution(solution, capacity2, num);
               
                    }
                }
            }
        }

        if (modelIsFeasible == false) {
            //TODO
        }
    }
    
    private void printSolution(Solution solution) {
    	ArrayList<Route> routes = solution.allRoutes;
    	int i = 0;
    	for (Route route:routes) {
    		i++;
    		ArrayList<Node> nodes = route.nodes;
    		System.out.println("In the route " + i + " the sequence of nodes is:");
    		for (Node node:nodes) {
    			System.out.println(node.ID + ",");
    		}
    	}
    }

    private Route GetLastRoute(ArrayList<Route> routeList) {
        if (routeList.isEmpty()) {
            return null;
        } else {
            return routeList.get(routeList.size() - 1);
        }
    }

    private void CreateAndPushAnEmptyRouteInTheSolution(Solution currentSolution, int capacity, int routenumb ) {
        Route rt = new Route(capacity);
        rt.nodes.add(depot);
        if (routenumb == 1) {
        	currentSolution.routes1500.add(rt);
        } else {
        	currentSolution.routes1200.add(rt);
        }
        currentSolution.allRoutes.add(rt);
    }

    private void ApplyCustomerInsertion(CustomerInsertion insertion, Solution solution) {
        Node insertedCustomer = insertion.customer;
        Route route = insertion.insertionRoute;

        route.nodes.add(insertedCustomer); //add(route.nodes.size() - 1, insertedCustomer);

        Node beforeInserted = route.nodes.get(route.nodes.size() - 2);

        double costAdded = distanceMatrix[beforeInserted.ID][insertedCustomer.ID];

        route.cost = route.cost + costAdded;
        route.load = route.load + insertedCustomer.demand;
        route.duration = costAdded / 35 + insertedCustomer.serviceTime;
        solution.cost = solution.cost + costAdded;

        insertedCustomer.isRouted = true;
    }

    private void IdentifyBestInsertion_NN(CustomerInsertion bestInsertion, Route lastRoute) {
        for (int j = 0; j < customers.size(); j++) {
            // The examined node is called candidate
            Node candidate = customers.get(j);
            // if this candidate has not been pushed in the solution
            if (candidate.isRouted == false) {
                if (lastRoute.load + candidate.demand <= lastRoute.capacity) {
                    ArrayList<Node> nodeSequence = lastRoute.nodes;
                    Node lastCustomerInTheRoute = nodeSequence.get(nodeSequence.size() - 1);

                    double trialCost = distanceMatrix[lastCustomerInTheRoute.ID][candidate.ID];
                    double trialDuration = trialCost/35 + candidate.serviceTime;
                   
                    if (lastRoute.duration + trialDuration <= 3.5) {  
                    	if (trialCost < bestInsertion.cost) {
                    		bestInsertion.customer = candidate;
                    		bestInsertion.insertionRoute = lastRoute;
                    		bestInsertion.cost = trialCost;
                    	}
                    }
                }
            }
        }
    }
    
    
    
    
   
    public void VND() {
    	
    	Solution solution = new Solution();
    	ArrayList<Route> routeList1500 = solution.routes1500;
        ArrayList<Route> routeList1200 = solution.routes1200;
        
        int i = 0; //insertions
        while (i < customers.size()) {
        	if (routeList1500.size() < 15) {
        		Route routeForSolution = findBestInsertionFor1500();
        		
        	} else {
        		findBestInsertionFor1200();
        		
        	}
        	
        	
        	i++;
        }
    }
    
    
    public Route findBestInsertionFor1500() {
    	double arraysum, absolute, testTime;
    	double bestArraysum, bestAbsolute, cost;
    	int j;
    	Route route = new Route(1500);
    	bestAbsolute = Double.MAX_VALUE;
    	ArrayList<Node> bestNodeSequence = new ArrayList<Node>();
    	
    	for (int i = 1; i < allNodes.size(); i++) {
    		ArrayList<Node> nodeSequence = new ArrayList<Node>();
    		nodeSequence.add(allNodes.get(0));
    		j = 1;
    		
    		Node test = allNodes.get(i);
    		
    		cost = 0;
    		arraysum = 0;
    		testTime = 0;
    		if (!test.isRouted) {
    			
    			while (j!=i && j < allNodes.size() && arraysum <= 1500 && testTime <= 3.5) {
    				
	    			arraysum += distanceMatrix[i][j];
	    			testTime += distanceMatrix[i][j]/35 + 0.25;
	    				
	    			if (arraysum <= 1500 && testTime <= 3.5) {
	    				nodeSequence.add(allNodes.get(j));
	    				cost = arraysum; 
	    			}
    				j++;
    			}
    			absolute = 1500 - cost;
    			if (absolute < bestAbsolute) {
    				bestAbsolute = absolute;
    				bestNodeSequence = nodeSequence; //node sequence
    				bestArraysum = cost; // cost of sequence
    			}
    		}
    		
    	}
    	
    	return route;
    
    
    }
    
    public void findBestInsertionFor1200() {
    	double arraysum, absolute, testTime;
    	double bestArraysum, bestAbsolute, cost;
    	int j;
 
    	bestAbsolute = Double.MAX_VALUE;
    	ArrayList<Node> bestNodeSequence = new ArrayList<Node>();
    	
    	for (int i = 1; i < allNodes.size(); i++) {
    		ArrayList<Node> nodeSequence = new ArrayList<Node>();
    		nodeSequence.add(allNodes.get(0));
    		j = 1;
    		
    		Node test = allNodes.get(i);
    		
    		cost = 0;
    		arraysum = 0;
    		testTime = 0;
    		if (!test.isRouted) {
    			
    			while (j!=i && j < allNodes.size() && arraysum <= 1200 && testTime <= 3.5) {
    				
	    			arraysum += distanceMatrix[i][j];
	    			testTime += distanceMatrix[i][j]/35 + 0.25;
	    				
	    			if (arraysum <= 1200 && testTime <= 3.5) {
	    				nodeSequence.add(allNodes.get(j));
	    				cost = arraysum; 
	    			}
    				j++;
    			}
    			absolute = 1200 - cost;
    			if (absolute < bestAbsolute) {
    				bestAbsolute = absolute;
    				bestNodeSequence = nodeSequence; //node sequence
    				bestArraysum = cost; // cost of sequence
    			}
    		}
    		
    	}
        
        
    }
/*
 * 
 	 

    private void TabuSearch(Solution sol) {
        bestSolutionThroughTabuSearch = cloneSolution(sol);

        RelocationMove rm = new RelocationMove();
        SwapMove sm = new SwapMove();
        TwoOptMove top = new TwoOptMove();
        
        for (int i = 0; i < 1000; i++) {
            InitializeOperators(rm, sm, top);

            int operatorType = 2;//DecideOperator();

            //Identify Best Move
            if (operatorType == 0) {
                FindBestRelocationMove(rm, sol);
            } else if (operatorType == 1) {
                FindBestSwapMove(sm, sol);
            } else if (operatorType == 2) {
                FindBestTwoOptMove(top, sol);
            }

            if (LocalOptimumHasBeenReached(operatorType, rm, sm, top)) {
                break;
            }

            //Apply move
            ApplyMove(operatorType, rm, sm, top, sol);
            System.out.println(i + " " + sol.cost);

            TestSolution(sol);

            SolutionDrawer.drawRoutes(allNodes, sol, Integer.toString(i));
        }

    }

    private Solution cloneSolution(Solution sol) {
        Solution cloned = new Solution();

        //No need to clone - basic type
        cloned.cost = sol.cost;

        //Need to clone: Arraylists are objects
        for (int i = 0; i < sol.routes.size(); i++) {
            Route rt = sol.routes.get(i);
            Route clonedRoute = cloneRoute(rt);
            cloned.routes.add(clonedRoute);
        }

        return cloned;
    }

    private Route cloneRoute(Route rt) {
        Route cloned = new Route(rt.capacity);
        cloned.cost = rt.cost;
        cloned.load = rt.load;
        cloned.nodes = new ArrayList();
        for (int i = 0; i < rt.nodes.size(); i++) {
            Node n = rt.nodes.get(i);
            cloned.nodes.add(n);
            //cloned.nodes.add(rt.nodes.get(i));
        }
        //cloned.nodes = rt.nodes.clone();
        return cloned;
    }

    private int DecideOperator() {
        return ran.nextInt(2);
        //return 0;
        //return 1;
    }

    private void FindBestRelocationMove(RelocationMove rm, Solution sol) {
        ArrayList<Route> routes = sol.routes;
        for (int originRouteIndex = 0; originRouteIndex < routes.size(); originRouteIndex++) {
            Route rt1 = routes.get(originRouteIndex);
            for (int targetRouteIndex = 0; targetRouteIndex < routes.size(); targetRouteIndex++) {
                Route rt2 = routes.get(targetRouteIndex);

                for (int originNodeIndex = 1; originNodeIndex < rt1.nodes.size() - 1; originNodeIndex++) {
                    for (int targetNodeIndex = 0; targetNodeIndex < rt2.nodes.size() - 1; targetNodeIndex++) {
                        //Why? No change for the route involved
                        if (originRouteIndex == targetRouteIndex && (targetNodeIndex == originNodeIndex || targetNodeIndex == originNodeIndex - 1)) {
                            continue;
                        }

                        Node a = rt1.nodes.get(originNodeIndex - 1);
                        Node b = rt1.nodes.get(originNodeIndex);
                        Node c = rt1.nodes.get(originNodeIndex + 1);

                        Node insPoint1 = rt2.nodes.get(targetNodeIndex);
                        Node insPoint2 = rt2.nodes.get(targetNodeIndex + 1);

                        //capacity constraints
                        if (originRouteIndex != targetRouteIndex) {
                            if (rt2.load + b.demand > rt2.capacity) {
                                continue;
                            }
                        }

                        double costAdded = distanceMatrix[a.ID][c.ID] + distanceMatrix[insPoint1.ID][b.ID] + distanceMatrix[b.ID][insPoint2.ID];
                        double costRemoved = distanceMatrix[a.ID][b.ID] + distanceMatrix[b.ID][c.ID] + distanceMatrix[insPoint1.ID][insPoint2.ID];
                        double moveCost = costAdded - costRemoved;

                        double costChangeOriginRoute = distanceMatrix[a.ID][c.ID] - (distanceMatrix[a.ID][b.ID] + distanceMatrix[b.ID][c.ID]);
                        double costChangeTargetRoute = distanceMatrix[insPoint1.ID][b.ID] + distanceMatrix[b.ID][insPoint2.ID] - distanceMatrix[insPoint1.ID][insPoint2.ID];
                        double totalObjectiveChange = costChangeOriginRoute + costChangeTargetRoute;

                        //Testing
                        if (Math.abs(moveCost - totalObjectiveChange) > 0.0001) {
                            int mn = 0;
                        }

                        //BuilArcList(arcsDaysCreated, a.uid, c.uid, p, insPoint1.uid, b.uid, p, b.uid, insPoint2.uid);
                        //BuilArcList(arcsDaysDeleted, a.uid, b.uid, p, b.uid, c.uid, p, insPoint1.uid, insPoint2.uid);
                        if (MoveIsTabu()) //Some Tabu Policy
                        {
                            continue;
                        }

                        StoreBestRelocationMove(originRouteIndex, targetRouteIndex, originNodeIndex, targetNodeIndex, moveCost, rm);
                    }
                }
            }
        }
    }

    private void StoreBestRelocationMove(int originRouteIndex, int targetRouteIndex, int originNodeIndex, int targetNodeIndex,
            double moveCost, RelocationMove rm) {

        if (moveCost < rm.moveCost) {
            rm.originNodePosition = originNodeIndex;
            rm.targetNodePosition = targetNodeIndex;
            rm.targetRoutePosition = targetRouteIndex;
            rm.originRoutePosition = originRouteIndex;

            rm.moveCost = moveCost;
        }
    }

    private void FindBestSwapMove(SwapMove sm, Solution sol) {
        ArrayList<Route> routes = sol.routes;
        for (int firstRouteIndex = 0; firstRouteIndex < routes.size(); firstRouteIndex++) {
            Route rt1 = routes.get(firstRouteIndex);
            for (int secondRouteIndex = firstRouteIndex; secondRouteIndex < routes.size(); secondRouteIndex++) {
                Route rt2 = routes.get(secondRouteIndex);
                for (int firstNodeIndex = 1; firstNodeIndex < rt1.nodes.size() - 1; firstNodeIndex++) {
                    int startOfSecondNodeIndex = 1;
                    if (rt1 == rt2) {
                        startOfSecondNodeIndex = firstNodeIndex + 1;
                    }
                    for (int secondNodeIndex = startOfSecondNodeIndex; secondNodeIndex < rt2.nodes.size() - 1; secondNodeIndex++) {
                        Node a1 = rt1.nodes.get(firstNodeIndex - 1);
                        Node b1 = rt1.nodes.get(firstNodeIndex);
                        Node c1 = rt1.nodes.get(firstNodeIndex + 1);

                        Node a2 = rt2.nodes.get(secondNodeIndex - 1);
                        Node b2 = rt2.nodes.get(secondNodeIndex);
                        Node c2 = rt2.nodes.get(secondNodeIndex + 1);

                        double moveCost = Double.MAX_VALUE;

                        if (rt1 == rt2) // within route 
                        {
                            if (firstNodeIndex == secondNodeIndex - 1) {
                                double costRemoved = distanceMatrix[a1.ID][b1.ID] + distanceMatrix[b1.ID][b2.ID] + distanceMatrix[b2.ID][c2.ID];
                                double costAdded = distanceMatrix[a1.ID][b2.ID] + distanceMatrix[b2.ID][b1.ID] + distanceMatrix[b1.ID][c2.ID];
                                moveCost = costAdded - costRemoved;
//                                      BuilArcList(arcsDaysCreated, a1.uid, b2.uid, p, b2.uid, b1.uid, b1.uid, c2.uid);
//                                      BuilArcList(arcsDaysDeleted, a1.uid, b1.uid, p, b1.uid, b2.uid, b2.uid, c2.uid);

                                if (MoveIsTabu()) //Some Tabu Policy
                                {
                                    continue;
                                }
                            } else {
                                double costRemoved1 = distanceMatrix[a1.ID][b1.ID] + distanceMatrix[b1.ID][c1.ID];
                                double costAdded1 = distanceMatrix[a1.ID][b2.ID] + distanceMatrix[b2.ID][c1.ID];

                                double costRemoved2 = distanceMatrix[a2.ID][b2.ID] + distanceMatrix[b2.ID][c2.ID];
                                double costAdded2 = distanceMatrix[a2.ID][b1.ID] + distanceMatrix[b1.ID][c2.ID];

                                moveCost = costAdded1 + costAdded2 - (costRemoved1 + costRemoved2);

                                if (MoveIsTabu()) //Some Tabu Policy
                                {
                                    continue;
                                }
                            }
                        } else // between routes
                        {
                            //capacity constraints
                            if (rt1.load - b1.demand + b2.demand > capacity) {
                                continue;
                            }
                            if (rt2.load - b2.demand + b1.demand > capacity) {
                                continue;
                            }

                            double costRemoved1 = distanceMatrix[a1.ID][b1.ID] + distanceMatrix[b1.ID][c1.ID];
                            double costAdded1 = distanceMatrix[a1.ID][b2.ID] + distanceMatrix[b2.ID][c1.ID];

                            double costRemoved2 = distanceMatrix[a2.ID][b2.ID] + distanceMatrix[b2.ID][c2.ID];
                            double costAdded2 = distanceMatrix[a2.ID][b1.ID] + distanceMatrix[b1.ID][c2.ID];

                            moveCost = costAdded1 + costAdded2 - (costRemoved1 + costRemoved2);
//                          BuilArcList(arcsDaysCreated, a1.uid, b2.uid, p, b2.uid, c1.uid, p, a2.uid, b1.uid, p, b1.uid, c2.uid);
//                          BuilArcList(arcsDaysDeleted, a1.uid, b1.uid, p, b1.uid, c1.uid, p, a2.uid, b2.uid, p, b2.uid, c2.uid);

                            if (MoveIsTabu()) //Some Tabu Policy
                            {
                                continue;
                            }
                        }
                        StoreBestSwapMove(firstRouteIndex, secondRouteIndex, firstNodeIndex, secondNodeIndex, moveCost, sm);
                    }
                }
            }
        }
    }

    private void StoreBestSwapMove(int firstRouteIndex, int secondRouteIndex, int firstNodeIndex, int secondNodeIndex, double moveCost, SwapMove sm) {
        if (moveCost < sm.moveCost) {
            sm.firstRoutePosition = firstRouteIndex;
            sm.firstNodePosition = firstNodeIndex;
            sm.secondRoutePosition = secondRouteIndex;
            sm.secondNodePosition = secondNodeIndex;
            sm.moveCost = moveCost;
        }
    }

    private void ApplyMove(int operatorType, RelocationMove rm, SwapMove sm, TwoOptMove top, Solution sol) {
        if (operatorType == 0) {
            ApplyRelocationMove(rm, sol);
        } else if (operatorType == 1) {
            ApplySwapMove(sm, sol);
        }
        else if (operatorType == 2)
        {
            ApplyTwoOptMove(top, sol);
        }
    }

    private void ApplyRelocationMove(RelocationMove rm, Solution sol) {
        if (rm.moveCost == Double.MAX_VALUE) {
            return;
        }

        Route originRoute = sol.routes.get(rm.originRoutePosition);
        Route targetRoute = sol.routes.get(rm.targetRoutePosition);

        Node B = originRoute.nodes.get(rm.originNodePosition);

        if (originRoute == targetRoute) {
            originRoute.nodes.remove(rm.originNodePosition);
            if (rm.originNodePosition < rm.targetNodePosition) {
                targetRoute.nodes.add(rm.targetNodePosition, B);
            } else {
                targetRoute.nodes.add(rm.targetNodePosition + 1, B);
            }

            originRoute.cost = originRoute.cost + rm.moveCost;
        } else {
            Node A = originRoute.nodes.get(rm.originNodePosition - 1);
            Node C = originRoute.nodes.get(rm.originNodePosition + 1);

            Node F = targetRoute.nodes.get(rm.targetNodePosition);
            Node G = targetRoute.nodes.get(rm.targetNodePosition + 1);

            double costChangeOrigin = distanceMatrix[A.ID][C.ID] - distanceMatrix[A.ID][B.ID] - distanceMatrix[B.ID][C.ID];
            double costChangeTarget = distanceMatrix[F.ID][B.ID] + distanceMatrix[B.ID][G.ID] - distanceMatrix[F.ID][G.ID];

            originRoute.load = originRoute.load - B.demand;
            targetRoute.load = targetRoute.load + B.demand;

            originRoute.cost = originRoute.cost + costChangeOrigin;
            targetRoute.cost = targetRoute.cost + costChangeTarget;

            originRoute.nodes.remove(rm.originNodePosition);
            targetRoute.nodes.add(rm.targetNodePosition + 1, B);

            double newMoveCost = costChangeOrigin + costChangeTarget;
            if (Math.abs(newMoveCost - rm.moveCost) > 0.0001) {
                int problem = 0;
            }
        }
        sol.cost = sol.cost + rm.moveCost;
    }

    private void ApplySwapMove(SwapMove sm, Solution sol) {
        if (sm.moveCost == Double.MAX_VALUE) {
            return;
        }

        Route firstRoute = sol.routes.get(sm.firstRoutePosition);
        Route secondRoute = sol.routes.get(sm.secondRoutePosition);

        if (firstRoute == secondRoute) {
            if (sm.firstNodePosition == sm.secondNodePosition - 1) {
                Node A = firstRoute.nodes.get(sm.firstNodePosition);
                Node B = firstRoute.nodes.get(sm.firstNodePosition + 1);

                firstRoute.nodes.set(sm.firstNodePosition, B);
                firstRoute.nodes.set(sm.firstNodePosition + 1, A);

            } else {
                Node A = firstRoute.nodes.get(sm.firstNodePosition);
                Node B = firstRoute.nodes.get(sm.secondNodePosition);

                firstRoute.nodes.set(sm.firstNodePosition, B);
                firstRoute.nodes.set(sm.secondNodePosition, A);
            }
            firstRoute.cost = firstRoute.cost + sm.moveCost;
        } else {
            Node A = firstRoute.nodes.get(sm.firstNodePosition - 1);
            Node B = firstRoute.nodes.get(sm.firstNodePosition);
            Node C = firstRoute.nodes.get(sm.firstNodePosition + 1);

            Node E = secondRoute.nodes.get(sm.secondNodePosition - 1);
            Node F = secondRoute.nodes.get(sm.secondNodePosition);
            Node G = secondRoute.nodes.get(sm.secondNodePosition + 1);

            double costChangeFirstRoute = distanceMatrix[A.ID][F.ID] + distanceMatrix[F.ID][C.ID] - distanceMatrix[A.ID][B.ID] - distanceMatrix[B.ID][C.ID];
            double costChangeSecondRoute = distanceMatrix[E.ID][B.ID] + distanceMatrix[B.ID][G.ID] - distanceMatrix[E.ID][F.ID] - distanceMatrix[F.ID][G.ID];

            firstRoute.cost = firstRoute.cost + costChangeFirstRoute;
            secondRoute.cost = secondRoute.cost + costChangeSecondRoute;

            firstRoute.load = firstRoute.load + F.demand - B.demand;
            secondRoute.load = secondRoute.load + B.demand - F.demand;

            firstRoute.nodes.set(sm.firstNodePosition, F);
            secondRoute.nodes.set(sm.secondNodePosition, B);

        }

        sol.cost = sol.cost + sm.moveCost;

    }

    private void TestSolution(Solution solution) {

        double secureSolutionCost = 0;
        for (int i = 0; i < solution.routes.size(); i++) {
            Route rt = solution.routes.get(i);

            double secureRouteCost = 0;
            double secureRouteLoad = 0;

            for (int j = 0; j < rt.nodes.size() - 1; j++) {
                Node A = rt.nodes.get(j);
                Node B = rt.nodes.get(j + 1);

                secureRouteCost = secureRouteCost + distanceMatrix[A.ID][B.ID];
                 secureRouteLoad += A.demand;
            }

            if (Math.abs(secureRouteCost - rt.cost) > 0.001) 
            {
                int routeCostProblem = 0;
            }
            
             if (secureRouteLoad != rt.load || secureRouteLoad > rt.capacity)
            {
                System.out.println("route Load Problem");
            }

            secureSolutionCost = secureSolutionCost + secureRouteCost;
        }

        if (Math.abs(secureSolutionCost - solution.cost) > 0.001) 
        {
            int solutionCostProblem = 0;
        }
    }

    private void InitializeOperators(RelocationMove rm, SwapMove sm, TwoOptMove top) {
        rm.moveCost = Double.MAX_VALUE;
        sm.moveCost = Double.MAX_VALUE;
        top.moveCost = Double.MAX_VALUE;
    }

    private boolean MoveIsTabu() {
        return false;
    }

    private boolean LocalOptimumHasBeenReached(int operatorType, RelocationMove rm, SwapMove sm, TwoOptMove top) {
        if (operatorType == 0) {
            if (rm.moveCost > -0.00001) {
                return true;
            }
        } else if (operatorType == 1) {
            if (sm.moveCost > 0.00001) {
                return true;
            }
        }else if (operatorType == 2) {
            if (top.moveCost > 0.00001) {
                return true;
            }
        }

        return false;
    }

    private void FindBestTwoOptMove(TwoOptMove top, Solution sol) {
        for (int rtInd1 = 0; rtInd1 < sol.routes.size(); rtInd1++) {
            Route rt1 = sol.routes.get(rtInd1);

            for (int rtInd2 = rtInd1; rtInd2 < sol.routes.size(); rtInd2++) {
                Route rt2 = sol.routes.get(rtInd2);

                for (int nodeInd1 = 0; nodeInd1 < rt1.nodes.size() - 1; nodeInd1++) {
                    int start2 = 0;
                    if (rt1 == rt2) {
                        start2 = nodeInd1 + 2;
                    }

                    for (int nodeInd2 = start2; nodeInd2 < rt2.nodes.size() - 1; nodeInd2++) 
                    {
                        double moveCost = Double.MAX_VALUE;

                        if (rt1 == rt2) {
                            Node A = rt1.nodes.get(nodeInd1);
                            Node B = rt1.nodes.get(nodeInd1 + 1);
                            Node K = rt2.nodes.get(nodeInd2);
                            Node L = rt2.nodes.get(nodeInd2 + 1);

                            if (nodeInd1 == 0 && nodeInd2 == rt1.nodes.size() - 2) {
                                continue;
                            }

                            double costAdded = distanceMatrix[A.ID][K.ID] + distanceMatrix[B.ID][L.ID];
                            double costRemoved = distanceMatrix[A.ID][B.ID] + distanceMatrix[K.ID][L.ID];

                            moveCost = costAdded - costRemoved;

                        } else {
                            Node A = (rt1.nodes.get(nodeInd1));
                            Node B = (rt1.nodes.get(nodeInd1 + 1));
                            Node K = (rt2.nodes.get(nodeInd2));
                            Node L = (rt2.nodes.get(nodeInd2 + 1));

                            if (nodeInd1 == 0 && nodeInd2 == 0) {
                                continue;
                            }
                            if (nodeInd1 == rt1.nodes.size() - 2 && nodeInd2 == rt2.nodes.size() - 2) {
                                continue;
                            }

                            if (CapacityConstraintsAreViolated(rt1, nodeInd1, rt2, nodeInd2)) {
                                continue;
                            }

                            double costAdded = distanceMatrix[A.ID][L.ID] + distanceMatrix[B.ID][K.ID];
                            double costRemoved = distanceMatrix[A.ID][B.ID] + distanceMatrix[K.ID][L.ID];

                            moveCost = costAdded - costRemoved;
                        }

                        if (moveCost < top.moveCost) 
                        {
                            StoreBestTwoOptMove(rtInd1, rtInd2, nodeInd1, nodeInd2, moveCost, top);
                        }
                    }
                }
            }
        }
    }

    private void StoreBestTwoOptMove(int rtInd1, int rtInd2, int nodeInd1, int nodeInd2, double moveCost, TwoOptMove top) {
        top.positionOfFirstRoute = rtInd1;
        top.positionOfSecondRoute = rtInd2;
        top.positionOfFirstNode = nodeInd1;
        top.positionOfSecondNode = nodeInd2;
        top.moveCost = moveCost;
    }

    private void ApplyTwoOptMove(TwoOptMove top, Solution sol) 
    {
        Route rt1 = sol.routes.get(top.positionOfFirstRoute);
        Route rt2 = sol.routes.get(top.positionOfSecondRoute);

        if (rt1 == rt2) 
        {
            ArrayList modifiedRt = new ArrayList();

            for (int i = 0; i <= top.positionOfFirstNode; i++) 
            {
                modifiedRt.add(rt1.nodes.get(i));
            }
            for (int i = top.positionOfSecondNode; i > top.positionOfFirstNode; i--) 
            {
                modifiedRt.add(rt1.nodes.get(i));
            }
            for (int i = top.positionOfSecondNode + 1; i < rt1.nodes.size(); i++) 
            {
                modifiedRt.add(rt1.nodes.get(i));
            }

            rt1.nodes = modifiedRt;
            
            rt1.cost += top.moveCost;
            sol.cost += top.moveCost;
        }
        else
        {
            ArrayList modifiedRt1 = new ArrayList();
            ArrayList modifiedRt2 = new ArrayList();
            
            Node A = (rt1.nodes.get(top.positionOfFirstNode));
            Node B = (rt1.nodes.get(top.positionOfFirstNode + 1));
            Node K = (rt2.nodes.get(top.positionOfSecondNode));
            Node L = (rt2.nodes.get(top.positionOfSecondNode + 1));
            
           
            for (int i = 0 ; i <= top.positionOfFirstNode; i++)
            {
                modifiedRt1.add(rt1.nodes.get(i));
            }
             for (int i = top.positionOfSecondNode + 1 ; i < rt2.nodes.size(); i++)
            {
                modifiedRt1.add(rt2.nodes.get(i));
            }
             
            for (int i = 0 ; i <= top.positionOfSecondNode; i++)
            {
                modifiedRt2.add(rt2.nodes.get(i));
            }
            for (int i = top.positionOfFirstNode + 1 ; i < rt1.nodes.size(); i++)
            {
                modifiedRt2.add(rt1.nodes.get(i));
            }
            
            double rt1SegmentLoad = 0;
            for (int i = 0 ; i <= top.positionOfFirstNode; i++)
            {
                rt1SegmentLoad += rt1.nodes.get(i).demand;
            }
            
            double rt2SegmentLoad = 0;
            for (int i = 0 ; i <= top.positionOfSecondNode; i++)
            {
                rt2SegmentLoad += rt2.nodes.get(i).demand;
            }
            
            double originalRt1Load = rt1.load;
            rt1.load = rt1SegmentLoad + (rt2.load - rt2SegmentLoad);
            rt2.load = rt2SegmentLoad + (originalRt1Load - rt1SegmentLoad);
            
            rt1.nodes = modifiedRt1;
            rt2.nodes = modifiedRt2;
            
            rt1.cost = UpdateRouteCost(rt1);
            rt2.cost = UpdateRouteCost(rt2);
            
            sol.cost += top.moveCost;
        }

    }

    private boolean CapacityConstraintsAreViolated(Route rt1, int nodeInd1, Route rt2, int nodeInd2) 
    {
        double rt1FirstSegmentLoad = 0;
        for (int i = 0 ; i <= nodeInd1; i++)
        {
            rt1FirstSegmentLoad += rt1.nodes.get(i).demand;
        }
        double rt1SecondSegment = rt1.load - rt1FirstSegmentLoad;
        
        double rt2FirstSegmentLoad = 0;
        for (int i = 0 ; i <= nodeInd2; i++)
        {
            rt2FirstSegmentLoad += rt2.nodes.get(i).demand;
        }
        double rt2SecondSegment = rt2.load - rt2FirstSegmentLoad;
        
        if (rt1FirstSegmentLoad +  rt2SecondSegment > rt1.capacity)
        {
            return true;
        }
        
        if (rt2FirstSegmentLoad +  rt1SecondSegment > rt2.capacity)
        {
            return true;
        }
        
        return false;
    }

    private double CalculateCostSol(Solution sol) 
    {
        double totalCost = 0;

        for (int i = 0; i < sol.routes.size(); i++) 
        {
            Route rt = sol.routes.get(i);

            for (int j = 0; j < rt.nodes.size() - 1; j++) {
                Node A = rt.nodes.get(j);
                Node B = rt.nodes.get(j + 1);

                totalCost += distanceMatrix[A.ID][B.ID];
            }
        }

        return totalCost;

    }

    private double UpdateRouteCost(Route rt) 
    {
        double totCost = 0 ;
        for (int i = 0 ; i < rt.nodes.size()-1; i++)
        {
            Node A = rt.nodes.get(i);
            Node B = rt.nodes.get(i+1);
            totCost += distanceMatrix[A.ID][B.ID];
        }
        return totCost;
    }
    **/
}
