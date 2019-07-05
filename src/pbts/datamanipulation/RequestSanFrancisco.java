package pbts.datamanipulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.io.*;
import java.util.*;

import pbts.entities.LatLng;
import pbts.entities.ParcelRequest;
import pbts.entities.PeopleRequest;
import pbts.shortestpaths.DijkstraBinaryHeap;
import pbts.simulation.*;
class PeopleRequestSanFrancisco{
	int id;
	int timeCall;
	int pickupLocationID;
	int deliveryLocationID;
	int earlyPickupTime;
	int latePickupTime;
	int earlyDeliveryTime;
	int lateDeliveryTime;
	double maxTravelDistance;
	int maxNbStops;
	ArrayList<HashMap<String, Double>> latlngPickup;
	ArrayList<HashMap<String, Double>> latlngDelivery;
	ArrayList<Double> timePickup;
	ArrayList<Double> timeDelivery;
	
	public PeopleRequestSanFrancisco(int id, int timeCall, int pickupLocationID, int deliveryLocationID,
			int earlyPickupTime, int latePickupTime, int earlyDeliveryTime, int lateDeliveryTime,
			double maxTravelDistance, int maxNbStops){
		this.id = id;
		this.timeCall = timeCall;
		this.pickupLocationID = pickupLocationID;
		this.deliveryLocationID = deliveryLocationID;
		this.earlyPickupTime = earlyPickupTime;
		this.latePickupTime = latePickupTime;
		this.earlyDeliveryTime = earlyDeliveryTime;
		this.lateDeliveryTime = lateDeliveryTime;
		this.maxTravelDistance = maxTravelDistance;
		this.maxNbStops = maxNbStops;
	}
}
public class RequestSanFrancisco {

	RoadMap M;
	
	public void loadMap(String fn){
		M = new RoadMap();
		M.loadData(fn);
	}
	public void analyze(String fn){
		try{
			double t0 = System.currentTimeMillis();
			double min_lat = 100000;
			double max_lat = -min_lat;
			double min_lng = 1000000;
			double max_lng = -min_lng;
			Scanner in = new Scanner(new File(fn));
			int lines = 0;
			HashSet<Integer> taxis = new HashSet<Integer>();
			HashMap<Integer, Integer> mR = new HashMap<Integer, Integer>();
			ArrayList<Integer> days = new ArrayList<Integer>();
			HashMap<Integer, HashSet<Integer>> mTaxiDay = new HashMap<Integer, HashSet<Integer>>();
			while(in.hasNext()){
				int taxiID = in.nextInt();
				lines++;
				//System.out.println("Line "+ lines + ", taxiID = "+ taxiID);
				if(taxiID == 0) break;
				taxis.add(taxiID);
				double s_t = in.nextDouble();
				Date s_d = new Date((long)s_t*1000);
				double s_lat = in.nextDouble();
				double s_lng = in.nextDouble();
				double e_t = in.nextDouble();
				Date e_d = new Date((long)e_t*1000);
				double e_lat = in.nextDouble();
				double e_lng = in.nextDouble();
				
				min_lat = min_lat < s_lat ? min_lat : s_lat;
				min_lat = min_lat < e_lat ? min_lat : e_lat;
				max_lat = max_lat > s_lat ? max_lat : s_lat;
				max_lat = max_lat > e_lat ? max_lat : e_lat;
				
				min_lng = min_lng < s_lng ? min_lng : s_lng;
				min_lng = min_lng < e_lng ? min_lng : e_lng;
				max_lng = max_lng > s_lng ? max_lng : s_lng;
				max_lng = max_lng > e_lng ? max_lng : e_lng;
				
				System.out.println("Line " + lines + ", taxi " + taxiID + ", Start " + s_lat + "," + s_lng + " AT " + s_d.getDate() + "/" + s_d.getMonth() + "/" + 
				s_d.getYear() + " - " + s_d.getHours() + ":" + s_d.getMinutes() + ":" + s_d.getSeconds() + ", End  " + e_lat + "," + e_lng + " AT " +  
				e_d.getDate() + "/" + e_d.getMonth() + "/" + 
				e_d.getYear() + " - " + e_d.getHours() + ":" + e_d.getMinutes() + ":" + e_d.getSeconds() + ", day " + e_d.getDay());
				
				int d = s_d.getDate();
				if(mR.get(d) == null){
					days.add(d);
					mR.put(d, 1);
				}else{
					mR.put(d, mR.get(d) + 1);
				}
				if(mTaxiDay.get(d) == null){
					mTaxiDay.put(d, new HashSet<Integer>());
				}else{
					mTaxiDay.get(d).add(taxiID);
				}
			}
			for(int i = 0; i < days.size(); i++){
				int d = days.get(i);
				System.out.println("day " + d + ", has " + mR.get(d) + " requests, nbTaxis = " + mTaxiDay.get(d).size());
			}
			double t = (System.currentTimeMillis() - t0)*0.001;
			System.out.println("taxis = " + taxis.size() + ", time = " + t);
			in.close();
			
			System.out.println("Request --> min_lat = " + min_lat + ", max_lat = " + max_lat + ", min_lng = " + min_lng + ", max_lng = " + max_lng);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public ArrayList<PeopleRequest> loadPeopleRequests(String fn){
		ArrayList<PeopleRequest> allPeopleRequests = new ArrayList<PeopleRequest>();
		try{
			Scanner in = new Scanner(new File(fn));
			
			
			String str = in.nextLine();
			//System.out.println("people request str = " + str);
			while(true){
				int id = in.nextInt();
				//System.out.println("people id = " + id);
				if(id == -1) break;
				int timePoint = in.nextInt();
				int pickupLocationID = in.nextInt();
				int deliveryLocationID = in.nextInt();
				int earlyPickupTime = in.nextInt();
				int latePickupTime = in.nextInt();
				int earlyDeliveryTime = in.nextInt();
				int lateDeliveryTime = in.nextInt();
				double maxDistance = in.nextDouble();
				int maxNbStops = in.nextInt();
				
				if(timePoint < 0) continue;
				//timePoint = earlyPickupTime;
				
				PeopleRequest pr = new PeopleRequest(pickupLocationID,deliveryLocationID);
				pr.id = id; 
				pr.timePoint = timePoint;
				pr.earlyPickupTime = earlyPickupTime;
				pr.latePickupTime = latePickupTime;
				pr.earlyDeliveryTime = earlyDeliveryTime;
				pr.lateDeliveryTime = lateDeliveryTime;
				pr.maxTravelDistance = maxDistance;
				pr.maxNbStops = maxNbStops;
				allPeopleRequests.add(pr);
				//mPeopleRequest.put(pr.id, pr);
			}
				in.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		preprocessTimeCallPeopleRequest(allPeopleRequests);
		return allPeopleRequests;
	}

	public void extractRequestToSpeedTxt(String fn, String dir, double threshold, int delta_time_call, 
			int delta_pickup_late, int delta_delivery_late, double maxTravelDistanceFactor, 
			double minDistanceRequest, int maxNbStops, double minSpeed, double maxSpeed, int maxTimReceiveRequest){
		try{
			DijkstraBinaryHeap dijkstra = new DijkstraBinaryHeap(M.V, M.A);
			
			double t0 = System.currentTimeMillis();
			double min_lat = 100000;
			double max_lat = -min_lat;
			double min_lng = 1000000;
			double max_lng = -min_lng;
			double minDis = 100000000;
			double maxDis = -minDis;
			Scanner in = new Scanner(new File(fn));
			int lines = 0;
			HashSet<Integer> taxis = new HashSet<Integer>();
			HashMap<Integer, ArrayList<PeopleRequestSanFrancisco>> mR = new HashMap<Integer, ArrayList<PeopleRequestSanFrancisco>>();
			ArrayList<Integer> days = new ArrayList<Integer>();
			HashMap<Integer, HashSet<Integer>> mTaxiDay = new HashMap<Integer, HashSet<Integer>>();
			int id = 0;
			try{
				String fo = "speed.txt";
				PrintWriter out = new PrintWriter(fo);
				while(in.hasNext()){
					int taxiID = in.nextInt();
					lines++;
					//if(lines > 5000) break;
					//System.out.println("Line "+ lines + ", taxiID = "+ taxiID);
					if(taxiID == 0) break;
					taxis.add(taxiID);
					double s_t = in.nextDouble();
					Date s_d = new Date((long)s_t*1000);
					double s_lat = in.nextDouble();
					double s_lng = in.nextDouble();
					double e_t = in.nextDouble();
					Date e_d = new Date((long)e_t*1000);
					double e_lat = in.nextDouble();
					double e_lng = in.nextDouble();
					
					/*
					min_lat = min_lat < s_lat ? min_lat : s_lat;
					min_lat = min_lat < e_lat ? min_lat : e_lat;
					max_lat = max_lat > s_lat ? max_lat : s_lat;
					max_lat = max_lat > e_lat ? max_lat : e_lat;
					
					min_lng = min_lng < s_lng ? min_lng : s_lng;
					min_lng = min_lng < e_lng ? min_lng : e_lng;
					max_lng = max_lng > s_lng ? max_lng : s_lng;
					max_lng = max_lng > e_lng ? max_lng : e_lng;
					*/
					
					//System.out.println("Line " + lines + ", taxi " + taxiID + ", Start " + s_lat + "," + s_lng + " AT " + s_d.getDate() + "/" + s_d.getMonth() + "/" + 
					//s_d.getYear() + " - " + s_d.getHours() + ":" + s_d.getMinutes() + ":" + s_d.getSeconds() + ", End  " + e_lat + "," + e_lng + " AT " +  
					//e_d.getDate() + "/" + e_d.getMonth() + "/" + 
					//e_d.getYear() + " - " + e_d.getHours() + ":" + e_d.getMinutes() + ":" + e_d.getSeconds() + ", day " + e_d.getDay());
					
					int d = s_d.getDate();
					if(d!= 1)
						continue;
					int pickupLocationID = M.findNearestPoint(s_lat, s_lng, threshold);
					if(pickupLocationID < 0) continue;
					int deliveryLocationID = M.findNearestPoint(e_lat, e_lng, threshold);
					if(deliveryLocationID < 0) continue;
					
					double dis = dijkstra.queryDistance(pickupLocationID, deliveryLocationID);
					//int travelTime = SimulatorTimeUnit.getTravelTime(dis, maxSpeed);
					double dis2 = Math.abs(s_lat - s_lng) + Math.abs(e_lat - e_lng);
					if(dis < dis2)
						dis = dis2;
					
					int p_t = TimeHorizon.hms2Int(s_d.getHours(), s_d.getMinutes(), s_d.getSeconds());
					int d_t = TimeHorizon.hms2Int(e_d.getHours(), e_d.getMinutes(), e_d.getSeconds());
					double delta = d_t -p_t;
					if(delta <= 0)
						continue;
					out.println(p_t + " " + (dis*3.6/delta) + " " + s_lat + " " + s_lng + " " + e_lat + " " + e_lng);	
					System.out.println("id = " + taxiID + ", dis = " + dis + ", time = " + delta + ", v =" + dis*3.6/delta);
				}
				out.close();
			}catch(Exception e){
				
			}
			
			double t = (System.currentTimeMillis() - t0)*0.001;
			System.out.println("taxis = " + taxis.size() + ", time = " + t);
			in.close();
			
			System.out.println("Request --> min_lat = " + min_lat + ", max_lat = " + max_lat + ", min_lng = " + min_lng + ", max_lng = " + max_lng);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void analyzeRequest(String fn, int taxi_id, double threshold, double minDistanceRequest){
		try{
			DijkstraBinaryHeap dijkstra = new DijkstraBinaryHeap(M.V, M.A);
			
			double t0 = System.currentTimeMillis();
			double min_lat = 100000;
			double max_lat = -min_lat;
			double min_lng = 1000000;
			double max_lng = -min_lng;
			double minDis = 100000000;
			double maxDis = -minDis;
			Scanner in = new Scanner(new File(fn));
			int lines = 0;
			HashSet<Integer> taxis = new HashSet<Integer>();
			HashMap<Integer, ArrayList<ServedRequest>> mR = new HashMap<Integer, ArrayList<ServedRequest>>();
			ArrayList<Integer> days = new ArrayList<Integer>();
			HashMap<Integer, HashSet<Integer>> mTaxiDay = new HashMap<Integer, HashSet<Integer>>();
			int id = 0;
			while(in.hasNext()){
				int taxiID = in.nextInt();
				lines++;
				//if(lines > 5000) break;
				System.out.println("Line "+ lines + ", taxiID = "+ taxiID);
				if(taxiID == 0) break;
				//if(taxiID != taxi_id) continue;
				taxis.add(taxiID);
				double s_t = in.nextDouble();
				Date s_d = new Date((long)s_t*1000);
				double s_lat = in.nextDouble();
				double s_lng = in.nextDouble();
				double e_t = in.nextDouble();
				Date e_d = new Date((long)e_t*1000);
				double e_lat = in.nextDouble();
				double e_lng = in.nextDouble();
				if(taxiID != taxi_id) continue;
				if(s_d.getDate() != e_d.getDate()){
					System.out.println("DATE DIFFERENT: start = (" + s_d.getDate() + "/" + s_d.getHours() + ":" + s_d.getMinutes() + ":" + 
				s_d.getSeconds() + ") end = (" + e_d.getDate() + "/"+ e_d.getHours() + ":" + e_d.getMinutes() + ":" + e_d.getSeconds() + ")");
					continue;
				}
				int d = s_d.getDate();
				if(mR.get(d) == null){
					days.add(d);
					mR.put(d, new ArrayList<ServedRequest>());
				}
				//else{
					//mR.put(d, mR.get(d) + 1);
					int pickupLocationID = M.findNearestPoint(s_lat, s_lng, threshold);
					if(pickupLocationID < 0) continue;
					int deliveryLocationID = M.findNearestPoint(e_lat, e_lng, threshold);
					if(deliveryLocationID < 0) continue;
					int p_t = TimeHorizon.hms2Int(s_d.getHours(), s_d.getMinutes(), s_d.getSeconds());
					int d_t = TimeHorizon.hms2Int(e_d.getHours(), e_d.getMinutes(), e_d.getSeconds());
					double dis = dijkstra.queryDistance(pickupLocationID, deliveryLocationID);
					if(dis < minDistanceRequest){
						System.out.println("TOO SMALL DISTANCE " + dis);
						continue;
					}
					minDis = minDis < dis ? minDis : dis;
					maxDis = maxDis > dis ? maxDis : dis;
					
					min_lat = min_lat < s_lat ? min_lat : s_lat;
					min_lat = min_lat < e_lat ? min_lat : e_lat;
					max_lat = max_lat > s_lat ? max_lat : s_lat;
					max_lat = max_lat > e_lat ? max_lat : e_lat;
					
					min_lng = min_lng < s_lng ? min_lng : s_lng;
					min_lng = min_lng < e_lng ? min_lng : e_lng;
					max_lng = max_lng > s_lng ? max_lng : s_lng;
					max_lng = max_lng > e_lng ? max_lng : e_lng;
					
					double ti = System.currentTimeMillis() - t0;
					ti = ti*0.001;
					//System.out.println("Line " + lines + ", days.sz = "+ days.size() + 
					//		", distance = " + dis + ", minDis = " + minDis + 
					//		", maxDis = " + maxDis + ", time = " + ti);
					id++;
					
					mR.get(d).add(new ServedRequest(taxiID,pickupLocationID,p_t,deliveryLocationID,d_t));
										
				//}
				if(mTaxiDay.get(d) == null){
					mTaxiDay.put(d, new HashSet<Integer>());
				}else{
					mTaxiDay.get(d).add(taxiID);
				}
			
			}
			double t = (System.currentTimeMillis() - t0)*0.001;
			System.out.println("taxis = " + taxis.size() + ", time = " + t);
			in.close();
			
			System.out.println("Request --> min_lat = " + min_lat + ", max_lat = " + max_lat + ", min_lng = " + min_lng + ", max_lng = " + max_lng);
		
			for(int i = 0; i < days.size(); i++){
				int d = days.get(i);
				ServedRequest[] R = new ServedRequest[mR.get(d).size()];
				for(int j = 0; j < mR.get(d).size(); j++)
					R[j] = mR.get(d).get(j);
				//sort the list;
				for(int i1 = 0; i1 < R.length-1; i1++)
					for(int i2 = i1+1; i2 < R.length; i2++)
						if(R[i1].pickupTime > R[i2].pickupTime){
							ServedRequest tmpR = R[i1]; R[i1] = R[i2]; R[i2] = tmpR;
						}
				mR.get(d).clear();
				for(int j = 0; j < R.length; j++)
					mR.get(d).add(R[j]);
			}
			
			PrintWriter log = new PrintWriter("RequestSanFrancisco-taxi-" + taxi_id + "-log.txt");
			for(int i = 0; i < days.size(); i++){
				int d = days.get(i);
				for(int j = 0; j < mR.get(d).size(); j++){
					ServedRequest R = mR.get(d).get(j);
					log.println(R.taxiID + "\t" + d + "\t" + R.pickupLocationID + "\t" + R.pickupTime + "\t" + R.deliveryLocationID + "\t" + R.deliveryTime);
				}
			}
			
			java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
			double minSpeed1 = 10000000;
			double maxSpeed1 = -minSpeed1;
			double minSpeed12 = 100000000;
			double maxSpeed12 = -minSpeed12;
			
			for(int i = 0; i < days.size(); i++){
				int d = days.get(i);
				for(int j = 0; j < mR.get(d).size()-1; j++){
					ServedRequest R1 = mR.get(d).get(j);
					ServedRequest R2 = mR.get(d).get(j+1);
					double d1 = dijkstra.queryDistance(R1.pickupLocationID, R1.deliveryLocationID);
					double d12 = dijkstra.queryDistance(R1.deliveryLocationID, R2.pickupLocationID);
					double t1 = R1.deliveryTime - R1.pickupTime;
					double t12 = R2.pickupTime - R1.deliveryTime;
					double speed1 = (d1*1.0/(t1))*3600/1000;
					double speed12 = (d12*1.0/(t12))*3600/1000;
					System.out.println("day " + d + ", d1 = " + d1 + "m, t1 = " + t1 + "s, speed1 = " + df.format(speed1) + "km/h, d12 = " + d12 + "m, t12 = " + t12 + "s, speed12 = " + df.format(speed12) + "km/h");
					log.println("day " + d + ", d1 = " + d1 + "m, t1 = " + t1 + "s, speed1 = " + df.format(speed1) + "km/h, d12 = " + d12 + "m, t12 = " + t12 + "s, speed12 = " + df.format(speed12) + "km/h");
					minSpeed1 = minSpeed1 < speed1 ? minSpeed1 : speed1;
					maxSpeed1 = maxSpeed1 > speed1 ? maxSpeed1 : speed1;
					minSpeed12 = minSpeed12 < speed12 ? minSpeed12 : speed12;
					maxSpeed12 = maxSpeed12 > speed12 ? maxSpeed12 : speed12;
				}
			}
			
			System.out.println("minSpeed 1= " + df.format(minSpeed1) + ", maxSpeed1 = " + df.format(maxSpeed1) + ", minSpeed12 = " +
			df.format(minSpeed12) + ", maxSpeed12 = " + df.format(maxSpeed12));
			log.println("minSpeed1 = " + df.format(minSpeed1) + ", maxSpeed1 = " + df.format(maxSpeed1) + ", minSpeed12 = " +
					df.format(minSpeed12) + ", maxSpeed12 = " + df.format(maxSpeed12));
					
			log.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	private void preprocessTimeCallPeopleRequest(ArrayList<PeopleRequest> peopleReq){
		if(peopleReq.size() == 0) return;
		PeopleRequest[] t = new PeopleRequest[peopleReq.size()];
		for(int i = 0; i < peopleReq.size(); i++)
			t[i] = peopleReq.get(i);
		
		// sort t in an increasing order
		for(int i = 0; i < t.length-1; i++){
			for(int j = i+1; j < t.length; j++){
				if(t[i].timePoint > t[j].timePoint){
					PeopleRequest tmp = t[i]; t[i] = t[j]; t[j] = tmp;
				}
			}
		}
		
		int d = 0;
		int nic = 0;
		int v = t[0].timePoint;
		for(int i = 1; i < t.length; i++){
			if(t[i].timePoint == v){
				d++;
				t[i].timePoint += (nic+d);
			}else{
				if(t[i-1].timePoint >= t[i].timePoint){
					nic = t[i-1].timePoint - t[i].timePoint + 1;
				}else nic = 0;
				d = 0;
				v = t[i].timePoint;
				t[i].timePoint += nic;
			}
		}
		peopleReq.clear();
		for(int i = 0; i < t.length; i++)
			peopleReq.add(t[i]);
	}
	
	public String name(){ return "RequestSanFrancisco";}
	public void genParcelRequestFromPeopleRequest(String fReq, String newFReq, int speed, int endRequestTime, int endSimulationTime,
			int delta_pickup_time, int delta_delivery_time){
		Simulator sim = new Simulator();
		System.out.println("RequestSanFrancisco::genParcelRequestFromPeopleRequest, fReq = "+ fReq);
		ArrayList<PeopleRequest> allpeopleReq = loadPeopleRequests(fReq);
		System.out.println("RequestSanFrancisco::genParcelRequestFromPeopleRequest --> AllPeopleRequest.sz = " + allpeopleReq.size());
		
		DijkstraBinaryHeap dijkstra = new DijkstraBinaryHeap(M.V, M.A);
		
		ArrayList<ParcelRequest> parcelReq = new ArrayList<ParcelRequest>();
		ArrayList<PeopleRequest> peopleReq = new ArrayList<PeopleRequest>();
		
		for(int i = 0; i < allpeopleReq.size(); i++){
			PeopleRequest pr = allpeopleReq.get(i);
			if(i%2==0){
				
				ParcelRequest par = new ParcelRequest();
				par.id = pr.id;
				par.timePoint = pr.timePoint;
					
				
				double d = dijkstra.queryDistance(pr.pickupLocationID, pr.deliveryLocationID);
				int t = SimulatorTimeUnit.getTravelTime(d, speed);
				
				if(pr.timePoint > endRequestTime) continue;
				if(pr.timePoint + t > endSimulationTime) continue;
				
				par.earlyPickupTime = pr.timePoint;
				par.latePickupTime = endSimulationTime - t;
				par.earlyDeliveryTime = pr.timePoint;
				par.lateDeliveryTime = endSimulationTime;
				par.pickupLocationID = pr.pickupLocationID;
				par.deliveryLocationID = pr.deliveryLocationID;	
				
				parcelReq.add(par);
			}else{
				double d = dijkstra.queryDistance(pr.pickupLocationID, pr.deliveryLocationID);
				int t = SimulatorTimeUnit.getTravelTime(d, speed);
				
				if(pr.timePoint > endRequestTime) continue;
				if(pr.timePoint + t > endSimulationTime) continue;
				
				pr.earlyPickupTime = pr.timePoint;
				pr.latePickupTime = pr.earlyPickupTime + delta_pickup_time;// delta_pickup_time = 15 minutes
				pr.earlyDeliveryTime = pr.timePoint;
				pr.lateDeliveryTime = pr.latePickupTime + t + delta_delivery_time;// delta_delivery_time = 30 minutes
				
				peopleReq.add(pr);
			}
			System.out.println(name() + "::genParcelRequestFromPeopleRequest file " + fReq + " --> finished " + i + "/" + allpeopleReq.size());
		}
		try{
			PrintWriter out = new PrintWriter(newFReq);
			out.println("r.id  r.timeCall  r.pickupLocationID r.deliveryLocationID  r.earlyPickupTime  r.latePickupTime  r.earlyDeliveryTime  r.lateDeliveryTime  r.maxTravelDistance  r.maxNbStops");
			for(int i = 0; i < peopleReq.size(); i++){
				PeopleRequest pr = peopleReq.get(i);
				out.println(pr.id + " " + pr.timePoint + " " + pr.pickupLocationID + " " + 
				pr.deliveryLocationID + " " + pr.earlyPickupTime + " " + pr.latePickupTime + " " +
				pr.earlyDeliveryTime + " " + pr.lateDeliveryTime + " " + pr.maxTravelDistance + " " + pr.maxNbStops);
			}
			out.println(-1);
			
			out.println("r.id  r.timeCall  r.pickupLocationID r.deliveryLocationID  r.earlyPickupTime  r.latePickupTime  r.earlyDeliveryTime  r.lateDeliveryTime");
			for(int i = 0; i < parcelReq.size(); i++){
				ParcelRequest pr = parcelReq.get(i);
				out.println(pr.id + " " + pr.timePoint + " " + pr.pickupLocationID + " " + 
				pr.deliveryLocationID + " " + pr.earlyPickupTime + " " + pr.latePickupTime + " " +
				pr.earlyDeliveryTime + " " + pr.lateDeliveryTime);
			}
			out.println(-1);
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void extractRequest(String fn, String dir, double threshold, int delta_time_call, 
			int delta_pickup_late, int delta_delivery_late, double maxTravelDistanceFactor, 
			double minDistanceRequest, int maxNbStops, double minSpeed, double maxSpeed, int maxTimReceiveRequest){
		try{
			DijkstraBinaryHeap dijkstra = new DijkstraBinaryHeap(M.V, M.A);
			
			double t0 = System.currentTimeMillis();
			double min_lat = 100000;
			double max_lat = -min_lat;
			double min_lng = 1000000;
			double max_lng = -min_lng;
			double minDis = 100000000;
			double maxDis = -minDis;
			Scanner in = new Scanner(new File(fn));
			int lines = 0;
			HashSet<Integer> taxis = new HashSet<Integer>();
			HashMap<Integer, ArrayList<PeopleRequestSanFrancisco>> mR = new HashMap<Integer, ArrayList<PeopleRequestSanFrancisco>>();
			ArrayList<Integer> days = new ArrayList<Integer>();
			HashMap<Integer, HashSet<Integer>> mTaxiDay = new HashMap<Integer, HashSet<Integer>>();
			int id = 0;
			while(in.hasNext()){
				int taxiID = in.nextInt();
				lines++;
				//if(lines > 5000) break;
				//System.out.println("Line "+ lines + ", taxiID = "+ taxiID);
				if(taxiID == 0) break;
				taxis.add(taxiID);
				double s_t = in.nextDouble();
				Date s_d = new Date((long)s_t*1000);
				double s_lat = in.nextDouble();
				double s_lng = in.nextDouble();
				double e_t = in.nextDouble();
				Date e_d = new Date((long)e_t*1000);
				double e_lat = in.nextDouble();
				double e_lng = in.nextDouble();
				
				/*
				min_lat = min_lat < s_lat ? min_lat : s_lat;
				min_lat = min_lat < e_lat ? min_lat : e_lat;
				max_lat = max_lat > s_lat ? max_lat : s_lat;
				max_lat = max_lat > e_lat ? max_lat : e_lat;
				
				min_lng = min_lng < s_lng ? min_lng : s_lng;
				min_lng = min_lng < e_lng ? min_lng : e_lng;
				max_lng = max_lng > s_lng ? max_lng : s_lng;
				max_lng = max_lng > e_lng ? max_lng : e_lng;
				*/
				
				//System.out.println("Line " + lines + ", taxi " + taxiID + ", Start " + s_lat + "," + s_lng + " AT " + s_d.getDate() + "/" + s_d.getMonth() + "/" + 
				//s_d.getYear() + " - " + s_d.getHours() + ":" + s_d.getMinutes() + ":" + s_d.getSeconds() + ", End  " + e_lat + "," + e_lng + " AT " +  
				//e_d.getDate() + "/" + e_d.getMonth() + "/" + 
				//e_d.getYear() + " - " + e_d.getHours() + ":" + e_d.getMinutes() + ":" + e_d.getSeconds() + ", day " + e_d.getDay());
				
				int d = s_d.getDate();
				if(mR.get(d) == null){
					days.add(d);
					mR.put(d, new ArrayList<PeopleRequestSanFrancisco>());
				}else{
					//mR.put(d, mR.get(d) + 1);
					int pickupLocationID = M.findNearestPoint(s_lat, s_lng, threshold);
					if(pickupLocationID < 0) continue;
					int deliveryLocationID = M.findNearestPoint(e_lat, e_lng, threshold);
					if(deliveryLocationID < 0) continue;
					
					double dis = dijkstra.queryDistance(pickupLocationID, deliveryLocationID);
					int travelTime = SimulatorTimeUnit.getTravelTime(dis, maxSpeed);
					
					int p_t = TimeHorizon.hms2Int(s_d.getHours(), s_d.getMinutes(), s_d.getSeconds());
					int d_t = TimeHorizon.hms2Int(e_d.getHours(), e_d.getMinutes(), e_d.getSeconds());
					int timeCall = p_t - delta_time_call;// 10 minutes
					if(timeCall <= 0 || timeCall > maxTimReceiveRequest) continue;
					
					int earlyPickupTime = timeCall;
					int latePickupTime = p_t + delta_pickup_late;
					int earlyDeliveryTime = earlyPickupTime + travelTime;// d_t;
					int lateDeliveryTime = earlyDeliveryTime + delta_delivery_late;// d_t + delta_delivery_late;
					
					
					//double t = d_t - p_t;
					//if(t <= 0) continue;
					//double sp = dis*1.0/(t)*3.6;
					//if(sp < minSpeed || sp > maxSpeed) continue;
					
					if(dis < minDistanceRequest) continue;
					minDis = minDis < dis ? minDis : dis;
					maxDis = maxDis > dis ? maxDis : dis;
					
					min_lat = min_lat < s_lat ? min_lat : s_lat;
					min_lat = min_lat < e_lat ? min_lat : e_lat;
					max_lat = max_lat > s_lat ? max_lat : s_lat;
					max_lat = max_lat > e_lat ? max_lat : e_lat;
					
					min_lng = min_lng < s_lng ? min_lng : s_lng;
					min_lng = min_lng < e_lng ? min_lng : e_lng;
					max_lng = max_lng > s_lng ? max_lng : s_lng;
					max_lng = max_lng > e_lng ? max_lng : e_lng;
					
					double ti = System.currentTimeMillis() - t0;
					ti = ti*0.001;
					System.out.println("Line " + lines + ", days.sz = "+ days.size() + 
							", distance = " + dis + ", minDis = " + minDis + 
							", maxDis = " + maxDis + ", time = " + ti);
					id++;
					
					mR.get(d).add(new PeopleRequestSanFrancisco(id,timeCall,pickupLocationID,
							deliveryLocationID,earlyPickupTime, latePickupTime,earlyDeliveryTime,
							lateDeliveryTime,dis*maxTravelDistanceFactor,maxNbStops));
					
				}
				if(mTaxiDay.get(d) == null){
					mTaxiDay.put(d, new HashSet<Integer>());
				}else{
					mTaxiDay.get(d).add(taxiID);
				}
			}
			for(int i = 0; i < days.size(); i++){
				int d = days.get(i);
				try{
					String fo = dir + "\\" + "request_day_" + d + "_minSpeed_" + minSpeed + "_maxSpeed_" + maxSpeed + ".txt";
					PrintWriter out = new PrintWriter(fo);
					out.println("r.id  r.timeCall  r.pickupLocationID " +  
							"r.deliveryLocationID  r.earlyPickupTime  r.latePickupTime " +
							" r.earlyDeliveryTime  r.lateDeliveryTime " + 
							" r.maxTravelDistance  r.maxNbStops");
					for(int j = 0; j < mR.get(d).size(); j++){
						PeopleRequestSanFrancisco r = mR.get(d).get(j);
						out.println(r.id + " " + r.timeCall + " " + r.pickupLocationID + " " + 
						r.deliveryLocationID + " " + r.earlyPickupTime + " " + r.latePickupTime +
						" " + r.earlyDeliveryTime + " " + r.lateDeliveryTime + " " + 
						r.maxTravelDistance + " " + r.maxNbStops);
					}
					out.println(-1);
					out.close();
					System.out.println("day " + d + ", has " + mR.get(d).size() + " requests, nbTaxis = " + mTaxiDay.get(d).size());
			
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
			double t = (System.currentTimeMillis() - t0)*0.001;
			System.out.println("taxis = " + taxis.size() + ", time = " + t);
			in.close();
			
			System.out.println("Request --> min_lat = " + min_lat + ", max_lat = " + max_lat + ", min_lng = " + min_lng + ", max_lng = " + max_lng);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void extractRequestAndCreateStatisticFiles(String fn, String dir, double threshold, int delta_time_call, 
			int delta_pickup_late, int delta_delivery_late, double maxTravelDistanceFactor, 
			double minDistanceRequest, int maxNbStops, double minSpeed, double maxSpeed, int maxTimReceiveRequest){
		try{
			DijkstraBinaryHeap dijkstra = new DijkstraBinaryHeap(M.V, M.A);
			
			double t0 = System.currentTimeMillis();
			double min_lat = 100000;
			double max_lat = -min_lat;
			double min_lng = 1000000;
			double max_lng = -min_lng;
			double minDis = 100000000;
			double maxDis = -minDis;
			Scanner in = new Scanner(new File(fn));
			int lines = 0;
			HashSet<Integer> taxis = new HashSet<Integer>();
			HashMap<Integer, ArrayList<PeopleRequestSanFrancisco>> mR = new HashMap<Integer, ArrayList<PeopleRequestSanFrancisco>>();
			ArrayList<Integer> days = new ArrayList<Integer>();
			HashMap<Integer, HashSet<Integer>> mTaxiDay = new HashMap<Integer, HashSet<Integer>>();
			int id = 0;
			while(in.hasNext()){
				int taxiID = in.nextInt();
				lines++;
				//if(lines > 5000) break;
				//System.out.println("Line "+ lines + ", taxiID = "+ taxiID);
				if(taxiID == 0) break;
				taxis.add(taxiID);
				double s_t = in.nextDouble();
				Date s_d = new Date((long)s_t*1000);
				double s_lat = in.nextDouble();
				double s_lng = in.nextDouble();
				double e_t = in.nextDouble();
				Date e_d = new Date((long)e_t*1000);
				double e_lat = in.nextDouble();
				double e_lng = in.nextDouble();
				
				/*
				min_lat = min_lat < s_lat ? min_lat : s_lat;
				min_lat = min_lat < e_lat ? min_lat : e_lat;
				max_lat = max_lat > s_lat ? max_lat : s_lat;
				max_lat = max_lat > e_lat ? max_lat : e_lat;
				
				min_lng = min_lng < s_lng ? min_lng : s_lng;
				min_lng = min_lng < e_lng ? min_lng : e_lng;
				max_lng = max_lng > s_lng ? max_lng : s_lng;
				max_lng = max_lng > e_lng ? max_lng : e_lng;
				*/
				
				//System.out.println("Line " + lines + ", taxi " + taxiID + ", Start " + s_lat + "," + s_lng + " AT " + s_d.getDate() + "/" + s_d.getMonth() + "/" + 
				//s_d.getYear() + " - " + s_d.getHours() + ":" + s_d.getMinutes() + ":" + s_d.getSeconds() + ", End  " + e_lat + "," + e_lng + " AT " +  
				//e_d.getDate() + "/" + e_d.getMonth() + "/" + 
				//e_d.getYear() + " - " + e_d.getHours() + ":" + e_d.getMinutes() + ":" + e_d.getSeconds() + ", day " + e_d.getDay());
				
				int d = s_d.getDate();
				if(mR.get(d) == null){
					days.add(d);
					mR.put(d, new ArrayList<PeopleRequestSanFrancisco>());
				}else{
					//mR.put(d, mR.get(d) + 1);
					int pickupLocationID = M.findNearestPoint(s_lat, s_lng, threshold);
					if(pickupLocationID < 0) continue;
					int deliveryLocationID = M.findNearestPoint(e_lat, e_lng, threshold);
					if(deliveryLocationID < 0) continue;
					
					double dis = dijkstra.queryDistance(pickupLocationID, deliveryLocationID);
					int travelTime = SimulatorTimeUnit.getTravelTime(dis, maxSpeed);
					
					int p_t = TimeHorizon.hms2Int(s_d.getHours(), s_d.getMinutes(), s_d.getSeconds());
					int d_t = TimeHorizon.hms2Int(e_d.getHours(), e_d.getMinutes(), e_d.getSeconds());
					int timeCall = p_t - delta_time_call;// 10 minutes
					if(timeCall <= 0 || timeCall > maxTimReceiveRequest) continue;
					
					int earlyPickupTime = timeCall;
					int latePickupTime = p_t + delta_pickup_late;
					int earlyDeliveryTime = earlyPickupTime + travelTime;// d_t;
					int lateDeliveryTime = earlyDeliveryTime + delta_delivery_late;// d_t + delta_delivery_late;
					
					
					//double t = d_t - p_t;
					//if(t <= 0) continue;
					//double sp = dis*1.0/(t)*3.6;
					//if(sp < minSpeed || sp > maxSpeed) continue;
					
					if(dis < minDistanceRequest) continue;
					minDis = minDis < dis ? minDis : dis;
					maxDis = maxDis > dis ? maxDis : dis;
					
					min_lat = min_lat < s_lat ? min_lat : s_lat;
					min_lat = min_lat < e_lat ? min_lat : e_lat;
					max_lat = max_lat > s_lat ? max_lat : s_lat;
					max_lat = max_lat > e_lat ? max_lat : e_lat;
					
					min_lng = min_lng < s_lng ? min_lng : s_lng;
					min_lng = min_lng < e_lng ? min_lng : e_lng;
					max_lng = max_lng > s_lng ? max_lng : s_lng;
					max_lng = max_lng > e_lng ? max_lng : e_lng;
					
					double ti = System.currentTimeMillis() - t0;
					ti = ti*0.001;
					System.out.println("Line " + lines + ", days.sz = "+ days.size() + 
							", distance = " + dis + ", minDis = " + minDis + 
							", maxDis = " + maxDis + ", time = " + ti);
					id++;
					
					mR.get(d).add(new PeopleRequestSanFrancisco(id,timeCall,pickupLocationID,
							deliveryLocationID,earlyPickupTime, latePickupTime,earlyDeliveryTime,
							lateDeliveryTime,dis*maxTravelDistanceFactor,maxNbStops));
					
				}
				if(mTaxiDay.get(d) == null){
					mTaxiDay.put(d, new HashSet<Integer>());
				}else{
					mTaxiDay.get(d).add(taxiID);
				}
			}
			for(int i = 0; i < days.size(); i++){
				int d = days.get(i);
				try{
					int[] listPd = {1440, 86400};
					int[] mss = {60, 1};
					for(int u = 0; u < listPd.length; u++){
						int periods = listPd[u];
						int ms = mss[u];
						int[] reqs = new int[periods];
						for(int f = 0; f < periods; f++){
							reqs[f] = 0;	
						}
						for(int j = 0; j < mR.get(d).size(); j++){
							PeopleRequestSanFrancisco r = mR.get(d).get(j);
							int ix = r.timeCall/ms;
							reqs[ix]++;
						}
						String fo = dir + "\\taxiDemands\\" + "request_day_" + d + "_prediod_" + periods + ".txt";
						PrintWriter out = new PrintWriter(fo);
						for(int k = 0; k < periods; k++){
							out.println(k + " " + reqs[k]);
							System.out.println("day " + d + ", has " + mR.get(d).size() + " requests, nbTaxis = " + mTaxiDay.get(d).size());
						}
						out.close();
					}
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
			double t = (System.currentTimeMillis() - t0)*0.001;
			System.out.println("taxis = " + taxis.size() + ", time = " + t);
			in.close();
			
			System.out.println("Request --> min_lat = " + min_lat + ", max_lat = " + max_lat + ", min_lng = " + min_lng + ", max_lng = " + max_lng);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void extractRequest(String tripsFN, String dir, double minSpeed, double maxSpeed, int maxTimeReceiveRequest){
		int threshold = 100;// 100m
		int delta_time_call = 600;// 10 minutes
		int delta_pickup_late = 300;// 5 minutes
		int delta_delivery_late = 1800;//30 minutes
		double maxTravelDistanceFactor = 2;
		int maxNbStops = 5;
		//double minSpeed = 5;//km/h
		//double maxSpeed = 60;//km/h
		double minDistanceRequestAccepted = 1000;// extract only request having distance >= 1000
		
		extractRequestAndCreateStatisticFiles(tripsFN, dir,
				threshold,delta_time_call,delta_pickup_late,delta_delivery_late,maxTravelDistanceFactor,
				minDistanceRequestAccepted,maxNbStops,minSpeed,maxSpeed,maxTimeReceiveRequest);
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//pbts.simulation.RoadMap map = new pbts.simulation.RoadMap();
		//map.loadData("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\SanFrancisco-contracted.txt");
	
		String home_dir =  "C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\code\\pbts\\";
		
		RequestSanFrancisco RS = new RequestSanFrancisco();
		String dir = home_dir + "SanFrancisco\\";
		//String dir = "C:\\DungPQ\\projects\\pbts\\SanFrancisco\\";
		RS.loadMap("E:\\Project\\pbts\\git_project\\SPT\\SanFrancisco_std\\SanFranciscoRoad-connected-contracted-5-refine-50.txt");
		//RS.analyze("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\2010_03.trips");
		
		//RS.analyzeRequest("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\2010_03.trips",49,100,1000);
		
		
		
		
		//String dir1 = "C:\\DungPQ\\projects\\pbts\\SanFrancisco_std\\";
		String dir1 = home_dir + "SanFrancisco_std_all_24h\\";
		
		int maxSpeed = 60;// kmh
		int minSpeed = 5;//kmh
		int maxTimeReceiveRequest = 3600*24;
		int maxSimulationTime = 3600*26;
		int delta_pickup_time = 900;// at most 15 minutes from time call
		int delta_delivery_time = 1800;// delivery late at most 30 minutes from pickup
		
		String tripsFN = "E:\\Project\\pbts\\git_project\\SPT\\SanFrancisco_std\\2010_03\\2010_03.trips";
		RS.extractRequest(tripsFN,dir1,minSpeed, maxSpeed, maxTimeReceiveRequest);
		
		/*
		for(int d = 1; d <= 31; d++){
			RS.genParcelRequestFromPeopleRequest(dir + "request_day_" + d + "_minSpeed_" + minSpeed + ".0_maxSpeed_" + maxSpeed + ".0.txt", 
				dir1 + "ins_day_" + d + "_minSpd_" + minSpeed + "_maxSpd_" + maxSpeed + ".txt",
				maxSpeed, maxTimeReceiveRequest, maxSimulationTime, delta_pickup_time, delta_delivery_time);
		}
		*/
		
	}

}
