package MEBEDE;

import java.util.ArrayList;

/**
 *
 * @author mzaxa
 */
public class Route {
        
    	ArrayList <Node> nodes;
        double cost;
        double load;
        double capacity;
        double duration;
        
        public Route(double cap) {
            cost = 0;
            duration = 0;
            nodes = new ArrayList();
            load = 0;
            capacity = cap;
        }
}

