package project_mebede;


public class ExerciseMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

        VRP vrp = new VRP(100, 1500, 1200);
        vrp.generateNetworkRandomly();
        
        for (int i = 0; i < 100; i++) {
		
		System.out.println(vrp.allNodes.get(i).x + "  " + vrp.allNodes.get(i).y);
		
        }
        
	}

}
