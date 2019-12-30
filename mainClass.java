package mebede;

/**
*
* @author mzaxa
*/
public class mainClass {

   
   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) {
       // TODO code application logic here
       VRP vrp = new VRP(100, 50);
       vrp.GenerateNetworkRandomly();
       vrp.Solve();
   }
}

