package SARP2014;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import gurobi.*;
import pbts.entities.LatLng;
import pbts.simulation.SimulatorTimeUnit;

public class Model {
	private SimulatorTimeUnit sim;
	String dir;
	
	public int m;//total passengers
	public int n;//total parcels
	public int s;//total requests
	public ArrayList<vehicle> K;//set of taxis
	public ArrayList<Stop> Vp;//set of passenger stops
	public ArrayList<Stop> Vf;//set of parcel stops
	public ArrayList<Stop> V;//set of stops = Vp U Vf U {0, 2s + 1}
	public ArrayList<Stop> Vp_o;//set of passenger origins
	public ArrayList<Stop> Vf_o;//set of parcel origins
	public ArrayList<Stop> Vp_d;//set of passenger destinations
	public ArrayList<Stop> Vf_d;//set of parcel destinations
	public HashMap<Integer, Integer> qi;//weight of request i
	public HashMap<Integer, Double> di;//distance from the origin to the destination for the request i
	public ArrayList<HashMap<Integer, Integer>> C;//set of pairs (i, j)
	public int eta;//maximum number of requests between one passenger service
	public Double dij[][];//dij: distance between stops i and j
	public int tij[][];//travel time between stops i and j
	public HashMap<Integer, Integer> wi; //maximum delivery time for request i
	public double anpha;
	public double beta;
	public double gamma1;
	public double gamma2;
	public double gamma3;
	public double gamma4;
	public HashMap<Integer, Integer> Pi;//index of request i in a sequence of a taxi.
	public int maxSpd = 40;//kmh
	
	public Model(int m, int n, int k){
		sim = new SimulatorTimeUnit();
		dir = "E:\\Project\\pbts\\git_project\\SPT\\";
		String mapFileName = dir + "SanFrancisco_std\\SanfranciscoRoad-connected-contracted-5-refine-50.txt";
		
		s = m + n;
		K = new ArrayList<vehicle>();
		for(int i = 1; i <= k; i++){
			vehicle taxi = new vehicle(i, s);
			K.add(taxi);
		}
		
		Vp = new ArrayList<Stop>();
		Vf = new ArrayList<Stop>();
		V = new ArrayList<Stop>();
		Vp_o = new ArrayList<Stop>();
		Vf_o = new ArrayList<Stop>();
		Vp_d = new ArrayList<Stop>();
		Vf_d = new ArrayList<Stop>();
		qi = new HashMap<Integer, Integer>();
		di = new HashMap<Integer, Double>();
		C = new ArrayList<HashMap<Integer, Integer>>();
		eta = 2;
			
		dij = new Double[2*s+1][2*s+1];
		tij = new int[2*s+1][2*s+1];
		wi = new HashMap<Integer, Integer>();
		for(int i = 1; i <= s; i++){
			wi.put(i, 50400);
		}
		anpha = 3.5;
		beta = 2.33;
		gamma1 = 2.7;
		gamma2 = 0.9;
		gamma3 = 0.8;
		gamma4 = 3.5;
		Pi = new HashMap<Integer, Integer>();
		
		sim.loadMapFromTextFile(mapFileName);
		
	}
	public double computeManhattanDistance(double lat1, double lng1, double lat2, double lng2){
		return Math.abs(lat1 - lat2) + Math.abs(lng1 - lng2);
	}
	public void readRequestFile(String rqFileName){
		try {
			Scanner in = new Scanner(new File(rqFileName));
			String str = in.nextLine();
			int nPeople = 0;
			int nParcels = 0;
			// System.out.println("people request str = " + str);
			while (true) {
				int id = in.nextInt();
				// System.out.println("people id = " + id);
				if (id == -1)
					break;
				int timePoint = in.nextInt();
				int pickId = in.nextInt();
				int deliveryId = in.nextInt();
				int earlyPick = in.nextInt();
				int latePick = in.nextInt();
				int earlyDelivery = in.nextInt();
				int lateDelivery = in.nextInt();
				double maxDis = in.nextDouble();
				int maxNStop = in.nextInt();
				
				LatLng pickLL = sim.map.mLatLng.get(pickId);
				Stop stO = new Stop(nPeople*2 + 1, pickLL.lat, pickLL.lng, earlyPick, latePick);
				LatLng deliveryLL = sim.map.mLatLng.get(deliveryId);
				Stop stD = new Stop(nPeople*2 + 2, deliveryLL.lat, deliveryLL.lng, earlyDelivery, lateDelivery);
				
				qi.put(nPeople + 1, 3);
				
				double D = computeManhattanDistance(pickLL.lat, pickLL.lng, deliveryLL.lat, deliveryLL.lng);
				di.put(nPeople + 1, D);
				
				for(int i = 0; i < V.size(); i++){
					double distanceij = computeManhattanDistance(pickLL.lat, pickLL.lng, V.get(i).lat, V.get(i).lng);
					dij[i][V.size()+1] = distanceij;
					dij[V.size()+1][i] = distanceij;
					tij[i][V.size()+1] = (int)distanceij/maxSpd;
					tij[V.size()+1][i] = (int)distanceij/maxSpd;
				}
				dij[V.size()+1][V.size()+1] = 0.0;
				tij[V.size()+1][V.size()+1] = 0;
				Vp.add(stO);
				Vp_o.add(stO);
				V.add(stO);
				
				for(int i = 0; i < V.size(); i++){
					double distanceij = computeManhattanDistance(deliveryLL.lat, deliveryLL.lng, V.get(i).lat, V.get(i).lng);
					dij[i][V.size()+1] = distanceij;
					dij[V.size()+1][i] = distanceij;
					tij[i][V.size()+1] = (int)distanceij/maxSpd;
					tij[V.size()+1][i] = (int)distanceij/maxSpd;
				}
				dij[V.size()+1][V.size()+1] = 0.0;
				tij[V.size()+1][V.size()+1] = 0;
				Vp.add(stD);
				Vp_d.add(stD);
				V.add(stD);
				
				str = in.nextLine();
				nPeople++;
			}
			str = in.nextLine();
			str = in.nextLine();
			// System.out.println("parcel request str = " + str);
			while (true) {
				int id = in.nextInt();
				// System.out.println("parcel request id = " + id);
				if (id == -1)
					break;
				int timePoint = in.nextInt();
				int pickId = in.nextInt();
				int deliveryId = in.nextInt();
				int earlyPick = in.nextInt();
				int latePick = in.nextInt();
				int earlyDelivery = in.nextInt();
				int lateDelivery = in.nextInt();
				
				LatLng pickLL = sim.map.mLatLng.get(pickId);
				Stop stO = new Stop((nPeople + nParcels)*2 + 1, pickLL.lat, pickLL.lng, earlyPick, latePick);
				LatLng deliveryLL = sim.map.mLatLng.get(deliveryId);
				Stop stD = new Stop((nPeople + nParcels)*2 + 2, deliveryLL.lat, deliveryLL.lng, earlyDelivery, lateDelivery);
				
				qi.put(nPeople + nParcels + 1, 1);
				
				double D = computeManhattanDistance(pickLL.lat, pickLL.lng, deliveryLL.lat, deliveryLL.lng);
				di.put(nPeople + nParcels + 1, D);
				
				for(int i = 0; i < V.size(); i++){
					double distanceij = computeManhattanDistance(pickLL.lat, pickLL.lng, V.get(i).lat, V.get(i).lng);
					dij[i][V.size()+1] = distanceij;
					dij[V.size()+1][i] = distanceij;
					tij[i][V.size()+1] = (int)distanceij/maxSpd;
					tij[V.size()+1][i] = (int)distanceij/maxSpd;
				}
				dij[V.size()+1][V.size()+1] = 0.0;
				tij[V.size()+1][V.size()+1] = 0;
				Vf.add(stO);
				Vf_o.add(stO);
				V.add(stO);
				
				for(int i = 0; i < V.size(); i++){
					double distanceij = computeManhattanDistance(deliveryLL.lat, deliveryLL.lng, V.get(i).lat, V.get(i).lng);
					dij[i][V.size()+1] = distanceij;
					dij[V.size()+1][i] = distanceij;
					tij[i][V.size()+1] = (int)distanceij/maxSpd;
					tij[V.size()+1][i] = (int)distanceij/maxSpd;
				}
				dij[V.size()+1][V.size()+1] = 0.0;
				tij[V.size()+1][V.size()+1] = 0;
				Vf.add(stD);
				Vf_d.add(stD);
				V.add(stD);
				
				str = in.nextLine();
				nParcels++;
			}
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public void initiallize(){
		String rqFileName = dir + "SARP_minSpd5_maxSpd60_" + s + "_reqs.txt";
		readRequestFile(rqFileName);
	}
	public void establistModel(){
		try{
			GRBEnv env = new GRBEnv();
			GRBModel model = new GRBModel(env);
			
		}
		catch(GRBException ex){
			System.out.println(ex);
		}
	}
}
