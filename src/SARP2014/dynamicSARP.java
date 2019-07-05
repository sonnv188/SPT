package SARP2014;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import pbts.algorithms.SequenceOptimizer;
import pbts.entities.ErrorMSG;
import pbts.entities.Itinerary;
import pbts.entities.ItineraryTravelTime;
import pbts.entities.LatLng;
import pbts.entities.ParcelRequest;
import pbts.entities.PeopleRequest;
import pbts.entities.Vehicle;
import pbts.enums.ErrorType;
import pbts.enums.VehicleStatus;
import pbts.onlinealgorithmtimeunit.GetManyTimesThenAddAPopularPointPlanner;
import pbts.onlinealgorithmtimeunit.GreedyExSharingDecisionTimeLimitAndGetManyTimesThenAddAPopularPointPlanner;
import pbts.onlinealgorithmtimeunit.GreedyExchangeSharingDecisionTimeLimitPlanner;
import pbts.onlinealgorithmtimeunit.OnlinePlanner;
import pbts.onlinealgorithmtimeunit.SequenceDecidedBasedOnAPopularPointPlanner;
import pbts.simulation.AnalysisTemplate;
import pbts.simulation.Simulator;
import pbts.simulation.SimulatorTimeUnit;

public class dynamicSARP {
	public SimulatorTimeUnit sim;
	public dynamicSARPplanner planner;
	public ArrayList<Stop> parcelRequests;
	public ArrayList<Stop> peopleRequests;
	public SequenceOptimizer seqOptimizer = null;
	
	public dynamicSARP(){
		sim = new SimulatorTimeUnit();
		SequenceOptimizer seqOptimizer = new SequenceOptimizer(sim, sim.maxPendingStops + 24);
		planner = new dynamicSARPplanner(sim, seqOptimizer);
		//planner = new dynamicSARPandPredictionPlanner(sim);
		parcelRequests = new ArrayList<Stop>();
		peopleRequests = new ArrayList<Stop>();
	}
	
	public String name() {
		return "dynamicSARP";
	}

	/*public double calculateTotalTravelDistance(ArrayList<Stop> stSequence){
		double D = 0;
		for(int i = 1; i < stSequence.size(); i++){
			D += sim.estimateTravelingDistanceHaversine(stSequence.get(i-1).id, stSequence.get(i).id);
		}
		return D;
	}
	
	//check time pick up & delivery people
	public boolean checkValidTimeWindows(int lastPoint, ArrayList<Stop> stopSequence){
		double D = 0;
		int pickIndex = -1;
		int deliveryIndex = -1;
		for(int i = 0; i < stopSequence.size(); i++){
			Stop st = stopSequence.get(i);
			if(i == 0)
				D += sim.estimateTravelingDistanceHaversine(st.id, lastPoint);
			else
				D += sim.estimateTravelingDistanceHaversine(st.id, stopSequence.get(i-1).id);
			
			if(st.peopleSt == true && st.pickUpSt == true && i!=0){
				int tPick = (int) (D/sim.maxSpeedkmh)*3600;
				if(tPick > st.e)
					return false;
				pickIndex = i;
			}
			else if(st.peopleSt == true && st.pickUpSt == false){
				int tDelivery = (int) (D/sim.maxSpeedkmh)*3600;
				if(tDelivery > st.l)
					return false;
				deliveryIndex = i;
			}
		}
		if(deliveryIndex <= pickIndex)
			return false;
		return true;
	}
	
	//check all the position of remain req list to insert request i
	public HashMap checkAllThePositionToInsertParcel(int lastPoint, ParcelRequest parcelReq, ArrayList<Stop> stopSequence, double totalTravelDistance){
		double bestDistance = 1000000;
		int posPick = 0;
		int posDelivery = 1;
		int length = stopSequence.size();
		HashMap result = new HashMap();
		ArrayList<Stop> stSq = new ArrayList<Stop>(stopSequence);
		ArrayList<Stop> stSq2 = new ArrayList<Stop>(stopSequence);
		double curDistance = totalTravelDistance;
		LatLng pickLL = sim.map.mLatLng.get(parcelReq.pickupLocationID);
		LatLng deliveryLL = sim.map.mLatLng.get(parcelReq.deliveryLocationID);
		Stop newPick = new Stop(parcelReq.pickupLocationID, parcelReq.id, pickLL.lat, pickLL.lng, parcelReq.earlyPickupTime, parcelReq.latePickupTime, true, false);
		Stop newDelivery = new Stop(parcelReq.deliveryLocationID, parcelReq.id, deliveryLL.lat, deliveryLL.lng, parcelReq.earlyDeliveryTime, parcelReq.lateDeliveryTime, false, false);
		
		for(int i = 0; i <= length; i++){
			stSq = new ArrayList<Stop>(stopSequence);
			if(i == length)
				stSq.add(newPick);
			else
				stSq.add(i, newPick);
			for(int j = i + 1; j <= length + 1; j++){
				stSq2 = new ArrayList<Stop>(stSq);
				if(j == length + 1)
					stSq2.add(newDelivery);
				else
					stSq2.add(j, newDelivery);
				double D = calculateTotalTravelDistance(stSq2);
				if(checkValidTimeWindows(lastPoint, stSq2)){
					if(bestDistance > D){
						bestDistance = D;
						posPick = i;
						posDelivery = j;
					}
				}
			}
		}
		
		result.put("totalDistance", bestDistance);
		result.put("sequence", stSq2);
		return result;
	}
	
	//check all the position of remain req list to insert request i
	public HashMap checkAllThePositionToInsertPeople(PeopleRequest peoReq, Vehicle taxi){
		double bestDistance = 1000000;
		int posPick = 0;
		int posDelivery = 1;
		int length = taxi.stopSequence.size();
		HashMap result = new HashMap();
		ArrayList<Stop> stSq = new ArrayList<Stop>(taxi.stopSequence);
		ArrayList<Stop> stSq2 = new ArrayList<Stop>(taxi.stopSequence);
		double curDistance = taxi.totalTravelDistance;
		LatLng pickLL = sim.map.mLatLng.get(peoReq.pickupLocationID);
		LatLng deliveryLL = sim.map.mLatLng.get(peoReq.deliveryLocationID);
		Stop newPick = new Stop(peoReq.pickupLocationID, peoReq.id, pickLL.lat, pickLL.lng, peoReq.earlyPickupTime, peoReq.latePickupTime, true, false);
		Stop newDelivery = new Stop(peoReq.deliveryLocationID, peoReq.id, deliveryLL.lat, deliveryLL.lng, peoReq.earlyDeliveryTime, peoReq.lateDeliveryTime, false, false);
		
		for(int i = 0; i <= length; i++){
			stSq = new ArrayList<Stop>(taxi.stopSequence);
			if(i == length)
				stSq.add(newPick);
			else
				stSq.add(i, newPick);
			for(int j = i + 1; j <= length + 1; j++){
				stSq2 = new ArrayList<Stop>(stSq);
				if(j == length + 1)
					stSq2.add(newDelivery);
				else
					stSq2.add(j, newDelivery);
				double D = calculateTotalTravelDistance(stSq2);
				if(checkValidTimeWindows(taxi.lastPoint, stSq2)){
					if(bestDistance > D){
						bestDistance = D;
						posPick = i;
						posDelivery = j;
					}
				}
			}
		}
		
		result.put("totalDistance", bestDistance);
		result.put("sequence", stSq2);
		return result;
	}
	
	//generate initial route for every taxi sequentially
	public Vehicle greedyInsertion(Vehicle taxi){

		ArrayList<ParcelRequest> allParcelReq = new ArrayList<ParcelRequest>(sim.allParcelRequests);
		
		while(taxi.stopSequence.size() < 6){
			if(sim.allParcelRequests.size() == 0)
				break;
			for(int j = 0; j < sim.allParcelRequests.size(); j++){
				ParcelRequest parcelReq = sim.allParcelRequests.get(j);
				HashMap result = checkAllThePositionToInsertParcel(taxi.lastPoint, parcelReq, taxi.stopSequence, taxi.totalTravelDistance);
				taxi.stopSequence = (ArrayList<Stop>)result.get("sequence");
				taxi.totalTravelDistance = (double)result.get("totalDistance");
				allParcelReq.remove(parcelReq);
			}
			sim.allParcelRequests = allParcelReq;
		}
		return taxi;
	}
	
	public HashMap updateStateOfAllTaxisAndGetNearestTaxi(PeopleRequest peoRq){
		double minD = 100000;
		Vehicle bestTaxi = null;
		int index = -1;
		for(int i = 0; i < sim.vehicles.size(); i++){
			Vehicle taxi = sim.vehicles.get(i);
			if(taxi.stopSequence.size() == 0){
				taxi = greedyInsertion(taxi);
				sim.vehicles.set(i, taxi);
			}
			
			//update status of this taxi
			ArrayList<Stop> remainStops = new ArrayList<Stop>(taxi.stopSequence);
			for(int j = 0; j < taxi.stopSequence.size(); j++){
				Stop st = taxi.stopSequence.get(j);
				if(st.l < peoRq.timePoint)
					remainStops.remove(st);
				if(st.peopleSt == true){
					if(st.pickUpSt == true){
						if(st.l > peoRq.timePoint)
							taxi.status = VehicleStatus.GOING_TO_PICKUP_PEOPLE;
						else
							taxi.status = VehicleStatus.GOING_TO_DELIVERY_PEOPLE;
					}
					else{
						if(st.l > peoRq.timePoint)
							taxi.status = VehicleStatus.GOING_TO_DELIVERY_PEOPLE;
						else{
							taxi.status = VehicleStatus.FINISHED_DELIVERY_PEOPLE;
							taxi.lastPoint = st.id;
						}
					}
				}
			}
			taxi.stopSequence = remainStops;
			sim.vehicles.set(i, taxi);
			if(taxi.status == VehicleStatus.REST_AT_PARKING || taxi.status == VehicleStatus.FINISHED_DELIVERY_PEOPLE){
				double D = sim.estimateTravelingDistanceHaversine(taxi.lastPoint, peoRq.pickupLocationID);
				if((int)(D/sim.maxSpeedkmh) * 3600 < peoRq.latePickupTime){
					if(D < minD ){
						bestTaxi = taxi;
						minD = D;
						index = i;
					}
				}
			}
		}
		HashMap result = new HashMap();
		result.put("vehicle", bestTaxi);
		result.put("index", index);
		return result;
	}
	
	/*public HashMap moveGreedyExchange(Vehicle taxi){
		if(sim.allParcelRequests.size() == 0)
			return null;

		double best_distance = taxi.totalTravelDistance;
		ArrayList<Stop> best_sequence = taxi.stopSequence;
		for(int k1 = 0; k1 < taxi.stopSequence.size(); k1++){
			ArrayList<Stop> stSqTemp = new ArrayList<Stop>(taxi.stopSequence);
			stSqTemp.remove(k1);
			for(int k2 = k1; k2 < taxi.stopSequence.size(); k2++){
				if(taxi.stopSequence.get(k2).reqId == taxi.stopSequence.get(k1).reqId){
					stSqTemp.remove(k2);
					break;
				}
			}
			
			double travelDistanceTemp = calculateTotalTravelDistance(stSqTemp);
			
			for(int j = 0; j < sim.allParcelRequests.size(); j++){
				ParcelRequest parcelReq = sim.allParcelRequests.get(j);
				HashMap result = checkAllThePositionToInsertParcel(taxi.lastPoint, parcelReq, stSqTemp, travelDistanceTemp);
				if(taxi.totalTravelDistance > (double)result.get("totalDistance")){
					best_distance = (double)result.get("totalDistance");
					best_sequence = (ArrayList<Stop>)result.get("sequence");
				}
			}
		}
		HashMap result = new HashMap();
		result.put("totalDistance", best_distance);
		result.put("sequence", best_sequence);
		return result;
	}
	
	public void simulateDynamicSARPLi2014(String requestFilename,
			int maxNbParcelsInserted){
		sim.loadRequests(requestFilename);
		sim.initVehicles();
		
		//generate initial route
		for(int i = 0; i < sim.vehicles.size(); i++){
			Vehicle taxi = sim.vehicles.get(i);
			taxi = greedyInsertion(taxi);
			sim.vehicles.set(i, taxi);
			System.out.println("initial route taxi " + i + ", sequence: " + taxi.stopSequence);
		}

		for(int i = 0; i < sim.vehicles.size(); i++){
			Vehicle taxi = sim.vehicles.get(i);
			if(taxi.stopSequence.size() !=0){
				System.out.println("Taxi " + i + " :" + taxi.stopSequence.size());
				for(int j = 0; j < taxi.stopSequence.size(); j++)
					System.out.print(taxi.stopSequence.get(j).id + " --> ");
			}
		}
		System.out.println("======================");
		for(int i = 0; i < sim.allPeopleRequests.size(); i++){
			PeopleRequest peoRq = sim.allPeopleRequests.get(i);
			HashMap resultNearestTaxi = updateStateOfAllTaxisAndGetNearestTaxi(peoRq);
			Vehicle taxi = (Vehicle)resultNearestTaxi.get("vehicle");
			HashMap result = checkAllThePositionToInsertPeople(peoRq, taxi);
			taxi.totalTravelDistance = (double)result.get("totalDistance");
			taxi.stopSequence = (ArrayList<Stop>)result.get("sequence");
			
			//optimize the route by neighborhood
			HashMap exchangeResult = moveGreedyExchange(taxi);
			if(exchangeResult != null){
				taxi.stopSequence = (ArrayList<Stop>)exchangeResult.get("sequence");
				taxi.totalTravelDistance = (double)exchangeResult.get("totalDistance");
			}
			sim.vehicles.set((int)resultNearestTaxi.get("index"), taxi);
			
			System.out.println("request: " + i);
			for(int i1 = 0; i1 < sim.vehicles.size(); i1++){
				Vehicle taxi1 = sim.vehicles.get(i1);
				if(taxi1.stopSequence.size() !=0){
					System.out.println("Taxi " + i1 + " :" + taxi1.stopSequence.size());
					for(int j1 = 0; j1 < taxi1.stopSequence.size(); j1++)
						System.out.print(taxi1.stopSequence.get(j1).id + " --> ");
				}
			}
		}
	}
	
	public void generateInitialRoutes(){
		//generate initial route
		for(int i = 0; i < sim.vehicles.size(); i++){
			Vehicle taxi = sim.vehicles.get(i);
			taxi = greedyInsertion(taxi);
			sim.vehicles.set(i, taxi);
		}
	}
	*/
	public void receiveRequests() {
		sim.queueParcelReq.clear();
		sim.queuePeopleReq.clear();
		while (sim.runningPeopleRequests.size() > 0) {
			PeopleRequest peopleR = sim.runningPeopleRequests.get(0);
			sim.runningPeopleRequests.remove(0);
			sim.queuePeopleReq.add(peopleR);
		}
		while (sim.runningParcelRequests.size() > 0) {
			ParcelRequest parcelR = sim.runningParcelRequests.get(0);
			if (parcelR.timePoint <= sim.T.currentTimePoint + sim.TimePointDuration) {
				sim.runningParcelRequests.remove(0);
				sim.queueParcelReq.add(parcelR);
			} else {
				break;
			}
		}

	}	
	
	public Vehicle greedyInsertionRoute(Vehicle taxi){
		for(int j = 0; j < sim.allParcelRequests.size(); j++){
			if(taxi.pendingParcelReqs.size() == 12 || sim.insertedParcelRequests.size() + sim.nbParcelRejects >= sim.allParcelRequests.size())
				break;
			ParcelRequest parcelReq = sim.allParcelRequests.get(j);
			if (sim.T.stopRequest()){
				if(!sim.rejectedParcelRequests.contains(parcelReq)){
					sim.nbParcelRejects++;
					sim.rejectedParcelRequests.add(parcelReq);
				}
			}
			
			if(!sim.insertedParcelRequests.contains(parcelReq)){
				ArrayList<Integer> newPendingParcelsList = sim.checkAllThePositionToInsertParcel(taxi, parcelReq);
				if(newPendingParcelsList != null){
					taxi.pendingParcelReqs = new ArrayList<Integer>(newPendingParcelsList);
					sim.insertedParcelRequests.add(parcelReq);
					sim.nbParcelRequestsProcessed++;;
				}
			}
		}
		return taxi;
	}
	
	public void generateInitialRoutes(){
		//generate initial route
		for(int i = 0; i < sim.vehicles.size(); i++){
			Vehicle taxi = sim.vehicles.get(i);
			taxi = greedyInsertionRoute(taxi);
			//System.out.println("taxi: " + i + ", pending: " + taxi.pendingParcelReqs.size());
		}
	}
	
	public void simulateDataFromFile(String requestFilename,
			int maxNbParcelsInserted, int maxNbStops){

		
		double t0 = System.currentTimeMillis();
		sim.loadRequestsSARP2014(requestFilename);

		sim.pendingParcelRequests = new ArrayList<ParcelRequest>();

		sim.initVehicles();

		sim.distanceRequests = new ArrayList<Double>();

		sim.queuePeopleReq = new ArrayList<PeopleRequest>();
		sim.queueParcelReq = new ArrayList<ParcelRequest>();

		System.out.println("T = " + sim.T + ", nbTaxis = " + sim.nbTaxis);

		sim.runningParcelRequests = new ArrayList<ParcelRequest>();
		sim.runningPeopleRequests = new ArrayList<PeopleRequest>();
		sim.totalParcelRequests = sim.allParcelRequests.size();
		for (int i = 0; i < sim.allPeopleRequests.size(); i++) {
			PeopleRequest pr = sim.allPeopleRequests.get(i);
			pr.maxNbStops = 14;
			sim.runningPeopleRequests.add(pr);

		}
		for (int i = 0; i < sim.allParcelRequests.size(); i++) {
			ParcelRequest pr = sim.allParcelRequests.get(i);
			sim.runningParcelRequests.add(pr);
		}

		sim.receiveRequests();

		sim.insertedParcelRequests = new ArrayList<ParcelRequest>();
		sim.insertedPeopleRequests = new ArrayList<PeopleRequest>();
		
		generateInitialRoutes();
		System.out.println("total people reqs: " + sim.allPeopleRequests.size() + ", total parcels: " + sim.allParcelRequests.size() + ", inserted parcels: " + sim.insertedParcelRequests.size());

		sim.totalParcelRequests = sim.allParcelRequests.size();
		while (!sim.T.finished()) {
			if((sim.T.currentTimePoint - sim.T.start)%10 == 0 && sim.T.currentTimePoint > sim.T.start){
				if(!sim.T.stopRequest() || !sim.allTaxiRestAtParking())
					sim.logStatistic();
			}
			for (int k = 0; k < sim.nbTaxis; k++) {
				Vehicle vh = sim.vehicles.get(k);
				if (vh.status != VehicleStatus.STOP_WORK)
					vh.moveLong(Simulator.TimePointDuration);

			}

			if (sim.T.stopRequest()) {
				// System.out.println("Simulator::simulate stop request!!!!");
				for (int k = 0; k < sim.nbTaxis; k++) {
					Vehicle vh = sim.vehicles.get(k);

					if (vh.status == VehicleStatus.TRAVEL_WITHOUT_LOAD) {
						System.out
								.println(name()
										+ "::simulateDataFromFile, taxi "
										+ vh.ID
										+ ", stopRequest, status = TRAVEL_WITHOUT_LOAD --> GO BACK TO DEPOT");
						sim.setupRouteBack2Depot(vh);
					} else if (vh.status == VehicleStatus.REST_AT_PARKING) {
						if (vh.lastPoint != sim.mTaxi2Depot.get(vh.ID)) {
							System.out
									.println(name()
											+ "::simulateDataFromFile, taxi "
											+ vh.ID
											+ ", stopRequest, status = REST_AT_PARKING --> GO BACK TO DEPOT");
							sim.setupRouteBack2Depot(vh);
						} else {
							vh.status = VehicleStatus.STOP_WORK;
						}
					}
				}
			}

			for(int k = 0; k < sim.vehicles.size(); k++){
				Vehicle taxi = sim.vehicles.get(k);
				if(!sim.T.stopRequest() && taxi.pendingParcelReqs.size() == 0 && taxi.remainRequestIDs.size() == 0){						
					taxi = greedyInsertionRoute(taxi);
					//sim.vehicles.set(k, taxi);
				}
			}
			for (int i = 0; i < sim.queuePeopleReq.size(); i++) {
				for(int k = 0; k < sim.vehicles.size(); k++){
					Vehicle taxi = sim.vehicles.get(k);
					if(!sim.T.stopRequest() && taxi.pendingParcelReqs.size() == 0 && taxi.remainRequestIDs.size() == 0){						
						taxi = greedyInsertionRoute(taxi);
						//sim.vehicles.set(k, taxi);
					}
				}
				
				PeopleRequest peopleR = sim.queuePeopleReq.get(i);
				
				sim.totalPeopleRequests++;
				if (sim.T.stopRequest()) {
					sim.nbPeopleRejects++;
				} else {
					PeopleRequest pr = peopleR;
					// allPeopleRequests.add(pr);
					System.out.println(name()
							+ "::simulateDataFromFile --> At "
							+ sim.T.timePointHMS(pr.timePoint)
							// + T.currentTimePointHMS()
							+ "[" + sim.T.currentTimePoint + "], people request "
							+ pr.id + " arrives");
					sim.log.println(name() + "::simulateDataFromFile --> At "
							+ sim.T.timePointHMS(pr.timePoint)
							// + T.currentTimePointHMS()
							+ "[" + sim.T.currentTimePoint + "], people request "
							+ pr.id + " arrives");
					double D = sim.dijkstra.queryDistance(pr.pickupLocationID,
							pr.deliveryLocationID);
					if (D > sim.dijkstra.infinity - 1) {
						sim.log.println("At " + sim.T.currentTimePointHMS()
								+ ", cannot serve people request from "
								+ pr.pickupLocationID + " to "
								+ pr.deliveryLocationID
								+ " due to disconnectivity");
						System.out.println(name()
								+ "::simulateDataFromFile --> At "
								+ sim.T.currentTimePointHMS()
								+ ", cannot serve people request from "
								+ pr.pickupLocationID + " to "
								+ pr.deliveryLocationID
								+ " due to disconnectivity");
						sim.nbDisconnectedRequests++;
					} else {
						// planner.processPeopleRequest(pr);
					}
				}
			}

			if (!sim.T.stopRequest() && sim.queuePeopleReq.size() > 0)
				planner.processPeopleRequests(sim.queuePeopleReq);

			sim.nbPeopleRequestsProcessed += sim.queuePeopleReq.size();
			
			sim.T.move(Simulator.TimePointDuration);
			sim.receiveRequests();
			
			if(sim.runningPeopleRequests.size() == 0){
				for(int k = 0; k < sim.vehicles.size(); k++){
					Vehicle taxi = sim.vehicles.get(k);
					for(int l = 0; l < taxi.pendingParcelReqs.size(); l++){
						if(taxi.pendingParcelReqs.get(l) > 0){
							ParcelRequest par = sim.mParcelRequest.get(taxi.pendingParcelReqs.get(l));
							planner.processParcelRequests(par, taxi);
						}
					}
					taxi.pendingParcelReqs.clear();
				}
			}
			// log.println("-------------------");
		}

		double totalDistance = 0;
		int nbUnusedTaxis = 0;
		for (int k = 0; k < sim.vehicles.size(); k++) {
			Vehicle taxi = sim.vehicles.get(k);
			if (taxi.totalTravelDistance <= 0) {
				nbUnusedTaxis++;
				continue;
			}
			// taxi.writeItinerriesToLog();
			totalDistance = totalDistance + taxi.totalTravelDistance;
			int costi = (int) (taxi.totalTravelDistance * sim.gamma3 / 1000);
			sim.log.println("distance of taxi[" + taxi.ID + "] = "
					+ taxi.totalTravelDistance / 1000 + "km, cost fuel = "
					+ costi + "K");
		}
		// logI.println(-2);

		for (int i = 0; i < sim.distanceRequests.size(); i++) {
			double D = sim.distanceRequests.get(i);
			int m = (int) (sim.alpha + sim.gamma1 * D);
			D = D / 1000;
			m = m / 1000;
			sim.log.println("requests " + i + " has distance = " + D
					+ "km, money = " + m + "K");
		}

		sim.cost = (int) totalDistance * sim.gamma3;
		sim.cost = sim.cost / 1000;
		totalDistance = totalDistance / 1000;
		sim.revenue = sim.revenue / 1000;
		sim.log.println("nbPeopleRequests = " + sim.totalPeopleRequests);
		sim.log.println("nbAcceptedPeople = " + sim.acceptedPeopleRequests);
		sim.log.println("nbParcelRequests = " + sim.totalParcelRequests);
		sim.log.println("nbAcceptedParcelRequests = " + sim.acceptedParcelRequests);
		sim.log.println("nbDisconnected Requests = " + sim.nbDisconnectedRequests);
		sim.log.println("total distance = " + totalDistance + "km");
		sim.log.println("revenue = " + sim.revenue + "K");
		sim.log.println("cost fuel = " + sim.cost + "K");
		double benefits = sim.revenue - sim.cost;
		sim.log.println("benefits = " + benefits + "K");
		sim.log.println("nbUnusedTaxis = " + nbUnusedTaxis);

		System.out
				.println("Simulator::simulateWithAParcelFollow --> FINISHED, allPeopleRequests.sz = "
						+ sim.allPeopleRequests.size()
						+ ", allParcelRequests.sz = "
						+ sim.allParcelRequests.size());

		double t = System.currentTimeMillis() - t0;
		t = t * 0.001;
		System.out.println("simulation time = " + t);
	}
	
	public static void main(String[] args){
		String data_dir = "C:\\DungPQ\\projects\\SPT\\SPT\\";
		String mapFileName = data_dir + "SanFrancisco_std\\SanfranciscoRoad-connected-contracted-5-refine-50.txt";
		String configFileName = data_dir + "SanFrancisco_std\\config-parameters.txt";
		String depotParkingFileName = data_dir + "SanFrancisco_std\\depots1000-parkings34.txt";
		int maxNbPendingStops = 12;
		int maxTimeReceiveRequest = 86400;
		int startSimulationTime = 0;
		int decisionTime = 15;
		for(int day = 1; day <= 9; day++){
			String requestFileName = data_dir + "SanFrancisco_std\\ins_day_" + day + "_minSpd_5_maxSpd_60.txt";
			String plannerName = "dynamicSARPplanner";
			String progressiveStatisticFileName = data_dir + "SanFrancisco_std\\all_in_8h_19h\\ins_day_" + day +"_minSpd_5_maxSpd_60.txt-planner"+ plannerName + "-maxPendingStops10-decisionTime15-statistic-progress.txt";
			String itineraryFileName = data_dir + "SanFrancisco_std\\all_in_8h_19h\\ins_day_" + day +"_minSpd_5_maxSpd_60.txt-planner"+ plannerName + "-maxPendingStops10-decisionTime15-itinerary.txt";
			String summaryFileName = data_dir + "SanFrancisco_std\\all_in_8h_19h\\ins_day_" + day +"_minSpd_5_maxSpd_60.txt-planner"+ plannerName + "-maxPendingStops10-decisionTime15-summary.xml";
			
			for(int i = 0; i < args.length; i++){
				if(args[i].equals("--mapFileName"))
					mapFileName = args[i+1];
				else if(args[i].equals("--configFileName"))
					configFileName = args[i+1];
				else if(args[i].equals("--requestFileName"))
					requestFileName = args[i+1];
				else if(args[i].equals("--depotParkingFileName"))
					depotParkingFileName = args[i+1];
				else if(args[i].equals("--plannerName"))
					plannerName = args[i+1];
				else if(args[i].equals("--progressiveStatisticFileName"))
					progressiveStatisticFileName = args[i+1];
				else if(args[i].equals("--itineraryFileName"))
					itineraryFileName = args[i+1];
				else if(args[i].equals("--summaryFileName"))
					summaryFileName = args[i+1];
				else if(args[i].equals("--maxNbPendingStops"))
					maxNbPendingStops = Integer.valueOf(args[i+1]);
				else if(args[i].equals("--maxTimeReceiveRequest"))
					maxTimeReceiveRequest = Integer.valueOf(args[i+1]);
				else if(args[i].equals("--startSimulationTime"))
					startSimulationTime = Integer.valueOf(args[i+1]);
				else if(args[i].equals("--decisionTime"))
					decisionTime = Integer.valueOf(args[i+1]);
				
			}
			
			System.out.println("Parameters:\n" +
			"mapFileName = " + mapFileName + "\n" + 
			"configFileName = " + configFileName + "\n" +
			"requestFileName = " + requestFileName + "\n" +
			"depotParkingFileName = " + depotParkingFileName + "\n" +
			"plannerName = " + plannerName + "\n"+
			"progressiveStatisticFileName = " + progressiveStatisticFileName + "\n" +
			"itineraryFileName = " + itineraryFileName + "\n" +
			"summaryFileName = " + summaryFileName + "\n" +
			"maxNbPendingStops = " + maxNbPendingStops + "\n" +
			"maxTimeReceiveRequest = " + maxTimeReceiveRequest + "\n" +
			"startSimulationTime = " + startSimulationTime + "\n" +
			"decisionTime = " + decisionTime
					);
			dynamicSARP dSarp = new dynamicSARP();
			
			dSarp.sim.loadMapFromTextFile(mapFileName);
	
			dSarp.sim.loadParameters(configFileName);
			dSarp.sim.loadDepotParkings(depotParkingFileName);
			dSarp.sim.initialize();
			dSarp.sim.statFilename = progressiveStatisticFileName;
			dSarp.sim.resetLog();
			dSarp.sim.getPredictionInfo();
			
			dSarp.sim.terminateRequestTime = maxTimeReceiveRequest;
			dSarp.sim.terminateWorkingTime = maxTimeReceiveRequest;
			dSarp.sim.TimePointDuration = decisionTime;
			dSarp.sim.maxPendingStops = maxNbPendingStops;
			dSarp.sim.startWorkingTime = startSimulationTime;
			dSarp.sim.maxPendingStops = 14;
			
			dSarp.sim.initTimeHorizon();
			
			dSarp.simulateDataFromFile(requestFileName, 6, 14);
			dSarp.sim.writeTaxiItineraries(itineraryFileName);
			
			dSarp.sim.initVehicles();
			dSarp.sim.loadRequestsSARP2014(requestFileName);
	
			dSarp.sim.relaxTimeWindowParcelRequests();
			HashMap<Integer, ItineraryTravelTime> itineraries = dSarp.sim.loadItineraries(itineraryFileName);
			AnalysisTemplate AT = dSarp.sim.analyzeSolution(itineraries, summaryFileName);
			dSarp.sim.finalize();
		}
		

		if(true) return;
	}
}
