package pbts.prediction.behrouz.runners;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import pbts.simulation.SimulatorTimeUnit;
import pbts.simulation.Simulator;
import pbts.entities.LatLng;
import pbts.prediction.behrouz.fixedrate.FixedRateSampler;

public class Runner {
	private FixedRateSampler frs;
	private SimulatorTimeUnit sim;
	String dir;
	public Runner(){
		frs = new FixedRateSampler();
		sim = new SimulatorTimeUnit();
		dir = "E:\\Project\\pbts\\git_project\\SPT\\";
		String mapFileName = dir + "SanFrancisco_std\\SanfranciscoRoad-connected-contracted-5-refine-50.txt";
		sim.loadMapFromTextFile(mapFileName);
		
	}
	public ArrayList getRequestInPeriod(int period){
		if(period >= 0 && period < 96)
			return frs.getRequests(period);
		return null;
	}
	
	public int percentageAppears(ArrayList<Integer> popularRq, ArrayList<Integer> allRq){
		
		int t = 0;
		for(int i = 0; i < popularRq.size(); i++){
			if(allRq.indexOf(popularRq.get(i)) >= 0)
				t++;
		}
		/*System.out.println("\n");
		System.out.println("The number of all requests at period: " + allRq.size());
		System.out.println("The percentage appears of popular requests in all request list at period: " + t + "/" + popularRq.size());*/
		return t;
	}
	public void computeSimilarity(){
		String fileName = dir + "predictionInfo.txt";
		try{
			PrintWriter out= new PrintWriter(fileName);
			
			int avgPer = 0;
			out.println("Period TotalRequest TotalPopularRequest Occurences Percent");
			for(int period = 0;period < 96; period++){
				
				ArrayList<Integer> popularRqId;
				
				popularRqId = getRequestInPeriod(period);
				ArrayList<Integer> allRqIdAtPeriod = new ArrayList<Integer>();
				
				for(int i = 1; i <= 31; i++){
					String rqFileName = dir + "SanFrancisco_std\\ins_day_" + i + "_minSpd_5_maxSpd_60.txt";
					try {
						Scanner in = new Scanner(new File(rqFileName));
						String str = in.nextLine();
						// System.out.println("people request str = " + str);
						while (true) {
							int id = in.nextInt();
							// System.out.println("people id = " + id);
							if (id == -1)
								break;
							int timePoint = in.nextInt();
							if(timePoint >= period*900 && timePoint < (period + 1)*900){
								int parcelPickupLocId = in.nextInt();
								allRqIdAtPeriod.add(parcelPickupLocId);
							}
							str = in.nextLine();
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
							if(timePoint >= period*900 && timePoint < (period + 1)*900){
								int peoplePickupLocId = in.nextInt();
								allRqIdAtPeriod.add(peoplePickupLocId);
							}
							str = in.nextLine();
						}
						in.close();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				int occurences = percentageAppears(popularRqId, allRqIdAtPeriod);
				int per = occurences*100/popularRqId.size();
				out.println(period + " " + allRqIdAtPeriod.size() + " " + popularRqId.size() + " " + occurences + " " + per);
				avgPer += per;
				/*System.out.println("\n");
				System.out.println("Popular Pickup locations at period " + period + ":");
				for (int pickup_point : PopularRequestsId)
					System.out.print(pickup_point + "\t");
				System.out.println("\n");
				System.out.println("All pickup locations at period " + period + ":");
				for (int all_pickup_point : allRqIdAtPeriod)
					System.out.print(all_pickup_point + "\t");
				System.out.println("\n");
				System.out.println("Percentage of appearance at " + period + ": " + per + "%");
				System.out.println("\n");*/
			}
			avgPer = avgPer / 96;
			out.println("average of percentage: " + avgPer);
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/****
	 * Trong khung thoi gian t, new request co nam gan nhung diem trong du doan hay ko
	 * Ket qua: ti le new request nam gan nhung diem du doan la 92%
	 */
	public void ShortestDistanceStatistics(){
		String fileName = dir + "shortestDistanceStatistic-1km.txt";
		try{
			PrintWriter out= new PrintWriter(fileName);
			out.println("Day TotalReq nReqInPopularArea Percentage");
			int perMonth = 0;
			for(int i = 1; i <= 31; i++){
				String rqFileName = dir + "SanFrancisco_std\\ins_day_" + i + "_minSpd_5_maxSpd_60.txt";
				int sum = 0;
				int nReq = 0;
				try {
					Scanner in = new Scanner(new File(rqFileName));
					String str = in.nextLine();
					// System.out.println("people request str = " + str);
					while (true) {
						int id = in.nextInt();
						// System.out.println("people id = " + id);
						if (id == -1)
							break;
						int timePoint = in.nextInt();
						int peoplePickupLocId = in.nextInt();
						sum++;
						int period = timePoint/900;
						ArrayList<Integer> ppIdInPeriod = new ArrayList<Integer>();
						ppIdInPeriod = frs.getRequests(period);
						double shortestDis = 1000000;
						for(int k = 0; k < ppIdInPeriod.size(); k++){
							double D = sim.estimateTravelingDistanceHaversine(peoplePickupLocId, ppIdInPeriod.get(k));
							//Find the nearest points in popular points
							if(shortestDis > D)
								shortestDis = D;
						}
						if(shortestDis <= 1)
							nReq++;
						str = in.nextLine();
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
						int parcelPickupLocId = in.nextInt();
						sum++;
						int period = timePoint/900;
						ArrayList<Integer> ppIdInPeriod = new ArrayList<Integer>();
						ppIdInPeriod = frs.getRequests(period);
						double shortestDis = 1000000;
						for(int k = 0; k < ppIdInPeriod.size(); k++){
							double D = sim.estimateTravelingDistanceHaversine(parcelPickupLocId, ppIdInPeriod.get(k));
							//Find the nearest points in popular points
							if(shortestDis > D)
								shortestDis = D;
						}
						if(shortestDis <= 1)
							nReq++;
						str = in.nextLine();
					}
					in.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				System.out.println("Day: " + i);
				if(sum != 0){
					out.println(i + " " + sum + " " + nReq + " " + nReq*100/sum);
					perMonth += nReq*100/sum;
				}
			}
			out.println("Percentage of the popular: " + perMonth/31);
			out.close();
			System.out.println("Done!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/*********
	 * Kiem tra tai nhung diem du doan co bao nhieu phan tram new request xuat hien.
	 * @param args
	 */
	public void popularPointAcceptedInPeriod(){
		String fileName = dir + "popularPointAcceptedStatistic-Day3-1km.txt";
		try{
			PrintWriter out= new PrintWriter(fileName);
			
			int avgPer = 0;
			out.println("Period TotalPp PpAccepted Percent");
			for(int period = 0;period < 96; period++){
				ArrayList<Integer> popularRqId;
				popularRqId = getRequestInPeriod(period);
				ArrayList<Integer> allRqIdAtPeriod = new ArrayList<Integer>();
				int acceptPoint = 0;
				for(int i = 3; i <= 3; i++){
					String rqFileName = dir + "SanFrancisco_std\\ins_day_" + i + "_minSpd_5_maxSpd_60.txt";
					try {
						Scanner in = new Scanner(new File(rqFileName));
						String str = in.nextLine();
						// System.out.println("people request str = " + str);
						while (true) {
							int id = in.nextInt();
							// System.out.println("people id = " + id);
							if (id == -1)
								break;
							int timePoint = in.nextInt();
							if(timePoint >= period*900 && timePoint < (period + 1)*900){
								int parcelPickupLocId = in.nextInt();
								allRqIdAtPeriod.add(parcelPickupLocId);
							}
							str = in.nextLine();
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
							if(timePoint >= period*900 && timePoint < (period + 1)*900){
								int peoplePickupLocId = in.nextInt();
								allRqIdAtPeriod.add(peoplePickupLocId);
							}
							str = in.nextLine();
						}
						in.close();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				
				for(int k = 0; k < popularRqId.size(); k++){
					for(int j = 0; j < allRqIdAtPeriod.size(); j++){
						if(sim.estimateTravelingDistanceHaversine(popularRqId.get(k), allRqIdAtPeriod.get(j)) <= 1){
							acceptPoint++;
							break;
						}
					}
				}
				System.out.println("Period: " + period);
				out.println(period + " " + popularRqId.size() + " " + acceptPoint + " " + acceptPoint*100 / popularRqId.size());
				avgPer += acceptPoint*100 / popularRqId.size();
			}
			avgPer = avgPer / 96;
			out.println("Average of percentage: " + avgPer);
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void compareTwoListPopularPoint(){
		String fileName = dir + "compareTwoListPp-1km.txt";
		try{
			PrintWriter out= new PrintWriter(fileName);
			out.println("Period FirstGet SecondGet Percentage");
			for(int period = 0; period < 95; period++){
				ArrayList<Integer> popularRqId_1;
				popularRqId_1 = getRequestInPeriod(period);
				ArrayList<Integer> popularRqId_2;
				popularRqId_2 = getRequestInPeriod(period);
				if(popularRqId_1.size() < popularRqId_2.size()){
					ArrayList<Integer> popularRqId_temp = new ArrayList<Integer>();
					for(int i = 0; i < popularRqId_2.size(); i++)
						popularRqId_temp.add(popularRqId_2.get(i));
					for(int i = 0; i < popularRqId_1.size(); i++){
						for(int j = 0; j < popularRqId_2.size(); j++){
							if(sim.estimateTravelingDistanceHaversine(popularRqId_1.get(i), popularRqId_2.get(j)) <= 1){
								popularRqId_temp.remove(popularRqId_2.get(j));
							}
						}
					}
					out.println(popularRqId_1.size() + " " + popularRqId_2.size() + " " + popularRqId_temp.size()*100 / popularRqId_2.size());
				}
				else{
					ArrayList<Integer> popularRqId_temp = new ArrayList<Integer>();
					for(int i = 0; i < popularRqId_1.size(); i++)
						popularRqId_temp.add(popularRqId_1.get(i));
					for(int i = 0; i < popularRqId_2.size(); i++){
						for(int j = 0; j < popularRqId_1.size(); j++){
							if(sim.estimateTravelingDistanceHaversine(popularRqId_1.get(j), popularRqId_2.get(i)) <= 1){
								popularRqId_temp.remove(popularRqId_1.get(j));
							}
						}
					}
					out.println(popularRqId_1.size() + " " + popularRqId_2.size() + " " + popularRqId_temp.size()*100 / popularRqId_1.size());
				}
			}
			System.out.println("Done!");
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public static void main(String[] args) {
		
		String[] weekdays = {
			"Monday", "Tuesday", "Wednesday",
			"Thursday", "Friday", "Saturday",
			"Sunday"
		};
		
		FixedRateSampler frs = new FixedRateSampler();
		Runner run = new Runner();
		//run.computeShortestDistance(30);
		//run.computeSimilarity();
		run.ShortestDistanceStatistics();
		run.popularPointAcceptedInPeriod();
		run.compareTwoListPopularPoint();
		/*int dayOfWeek = 2;
		int period = 0;
		ArrayList<Integer> requests;
		requests = frs.getRequests(period);
		
		
		System.out.println("Pickup locations at period " + period + ":");
		for (int pickup_point : requests)
			System.out.print(pickup_point + "\t");
		System.out.println("\n");
		
		requests = frs.getRequests(dayOfWeek, period);
		System.out.println("Pickup locations at period " + period + " on " + weekdays[dayOfWeek] + ":");
		for (int pickup_point : requests)
			System.out.print(pickup_point + "\t");
		System.out.println();*/
	}

}
