package pbts.entities;

import java.util.*;

import SARP2014.Stop;

import java.io.*;

import pbts.enums.ErrorType;
import pbts.enums.VehicleAction;
import pbts.enums.VehicleStatus;
import pbts.simulation.*;

public class Vehicle {

	/**
	 * @param args
	 */

	public int ID;
	public RoadMap map;
	public Simulator sim;
	public int lastPoint;
	public int lastIndexPoint;// index of lastPoint in currentItinerary
	public int remainTimeToNextPoint;// remain time to reach the point
										// nextItinerary[0]
	public int remainTimeToNextDeparture;// remain time to next departure status

	public VehicleStatus status;

	public ArrayList<ItineraryTravelTime> LI;// list of itineraries during the
												// working session
	
	public ArrayList<Stop> stopSequence;//use to SARP2014
	public ArrayList<Integer> pendingParcelReqs;//use to SARP2014
	public int nComeBackDepot;
	
	public ItineraryTravelTime currentItinerary;// the pre-computed itinerary of
												// the vehicle,
	/*
	 * the vehicle will follow this itinerary if no further events occurred this
	 * itinerary might be updated when further requests arrive
	 */
	public ArrayList<Integer> remainRequestIDs;// list of remaining requests in
												// services (represented by id,
												// '+' means pickup, '-' means
												// delivery)
	public double remainDistance;// remain distance from current point to the
									// end of the itinerary

	public HashMap<Integer, Integer> mRequestID2Index;
	
	public ArrayList<Integer> indicesCompleteServiceSequence;/* list of indices i such that remainRequestIDs from i to the end
															* is the list of both pickup and delivery points of requests
	 														*/
	
	public ArrayList<PeopleRequest> bookedPeoReq;
	public ArrayList<ParcelRequest> bookedParReq;

	public ArrayList<Integer> peopleReqIDonBoard;
	public ArrayList<Integer> parcelReqIDonBoard;

	public int startTimePointNewItinerary;

	public HashMap<Integer, VehicleStatus> mStatus;// mStatus.get(i) is the new
													// status of the vehicle
													// from ith point of
													// currentItinerary
													// in other words: at ith
													// point, the status of the
													// vehicle will be
													// mStatus.get(i)

	public ItineraryTravelTime nextItinerary;
	public HashMap<Integer, VehicleStatus> mNextStatus;

	public HashMap<Integer, Integer> mService;// mService.get(i) is the id of
												// people or parcel request that
												// the vehicle serves at ith
												// point of currentItinerary
												// mService.get(t) is the id of
												// request of people or parcel
												// that the vehicle serves at
												// time point t

	// analyzed information
	public double totalTravelDistance;
	public int nbRequestsServed;
	
	/*
	 * SPECIFICATION: at current position, whenever an event arrives, the
	 * vehicle must go to nextItinerary[0]
	 */

	public PrintWriter log = null;
	public PrintWriter logI = null;
	public TimeHorizon T;

	public Vehicle(int ID, RoadMap map, Simulator sim) {
		this.map = map;
		this.sim = sim;
		this.ID = ID;
		mStatus = new HashMap<Integer, VehicleStatus>();
		mService = new HashMap<Integer, Integer>();
		LI = new ArrayList<ItineraryTravelTime>();
		remainRequestIDs = new ArrayList<Integer>();
		peopleReqIDonBoard = new ArrayList<Integer>();
		parcelReqIDonBoard = new ArrayList<Integer>();
		currentItinerary = new ItineraryTravelTime();
		lastIndexPoint = 0;
		totalTravelDistance = 0;
		nbRequestsServed = 0;
		mRequestID2Index = new HashMap<Integer, Integer>();
		indicesCompleteServiceSequence = new ArrayList<Integer>();	
		nComeBackDepot = 0;
		stopSequence = new ArrayList<Stop>();
		pendingParcelReqs = new ArrayList<Integer>();
	}

	public String nextItinerary2String() {
		String s = "";
		for (int i = 0; i < currentItinerary.size(); i++) {
			s = s + "[idx: " + i + ", point: " + currentItinerary.get(i)
					+ ", status: " + getStatusDescription(mStatus.get(i))
					+ ", action = "
					+ getActionDescription(currentItinerary.getAction(i))
					+ ", ta = " + currentItinerary.getArrivalTime(i)
					+ ", td = " + currentItinerary.getDepartureTime(i)
					+ ", requestID = " + currentItinerary.getRequestID(i)
					+ "]\n";
			// if(i > 100) break;
		}
		return s;
	}

	public void establishCompletePickupDeliveryPoints(){
		indicesCompleteServiceSequence.clear();
		int sum = 0;
		for(int i = remainRequestIDs.size()-1; i >= 0; i--){
			int rid = remainRequestIDs.get(i);
			sum += rid;
			if(sum == 0){
				indicesCompleteServiceSequence.add(i);
			}
		}
		//if(indicesCompleteServiceSequence.size() > 0){
			//System.out.println(name() + "::establishCompletePickupDeliveryPoints -> System.exit(-1)");
			//sim.exit();
		//}
	}
	
	public boolean checkNotConstains(int fromIndex, ArrayList<Integer> req){
		// return true if itinerary starting fromIndex does not contains any request of req
		for(int i = fromIndex; i < currentItinerary.size(); i++){
			int rid = currentItinerary.getRequestID(i);
			if(rid > 0){
				for(int j = 0; j < req.size(); j++){
					if(Math.abs(req.get(j)) == rid) return false;
				}
			}
		}
		return true;
	}
	public boolean checkRemainRequestConsistent(int fromIndex){
		int idx = 0;
		for(int i = fromIndex; i < currentItinerary.size(); i++){
			int rid = currentItinerary.getRequestID(i);
			if(rid > 0){
				VehicleAction act = currentItinerary.getAction(i);
				if(act == VehicleAction.DELIVERY_PARCEL || act == VehicleAction.DELIVERY_PEOPLE) rid = -rid;
			}
			if(idx > remainRequestIDs.size()-1) return false;
			if(rid != remainRequestIDs.get(idx)) return false;
			idx++;
		}
		if(idx < remainRequestIDs.size()) return false;// itinerary is scanned completely, but remainRequestIDs not scanned completely
		return true;
	}
	public boolean checkStatusConsistent() {
		if (status == VehicleStatus.DELIVERY_PARCEL) {

		} else if (status == VehicleStatus.DELIVERY_PEOPLE) {

		} else if (status == VehicleStatus.FINISHED_DELIVERY_PARCEL) {

		} else if (status == VehicleStatus.FINISHED_DELIVERY_PEOPLE) {

		} else if (status == VehicleStatus.FINISHED_PICKUP_PARCEL) {

		} else if (status == VehicleStatus.FINISHED_PICKUP_PEOPLE) {

		} else if (status == VehicleStatus.PICKUP_PARCEL) {

		} else if (status == VehicleStatus.PICKUP_PEOPLE) {

		} else if (status == VehicleStatus.REST_AT_PARKING) {
			if (currentItinerary != null)
				return false;
		}

		return true;
	}

	public String getCurrenPositionDescription() {
		String s = "";
		s = s + "lastIndexPoint = " + lastIndexPoint + ", lastPoint = "
				+ lastPoint;
		return s;
	}

	public String getStatusDescription() {
		String s = "";
		if (status == VehicleStatus.REST_AT_PARKING)
			s = "REST_AT_PARKING";
		else if (status == VehicleStatus.TRAVEL_WITHOUT_LOAD)
			s = "TRAVEL_WITHOUT_LOAD";
		else if (status == VehicleStatus.GOING_TO_DELIVERY_PARCEL)
			s = "GOING_TO_DELIVERY_PARCEL";
		else if (status == VehicleStatus.GOING_TO_DELIVERY_PEOPLE)
			s = "GOING_TO_DELIVERY_PEOPEL";
		else if (status == VehicleStatus.GOING_TO_PICKUP_PARCEL)
			s = "GOING_TO_PICKUP_PARCEL";
		else if (status == VehicleStatus.GOING_TO_PICKUP_PEOPLE)
			s = "GOING_TO_PICKUP_PEOPLE";
		else if (status == VehicleStatus.GO_BACK_DEPOT_FINISH_WORK)
			s = "GO_BACK_DEPOT_FINISH_WORK";
		else if (status == VehicleStatus.STOP_WORK)
			s = "STOP_WORK";
		return s;
	}

	public static String getStatusDescription(VehicleStatus status) {
		String s = "-";
		if (status == VehicleStatus.REST_AT_PARKING)
			s = "REST_AT_PARKING";
		else if (status == VehicleStatus.TRAVEL_WITHOUT_LOAD)
			s = "TRAVEL_WITHOUT_LOAD";
		else if (status == VehicleStatus.GOING_TO_DELIVERY_PARCEL)
			s = "GOING_TO_DELIVERY_PARCEL";
		else if (status == VehicleStatus.GOING_TO_DELIVERY_PEOPLE)
			s = "GOING_TO_DELIVERY_PEOPEL";
		else if (status == VehicleStatus.GOING_TO_PICKUP_PARCEL)
			s = "GOING_TO_PICKUP_PARCEL";
		else if (status == VehicleStatus.GOING_TO_PICKUP_PEOPLE)
			s = "GOING_TO_PICKUP_PEOPLE";
		else if (status == VehicleStatus.PICKUP_PEOPLE)
			s = "PICKUP_PEOPLE";
		else if (status == VehicleStatus.DELIVERY_PEOPLE)
			s = "DELIVERY_PEOPLE";
		else if (status == VehicleStatus.PICKUP_PARCEL)
			s = "PICKUP_PARCEL";
		else if (status == VehicleStatus.DELIVERY_PARCEL)
			s = "DELIVERY_PARCEL";
		else if (status == VehicleStatus.GO_BACK_DEPOT_FINISH_WORK)
			s = "GO_BACK_DEPOT_FINISH_WORK";
		else if (status == VehicleStatus.STOP_WORK)
			s = "STOP_WORK";
		else if (status == VehicleStatus.NOT_WORK)
			s = "NOT_WORK";
		else if (status == VehicleStatus.PREPARE_NEW_ITINERARY)
			s = "PREPARE_NEW_ITINERARY";
		else if (status == VehicleStatus.FINISHED_PICKUP_PEOPLE)
			s = "FINISHED_PICKUP_PEOPLE";
		else if (status == VehicleStatus.FINISHED_DELIVERY_PEOPLE)
			s = "FINISHED_DELIVERY_PEOPLE";
		else if (status == VehicleStatus.FINISHED_PICKUP_PARCEL)
			s = "FINISHED_PICKUP_PARCEL";
		else if (status == VehicleStatus.FINISHED_DELIVERY_PARCEL)
			s = "FINISHED_DELIVERY_PARCEL";
		return s;
	}

	public static String getActionDescription(VehicleAction a) {
		String s = "-";
		if (a == VehicleAction.PASS)
			s = "PASS";
		else if (a == VehicleAction.PICKUP_PEOPLE)
			s = "PICKUP_PEOPLE";
		else if (a == VehicleAction.DELIVERY_PEOPLE)
			s = "DELIVERY_PEOPLE";
		else if (a == VehicleAction.STOP)
			s = "STOP";
		else if (a == VehicleAction.PICKUP_PARCEL)
			s = "PICKUP_PARCEL";
		else if (a == VehicleAction.DELIVERY_PARCEL)
			s = "DELIVERY_PARCEL";
		else if (a == VehicleAction.FINISH_WORK)
			s = "FINISH_WORK";
		return s;
	}

	public String getActionDescription() {
		VehicleAction a = VehicleAction.STOP;
		if (currentItinerary != null)
			a = this.currentItinerary.getAction(lastIndexPoint);
		return getActionDescription(a);
	}

	public void addItinerary(ItineraryTravelTime I) {
		LI.add(I);
	}

	public int getLastArrivalTime(){
		if(LI.size() == 0) return -1;
		ItineraryTravelTime I = LI.get(LI.size());
		return I.getArrivalTime(I.size()-1);
	}
	public void writeItinerriesToLog() {
		logI.println(ID);

		/*
		 * If final location is the depot, then set final action = STOP_WORK
		 */
		if (LI != null)
			if (LI.size() > 0) {
				ItineraryTravelTime I = LI.get(LI.size() - 1);
				int depot = sim.mTaxi2Depot.get(ID);
				if (I.size() > 0)
					if (I.get(I.size() - 1) == depot) {
						I.setAction(VehicleAction.FINISH_WORK, I.size() - 1);
					}
			}
		for (int i = 0; i < LI.size(); i++) {
			ItineraryTravelTime I = LI.get(i);
			// logI.println(i + "th itinerary: ");
			int startJ = 0;
			if (i > 0)
				startJ = 1;
			for (int j = startJ; j < I.path.size(); j++) {
				int loc = I.path.get(j);
				int arr_time = -1;
				// if(I.getArrivalTime(j) != null)
				arr_time = I.getArrivalTime(j);
				int dep_time = -1;
				// if(I.getDepartureTime(j) != null)
				dep_time = I.getDepartureTime(j);
				if (j == I.size() - 1 && dep_time == -1 && i < LI.size() - 1) {
					ItineraryTravelTime I1 = LI.get(i + 1);
					dep_time = I1.getDepartureTime(0);
				}
				String stat = "-";
				int request_id = -1;
				// if(mStatus.get(j) != null) stat =
				// getStatusDescription(mStatus.get(j));
				// if(mService.get(j) != null) request_id = mService.get(j);
				stat = getActionDescription(I.getAction(j));
				request_id = I.getRequestID(j);
				// logI.println("taxi[" + ID + "]: " + loc + " " + arr_time +
				// " " + dep_time + " " + stat + " " + request_id);
				logI.println(loc + " " + arr_time + " " + dep_time + " " + stat
						+ " " + request_id);
				// System.out.println("taxi[" + ID + "]: " + loc + " " +
				// arr_time + " " + dep_time + " " + stat + " " + request_id);
			}
		}
		logI.println(-1);
	}

	public String toString() {
		return ID + "," + getStatusDescription() + "," + lastPoint;
	}

	public int findFirstRequestID(int fromIndex){
		for(int i = fromIndex + 1; i < currentItinerary.size(); i++){
			int rid = currentItinerary.getRequestID(i);
			if(rid > 0){
				VehicleAction act = currentItinerary.getAction(i);
				if(act == VehicleAction.DELIVERY_PARCEL || act == VehicleAction.DELIVERY_PEOPLE)
					rid = -rid;
				return rid;
			}
		}
		return 0;
	}
	public void cancelSubItinerary(int fromIdx) {
		if (currentItinerary == null)
			return;
		int idx = fromIdx;
		for (int i = fromIdx; i < currentItinerary.size(); i++) {
			if (mStatus.get(i) != null)
				mStatus.remove(i);
			currentItinerary.arrTime.remove(i);
			currentItinerary.depTime.remove(i);
		}
		while (currentItinerary.size() > idx) {
			currentItinerary.remove(idx);
			currentItinerary.removeAction(idx);
			currentItinerary.removeRequestID(idx);

		}
	}

	public void cancelRemainItinerary() {
		cancelSubItinerary(lastIndexPoint + 1);
		/*
		 * int idx = lastIndexPoint+1; while(currentItinerary.size() > idx){
		 * currentItinerary.remove(idx); currentItinerary.removeAction(idx);
		 * currentItinerary.removeRequestID(idx); }
		 */
	}

	public void setNewItinerary() {
		currentItinerary = nextItinerary;
		lastIndexPoint = 0;
		lastPoint = currentItinerary.get(lastIndexPoint);
		mStatus = mNextStatus;
		status = mStatus.get(lastIndexPoint);

		addItinerary(currentItinerary);

		if (currentItinerary.size() >= 2) {
			if (currentItinerary.get(0) != currentItinerary.get(1)) {
				Arc a = map.getArc(currentItinerary.get(0),
						currentItinerary.get(1));
				remainTimeToNextPoint = Simulator.getTravelTime(a,
						Simulator.maxSpeedms);
				totalTravelDistance += a.w;
			}
		} else {
			remainTimeToNextPoint = 0;
		}
		if (ID == sim.debugTaxiID) {
			log.println(name()
					+ "::setNewItinerary DEBUG, T.currentTimePoint = "
					+ T.currentTimePoint + ", currentItinerary = "
					+ currentItinerary.toString());
		}
		// currentItinerary.setDepartureTime(0, T.currentTimePoint);
	}

	/*
	 * public void move(){ //log.println("Vehicle::move At time point " +
	 * T.currentTimePoint + " --> taxi[" + ID + "] has status = " +
	 * getStatusDescription(status) + //", lastIndexPoint = " + lastIndexPoint +
	 * ", lastPoint = " + lastPoint + ", remaiTimeToNextPoint = " +
	 * remainTimeToNextPoint); if(status == VehicleStatus.PICKUP_PEOPLE){
	 * 
	 * remainTimeToNextDeparture--; if(remainTimeToNextDeparture == 0){
	 * currentItinerary.setDepartureTime(lastIndexPoint, T.currentTimePoint);
	 * //System.out.println("Taxi " + ID + ", setDepartureTime(" +
	 * lastIndexPoint + "," + T.currentTimePoint); status =
	 * VehicleStatus.FINISHED_PICKUP_PEOPLE; }
	 * 
	 * 
	 * return; } if(status == VehicleStatus.DELIVERY_PEOPLE){
	 * remainTimeToNextDeparture--; if(remainTimeToNextDeparture == 0){ //status
	 * = VehicleStatus.GOING_TO_DELIVERY_PEOPEL;//VehicleStatus.DELIVERY_PEOPLE;
	 * currentItinerary.setDepartureTime(lastIndexPoint, T.currentTimePoint);
	 * //System.out.println("Taxi " + ID + ", setDepartureTime(" +
	 * lastIndexPoint + "," + T.currentTimePoint); status =
	 * VehicleStatus.FINISHED_DELIVERY_PEOPLE; } return; } if(status ==
	 * VehicleStatus.PICKUP_PARCEL){ remainTimeToNextDeparture--;
	 * if(remainTimeToNextDeparture == 0){ //status =
	 * VehicleStatus.GOING_TO_DELIVERY_PEOPEL;//VehicleStatus.DELIVERY_PEOPLE;
	 * currentItinerary.setDepartureTime(lastIndexPoint, T.currentTimePoint);
	 * //System.out.println("Taxi " + ID + ", setDepartureTime(" +
	 * lastIndexPoint + "," + T.currentTimePoint); status =
	 * VehicleStatus.FINISHED_PICKUP_PARCEL; } return; } if(status ==
	 * VehicleStatus.DELIVERY_PARCEL){ remainTimeToNextDeparture--;
	 * if(remainTimeToNextDeparture == 0){ //status =
	 * VehicleStatus.GOING_TO_DELIVERY_PEOPEL;//VehicleStatus.DELIVERY_PEOPLE;
	 * currentItinerary.setDepartureTime(lastIndexPoint, T.currentTimePoint);
	 * //System.out.println("Taxi " + ID + ", setDepartureTime(" +
	 * lastIndexPoint + "," + T.currentTimePoint); status =
	 * VehicleStatus.FINISHED_DELIVERY_PARCEL; } return; }
	 * 
	 * if(status == VehicleStatus.REST_AT_PARKING){ return;
	 * 
	 * }
	 * 
	 * 
	 * if(status == VehicleStatus.PREPARE_NEW_ITINERARY){
	 * remainTimeToNextPoint--; if(remainTimeToNextPoint == 0){
	 * lastIndexPoint++; lastPoint = currentItinerary.get(lastIndexPoint);
	 * currentItinerary.setArrivalTime(lastIndexPoint, T.currentTimePoint);
	 * //System.out.println("Taxi " + ID + ", setArrivalTime(" + lastIndexPoint
	 * + "," + T.currentTimePoint); cancelRemainItinerary(); setNewItinerary();
	 * } return; } if(status == VehicleStatus.GO_BACK_DEPOT_FINISH_WORK){
	 * //System.out.println("Vehicle::move, ID = " + ID +
	 * ", status = GO_BACK_DEPOT_FINISH_WORK"); } remainTimeToNextPoint--;
	 * if(remainTimeToNextPoint == 0){ lastIndexPoint++;
	 * 
	 * lastPoint = currentItinerary.get(lastIndexPoint);
	 * currentItinerary.setArrivalTime(lastIndexPoint, T.currentTimePoint);
	 * //System.out.println("Taxi " + ID + ", setArrivalTime(" + lastIndexPoint
	 * + "," + T.currentTimePoint);
	 * 
	 * String actionStr = "NULL"; if(currentItinerary.getAction(lastIndexPoint)
	 * != null){ actionStr =
	 * getActionDescription(currentItinerary.getAction(lastIndexPoint)); }
	 * if(currentItinerary.getAction(lastIndexPoint) == VehicleAction.PASS){
	 * currentItinerary.setDepartureTime(lastIndexPoint, T.currentTimePoint); }
	 * //if(ID == 4){ //System.out.println("Vehicle::move, lastIndexPoint = " +
	 * lastIndexPoint + ", action = " + actionStr); //}
	 * //log.println("At timePoint " + T.currentTimePoint + ": taxi[" + ID +
	 * "] changes lastPoint = " + lastPoint); //log.println("At " +
	 * T.currentTimePointHMS() + ", taxi[" + ID + "] lastIndexPoint = " +
	 * lastIndexPoint + ", lastPoint = " + lastPoint + ", status " +
	 * getStatusDescription(status)); if(currentItinerary.size() >
	 * lastIndexPoint + 1){ int v = currentItinerary.get(lastIndexPoint+1); Arc
	 * a = map.getArc(lastPoint, v); double speed = computeSpeed();//
	 * Simulator.maxSpeedms; //System.out.println("Vehicle::move --> speed = " +
	 * speed + ", maxSpeed = " + sim.maxSpeedms + ", minSpeed = " +
	 * sim.minSpeedms); remainTimeToNextPoint = Simulator.getTravelTime(a,
	 * speed);//Simulator.maxSpeedms);
	 * //currentItinerary.getTravelTime(lastIndexPoint);//a.t;
	 * if(remainTimeToNextPoint == 0) remainTimeToNextPoint = 1;
	 * totalTravelDistance += a.w; //log.println("At " + T.currentTimePoint +
	 * ", remainTimeToNextPoint = " + remainTimeToNextPoint); }else{ //status =
	 * VehicleStatus.REST_AT_PARKING; } if(mStatus.get(lastIndexPoint) != null){
	 * status = mStatus.get(lastIndexPoint); log.println("At " +
	 * T.currentTimePointHMS() + "[" + T.currentTimePoint + "], " + "taxi[" + ID
	 * + "] changes status = " + getStatusDescription(status) + " at point " +
	 * lastPoint);
	 * 
	 * if(currentItinerary.getAction(lastIndexPoint) ==
	 * VehicleAction.FINISH_WORK){ System.out.println("Vehicle::move, ID = " +
	 * ID + ", action = FINISH_WORK, status = " + getStatusDescription(status));
	 * if(status != VehicleStatus.STOP_WORK){
	 * System.out.println("Vehicle::move, ID = " + ID +
	 * ", exception inconsistent, action = FINISH_WORK, but status != STOP_WORK"
	 * ); System.exit(-1); } } if(currentItinerary.getAction(lastIndexPoint) ==
	 * VehicleAction.PICKUP_PEOPLE){ if(status != VehicleStatus.PICKUP_PEOPLE){
	 * System.out.println("Vehicle::move, time = " + T.currentTimePoint +
	 * " --> INCONSISTENT"); System.exit(1); } PeopleRequest pr =
	 * sim.getPeopleRequest(currentItinerary.getRequestID(lastIndexPoint));
	 * remainTimeToNextDeparture = pr.pickupDuration;// 60;// default pickup
	 * time is 60s //if(currentItinerary.getRequestID(lastIndexPoint) == 242){
	 * //System.out.println("Vehicle[" + ID + "]::move --> pickup people " + 242
	 * + " at time point " + T.currentTimePoint); //System.exit(-1); //} }else
	 * if(currentItinerary.getAction(lastIndexPoint) ==
	 * VehicleAction.DELIVERY_PEOPLE){ if(status !=
	 * VehicleStatus.DELIVERY_PEOPLE){
	 * System.out.println("Vehicle::move, time = " + T.currentTimePoint +
	 * " --> INCONSISTENT"); System.exit(1); } PeopleRequest pr =
	 * sim.getPeopleRequest(currentItinerary.getRequestID(lastIndexPoint));
	 * remainTimeToNextDeparture = pr.deliveryDuration;//60;// default delivery
	 * time is 60s }else if(currentItinerary.getAction(lastIndexPoint) ==
	 * VehicleAction.PICKUP_PARCEL){ if(status != VehicleStatus.PICKUP_PARCEL){
	 * System.out.println("Vehicle::move, time = " + T.currentTimePoint +
	 * " --> INCONSISTENT"); System.exit(1); } ParcelRequest pr =
	 * sim.getParcelRequest(currentItinerary.getRequestID(lastIndexPoint));
	 * remainTimeToNextDeparture = pr.pickupDuration;//60;// default pickup time
	 * is 60s }else if(currentItinerary.getAction(lastIndexPoint) ==
	 * VehicleAction.DELIVERY_PARCEL){ if(status !=
	 * VehicleStatus.DELIVERY_PARCEL){
	 * System.out.println("Vehicle::move, time = " + T.currentTimePoint +
	 * " --> INCONSISTENT"); System.exit(1); } ParcelRequest pr =
	 * sim.getParcelRequest(currentItinerary.getRequestID(lastIndexPoint));
	 * remainTimeToNextDeparture = pr.deliveryDuration;//60;// default delivery
	 * time is 60s } }
	 * 
	 * } }
	 */
	public Parking getFinalParking() {
		if (currentItinerary == null)
			return null;
		int locID = currentItinerary.get(currentItinerary.size() - 1);
		//System.out.println(name() + "::getFinalParking, locID = " + locID);
		Parking P = sim.findParking(locID);
		if(P == null){
			System.out.println(name() + "::getFinalParking, locID = " + locID + ", P = NULL -> EXCETION???? System.exit(-1)");
			sim.exit();
		}
		return P;
	}

	public int getNextStartPointIndex() {
		Vehicle taxi = this;
		ItineraryTravelTime I = taxi.currentItinerary;
		if (I == null)
			return 0;

		int nextStartPointIndex = -1;
		if (taxi.status == VehicleStatus.REST_AT_PARKING
				|| taxi.status == VehicleStatus.PICKUP_PARCEL
				|| taxi.status == VehicleStatus.PICKUP_PEOPLE
				|| taxi.status == VehicleStatus.DELIVERY_PARCEL
				|| taxi.status == VehicleStatus.DELIVERY_PEOPLE
				|| taxi.status == VehicleStatus.STOP_WORK) {
			nextStartPointIndex = taxi.lastIndexPoint;
		} else {
			if (taxi.lastIndexPoint < I.size() - 1) {
				nextStartPointIndex = taxi.lastIndexPoint + 1;
			} else {
				System.out
						.println("Vehicle::getNextStartPoint "
								+ "BUG???, taxi.status = "
								+ taxi.getStatusDescription(taxi.status)
								+ " BUT itinerary ended lastIndexPoint = Itinerary.sz = "
								+ I.size());
				log.println("Vehicle::getNextStartPoint "
						+ "BUG???, taxi.status = "
						+ taxi.getStatusDescription(taxi.status)
						+ " BUT itinerary ended lastIndexPoint = Itinerary.sz = "
						+ I.size());
				log.close();
				System.exit(-1);
			}
		}
		return nextStartPointIndex;
	}

	public int getNextStartPoint() {
		Vehicle taxi = this;
		ItineraryTravelTime I = taxi.currentItinerary;
		if (I == null)
			return taxi.lastPoint;

		int nextStartPoint = -1;
		if (taxi.status == VehicleStatus.REST_AT_PARKING
				|| taxi.status == VehicleStatus.PICKUP_PARCEL
				|| taxi.status == VehicleStatus.PICKUP_PEOPLE
				|| taxi.status == VehicleStatus.DELIVERY_PARCEL
				|| taxi.status == VehicleStatus.DELIVERY_PEOPLE
				|| taxi.status == VehicleStatus.STOP_WORK) {
			nextStartPoint = taxi.lastPoint;
		} else {
			if (taxi.lastIndexPoint < I.size() - 1) {
				nextStartPoint = I.get(taxi.lastIndexPoint + 1);
			} else {
				System.out
						.println("Vehicle::getNextStartPoint "
								+ "BUG???, taxi.status = "
								+ taxi.getStatusDescription(taxi.status)
								+ " BUT itinerary ended lastIndexPoint = Itinerary.sz = "
								+ I.size());
				log.println("Vehicle::getNextStartPoint "
						+ "BUG???, taxi.status = "
						+ taxi.getStatusDescription(taxi.status)
						+ " BUT itinerary ended lastIndexPoint = Itinerary.sz = "
						+ I.size());
				log.close();
				System.exit(-1);
			}
		}
		return nextStartPoint;
	}

	public int getLastIndexPeopleDeliveryItinerary(int fromIndex){
		int idx = -1;
		for(int i = remainRequestIDs.size()-1; i >= 0; i--){
			int rid = remainRequestIDs.get(i);
			if(rid < 0){
				PeopleRequest req= sim.mPeopleRequest.get(-rid);
				if(req != null){
					idx = mRequestID2Index.get(rid);
					if(idx >= fromIndex) return idx;
					break;
				}
			}
		}
		return -1;
	}
	
	public int findLastIndexPeopleDeliveryItinerary(int fromIndex){
		int idx = -1;
		for(int i = fromIndex; i < currentItinerary.size(); i++){
			int rid = currentItinerary.getRequestID(i);
			if(rid > 0 && currentItinerary.getAction(i) == VehicleAction.DELIVERY_PEOPLE){
				idx = i;
				//PeopleRequest pr = sim.mPeopleRequest.get(rid);
				//if(pr != null) idx = i;
			}
		}
		return idx;
	}
	public int findLastIndexPeopleDeliveryRemainRequetsIDs(){
		for(int i =  remainRequestIDs.size()-1; i >= 0; i--){
			int rid = remainRequestIDs.get(i);
			if(ID == sim.debugTaxiID){
				sim.log.println(name() + "::findLastIndexPeopleDeliveryRemainRequetsIDs, rid = " + rid);
			}
			if(rid < 0){
				rid = -rid;
				PeopleRequest pr = sim.mPeopleRequest.get(rid);
				if(pr != null){
					return i;
				}
			}
		}
		return -1;
	}
	public TimePointIndex getNextTimePointIndex(int fromIndexPoint,
			int currentTime, int dt) {
		// normally, when called, fromIndexPoint = lastPointIndex, currentTime =
		// T.currentTimePoint
		int idxReqID = -1;
		int nextStartTimePoint = currentTime + dt;
		int nextPoint = lastPoint;
		int nextPointIndex = lastIndexPoint;
		int taxiID = sim.debugTaxiID;
		ItineraryTravelTime I = currentItinerary;
		//if (I == null) {
		if(I.size() == 0){
			if (ID == sim.debugTaxiID)
				sim.log.println(name() + "::getNextStartTimePoint, DEBUG taxi "
						+ taxiID + ", currentItinerary = null, currentTime = "
						+ currentTime + ", dt = " + dt
						+ ", returned nexStartTimePoint = "
						+ nextStartTimePoint);
			return new TimePointIndex(nextStartTimePoint, lastPoint,
					lastIndexPoint);
		}
		if (ID == taxiID) {
			sim.log.println(name() + "::getNextStartTimePoint, DEBUG taxi "
					+ taxiID + " status = " + getStatusDescription()
					+ ", lastIndexPoint = " + lastIndexPoint + ", lastPoint = "
					+ lastPoint + ", sz = " + I.size() + ", currentTime = "
					+ currentTime + ", dt = " + dt + ", fromIndexPoint = "
					+ fromIndexPoint + ", I.sz = " + I.size()
					+ ", departureTimeLastIndexPoint = "
					+ I.getDepartureTime(lastIndexPoint) + ", remainRequestIDs = " + Utility.arr2String(remainRequestIDs));// + ", I = " +
															// I.toString());
		}
		//if (status == VehicleStatus.REST_AT_PARKING
		//		|| status == VehicleStatus.STOP_WORK) {
		/*
		if(I.size() == 0){
			nextStartTimePoint = I.getDepartureTime(lastIndexPoint);
			if (ID == taxiID) {
				log.println(name()
						+ "::getNextStartTimePoint, status = REST_AT_PARKING, "
						+ "lastIndexPoint = " + lastIndexPoint
						+ ", nextStartTimePoint = " + nextStartTimePoint);

			}
			if (nextStartTimePoint < 0)// not predefined yet
				nextStartTimePoint = currentTime + dt;

		} else {
		*/	
			boolean ok = false;

			//int lastIndexPeopleDelivery = findLastIndexPeopleDelivery();
			//fromIndexPoint = fromIndexPoint > lastIndexPeopleDelivery ? fromIndexPoint : lastIndexPeopleDelivery;// skip index point until reaching last people delivery
			
			for (int i = fromIndexPoint; i < I.size(); i++) {
				int rid = I.getRequestID(i);
				if (ID == sim.debugTaxiID) {
					sim.log.println(name()
							+ "::getNextIndexTimePoint, fromIndexPoint = " + fromIndexPoint + ", i = " + i + ", rid = " + rid);
				}
				if (rid > 0) {
					int rrid = rid;
					if (I.getAction(i) == VehicleAction.DELIVERY_PARCEL
							|| I.getAction(i) == VehicleAction.DELIVERY_PEOPLE)
						rrid = -rid;
					int i1 = remainRequestIDs.indexOf(rrid);
					if (ID == sim.debugTaxiID) {
						sim.log.println(name() + "::getNextIndexTimePoint, scan i = " + i + ", rid = " + rid + ", rrid = " + rrid + 
								", action = " + getActionDescription(I.getAction(i)) + ", i1 = " + i1 + ", remainRequestIDs = " + 
								Utility.arr2String(remainRequestIDs));
					}
					if (i1 >= 0) {// reach a service point
						// there are cases in which rid > 0 but it is not
						// included in remainRequestIDs
						// (e.g., fromPointIndex is a service point, then rid >
						// 0 and has just been removed from remainRequestIDs)
						idxReqID++;	//[SonNV] number of PICKUP or DELIVERY point in I itinerary which is passed in decision time.
									//= index of last point in remainRequestIDs which is passed in dt.
						if (ID == sim.debugTaxiID) {
							sim.log.println(name()
									+ "::getNextIndexTimePoint, rid (> 0) = "
									+ rid + ", i = " + i
									+ ", fromIndexPoint = " + fromIndexPoint
									+ ", i1 = " + i1 + ", remainRequestIDs = "
									+ Utility.arr2String(remainRequestIDs)
									+ ", --> idxReqID++ = " + idxReqID);
						}

					}
				}
				if (I.getDepartureTime(i) >= currentTime + dt) {
					nextStartTimePoint = I.getDepartureTime(i);
					nextPoint = I.get(i);
					nextPointIndex = i;
					ok = true;
					break;
				}
			}
			
			if (!ok) {// i reach final index of I. If I.getArrivalTime(I.size()-1) > currentTime+dt -> take If I.getArrivalTime(I.size()-1)
				nextStartTimePoint = currentTime + dt;
				nextStartTimePoint = nextStartTimePoint > I.getArrivalTime(I.size()-1) ? nextStartTimePoint : I.getArrivalTime(I.size()-1);
				nextPoint = I.get(I.size() - 1);
				nextPointIndex = I.size() - 1;
			}
		//}

		TimePointIndex tpi = new TimePointIndex(nextStartTimePoint, nextPoint,
				nextPointIndex);
		tpi.indexRemainRequestIDs = idxReqID;
		return tpi;
	}

	public int getNextStartTimePoint() {
		Vehicle taxi = this;
		int startTimePoint = -1;// T.currentTimePoint + 1;
		ItineraryTravelTime I = taxi.currentItinerary;
		if (I == null)
			return T.currentTimePoint + 1;
		if (ID == 47) {
			log.println(name()
					+ "::getNextStartTimePoint, DEBUG taxi 47 status = "
					+ getStatusDescription() + ", lastIndexPoint = "
					+ lastIndexPoint + ", lastPoint = " + lastPoint + ", sz = "
					+ I.size() + ", T.current = " + T.currentTimePoint
					+ ", departureTimeLastIndexPoint = "
					+ I.getDepartureTime(lastIndexPoint));
		}
		if (taxi.status == VehicleStatus.REST_AT_PARKING
				|| taxi.status == VehicleStatus.STOP_WORK) {
			startTimePoint = I.getDepartureTime(taxi.lastIndexPoint);
			if (startTimePoint < 0)// not predefined yet
				startTimePoint = T.currentTimePoint + 1;
		} else if (taxi.status == VehicleStatus.PICKUP_PARCEL
				|| taxi.status == VehicleStatus.PICKUP_PEOPLE
				|| taxi.status == VehicleStatus.DELIVERY_PARCEL
				|| taxi.status == VehicleStatus.DELIVERY_PEOPLE) {
			startTimePoint = I.getDepartureTime(taxi.lastIndexPoint);
			if (startTimePoint < 0)// not predefined yet
				startTimePoint = T.currentTimePoint + 1;

			if (ID == 47)
				log.println(name()
						+ "::getNextStartTimePoint, DEBUG taxi 47 in SERVICE status = "
						+ getStatusDescription() + ", lastIndexPoint = "
						+ lastIndexPoint + ", sz = " + I.size()
						+ ", T.current = " + T.currentTimePoint
						+ ", departureTimeLastIndexPoint = "
						+ I.getDepartureTime(lastIndexPoint));
		} else {
			if (taxi.lastIndexPoint < I.size() - 1) {// travelling
				// startTimePoint = I.getArrivalTime(taxi.lastIndexPoint+1);
				startTimePoint = I.getDepartureTime(taxi.lastIndexPoint + 1);
				if (ID == 47) {
					log.println(name()
							+ "::getNextStartTimePoint, DEBUG taxi 47 TRAVELING status = "
							+ getStatusDescription() + ", lastIndexPoint = "
							+ lastIndexPoint + ", sz = " + I.size()
							+ ", T.current = " + T.currentTimePoint
							+ ", startTimePoint = " + startTimePoint);
				}
			} else {
				System.out
						.println("Vehicle::getNextStartTimePoint "
								+ "BUG???, taxi.status = "
								+ taxi.getStatusDescription(taxi.status)
								+ " BUT itinerary ended lastIndexPoint = Itinerary.sz = "
								+ I.size());
				log.println("Vehicle::getNextStartTimePoint "
						+ "BUG???, taxi.status = "
						+ taxi.getStatusDescription(taxi.status)
						+ " BUT itinerary ended lastIndexPoint = Itinerary.sz = "
						+ I.size());
				log.close();
				System.exit(-1);
			}
		}
		return startTimePoint;
	}

	public String getRemainRequestID() {
		String s = "";
		for (int i = 0; i < remainRequestIDs.size(); i++)
			s = s + remainRequestIDs.get(i) + ",";
		return s + ", size = " + remainRequestIDs.size();
	}

	public String requestStatus() {
		String s = "";
		s = s + "STATUS[PeopleRequestOnBoards = "
				+ Utility.arr2String(peopleReqIDonBoard)
				+ ", ParcelOnBoards = "
				+ Utility.arr2String(parcelReqIDonBoard)
				+ ", RemainrequestIDs = "
				+ Utility.arr2String(remainRequestIDs) + "]";
		return s;
	}

	public String getRemainRequestID(ArrayList<Integer> L) {
		String s = "";
		for (int i = 0; i < L.size(); i++)
			s = s + L.get(i) + ",";
		return s + ", size = " + L.size();
	}
	public boolean hasPeopleTobePickuped(){
		boolean ok = false;
		for(int i = 0; i < remainRequestIDs.size(); i++){
			int r = remainRequestIDs.get(i);
			if(r > 0){
				PeopleRequest pr = sim.mPeopleRequest.get(r);
				if(pr != null) return true;
			}
		}
		return ok;
	}
	public VehicleStatus changeStatus(int fromIdx) {
		for (int i = fromIdx; i < currentItinerary.size(); i++) {
			VehicleAction a = currentItinerary.getAction(i);
			if (a == VehicleAction.PICKUP_PARCEL)
				status = VehicleStatus.GOING_TO_PICKUP_PARCEL;
			else if (a == VehicleAction.DELIVERY_PARCEL)
				status = VehicleStatus.GOING_TO_DELIVERY_PARCEL;
			else if (a == VehicleAction.PICKUP_PEOPLE)
				status = VehicleStatus.GOING_TO_PICKUP_PEOPLE;
			else if (a == VehicleAction.DELIVERY_PEOPLE)
				status = VehicleStatus.GOING_TO_DELIVERY_PEOPLE;
			else if (a == VehicleAction.FINISH_WORK)
				status = VehicleStatus.GO_BACK_DEPOT_FINISH_WORK;
		}
		return status;
	}

	/*
	 * public void move(){ //log.println("Vehicle::move At time point " +
	 * T.currentTimePoint + " --> taxi[" + ID + "] has status = " +
	 * getStatusDescription(status) + //", lastIndexPoint = " + lastIndexPoint +
	 * ", lastPoint = " + lastPoint + ", remaiTimeToNextPoint = " +
	 * remainTimeToNextPoint); int taxiID = -1; if(status ==
	 * VehicleStatus.PICKUP_PEOPLE){ if(T.currentTimePoint ==
	 * currentItinerary.getDepartureTime(lastIndexPoint)){ status =
	 * VehicleStatus.FINISHED_PICKUP_PEOPLE; if(lastIndexPoint <
	 * currentItinerary.size()-1) if(T.currentTimePoint ==
	 * currentItinerary.getArrivalTime(lastIndexPoint+1)){ lastIndexPoint++;
	 * lastPoint = currentItinerary.get(lastIndexPoint);
	 * if(mStatus.get(lastIndexPoint) != null){ status =
	 * mStatus.get(lastIndexPoint); } } }
	 * 
	 * 
	 * return; } if(status == VehicleStatus.DELIVERY_PEOPLE){
	 * if(T.currentTimePoint ==
	 * currentItinerary.getDepartureTime(lastIndexPoint)){ status =
	 * VehicleStatus.FINISHED_DELIVERY_PEOPLE;
	 * 
	 * if(lastIndexPoint < currentItinerary.size()-1) if(T.currentTimePoint ==
	 * currentItinerary.getArrivalTime(lastIndexPoint+1)){ lastIndexPoint++;
	 * lastPoint = currentItinerary.get(lastIndexPoint);
	 * if(mStatus.get(lastIndexPoint) != null){ status =
	 * mStatus.get(lastIndexPoint); } } }
	 * 
	 * return; } if(status == VehicleStatus.PICKUP_PARCEL){
	 * if(T.currentTimePoint ==
	 * currentItinerary.getDepartureTime(lastIndexPoint)){ status =
	 * VehicleStatus.FINISHED_PICKUP_PARCEL;
	 * 
	 * if(lastIndexPoint < currentItinerary.size()-1) if(T.currentTimePoint ==
	 * currentItinerary.getArrivalTime(lastIndexPoint+1)){ lastIndexPoint++;
	 * lastPoint = currentItinerary.get(lastIndexPoint);
	 * if(mStatus.get(lastIndexPoint) != null){ status =
	 * mStatus.get(lastIndexPoint); } } }
	 * 
	 * return; } if(status == VehicleStatus.DELIVERY_PARCEL){
	 * if(T.currentTimePoint ==
	 * currentItinerary.getDepartureTime(lastIndexPoint)){ status =
	 * VehicleStatus.FINISHED_DELIVERY_PARCEL;
	 * 
	 * if(lastIndexPoint < currentItinerary.size()-1) if(T.currentTimePoint ==
	 * currentItinerary.getArrivalTime(lastIndexPoint+1)){ lastIndexPoint++;
	 * lastPoint = currentItinerary.get(lastIndexPoint);
	 * if(mStatus.get(lastIndexPoint) != null){ status =
	 * mStatus.get(lastIndexPoint); } } }
	 * 
	 * return; }
	 * 
	 * // update data structure when we go to the next time point
	 * 
	 * 
	 * if(status == VehicleStatus.REST_AT_PARKING){
	 * 
	 * if(currentItinerary != null) if(T.currentTimePoint ==
	 * currentItinerary.getDepartureTime(lastIndexPoint)){ status =
	 * changeStatus(lastIndexPoint); } return;
	 * 
	 * }
	 * 
	 * 
	 * if(status == VehicleStatus.PREPARE_NEW_ITINERARY){ if(T.currentTimePoint
	 * == currentItinerary.getArrivalTime(lastIndexPoint+1)){
	 * cancelRemainItinerary(); setNewItinerary(); }
	 * 
	 * return; } if(status == VehicleStatus.GO_BACK_DEPOT_FINISH_WORK){
	 * //System.out.println("Vehicle::move, ID = " + ID +
	 * ", status = GO_BACK_DEPOT_FINISH_WORK"); } remainTimeToNextPoint--;
	 * //if(remainTimeToNextPoint == 0){ //if(T.currentTimePoint ==
	 * currentItinerary.getArrivalTime(lastIndexPoint+1)){ if(ID ==
	 * taxiID)log.println("Vehicle[" + ID + "]::move, T.current = " +
	 * T.currentTimePoint + ", lastIndexPoint = " + lastIndexPoint +
	 * ", lastPoint = " + lastPoint + ", getArrivalTime(lastIndexPoint+1) = " +
	 * currentItinerary.getArrivalTime(lastIndexPoint+1) + ", status = " +
	 * getStatusDescription(status));
	 * 
	 * while(T.currentTimePoint ==
	 * currentItinerary.getArrivalTime(lastIndexPoint+1)){ int nextPoint =
	 * currentItinerary.get(lastIndexPoint+1); Arc a0 = map.getArc(lastPoint,
	 * nextPoint); if(a0 != null){ remainDistance = remainDistance - a0.w; }
	 * 
	 * int rid = currentItinerary.getRequestID(lastIndexPoint+1); VehicleAction
	 * act = currentItinerary.getAction(lastIndexPoint+1); if(rid > 0){
	 * remainRequestIDs.remove(0); if(act == VehicleAction.PICKUP_PARCEL){
	 * parcelReqIDonBoard.add(rid); }else if(act ==
	 * VehicleAction.DELIVERY_PARCEL){ int idx =
	 * parcelReqIDonBoard.indexOf(rid); parcelReqIDonBoard.remove(idx); }else
	 * if(act == VehicleAction.PICKUP_PEOPLE){ peopleReqIDonBoard.add(rid);
	 * sim.countStop.put(rid, 0); sim.accumulateDistance.put(rid, 0.0); }else
	 * if(act == VehicleAction.DELIVERY_PEOPLE){ int idx =
	 * peopleReqIDonBoard.indexOf(rid); peopleReqIDonBoard.remove(idx); }else{
	 * System.out.println("Vehicle::move EXCEPTION unknown action?????");
	 * System.exit(-1); }
	 * 
	 * // update stops of people requests for(int i = 0; i <
	 * peopleReqIDonBoard.size(); i++){ int r = peopleReqIDonBoard.get(i); if(r
	 * != rid){ sim.countStop.put(r, sim.countStop.get(r) + 1);
	 * 
	 * if(a0 != null) sim.accumulateDistance.put(r,
	 * sim.accumulateDistance.get(r) + a0.w); } } }
	 * 
	 * 
	 * lastIndexPoint++;
	 * 
	 * lastPoint = currentItinerary.get(lastIndexPoint); if(ID ==
	 * taxiID)log.println("Vehicle[" + ID + "]::move, REACH T.current = " +
	 * T.currentTimePoint + ", lastIndexPoint = " + lastIndexPoint +
	 * ", lastPoint = " + lastPoint + ", getArrivalTime(lastIndexPoint+1) = " +
	 * currentItinerary.getArrivalTime(lastIndexPoint+1) + ", status = " +
	 * getStatusDescription(status));
	 * 
	 * //currentItinerary.setArrivalTime(lastIndexPoint, T.currentTimePoint);
	 * //System.out.println("Taxi " + ID + ", setArrivalTime(" + lastIndexPoint
	 * + "," + T.currentTimePoint);
	 * 
	 * String actionStr = "NULL"; if(currentItinerary.getAction(lastIndexPoint)
	 * != null){ actionStr =
	 * getActionDescription(currentItinerary.getAction(lastIndexPoint)); }
	 * if(currentItinerary.getAction(lastIndexPoint) == VehicleAction.PASS){
	 * //currentItinerary.setDepartureTime(lastIndexPoint, T.currentTimePoint);
	 * } //if(ID == 4){ //System.out.println("Vehicle::move, lastIndexPoint = "
	 * + lastIndexPoint + ", action = " + actionStr); //}
	 * //log.println("At timePoint " + T.currentTimePoint + ": taxi[" + ID +
	 * "] changes lastPoint = " + lastPoint); //log.println("At " +
	 * T.currentTimePointHMS() + ", taxi[" + ID + "] lastIndexPoint = " +
	 * lastIndexPoint + ", lastPoint = " + lastPoint + ", status " +
	 * getStatusDescription(status)); if(currentItinerary.size() >
	 * lastIndexPoint + 1){ int v = currentItinerary.get(lastIndexPoint+1); Arc
	 * a = map.getArc(lastPoint, v);
	 * 
	 * if(a != null) totalTravelDistance += a.w; else{
	 * System.out.println("Vehicle::move, Arc (" + lastPoint + "," + v +
	 * ") does not exists ????????????????"); log.println("Vehicle[" + ID +
	 * "]::move, Arc (" + lastPoint + "," + v +
	 * ") does not exists ????????????????"); } //log.println("At " +
	 * T.currentTimePoint + ", remainTimeToNextPoint = " +
	 * remainTimeToNextPoint); }else{ //status = VehicleStatus.REST_AT_PARKING;
	 * } if(mStatus.get(lastIndexPoint) != null){ status =
	 * mStatus.get(lastIndexPoint); if(status == VehicleStatus.REST_AT_PARKING){
	 * System.out.println("Vehicle::move, At " + T.currentTimePointHMS() + "[" +
	 * T.currentTimePoint + "], " + "taxi[" + ID + "] REST AT PARKING"); }
	 * log.println("Vehicle::move, At " + T.currentTimePointHMS() + "[" +
	 * T.currentTimePoint + "], " + "taxi[" + ID + "] changes status = " +
	 * getStatusDescription(status) + " at point " + lastPoint);
	 * 
	 * if(currentItinerary.getAction(lastIndexPoint) ==
	 * VehicleAction.FINISH_WORK){ System.out.println("Vehicle::move, ID = " +
	 * ID + ", action = FINISH_WORK, status = " + getStatusDescription(status));
	 * if(status != VehicleStatus.STOP_WORK){
	 * System.out.println("Vehicle::move, ID = " + ID +
	 * ", exception inconsistent, action = FINISH_WORK, but status != STOP_WORK"
	 * ); System.exit(-1); } } if(currentItinerary.getAction(lastIndexPoint) ==
	 * VehicleAction.PICKUP_PEOPLE){ if(status != VehicleStatus.PICKUP_PEOPLE){
	 * System.out.println("Vehicle::move, time = " + T.currentTimePoint +
	 * " --> INCONSISTENT"); System.exit(1); } PeopleRequest pr =
	 * sim.getPeopleRequest(currentItinerary.getRequestID(lastIndexPoint));
	 * remainTimeToNextDeparture = pr.pickupDuration;// 60;// default pickup
	 * time is 60s //if(currentItinerary.getRequestID(lastIndexPoint) == 242){
	 * //System.out.println("Vehicle[" + ID + "]::move --> pickup people " + 242
	 * + " at time point " + T.currentTimePoint); //System.exit(-1); //} }else
	 * if(currentItinerary.getAction(lastIndexPoint) ==
	 * VehicleAction.DELIVERY_PEOPLE){ if(status !=
	 * VehicleStatus.DELIVERY_PEOPLE){
	 * System.out.println("Vehicle::move, time = " + T.currentTimePoint +
	 * " --> INCONSISTENT"); System.exit(1); } PeopleRequest pr =
	 * sim.getPeopleRequest(currentItinerary.getRequestID(lastIndexPoint));
	 * remainTimeToNextDeparture = pr.deliveryDuration;//60;// default delivery
	 * time is 60s }else if(currentItinerary.getAction(lastIndexPoint) ==
	 * VehicleAction.PICKUP_PARCEL){ if(status != VehicleStatus.PICKUP_PARCEL){
	 * System.out.println("Vehicle::move, time = " + T.currentTimePoint +
	 * " --> INCONSISTENT"); System.exit(1); }
	 * System.out.println("Vehicle::move, lastIndexPoint = " +
	 * currentItinerary.getRequestID(lastIndexPoint) + ", nextItinerary = " +
	 * nextItinerary2String()); ParcelRequest pr =
	 * sim.getParcelRequest(currentItinerary.getRequestID(lastIndexPoint));
	 * remainTimeToNextDeparture = pr.pickupDuration;//60;// default pickup time
	 * is 60s }else if(currentItinerary.getAction(lastIndexPoint) ==
	 * VehicleAction.DELIVERY_PARCEL){ if(status !=
	 * VehicleStatus.DELIVERY_PARCEL){
	 * System.out.println("Vehicle::move, time = " + T.currentTimePoint +
	 * " --> INCONSISTENT"); System.exit(1); } ParcelRequest pr =
	 * sim.getParcelRequest(currentItinerary.getRequestID(lastIndexPoint));
	 * remainTimeToNextDeparture = pr.deliveryDuration;//60;// default delivery
	 * time is 60s } }
	 * 
	 * } }
	 */
	public double computeSpeed() {

		double dis = 0;
		int reqID = -1;
		int time = -1;
		// int taxiID = -1;
		if (ID == sim.debugTaxiID)
			log.println("Vehicle[" + ID + "]::computeSpeed, lastIndexPoint = "
					+ lastIndexPoint);
		for (int ii = lastIndexPoint; ii < currentItinerary.size(); ii++) {
			if (currentItinerary.getAction(ii) == VehicleAction.PICKUP_PARCEL
					&& ii > lastIndexPoint) {

				reqID = currentItinerary.getRequestID(ii);
				ParcelRequest pr = sim.getParcelRequest(reqID);
				int t = T.currentTimePoint + (int) (dis / sim.maxSpeedms);
				if (ID == sim.debugTaxiID)
					log.println("Vehicle[" + ID
							+ "]::computeSpeed, T.current = "
							+ T.currentTimePoint + ", pickup parcel " + pr.id
							+ ", pr.earlyPickup = " + pr.earlyPickupTime
							+ ", pr.latePickup = " + pr.latePickupTime
							+ ", pr.pickupDuration = " + pr.pickupDuration
							+ ", t = " + t);

				if (t + pr.pickupDuration >= pr.earlyPickupTime
						&& t + pr.pickupDuration <= pr.latePickupTime) {
					if (ID == sim.debugTaxiID)
						log.println("Vehicle[" + ID + "]::computeSpeed, t = "
								+ t + ", parcelRequest " + pr.id
								+ ", pr.pickupDuration = " + pr.pickupDuration
								+ ", pr.earlyPickupTime = "
								+ pr.earlyPickupTime + ", pr.latePickupTime = "
								+ pr.latePickupTime
								+ ", --> return maxSpeedms = " + sim.maxSpeedms);
					return sim.maxSpeedms;
				} else if (t + pr.pickupDuration < pr.earlyPickupTime) {
					double speed = dis
							/ (pr.earlyPickupTime - pr.pickupDuration - T.currentTimePoint);
					if (ID == sim.debugTaxiID)
						log.println("Vehicle[" + ID + "]::computeSpeed, t = "
								+ t + ", parcelRequest " + pr.id
								+ ", pr.pickupDuration = " + pr.pickupDuration
								+ ", < pr.earlyPickupTime, dis = " + dis
								+ ", --> return speed = " + speed);
					return speed;
				} else {
					if (pr.latePickupTime - pr.pickupDuration / 2
							- T.currentTimePoint <= 0) {
						System.out.println("Vehicle[" + ID
								+ "]::computeSpeed, BUG???? " + "T.current = "
								+ T.currentTimePoint + ", pr.latePickupTime = "
								+ pr.latePickupTime + ", pr.pickupDuration = "
								+ pr.pickupDuration + ", distance = " + dis);
						if (ID == sim.debugTaxiID)
							log.println("Vehicle[" + ID
									+ "]::computeSpeed, ii = " + ii
									+ ", BUG???? " + "T.current = "
									+ T.currentTimePoint
									+ ", pr.latePickupTime = "
									+ pr.latePickupTime
									+ ", pr.pickupDuration = "
									+ pr.pickupDuration + ", distance = " + dis);
						log.close();
						System.exit(-1);
					}
					double speed = dis
							/ (pr.latePickupTime - pr.pickupDuration / 2 - T.currentTimePoint);
					if (ID == sim.debugTaxiID)
						log.println("Vehicle[" + ID
								+ "]::computeSpeed, parcelRequest " + pr.id
								+ ", pr.latePickupTime = " + pr.latePickupTime
								+ " - pr.pickupDuration = " + pr.pickupDuration
								+ " > T.current =  " + T.currentTimePoint
								+ ", dis = " + dis + " --> return speed =  "
								+ speed);
					return speed;
				}
				/*
				 * time = (pr.latePickupTime + pr.earlyPickupTime)/2 -
				 * pr.pickupDuration - T.currentTimePoint; if(time <= 0) time =
				 * pr.latePickupTime - pr.pickupDuration - T.currentTimePoint;
				 * //
				 * System.out.println("Vehicle::computeSpeed, peopleRequest = "
				 * + reqID + ", pr.latePickupTime = " // + pr.latePickupTime +
				 * ", pr.pickupDuration = " + pr.pickupDuration +
				 * ", currentTime = " + T.currentTimePoint + ", time = " + time
				 * + ", dis = " + dis); if(time <= 0 && dis > 0){
				 * System.out.println("Vehicle::computeSpeed, time = " + time +
				 * " < 0 --> BUG???????"); System.exit(-1); }
				 */
				// break;
			} else if (currentItinerary.getAction(ii) == VehicleAction.PICKUP_PEOPLE
					&& ii > lastIndexPoint) {
				reqID = currentItinerary.getRequestID(ii);
				PeopleRequest pr = sim.getPeopleRequest(reqID);
				int t = T.currentTimePoint + (int) (dis / sim.maxSpeedms);
				if (ID == sim.debugTaxiID)
					log.println("Vehicle[" + ID
							+ "]::computeSpeed, T.current = "
							+ T.currentTimePoint + ", pickup people " + pr.id
							+ ", pr.earlyPickup = " + pr.earlyPickupTime
							+ ", pr.latePickup = " + pr.latePickupTime
							+ ", pr.pickupDuration = " + pr.pickupDuration
							+ ", t = " + t);

				if (t + pr.pickupDuration >= pr.earlyPickupTime
						&& t + pr.pickupDuration <= pr.latePickupTime) {
					if (ID == sim.debugTaxiID)
						log.println("Vehicle[" + ID + "]::computeSpeed, t = "
								+ t + ", people Request " + pr.id
								+ ", pr.pickupDuration = " + pr.pickupDuration
								+ ", pr.earlyPickupTime = "
								+ pr.earlyPickupTime + ", pr.latePickupTime = "
								+ pr.latePickupTime
								+ ", --> return maxSpeedms = " + sim.maxSpeedms);

					return sim.maxSpeedms;
				} else if (t + pr.pickupDuration < pr.earlyPickupTime) {
					double speed = dis
							/ (pr.earlyPickupTime - pr.pickupDuration - T.currentTimePoint);
					if (ID == sim.debugTaxiID)
						log.println("Vehicle[" + ID + "]::computeSpeed, t = "
								+ t + ", people Request " + pr.id
								+ ", pr.pickupDuration = " + pr.pickupDuration
								+ ", < pr.earlyPickupTime, dis = " + dis
								+ " --> return speed = " + speed);

					return speed;
				} else {
					if (pr.latePickupTime - pr.pickupDuration / 2
							- T.currentTimePoint <= 0) {
						System.out.println("Vehicle[" + ID
								+ "]::computeSpeed, BUG???? " + "T.current = "
								+ T.currentTimePoint + ", pr.latePickupTime = "
								+ pr.latePickupTime + ", pr.pickupDuration = "
								+ pr.pickupDuration + ", distance = " + dis);
						if (ID == sim.debugTaxiID)
							log.println("Vehicle[" + ID
									+ "]::computeSpeed, ii = " + ii
									+ ", BUG???? " + "T.current = "
									+ T.currentTimePoint
									+ ", pr.latePickupTime = "
									+ pr.latePickupTime
									+ ", pr.pickupDuration = "
									+ pr.pickupDuration + ", distance = " + dis);
						log.close();
						System.exit(-1);
					}
					double speed = dis
							/ (pr.latePickupTime - pr.pickupDuration / 2 - T.currentTimePoint);
					if (ID == sim.debugTaxiID)
						log.println("Vehicle[" + ID
								+ "]::computeSpeed, people Request " + pr.id
								+ ", pr.latePickupTime = " + pr.latePickupTime
								+ " - pr.pickupDuration = " + pr.pickupDuration
								+ " > T.current =  " + T.currentTimePoint
								+ ", dis = " + dis + " --> return speed =  "
								+ speed);
					return speed;
				}
				// System.out.println("Vehicle::computeSpeed, peopleRequest = "
				// + reqID + ", pr.pickupTime = "
				// + pr.earlyPickupTime + ", pr.pickupDuration = " +
				// pr.pickupDuration + ", currentTime = " + T.currentTimePoint);
				/*
				 * time = (pr.latePickupTime + pr.earlyPickupTime)/2 -
				 * pr.pickupDuration - T.currentTimePoint; if(time <= 0) time =
				 * pr.latePickupTime - pr.pickupDuration - T.currentTimePoint;
				 * //
				 * System.out.println("Vehicle::computeSpeed, peopleRequest = "
				 * + reqID + ", pr.latePickupTime = " // + pr.latePickupTime +
				 * ", pr.pickupDuration = " + pr.pickupDuration +
				 * ", currentTime = " + T.currentTimePoint + ", time = " + time
				 * + ", dis = " + dis); if(time <= 0 && dis > 0){
				 * System.out.println("Vehicle::computeSpeed, time = " + time +
				 * " <= 0 --> BUG???????"); System.exit(-1); }
				 */

				// break;
			} else if (currentItinerary.getAction(ii) == VehicleAction.DELIVERY_PEOPLE
					&& ii > lastIndexPoint) {
				reqID = currentItinerary.getRequestID(ii);
				PeopleRequest pr = sim.getPeopleRequest(reqID);
				int t = T.currentTimePoint + (int) (dis / sim.maxSpeedms);

				if (ID == sim.debugTaxiID)
					log.println("Vehicle[" + ID
							+ "]::computeSpeed, T.current = "
							+ T.currentTimePoint + ", delivery people " + pr.id
							+ ", pr.earlyDelivery = " + pr.earlyDeliveryTime
							+ ", pr.lateDelivery = " + pr.lateDeliveryTime
							+ ", pr.deliveryDuration = " + pr.deliveryDuration
							+ ", t = " + t);

				if (t >= pr.earlyDeliveryTime && t <= pr.lateDeliveryTime) {
					if (ID == sim.debugTaxiID)
						log.println("Vehicle[" + ID
								+ "]::computeSpeed, peopleRequest " + pr.id
								+ ", t = " + t + ", pr.earlyDeliveryTime = "
								+ pr.earlyDeliveryTime
								+ ", pr.lateDeliveryTime = "
								+ pr.lateDeliveryTime
								+ " --> return maxSpeed = " + sim.maxSpeedms);
					return sim.maxSpeedms;
				} else if (t < pr.earlyDeliveryTime) {
					double speed = dis
							/ (pr.earlyPickupTime - T.currentTimePoint);
					if (ID == sim.debugTaxiID)
						log.println("Vehicle[" + ID
								+ "]::computeSpeed, people request " + pr.id
								+ ", t = " + t + " < pr.earlyDeliveryTime = "
								+ pr.earlyDeliveryTime + ", dis = " + dis
								+ " --> return  speed = " + speed);
					return speed;
				} else {
					if (pr.lateDeliveryTime - T.currentTimePoint <= 0) {
						System.out.println("Vehicle[" + ID
								+ "]::computeSpeed, BUG???? " + "T.current = "
								+ T.currentTimePoint
								+ ", pr.lateDeliveryTime = "
								+ pr.lateDeliveryTime + ", distance = " + dis);
						if (ID == sim.debugTaxiID)
							log.println("Vehicle[" + ID
									+ "]::computeSpeed, ii = " + ii
									+ ", BUG???? " + "T.current = "
									+ T.currentTimePoint
									+ ", pr.lateDeliveryTime = "
									+ pr.lateDeliveryTime + ", distance = "
									+ dis);
						log.close();
						System.exit(-1);
					}
					double speed = dis
							/ (pr.lateDeliveryTime - T.currentTimePoint);
					if (ID == sim.debugTaxiID)
						log.println("Vehicle[" + ID
								+ "]::computeSpeed, people request " + pr.id
								+ ", pr.lateDeliveryTime = "
								+ pr.lateDeliveryTime + " > T.current, dis = "
								+ dis + " --> return speed = " + speed);
					return speed;
				}
				/*
				 * time =(pr.lateDeliveryTime + pr.earlyDeliveryTime)/2 -
				 * pr.deliveryDuration - T.currentTimePoint; if(time <= 0) time
				 * = pr.lateDeliveryTime - pr.deliveryDuration -
				 * T.currentTimePoint;
				 * //System.out.println("Vehicle::computeSpeed, peopleRequest = "
				 * + reqID + ", pr.lateDeliveryTime = " //+ pr.lateDeliveryTime
				 * + ", pr.pickupDuration = " + pr.pickupDuration +
				 * ", currentTime = " + T.currentTimePoint + ", time = " + time
				 * + ", dis = " + dis); if(time <= 0 && dis > 0){
				 * System.out.println("Vehicle::computeSpeed, time = " + time +
				 * " < 0 --> BUG???????"); System.exit(-1); } break;
				 */
			} else if (currentItinerary.getAction(ii) == VehicleAction.DELIVERY_PARCEL
					&& ii > lastIndexPoint) {
				reqID = currentItinerary.getRequestID(ii);
				ParcelRequest pr = sim.getParcelRequest(reqID);
				int t = T.currentTimePoint + (int) (dis / sim.maxSpeedms);
				if (ID == sim.debugTaxiID)
					log.println("Vehicle[" + ID
							+ "]::computeSpeed, T.current = "
							+ T.currentTimePoint + ", delivery people " + pr.id
							+ ", pr.earlyDelivery = " + pr.earlyDeliveryTime
							+ ", pr.lateDelivery = " + pr.lateDeliveryTime
							+ ", pr.deliveryDuration = " + pr.deliveryDuration
							+ ", t = " + t);

				if (t >= pr.earlyDeliveryTime && t <= pr.lateDeliveryTime) {
					if (ID == sim.debugTaxiID)
						log.println("Vehicle[" + ID
								+ "]::computeSpeed, parcelRequest " + pr.id
								+ ", t = " + t + ", pr.earlyDeliveryTime = "
								+ pr.earlyDeliveryTime
								+ ", pr.lateDeliveryTime = "
								+ pr.lateDeliveryTime
								+ " --> return maxSpeed = " + sim.maxSpeedms);

					return sim.maxSpeedms;
				} else if (t < pr.earlyDeliveryTime) {
					double speed = dis
							/ (pr.earlyPickupTime - T.currentTimePoint);
					if (ID == sim.debugTaxiID)
						log.println("Vehicle[" + ID
								+ "]::computeSpeed, parcel request " + pr.id
								+ ", t = " + t + " < pr.earlyDeliveryTime = "
								+ pr.earlyDeliveryTime + ", dis = " + dis
								+ ", --> return  speed = " + speed);

					return speed;
				} else {
					if (pr.lateDeliveryTime - T.currentTimePoint <= 0) {
						System.out.println("Vehicle[" + ID
								+ "]::computeSpeed, ii = " + ii + ", BUG???? "
								+ "T.current = " + T.currentTimePoint
								+ ", pr.lateDeliveryTime = "
								+ pr.lateDeliveryTime + ", distance = " + dis);
						if (ID == sim.debugTaxiID)
							log.println("Vehicle[" + ID
									+ "]::computeSpeed, ii = " + ii
									+ ", BUG???? " + "T.current = "
									+ T.currentTimePoint
									+ ", pr.lateDeliveryTime = "
									+ pr.lateDeliveryTime + ", distance = "
									+ dis);
						log.close();
						System.exit(-1);
					}
					double speed = dis
							/ (pr.lateDeliveryTime - T.currentTimePoint);
					if (ID == sim.debugTaxiID)
						log.println("Vehicle[" + ID
								+ "]::computeSpeed, parcel request + " + pr.id
								+ ", pr.lateDeliveryTime = "
								+ pr.lateDeliveryTime + " > T.current, dis = "
								+ dis + ", --> return speed = " + speed);
					return speed;
					// return dis/(pr.lateDeliveryTime - T.currentTimePoint);
				}
				/*
				 * time = (pr.lateDeliveryTime + pr.earlyDeliveryTime)/2 -
				 * pr.deliveryDuration - T.currentTimePoint; if(time <= 0) time
				 * = pr.lateDeliveryTime - pr.deliveryDuration -
				 * T.currentTimePoint;
				 * //System.out.println("Vehicle::computeSpeed, peopleRequest = "
				 * + reqID + ", pr.lateDeliveryTime = " //+ pr.lateDeliveryTime
				 * + ", pr.pickupDuration = " + pr.pickupDuration +
				 * ", currentTime = " + T.currentTimePoint + ", time = " + time
				 * + ", dis = " + dis); if(time <= 0 && dis > 0){
				 * System.out.println("Vehicle::computeSpeed, time = " + time +
				 * " < 0 --> BUG???????"); System.exit(-1); } break;
				 */
			} else {
				if (ii + 1 < currentItinerary.size()) {
					int u1 = currentItinerary.get(ii);
					int u2 = currentItinerary.get(ii + 1);
					Arc a12 = map.getArc(u1, u2);
					dis = dis + a12.w;
					// log.println("Vehicle[" + ID + "]::computeSpeed, ii = " +
					// ii + ", arc(" + u1 + "," + u2 + ") w = " + a12.w +
					// ", dis = " + dis);
				}
			}
		}
		if (time < 0)
			return sim.maxSpeedms;

		return dis / time;
	}

	public boolean finishedItinerary() {
		return lastIndexPoint == currentItinerary.size() - 1;
	}

	public String name() {
		return "Vehicle[" + ID + "]";
	}

	public boolean restAtParking(){
		return currentItinerary.size() == 0;
	}
	public void moveWithoutStatus(int currentTimePoint) {
		if (currentItinerary.size() == 0)// rest at parking
			return;
		ItineraryTravelTime I = currentItinerary;
		if(lastIndexPoint == I.size()-1){
			if(currentItinerary.getArrivalTime(lastIndexPoint) <= currentTimePoint){
				currentItinerary = new ItineraryTravelTime();// finished current itinerary --> rest at parking
				lastIndexPoint = 0;
			}
			return;
		}
		
		//if(currentTimePoint == I.getArrivalTime(lastIndexPoint+1)){
			//lastIndexPoint++;
			//lastPoint = I.get(lastIndexPoint);
			//int rid = I.getRequestID(lastIndexPoint);
			
			while(lastIndexPoint < I.size()-1){
				
				if(I.getArrivalTime(lastIndexPoint+1) > currentTimePoint){
					// do nothing, break, wait until I.getArrivalTime(lastIndexPoint+1) = currentTimePoint
					break;
				}else if(I.getArrivalTime(lastIndexPoint+1) < currentTimePoint){
					sim.log.println(name() + "::moveWithoutStatus(" + currentTimePoint + " EXCEPTION, lastIndexPoint = " +
				lastIndexPoint + ", arrivalTime(lastIndexPoint+1) = " + I.getArrivalTime(lastIndexPoint+1) + " < currentTimePoint = " +
							currentTimePoint);
					
					sim.exit();
				}
				// from this point, I.getArrivalTime(lastIndexPoint+1) == currentTimePoint
				// --> move to next point of the itinerary
				int nextPoint = currentItinerary.get(lastIndexPoint + 1);
				Arc a0 = map.getArc(lastPoint, nextPoint);
				if (a0 != null) {
					remainDistance = remainDistance - a0.w;
					totalTravelDistance += a0.w;
					if(sim.maxTravelDistanceOneTaxi < totalTravelDistance)
						sim.maxTravelDistanceOneTaxi = totalTravelDistance;
					
					sim.totalTravelDistance += a0.w;
					sim.cost = sim.getCostFuel(sim.totalTravelDistance);
					// update accumulate distance of people requests
					for (int i = 0; i < peopleReqIDonBoard.size(); i++) {
						int r = peopleReqIDonBoard.get(i);
						sim.accumulateDistance.put(r,sim.accumulateDistance.get(r) + a0.w);
						
					}
					
					sim.runningTaxis.add(this);
					
				}
				lastIndexPoint++;
				lastPoint = I.get(lastIndexPoint);
				
				/*
				LatLng ll = sim.map.mLatLng.get(lastPoint);
				Square sq = sim.map.findSquare(ll.lat, ll.lng);
				if(sq == null){
					System.out.println(name() + "::moveWithoutStatus, EXCEPTION??? square of taxi " + ID + " NULL, System.exit(-1)");
					sim.exit();
				}
				Square oldSQ = sim.squareOfTaxi.get(this); 
				if(sq != oldSQ){
					sim.squareOfTaxi.put(this, sq);
					ArrayList<Vehicle> taxis = sim.taxisOfSquare.get(oldSQ);
					taxis.remove(taxis.indexOf(this));
					taxis = sim.taxisOfSquare.get(sq);
					taxis.add(this);
				}
				*/
				
				int rid = I.getRequestID(lastIndexPoint);
				if(rid > 0){// lastIndexPoint/lastPoint is a service point (pickup or delivery people or parcel)
					VehicleAction act = I.getAction(lastIndexPoint);
					if(act == VehicleAction.PICKUP_PARCEL){
						parcelReqIDonBoard.add(rid);
						if(remainRequestIDs.size() <= 0){
							if(sim.DEBUG){
								sim.log.println(name() + "::moveWithoutStatus(" + currentTimePoint + ") EXCEPTION pickup parcel " + rid + 
										" BUT remainRequestIDs.sz = 0, STATUS = " + requestStatus() + ", currentItinerary = " + I.toString());
								sim.exit();
							}
						}
						int r = remainRequestIDs.get(0);
						if(r != rid){
							if(sim.DEBUG){
								sim.log.println(name() + "::moveWithoutStatus(" + currentTimePoint + ") EXCEPTION pickup parcel " + rid + 
										" BUT remainRequestIDs[0] = " + r + ", STATUS = " + requestStatus() + ", currentItinerary = " + I.toString());
								sim.exit();
							}
						}
						remainRequestIDs.remove(0);
						sim.nbParcelOnBoard++;
						sim.nbParcelServed++;
						
						establishCompletePickupDeliveryPoints();
					}else if(act == VehicleAction.DELIVERY_PARCEL){
						if(parcelReqIDonBoard.size() <= 0){
							if(sim.DEBUG){
								sim.log.println(name() + "::moveWithoutStatus(" + currentTimePoint + ") EXCEPTION delivery parcel " + rid + 
										" BUT parcelReqIDOnBoard.sz = 0, STATUS = " + requestStatus() + ", currentItinerary = " + I.toString());
								sim.exit();
							}
						}
						nbRequestsServed++;
						if(sim.maxRequestsServedOneTaxi < nbRequestsServed) sim.maxRequestsServedOneTaxi = nbRequestsServed;
						
						int idxr = parcelReqIDonBoard.indexOf(rid);
						
						if(idxr < 0){
							if(sim.DEBUG){
								sim.log.println(name() + "::moveWithoutStatus(" + currentTimePoint + ") EXCEPTION delivery parcel " + rid + 
										" BUT index of parcel on board = idxr = " + idxr + ", STATUS = " + requestStatus() + ", currentItinerary = " + I.toString());
								sim.exit();
							}
						}
						parcelReqIDonBoard.remove(idxr);
						if(remainRequestIDs.size() <= 0){
							if(sim.DEBUG){
								sim.log.println(name() + "::moveWithoutStatus(" + currentTimePoint + ") EXCEPTION delivery parcel " + rid + 
										" BUT remainRequestIDs.sz = 0, STATUS = " + requestStatus() + ", currentItinerary = " + I.toString());
								sim.exit();
							}
						}
						int r = remainRequestIDs.get(0);
						if(r != -rid){
							if(sim.DEBUG){
								sim.log.println(name() + "::moveWithoutStatus(" + currentTimePoint + ") EXCEPTION delivery parcel " + rid + 
										" BUT remainRequestIDs[0] = " + r + ", STATUS = " + requestStatus() + ", currentItinerary = " + I.toString());
								sim.exit();
							}
						}
						remainRequestIDs.remove(0);
						sim.nbParcelOnBoard--;
						sim.nbParcelComplete++;
						ParcelRequest pr = sim.mParcelRequest.get(rid);
						double disParcel = sim.dijkstra.queryDistance(pr.pickupLocationID, pr.deliveryLocationID);
						sim.revenueParcel += sim.getParcelRevenue(disParcel);
						establishCompletePickupDeliveryPoints();
					}else if(act == VehicleAction.PICKUP_PEOPLE){
						peopleReqIDonBoard.add(rid);
						if(remainRequestIDs.size() <= 0){
							if(sim.DEBUG){
								sim.log.println(name() + "::moveWithoutStatus(" + currentTimePoint + ") EXCEPTION pickup people " + rid + 
										" BUT remainRequestIDs.sz = 0, STATUS = " + requestStatus() + ", currentItinerary = " + I.toString());
								sim.exit();
							}
						}
						int r = remainRequestIDs.get(0);
						if(r != rid){
							if(sim.DEBUG){
								sim.log.println(name() + "::moveWithoutStatus(" + currentTimePoint + ") EXCEPTION pickup people " + rid + 
										" BUT remainRequestIDs[0] = " + r + ", STATUS = " + requestStatus() + ", currentItinerary = " + I.toString());
								sim.exit();
							}
						}
						remainRequestIDs.remove(0);
						sim.nbPeopleOnBoard++;
						sim.nbPeopleServed++;
						sim.countStop.put(rid,0);
						sim.accumulateDistance.put(rid, 0.0);
						establishCompletePickupDeliveryPoints();
					}else if(act == VehicleAction.DELIVERY_PEOPLE){
						if(peopleReqIDonBoard.size() <= 0){
							if(sim.DEBUG){
								sim.log.println(name() + "::moveWithoutStatus(" + currentTimePoint + ") EXCEPTION delivery people " + rid + 
										" BUT peopleReqIDOnBoard.sz = 0, STATUS = " + requestStatus() + ", currentItinerary = " + I.toString());
								sim.exit();
							}
						}
						nbRequestsServed++;
						if(sim.maxRequestsServedOneTaxi < nbRequestsServed) sim.maxRequestsServedOneTaxi = nbRequestsServed;
						
						int idxr = peopleReqIDonBoard.indexOf(rid);
						
						if(idxr < 0){
							if(sim.DEBUG){
								sim.log.println(name() + "::moveWithoutStatus(" + currentTimePoint + ") EXCEPTION delivery people " + rid + 
										" BUT index of people on board = idxr = " + idxr + ", STATUS = " + requestStatus() + ", currentItinerary = " + I.toString());
								sim.exit();
							}
						}
						peopleReqIDonBoard.remove(idxr);
						if(remainRequestIDs.size() <= 0){
							if(sim.DEBUG){
								sim.log.println(name() + "::moveWithoutStatus(" + currentTimePoint + ") EXCEPTION delivery people " + rid + 
										" BUT remainRequestIDs.sz = 0, STATUS = " + requestStatus() + ", currentItinerary = " + I.toString());
								sim.exit();
							}
						}
						int r = remainRequestIDs.get(0);
						if(r != -rid){
							if(sim.DEBUG){
								sim.log.println(name() + "::moveWithoutStatus(" + currentTimePoint + ") EXCEPTION delivery people " + rid + 
										" BUT remainRequestIDs[0] = " + r + ", STATUS = " + requestStatus() + ", currentItinerary = " + I.toString());
								sim.exit();
							}
						}
						remainRequestIDs.remove(0);
						sim.nbPeopleComplete++;
						sim.nbPeopleOnBoard--;
						establishCompletePickupDeliveryPoints();
						
						PeopleRequest pr = sim.mPeopleRequest.get(rid);
						double expectedDistancePeople = sim.dijkstra.queryDistance(pr.pickupLocationID, pr.deliveryLocationID);
						sim.revenuePeople += sim.getPeopleRevenue(expectedDistancePeople);
						double realDis = sim.accumulateDistance.get(rid);
						double discount =  sim.getDiscount(expectedDistancePeople, realDis);
						sim.discount += discount;
						if(discount < 0){
							System.out.println(name() + "::moveWithoutStatus discount < 0 = " + discount + ", realDis = " + realDis + 
									", disPeople = " + expectedDistancePeople + " --> System.exit(-1)");
							System.exit(-1);
						}
					}else{
						sim.log.println(name() + "::moveWithoutStatus(" + currentTimePoint + " EXCEPTION rid = " + rid + 
								", Unknown action act = " + getActionDescription(act));
						sim.exit();
					}
					
					//* move following instruction outside if(rid>0) condition
					// update stops of people requests
					for (int i = 0; i < peopleReqIDonBoard.size(); i++) {
						int r = peopleReqIDonBoard.get(i);
						if (r != rid) {
							sim.countStop.put(r, sim.countStop.get(r) + 1);

							//if (a0 != null)	sim.accumulateDistance.put(r,sim.accumulateDistance.get(r) + a0.w);
						}
					}
					ErrorMSG err = sim.checkServiceSequence(this, null); 
					if (err.err != ErrorType.NO_ERROR) {
						log.println(name()
								+ "::move --> checkServiceSequence FAILED errMSG = " + err.msg + ", Status = "
								+ requestStatus());
						
						sim.exit();// close log, update execution time
						
					}
					
				}
				
				/*
				// update stops of people requests
				for (int i = 0; i < peopleReqIDonBoard.size(); i++) {
					int r = peopleReqIDonBoard.get(i);
					if (r != rid) {
						sim.countStop.put(r, sim.countStop.get(r) + 1);

						if (a0 != null)
							sim.accumulateDistance.put(r,
									sim.accumulateDistance.get(r) + a0.w);
					}
				}
				ErrorMSG err = sim.checkServiceSequence(this, null); 
				if (err.err != ErrorType.NO_ERROR) {
					log.println(name()
							+ "::move --> checkServiceSequence FAILED errMSG = " + err.msg + ", Status = "
							+ requestStatus());
					
					sim.exit();// close log, update execution time
					
				}
				*/

			}
		//}
	}

	public void move(int currentTimePoint) {

		/*
		 * move not one second BUT dt seconds. Update status of the taxi after
		 * dt seconds
		 */

		if (currentItinerary != null) {
			if (lastIndexPoint < currentItinerary.size() - 1) {
				int tp = currentItinerary.getArrivalTime(lastIndexPoint + 1);
				if (tp < currentTimePoint) {
					System.out
							.println(name()
									+ "::move("
									+ currentTimePoint
									+ ") EXCEPTION??? lastIndexPoint = "
									+ lastIndexPoint
									+ ", currentItinerary.getArrivalTime(lastIndexPoint+1) = "
									+ tp + " < currentTimePoint = "
									+ currentTimePoint
									+ ", currentItinerary = "
									+ currentItinerary.toString());
					log.println(name()
							+ "::move("
							+ currentTimePoint
							+ ") EXCEPTION??? lastIndexPoint = "
							+ lastIndexPoint
							+ ", currentItinerary.getArrivalTime(lastIndexPoint+1) = "
							+ tp + " < currentTimePoint = " + currentTimePoint
							+ ", currentItinerary = "
							+ currentItinerary.toString());
					log.close();
					sim.exit();// System.exit(-1);
				}
			}
		}
		// System.out.println(name() + "::move(currentTime = " +
		// currentTimePoint + ")");

		// if(ID == 3)
		// System.out.println(name() + "["+ ID + "]::move() --> T = " +
		// T.currentTimePoint + ", lastIndexPoint = " +
		// lastIndexPoint + ", lastPoint = " + lastPoint + ", status = " +
		// getStatusDescription() + ", action = " + getActionDescription());
		// log.println("Vehicle::move At time point " + T.currentTimePoint +
		// " --> taxi[" + ID + "] has status = " + getStatusDescription(status)
		// +
		// ", lastIndexPoint = " + lastIndexPoint + ", lastPoint = " + lastPoint
		// + ", remaiTimeToNextPoint = " + remainTimeToNextPoint);
		// int taxiID = 278;
		if (status == VehicleStatus.PICKUP_PEOPLE) {
			if (currentTimePoint == currentItinerary
					.getDepartureTime(lastIndexPoint)) {
				status = VehicleStatus.FINISHED_PICKUP_PEOPLE;
				if (ID == sim.debugTaxiID) {
					sim.log.println(name() + "::move(" + currentTimePoint
							+ ") TAXI " + ID + "AT "
							+ getCurrenPositionDescription()
							+ ", change status = " + getStatusDescription());
				}
				if (lastIndexPoint < currentItinerary.size() - 1)
					if (currentTimePoint == currentItinerary
							.getArrivalTime(lastIndexPoint + 1)) {
						lastIndexPoint++;
						lastPoint = currentItinerary.get(lastIndexPoint);
						if (mStatus.get(lastIndexPoint) != null) {
							status = mStatus.get(lastIndexPoint);
							if (ID == sim.debugTaxiID) {
								sim.log.println(name() + "::move("
										+ currentTimePoint + ") TAXI " + ID
										+ "AT "
										+ getCurrenPositionDescription()
										+ ", change status = "
										+ getStatusDescription());
							}
						}
					}
			}

			// return;
		}
		if (status == VehicleStatus.DELIVERY_PEOPLE) {
			if (currentTimePoint == currentItinerary
					.getDepartureTime(lastIndexPoint)) {
				status = VehicleStatus.FINISHED_DELIVERY_PEOPLE;
				if (ID == sim.debugTaxiID) {
					sim.log.println(name() + "::move(" + currentTimePoint
							+ ") TAXI " + ID + "AT "
							+ getCurrenPositionDescription()
							+ ", change status = " + getStatusDescription());
				}

				if (lastIndexPoint < currentItinerary.size() - 1)
					if (currentTimePoint == currentItinerary
							.getArrivalTime(lastIndexPoint + 1)) {
						lastIndexPoint++;
						lastPoint = currentItinerary.get(lastIndexPoint);
						if (mStatus.get(lastIndexPoint) != null) {
							status = mStatus.get(lastIndexPoint);
							if (ID == sim.debugTaxiID) {
								sim.log.println(name() + "::move("
										+ currentTimePoint + ") TAXI " + ID
										+ "AT "
										+ getCurrenPositionDescription()
										+ ", change status = "
										+ getStatusDescription());
							}
						}
					}
			}
			// return;
		}
		if (status == VehicleStatus.PICKUP_PARCEL) {
			if (currentTimePoint == currentItinerary
					.getDepartureTime(lastIndexPoint)) {
				status = VehicleStatus.FINISHED_PICKUP_PARCEL;
				if (ID == sim.debugTaxiID) {
					sim.log.println(name() + "::move(" + currentTimePoint
							+ ") TAXI " + ID + "AT "
							+ getCurrenPositionDescription()
							+ ", change status = " + getStatusDescription());
				}
				if (lastIndexPoint < currentItinerary.size() - 1)
					if (currentTimePoint == currentItinerary
							.getArrivalTime(lastIndexPoint + 1)) {
						lastIndexPoint++;
						lastPoint = currentItinerary.get(lastIndexPoint);
						if (mStatus.get(lastIndexPoint) != null) {
							status = mStatus.get(lastIndexPoint);
							if (ID == sim.debugTaxiID) {
								sim.log.println(name() + "::move("
										+ currentTimePoint + ") TAXI " + ID
										+ "AT "
										+ getCurrenPositionDescription()
										+ ", change status = "
										+ getStatusDescription());
							}
						}
					}
			}
			// return;
		}
		if (status == VehicleStatus.DELIVERY_PARCEL) {
			if (currentTimePoint == currentItinerary
					.getDepartureTime(lastIndexPoint)) {
				status = VehicleStatus.FINISHED_DELIVERY_PARCEL;
				if (ID == sim.debugTaxiID) {
					sim.log.println(name() + "::move(" + currentTimePoint
							+ ") TAXI " + ID + "AT "
							+ getCurrenPositionDescription()
							+ ", change status = " + getStatusDescription());
				}
				if (lastIndexPoint < currentItinerary.size() - 1)
					if (currentTimePoint == currentItinerary
							.getArrivalTime(lastIndexPoint + 1)) {
						lastIndexPoint++;
						lastPoint = currentItinerary.get(lastIndexPoint);
						if (mStatus.get(lastIndexPoint) != null) {
							status = mStatus.get(lastIndexPoint);
							if (ID == sim.debugTaxiID) {
								sim.log.println(name() + "::move("
										+ currentTimePoint + ") TAXI " + ID
										+ "AT "
										+ getCurrenPositionDescription()
										+ ", change status = "
										+ getStatusDescription());
							}
						}
					}
			}
			// return;
		}
		// update data structure when we go to the next time point

		if (status == VehicleStatus.REST_AT_PARKING) {
			if (currentItinerary != null)
				if (currentTimePoint == currentItinerary
						.getDepartureTime(lastIndexPoint)) {
					status = changeStatus(lastIndexPoint);
					if (ID == sim.debugTaxiID) {
						sim.log.println(name() + "::move(" + currentTimePoint
								+ ") TAXI " + ID + "AT "
								+ getCurrenPositionDescription()
								+ ", change status = " + getStatusDescription());
					}
					if (ID == sim.debugTaxiID) {
						sim.log.println(name() + "::move(" + currentTimePoint
								+ ") TAXI " + ID + "AT "
								+ getCurrenPositionDescription()
								+ ", change status = " + getStatusDescription());
					}
				}
			// return;

		}

		if (status == VehicleStatus.PREPARE_NEW_ITINERARY) {
			if (currentTimePoint == currentItinerary
					.getArrivalTime(lastIndexPoint + 1)) {
				cancelRemainItinerary();
				setNewItinerary();
			}
			return;
		}
		if (status == VehicleStatus.GO_BACK_DEPOT_FINISH_WORK) {
			// System.out.println("Vehicle::move, ID = " + ID +
			// ", status = GO_BACK_DEPOT_FINISH_WORK");
		}
		remainTimeToNextPoint--;

		if (currentItinerary == null)
			return;

		// if(remainTimeToNextPoint == 0){
		// if(T.currentTimePoint ==
		// currentItinerary.getArrivalTime(lastIndexPoint+1)){
		if (ID == sim.debugTaxiID)
			log.println("Vehicle[" + ID + "]::move, currentTimePoint = "
					+ currentTimePoint + ", lastIndexPoint = " + lastIndexPoint
					+ ", lastPoint = " + lastPoint
					+ ", getArrivalTime(lastIndexPoint+1) = "
					+ currentItinerary.getArrivalTime(lastIndexPoint + 1)
					+ ", status = " + getStatusDescription(status));

		while (currentTimePoint == currentItinerary
				.getArrivalTime(lastIndexPoint + 1)) {
			int nextPoint = currentItinerary.get(lastIndexPoint + 1);
			Arc a0 = map.getArc(lastPoint, nextPoint);
			if (a0 != null) {
				remainDistance = remainDistance - a0.w;
			}

			int rid = currentItinerary.getRequestID(lastIndexPoint + 1);
			VehicleAction act = currentItinerary.getAction(lastIndexPoint + 1);
			if (rid > 0) {
				if (remainRequestIDs.size() <= 0) {
					int a = lastIndexPoint + 1;
					log.println(name()
							+ "::move("
							+ currentTimePoint
							+ ") EXCEPTION, lastIndexPoint +1 = "
							+ a
							+ ", rid = "
							+ rid
							+ " > 0 BUT remainRequestIDs.sz = 0, currentItinerary = "
							+ currentItinerary.toString());
					System.out
							.println(name()
									+ "::move("
									+ currentTimePoint
									+ ") EXCEPTION???????????????????, lastIndexPoint +1 = "
									+ a
									+ ", rid = "
									+ rid
									+ " > 0 BUT remainRequestIDs.sz = 0, currentItinerary = "
									+ currentItinerary.toString());
					log.close();
					sim.exit();// System.exit(-1);
				}
				int a = remainRequestIDs.get(0);
				remainRequestIDs.remove(0);
				if (ID == sim.debugTaxiID) {
					sim.log.println(name() + "::move REACH rid > 0 = " + rid
							+ ", remainRequestIDs.remove(0) = " + a);
				}

				if (act == VehicleAction.PICKUP_PARCEL) {
					parcelReqIDonBoard.add(rid);
					if (sim.debugTaxiID == ID) {
						log.print(name() + "::move, pickup parcel " + rid
								+ ", parcels on board = ");
						for (int jj = 0; jj < parcelReqIDonBoard.size(); jj++)
							log.print(parcelReqIDonBoard.get(jj) + ",");
						log.print(" remainRequestIDs = " + getRemainRequestID());
						log.println();
					}
					sim.nbParcelOnBoard++;
					sim.nbParcelServed++;
					// if(peopleReqIDonBoard.size() > 0){
					// int peopleReq = peopleReqIDonBoard.get(0);
					// sim.countStop.put(peopleReq,
					// sim.countStop.get(peopleReq)+1);
					// }
					System.out.println("Vehicle[" + ID + "] At "
							+ T.timePointHMS(currentTimePoint)
							+ ", pickupParcel, nbParcelServed = "
							+ sim.nbParcelServed + ", nbParlceOnBoard = "
							+ sim.nbParcelOnBoard);
				} else if (act == VehicleAction.DELIVERY_PARCEL) {
					if (sim.debugTaxiID == ID) {
						log.print(name() + "::move, delivery parcel " + rid
								+ ", parcels on board = ");
						for (int jj = 0; jj < parcelReqIDonBoard.size(); jj++)
							log.print(parcelReqIDonBoard.get(jj) + ",");
						log.print(" remainRequestIDs = " + getRemainRequestID());
						log.println();
					}
					int idx = parcelReqIDonBoard.indexOf(rid);
					if (idx < 0) {
						log.println(name()
								+ "::move, EXCEPTION currentItinerary = "
								+ currentItinerary.toString());
						log.println(name()
								+ "::move, taxi "
								+ ID
								+ ", action = delivery parcel "
								+ rid
								+ ", EXCEPTION index of parcel on board = idx = "
								+ idx + ", lastIndexPoint = " + lastIndexPoint
								+ ", lastPoint = " + lastPoint
								+ ", T.current = " + T.currentTimePoint
								+ ", currentTimePoint = " + currentTimePoint);
						sim.log.close();
					}

					parcelReqIDonBoard.remove(idx);
					sim.nbParcelComplete++;
					sim.nbParcelOnBoard--;

					// if(peopleReqIDonBoard.size() > 0){
					// int peopleReq = peopleReqIDonBoard.get(0);
					// sim.countStop.put(peopleReq,
					// sim.countStop.get(peopleReq)+1);
					// }
					System.out.println("Vehicle[" + ID + "] At "
							+ T.timePointHMS(currentTimePoint)
							+ ", deliveryParcel, nbParcelComplete = "
							+ sim.nbParcelComplete + ", nbParlceOnBoard = "
							+ sim.nbParcelOnBoard);
				} else if (act == VehicleAction.PICKUP_PEOPLE) {
					peopleReqIDonBoard.add(rid);
					if (sim.debugTaxiID == ID) {
						log.print(name() + "::move, pickup people " + rid
								+ ", people on board = ");
						for (int jj = 0; jj < peopleReqIDonBoard.size(); jj++)
							log.print(peopleReqIDonBoard.get(jj) + ",");
						log.print(" remainRequestIDs = " + getRemainRequestID());
						log.println();
					}
					sim.countStop.put(rid, 0);
					sim.accumulateDistance.put(rid, 0.0);
					sim.nbPeopleOnBoard++;
					sim.nbPeopleServed++;
					System.out.println("Vehicle[" + ID + "] At "
							+ T.timePointHMS(currentTimePoint)
							+ ", pickupPeople, nbPeopleServed = "
							+ sim.nbPeopleServed + ", nbPeopleOnBoard = "
							+ sim.nbPeopleOnBoard);
				} else if (act == VehicleAction.DELIVERY_PEOPLE) {
					if (sim.debugTaxiID == ID) {
						log.print(name() + "::move, delivery people " + rid
								+ ", people on board = ");
						for (int jj = 0; jj < peopleReqIDonBoard.size(); jj++)
							log.print(peopleReqIDonBoard.get(jj) + ",");
						log.print(" remainRequestIDs = " + getRemainRequestID());
						log.println();
					}
					int idx = peopleReqIDonBoard.indexOf(rid);
					if (idx < 0) {
						log.println(name()
								+ "::move, EXCEPTION currentItinerary = "
								+ currentItinerary.toString());
						log.println(name()
								+ "::move, taxi "
								+ ID
								+ ", action = delivery people "
								+ rid
								+ ", EXCEPTION index of parcel on board = idx = "
								+ idx + ", lastIndexPoint = " + lastIndexPoint
								+ ", lastPoint = " + lastPoint
								+ ", T.current = " + T.currentTimePoint
								+ ", currentTimePoint = " + currentTimePoint);
						sim.log.close();
					}
					peopleReqIDonBoard.remove(idx);
					sim.nbPeopleComplete++;
					sim.nbPeopleOnBoard--;
					System.out.println("Vehicle[" + ID + "] At "
							+ T.timePointHMS(currentTimePoint)
							+ ", deliveryPeople, nbPeopleComplete = "
							+ sim.nbPeopleComplete + ", nbPeopleOnBoard = "
							+ sim.nbPeopleOnBoard);
				} else {
					System.out
							.println("Vehicle::move EXCEPTION unknown action?????");
					System.exit(-1);
				}

				if (ID == sim.debugTaxiID)
					sim.log.println(name() + "::move --> request status = "
							+ requestStatus());
				// update stops of people requests
				for (int i = 0; i < peopleReqIDonBoard.size(); i++) {
					int r = peopleReqIDonBoard.get(i);
					if (r != rid) {
						sim.countStop.put(r, sim.countStop.get(r) + 1);

						if (a0 != null)
							sim.accumulateDistance.put(r,
									sim.accumulateDistance.get(r) + a0.w);
					}
				}

				ErrorMSG err = sim.checkServiceSequence(this, null);
				if (err.err != ErrorType.NO_ERROR) {
					log.println(name()
							+ "::move --> checkServiceSequence FAILED, errMSG = " + err.msg + ", Status = "
							+ requestStatus());
					// sim.log.close();
					sim.exit();// close log, update execution time
					// System.out.println(name() + "::move exec time = " +
					// sim.getExecTimeSeconds());

					System.exit(-1);
				}

			}

			lastIndexPoint++;

			lastPoint = currentItinerary.get(lastIndexPoint);
			// if(ID == sim.debugTaxiID)log.println("Vehicle[" + ID +
			// "]::move, REACH currentTimePoint = " + currentTimePoint +
			// ", lastIndexPoint = " + lastIndexPoint + ", lastPoint = " +
			// lastPoint + ", getArrivalTime(lastIndexPoint+1) = " +
			// currentItinerary.getArrivalTime(lastIndexPoint+1) + ", status = "
			// + getStatusDescription(status));

			// currentItinerary.setArrivalTime(lastIndexPoint,
			// T.currentTimePoint);
			// System.out.println("Taxi " + ID + ", setArrivalTime(" +
			// lastIndexPoint + "," + T.currentTimePoint);

			String actionStr = "NULL";
			if (currentItinerary.getAction(lastIndexPoint) != null) {
				actionStr = getActionDescription(currentItinerary
						.getAction(lastIndexPoint));
			}
			if (currentItinerary.getAction(lastIndexPoint) == VehicleAction.PASS) {
				// currentItinerary.setDepartureTime(lastIndexPoint,
				// T.currentTimePoint);
			}
			// if(ID == 4){
			// System.out.println("Vehicle::move, lastIndexPoint = " +
			// lastIndexPoint + ", action = " + actionStr);
			// }
			// log.println("At timePoint " + T.currentTimePoint + ": taxi[" + ID
			// + "] changes lastPoint = " + lastPoint);
			// log.println("At " + T.currentTimePointHMS() + ", taxi[" + ID +
			// "] lastIndexPoint = " + lastIndexPoint + ", lastPoint = " +
			// lastPoint + ", status " + getStatusDescription(status));
			if (currentItinerary.size() > lastIndexPoint + 1) {
				int v = currentItinerary.get(lastIndexPoint + 1);
				Arc a = map.getArc(lastPoint, v);
				/*
				 * double speed = computeSpeed();// Simulator.maxSpeedms;
				 * //System.out.println("Vehicle::move --> speed = " + speed +
				 * ", maxSpeed = " + sim.maxSpeedms + ", minSpeed = " +
				 * sim.minSpeedms); remainTimeToNextPoint =
				 * Simulator.getTravelTime(a, speed);//Simulator.maxSpeedms);
				 * //currentItinerary.getTravelTime(lastIndexPoint);//a.t;
				 * if(remainTimeToNextPoint == 0) remainTimeToNextPoint = 1;
				 */
				if (a != null)
					totalTravelDistance += a.w;
				else {
					System.out.println("Vehicle::move, Arc (" + lastPoint + ","
							+ v + ") does not exists ????????????????");
					log.println("Vehicle[" + ID + "]::move, Arc (" + lastPoint
							+ "," + v + ") does not exists ????????????????");
				}
				// log.println("At " + T.currentTimePoint +
				// ", remainTimeToNextPoint = " + remainTimeToNextPoint);
			} else {
				// status = VehicleStatus.REST_AT_PARKING;
			}
			if (mStatus.get(lastIndexPoint) != null) {
				status = mStatus.get(lastIndexPoint);
				if (ID == sim.debugTaxiID) {
					sim.log.println(name() + "::move(" + currentTimePoint
							+ ") TAXI " + ID + "AT "
							+ getCurrenPositionDescription()
							+ ", change status = " + getStatusDescription());
				}
				if (status == VehicleStatus.REST_AT_PARKING) {
					System.out.println("Vehicle[" + ID + "]::move, At "
							+ T.timePointHMS(currentTimePoint) + "["
							+ currentTimePoint + "], " +

							"taxi[" + ID + "] REST AT PARKING");
				}
				// log.println("Vehicle[" + ID + "]::move, At " +
				// T.timePointHMS(currentTimePoint) + "[" + currentTimePoint +
				// "], " +
				// "taxi[" + ID + "] changes status = " +
				// getStatusDescription(status) +
				// " at point " + lastPoint);

				if (currentItinerary.getAction(lastIndexPoint) == VehicleAction.FINISH_WORK) {
					System.out.println("Vehicle::move, ID = " + ID
							+ ", action = FINISH_WORK, status = "
							+ getStatusDescription(status));
					if (status != VehicleStatus.STOP_WORK) {
						System.out
								.println("Vehicle["
										+ ID
										+ "]::move, ID = "
										+ ID
										+ ", exception inconsistent, action = FINISH_WORK, but status != STOP_WORK");
						System.exit(-1);
					}
				}
				if (currentItinerary.getAction(lastIndexPoint) == VehicleAction.PICKUP_PEOPLE) {
					if (status != VehicleStatus.PICKUP_PEOPLE) {
						System.out.println("Vehicle::move, time = "
								+ T.currentTimePoint + " --> INCONSISTENT");
						System.exit(1);
					}
					PeopleRequest pr = sim.getPeopleRequest(currentItinerary
							.getRequestID(lastIndexPoint));
					remainTimeToNextDeparture = pr.pickupDuration;// 60;//
																	// default
																	// pickup
																	// time is
																	// 60s
					// if(currentItinerary.getRequestID(lastIndexPoint) == 242){
					// System.out.println("Vehicle[" + ID +
					// "]::move --> pickup people " + 242 + " at time point " +
					// T.currentTimePoint);
					// System.exit(-1);
					// }
				} else if (currentItinerary.getAction(lastIndexPoint) == VehicleAction.DELIVERY_PEOPLE) {
					if (status != VehicleStatus.DELIVERY_PEOPLE) {
						System.out.println("Vehicle::move, time = "
								+ currentTimePoint + " --> INCONSISTENT");
						System.exit(1);
					}
					PeopleRequest pr = sim.getPeopleRequest(currentItinerary
							.getRequestID(lastIndexPoint));
					remainTimeToNextDeparture = pr.deliveryDuration;// 60;//
																	// default
																	// delivery
																	// time is
																	// 60s
				} else if (currentItinerary.getAction(lastIndexPoint) == VehicleAction.PICKUP_PARCEL) {
					if (status != VehicleStatus.PICKUP_PARCEL) {
						System.out.println("Vehicle::move, time = "
								+ currentTimePoint + " --> INCONSISTENT");
						System.exit(1);
					}
					// System.out.println("Vehicle["+ ID +
					// "]::move, lastIndexPoint = " + lastIndexPoint +
					// ", lastIndex RequestID = " +
					// currentItinerary.getRequestID(lastIndexPoint) +
					// ", nextItinerary = " + nextItinerary2String());
					ParcelRequest pr = sim.getParcelRequest(currentItinerary
							.getRequestID(lastIndexPoint));
					remainTimeToNextDeparture = pr.pickupDuration;// 60;//
																	// default
																	// pickup
																	// time is
																	// 60s
				} else if (currentItinerary.getAction(lastIndexPoint) == VehicleAction.DELIVERY_PARCEL) {
					if (status != VehicleStatus.DELIVERY_PARCEL) {
						System.out.println("Vehicle::move, time = "
								+ currentTimePoint + " --> INCONSISTENT");
						System.exit(1);
					}
					ParcelRequest pr = sim.getParcelRequest(currentItinerary
							.getRequestID(lastIndexPoint));
					remainTimeToNextDeparture = pr.deliveryDuration;// 60;//
																	// default
																	// delivery
																	// time is
																	// 60s
				}
			}

		}
	}

	public void moveLong(int dt) {
		for (int i = 0; i < dt; i++) {
			//move(T.currentTimePoint + i);
			moveWithoutStatus(T.currentTimePoint+i);
		}
	}

	public void move() {
		// if(ID == 3)
		// System.out.println(name() + "["+ ID + "]::move() --> T = " +
		// T.currentTimePoint + ", lastIndexPoint = " +
		// lastIndexPoint + ", lastPoint = " + lastPoint + ", status = " +
		// getStatusDescription() + ", action = " + getActionDescription());
		// log.println("Vehicle::move At time point " + T.currentTimePoint +
		// " --> taxi[" + ID + "] has status = " + getStatusDescription(status)
		// +
		// ", lastIndexPoint = " + lastIndexPoint + ", lastPoint = " + lastPoint
		// + ", remaiTimeToNextPoint = " + remainTimeToNextPoint);
		// int taxiID = 278;
		if (status == VehicleStatus.PICKUP_PEOPLE) {
			if (T.currentTimePoint == currentItinerary
					.getDepartureTime(lastIndexPoint)) {
				status = VehicleStatus.FINISHED_PICKUP_PEOPLE;
				if (lastIndexPoint < currentItinerary.size() - 1)
					if (T.currentTimePoint == currentItinerary
							.getArrivalTime(lastIndexPoint + 1)) {
						lastIndexPoint++;
						lastPoint = currentItinerary.get(lastIndexPoint);
						if (mStatus.get(lastIndexPoint) != null) {
							status = mStatus.get(lastIndexPoint);
						}
					}
			}
			/*
			 * remainTimeToNextDeparture--; if(remainTimeToNextDeparture == 0){
			 * currentItinerary.setDepartureTime(lastIndexPoint,
			 * T.currentTimePoint); //System.out.println("Taxi " + ID +
			 * ", setDepartureTime(" + lastIndexPoint + "," +
			 * T.currentTimePoint); status =
			 * VehicleStatus.FINISHED_PICKUP_PEOPLE; }
			 */

			return;
		}
		if (status == VehicleStatus.DELIVERY_PEOPLE) {
			if (T.currentTimePoint == currentItinerary
					.getDepartureTime(lastIndexPoint)) {
				status = VehicleStatus.FINISHED_DELIVERY_PEOPLE;

				if (lastIndexPoint < currentItinerary.size() - 1)
					if (T.currentTimePoint == currentItinerary
							.getArrivalTime(lastIndexPoint + 1)) {
						lastIndexPoint++;
						lastPoint = currentItinerary.get(lastIndexPoint);
						if (mStatus.get(lastIndexPoint) != null) {
							status = mStatus.get(lastIndexPoint);
						}
					}
			}
			/*
			 * remainTimeToNextDeparture--; if(remainTimeToNextDeparture == 0){
			 * //status =
			 * VehicleStatus.GOING_TO_DELIVERY_PEOPEL;//VehicleStatus.
			 * DELIVERY_PEOPLE;
			 * currentItinerary.setDepartureTime(lastIndexPoint,
			 * T.currentTimePoint); //System.out.println("Taxi " + ID +
			 * ", setDepartureTime(" + lastIndexPoint + "," +
			 * T.currentTimePoint); status =
			 * VehicleStatus.FINISHED_DELIVERY_PEOPLE; }
			 */
			return;
		}
		if (status == VehicleStatus.PICKUP_PARCEL) {
			if (T.currentTimePoint == currentItinerary
					.getDepartureTime(lastIndexPoint)) {
				status = VehicleStatus.FINISHED_PICKUP_PARCEL;

				if (lastIndexPoint < currentItinerary.size() - 1)
					if (T.currentTimePoint == currentItinerary
							.getArrivalTime(lastIndexPoint + 1)) {
						lastIndexPoint++;
						lastPoint = currentItinerary.get(lastIndexPoint);
						if (mStatus.get(lastIndexPoint) != null) {
							status = mStatus.get(lastIndexPoint);
						}
					}
			}
			/*
			 * remainTimeToNextDeparture--; if(remainTimeToNextDeparture == 0){
			 * //status =
			 * VehicleStatus.GOING_TO_DELIVERY_PEOPEL;//VehicleStatus.
			 * DELIVERY_PEOPLE;
			 * currentItinerary.setDepartureTime(lastIndexPoint,
			 * T.currentTimePoint); //System.out.println("Taxi " + ID +
			 * ", setDepartureTime(" + lastIndexPoint + "," +
			 * T.currentTimePoint); status =
			 * VehicleStatus.FINISHED_PICKUP_PARCEL; }
			 */
			return;
		}
		if (status == VehicleStatus.DELIVERY_PARCEL) {
			if (T.currentTimePoint == currentItinerary
					.getDepartureTime(lastIndexPoint)) {
				status = VehicleStatus.FINISHED_DELIVERY_PARCEL;

				if (lastIndexPoint < currentItinerary.size() - 1)
					if (T.currentTimePoint == currentItinerary
							.getArrivalTime(lastIndexPoint + 1)) {
						lastIndexPoint++;
						lastPoint = currentItinerary.get(lastIndexPoint);
						if (mStatus.get(lastIndexPoint) != null) {
							status = mStatus.get(lastIndexPoint);
						}
					}
			}
			/*
			 * remainTimeToNextDeparture--; if(remainTimeToNextDeparture == 0){
			 * //status =
			 * VehicleStatus.GOING_TO_DELIVERY_PEOPEL;//VehicleStatus.
			 * DELIVERY_PEOPLE;
			 * currentItinerary.setDepartureTime(lastIndexPoint,
			 * T.currentTimePoint); //System.out.println("Taxi " + ID +
			 * ", setDepartureTime(" + lastIndexPoint + "," +
			 * T.currentTimePoint); status =
			 * VehicleStatus.FINISHED_DELIVERY_PARCEL; }
			 */
			return;
		}
		/*
		 * if(status == VehicleStatus.REST_AT_PARKING){ Parking p =
		 * sim.findParking(lastPoint); if(p != null){ p.load--;
		 * p.lastUpdateTimePoint = T.currentTimePoint;
		 * System.out.println("Vehicle::move At " + T.currentTimePointHMS() +
		 * "[" + T.currentTimePoint + "], " + "parking " + p +
		 * " load decrease = " + p.load + ", capacity = " + p.capacity); } }
		 */
		// update data structure when we go to the next time point

		if (status == VehicleStatus.REST_AT_PARKING) {
			/*
			 * if(T.currentTimePoint > T.endrequest){ //status =
			 * VehicleStatus.NOT_WORK; status = VehicleStatus.STOP_WORK;
			 * log.println("At time point " + T.currentTimePoint + ", taxi[" +
			 * ID + "] --> STOP WOTKING"); }
			 */
			if (currentItinerary != null)
				if (T.currentTimePoint == currentItinerary
						.getDepartureTime(lastIndexPoint)) {
					status = changeStatus(lastIndexPoint);
				}
			return;

		}

		if (status == VehicleStatus.PREPARE_NEW_ITINERARY) {
			if (T.currentTimePoint == currentItinerary
					.getArrivalTime(lastIndexPoint + 1)) {
				cancelRemainItinerary();
				setNewItinerary();
			}
			/*
			 * remainTimeToNextPoint--; if(remainTimeToNextPoint == 0){
			 * lastIndexPoint++; lastPoint =
			 * currentItinerary.get(lastIndexPoint);
			 * currentItinerary.setArrivalTime(lastIndexPoint,
			 * T.currentTimePoint); //System.out.println("Taxi " + ID +
			 * ", setArrivalTime(" + lastIndexPoint + "," + T.currentTimePoint);
			 * cancelRemainItinerary(); setNewItinerary(); }
			 */
			return;
		}
		if (status == VehicleStatus.GO_BACK_DEPOT_FINISH_WORK) {
			// System.out.println("Vehicle::move, ID = " + ID +
			// ", status = GO_BACK_DEPOT_FINISH_WORK");
		}
		remainTimeToNextPoint--;
		// if(remainTimeToNextPoint == 0){
		// if(T.currentTimePoint ==
		// currentItinerary.getArrivalTime(lastIndexPoint+1)){
		if (ID == sim.debugTaxiID)
			log.println("Vehicle[" + ID + "]::move, T.current = "
					+ T.currentTimePoint + ", lastIndexPoint = "
					+ lastIndexPoint + ", lastPoint = " + lastPoint
					+ ", getArrivalTime(lastIndexPoint+1) = "
					+ currentItinerary.getArrivalTime(lastIndexPoint + 1)
					+ ", status = " + getStatusDescription(status));

		while (T.currentTimePoint == currentItinerary
				.getArrivalTime(lastIndexPoint + 1)) {
			int nextPoint = currentItinerary.get(lastIndexPoint + 1);
			Arc a0 = map.getArc(lastPoint, nextPoint);
			if (a0 != null) {
				remainDistance = remainDistance - a0.w;
			}

			int rid = currentItinerary.getRequestID(lastIndexPoint + 1);
			VehicleAction act = currentItinerary.getAction(lastIndexPoint + 1);
			if (rid > 0) {
				remainRequestIDs.remove(0);
				if (act == VehicleAction.PICKUP_PARCEL) {
					parcelReqIDonBoard.add(rid);
					if (sim.debugTaxiID == ID) {
						log.print(name() + "::move, pickup parcel " + rid
								+ ", parcels on board = ");
						for (int jj = 0; jj < parcelReqIDonBoard.size(); jj++)
							log.print(parcelReqIDonBoard.get(jj) + ",");
						log.print(" remainRequestIDs = " + getRemainRequestID());
						log.println();
					}
					sim.nbParcelOnBoard++;
					sim.nbParcelServed++;
					System.out.println("Vehicle[" + ID + "] At "
							+ T.currentTimePointHMS()
							+ ", pickupParcel, nbParcelServed = "
							+ sim.nbParcelServed + ", nbParlceOnBoard = "
							+ sim.nbParcelOnBoard);
				} else if (act == VehicleAction.DELIVERY_PARCEL) {
					if (sim.debugTaxiID == ID) {
						log.print(name() + "::move, delivery parcel " + rid
								+ ", parcels on board = ");
						for (int jj = 0; jj < parcelReqIDonBoard.size(); jj++)
							log.print(parcelReqIDonBoard.get(jj) + ",");
						log.print(" remainRequestIDs = " + getRemainRequestID());
						log.println();
					}
					int idx = parcelReqIDonBoard.indexOf(rid);
					if (idx < 0 && ID == sim.debugTaxiID) {
						log.println(name() + "::move, taxi " + ID
								+ ", action = delivery parcel " + rid
								+ ", EXCEPTION idx = " + idx);
						sim.log.close();
					}
					parcelReqIDonBoard.remove(idx);
					sim.nbParcelComplete++;
					sim.nbParcelOnBoard--;
					System.out.println("Vehicle[" + ID + "] At "
							+ T.currentTimePointHMS()
							+ ", deliveryParcel, nbParcelComplete = "
							+ sim.nbParcelComplete + ", nbParlceOnBoard = "
							+ sim.nbParcelOnBoard);
				} else if (act == VehicleAction.PICKUP_PEOPLE) {
					peopleReqIDonBoard.add(rid);
					if (sim.debugTaxiID == ID) {
						log.print(name() + "::move, pickup people " + rid
								+ ", people on board = ");
						for (int jj = 0; jj < peopleReqIDonBoard.size(); jj++)
							log.print(peopleReqIDonBoard.get(jj) + ",");
						log.print(" remainRequestIDs = " + getRemainRequestID());
						log.println();
					}
					sim.countStop.put(rid, 0);
					sim.accumulateDistance.put(rid, 0.0);
					sim.nbPeopleOnBoard++;
					sim.nbPeopleServed++;
					System.out.println("Vehicle[" + ID + "] At "
							+ T.currentTimePointHMS()
							+ ", pickupPeople, nbPeopleServed = "
							+ sim.nbPeopleServed + ", nbPeopleOnBoard = "
							+ sim.nbPeopleOnBoard);
				} else if (act == VehicleAction.DELIVERY_PEOPLE) {
					if (sim.debugTaxiID == ID) {
						log.print(name() + "::move, delivery people " + rid
								+ ", people on board = ");
						for (int jj = 0; jj < peopleReqIDonBoard.size(); jj++)
							log.print(peopleReqIDonBoard.get(jj) + ",");
						log.print(" remainRequestIDs = " + getRemainRequestID());
						log.println();
					}
					int idx = peopleReqIDonBoard.indexOf(rid);
					if (idx < 0 && ID == sim.debugTaxiID) {
						log.println(name() + "::move, taxi " + ID
								+ ", action = delivery people " + rid
								+ ", EXCEPTION idx = " + idx);
						sim.log.close();
					}
					peopleReqIDonBoard.remove(idx);
					sim.nbPeopleComplete++;
					sim.nbPeopleOnBoard--;
					System.out.println("Vehicle[" + ID + "] At "
							+ T.currentTimePointHMS()
							+ ", deliveryPeople, nbPeopleComplete = "
							+ sim.nbPeopleComplete + ", nbPeopleOnBoard = "
							+ sim.nbPeopleOnBoard);
				} else {
					System.out
							.println("Vehicle::move EXCEPTION unknown action?????");
					System.exit(-1);
				}

				// update stops of people requests
				for (int i = 0; i < peopleReqIDonBoard.size(); i++) {
					int r = peopleReqIDonBoard.get(i);
					if (r != rid) {
						sim.countStop.put(r, sim.countStop.get(r) + 1);

						if (a0 != null)
							sim.accumulateDistance.put(r,
									sim.accumulateDistance.get(r) + a0.w);
					}
				}
			}

			lastIndexPoint++;

			lastPoint = currentItinerary.get(lastIndexPoint);
			if (ID == sim.debugTaxiID)
				log.println("Vehicle[" + ID + "]::move, REACH T.current = "
						+ T.currentTimePoint + ", lastIndexPoint = "
						+ lastIndexPoint + ", lastPoint = " + lastPoint
						+ ", getArrivalTime(lastIndexPoint+1) = "
						+ currentItinerary.getArrivalTime(lastIndexPoint + 1)
						+ ", status = " + getStatusDescription(status));

			// currentItinerary.setArrivalTime(lastIndexPoint,
			// T.currentTimePoint);
			// System.out.println("Taxi " + ID + ", setArrivalTime(" +
			// lastIndexPoint + "," + T.currentTimePoint);

			String actionStr = "NULL";
			if (currentItinerary.getAction(lastIndexPoint) != null) {
				actionStr = getActionDescription(currentItinerary
						.getAction(lastIndexPoint));
			}
			if (currentItinerary.getAction(lastIndexPoint) == VehicleAction.PASS) {
				// currentItinerary.setDepartureTime(lastIndexPoint,
				// T.currentTimePoint);
			}
			// if(ID == 4){
			// System.out.println("Vehicle::move, lastIndexPoint = " +
			// lastIndexPoint + ", action = " + actionStr);
			// }
			// log.println("At timePoint " + T.currentTimePoint + ": taxi[" + ID
			// + "] changes lastPoint = " + lastPoint);
			// log.println("At " + T.currentTimePointHMS() + ", taxi[" + ID +
			// "] lastIndexPoint = " + lastIndexPoint + ", lastPoint = " +
			// lastPoint + ", status " + getStatusDescription(status));
			if (currentItinerary.size() > lastIndexPoint + 1) {
				int v = currentItinerary.get(lastIndexPoint + 1);
				Arc a = map.getArc(lastPoint, v);
				/*
				 * double speed = computeSpeed();// Simulator.maxSpeedms;
				 * //System.out.println("Vehicle::move --> speed = " + speed +
				 * ", maxSpeed = " + sim.maxSpeedms + ", minSpeed = " +
				 * sim.minSpeedms); remainTimeToNextPoint =
				 * Simulator.getTravelTime(a, speed);//Simulator.maxSpeedms);
				 * //currentItinerary.getTravelTime(lastIndexPoint);//a.t;
				 * if(remainTimeToNextPoint == 0) remainTimeToNextPoint = 1;
				 */
				if (a != null)
					totalTravelDistance += a.w;
				else {
					System.out.println("Vehicle::move, Arc (" + lastPoint + ","
							+ v + ") does not exists ????????????????");
					log.println("Vehicle[" + ID + "]::move, Arc (" + lastPoint
							+ "," + v + ") does not exists ????????????????");
				}
				// log.println("At " + T.currentTimePoint +
				// ", remainTimeToNextPoint = " + remainTimeToNextPoint);
			} else {
				// status = VehicleStatus.REST_AT_PARKING;
			}
			if (mStatus.get(lastIndexPoint) != null) {
				status = mStatus.get(lastIndexPoint);
				if (status == VehicleStatus.REST_AT_PARKING) {
					System.out.println("Vehicle[" + ID + "]::move, At "
							+ T.currentTimePointHMS() + "["
							+ T.currentTimePoint + "], " +

							"taxi[" + ID + "] REST AT PARKING");
				}
				log.println("Vehicle[" + ID + "]::move, At "
						+ T.currentTimePointHMS() + "[" + T.currentTimePoint
						+ "], " + "taxi[" + ID + "] changes status = "
						+ getStatusDescription(status) + " at point "
						+ lastPoint);

				if (currentItinerary.getAction(lastIndexPoint) == VehicleAction.FINISH_WORK) {
					System.out.println("Vehicle::move, ID = " + ID
							+ ", action = FINISH_WORK, status = "
							+ getStatusDescription(status));
					if (status != VehicleStatus.STOP_WORK) {
						System.out
								.println("Vehicle["
										+ ID
										+ "]::move, ID = "
										+ ID
										+ ", exception inconsistent, action = FINISH_WORK, but status != STOP_WORK");
						System.exit(-1);
					}
				}
				if (currentItinerary.getAction(lastIndexPoint) == VehicleAction.PICKUP_PEOPLE) {
					if (status != VehicleStatus.PICKUP_PEOPLE) {
						System.out.println("Vehicle::move, time = "
								+ T.currentTimePoint + " --> INCONSISTENT");
						System.exit(1);
					}
					PeopleRequest pr = sim.getPeopleRequest(currentItinerary
							.getRequestID(lastIndexPoint));
					remainTimeToNextDeparture = pr.pickupDuration;// 60;//
																	// default
																	// pickup
																	// time is
																	// 60s
					// if(currentItinerary.getRequestID(lastIndexPoint) == 242){
					// System.out.println("Vehicle[" + ID +
					// "]::move --> pickup people " + 242 + " at time point " +
					// T.currentTimePoint);
					// System.exit(-1);
					// }
				} else if (currentItinerary.getAction(lastIndexPoint) == VehicleAction.DELIVERY_PEOPLE) {
					if (status != VehicleStatus.DELIVERY_PEOPLE) {
						System.out.println("Vehicle::move, time = "
								+ T.currentTimePoint + " --> INCONSISTENT");
						System.exit(1);
					}
					PeopleRequest pr = sim.getPeopleRequest(currentItinerary
							.getRequestID(lastIndexPoint));
					remainTimeToNextDeparture = pr.deliveryDuration;// 60;//
																	// default
																	// delivery
																	// time is
																	// 60s
				} else if (currentItinerary.getAction(lastIndexPoint) == VehicleAction.PICKUP_PARCEL) {
					if (status != VehicleStatus.PICKUP_PARCEL) {
						System.out.println("Vehicle::move, time = "
								+ T.currentTimePoint + " --> INCONSISTENT");
						System.exit(1);
					}
					// System.out.println("Vehicle["+ ID +
					// "]::move, lastIndexPoint = " + lastIndexPoint +
					// ", lastIndex RequestID = " +
					// currentItinerary.getRequestID(lastIndexPoint) +
					// ", nextItinerary = " + nextItinerary2String());
					ParcelRequest pr = sim.getParcelRequest(currentItinerary
							.getRequestID(lastIndexPoint));
					remainTimeToNextDeparture = pr.pickupDuration;// 60;//
																	// default
																	// pickup
																	// time is
																	// 60s
				} else if (currentItinerary.getAction(lastIndexPoint) == VehicleAction.DELIVERY_PARCEL) {
					if (status != VehicleStatus.DELIVERY_PARCEL) {
						System.out.println("Vehicle::move, time = "
								+ T.currentTimePoint + " --> INCONSISTENT");
						System.exit(1);
					}
					ParcelRequest pr = sim.getParcelRequest(currentItinerary
							.getRequestID(lastIndexPoint));
					remainTimeToNextDeparture = pr.deliveryDuration;// 60;//
																	// default
																	// delivery
																	// time is
																	// 60s
				}
			}

		}
	}

	public int getNbrEngagedParcelRequestIDs() {
		if (currentItinerary == null)
			return 0;
		int count = parcelReqIDonBoard.size();
		for (int i = 0; i < remainRequestIDs.size(); i++) {
			int rid = remainRequestIDs.get(i);
			if (rid >= 0)
				if (sim.mParcelRequest.get(rid) != null)
					count++;
		}
		return count;

	}

	public String getPeopleOnBoards() {
		String s = "";
		for (int i = 0; i < peopleReqIDonBoard.size(); i++)
			s = s + peopleReqIDonBoard.get(i) + ",";
		s = s + ", sz = " + peopleReqIDonBoard.size();
		return s;
	}

	public String getParcelOnBoards() {
		String s = "";
		for (int i = 0; i < parcelReqIDonBoard.size(); i++)
			s = s + parcelReqIDonBoard.get(i) + ",";
		s = s + ", sz = " + parcelReqIDonBoard.size();
		return s;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
