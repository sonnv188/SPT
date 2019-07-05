package pbts.datamanipulation;

import java.util.*;
import java.io.*;

import pbts.simulation.RoadMap;
public class ParkingsSanFrancisco {

	public void collectParkingDepots(String dir, String fLatLng, String mapFilename, int nbTaxis){
		try{
			pbts.simulation.RoadMap M = new pbts.simulation.RoadMap();
			M.loadData(mapFilename);
			
			Scanner in = new Scanner(new File(fLatLng));
			
			HashSet<Integer> P = new HashSet<Integer>();
			while(true){
				String line = in.nextLine();
				if(line.equals("-1")) break;
				String[] s = line.split(",");
				double lat = Double.valueOf(s[0]);
				double lng = Double.valueOf(s[1]);
				int p = M.findNearestPoint(lat, lng, 1000);
				System.out.println("ParkingsSanFrancisco, findPoint (" + lat + "," + lng + ") = " + p);
				if(p != -1){
					P.add(p);
				}
			}
			System.out.println("Number of distinc parkings = " + P.size());
			//System.exit(-1);
			
			String fo = dir + "\\depots" + nbTaxis + "-parkings" + P.size() + ".txt";
			PrintWriter out = new PrintWriter(fo);
			
			out.println("Depots");
			int nbTaxisPerDepot = nbTaxis/P.size();
			int count = 0;
			for(int i : P){
				for(int j = 0; j < nbTaxisPerDepot; j++){
					out.print(i + " ");
					count++;
				}
			}
			HashSet<Integer> Q = new HashSet<Integer>();
			for(int i : P) Q.add(i);
			
			for(int j = count+1; j <= nbTaxis; j++){
				for(int i : Q){
					out.print(i + " ");
					Q.remove(i);
					break;
				}
			}
			
			out.println(-1);
			out.println("Parkings");
			
			int capacity = nbTaxisPerDepot + 10;
			
			for(int i : P)
				out.println(i + "  " + capacity);
			out.println(-1);
			out.close();
			in.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParkingsSanFrancisco PS = new ParkingsSanFrancisco();
		//String dir = "C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\SanFrancisco";
		String dir = "SanFrancisco_std";
		int nbTaxis = 1000;
		
		PS.collectParkingDepots(dir,dir + "\\parkings.txt", 
				dir + "\\SanFranciscoRoad-connected-contracted-5-refine-50.txt",nbTaxis);
		
	}

}
