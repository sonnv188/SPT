package pbts.simulation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import SARP2014.dynamicSARP;
import pbts.algorithms.SequenceOptimizer;
import pbts.entities.*;
import pbts.enums.VehicleAction;
import pbts.enums.VehicleStatus;
import pbts.shortestpaths.DijkstraBinaryHeap;
import pbts.onlinealgorithmtimeunit.*;

public class SimulatorTimeUnit extends Simulator {

	/**
	 * @param args
	 */

	public ArrayList<PeopleRequest> insertedPeopleRequests;
	public ArrayList<ParcelRequest> insertedParcelRequests;

	public ArrayList<PeopleRequest> queuePeopleReq;
	public ArrayList<ParcelRequest> queueParcelReq;

	public OnlinePlanner planner;

	public SimulatorTimeUnit() {
		super();

	}

	public void setPlanner(OnlinePlanner planner) {
		this.planner = planner;
		//statFilename = planner.name() + "-statistic.txt";
	}

	public TaxiTimePointIndex getFirstAvailableTaxiForPeople(PeopleRequest pr, double maxTime){
		double t0 = System.currentTimeMillis();
		for(int k = 0; k < vehicles.size(); k++){
			Vehicle taxi = vehicles.get(k);
			TaxiTimePointIndex tpi = availableTaxiWithTimePriority(taxi, pr);//availableTaxi(taxi, pr);
			if(tpi != null) return tpi;
			if((System.currentTimeMillis()-t0)*0.001 > maxTime) break;
		}
		return null;
	}
	
	/****[SonNV]
	 * Find nearest available taxi for people insertion.
	 * @param:
	 * 		pr			:		people request.
	 * 		maxTime		:		max time allowed finding best taxi for people insertion.
	 */
	public TaxiTimePointIndex getNearestAvailableTaxiForPeople(PeopleRequest pr, double maxTime){
		double t0 = System.currentTimeMillis();
		TaxiTimePointIndex sel_tpi = null;
		double minDis = 1000000000;
		for(int k = 0; k < vehicles.size(); k++){
			Vehicle taxi = vehicles.get(k);
			if (taxi.status == VehicleStatus.STOP_WORK)
				continue;
			if (taxi.totalTravelDistance > maxTravelDistance)
				continue;
			if (taxi.remainRequestIDs.size()+2 > maxPendingStops)// consider also pickup and delivery points of pr
				continue;
			TaxiTimePointIndex tpi = availableTaxiWithTimePriority(taxi, pr);
			if(tpi != null){
				if(tpi.estimation < minDis){
					sel_tpi = tpi;
					minDis = sel_tpi.estimation;
				}
			}
			if((System.currentTimeMillis()-t0)*0.001 > maxTime) break;
		}
		
		double t = System.currentTimeMillis() - t0;
		t = t * 0.001;
		if(t > maxTimeFindTaxiParcelInsertion) maxTimeFindTaxiParcelInsertion = t;
		return sel_tpi;
	}
	
	public TaxiTimePointIndex getNearestAvailableTaxiForPeopleSARP2014(SequenceOptimizer seqOptimizer, PeopleRequest pr, double maxTime){
		double t0 = System.currentTimeMillis();
		TaxiTimePointIndex sel_tpi = null;
		double minDis = 1000000000;
		boolean pendingTx = false;
		int nbTaxiNotRunning = 0;
		for(int k = 0; k < vehicles.size(); k++){
			Vehicle taxi = vehicles.get(k);
			if(taxi.pendingParcelReqs.size() == 12){
				pendingTx = true;
				nbTaxiNotRunning++;
			}
		}
		System.out.println("nbTaxi: " + vehicles.size() + ", not run: " + nbTaxiNotRunning);
		int runningTx = 0;
		for(int k = 0; k < vehicles.size(); k++){
			Vehicle taxi = vehicles.get(k);
			//if(taxi.remainRequestIDs.size() > 0){
			//	runningTx++;
				//continue;
			//}
			if(taxi.pendingParcelReqs.size() == 0 && taxi.remainRequestIDs.size() == 0 && pendingTx == true)
				continue;
			if (taxi.status == VehicleStatus.STOP_WORK)
				continue;
			if (taxi.totalTravelDistance > maxTravelDistance)
				continue;
			if (taxi.remainRequestIDs.size()+2 > maxPendingStops)// consider also pickup and delivery points of pr
				continue;
			TaxiTimePointIndex tpi = availableTaxiWithTimePrioriySARP2014(taxi, pr);
			if(tpi != null){
				if(tpi.estimation < minDis){
					sel_tpi = tpi;
					minDis = sel_tpi.estimation;
				}
			}
			if((System.currentTimeMillis()-t0)*0.001 > maxTime) break;
		}
		//System.out.println("running: " + runningTx);

		double t = System.currentTimeMillis() - t0;
		t = t * 0.001;
		if(t > maxTimeFindTaxiParcelInsertion) maxTimeFindTaxiParcelInsertion = t;
		return sel_tpi;
	}
	
	public TaxiTimePointIndex availableTaxiForPeople(Vehicle taxi, PeopleRequest pr) {
		// find delivery people location
		int locID = -1;
		int idxLocID = taxi.findLastIndexPeopleDeliveryRemainRequetsIDs();// -1;
		
		TimePointIndex tpi = taxi.getNextTimePointIndex(taxi.lastIndexPoint,
				T.currentTimePoint, TimePointDuration);
		if (taxi.ID == debugTaxiID) {
			System.out.println(name() + "::availableTaxi, taxi = " + taxi.ID
					+ ", pr = " + pr.id + ", T.currentTimePoint = "
					+ T.currentTimePoint + ", tpi = " + tpi.toString());
			log.println(name() + "::availableTaxi, taxi = " + taxi.ID
					+ ", pr = " + pr.id + ", T.currentTimePoint = "
					+ T.currentTimePoint + ", tpi = " + tpi.toString() + ", idxLocID = " + idxLocID + ", remainRequestIDs = " + Utility.arr2String(taxi.remainRequestIDs));
			//sim.exit();
		}
		//sim.exit();
		
		//String Is = "null";
		//if(taxi.currentItinerary != null) Is = taxi.currentItinerary.toString();
		//System.out.println(name() + "::availableTaxi, T.currentTimePoint = " + T.currentTimePoint + ", tpi = " + 
	//	tpi.toString() + ", taxi = " + taxi.ID + ", STATUS = " + taxi.requestStatus() + ", idxLocID = " + idxLocID + 
	//	", tpi.indexRemainRequestID = " + tpi.indexRemainRequestIDs + ", taxi.currentItinerary = " + 
	//			Is);
		idxLocID = idxLocID > tpi.indexRemainRequestIDs ? idxLocID : tpi.indexRemainRequestIDs;
		if(taxi.ID == debugTaxiID){
			log.println(name() + "::availableTaxi, idxLocID = " + idxLocID + ", tpi.indexRemainRequestIDs = " + tpi.indexRemainRequestIDs);
		}
		
		int timePoint = -1;
		int point = -1;
		int indexPoint = -1;
		int lastIndexPeopleDelivery = taxi.getLastIndexPeopleDeliveryItinerary(taxi.lastIndexPoint);
		if(taxi.ID == debugTaxiID){
			log.println(name() + "::availableTaxi lastIndexPeopleDelivery = " + lastIndexPeopleDelivery);
		}
		if(lastIndexPeopleDelivery >= 0){
			timePoint = taxi.currentItinerary.getDepartureTime(lastIndexPeopleDelivery);
			point = taxi.currentItinerary.get(lastIndexPeopleDelivery);
			indexPoint = lastIndexPeopleDelivery;
		
			if(taxi.ID == debugTaxiID)
			log.println(name() + "::availableTaxi taxi = " + taxi.ID + ", people request = " + pr.id + ", lastIndexPeopleDelivery = " + lastIndexPeopleDelivery + 
					" UPDATE timePoint = " + timePoint + ", point = " + point + ", indexPoint = " + indexPoint);
		}
		
		ArrayList<Integer> R = new ArrayList<Integer>();// new remain requestIDs
		ArrayList<Integer> KR = new ArrayList<Integer>();// kept remain request sequences
		idxLocID = idxLocID < taxi.remainRequestIDs.size()-1 ? idxLocID : taxi.remainRequestIDs.size()-1;
		if(taxi.ID ==	debugTaxiID){
			log.println(name() + "::availableTaxi, taxi = " + taxi.ID + ", peopleReq = " + pr.id + ", idxLocID = " + idxLocID + ", taxi.remainRequestIDs.size()  = " + 
		taxi.remainRequestIDs.size());
		}
		for(int i = 0; i <= idxLocID; i++)
			KR.add(taxi.remainRequestIDs.get(i));
		for(int i = idxLocID+1; i < taxi.remainRequestIDs.size(); i++)
			R.add(taxi.remainRequestIDs.get(i));
		if(taxi.ID == debugTaxiID){
			log.println(name() + "::availableTaxi, taxi = " + taxi.ID + ", peopleReq = " + pr.id + ", keep KR = " + Utility.arr2String(KR) + ", remain R = " + Utility.arr2String(R));
		}
		

		if (timePoint < tpi.timePoint) {
			timePoint = tpi.timePoint;
			point = tpi.point;
			indexPoint = tpi.indexPoint;
			if(taxi.ID == debugTaxiID){
				log.println(name() + "::availableTaxi, update point = " + point + ", indexPoint = " + indexPoint + ", timePoint = " + timePoint);
			}
		}

		/*
		LatLng pickup = sim.map.mLatLng.get(pr.pickupLocationID);
		LatLng ll = sim.map.mLatLng.get(point);
		 double d = sim.G.computeDistanceHaversine(ll.lat, ll.lng, pickup.lat, pickup.lng);
		 d = d * 1.5;// approximate factor
		 */
		
		//double d = sim.estimateTravelingDistance(point, pr.pickupLocationID);
		
		double d = dijkstra.queryDistance(point, pr.pickupLocationID);
		double t = getTravelTime(d, maxSpeedms);
		tpi = new TimePointIndex(timePoint, point, indexPoint);

		if(taxi.ID == debugTaxiID && pr.id == 184014){
			log.println(name() + "::availableTaxi CHECK DISTANCE --> taxi " + taxi.ID + ", people request " + pr.id + ", check distance from " +
		point + " to " + pr.pickupLocationID + ", distance = " + d + ", travel time = " + t + ", timePoint = " + timePoint);
			//sim.exit();
		}
		
		if (t + timePoint <= pr.latePickupTime)
			return new TaxiTimePointIndex(taxi, tpi, R, KR);

		return null;
	}

	public TaxiIndex findAppropriateTaxiForInsertion(ParcelRequest pr) {
		double minD = DijkstraBinaryHeap.infinity;
		Vehicle sel_taxi = null;
		int index = -1;
		int sel_index = -1;
		int taxiID = -1;
		for (int k = 0; k < nbTaxis; k++) {
			Vehicle tx = vehicles.get(k);
			int pos = -1;
			if (tx.status == VehicleStatus.REST_AT_PARKING) {
				pos = tx.lastPoint;
				index = -1;
			} else if (tx.status != VehicleStatus.STOP_WORK
					&& tx.status != VehicleStatus.GO_BACK_DEPOT_FINISH_WORK) {
				int idx = tx.currentItinerary
						.findLastDeliveryIndexPoint(tx.lastIndexPoint);
				if (tx.ID == taxiID) {
					System.out
							.println("SimulatorBookedRequests::findAppropriateTaxiForInsertion, taxi = 173, tx.lastIndexPoint = "
									+ tx.lastIndexPoint + ", idx = " + idx);
					log.println("SimulatorBookedRequests::findAppropriateTaxiForInsertion, taxi = 173, tx.lastIndexPoint = "
							+ tx.lastIndexPoint + ", idx = " + idx);
					if (idx > 0
							&& tx.currentItinerary.getDepartureTime(idx) < T.currentTimePoint) {
						log.println("SimulatorBookedRequests::findAppropriateTaxiForInsertion, taxi = 173, "
								+ ", BUG???????? tx.currentItinerary.getDepartureTime("
								+ idx
								+ ") = "
								+ tx.currentItinerary.getDepartureTime(idx)
								+ " < T.current = " + T.currentTimePoint);
					}
				}
				if (idx < 0) {
					if (tx.status == VehicleStatus.DELIVERY_PARCEL
							|| tx.status == VehicleStatus.DELIVERY_PEOPLE)
						idx = tx.lastIndexPoint;
					else
						idx = tx.lastIndexPoint + 1;
				}
				pos = tx.currentItinerary.get(idx);
				index = idx;
			}
			if (pos < 0)
				return null;
			double D = dijkstra.queryDistance(pos, pr.pickupLocationID);
			if (D < minD) {
				minD = D;
				sel_taxi = tx;
				sel_index = index;
			}
		}
		if (sel_taxi == null)
			return null;
		return new TaxiIndex(sel_taxi, sel_index);
	}

	/*
	 * public ArrayList<Integer> collectRemainingStops(Vehicle taxi){
	 * ArrayList<Integer> L = new ArrayList<Integer>(); ItineraryTravelTime I =
	 * taxi.currentItinerary; if(I != null) for(int i = taxi.lastIndexPoint+1; i
	 * < I.size(); i++){ int rid = I.getRequestID(i); VehicleAction act =
	 * I.getAction(i); if(act == VehicleAction.PICKUP_PARCEL){ ParcelRequest pri
	 * = mParcelRequest.get(rid); L.add(pri.pickupLocationID); }else if(act ==
	 * VehicleAction.DELIVERY_PARCEL){ ParcelRequest pri =
	 * mParcelRequest.get(rid); L.add(pri.deliveryLocationID); }else if(act ==
	 * VehicleAction.PICKUP_PEOPLE){ PeopleRequest pri =
	 * mPeopleRequest.get(rid); L.add(pri.pickupLocationID); }else if(act ==
	 * VehicleAction.DELIVERY_PEOPLE){ PeopleRequest pri =
	 * mPeopleRequest.get(rid); L.add(pri.deliveryLocationID); } } return L; }
	 */

	/*
	public ItineraryTravelTime establishItinerary(Vehicle taxi,
			int nextStartTimePoint, int fromIndex, 
			int fromPoint, ServiceSequence ss) {
		
		int reqIDAtFromPoint = -1;
		if (taxi.currentItinerary.size() > 0)
			reqIDAtFromPoint = taxi.currentItinerary.getRequestID(fromIndex);
		
		int taxiID = debugTaxiID;
		if (taxiID == taxi.ID) {
			log.println(name() + "::establishItinerary(DEBUG taxi = " + taxi.ID
					+ ", nextStartTimePoint = " + nextStartTimePoint
					+ ", fromPoint = " + fromPoint);
		}
		ItineraryTravelTime retI = new ItineraryTravelTime();

		int curPos = fromPoint;
		int td = nextStartTimePoint;

		// retI.addPoint(curPos);
		// retI.addAction(VehicleAction.PASS);
		// retI.addRequestID(-1);

		// taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1,
		// td);
		// retI.setDepartureTime(retI.size(), td);

		// int lp = taxi.currentItinerary.get(taxi.currentItinerary.size()-1);
		// int lp = retI.get(retI.size()-1);
		// if(taxiID == taxi.ID)
		// log.println("SimulatorBookedRequest::admitNewItinerary, taxi " +
		// taxi.ID + ", INIT setDepartureTime(" + lp + "," + td + ")" +
		// ", T.currnet = " + T.currentTimePoint);

		int startI = 0;
		int firstLoc = getLocationFromEncodedRequest(ss.rids[0]);
		// if(taxi.currentItinerary.get(taxi.currentItinerary.size()-1) ==
		// firstLoc &&
		// taxi.currentItinerary.getRequestID(taxi.currentItinerary.size()-1) ==
		// Math.abs(ss.rids[0]))
		if (firstLoc == fromPoint && reqIDAtFromPoint == Math.abs(ss.rids[0]))
			startI = 1;// fromPoint = first point of ss.rids then startI = 1 in
						// order to avoid repeating point
		for (int i = startI; i < ss.rids.length; i++) {
			// for(int i = 0; i < ss.rids.length; i++){
			int rid = ss.rids[i];
			int arid = Math.abs(rid);
			int nextPoint = -1;
			int duration = -1;
			int early = -1;
			int late = -1;
			PeopleRequest peopleReq = mPeopleRequest.get(arid);
			if (peopleReq != null) {
				if (rid > 0){
					nextPoint = peopleReq.pickupLocationID;
					duration = peopleReq.pickupDuration;
					early = peopleReq.earlyPickupTime; late = peopleReq.latePickupTime;
				}else{
					nextPoint = peopleReq.deliveryLocationID;
					duration = peopleReq.deliveryDuration;
					early = peopleReq.earlyDeliveryTime; late = peopleReq.lateDeliveryTime;
				}
				Itinerary I = dijkstra.queryShortestPath(curPos, nextPoint);
				int min_t = getTravelTime(I.getDistance(), maxSpeedms);
				int max_t = getTravelTime(I.getDistance(), minSpeedms);
				if(td + min_t > late || td + max_t < early){
					System.out.println(name() + "::establishItinerary, taxi " + taxi.ID + ", out of time windows of people, td = " + td + 
							", min_t = " + min_t + ", max_ t= " + max_t + ", early = " + early + ", late = " + 
							late + " --> RETURN NULL");
					return null;
				}
				
				int t = early < td + min_t ? td + min_t : early;// run as fast as possible but respect early time window
				t = t - td;
				
				double d = I.getDistance();
				int v0 = I.get(0);
				for (int j = 1; j < I.size() - 1; j++) {
					int v = I.get(j);

					// taxi.currentItinerary.addPoint(v);
					retI.addPoint(v);
					// taxi.currentItinerary.addAction(VehicleAction.PASS);
					retI.addAction(VehicleAction.PASS);
					// taxi.currentItinerary.addRequestID(-1);
					retI.addRequestID(-1);

					Arc a = map.getArc(v0, v);
					int dt = (int) (t * a.w / d);
					td = td + dt;
					t = t - dt;
					d = d - a.w;
					v0 = v;
					// taxi.currentItinerary.setArrivalTime(taxi.currentItinerary.size()-1,
					// td);
					retI.setArrivalTime(retI.size() - 1, td);
					// taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1,
					// td);
					retI.setDepartureTime(retI.size() - 1, td);
				}
				// taxi.currentItinerary.addPoint(I.get(I.size()-1));
				retI.addPoint(I.get(I.size() - 1));
				// taxi.currentItinerary.addRequestID(arid);
				retI.addRequestID(arid);

				td = td + t;
				// taxi.currentItinerary.setArrivalTime(taxi.currentItinerary.size()-1,
				// td);
				retI.setArrivalTime(retI.size() - 1, td);
				// lp =
				// taxi.currentItinerary.get(taxi.currentItinerary.size()-1);
				// lp = retI.get(retI.size()-1);

				// if(taxiID == taxi.ID)
				// log.println("SimulatorBookedRequest::admitNewItinerary, taxi "
				// + taxi.ID + ", setArrivalTime(" + lp + "," + td + ")" +
				// ", T.currnet = " + T.currentTimePoint);
				if (rid > 0) {
					// taxi.currentItinerary.addAction(VehicleAction.PICKUP_PEOPLE);
					retI.addAction(VehicleAction.PICKUP_PEOPLE);
					// taxi.mStatus.put(taxi.currentItinerary.size()-1,
					// VehicleStatus.PICKUP_PEOPLE);
					// taxi.mStatus.put(taxi.currentItinerary.size()-1,
					// VehicleStatus.PICKUP_PEOPLE);

					if (td > peopleReq.latePickupTime || td <
							 peopleReq.earlyPickupTime) return null;
							 
					td = td + peopleReq.pickupDuration;

					
					 

					// taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1,
					// td);
					retI.setDepartureTime(retI.size() - 1, td);
					// if(taxiID == taxi.ID)
					// log.println("SimulatorBookedRequest::admitNewItinerary, taxi "
					// + taxi.ID + ", setDepartureTime(" + lp + "," + td + ")" +
					// ", T.currnet = " + T.currentTimePoint);
				} else {
					// taxi.currentItinerary.addAction(VehicleAction.DELIVERY_PEOPLE);
					retI.addAction(VehicleAction.DELIVERY_PEOPLE);
					// taxi.mStatus.put(taxi.currentItinerary.size()-1,
					// VehicleStatus.DELIVERY_PEOPLE);

					
					if (td > peopleReq.lateDeliveryTime || td <
					 peopleReq.earlyDeliveryTime) return null;
					 

					td = td + peopleReq.deliveryDuration;
					// taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1,
					// td);
					retI.setDepartureTime(retI.size() - 1, td);
					// if(taxiID == taxi.ID)
					// log.println("SimulatorBookedRequest::admitNewItinerary, taxi "
					// + taxi.ID + ", setDepartureTime(" + lp + "," + td + ")" +
					// ", T.currnet = " + T.currentTimePoint);
				}
			} else {
				ParcelRequest parcelReq = mParcelRequest.get(arid);
				if (rid > 0){
					nextPoint = parcelReq.pickupLocationID;
					duration = parcelReq.pickupDuration;
					early = parcelReq.earlyPickupTime; late = parcelReq.latePickupTime;
				}else{
					nextPoint = parcelReq.deliveryLocationID;
					duration = parcelReq.deliveryDuration;
					early = parcelReq.earlyDeliveryTime; late = parcelReq.lateDeliveryTime;
				}
				Itinerary I = dijkstra.queryShortestPath(curPos, nextPoint);
				int min_t = getTravelTime(I.getDistance(), maxSpeedms);
				int max_t = getTravelTime(I.getDistance(), minSpeedms);
				
				if(td + min_t > late || td + max_t < early){
					System.out.println(name() + "::establishItinerary, taxi " + taxi.ID + ", out of time windows of parcel, td = " + td + 
							", min_t = " + min_t + ", max_ t= " + max_t + ", early = " + early + ", late = " + 
							late + " --> RETURN NULL");
					return null;
				}
				
				int t = early < td + min_t ? td + min_t : early;// run as fast as possible but respect early time window
				t = t - td;
				
				double d = I.getDistance();
				int v0 = I.get(0);
				for (int j = 1; j < I.size() - 1; j++) {
					int v = I.get(j);
					// taxi.currentItinerary.addPoint(v);
					retI.addPoint(v);
					// taxi.currentItinerary.addAction(VehicleAction.PASS);
					retI.addAction(VehicleAction.PASS);
					// taxi.currentItinerary.addRequestID(-1);
					retI.addRequestID(-1);

					Arc a = map.getArc(v0, v);
					int dt = (int) (t * a.w / d);
					td = td + dt;
					t = t - dt;
					d = d - a.w;
					v0 = v;
					// taxi.currentItinerary.setArrivalTime(taxi.currentItinerary.size()-1,
					// td);
					retI.setArrivalTime(retI.size() - 1, td);
					// taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1,
					// td);
					retI.setDepartureTime(retI.size() - 1, td);
				}
				// taxi.currentItinerary.addPoint(I.get(I.size()-1));
				retI.addPoint(I.get(I.size() - 1));
				// taxi.currentItinerary.addRequestID(arid);
				retI.addRequestID(arid);

				td = td + t;
				// taxi.currentItinerary.setArrivalTime(taxi.currentItinerary.size()-1,
				// td);
				retI.setArrivalTime(retI.size() - 1, td);

				if (rid > 0) {
					// taxi.currentItinerary.addAction(VehicleAction.PICKUP_PARCEL);
					retI.addAction(VehicleAction.PICKUP_PARCEL);
					// taxi.mStatus.put(taxi.currentItinerary.size()-1,
					// VehicleStatus.PICKUP_PARCEL);

					 if ((parcelReq.earlyPickupTime > td ||
					 parcelReq.latePickupTime < td) &&
					 (parcelReq.earlyPickupTime > td +
					 parcelReq.pickupDuration || parcelReq.latePickupTime < td
					 + parcelReq.pickupDuration)) return null;
					 

					td = td + parcelReq.pickupDuration;
					// taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1,
					// td);
					retI.setDepartureTime(retI.size() - 1, td);
				} else {
					// taxi.currentItinerary.addAction(VehicleAction.DELIVERY_PARCEL);
					retI.addAction(VehicleAction.DELIVERY_PARCEL);
					// taxi.mStatus.put(taxi.currentItinerary.size()-1,
					// VehicleStatus.DELIVERY_PARCEL);

					
					if ((parcelReq.earlyDeliveryTime > td ||
					 parcelReq.lateDeliveryTime < td) &&
					 (parcelReq.earlyDeliveryTime > td +
					 parcelReq.deliveryDuration || parcelReq.lateDeliveryTime
					 < td + parcelReq.deliveryDuration)) return null;
					 

					td = td + parcelReq.deliveryDuration;
					// taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1,
					// td);
					retI.setDepartureTime(retI.size() - 1, td);
				}
			}
			curPos = nextPoint;
		}
		Itinerary I = dijkstra.queryShortestPath(curPos,
				ss.parkingLocationPoint);
		int t = getTravelTime(I.getDistance(), maxSpeedms);
		double d = I.getDistance();
		int v0 = I.get(0);

		for (int j = 1; j < I.size() - 1; j++) {
			int v = I.get(j);
			// taxi.currentItinerary.addPoint(v);
			retI.addPoint(v);
			// taxi.currentItinerary.addAction(VehicleAction.PASS);
			retI.addAction(VehicleAction.PASS);
			// taxi.currentItinerary.addRequestID(-1);
			retI.addRequestID(-1);
			Arc a = map.getArc(v0, v);
			int dt = (int) (t * a.w / d);
			td = td + dt;
			t = t - dt;
			d = d - a.w;
			v0 = v;
			// taxi.currentItinerary.setArrivalTime(taxi.currentItinerary.size()-1,
			// td);
			retI.setArrivalTime(retI.size() - 1, td);
			// taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1,
			// td);
			retI.setDepartureTime(retI.size() - 1, td);
		}
		// taxi.currentItinerary.addPoint(I.get(I.size()-1));
		retI.addPoint(I.get(I.size() - 1));
		// taxi.currentItinerary.addRequestID(-1);
		retI.addRequestID(-1);
		// taxi.currentItinerary.addAction(VehicleAction.STOP);
		retI.addAction(VehicleAction.STOP);
		// taxi.mStatus.put(taxi.currentItinerary.size()-1,
		// VehicleStatus.REST_AT_PARKING);
		td = td + t;
		// taxi.currentItinerary.setArrivalTime(taxi.currentItinerary.size()-1,
		// td);
		retI.setArrivalTime(retI.size() - 1, td);
		
		return retI;
	}
	*/
	public String name() {
		return "SimulatorTimeUnit";
	}

	public void admitNewItinerary(Vehicle taxi, int nextStartTimePoint,
			int fromIndex, int fromPoint, ItineraryTravelTime I,
			ServiceSequence ss) {

		int taxiID = debugTaxiID;// 47;
		if (taxiID == taxi.ID) {
			String Is = "null";
			if (taxi.currentItinerary != null)
				Is = taxi.currentItinerary.toString();
			log.println(name() + "::admitNewItinerary, BEFORE admit taxi "
					+ taxi.ID + " DEBUG " + "curPos = lastPoint = "
					+ taxi.lastPoint + ", nextStartTimePoint = "
					+ nextStartTimePoint + ", fromPoint = " + fromPoint
					+ ", fromIndex = " + fromIndex + ", status = "
					+ taxi.getStatusDescription(taxi.status) + ", T.current = "
					+ T.currentTimePoint + ", currentItinerary = " + Is);
			System.out.println(name() + "::admitNewItinerary, taxi " + taxi.ID
					+ " DEBUG " + "curPos = lastPoint = " + taxi.lastPoint
					+ ", nextStartTimePoint = " + nextStartTimePoint
					+ ", fromPoint = " + fromPoint + ", fromIndex = "
					+ fromIndex + ", status = "
					+ taxi.getStatusDescription(taxi.status) + ", T.current = "
					+ T.currentTimePoint);
		}
		// if (taxi.status == VehicleStatus.REST_AT_PARKING) {
		if (taxi.currentItinerary.size() == 0) {// rest at parking
			if (taxi.ID == taxiID) {
				if (fromPoint == 10041) {
					// System.out.println(name() +
					// "::admitNewItinerary, T.current = " + T.currentTimePoint
					// + ", taxi " + taxi.ID + ", fromPoint = " + fromPoint +
					// ", taxi.currentItinerary.sz = 0 (rest at parking) + nextStartTime = "
					// + nextStartTimePoint);

				}
				// System.out.println(name() +
				// "::admitNewItinerary call to exit!!!");
				// exit();

				// System.out.println("SimulatorBookedRequest::insertParcelRequest, taxi 161 REST AT PARKING, curPos = lastPoint = "
				// + curPos);
				// log.println("SimulatorBookedRequest::insertParcelRequest, taxi "
				// + taxi.ID + " REST AT PARKING, " +
				// "curPos = lastPoint = " + taxi.lastPoint +
				// ", nextStartTimePoint = " + nextStartTimePoint +
				// ", fromPoint = " + fromPoint + ", fromIndex = " + fromIndex);
				// System.exit(-1);
			}
			taxi.mStatus.clear();
			taxi.mStatus.put(0, VehicleStatus.GOING_TO_PICKUP_PARCEL);
			taxi.status = VehicleStatus.GOING_TO_PICKUP_PARCEL;
			int[] path = new int[1];
			ArrayList<Integer> requestID = new ArrayList<Integer>();
			ArrayList<VehicleAction> actions = new ArrayList<VehicleAction>();
			path[0] = fromPoint;
			requestID.add(-1);
			actions.add(VehicleAction.PASS);
			// taxi.currentItinerary = new ItineraryTravelTime(path, requestID,
			// actions);
			taxi.currentItinerary.path.add(fromPoint);
			taxi.currentItinerary.requestID = requestID;
			taxi.currentItinerary.actions = actions;

			taxi.currentItinerary.setDepartureTime(
					taxi.currentItinerary.size() - 1, nextStartTimePoint);
			taxi.lastIndexPoint = 0;

			int la = taxi.getLastArrivalTime();// last arrival time of last
												// point of last itinerary
			// if(la >= 0){
			if (la > nextStartTimePoint) {
				log.println(name()
						+ "::admitNewItinerary FAILED???????, lastArrivalTime = "
						+ la + " > nextStartTime = " + nextStartTimePoint);
				System.out
						.println(name()
								+ "::admitNewItinerary FAILED????????, lastArrivalTime = "
								+ la + " > nextStartTime = "
								+ nextStartTimePoint);

				exit();
			}
			// }

			taxi.addItinerary(taxi.currentItinerary);

			if (taxi.ID == debugTaxiID)
				log.println(name() + "::admitNewItinerary, taxi " + taxi.ID
						+ " DEBUG --> REST AT PARKING "
						+ "curPos = lastPoint = " + taxi.lastPoint
						+ ", nextStartTimePoint = " + nextStartTimePoint
						+ ", fromPoint = " + fromPoint + ", fromIndex = "
						+ fromIndex + ", status = "
						+ taxi.getStatusDescription(taxi.status)
						+ ", T.current = " + T.currentTimePoint
						+ ", new currentItinerary, sz = 1, "
						+ ", departureTime at point 0 = " + fromPoint + " = "
						+ nextStartTimePoint);
		}

		pbts.entities.Parking P = taxi.getFinalParking();
		if (P != null) {
			P.load--;
		}

		/*
		 * If fromIndex is the last index of current itinerary and that last
		 * index is a parking, then departure time is preassigned = -1 -> set
		 * new departure time by nextStartTimePoint
		 */
		if (taxi.currentItinerary.getDepartureTime(fromIndex) < 0) {
			taxi.currentItinerary.setDepartureTime(fromIndex,
					nextStartTimePoint);
		}

		taxi.cancelSubItinerary(fromIndex + 1);

		ItineraryTravelTime CI = taxi.currentItinerary;
		for (int i = 0; i < I.size(); i++) {
			CI.addPoint(I.get(i));
			CI.addAction(I.getAction(i));
			CI.addRequestID(I.getRequestID(i));
			if (i < I.size() - 1)
				if (I.getArrivalTime(i) > I.getDepartureTime(i)) {
					System.out.println(name()
							+ "::admitItinerary EXCEPTION I.arrTime(" + i
							+ ") = " + I.getArrivalTime(i) + " > I.depTime("
							+ i + ") = " + I.getDepartureTime(i) + "	CI = "
							+ CI.toString() + ", CI.sz = " + CI.size() + "\n"
							+ " I(0," + i + ") = " + I.toString(0, i));

					System.out.println(name() + "::admitItinerary taxi "
							+ taxi.ID + ", I.arrTime(" + i + ") = "
							+ I.getArrivalTime(i) + " > I.deparTime(" + i
							+ ") = " + I.getDepartureTime(i));

					log.println(name() + "::admitItinerary EXCEPTION CI = "
							+ CI.toString() + ", CI.sz = " + CI.size() + "\n"
							+ " I(0," + i + ") = " + I.toString(0, i));

					log.println(name() + "::admitItinerary taxi " + taxi.ID
							+ ", I.arrTime(" + i + ") = " + I.getArrivalTime(i)
							+ " > I.deparTime(" + i + ") = "
							+ I.getDepartureTime(i));
					log.close();
					System.exit(-1);
				}
			if (CI.getDepartureTime(CI.size() - 2) > I.getArrivalTime(i)) {
				System.out.println(name() + "::admitItinerary EXCEPTION CI = "
						+ CI.toString() + ", CI.sz = " + CI.size() + "\n"
						+ " I(0," + i + ") = " + I.toString(0, i));

				int sz = CI.size() - 2;
				System.out.println(name() + "::admitItinerary taxi " + taxi.ID
						+ ", CI.departTime(" + sz + ") = "
						+ CI.getDepartureTime(CI.size() - 2) + " > I.arrTime("
						+ i + ") = " + I.getArrivalTime(i));

				log.println(name() + "::admitItinerary EXCEPTION CI = "
						+ CI.toString() + ", CI.sz = " + CI.size() + "\n"
						+ " I(0," + i + ") = " + I.toString(0, i));

				log.println(name() + "::admitItinerary taxi " + taxi.ID
						+ ", CI.departTime(" + sz + ") = "
						+ CI.getDepartureTime(CI.size() - 2) + " > I.arrTime("
						+ i + ") = " + I.getArrivalTime(i));

				log.close();
				System.exit(-1);
			}
			CI.setArrivalTime(CI.size() - 1, I.getArrivalTime(i));
			CI.setDepartureTime(CI.size() - 1, I.getDepartureTime(i));
			if (I.getAction(i) == VehicleAction.PICKUP_PARCEL)
				taxi.mStatus.put(CI.size() - 1, VehicleStatus.PICKUP_PARCEL);
			else if (I.getAction(i) == VehicleAction.DELIVERY_PARCEL)
				taxi.mStatus.put(CI.size() - 1, VehicleStatus.DELIVERY_PARCEL);
			else if (I.getAction(i) == VehicleAction.PICKUP_PEOPLE)
				taxi.mStatus.put(CI.size() - 1, VehicleStatus.PICKUP_PEOPLE);
			else if (I.getAction(i) == VehicleAction.DELIVERY_PEOPLE)
				taxi.mStatus.put(CI.size() - 1, VehicleStatus.DELIVERY_PEOPLE);
			else if (I.getAction(i) == VehicleAction.STOP)
				taxi.mStatus.put(CI.size() - 1, VehicleStatus.REST_AT_PARKING);

		}
		HashSet<Integer> S = new HashSet<Integer>();
		for (int i = 0; i < ss.rids.length; i++)
			S.add(ss.rids[i]);

		ArrayList<Integer> L = new ArrayList<Integer>();

		for (int i = 0; i < taxi.remainRequestIDs.size(); i++) {
			int rid = taxi.remainRequestIDs.get(i);
			if (!S.contains(rid))
				L.add(rid);
		}
		// taxi.remainRequestIDs = new ArrayList<Integer>();
		for (int i = 0; i < ss.rids.length; i++)
			L.add(ss.rids[i]);
		taxi.remainRequestIDs = L;
		taxi.remainDistance = ss.distance;

		P = taxi.getFinalParking();
		if (P != null) {
			P.load++;
		}

		if (DEBUG) {
			if (!taxi.checkRemainRequestConsistent(fromIndex + 1)) {
				System.out
						.println(name()
								+ "::admitNewItinerary checkRemainRequestConsistent FAILED?????");

				log.println(name() + "::admitNewItinerary taxi " + taxi.ID
						+ ", checkRemainRequestConsistent FAILED?????");
				log.println("remainRequestIDs = "
						+ Utility.arr2String(taxi.remainRequestIDs)
						+ ", currentItinerary = "
						+ taxi.currentItinerary.toString());
				log.println(name()
						+ "::admitNewItinerary checkRemainRequestConsistent FAILED?????");

				exit();
			}
		}
		
	}

	public void admitNewItineraryWithoutStatusSARP2014(Vehicle taxi,
			int nextStartTimePoint, int fromIndex, int fromPoint,
			ItineraryTravelTime I, ServiceSequence ss) {
		
		int taxiID = debugTaxiID;// 47;
		if (taxiID == taxi.ID) {
			String Is = "null";
			if (taxi.currentItinerary != null)
				Is = taxi.currentItinerary.toString();
			log.println(name() + "::admitNewItinerary, BEFORE admit taxi "
					+ taxi.ID + " DEBUG " + "curPos = lastPoint = "
					+ taxi.lastPoint + ", nextStartTimePoint = "
					+ nextStartTimePoint + ", fromPoint = " + fromPoint
					+ ", fromIndex = " + fromIndex + ", status = "
					+ taxi.getStatusDescription(taxi.status) + ", T.current = "
					+ T.currentTimePoint + ", currentItinerary = " + Is);
			System.out.println(name() + "::admitNewItinerary, taxi " + taxi.ID
					+ " DEBUG " + "curPos = lastPoint = " + taxi.lastPoint
					+ ", nextStartTimePoint = " + nextStartTimePoint
					+ ", fromPoint = " + fromPoint + ", fromIndex = "
					+ fromIndex + ", status = "
					+ taxi.getStatusDescription(taxi.status) + ", T.current = "
					+ T.currentTimePoint);
		}
		// if (taxi.status == VehicleStatus.REST_AT_PARKING) {
		// if(taxi.currentItinerary == null){
		// System.out.println("currentItinerary NULL");
		// }
		if (taxi.currentItinerary.size() == 0) {// rest at parking
			if (taxi.ID == taxiID) {
				// System.out.println("SimulatorBookedRequest::insertParcelRequest, taxi 161 REST AT PARKING, curPos = lastPoint = "
				// + curPos);
				// log.println("SimulatorBookedRequest::insertParcelRequest, taxi "
				// + taxi.ID + " REST AT PARKING, " +
				// "curPos = lastPoint = " + taxi.lastPoint +
				// ", nextStartTimePoint = " + nextStartTimePoint +
				// ", fromPoint = " + fromPoint + ", fromIndex = " + fromIndex);
				// System.exit(-1);
			}
			taxi.mStatus.clear();
			taxi.mStatus.put(0, VehicleStatus.GOING_TO_PICKUP_PARCEL);
			taxi.status = VehicleStatus.GOING_TO_PICKUP_PARCEL;
			// int[] path = new int[1];
			ArrayList<Integer> path = new ArrayList<Integer>();
			ArrayList<Integer> requestID = new ArrayList<Integer>();
			ArrayList<VehicleAction> actions = new ArrayList<VehicleAction>();
			// path[0] = fromPoint;
			path.add(fromPoint);
			requestID.add(-1);
			actions.add(VehicleAction.PASS);
			// taxi.currentItinerary = new ItineraryTravelTime(path, requestID,
			// actions);
			taxi.currentItinerary.path = path;
			taxi.currentItinerary.requestID = requestID;
			taxi.currentItinerary.actions = actions;
			taxi.currentItinerary.setDepartureTime(
					taxi.currentItinerary.size() - 1, nextStartTimePoint);
			taxi.lastIndexPoint = 0;

			taxi.addItinerary(taxi.currentItinerary);

			if (taxi.ID == debugTaxiID)
				log.println(name() + "::admitNewItinerary, taxi " + taxi.ID
						+ " DEBUG --> REST AT PARKING "
						+ "curPos = lastPoint = " + taxi.lastPoint
						+ ", nextStartTimePoint = " + nextStartTimePoint
						+ ", fromPoint = " + fromPoint + ", fromIndex = "
						+ fromIndex + ", status = "
						+ taxi.getStatusDescription(taxi.status)
						+ ", T.current = " + T.currentTimePoint
						+ ", new currentItinerary, sz = 1, "
						+ ", departureTime at point 0 = " + fromPoint + " = "
						+ nextStartTimePoint);
		}

		pbts.entities.Parking P = taxi.getFinalParking();
		if (P != null) {
			P.load--;
			P.nTaxisDeparture++;
		}

		/*
		 * If fromIndex is the last index of current itinerary and that last
		 * index is a parking, then departure time is preassigned = -1 -> set
		 * new departure time by nextStartTimePoint
		 */
		if (taxi.currentItinerary.getDepartureTime(fromIndex) < 0) {
			taxi.currentItinerary.setDepartureTime(fromIndex,
					nextStartTimePoint);
		}

		// store first request point from fromIndex
		int firstReqID = taxi.findFirstRequestID(fromIndex);
		
		taxi.cancelSubItinerary(fromIndex + 1);

		ItineraryTravelTime CI = taxi.currentItinerary;
		for (int i = 0; i < I.size(); i++) {
			CI.addPoint(I.get(i));
			CI.addAction(I.getAction(i));
			CI.addRequestID(I.getRequestID(i));
			if (i < I.size() - 1)
				if (I.getArrivalTime(i) > I.getDepartureTime(i)) { //[SonNV] don't understand: arrival time i > departure time i (i+1)?
					System.out.println(name()
							+ "::admitItinerary EXCEPTION I.arrTime(" + i
							+ ") = " + I.getArrivalTime(i) + " > I.depTime("
							+ i + ") = " + I.getDepartureTime(i) + "	CI = "
							+ CI.toString() + ", CI.sz = " + CI.size() + "\n"
							+ " I(0," + i + ") = " + I.toString(0, i));

					System.out.println(name() + "::admitItinerary taxi "
							+ taxi.ID + ", I.arrTime(" + i + ") = "
							+ I.getArrivalTime(i) + " > I.deparTime(" + i
							+ ") = " + I.getDepartureTime(i));

					log.println(name() + "::admitItinerary EXCEPTION CI = "
							+ CI.toString() + ", CI.sz = " + CI.size() + "\n"
							+ " I(0," + i + ") = " + I.toString(0, i));

					log.println(name() + "::admitItinerary taxi " + taxi.ID
							+ ", I.arrTime(" + i + ") = " + I.getArrivalTime(i)
							+ " > I.deparTime(" + i + ") = "
							+ I.getDepartureTime(i));
					log.close();
					System.exit(-1);
				}
			if (CI.getDepartureTime(CI.size() - 2) > I.getArrivalTime(i)) {
				System.out.println(name() + "::admitItinerary EXCEPTION CI = "
						+ CI.toString() + ", CI.sz = " + CI.size() + "\n"
						+ " I(0," + i + ") = " + I.toString(0, i));

				int sz = CI.size() - 2;
				System.out.println(name() + "::admitItinerary taxi " + taxi.ID
						+ ", CI.departTime(" + sz + ") = "
						+ CI.getDepartureTime(CI.size() - 2) + " > I.arrTime("
						+ i + ") = " + I.getArrivalTime(i));

				log.println(name() + "::admitItinerary EXCEPTION CI = "
						+ CI.toString() + ", CI.sz = " + CI.size() + "\n"
						+ " I(0," + i + ") = " + I.toString(0, i));

				log.println(name() + "::admitItinerary taxi " + taxi.ID
						+ ", CI.departTime(" + sz + ") = "
						+ CI.getDepartureTime(CI.size() - 2) + " > I.arrTime("
						+ i + ") = " + I.getArrivalTime(i));

				log.close();
				System.exit(-1);
			}
			CI.setArrivalTime(CI.size() - 1, I.getArrivalTime(i));
			CI.setDepartureTime(CI.size() - 1, I.getDepartureTime(i));
			if (I.getAction(i) == VehicleAction.PICKUP_PARCEL){
				taxi.mStatus.put(CI.size() - 1, VehicleStatus.PICKUP_PARCEL);
				int idx = CI.size()-1;
				int rid = CI.getRequestID(idx);
				taxi.mRequestID2Index.put(rid,idx);
				log.println(name() + "::admitItineraryTravelTime taxi = " + taxi.ID + " -> mRequestID2Index.put(" + rid + "," + idx + ")");
			}else if (I.getAction(i) == VehicleAction.DELIVERY_PARCEL){
				taxi.mStatus.put(CI.size() - 1, VehicleStatus.DELIVERY_PARCEL);
				int idx = CI.size()-1;
				int rid = -CI.getRequestID(idx);
				taxi.mRequestID2Index.put(rid,idx);
				log.println(name() + "::admitItineraryTravelTime taxi = " + taxi.ID + " -> mRequestID2Index.put(" + rid + "," + idx + ")");
			}else if (I.getAction(i) == VehicleAction.PICKUP_PEOPLE){
				taxi.mStatus.put(CI.size() - 1, VehicleStatus.PICKUP_PEOPLE);
				int idx = CI.size()-1;
				int rid = CI.getRequestID(idx);
				taxi.mRequestID2Index.put(rid,idx);
				log.println(name() + "::admitItineraryTravelTime taxi = " + taxi.ID + "  -> mRequestID2Index.put(" + rid + "," + idx + ")");
			}else if (I.getAction(i) == VehicleAction.DELIVERY_PEOPLE){
				taxi.mStatus.put(CI.size() - 1, VehicleStatus.DELIVERY_PEOPLE);
				int idx = CI.size()-1;
				int rid = -CI.getRequestID(idx);
				taxi.mRequestID2Index.put(rid,idx);
				log.println(name() + "::admitItineraryTravelTime taxi = " + taxi.ID + "  -> mRequestID2Index.put(" + rid + "," + idx + ")");
			}else if (I.getAction(i) == VehicleAction.STOP){
				taxi.mStatus.put(CI.size() - 1, VehicleStatus.REST_AT_PARKING);
			}
		}
		
		ArrayList<Integer> L = new ArrayList<Integer>();
		for(int i= 0; i < taxi.remainRequestIDs.size(); i++){
			int rid = taxi.remainRequestIDs.get(i);
			if(rid == firstReqID) break;
			L.add(rid);
		}
		for (int i = 0; i < ss.rids.length; i++)
			L.add(ss.rids[i]);
		
		/*
		HashSet<Integer> S = new HashSet<Integer>();
		for (int i = 0; i < ss.rids.length; i++)
			S.add(ss.rids[i]);

		ArrayList<Integer> L = new ArrayList<Integer>();

		for (int i = 0; i < taxi.remainRequestIDs.size(); i++) {
			int rid = taxi.remainRequestIDs.get(i);
			if (!S.contains(rid))
				L.add(rid);
		}
		// taxi.remainRequestIDs = new ArrayList<Integer>();
		for (int i = 0; i < ss.rids.length; i++)
			L.add(ss.rids[i]);
		
		*/
		taxi.remainRequestIDs = L;
		taxi.remainDistance = ss.distance;

		P = taxi.getFinalParking();
		if (P != null) {
			P.load++;
		}

		taxi.establishCompletePickupDeliveryPoints();
		//updatePendingParcelReqsSARP2014(taxi);
	}
	
	public void admitNewItineraryWithoutStatus(Vehicle taxi,
			int nextStartTimePoint, int fromIndex, int fromPoint,
			ItineraryTravelTime I, ServiceSequence ss) {
		
		int taxiID = debugTaxiID;// 47;
		if (taxiID == taxi.ID) {
			String Is = "null";
			if (taxi.currentItinerary != null)
				Is = taxi.currentItinerary.toString();
			log.println(name() + "::admitNewItinerary, BEFORE admit taxi "
					+ taxi.ID + " DEBUG " + "curPos = lastPoint = "
					+ taxi.lastPoint + ", nextStartTimePoint = "
					+ nextStartTimePoint + ", fromPoint = " + fromPoint
					+ ", fromIndex = " + fromIndex + ", status = "
					+ taxi.getStatusDescription(taxi.status) + ", T.current = "
					+ T.currentTimePoint + ", currentItinerary = " + Is);
			System.out.println(name() + "::admitNewItinerary, taxi " + taxi.ID
					+ " DEBUG " + "curPos = lastPoint = " + taxi.lastPoint
					+ ", nextStartTimePoint = " + nextStartTimePoint
					+ ", fromPoint = " + fromPoint + ", fromIndex = "
					+ fromIndex + ", status = "
					+ taxi.getStatusDescription(taxi.status) + ", T.current = "
					+ T.currentTimePoint);
		}
		// if (taxi.status == VehicleStatus.REST_AT_PARKING) {
		// if(taxi.currentItinerary == null){
		// System.out.println("currentItinerary NULL");
		// }
		if (taxi.currentItinerary.size() == 0) {// rest at parking
			if (taxi.ID == taxiID) {
				// System.out.println("SimulatorBookedRequest::insertParcelRequest, taxi 161 REST AT PARKING, curPos = lastPoint = "
				// + curPos);
				// log.println("SimulatorBookedRequest::insertParcelRequest, taxi "
				// + taxi.ID + " REST AT PARKING, " +
				// "curPos = lastPoint = " + taxi.lastPoint +
				// ", nextStartTimePoint = " + nextStartTimePoint +
				// ", fromPoint = " + fromPoint + ", fromIndex = " + fromIndex);
				// System.exit(-1);
			}
			taxi.mStatus.clear();
			taxi.mStatus.put(0, VehicleStatus.GOING_TO_PICKUP_PARCEL);
			taxi.status = VehicleStatus.GOING_TO_PICKUP_PARCEL;
			// int[] path = new int[1];
			ArrayList<Integer> path = new ArrayList<Integer>();
			ArrayList<Integer> requestID = new ArrayList<Integer>();
			ArrayList<VehicleAction> actions = new ArrayList<VehicleAction>();
			// path[0] = fromPoint;
			path.add(fromPoint);
			requestID.add(-1);
			actions.add(VehicleAction.PASS);
			// taxi.currentItinerary = new ItineraryTravelTime(path, requestID,
			// actions);
			taxi.currentItinerary.path = path;
			taxi.currentItinerary.requestID = requestID;
			taxi.currentItinerary.actions = actions;
			taxi.currentItinerary.setDepartureTime(
					taxi.currentItinerary.size() - 1, nextStartTimePoint);
			taxi.lastIndexPoint = 0;

			taxi.addItinerary(taxi.currentItinerary);

			if (taxi.ID == debugTaxiID)
				log.println(name() + "::admitNewItinerary, taxi " + taxi.ID
						+ " DEBUG --> REST AT PARKING "
						+ "curPos = lastPoint = " + taxi.lastPoint
						+ ", nextStartTimePoint = " + nextStartTimePoint
						+ ", fromPoint = " + fromPoint + ", fromIndex = "
						+ fromIndex + ", status = "
						+ taxi.getStatusDescription(taxi.status)
						+ ", T.current = " + T.currentTimePoint
						+ ", new currentItinerary, sz = 1, "
						+ ", departureTime at point 0 = " + fromPoint + " = "
						+ nextStartTimePoint);
		}

		pbts.entities.Parking P = taxi.getFinalParking();
		if (P != null) {
			P.load--;
			P.nTaxisDeparture++;
		}

		/*
		 * If fromIndex is the last index of current itinerary and that last
		 * index is a parking, then departure time is preassigned = -1 -> set
		 * new departure time by nextStartTimePoint
		 */
		if (taxi.currentItinerary.getDepartureTime(fromIndex) < 0) {
			taxi.currentItinerary.setDepartureTime(fromIndex,
					nextStartTimePoint);
		}

		// store first request point from fromIndex
		int firstReqID = taxi.findFirstRequestID(fromIndex);
		
		taxi.cancelSubItinerary(fromIndex + 1);

		ItineraryTravelTime CI = taxi.currentItinerary;
		for (int i = 0; i < I.size(); i++) {
			CI.addPoint(I.get(i));
			CI.addAction(I.getAction(i));
			CI.addRequestID(I.getRequestID(i));
			if (i < I.size() - 1)
				if (I.getArrivalTime(i) > I.getDepartureTime(i)) { //[SonNV] don't understand: arrival time i > departure time i (i+1)?
					System.out.println(name()
							+ "::admitItinerary EXCEPTION I.arrTime(" + i
							+ ") = " + I.getArrivalTime(i) + " > I.depTime("
							+ i + ") = " + I.getDepartureTime(i) + "	CI = "
							+ CI.toString() + ", CI.sz = " + CI.size() + "\n"
							+ " I(0," + i + ") = " + I.toString(0, i));

					System.out.println(name() + "::admitItinerary taxi "
							+ taxi.ID + ", I.arrTime(" + i + ") = "
							+ I.getArrivalTime(i) + " > I.deparTime(" + i
							+ ") = " + I.getDepartureTime(i));

					log.println(name() + "::admitItinerary EXCEPTION CI = "
							+ CI.toString() + ", CI.sz = " + CI.size() + "\n"
							+ " I(0," + i + ") = " + I.toString(0, i));

					log.println(name() + "::admitItinerary taxi " + taxi.ID
							+ ", I.arrTime(" + i + ") = " + I.getArrivalTime(i)
							+ " > I.deparTime(" + i + ") = "
							+ I.getDepartureTime(i));
					log.close();
					System.exit(-1);
				}
			if (CI.getDepartureTime(CI.size() - 2) > I.getArrivalTime(i)) {
				System.out.println(name() + "::admitItinerary EXCEPTION CI = "
						+ CI.toString() + ", CI.sz = " + CI.size() + "\n"
						+ " I(0," + i + ") = " + I.toString(0, i));

				int sz = CI.size() - 2;
				System.out.println(name() + "::admitItinerary taxi " + taxi.ID
						+ ", CI.departTime(" + sz + ") = "
						+ CI.getDepartureTime(CI.size() - 2) + " > I.arrTime("
						+ i + ") = " + I.getArrivalTime(i));

				log.println(name() + "::admitItinerary EXCEPTION CI = "
						+ CI.toString() + ", CI.sz = " + CI.size() + "\n"
						+ " I(0," + i + ") = " + I.toString(0, i));

				log.println(name() + "::admitItinerary taxi " + taxi.ID
						+ ", CI.departTime(" + sz + ") = "
						+ CI.getDepartureTime(CI.size() - 2) + " > I.arrTime("
						+ i + ") = " + I.getArrivalTime(i));

				log.close();
				System.exit(-1);
			}
			CI.setArrivalTime(CI.size() - 1, I.getArrivalTime(i));
			CI.setDepartureTime(CI.size() - 1, I.getDepartureTime(i));
			if (I.getAction(i) == VehicleAction.PICKUP_PARCEL){
				taxi.mStatus.put(CI.size() - 1, VehicleStatus.PICKUP_PARCEL);
				int idx = CI.size()-1;
				int rid = CI.getRequestID(idx);
				taxi.mRequestID2Index.put(rid,idx);
				log.println(name() + "::admitItineraryTravelTime taxi = " + taxi.ID + " -> mRequestID2Index.put(" + rid + "," + idx + ")");
			}else if (I.getAction(i) == VehicleAction.DELIVERY_PARCEL){
				taxi.mStatus.put(CI.size() - 1, VehicleStatus.DELIVERY_PARCEL);
				int idx = CI.size()-1;
				int rid = -CI.getRequestID(idx);
				taxi.mRequestID2Index.put(rid,idx);
				log.println(name() + "::admitItineraryTravelTime taxi = " + taxi.ID + " -> mRequestID2Index.put(" + rid + "," + idx + ")");
			}else if (I.getAction(i) == VehicleAction.PICKUP_PEOPLE){
				taxi.mStatus.put(CI.size() - 1, VehicleStatus.PICKUP_PEOPLE);
				int idx = CI.size()-1;
				int rid = CI.getRequestID(idx);
				taxi.mRequestID2Index.put(rid,idx);
				log.println(name() + "::admitItineraryTravelTime taxi = " + taxi.ID + "  -> mRequestID2Index.put(" + rid + "," + idx + ")");
			}else if (I.getAction(i) == VehicleAction.DELIVERY_PEOPLE){
				taxi.mStatus.put(CI.size() - 1, VehicleStatus.DELIVERY_PEOPLE);
				int idx = CI.size()-1;
				int rid = -CI.getRequestID(idx);
				taxi.mRequestID2Index.put(rid,idx);
				log.println(name() + "::admitItineraryTravelTime taxi = " + taxi.ID + "  -> mRequestID2Index.put(" + rid + "," + idx + ")");
			}else if (I.getAction(i) == VehicleAction.STOP){
				taxi.mStatus.put(CI.size() - 1, VehicleStatus.REST_AT_PARKING);
			}
		}
		
		ArrayList<Integer> L = new ArrayList<Integer>();
		for(int i= 0; i < taxi.remainRequestIDs.size(); i++){
			int rid = taxi.remainRequestIDs.get(i);
			if(rid == firstReqID) break;
			L.add(rid);
		}
		for (int i = 0; i < ss.rids.length; i++)
			L.add(ss.rids[i]);
		
		/*
		HashSet<Integer> S = new HashSet<Integer>();
		for (int i = 0; i < ss.rids.length; i++)
			S.add(ss.rids[i]);

		ArrayList<Integer> L = new ArrayList<Integer>();

		for (int i = 0; i < taxi.remainRequestIDs.size(); i++) {
			int rid = taxi.remainRequestIDs.get(i);
			if (!S.contains(rid))
				L.add(rid);
		}
		// taxi.remainRequestIDs = new ArrayList<Integer>();
		for (int i = 0; i < ss.rids.length; i++)
			L.add(ss.rids[i]);
		
		*/
		taxi.remainRequestIDs = L;
		taxi.remainDistance = ss.distance;

		P = taxi.getFinalParking();
		if (P != null) {
			P.load++;
		}

		taxi.establishCompletePickupDeliveryPoints();
	}

	public boolean checkCapacity(int parcelsOnBoard, int[] rids, int Q) {
		int l = parcelsOnBoard;
		if (l > Q)
			return false;
		for (int i = 0; i < rids.length; i++) {
			if (rids[i] > 0)
				l++;
			else
				l--;
			if (l > Q)
				return false;
		}
		return true;
	}

	public void admitNewItinerary(Vehicle taxi, int nextStartTimePoint,
			int fromIndex, int fromPoint, ServiceSequence ss) {
		// System.out.println(name() + "::admitNewItinerary, taxi[" + taxi.ID +
		// "] status = " + taxi.getStatusDescription());
		// if(taxi.currentItinerary == null){
		// System.out.println(name() + "::admitNewItinerary, taxi[" + taxi.ID +
		// "] status = " +
		// taxi.getStatusDescription() + ", currentItinerary = NULL?????");
		// }
		if (ss == null)
			return;
		int taxiID = 1;
		if (taxiID == taxi.ID)
			log.println(name() + "::admitNewItinerary, taxi " + taxi.ID
					+ " REST AT PARKING, " + "curPos = lastPoint = "
					+ taxi.lastPoint + ", nextStartTimePoint = "
					+ nextStartTimePoint + ", fromPoint = " + fromPoint
					+ ", fromIndex = " + fromIndex + ", status = "
					+ taxi.getStatusDescription(taxi.status) + ", T.current = "
					+ T.currentTimePoint);
		if (taxi.status == VehicleStatus.REST_AT_PARKING) {
			if (taxi.ID == taxi.ID) {
				// System.out.println("SimulatorBookedRequest::insertParcelRequest, taxi 161 REST AT PARKING, curPos = lastPoint = "
				// + curPos);
				// log.println("SimulatorBookedRequest::insertParcelRequest, taxi "
				// + taxi.ID + " REST AT PARKING, " +
				// "curPos = lastPoint = " + taxi.lastPoint +
				// ", nextStartTimePoint = " + nextStartTimePoint +
				// ", fromPoint = " + fromPoint + ", fromIndex = " + fromIndex);
				// System.exit(-1);
			}
			taxi.mStatus.clear();
			taxi.mStatus.put(0, VehicleStatus.GOING_TO_PICKUP_PARCEL);
			taxi.status = VehicleStatus.GOING_TO_PICKUP_PARCEL;
			int[] path = new int[1];
			ArrayList<Integer> requestID = new ArrayList<Integer>();
			ArrayList<VehicleAction> actions = new ArrayList<VehicleAction>();
			path[0] = fromPoint;
			requestID.add(-1);
			actions.add(VehicleAction.PASS);
			taxi.currentItinerary = new ItineraryTravelTime(path, requestID,
					actions);

			taxi.lastIndexPoint = 0;

			taxi.addItinerary(taxi.currentItinerary);
		}

		pbts.entities.Parking P = taxi.getFinalParking();
		if (P != null) {
			P.load--;
		}
		taxi.cancelSubItinerary(fromIndex + 1);

		int curPos = fromPoint;
		int td = nextStartTimePoint;
		taxi.currentItinerary.setDepartureTime(
				taxi.currentItinerary.size() - 1, td);
		int lp = taxi.currentItinerary.get(taxi.currentItinerary.size() - 1);
		if (taxiID == taxi.ID)
			log.println(name() + "::admitNewItinerary, taxi " + taxi.ID
					+ ", INIT setDepartureTime(" + lp + "," + td + ")"
					+ ", T.currnet = " + T.currentTimePoint);

		int startI = 0;
		int firstLoc = getLocationFromEncodedRequest(ss.rids[0]);
		if (taxi.currentItinerary.get(taxi.currentItinerary.size() - 1) == firstLoc
				&& taxi.currentItinerary.getRequestID(taxi.currentItinerary
						.size() - 1) == Math.abs(ss.rids[0]))
			startI = 1;// fromPoint = first point of ss.rids then startI = 1 in
						// order to avoid repeating point
		for (int i = startI; i < ss.rids.length; i++) {
			// for(int i = 0; i < ss.rids.length; i++){
			int rid = ss.rids[i];
			int arid = Math.abs(rid);
			int nextPoint = -1;
			PeopleRequest peopleReq = mPeopleRequest.get(arid);
			if (peopleReq != null) {
				if (rid > 0)
					nextPoint = peopleReq.pickupLocationID;
				else
					nextPoint = peopleReq.deliveryLocationID;
				Itinerary I = dijkstra.queryShortestPath(curPos, nextPoint);
				int t = getTravelTime(I.getDistance(), maxSpeedms);
				double d = I.getDistance();
				int v0 = I.get(0);
				for (int j = 1; j < I.size() - 1; j++) {
					int v = I.get(j);

					taxi.currentItinerary.addPoint(v);
					taxi.currentItinerary.addAction(VehicleAction.PASS);
					taxi.currentItinerary.addRequestID(-1);

					Arc a = map.getArc(v0, v);
					int dt = (int) (t * a.w / d);
					td = td + dt;
					t = t - dt;
					d = d - a.w;
					v0 = v;
					taxi.currentItinerary.setArrivalTime(
							taxi.currentItinerary.size() - 1, td);
					taxi.currentItinerary.setDepartureTime(
							taxi.currentItinerary.size() - 1, td);
				}
				taxi.currentItinerary.addPoint(I.get(I.size() - 1));
				taxi.currentItinerary.addRequestID(arid);
				td = td + t;
				taxi.currentItinerary.setArrivalTime(
						taxi.currentItinerary.size() - 1, td);
				lp = taxi.currentItinerary
						.get(taxi.currentItinerary.size() - 1);
				if (taxiID == taxi.ID)
					log.println(name() + "::admitNewItinerary, taxi " + taxi.ID
							+ ", setArrivalTime(" + lp + "," + td + ")"
							+ ", T.currnet = " + T.currentTimePoint);
				if (rid > 0) {
					taxi.currentItinerary
							.addAction(VehicleAction.PICKUP_PEOPLE);
					taxi.mStatus.put(taxi.currentItinerary.size() - 1,
							VehicleStatus.PICKUP_PEOPLE);

					td = td + peopleReq.pickupDuration;
					taxi.currentItinerary.setDepartureTime(
							taxi.currentItinerary.size() - 1, td);
					if (taxiID == taxi.ID)
						log.println(name() + "::admitNewItinerary, taxi "
								+ taxi.ID + ", setDepartureTime(" + lp + ","
								+ td + ")" + ", T.currnet = "
								+ T.currentTimePoint);
				} else {
					taxi.currentItinerary
							.addAction(VehicleAction.DELIVERY_PEOPLE);
					taxi.mStatus.put(taxi.currentItinerary.size() - 1,
							VehicleStatus.DELIVERY_PEOPLE);

					td = td + peopleReq.deliveryDuration;
					taxi.currentItinerary.setDepartureTime(
							taxi.currentItinerary.size() - 1, td);
					if (taxiID == taxi.ID)
						log.println(name() + "::admitNewItinerary, taxi "
								+ taxi.ID + ", setDepartureTime(" + lp + ","
								+ td + ")" + ", T.currnet = "
								+ T.currentTimePoint);
				}
			} else {
				ParcelRequest parcelReq = mParcelRequest.get(arid);
				if (rid > 0)
					nextPoint = parcelReq.pickupLocationID;
				else
					nextPoint = parcelReq.deliveryLocationID;
				Itinerary I = dijkstra.queryShortestPath(curPos, nextPoint);
				int t = getTravelTime(I.getDistance(), maxSpeedms);
				double d = I.getDistance();
				int v0 = I.get(0);
				for (int j = 1; j < I.size() - 1; j++) {
					int v = I.get(j);
					taxi.currentItinerary.addPoint(v);
					taxi.currentItinerary.addAction(VehicleAction.PASS);
					taxi.currentItinerary.addRequestID(-1);

					Arc a = map.getArc(v0, v);
					int dt = (int) (t * a.w / d);
					td = td + dt;
					t = t - dt;
					d = d - a.w;
					v0 = v;
					taxi.currentItinerary.setArrivalTime(
							taxi.currentItinerary.size() - 1, td);
					taxi.currentItinerary.setDepartureTime(
							taxi.currentItinerary.size() - 1, td);
				}
				taxi.currentItinerary.addPoint(I.get(I.size() - 1));
				taxi.currentItinerary.addRequestID(arid);

				td = td + t;
				taxi.currentItinerary.setArrivalTime(
						taxi.currentItinerary.size() - 1, td);

				if (rid > 0) {
					taxi.currentItinerary
							.addAction(VehicleAction.PICKUP_PARCEL);
					taxi.mStatus.put(taxi.currentItinerary.size() - 1,
							VehicleStatus.PICKUP_PARCEL);

					td = td + parcelReq.pickupDuration;
					taxi.currentItinerary.setDepartureTime(
							taxi.currentItinerary.size() - 1, td);
				} else {
					taxi.currentItinerary
							.addAction(VehicleAction.DELIVERY_PARCEL);
					taxi.mStatus.put(taxi.currentItinerary.size() - 1,
							VehicleStatus.DELIVERY_PARCEL);

					td = td + parcelReq.deliveryDuration;
					taxi.currentItinerary.setDepartureTime(
							taxi.currentItinerary.size() - 1, td);
				}
			}
			curPos = nextPoint;
		}
		Itinerary I = dijkstra.queryShortestPath(curPos,
				ss.parkingLocationPoint);
		int t = getTravelTime(I.getDistance(), maxSpeedms);
		double d = I.getDistance();
		int v0 = I.get(0);

		for (int j = 1; j < I.size() - 1; j++) {
			int v = I.get(j);
			taxi.currentItinerary.addPoint(v);
			taxi.currentItinerary.addAction(VehicleAction.PASS);
			taxi.currentItinerary.addRequestID(-1);

			Arc a = map.getArc(v0, v);
			int dt = (int) (t * a.w / d);
			td = td + dt;
			t = t - dt;
			d = d - a.w;
			v0 = v;
			taxi.currentItinerary.setArrivalTime(
					taxi.currentItinerary.size() - 1, td);
			taxi.currentItinerary.setDepartureTime(
					taxi.currentItinerary.size() - 1, td);
		}
		taxi.currentItinerary.addPoint(I.get(I.size() - 1));
		taxi.currentItinerary.addRequestID(-1);
		taxi.currentItinerary.addAction(VehicleAction.STOP);
		taxi.mStatus.put(taxi.currentItinerary.size() - 1,
				VehicleStatus.REST_AT_PARKING);
		td = td + t;
		taxi.currentItinerary.setArrivalTime(taxi.currentItinerary.size() - 1,
				td);
		/*
		 * boolean ok = assignTimePoint(taxi.currentItinerary, taxi, fromIndex,
		 * nextStartTimePoint, insertedPeopleRequests, insertedParcelRequests);
		 * if(!ok){ System.out.println(
		 * "SimulatorBookedRequest::admitNewItinerary --> FAILED when assignTimePoint????"
		 * ); log.close(); System.exit(-1);
		 * 
		 * }
		 */
		taxi.remainRequestIDs = new ArrayList<Integer>();
		for (int i = 0; i < ss.rids.length; i++)
			taxi.remainRequestIDs.add(ss.rids[i]);
		taxi.remainDistance = ss.distance;

		P = taxi.getFinalParking();
		if (P != null) {
			P.load++;
		}
	}

	/*
	 * public ServiceSequence computeBestProfitsParcelInsertion(Vehicle taxi,
	 * ParcelRequest pr){ int startIdx = 0; int idxDelivery = -1; int rid = -1;
	 * PeopleRequest peopleReq = null; if(taxi.peopleReqIDonBoard.size() > 0){
	 * rid = taxi.peopleReqIDonBoard.get(0); peopleReq =
	 * mPeopleRequest.get(rid);
	 * 
	 * for(int i = 0; i < taxi.remainRequestIDs.size(); i++){
	 * if(taxi.remainRequestIDs.get(i) == -rid){ idxDelivery = i; break; } }
	 * if(countStop.get(rid) + idxDelivery >= peopleReq.maxNbStops){ startIdx =
	 * idxDelivery+1; } } int nextStartPoint = taxi.getNextStartPoint(); int
	 * startTimePoint = taxi.getNextStartTimePoint();
	 * 
	 * int[] nod = new int[taxi.remainRequestIDs.size() + 2]; // explore all
	 * possible position i1 (pickup), i2 (delivery) among taxi.remainRequestIDs
	 * for inserting pr ArrayList<Integer> parkings =
	 * collectAvailableParkings(taxi); double maxProfits = -dijkstra.infinity;
	 * //int sel_pickup_index = -1; //int sel_delivery_index = -1; //int sel_pk
	 * = -1; ServiceSequence ss = null; double expectDistanceParcel =
	 * dijkstra.queryDistance(pr.pickupLocationID, pr.deliveryLocationID);
	 * for(int i1 = startIdx; i1 <= taxi.remainRequestIDs.size(); i1++){ for(int
	 * i2 = i1; i2 <= taxi.remainRequestIDs.size(); i2++){ // establish new
	 * sequence of request ids stored in nod if(rid > 0){ if(i1 <= idxDelivery
	 * && i2 <= idxDelivery && countStop.get(rid) + idxDelivery + 2 >
	 * peopleReq.maxNbStops) continue; } int idx = -1; double profits =
	 * getParcelRevenue(expectDistanceParcel); for(int k1 = 0; k1 < i1; k1++){
	 * idx++; nod[idx] = taxi.remainRequestIDs.get(k1); } idx++; nod[idx] =
	 * pr.id;// insert pickup for(int k1 = 0; k1 < i2-i1; k1++){ idx++; nod[idx]
	 * = taxi.remainRequestIDs.get(i1 + k1); } idx++; nod[idx] = -pr.id;//
	 * insert delivery for(int k1 = i2; k1 < taxi.remainRequestIDs.size();
	 * k1++){ idx++; nod[idx] = taxi.remainRequestIDs.get(k1); }
	 * 
	 * // evaluate the insertion double D =
	 * computeFeasibleDistance(nextStartPoint, startTimePoint, nod); if(D >
	 * dijkstra.infinity - 1) continue;// constraints are violated
	 * 
	 * for(int k = 0; k < parkings.size(); k++){ int pk = parkings.get(k);
	 * //double D = computeFeasibleDistance(nextStartPoint, startTimePoint, nod,
	 * pk); //if(D > dijkstra.infinity - 1) continue;// constraints are violated
	 * int endPoint = getLocationFromEncodedRequest(nod[nod.length-1]); D = D +
	 * dijkstra.queryDistance(endPoint, pk); double extraDistance = D -
	 * taxi.remainDistance; profits = profits - getCostFuel(extraDistance);
	 * if(profits > maxProfits){ maxProfits = profits; //sel_pickup_index = i1;
	 * //sel_delivery_index = i2; //sel_pk = pk; ss = new
	 * ServiceSequence(nod,profits,pk,D); } } } }
	 * 
	 * return ss; }
	 */

	/*
	 * public ServiceSequence computeBestProfitsPeopleInsertion(Vehicle taxi,
	 * PeopleRequest pr){ int startIdx = 0; int taxiID = 1;
	 * if(taxi.peopleReqIDonBoard.size() > 0){ // taxi is carrying a passenger
	 * int rid = taxi.peopleReqIDonBoard.get(0); for(int i = 0; i <
	 * taxi.remainRequestIDs.size(); i++){ if(taxi.remainRequestIDs.get(i) ==
	 * -rid){ startIdx = i+1; break; } } if(taxi.ID == taxiID){
	 * System.out.println
	 * ("SimulatorBookedRequest::computeBestProfitsPeopleInsertion, " +
	 * "request on board = " + rid + ", remainRequestIDs = " +
	 * taxi.getRemainRequestID() + ", startIdx = " + startIdx +
	 * ", new people quest = " + pr.id);
	 * log.println("SimulatorBookedRequest::computeBestProfitsPeopleInsertion, "
	 * + "request on board = " + rid + ", remainRequestIDs = " +
	 * taxi.getRemainRequestID() + ", startIdx = " + startIdx +
	 * ", new people quest = " + pr.id);
	 * 
	 * } }
	 * 
	 * int nextStartPoint = taxi.getNextStartPoint(); int startTimePoint =
	 * taxi.getNextStartTimePoint();
	 * 
	 * int[] nod = new int[taxi.remainRequestIDs.size() + 2]; // explore all
	 * possible position i1 (pickup), i2 (delivery) among taxi.remainRequestIDs
	 * for inserting pr ArrayList<Integer> parkings =
	 * collectAvailableParkings(taxi); //double minExtraDistance =
	 * dijkstra.infinity; //int sel_pickup_index = -1; //int sel_delivery_index
	 * = -1; //int sel_pk = -1; double maxProfits = -dijkstra.infinity;
	 * ServiceSequence ss = null; double expectDistancePeople =
	 * dijkstra.queryDistance(pr.pickupLocationID, pr.deliveryLocationID);
	 * for(int i1 = startIdx; i1 <= taxi.remainRequestIDs.size(); i1++){ int max
	 * = taxi.remainRequestIDs.size() < i1 + pr.maxNbStops ?
	 * taxi.remainRequestIDs.size() : i1 + pr.maxNbStops; for(int i2 = i1; i2 <=
	 * taxi.remainRequestIDs.size(); i2++){ //for(int i2 = i1; i2 <= i1; i2++){
	 * // establish new sequence of request ids stored in nod int idx = -1; int
	 * pickup_idx = -1; int delivery_idx = -1; double profits =
	 * getPeopleRevenue(expectDistancePeople); for(int k1 = 0; k1 < i1; k1++){
	 * idx++; nod[idx] = taxi.remainRequestIDs.get(k1); } idx++; nod[idx] =
	 * pr.id;// insert pickup pickup_idx = idx; for(int k1 = 0; k1 < i2-i1;
	 * k1++){ idx++; nod[idx] = taxi.remainRequestIDs.get(i1 + k1); } idx++;
	 * nod[idx] = -pr.id;// insert delivery delivery_idx = idx; for(int k1 = i2;
	 * k1 < taxi.remainRequestIDs.size(); k1++){ idx++; nod[idx] =
	 * taxi.remainRequestIDs.get(k1); }
	 * 
	 * // compute the distance of passenger service double distancePeople = 0;
	 * for(int k1 = pickup_idx; k1 < delivery_idx; k1++){ int u =
	 * getLocationFromEncodedRequest(nod[k1]); int v =
	 * getLocationFromEncodedRequest(nod[k1+1]); distancePeople = distancePeople
	 * + dijkstra.queryDistance(u,v); } if(distancePeople >
	 * pr.maxTravelDistance) continue;
	 * 
	 * // check if travel distance of passenger on board exceeds maximum
	 * distance allowed boolean ok = true; for(int k1 = 0; k1 <
	 * taxi.peopleReqIDonBoard.size(); k1++){ int pobReqID =
	 * taxi.peopleReqIDonBoard.get(k1); PeopleRequest pR =
	 * mPeopleRequest.get(pobReqID); double d1 =
	 * computeRemainTravelDistance(pobReqID,nod,nextStartPoint); Arc A =
	 * map.getArc(taxi.lastPoint, nextStartPoint); double d2 = d1 +
	 * accumulateDistance.get(pobReqID); if(A != null) d2 = d2 + A.w; if(d2 >
	 * pR.maxTravelDistance){ ok = false; break; } } if(!ok) continue;
	 * 
	 * if(pickup_idx < delivery_idx - 1)// not direct delivery profits = profits
	 * - getDiscount(expectDistancePeople, distancePeople);
	 * 
	 * // evaluate the insertion
	 * 
	 * double D = computeFeasibleDistance(nextStartPoint, startTimePoint, nod);
	 * if(D > dijkstra.infinity - 1) continue;// constraints are violated
	 * 
	 * for(int k = 0; k < parkings.size(); k++){ int pk = parkings.get(k);
	 * //double D = computeFeasibleDistance(nextStartPoint, startTimePoint, nod,
	 * pk); //if(D > dijkstra.infinity - 1) continue;// constraints are violated
	 * int endPoint = getLocationFromEncodedRequest(nod[nod.length-1]); D = D +
	 * dijkstra.queryDistance(endPoint, pk); double extraDistance = D -
	 * taxi.remainDistance; profits = profits - getCostFuel(extraDistance);
	 * 
	 * if(profits > maxProfits){ maxProfits = profits; //sel_pickup_index = i1;
	 * //sel_delivery_index = i2; //sel_pk = pk; ss = new
	 * ServiceSequence(nod,profits,pk,D);
	 * 
	 * System.out.println(
	 * "SimulatorBookedRequest::computeBestPeopleInsertion, UPDATE ss = " +
	 * ss.profitEvaluation + ", sequence = " + ss.getSequence() +
	 * ", distance D = " + ss.distance); log.println(
	 * "SimulatorBookedRequest::computeBestPeopleInsertion, UPDATE ss = " +
	 * ss.profitEvaluation + ", sequence = " + ss.getSequence() +
	 * ", distance D = " + ss.distance);
	 * 
	 * } } } }
	 * 
	 * return ss; }
	 */
	/*
	 * public double computeFeasibleDistance(int startPoint, int startTimePoint,
	 * int[] rids, int parkingLoc){ // feasible distance of itinerary starting
	 * from startPoint, traversing rids (+ means pickup and 0 means delivery) //
	 * and back to a nearest available parking // return infinity if the
	 * itinerary violates time window constraints System.out.print(
	 * "SimulatorBookedRequest::computeFeasibleDistance(startPoint = " +
	 * startPoint + ", startTimePoint = " + startTimePoint + ", rids = ");
	 * for(int i = 0; i < rids.length; i++) System.out.print(rids[i] + ",");
	 * System.out.println();
	 * log.print("SimulatorBookedRequest::computeFeasibleDistance(startPoint = "
	 * + startPoint + ", startTimePoint = " + startTimePoint + ", rids = ");
	 * for(int i = 0; i < rids.length; i++) log.print(rids[i] + ",");
	 * log.println();
	 * 
	 * double distance = 0; int curPoint = startPoint; int td = startTimePoint;
	 * for(int i = 0; i < rids.length; i++){ int rid = rids[i]; int arid =
	 * Math.abs(rid); int nextPoint = -1; int ta = -1; ParcelRequest parcelReq =
	 * mParcelRequest.get(arid); if(parcelReq != null){ if(rid > 0) nextPoint =
	 * parcelReq.pickupLocationID; else nextPoint =
	 * parcelReq.deliveryLocationID; double Di =
	 * dijkstra.queryDistance(curPoint, nextPoint); distance = distance + Di; ta
	 * = td + getTravelTime(Di, maxSpeedms); if(rid > 0){ if(ta >
	 * parcelReq.latePickupTime) return dijkstra.infinity; td = ta +
	 * parcelReq.pickupDuration; }else{ if(ta > parcelReq.lateDeliveryTime)
	 * return dijkstra.infinity; td = ta + parcelReq.deliveryDuration; }
	 * System.out
	 * .println("SimulatorBookedRequest::computeFeasibleDistance, i = " + i +
	 * ", parcelReq  = " + rid + ", ta = " + ta + ", new td = " + td +
	 * ", curPoint = " + curPoint + ", nextPoint = " + nextPoint + ", Di = " +
	 * Di); log.println("SimulatorBookedRequest::computeFeasibleDistance, i = "
	 * + i + ", parcelReq  = " + rid + ", ta = " + ta + ", new td = " + td +
	 * ", curPoint = " + curPoint + ", nextPoint = " + nextPoint + ", Di = " +
	 * Di); }else{ PeopleRequest peopleReq = mPeopleRequest.get(arid); if(rid >
	 * 0) nextPoint = peopleReq.pickupLocationID; else nextPoint =
	 * peopleReq.deliveryLocationID; double Di =
	 * dijkstra.queryDistance(curPoint, nextPoint); distance = distance + Di; ta
	 * = td + getTravelTime(Di, maxSpeedms); if(rid > 0){ if(ta >
	 * peopleReq.latePickupTime) return dijkstra.infinity; td = ta +
	 * peopleReq.pickupDuration; }else{ if(ta > peopleReq.lateDeliveryTime)
	 * return dijkstra.infinity; td = ta + peopleReq.deliveryDuration; }
	 * System.out
	 * .println("SimulatorBookedRequest::computeFeasibleDistance, i = " + i +
	 * ", peopleReq  = " + rid + ", ta = " + ta + ", new td = " + td +
	 * ", curPoint = " + curPoint + ", nextPoint = " + nextPoint + ", Di = " +
	 * Di); log.println("SimulatorBookedRequest::computeFeasibleDistance, i = "
	 * + i + ", peopleReq  = " + rid + ", ta = " + ta + ", new td = " + td +
	 * ", curPoint = " + curPoint + ", nextPoint = " + nextPoint + ", Di = " +
	 * Di); } curPoint = nextPoint; } distance = distance +
	 * dijkstra.queryDistance(curPoint, parkingLoc); return distance; }
	 */

	public double computeFeasibleDistanceNonFixedSpeed(int startPoint,
			int startTimePoint, int[] rids) {
		int td = startTimePoint;
		int curPoint = startPoint;
		int nextPoint = -1;
		int ta = -1;

		int countStop = -1;
		ArrayList<Integer> stack = new ArrayList<Integer>();
		double distance = 0;

		for (int i = 0; i < rids.length; i++) {

			int arid = Math.abs(rids[i]);
			ParcelRequest parcelReq = mParcelRequest.get(arid);
			if (parcelReq != null) {
				if (rids[i] > 0)
					nextPoint = parcelReq.pickupLocationID;
				else
					nextPoint = parcelReq.deliveryLocationID;
				double Di = dijkstra.queryDistance(curPoint, nextPoint);
				int tmin = getTravelTime(Di, maxSpeedms);
				int tmax = getTravelTime(Di, minSpeedms);

				if (rids[i] > 0) {
					if (td + tmin > parcelReq.latePickupTime)
						return dijkstra.infinity;
					if (td + tmax < parcelReq.earlyPickupTime)
						return dijkstra.infinity;
					if (td + tmin < parcelReq.earlyPickupTime)
						ta = parcelReq.earlyPickupTime;
					else
						ta = td + tmin;
					td = ta + parcelReq.pickupDuration;
				} else {
					if (td + tmin > parcelReq.lateDeliveryTime)
						return dijkstra.infinity;
					if (td + tmax < parcelReq.earlyDeliveryTime)
						return dijkstra.infinity;
					if (td + tmin < parcelReq.earlyDeliveryTime)
						ta = parcelReq.earlyDeliveryTime;
					else
						ta = td + tmin;
					td = ta + parcelReq.deliveryDuration;
				}

				if (countStop >= 0) {
					countStop++;

					int r = stack.get(0);
					PeopleRequest pr = mPeopleRequest.get(r);
					if (countStop > pr.maxNbStops) {
						return dijkstra.infinity;
					}

				}
			} else {
				PeopleRequest peopleReq = mPeopleRequest.get(arid);
				if (peopleReq != null) {
					if (rids[i] > 0)
						nextPoint = peopleReq.pickupLocationID;
					else
						nextPoint = peopleReq.deliveryLocationID;
					double Di = dijkstra.queryDistance(curPoint, nextPoint);
					int tmin = getTravelTime(Di, maxSpeedms);
					int tmax = getTravelTime(Di, minSpeedms);

					if (rids[i] > 0) {
						if (td + tmin > peopleReq.latePickupTime)
							return dijkstra.infinity;
						if (td + tmax < peopleReq.earlyPickupTime)
							return dijkstra.infinity;
						if (td + tmin < peopleReq.earlyPickupTime)
							ta = peopleReq.earlyPickupTime;
						else
							ta = td + tmin;
						td = ta + peopleReq.pickupDuration;

						if (stack.size() > 0)
							return dijkstra.infinity;
						stack.add(rids[i]);
						countStop = 0;
					} else {
						if (td + tmin > peopleReq.lateDeliveryTime)
							return dijkstra.infinity;
						if (td + tmax < peopleReq.earlyDeliveryTime)
							return dijkstra.infinity;
						if (td + tmin < peopleReq.earlyDeliveryTime)
							ta = peopleReq.earlyDeliveryTime;
						else
							ta = td + tmin;
						td = ta + peopleReq.deliveryDuration;

						if (stack.size() > 0) {
							if (stack.get(stack.size() - 1) == -rids[i]) {
								stack.remove(stack.size() - 1);
								countStop = -1;
							}
						}
					}

				} else {
					System.out
							.println(name()
									+ "::computeFeasibleDistanceNonFixedSpeed EXCEPTION, unknown request "
									+ rids[i]);
					System.exit(-1);
				}
			}
			curPoint = nextPoint;
		}

		return dijkstra.infinity;
	}

	public double computeFeasibleDistance(int startPoint, int startTimePoint,
			int[] rids) {
		// feasible distance of itinerary starting from startPoint, traversing
		// rids ('+' means pickup and '-' means delivery)
		// and back to a nearest available parking
		// return infinity if the itinerary violates time window constraints
		/*
		 * System.out.print(
		 * "SimulatorBookedRequest::computeFeasibleDistance(startPoint = " +
		 * startPoint + ", startTimePoint = " + startTimePoint + ", rids = ");
		 * for(int i = 0; i < rids.length; i++) System.out.print(rids[i] + ",");
		 * System.out.println();
		 * log.print("SimulatorBookedRequest::computeFeasibleDistance(startPoint = "
		 * + startPoint + ", startTimePoint = " + startTimePoint + ", rids = ");
		 * for(int i = 0; i < rids.length; i++) log.print(rids[i] + ",");
		 * log.println();
		 */
		ArrayList<Integer> stack = new ArrayList<Integer>();

		double distance = 0;
		int curPoint = startPoint;
		int td = startTimePoint;
		int countStop = -1;
		for (int i = 0; i < rids.length; i++) {
			int rid = rids[i];
			int arid = Math.abs(rid);
			int nextPoint = -1;
			int ta = -1;
			ParcelRequest parcelReq = mParcelRequest.get(arid);
			if (parcelReq != null) {
				if (rid > 0)
					nextPoint = parcelReq.pickupLocationID;
				else
					nextPoint = parcelReq.deliveryLocationID;
				double Di = dijkstra.queryDistance(curPoint, nextPoint);
				// System.out.println(name() +
				// "::computeFeasibleDistance, ParcelReq distance from " +
				// curPoint + " to " + nextPoint + " Di = "+ Di);
				distance = distance + Di;
				// int tmin = getTravelTime(Di, maxSpeedms);
				// int tmax = getTravelTime(Di, minSpeedms);

				ta = td + getTravelTime(Di, maxSpeedms);
				if (rid > 0) {
					// if(td + tmax < parcelReq.earlyPickupTime) return
					// dijkstra.infinity;
					// if(td + tmin > parcelReq.latePickupTime) return
					// dijkstra.infinity;
					// if(td + tmin < parcelReq.earlyPickupTime) ta =
					// parcelReq.earlyPickupTime; else ta = td + tmin;// arrive
					// as ea

					td = ta + parcelReq.pickupDuration;

					/*
					 * if ((ta > parcelReq.latePickupTime || ta <
					 * parcelReq.earlyPickupTime) && (td >
					 * parcelReq.latePickupTime || td <
					 * parcelReq.earlyPickupTime)) { //
					 * System.out.println(name() + //
					 * "::computeFeasibleDistance, pickup rid = " + rid + //
					 * ", ta = " + ta + // ", earlyPickupTime = " +
					 * parcelReq.earlyPickupTime + // ", latePickupTime = " +
					 * parcelReq.latePickupTime + // ", earlyDeliveryTime = " +
					 * // parcelReq.earlyDeliveryTime + ", lateDeliveryTime = "
					 * // + parcelReq.lateDeliveryTime + //
					 * " --> return infinity");
					 * 
					 * return dijkstra.infinity; }
					 */
					// ta = td + getTravelTimeSegments(curPoint, nextPoint);
					// if(ta > parcelReq.latePickupTime) return
					// dijkstra.infinity;

				} else {
					td = ta + parcelReq.deliveryDuration;
					/*
					 * if ((ta > parcelReq.lateDeliveryTime || ta <
					 * parcelReq.earlyDeliveryTime) && (td >
					 * parcelReq.lateDeliveryTime || td <
					 * parcelReq.earlyDeliveryTime)) { //
					 * System.out.println(name() + //
					 * "::computeFeasibleDistance, delivery rid = "+ rid + //
					 * ", ta = " + ta + // ", earlyPickupTime = " +
					 * parcelReq.earlyPickupTime + // ", latePickupTime = " +
					 * parcelReq.latePickupTime + // ", earlyDeliveryTime = " +
					 * // parcelReq.earlyDeliveryTime + ", lateDeliveryTime = "
					 * // + parcelReq.lateDeliveryTime + //
					 * " --> return infinity");
					 * 
					 * return dijkstra.infinity; }
					 */
					// ta = td + getTravelTimeSegments(curPoint, nextPoint);
					// if(ta > parcelReq.lateDeliveryTime) return
					// dijkstra.infinity;

				}
				/*
				 * System.out.println(
				 * "SimulatorBookedRequest::computeFeasibleDistance, i = " + i +
				 * ", parcelReq  = " + rid + ", ta = " + ta + ", new td = " + td
				 * + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint +
				 * ", Di = " + Di); log.println(
				 * "SimulatorBookedRequest::computeFeasibleDistance, i = " + i +
				 * ", parcelReq  = " + rid + ", ta = " + ta + ", new td = " + td
				 * + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint +
				 * ", Di = " + Di);
				 */

				if (countStop >= 0) {
					countStop++;

					int r = stack.get(0);
					PeopleRequest pr = mPeopleRequest.get(r);
					if (countStop > pr.maxNbStops) {
						// System.out.println(name() +
						// "::computeFeasibleDistance, countStop = " + countStop
						// + " > pr.maxNbStops = " + pr.maxNbStops +
						// " --> return infinity");
						return dijkstra.infinity;
					}
					// if(countStop > 0) return dijkstra.infinity;
				}
			} else {
				PeopleRequest peopleReq = mPeopleRequest.get(arid);
				if (rid > 0)
					nextPoint = peopleReq.pickupLocationID;
				else
					nextPoint = peopleReq.deliveryLocationID;
				double Di = dijkstra.queryDistance(curPoint, nextPoint);
				distance = distance + Di;
				ta = td + getTravelTime(Di, maxSpeedms);
				if (rid > 0) {
					td = ta + peopleReq.pickupDuration;
					/*
					 * if (td > peopleReq.latePickupTime || td <
					 * peopleReq.earlyPickupTime) return dijkstra.infinity;
					 */
					// ta = td + getTravelTimeSegments(curPoint, nextPoint);
					// if(ta > peopleReq.latePickupTime) return
					// dijkstra.infinity;

					if (stack.size() > 0)
						return dijkstra.infinity;
					stack.add(rid);
					countStop = 0;
				} else {
					td = ta + peopleReq.deliveryDuration;
					/*
					 * if (ta > peopleReq.lateDeliveryTime || ta <
					 * peopleReq.earlyDeliveryTime) return dijkstra.infinity;
					 */
					// ta = td + getTravelTimeSegments(curPoint, nextPoint);
					// if(ta > peopleReq.latePickupTime) return
					// dijkstra.infinity;

					if (stack.size() > 0) {
						if (stack.get(stack.size() - 1) == -rid) {
							stack.remove(stack.size() - 1);
							countStop = -1;
						}
					}
				}
				/*
				 * System.out.println(
				 * "SimulatorBookedRequest::computeFeasibleDistance, i = " + i +
				 * ", peopleReq  = " + rid + ", ta = " + ta + ", new td = " + td
				 * + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint +
				 * ", Di = " + Di); log.println(
				 * "SimulatorBookedRequest::computeFeasibleDistance, i = " + i +
				 * ", peopleReq  = " + rid + ", ta = " + ta + ", new td = " + td
				 * + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint +
				 * ", Di = " + Di);
				 */
			}
			curPoint = nextPoint;
		}

		return distance;
	}

	public double computeFeasibleDistance(int startPoint, int startTimePoint,
			ArrayList<Integer> rids) {
		// feasible distance of itinerary starting from startPoint, traversing
		// rids ('+' means pickup and '-' means delivery)
		// and back to a nearest available parking
		// return infinity if the itinerary violates time window constraints
		/*
		 * System.out.print(
		 * "SimulatorBookedRequest::computeFeasibleDistance(startPoint = " +
		 * startPoint + ", startTimePoint = " + startTimePoint + ", rids = ");
		 * for(int i = 0; i < rids.length; i++) System.out.print(rids[i] + ",");
		 * System.out.println();
		 * log.print("SimulatorBookedRequest::computeFeasibleDistance(startPoint = "
		 * + startPoint + ", startTimePoint = " + startTimePoint + ", rids = ");
		 * for(int i = 0; i < rids.length; i++) log.print(rids[i] + ",");
		 * log.println();
		 */
		ArrayList<Integer> stack = new ArrayList<Integer>();

		double distance = 0;
		int curPoint = startPoint;
		int td = startTimePoint;
		int countStop = -1;
		for (int i = 0; i < rids.size(); i++) {
			int rid = rids.get(i);
			int arid = Math.abs(rid);
			int nextPoint = -1;
			int ta = -1;
			ParcelRequest parcelReq = mParcelRequest.get(arid);
			if (parcelReq != null) {
				if (rid > 0)
					nextPoint = parcelReq.pickupLocationID;
				else
					nextPoint = parcelReq.deliveryLocationID;
				double Di = dijkstra.queryDistance(curPoint, nextPoint);
				distance = distance + Di;
				ta = td + getTravelTime(Di, maxSpeedms);
				if (rid > 0) {
					td = ta + parcelReq.pickupDuration;
					if ((ta > parcelReq.latePickupTime || ta < parcelReq.earlyPickupTime)
							&& (td > parcelReq.latePickupTime || td < parcelReq.earlyPickupTime))
						return dijkstra.infinity;
					// ta = td + getTravelTimeSegments(curPoint, nextPoint);
					// if(ta > parcelReq.latePickupTime) return
					// dijkstra.infinity;

				} else {
					td = ta + parcelReq.deliveryDuration;
					if ((ta > parcelReq.lateDeliveryTime || ta < parcelReq.earlyDeliveryTime)
							&& (td > parcelReq.lateDeliveryTime || td < parcelReq.earlyDeliveryTime))
						return dijkstra.infinity;
					// ta = td + getTravelTimeSegments(curPoint, nextPoint);
					// if(ta > parcelReq.lateDeliveryTime) return
					// dijkstra.infinity;

				}
				/*
				 * System.out.println(
				 * "SimulatorBookedRequest::computeFeasibleDistance, i = " + i +
				 * ", parcelReq  = " + rid + ", ta = " + ta + ", new td = " + td
				 * + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint +
				 * ", Di = " + Di); log.println(
				 * "SimulatorBookedRequest::computeFeasibleDistance, i = " + i +
				 * ", parcelReq  = " + rid + ", ta = " + ta + ", new td = " + td
				 * + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint +
				 * ", Di = " + Di);
				 */

				if (countStop >= 0) {
					countStop++;

					int r = stack.get(0);
					PeopleRequest pr = mPeopleRequest.get(r);
					if (countStop > pr.maxNbStops)
						return dijkstra.infinity;
					// if(countStop > 0) return dijkstra.infinity;
				}
			} else {
				PeopleRequest peopleReq = mPeopleRequest.get(arid);
				if (rid > 0)
					nextPoint = peopleReq.pickupLocationID;
				else
					nextPoint = peopleReq.deliveryLocationID;
				double Di = dijkstra.queryDistance(curPoint, nextPoint);
				distance = distance + Di;
				ta = td + getTravelTime(Di, maxSpeedms);
				if (rid > 0) {
					td = ta + peopleReq.pickupDuration;
					if (td > peopleReq.latePickupTime
							|| td < peopleReq.earlyPickupTime)
						return dijkstra.infinity;
					// ta = td + getTravelTimeSegments(curPoint, nextPoint);
					// if(ta > peopleReq.latePickupTime) return
					// dijkstra.infinity;

					if (stack.size() > 0)
						return dijkstra.infinity;
					stack.add(rid);
					countStop = 0;
				} else {
					td = ta + peopleReq.deliveryDuration;
					if (ta > peopleReq.lateDeliveryTime
							|| ta < peopleReq.earlyDeliveryTime)
						return dijkstra.infinity;
					// ta = td + getTravelTimeSegments(curPoint, nextPoint);
					// if(ta > peopleReq.latePickupTime) return
					// dijkstra.infinity;

					if (stack.size() > 0) {
						if (stack.get(stack.size() - 1) == -rid) {
							stack.remove(stack.size() - 1);
							countStop = -1;
						}
					}
				}
				/*
				 * System.out.println(
				 * "SimulatorBookedRequest::computeFeasibleDistance, i = " + i +
				 * ", peopleReq  = " + rid + ", ta = " + ta + ", new td = " + td
				 * + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint +
				 * ", Di = " + Di); log.println(
				 * "SimulatorBookedRequest::computeFeasibleDistance, i = " + i +
				 * ", peopleReq  = " + rid + ", ta = " + ta + ", new td = " + td
				 * + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint +
				 * ", Di = " + Di);
				 */
			}
			curPoint = nextPoint;
		}

		return distance;
	}

	public boolean checkDirectPeopleServices(int[] rids) {
		// return true if all people services are direct
		/*
		 * System.out.print(
		 * "SimulatorBookedRequest::computeFeasibleDistance(startPoint = " +
		 * startPoint + ", startTimePoint = " + startTimePoint + ", rids = ");
		 * for(int i = 0; i < rids.length; i++) System.out.print(rids[i] + ",");
		 * System.out.println();
		 * log.print("SimulatorBookedRequest::computeFeasibleDistance(startPoint = "
		 * + startPoint + ", startTimePoint = " + startTimePoint + ", rids = ");
		 * for(int i = 0; i < rids.length; i++) log.print(rids[i] + ",");
		 * log.println();
		 */

		boolean firstDeliveryPeople = false;
		for (int i = 0; i < rids.length; i++) {
			if (rids[i] > 0) {
				PeopleRequest pr = mPeopleRequest.get(rids[i]);
				if (pr != null) {
					firstDeliveryPeople = true;
					if (i >= rids.length - 1)
						return false;// no corresponding delivery for people
										// request
					if (rids[i] + rids[i + 1] != 0)
						return false;
				}
			} else {
				PeopleRequest pr = mPeopleRequest.get(Math.abs(rids[i]));
				if (pr != null && !firstDeliveryPeople && i > 0)
					return false;// first delivery of people
			}
		}
		if (true)
			return true;

		ArrayList<Integer> stack = new ArrayList<Integer>();

		int countStop = -1;
		for (int i = 0; i < rids.length; i++) {
			int rid = rids[i];
			int arid = Math.abs(rid);
			int nextPoint = -1;
			int ta = -1;
			ParcelRequest parcelReq = mParcelRequest.get(arid);
			if (parcelReq != null) {
				/*
				 * System.out.println(
				 * "SimulatorBookedRequest::computeFeasibleDistance, i = " + i +
				 * ", parcelReq  = " + rid + ", ta = " + ta + ", new td = " + td
				 * + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint +
				 * ", Di = " + Di); log.println(
				 * "SimulatorBookedRequest::computeFeasibleDistance, i = " + i +
				 * ", parcelReq  = " + rid + ", ta = " + ta + ", new td = " + td
				 * + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint +
				 * ", Di = " + Di);
				 */

				if (countStop >= 0) {
					countStop++;

					int r = stack.get(0);
					PeopleRequest pr = mPeopleRequest.get(r);
					// if(countStop > pr.maxNbStops) return dijkstra.infinity;
					if (countStop > 0)
						return false;
				}
			} else {
				PeopleRequest peopleReq = mPeopleRequest.get(arid);

				if (rid > 0) {

					if (stack.size() > 0)
						return false;
					stack.add(rid);
					countStop = 0;
				} else {

					if (stack.size() > 0) {
						if (stack.get(stack.size() - 1) == -rid) {
							stack.remove(stack.size() - 1);
							countStop = -1;
						}
					}
				}

				/*
				 * System.out.println(
				 * "SimulatorBookedRequest::computeFeasibleDistance, i = " + i +
				 * ", peopleReq  = " + rid + ", ta = " + ta + ", new td = " + td
				 * + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint +
				 * ", Di = " + Di); log.println(
				 * "SimulatorBookedRequest::computeFeasibleDistance, i = " + i +
				 * ", peopleReq  = " + rid + ", ta = " + ta + ", new td = " + td
				 * + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint +
				 * ", Di = " + Di);
				 */
			}

		}

		return true;
	}

	public boolean checkDirectParcelServices(int[] rids) {
		// return true if all parcel services are direct
		for (int i = 0; i < rids.length; i++) {
			int arid = Math.abs(rids[i]);
			ParcelRequest pr = mParcelRequest.get(arid);
			if (pr != null) {
				if (i + 1 < rids.length) {
					if (rids[i] > 0 && rids[i] + rids[i + 1] != 0)
						return false;
				}
			}
		}
		/*
		 * int i = 0; if(rids.length % 2 == 1) i = 1; while(i < rids.length){
		 * boolean ok = rids[i] + rids[i+1] == 0 && rids[i] > 0 && rids[i+1] <
		 * 0; if(!ok) return false; i = i + 2; }
		 */
		return true;
	}

	public TaxiTimePointIndex estimatePickupPlusDeliveryDistanceTaxi(
			Vehicle taxi, ParcelRequest pr) {
		
		// find delivery people location
		//LatLng llPickup = map.mLatLng.get(pr.pickupLocationID);
		//LatLng llDelivery = map.mLatLng.get(pr.deliveryLocationID);
		int locID = -1;
		int idxLocID = -1;
		TimePointIndex tpi = taxi.getNextTimePointIndex(taxi.lastIndexPoint,
				T.currentTimePoint, TimePointDuration);

		if (taxi.ID == debugTaxiID) {
			log.println(name()
					+ "::estimatePickupPlusDeliveryDistanceTaxi, taxi "
					+ taxi.ID + " HAVING " + taxi.requestStatus() + ", tpi = "
					+ tpi.toString());
		}
		//When the taxi is initialized, remainRequestIDs is not null.
		/*boolean ok = taxi.remainRequestIDs == null;
		ok = ok || taxi.remainRequestIDs.size() == 0;*/
		//[SonNV] If the itinerary of the taxi is finished, return this taxi.
		if (taxi.remainRequestIDs.size() == 0) {
			/*
			LatLng ll = map.mLatLng.get(tpi.point);
			double d = G.computeDistanceHaversine(ll.lat, ll.lng, llPickup.lat,
					llPickup.lng)
					+ G.computeDistanceHaversine(ll.lat, ll.lng,
							llDelivery.lat, llDelivery.lng);
			*/
			//double d = estimateTravelingDistance(tpi.point,pr.pickupLocationID) + estimateTravelingDistance(tpi.point, pr.deliveryLocationID);
			//LatLng ll = map.mLatLng.get(tpi.point);
			//double d = G.computeDistanceHaversine(ll.lat, ll.lng, llPickup.lat, llPickup.lng);
			double d = estimateTravelingDistance(tpi.point,pr.pickupLocationID);
			
			TaxiTimePointIndex ttpi = new TaxiTimePointIndex(taxi, tpi,
					taxi.remainRequestIDs, new ArrayList<Integer>());
			ttpi.estimation = d;
			ttpi.idx = -1;
			return ttpi;
		}

		if (taxi.ID == debugTaxiID) {
			System.out.println(name()
					+ "::estimatePickupPlusDeliveryDistanceTaxi, taxi = "
					+ taxi.ID + ", pr = " + pr.id + ", T.currentTimePoint = "
					+ T.currentTimePoint + ", tpi = " + tpi.toString());
			log.println(name() + "::availableTaxi, taxi = " + taxi.ID
					+ ", pr = " + pr.id + ", T.currentTimePoint = "
					+ T.currentTimePoint + ", tpi = " + tpi.toString());
		}
		
		//[SonNV] Get last people delivery point.
		
		int countPeopleDelivery = 0;
		int lastPeopleDeliveryRequestID = -1;
		for (int i = 0; i < taxi.remainRequestIDs.size(); i++) {
			int rid = taxi.remainRequestIDs.get(i);
			if (rid > 0) //pickup point
				continue;
			//[SonNV]If rid < 0, this point is either parcel or people delivery point.
			PeopleRequest r = mPeopleRequest.get(Math.abs(rid));
			if (r != null) { //[SonNV]If it is people request, the last people request point is saved.
				locID = r.deliveryLocationID;
				idxLocID = i;
				lastPeopleDeliveryRequestID = r.id;
				countPeopleDelivery++;
				// break;
			}
		}
		// System.out.println(name() +
		// "::estimatePickupPlusDeliveryDistanceTaxi, taxi = " + taxi.ID +
		// ", STATUS = " +
		// taxi.requestStatus() + ", idxLocID = " + idxLocID +
		// ", tpi.indexRemainRequestID = " + tpi.indexRemainRequestIDs +
		// ", taxi.currentItinerary = " + taxi.currentItinerary.toString());

		if (taxi.ID == debugTaxiID) {
			log.println(name()
					+ "::estimatePickupPlusDeliveryDistanceTaxi, taxi = "
					+ taxi.ID + ", STATUS = " + taxi.requestStatus()
					+ ", idxLocID = " + idxLocID
					+ ", tpi.indexRemainRequestID = "
					+ tpi.indexRemainRequestIDs + ", taxi.currentItinerary = "
					+ taxi.currentItinerary.toString());
		}

		idxLocID = idxLocID > tpi.indexRemainRequestIDs ? idxLocID
				: tpi.indexRemainRequestIDs;

		if (countPeopleDelivery > 2) {// more than 2 delivery people on the
										// itinerary
			return null;
		}

		int timePoint = -1;
		int point = -1;
		int indexPoint = -1;
		ArrayList<Integer> R = new ArrayList<Integer>();// new remain requestIDs
		ArrayList<Integer> KR = new ArrayList<Integer>();// kept remain request
															// sequences

		idxLocID = idxLocID < taxi.remainRequestIDs.size() - 1 ? idxLocID
				: taxi.remainRequestIDs.size() - 1;
		for (int i = 0; i <= idxLocID; i++)
			KR.add(taxi.remainRequestIDs.get(i));
		for (int i = idxLocID + 1; i < taxi.remainRequestIDs.size(); i++)
			R.add(taxi.remainRequestIDs.get(i));

		//[SonNV]Have people request in remain requests list.
		if (locID >= 0) {
			// for(int i = 0; i <= idxLocID; i++)
			// KR.add(taxi.remainRequestIDs.get(i));
			// for(int i = idxLocID+1; i < taxi.remainRequestIDs.size(); i++)
			// R.add(taxi.remainRequestIDs.get(i));

			//[SonNV]Find last people delivery point and update variables: timePoint, indexPoint, point.
			for (int i = taxi.lastIndexPoint; i < taxi.currentItinerary.size(); i++) {
				if (taxi.currentItinerary.getAction(i) == VehicleAction.DELIVERY_PEOPLE
						&&
						// taxi.currentItinerary.get(i) == locID){ replaced by
						taxi.currentItinerary.getRequestID(i) == lastPeopleDeliveryRequestID) {
					// return new
					// TimePointIndex(taxi.currentItinerary.getDepartureTime(i),taxi.currentItinerary.get(i),i);
					timePoint = taxi.currentItinerary.getDepartureTime(i);
					if (timePoint < 0)// ith position is not only delivery
										// location but also parking location
						timePoint = taxi.currentItinerary.getArrivalTime(i)
								+ pr.deliveryDuration;
					point = taxi.currentItinerary.get(i);
					indexPoint = i;
					if (taxi.ID == debugTaxiID) {
						System.out.println(name()
								+ "::estimatePickupPlusDeliveryDistanceTaxi, timePoint delivery = "
								+ timePoint);
						log.println(name()
								+ "::estimatePickupPlusDeliveryDistanceTaxi, timePoint delivery = "
								+ timePoint);
					}
					break;
				}
			}

		} else {

		}

		//[SonNV]Update variables in case index of last delivery point is higher than 
		//index of last point in remainRequestIDs which is passed in decision time (indexRemainRequestIDs).
		if (timePoint > tpi.timePoint) {
			tpi.timePoint = timePoint;
			tpi.point = point;
			tpi.indexPoint = indexPoint;
		}
		
		if (DEBUG && taxi.ID == debugTaxiID) {
			if (!taxi.checkNotConstains(indexPoint + 1, KR)) {
				System.out.println(name() + "::availableTaxi, taxi " + taxi.ID
						+ " checkNotContains FAILED??");

				log.println(name() + "::availableTaxi, taxi " + taxi.ID
						+ " checkNotContains FAILED??");
				log.println("KR = " + Utility.arr2String(KR) + ", fromIndex = "
						+ indexPoint + ", currentItinerary = "
						+ taxi.currentItinerary.toString());
				exit();
			} else {
				System.out.println(name()
						+ "::availableTaxi, DEBUG TRUE, checkNotContains TRUE");
			}
		}
		
		//estimate shortest distance to go to pickup and delivery locations.
		double distancePickup = 1000000000;
		int idxInRemainRqL = -1;

		for (int i = idxLocID + 1; i < taxi.remainRequestIDs.size(); i++) {
			int rid = taxi.remainRequestIDs.get(i);
			int arid = Math.abs(rid);
			int p = -1;
			PeopleRequest peoReq = mPeopleRequest.get(arid);
			if (peoReq != null) {
				if (rid > 0)
					p = peoReq.pickupLocationID;
				else
					p = peoReq.deliveryLocationID;
			} else {
				ParcelRequest parReq = mParcelRequest.get(arid);
				if (rid > 0)
					p = parReq.pickupLocationID;
				else
					p = parReq.deliveryLocationID;
			}
			//LatLng ll = map.mLatLng.get(p);

			double dpickup = estimateTravelingDistance(pr.pickupLocationID, p);//G.computeDistanceHaversine(llPickup.lat, llPickup.lng, ll.lat, ll.lng);
			//double ddelivery = estimateTravelingDistance(pr.deliveryLocationID, p);//G.computeDistanceHaversine(llDelivery.lat, llDelivery.lng, ll.lat, ll.lng);

			if(dpickup < distancePickup){
				distancePickup = dpickup;
				idxInRemainRqL = i - idxLocID - 1;
			}
			//distanceDelivery = distanceDelivery < ddelivery ? distanceDelivery
			//		: ddelivery;

		}
		TaxiTimePointIndex ttpi = new TaxiTimePointIndex(taxi, tpi, R, KR);
		//ttpi.estimation = distancePickup + distanceDelivery;
		ttpi.estimation = distancePickup;
		ttpi.idx = idxInRemainRqL;
		return ttpi;
	}

	public boolean insertParcelRequest(Vehicle taxi, ParcelRequest pr,
			int fromIdx) {
		int curPos = taxi.lastPoint;
		int t = T.currentTimePoint;
		int taxiID = 11;
		if (taxi.ID == taxiID) {
			if (taxi.currentItinerary != null) {
				log.println("SimulatorBookedRequests::insertParcelRequest, taxi = "
						+ taxi.ID
						+ ", status = "
						+ taxi.getStatusDescription(taxi.status)
						+ ", T.current = "
						+ T.currentTimePoint
						+ ", currentItinerary = ");
				taxi.currentItinerary.writeToFile(log);
				System.out
						.println("SimulatorBookedRequests::insertParcelRequest, taxi = "
								+ taxi.ID
								+ ", parcel request id = "
								+ pr.id
								+ ", T.current = "
								+ t
								+ ", fromIdx = "
								+ fromIdx
								+ ", "
								+ "lastIndexPoint = "
								+ taxi.lastIndexPoint
								+ ", lastPoint = "
								+ taxi.lastPoint
								+ ", depTime at formIdx = "
								+ taxi.currentItinerary
										.getDepartureTime(fromIdx));
				log.println("SimulatorBookedRequests::insertParcelRequest, taxi = "
						+ taxi.ID
						+ ", status = "
						+ taxi.getStatusDescription(taxi.status)
						+ ", T.current = "
						+ T.currentTimePoint
						+ ", parcel request id = "
						+ pr.id
						+ ", T.current = "
						+ t
						+ ", fromIdx = "
						+ fromIdx
						+ ", "
						+ "lastIndexPoint = "
						+ taxi.lastIndexPoint
						+ ", lastPoint = "
						+ taxi.lastPoint
						+ ", depTime at formIdx = "
						+ taxi.currentItinerary.getDepartureTime(fromIdx));

			}
		}
		if (taxi.status == VehicleStatus.REST_AT_PARKING) {
			// if(taxi.ID == 161){
			// System.out.println("SimulatorBookedRequest::insertParcelRequest, taxi 161 REST AT PARKING, curPos = lastPoint = "
			// + curPos);
			// log.println("SimulatorBookedRequest::insertParcelRequest, taxi 161 REST AT PARKING, curPos = lastPoint = "
			// + curPos);
			// System.exit(-1);
			// }
			taxi.mStatus.clear();
			taxi.mStatus.put(0, VehicleStatus.GOING_TO_PICKUP_PARCEL);
			taxi.status = VehicleStatus.GOING_TO_PICKUP_PARCEL;
			int[] path = new int[1];
			ArrayList<Integer> requestID = new ArrayList<Integer>();
			ArrayList<VehicleAction> actions = new ArrayList<VehicleAction>();
			path[0] = curPos;
			requestID.add(-1);
			actions.add(VehicleAction.PASS);
			taxi.currentItinerary = new ItineraryTravelTime(path, requestID,
					actions);

			// taxi.currentItinerary.addPoint(curPos);
			// taxi.currentItinerary.addAction(VehicleAction.PASS);
			// taxi.currentItinerary.addRequestID(-1);

			taxi.lastIndexPoint = 0;
			fromIdx = 0;
			taxi.addItinerary(taxi.currentItinerary);
			System.out
					.println("SimulatorBookedRequests::insertParcelRequest, setup  new itinerary for taxi "
							+ taxi.ID);
			if (taxi.ID == taxiID)
				log.println("SimulatorBookedRequests::insertParcelRequest, status = REST_AT_PARKING --> setup  new itinerary for taxi "
						+ taxi.ID);
			pbts.entities.Parking P = findParking(taxi.lastPoint);
			if (P != null) {
				P.load--;
				P.lastUpdateTimePoint = T.currentTimePoint;
			}
		} else {
			if (fromIdx >= 0) {
				int locID = taxi.currentItinerary.get(taxi.currentItinerary
						.size() - 1);
				pbts.entities.Parking P = findParking(locID);
				if (P != null) {
					P.load--;
				}
				System.out
						.println("SimulatorBookedRequests::insertParcelRequest, cancelSubItinerary("
								+ fromIdx
								+ " + 1), of taxi "
								+ taxi.ID
								+ ", size of currentItinerary  = "
								+ taxi.currentItinerary.size());
				if (taxi.ID == taxiID) {
					log.println("SimulatorBookedRequests::insertParcelRequest, cancelSubItinerary("
							+ fromIdx
							+ " + 1), of taxi "
							+ taxi.ID
							+ ", size of currentItinerary  = "
							+ taxi.currentItinerary.size()
							+ ", before cancel, I = ");
					taxi.currentItinerary.writeToFile(log);
				}
				t = taxi.currentItinerary.getDepartureTime(fromIdx);
				if (t < 0) {// fromIdx will be a parking
					t = taxi.currentItinerary.getArrivalTime(fromIdx);
					if (taxi.currentItinerary.getAction(fromIdx) != VehicleAction.STOP) {
						log.println("SimulateBookedRequest::insertParcelRequest, BUG AT STOP????, fromIdx = "
								+ fromIdx
								+ ", currentItinerary.sz = "
								+ taxi.currentItinerary.size());

					}
				}
				taxi.cancelSubItinerary(fromIdx + 1);

				curPos = taxi.currentItinerary.get(fromIdx);
				if (taxi.ID == taxiID) {
					log.println("SimulatorBookedRequests::insertParcelRequest, cancelSubItinerary("
							+ fromIdx
							+ " + 1), of taxi "
							+ taxi.ID
							+ ", size of currentItinerary  = "
							+ taxi.currentItinerary.size()
							+ ", after cancel, I = ");
					taxi.currentItinerary.writeToFile(log);
				}
				// if(taxi.ID == 161){
				// log.println("SimulatorBookedRequests::insertParcelRequest, taxi 161, fromIdx = "
				// + fromIdx + ", curPos = " + curPos);
				// }

			}
		}

		Itinerary I1 = dijkstra.queryShortestPath(curPos, pr.pickupLocationID);
		Itinerary I2 = dijkstra.queryShortestPath(pr.pickupLocationID,
				pr.deliveryLocationID);
		double minD = dijkstra.infinity;
		pbts.entities.Parking sel_P = null;
		for (int k = 0; k < lstParkings.size(); k++) {
			pbts.entities.Parking P = lstParkings.get(k);
			if (P.load < P.capacity) {
				double D = dijkstra.queryDistance(pr.deliveryLocationID,
						P.locationID);
				if (D < minD) {
					minD = D;
					sel_P = P;
				}
			}
		}
		int destinationLocationID = mTaxi2Depot.get(taxi.ID);
		if (sel_P != null) {
			sel_P.load++;
			sel_P.lastUpdateTimePoint = T.currentTimePoint;
			destinationLocationID = sel_P.locationID;
		}
		Itinerary I3 = dijkstra.queryShortestPath(pr.deliveryLocationID,
				destinationLocationID);

		for (int i = 1; i < I1.size() - 1; i++) {
			taxi.currentItinerary.addPoint(I1.get(i));
			taxi.currentItinerary.addAction(VehicleAction.PASS);
			taxi.currentItinerary.addRequestID(-1);
		}
		taxi.currentItinerary.addPoint(pr.pickupLocationID);
		taxi.currentItinerary.addAction(VehicleAction.PICKUP_PARCEL);
		taxi.currentItinerary.addRequestID(pr.id);
		taxi.mStatus.put(taxi.currentItinerary.size() - 1,
				VehicleStatus.PICKUP_PARCEL);
		// if(taxi.ID == 161){
		// log.println("SimulatorBookedRequests::insertParcelRequest, taxi 161, SET PICKUP_PARCEL at point "
		// + pr.pickupLocationID);
		// }
		for (int i = 1; i < I2.size() - 1; i++) {
			taxi.currentItinerary.addPoint(I2.get(i));
			taxi.currentItinerary.addAction(VehicleAction.PASS);
			taxi.currentItinerary.addRequestID(-1);
		}
		taxi.currentItinerary.addPoint(pr.deliveryLocationID);
		taxi.currentItinerary.addAction(VehicleAction.DELIVERY_PARCEL);
		taxi.currentItinerary.addRequestID(pr.id);
		taxi.mStatus.put(taxi.currentItinerary.size() - 1,
				VehicleStatus.DELIVERY_PARCEL);
		// if(taxi.ID == 161){
		// log.println("SimulatorBookedRequests::insertParcelRequest, taxi 161, SET DELIVERY_PARCEL at point "
		// + pr.deliveryLocationID);
		// }
		for (int i = 1; i < I3.size() - 1; i++) {
			taxi.currentItinerary.addPoint(I3.get(i));
			taxi.currentItinerary.addAction(VehicleAction.PASS);
			taxi.currentItinerary.addRequestID(-1);
		}
		taxi.currentItinerary.addPoint(I3.get(I3.size() - 1));
		taxi.currentItinerary.addAction(VehicleAction.STOP);
		taxi.currentItinerary.addRequestID(-1);
		taxi.mStatus.put(taxi.currentItinerary.size() - 1,
				VehicleStatus.REST_AT_PARKING);
		// if(taxi.ID == 161){
		// log.println("SimulatorBookedRequests::insertParcelRequest, taxi 161, SET REST_AT_PARKING at point "
		// + I3.get(I3.size()-1) + " = " + sel_P.locationID);
		// }
		// ArrayList<PeopleRequest> peopleReq = new ArrayList<PeopleRequest>();
		// ArrayList<ParcelRequest> parcels = new ArrayList<ParcelRequest>();
		// parcels.add(pr);

		assignTimePoint(taxi.currentItinerary, taxi, fromIdx, t + 1,
				insertedPeopleRequests, insertedParcelRequests);
		for (int i = fromIdx + 1; i < taxi.currentItinerary.size(); i++) {
			if (taxi.currentItinerary.getArrivalTime(i) < 0) {
				System.out
						.println("SimulatorBookedRequests::insertParcelRequest, FAILED assignTimePoint taxi "
								+ taxi.ID
								+ ", at T = "
								+ T.currentTimePoint
								+ ", arrTime("
								+ i
								+ ") < 0, fromIdx = "
								+ fromIdx
								+ ", Itinerary.sz = "
								+ taxi.currentItinerary.size());
				taxi.currentItinerary.writeToFile(log);
				log.close();
				System.exit(-1);
			}
		}
		if (taxi.ID == taxiID) {
			log.println("SimulatorBookedRequests::insertParcelRequest, taxi "
					+ taxi.ID + ", At time T.current = " + T.currentTimePoint
					+ ", setup Itinerary: ");
			taxi.currentItinerary.writeToFile(log);
		}
		return false;
	}

	/*
	 * public void processParcelRequest(ParcelRequest pr){
	 * 
	 * ServiceSequence sel_ss = null; Vehicle sel_taxi = null; for(int k = 0; k
	 * < vehicles.size(); k++){ Vehicle taxi = vehicles.get(k);
	 * if(taxi.remainRequestIDs.size() > maxPendingStops) continue;
	 * ServiceSequence ss = computeBestProfitsParcelInsertion(taxi, pr); if(ss
	 * != null){
	 * //System.out.println("SimulatorBookedRequest::processPeopleRequest, taxi "
	 * + taxi.ID + //", profits = " + ss.profitEvaluation); if(sel_ss == null){
	 * sel_ss = ss; sel_taxi = taxi; }else{ if(sel_ss.profitEvaluation <
	 * ss.profitEvaluation){ sel_ss = ss; sel_taxi = taxi; } } } } if(sel_taxi
	 * == null){ System.out.println(
	 * "SimulatorBookedRequest::processParcelRequest --> request " + pr.id +
	 * " is REJECTED");
	 * log.println("SimulatorBookedRequest::processParcelRequest --> request " +
	 * pr.id + " is REJECTED"); return; } int nextStartTimePoint =
	 * sel_taxi.getNextStartTimePoint(); int fromPoint =
	 * sel_taxi.getNextStartPoint(); int fromIndex =
	 * sel_taxi.getNextStartPointIndex();
	 * 
	 * System.out.println(
	 * "SimulatorBookedRequest::processParcelRequest, sequence = " +
	 * sel_ss.getSequence() + ", maxProfits = " + sel_ss.profitEvaluation +
	 * ", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = " +
	 * nextStartTimePoint + ", fromIndex = " + fromIndex + ", fromPoint = " +
	 * fromPoint);
	 * log.println("SimulatorBookedRequest::processParcelRequest, sequence = " +
	 * sel_ss.getSequence() + ", maxProfits = " + sel_ss.profitEvaluation +
	 * ", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = " +
	 * nextStartTimePoint + ", fromIndex = " + fromIndex + ", fromPoint = " +
	 * fromPoint); admitNewItinerary(sel_taxi, nextStartTimePoint, fromIndex,
	 * fromPoint, sel_ss); } public void processPeopleRequest(PeopleRequest pr){
	 * ServiceSequence sel_ss = null; Vehicle sel_taxi = null; int taxiID = 1;
	 * for(int k = 0; k < vehicles.size(); k++){ Vehicle taxi = vehicles.get(k);
	 * if(taxi.remainRequestIDs.size() > maxPendingStops) continue;
	 * ServiceSequence ss = computeBestProfitsPeopleInsertion(taxi, pr); if(ss
	 * != null){
	 * //System.out.println("SimulatorBookedRequest::processPeopleRequest, taxi "
	 * + taxi.ID + //", profits = " + ss.profitEvaluation); if(sel_ss == null){
	 * sel_ss = ss; sel_taxi = taxi; }else{ if(sel_ss.profitEvaluation <
	 * ss.profitEvaluation){ sel_ss = ss; sel_taxi = taxi; System.out.println(
	 * "SimulatorBookedRequest::processPeopleRequest, UPDATE sel_taxi " +
	 * sel_taxi.ID + ", sel_ss.profits = " + sel_ss.profitEvaluation); if(taxiID
	 * == sel_taxi.ID)
	 * log.println("SimulatorBookedRequest::processPeopleRequest, UPDATE sel_taxi "
	 * + sel_taxi.ID + ", sel_ss.profits = " + sel_ss.profitEvaluation); } } } }
	 * if(sel_taxi == null){ System.out.println(
	 * "SimulatorBookedRequest::processPeopleRequest --> request " + pr.id +
	 * " is REJECTED");
	 * log.println("SimulatorBookedRequest::processPeopleRequest --> request " +
	 * pr.id + " is REJECTED"); return; } int nextStartTimePoint =
	 * sel_taxi.getNextStartTimePoint(); int fromPoint =
	 * sel_taxi.getNextStartPoint(); int fromIndex =
	 * sel_taxi.getNextStartPointIndex(); System.out.println(
	 * "SimulatorBookedRequest::processPeopleRequest, sequence = " +
	 * sel_ss.getSequence() + ", maxProfits = " + sel_ss.profitEvaluation +
	 * ", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = " +
	 * nextStartTimePoint + ", fromIndex = " + fromIndex + ", fromPoint = " +
	 * fromPoint); if(taxiID == sel_taxi.ID)log.println(
	 * "SimulatorBookedRequest::processPeopleRequest, sequence = " +
	 * sel_ss.getSequence() + ", maxProfits = " + sel_ss.profitEvaluation +
	 * ", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = " +
	 * nextStartTimePoint + ", fromIndex = " + fromIndex + ", fromPoint = " +
	 * fromPoint); admitNewItinerary(sel_taxi, nextStartTimePoint, fromIndex,
	 * fromPoint, sel_ss);
	 * 
	 * //admitNewItinerary(sel_taxi, nextStartTimePoint, fromIndex, fromPoint,
	 * sel_ss); }
	 */

	public void receiveRequests() {
		queueParcelReq.clear();
		queuePeopleReq.clear();
		while (runningPeopleRequests.size() > 0) {
			PeopleRequest peopleR = runningPeopleRequests.get(0);
			if (peopleR.timePoint <= T.currentTimePoint + TimePointDuration) {
				runningPeopleRequests.remove(0);
				queuePeopleReq.add(peopleR);
			} else {
				break;
			}
		}
		while (runningParcelRequests.size() > 0) {
			ParcelRequest parcelR = runningParcelRequests.get(0);
			if (parcelR.timePoint <= T.currentTimePoint + TimePointDuration) {
				runningParcelRequests.remove(0);
				queueParcelReq.add(parcelR);
			} else {
				break;
			}
		}

	}

	public void simulateDataFromFile(String requestFilename,
			int maxNbParcelsInserted, int maxNbStops) {
		double t0 = System.currentTimeMillis();
		loadRequestsSARP2014(requestFilename);
		
		//loadRequests(requestFilename, this.terminateRequestTime);
		
		// System.exit(-1);

		// if(true) return;

		// initTimeHorizon();

		pendingParcelRequests = new ArrayList<ParcelRequest>();

		initVehicles();

		distanceRequests = new ArrayList<Double>();

		queuePeopleReq = new ArrayList<PeopleRequest>();
		queueParcelReq = new ArrayList<ParcelRequest>();

		// EventGenerator eg = new EventGenerator(map,T);
		// eg.log = log;
		System.out.println("T = " + T + ", nbTaxis = " + nbTaxis);
		// if(true) return;

		// for(int t = startWorkingTime; t < terminateWorkingTime; t++){
		//[SonNV] this variable is not used
		//boolean ok = false;
		// PeopleRequest peopleR = null;
		// ParcelRequest parcelR = null;

		runningParcelRequests = new ArrayList<ParcelRequest>();
		runningPeopleRequests = new ArrayList<PeopleRequest>();
		for (int i = 0; i < allPeopleRequests.size(); i++) {
			PeopleRequest pr = allPeopleRequests.get(i);
			pr.maxNbStops = pr.maxNbStops < maxNbStops ? pr.maxNbStops
					: maxNbStops;
			runningPeopleRequests.add(pr);
			// System.out.println(pr.id + ", timePoint = " + pr.timePoint);
			// if(pr.timePoint < 0){

			// System.exit(-1);
			// }
		}
		for (int i = 0; i < allParcelRequests.size(); i++) {
			ParcelRequest pr = allParcelRequests.get(i);

			runningParcelRequests.add(pr);
		}

		// System.out.println("runnningPeopleReq.sz = " +
		// runningPeopleRequests.size() + ", runningParcelReq.sz = " +
		// runningParcelRequests.size());
		// if(true)return;
		receiveRequests();

		insertedParcelRequests = new ArrayList<ParcelRequest>();
		insertedPeopleRequests = new ArrayList<PeopleRequest>();

		// System.out.println("START WHILE.....T.current = " +
		// T.currentTimePoint + ", T.end = " + T.end);
		while (!T.finished()) {
			// System.out.println("WHILE T.current = " + T.currentTimePoint +
			// ", T.end = " + T.end);
			// for(int t = startWorkingTime; t < startWorkingTime+3600*2; t++){
			// log.println("time point " + t);
			// int t = T.currentTimePoint;
			// System.out.println("SimulatorBookedRequests::simulateFromFile, t = "
			// + t + ", T.end = " + T.end);
			if((T.currentTimePoint - T.start)%10 == 0 && T.currentTimePoint > T.start){
				if(!T.stopRequest() || !allTaxiRestAtParking())
					logStatistic();
			}
			for (int k = 0; k < nbTaxis; k++) {
				Vehicle vh = vehicles.get(k);
				if (vh.status != VehicleStatus.STOP_WORK)
					vh.moveLong(Simulator.TimePointDuration);

			}

			if (T.stopRequest()) {
				// System.out.println("Simulator::simulate stop request!!!!");
				for (int k = 0; k < nbTaxis; k++) {
					Vehicle vh = vehicles.get(k);
					// if(vh.ID == 4){
					// System.out.println("Simulator::simulateDataFromFile, taxi "
					// + vh.ID + ", stopRequest, status = " +
					// Vehicle.getStatusDescription(vh.status));
					// System.exit(-1);
					// }
					if (vh.status == VehicleStatus.TRAVEL_WITHOUT_LOAD) {
						System.out
								.println(name()
										+ "::simulateDataFromFile, taxi "
										+ vh.ID
										+ ", stopRequest, status = TRAVEL_WITHOUT_LOAD --> GO BACK TO DEPOT");
						setupRouteBack2Depot(vh);
					} else if (vh.status == VehicleStatus.REST_AT_PARKING) {
						if (vh.lastPoint != mTaxi2Depot.get(vh.ID)) {
							System.out
									.println(name()
											+ "::simulateDataFromFile, taxi "
											+ vh.ID
											+ ", stopRequest, status = REST_AT_PARKING --> GO BACK TO DEPOT");
							setupRouteBack2Depot(vh);
						} else {
							vh.status = VehicleStatus.STOP_WORK;
						}
					}
				}
			}

			// System.out.println("WHILE queueParcel = " + queueParcelReq.size()
			// + " queuePeople = " + queuePeopleReq.size());

			//[SonNV] Because this statistics are contained others, should remove it.
			for (int i = 0; i < queueParcelReq.size(); i++) {
				ParcelRequest parcelR = queueParcelReq.get(i);

				// if (parcelR.timePoint == t) {
				totalParcelRequests++;
				if (T.stopRequest()) {
					nbParcelRejects++;
					/*
					 * System.out .println(name() +
					 * "::simulateDataFromFile --> reject parcel request due to work session closed, "
					 * + "nbParcelRejects = " + nbParcelRejects +
					 * ", totalParcelRequets Arrive = " + totalParcelRequests +
					 * "/" + allParcelRequests.size());
					 */
					// if(runningParcelRequests.size() > 0){
					// parcelR = runningParcelRequests.get(0);
					// runningParcelRequests.remove(0);
					// }
					// continue;
				} else {
					ParcelRequest pr = parcelR;
					// allParcelRequests.add(pr);
					System.out.println(name()
							+ "::simulateDataFromFile --> At "
							+ T.timePointHMS(pr.timePoint)
							// + T.currentTimePointHMS()
							+ "[" + T.currentTimePoint + "], parcel request "
							+ pr.id + " arrives");
					log.println(name() + "::simulateDataFromFile --> At "
							+ T.timePointHMS(pr.timePoint)
							// + T.currentTimePointHMS()
							+ "[" + T.currentTimePoint + "], parcel request "
							+ pr.id + " arrives");
					Itinerary I = dijkstra.solve(pr.pickupLocationID,
							pr.deliveryLocationID);
					// if(I.getDistance() >= dijkstra.infinity){
					if (I == null) {
						log.println("At " + T.currentTimePointHMS()
								+ ", cannot serve parcel request from "
								+ pr.pickupLocationID + " to "
								+ pr.deliveryLocationID);
						System.out.println(name()
								+ "::simulateDataFromFile --> At "
								+ T.currentTimePointHMS()
								+ ", cannot serve parcel request from "
								+ pr.pickupLocationID + " to "
								+ pr.deliveryLocationID);
						nbDisconnectedRequests++;
					} else {

						// planner.processParcelRequest(pr);
					}
				}

				// if (runningParcelRequests.size() > 0) {
				// parcelR = runningParcelRequests.get(0);
				// runningParcelRequests.remove(0);
				// }

			}
			if (!T.stopRequest() && queueParcelReq.size() > 0)
				planner.processParcelRequests(queueParcelReq);
			
			// System.out.println("peopleReq.timePoint = " + peopleR.timePoint +
			// ", t = " + t);
			for (int i = 0; i < queuePeopleReq.size(); i++) {
				PeopleRequest peopleR = queuePeopleReq.get(i);
				// if (peopleR.timePoint == t) {

				totalPeopleRequests++;
				if (T.stopRequest()) {
					nbPeopleRejects++;
					/*
					 * System.out .println(name() +
					 * "::simulateDataFromFile --> reject people request due to work session closed, "
					 * + "nbPeopleRejects = " + nbPeopleRejects +
					 * ", totalPeopleRequets Arrive = " + totalPeopleRequests +
					 * "/" + allPeopleRequests.size());
					 */
					// if (runningPeopleRequests.size() > 0) {
					// peopleR = runningPeopleRequests.get(0);
					// runningPeopleRequests.remove(0);
					// }
					// continue;
				} else {

					PeopleRequest pr = peopleR;
					// allPeopleRequests.add(pr);
					System.out.println(name()
							+ "::simulateDataFromFile --> At "
							+ T.timePointHMS(pr.timePoint)
							// + T.currentTimePointHMS()
							+ "[" + T.currentTimePoint + "], people request "
							+ pr.id + " arrives");
					log.println(name() + "::simulateDataFromFile --> At "
							+ T.timePointHMS(pr.timePoint)
							// + T.currentTimePointHMS()
							+ "[" + T.currentTimePoint + "], people request "
							+ pr.id + " arrives");
					// if(pr.id == 242){
					// System.exit(-1);
					// }
					// Itinerary I = dijkstra.solve(pr.pickupLocationID,
					// pr.deliveryLocationID);
					double D = dijkstra.queryDistance(pr.pickupLocationID,
							pr.deliveryLocationID);
					// if(I.getDistance() >= dijkstra.infinity){
					// if(I == null){
					if (D > dijkstra.infinity - 1) {
						log.println("At " + T.currentTimePointHMS()
								+ ", cannot serve people request from "
								+ pr.pickupLocationID + " to "
								+ pr.deliveryLocationID
								+ " due to disconnectivity");
						System.out.println(name()
								+ "::simulateDataFromFile --> At "
								+ T.currentTimePointHMS()
								+ ", cannot serve people request from "
								+ pr.pickupLocationID + " to "
								+ pr.deliveryLocationID
								+ " due to disconnectivity");
						nbDisconnectedRequests++;
					} else {

						// planner.processPeopleRequest(pr);
					}
				}
				// if (runningPeopleRequests.size() > 0) {
				// peopleR = runningPeopleRequests.get(0);
				// runningPeopleRequests.remove(0);
				// }

			}

			if (!T.stopRequest() && queuePeopleReq.size() > 0)
				planner.processPeopleRequests(queuePeopleReq);

			nbParcelRequestsProcessed += queueParcelReq.size();
			nbPeopleRequestsProcessed += queuePeopleReq.size();
			
			T.move(Simulator.TimePointDuration);
			receiveRequests();
			// log.println("-------------------");
		}

		double totalDistance = 0;
		int nbUnusedTaxis = 0;
		for (int k = 0; k < vehicles.size(); k++) {
			Vehicle taxi = vehicles.get(k);
			if (taxi.totalTravelDistance <= 0) {
				nbUnusedTaxis++;
				continue;
			}
			// taxi.writeItinerriesToLog();
			totalDistance = totalDistance + taxi.totalTravelDistance;
			int costi = (int) (taxi.totalTravelDistance * gamma3 / 1000);
			log.println("distance of taxi[" + taxi.ID + "] = "
					+ taxi.totalTravelDistance / 1000 + "km, cost fuel = "
					+ costi + "K");
		}
		// logI.println(-2);

		for (int i = 0; i < distanceRequests.size(); i++) {
			double D = distanceRequests.get(i);
			int m = (int) (alpha + gamma1 * D);
			D = D / 1000;
			m = m / 1000;
			log.println("requests " + i + " has distance = " + D
					+ "km, money = " + m + "K");
		}

		cost = (int) totalDistance * gamma3;
		cost = cost / 1000;
		totalDistance = totalDistance / 1000;
		revenue = revenue / 1000;
		log.println("nbPeopleRequests = " + totalPeopleRequests);
		log.println("nbAcceptedPeople = " + acceptedPeopleRequests);
		log.println("nbParcelRequests = " + totalParcelRequests);
		log.println("nbAcceptedParcelRequests = " + acceptedParcelRequests);
		log.println("nbDisconnected Requests = " + nbDisconnectedRequests);
		log.println("total distance = " + totalDistance + "km");
		log.println("revenue = " + revenue + "K");
		log.println("cost fuel = " + cost + "K");
		double benefits = revenue - cost;
		log.println("benefits = " + benefits + "K");
		log.println("nbUnusedTaxis = " + nbUnusedTaxis);

		System.out
				.println("Simulator::simulateWithAParcelFollow --> FINISHED, allPeopleRequests.sz = "
						+ allPeopleRequests.size()
						+ ", allParcelRequests.sz = "
						+ allParcelRequests.size());

		double t = System.currentTimeMillis() - t0;
		t = t * 0.001;
		System.out.println("simulation time = " + t);
	}

	public static void runBatch() {
		int prob[] = { 10, 3, 2, 1 };
		int start_idx[] = { 1, 1, 1, 1 };
		int end_idx[] = { 1, 1, 5, 5 };
		int nbIns = 5;

		// for(int i = 0; i < prob.length; i++){
		for (int i = 0; i < 1; i++) {
			for (int k = start_idx[i]; k <= end_idx[i]; k++) {
				String requestFN = "requests-long-people-" + prob[i]
						+ "-parcel-" + prob[i] + ".ins" + k + ".txt";
				// requestFN = "quangnn\\requests-people-21-parcel-7.ins1.txt";
				System.out.println(requestFN);
				// System.exit(-1);
				// String itinerariesFN =
				// "direct-people-direct-parcel-service-itinerary-" + requestFN;
				// String itinerariesFN = "direct-people-service-itinerary-" +
				// requestFN;
				// String itinerariesFN = "sharing-best-insertion-itinerary-" +
				// requestFN;
				// String itinerariesFN =
				// "preserve-next-service-point-itinerary-" + requestFN;
				// String itinerariesFN =
				// "/Users/dungpq/Downloads/20150208/result_requests-long-people-10-parcel-10.ins1.txt";
				// String itinerariesFN =
				// "C:\\Users\\DHBK\\Downloads\\20150208\\Output\\result_requests-long-people-10-parcel-10.ins1.txt";
				String itinerariesFN = "result_requests-long-people-10-parcel-10.ins1.txt";
				// String itinerariesFN =
				// "C:\\Users\\DHBK\\Downloads\\20150208\\Output\\result_requests-people-3-parcel-3.ins5.txt";
				// String itinerariesFN =
				// "tuan_result_requests-long-people-10-parcel-10.ins1.txt";

				SimulatorTimeUnit sim = new SimulatorTimeUnit();
				sim.loadMapFromTextFile("map-hanoi-connected.txt");
				// sim.loadMapFromTextFile("data-tokyo\\DataMap.txt");
				// if(true) return;
				// sim.loadParameters("quangnn\\config-parameters.txt");
				sim.loadParameters("config-parameters.txt");
				// sim.loadDepotParkings("quangnn\\depots10-parkings10.txt");
				sim.loadDepotParkings("depots300-parkings20.txt");

				GreedyWithSharing planner = new GreedyWithSharing(sim);
				// GreedyWithSharingPreserveNextServicePoint planner = new
				// GreedyWithSharingPreserveNextServicePoint(sim);
				// GreedyDirectPeopleService planner = new
				// GreedyDirectPeopleService(sim);
				// GreedyDirectPeopleDirectParcelServices planner = new
				// GreedyDirectPeopleDirectParcelServices(sim);
				sim.setPlanner(planner);

				// sim.simulateDataFromFile(requestFN,1,2);
				// sim.writeTaxiItineraries(itinerariesFN);

				sim.initVehicles();
				sim.loadRequests(requestFN);
				HashMap<Integer, ItineraryTravelTime> itineraries = sim
						.loadItineraries(itinerariesFN);
				sim.analyzeSolution(itineraries, "analyzer.txt");

				sim.finalize();
			}
		}
	}

	public boolean resultAvailable(String path){
		try{
			File f = new File(path);
			if(f.exists() && f.isFile()) return true;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return false;
	}
	public void analyzeRequest(int maxTimeRequest, String requestFN, String dir){
		String requestPath = dir + dir_slash + requestFN;
		loadRequests(requestPath, requestFN, maxTimeRequest);
	}
	public void run(int maxTimeRequest, int timePointDuration,
			String requestFN, OnlinePlanner planner, int maxNbPendingStops, String dir) {
		try {
			String fn = dir + dir_slash + requestFN + "-planner" + planner.name()
					+ "-TimeReq" + maxTimeRequest + "-queueDuration"
					+ timePointDuration + "-maxPendingStops" + maxNbPendingStops;
			String fnItinerary = fn + "-itinerary.txt";
			String fnSummary = fn + "-summary.xml";

			String mapFN = "SanfranciscoRoad-connected-contracted-5-refine-50.txt";	// max
																					// Arc
																					// length
																					// =
																					// 50m
			String configParametersFN = "config-parameters.txt";
			String depotParkingFN = "depot600-parkings54.txt";
			
			if(resultAvailable(fnSummary)){
				System.out.println("result file " + fnSummary + " exists");
				return;
			}else{
				System.out.println("result file " + fnSummary + " NOT exists");
			}
			
			initialize();
			
			loadMapFromTextFile(dir + dir_slash + mapFN);

			loadParameters(dir + dir_slash + configParametersFN);
			loadDepotParkings(dir + dir_slash + depotParkingFN);

			String requestPath = dir + dir_slash + requestFN;

			this.terminateRequestTime = maxTimeRequest;
			this.TimePointDuration = timePointDuration;
			this.maxPendingStops = maxNbPendingStops;
					
			setPlanner(planner);
			statFilename = fn + "-statistic-progress.txt";
			
			resetLog();
					
			initTimeHorizon();

			simulateDataFromFile(requestPath, 1, 2);
			writeTaxiItineraries(fnItinerary);

			initVehicles();
			loadRequests(requestPath);

			relaxTimeWindowParcelRequests();
			HashMap<Integer, ItineraryTravelTime> itineraries = loadItineraries(fnItinerary);
			analyzeSolution(itineraries, fnSummary);
			
			finalize();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void getPredictionInfo(){
		listPredictedPoints = new HashMap<Integer, ArrayList<Integer>>();
		for(int period = 0; period < 96; period++){
			listPredictedPoints.put(period, frs.getRequests(period));
		}
	}
	
	//calculate the number of popular points near parking p at all periods.
	public void initilizeParkingScore(){
		for(int i = 0; i < lstParkings.size(); i++){
			Parking p = lstParkings.get(i);
			for(int period = 0; period < 96; period++){
				int nPp = 0;//the number of popular points
				ArrayList<Integer> PpList = listPredictedPoints.get(period);
				for(int j = 0; j < PpList.size(); j++){
					if(estimateTravelingDistanceHaversine(PpList.get(j), p.locationID) < 5)
						nPp++;
				}
				p.nPpNearInPeriod.put(period, nPp);
			}
			lstParkings.set(i, p);
		}
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//[SonNV] change path selected.
		String data_dir = "E:\\Project\\pbts\\git_project\\SPT\\";
		String mapFileName = data_dir + "SanFrancisco_std\\SanfranciscoRoad-connected-contracted-5-refine-50.txt";
		String configFileName = data_dir + "SanFrancisco_std\\config-parameters.txt";
		String depotParkingFileName = data_dir + "SanFrancisco_std\\depots1000-parkings34.txt";
		int maxNbPendingStops = 10;
		int maxTimeReceiveRequest = 86400;
		int startSimulationTime = 0;
		int decisionTime = 15;
		
		for(int day = 1; day <= 9; day++){
			String requestFileName = data_dir + "SanFrancisco_std\\ins_day_" + day +"_minSpd_5_maxSpd_60.txt";
			double maxBenefits = 0;
			int plIdx = 0;
			ArrayList<String> listPlanner = new ArrayList<String>();			
			//listPlanner.add("GreedyExchangeSharingDecisionTimeLimitPlanner");
			//listPlanner.add("GreedyExSharingDecisionTimeLimitAndGetManyTimesThenAddAPopularPointPlanner");
			//listPlanner.add("dynamicSARPplanner");
			listPlanner.add("GreedyExSharingDecisionTimeLimitAndBestParkingPlanner");
			//listPlanner.add("GreedySharingNoExchangeDecisionTimeLimitPlanner");
			//listPlanner.add("GreedySharingNoExchangeDecisionTimeLimitAndGetManyTimesThenAddAPopularPointPlanner");
			//listPlanner.add("SequenceDecidedBasedOnAPopularPointPlanner");
			//listPlanner.add("GetManyTimesThenAddAPopularPointPlanner");
			//listPlanner.add("GetManyTimesThenAddASequencePopularPointsPlanner");
			
			for(int pl = 0; pl < listPlanner.size(); pl++){
				String plannerName = listPlanner.get(pl);
				String progressiveStatisticFileName = data_dir + "SanFrancisco_std\\debug\\ins_day_" + day +"_minSpd_5_maxSpd_60.txt-planner"+ plannerName + "-maxPendingStops10-decisionTime15-statistic-progress.txt";
				String itineraryFileName = data_dir + "SanFrancisco_std\\debug\\ins_day_" + day +"_minSpd_5_maxSpd_60.txt-planner"+ plannerName + "-maxPendingStops10-decisionTime15-itinerary.txt";
				String summaryFileName = data_dir + "SanFrancisco_std\\debug\\ins_day_" + day +"_minSpd_5_maxSpd_60.txt-planner"+ plannerName + "-maxPendingStops10-decisionTime15-summary.xml";
				
				
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
				if(plannerName != "dynamicSARPplanner"){
					SimulatorTimeUnit simulator = new SimulatorTimeUnit();
					
					simulator.loadMapFromTextFile(mapFileName);
					simulator.loadParameters(configFileName);
					simulator.loadDepotParkings(depotParkingFileName);
					simulator.initialize();
					simulator.statFilename = progressiveStatisticFileName;
					simulator.resetLog();
					simulator.getPredictionInfo();
					simulator.initilizeParkingScore();
					
					OnlinePlanner[] planners = new OnlinePlanner[] { 	//new GreedyExchangeSharingPlanner(simulator), 
																		//new GreedyExchangeSharingPlanner(simulator),
																		//new GreedyPeopleDirectExchangePlanner(simulator),
																		//new GreedyPeopleDirectNoExchangePlanner(simulator),
																		//new NaiveSequentialPlanner(simulator),
																		new GreedyExchangeSharingDecisionTimeLimitPlanner(simulator),
																		new GreedyExSharingDecisionTimeLimitAndGetManyTimesThenAddAPopularPointPlanner(simulator),
																		new GreedyExSharingDecisionTimeLimitAndBestParkingPlanner(simulator),
																		//new GreedySharingNoExchangeDecisionTimeLimitPlanner(simulator),
																		//new GreedySharingNoExchangeDecisionTimeLimitAndGetManyTimesThenAddAPopularPointPlanner(simulator),
																		//new NaiveSequentialDecisionTimeLimitPlanner(simulator),
																		//new SequenceDecidedBasedOnAPopularPointPlanner(simulator),
																		//new GetManyTimesThenAddAPopularPointPlanner(simulator),
																		//new GetManyTimesThenAddASequencePopularPointsPlanner(simulator)
																	};
					
					OnlinePlanner selectedPlanner = null;
					
					for(int i = 0; i < planners.length; i++){
						if(planners[i].name().equals(plannerName)){
							selectedPlanner = planners[i];
							break;
						}
					}
					
					simulator.terminateRequestTime = maxTimeReceiveRequest;
					simulator.TimePointDuration = decisionTime;
					simulator.maxPendingStops = maxNbPendingStops;
					simulator.startWorkingTime = startSimulationTime;
					
					simulator.setPlanner(selectedPlanner);
					
					simulator.initTimeHorizon();
					
					simulator.simulateDataFromFile(requestFileName, 1, 2);
					simulator.writeTaxiItineraries(itineraryFileName);
			
					simulator.initVehicles();
					simulator.loadRequestsSARP2014(requestFileName);
			
					simulator.relaxTimeWindowParcelRequests();
					HashMap<Integer, ItineraryTravelTime> itineraries = simulator.loadItineraries(itineraryFileName);
					AnalysisTemplate AT = simulator.analyzeSolution(itineraries, summaryFileName);
					if(AT.benefits > maxBenefits){
						maxBenefits = AT.benefits;
						plIdx = pl+1;
					}
					simulator.finalize();
				}
				else{
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
			}
		}
		
		if(true) return;
		
		
		// SimulatorBookedRequests sim = new SimulatorBookedRequests();
		// sim.loadMapFromTextFile("map-hanoi-connected.txt");
		// sim.checkDistance("shortestPath.txt");
		// sim.checkDistance(59,10);

		// SimulatorBookedRequests.runBatch();
		// if(true) return;

		// String dir =
		// "C:\\DungPQ\\projects\\prediction-based-transport-scheduling\\data\\SanFrancisco";
		//String dir = "SanFrancisco";
		String dir = "data/SanFrancisco_std";
		
		// String dir =
		// "C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\tuan\\datacheck\\data01000000";
		SimulatorTimeUnit sim = new SimulatorTimeUnit();
		// String mapFN = "SanfranciscoRoad-connected-contracted-5.txt";// max
		// Arc length is now > 3000m
		String mapFN = "SanfranciscoRoad-connected-contracted-5-refine-50.txt";// max
																				// Arc
																				// length
																				// =
																				// 50m
		// String mapFN = "reduceGraph.txt";
		String configParametersFN = "config-parameters.txt";
		String depotParkingFN = "depot600-parkings54.txt";
		// String depotParkingFN = "depot1000-parkings54.txt";
		// String depotParkingFN = "depot2.txt";
		// String requestFN =
		// "request_people_parcel_day_1_minSpeed_5.0_maxSpeed_60.0.txt";
		// String requestFN = "requestsPeopleParcel.txt";

		sim.loadMapFromTextFile(dir + sim.dir_slash + mapFN);
		// System.exit(-1);
		// sim.loadRequests(dir + "\\request_day_1.txt");

		sim.loadParameters(dir + sim.dir_slash + configParametersFN);
		sim.loadDepotParkings(dir + sim.dir_slash + depotParkingFN);
		sim.initialize();
		sim.resetLog();

		//GreedyPeopleParcelInsertSharing planner1 = new GreedyPeopleParcelInsertSharing(sim);
		//PeopleDirectServiceGreedy planner2 = new PeopleDirectServiceGreedy(sim);
		GreedyExchangeSharingPlanner planner1 = new GreedyExchangeSharingPlanner(sim);
		GreedySharingNoExchangePlanner planner2 = new GreedySharingNoExchangePlanner(sim);
		GreedyPeopleDirectExchangePlanner planner3 = new GreedyPeopleDirectExchangePlanner(sim);
		GreedyPeopleDirectNoExchangePlanner planner4 = new GreedyPeopleDirectNoExchangePlanner(sim);
		NaiveSequentialPlanner planner5 = new NaiveSequentialPlanner(sim);
		GreedyExchangeSharingDecisionTimeLimitPlanner planner6 = new GreedyExchangeSharingDecisionTimeLimitPlanner(sim);
		GreedySharingNoExchangeDecisionTimeLimitPlanner planner7 = new GreedySharingNoExchangeDecisionTimeLimitPlanner(sim);
		NaiveSequentialDecisionTimeLimitPlanner planner8 = new NaiveSequentialDecisionTimeLimitPlanner(sim);
		SequenceDecidedBasedOnAPopularPointPlanner planner9 = new SequenceDecidedBasedOnAPopularPointPlanner(sim);
		GetManyTimesThenAddAPopularPointPlanner planner10 = new GetManyTimesThenAddAPopularPointPlanner(sim);
		GetManyTimesThenAddASequencePopularPointsPlanner planner11 = new GetManyTimesThenAddASequencePopularPointsPlanner(sim);
		
		OnlinePlanner[] planner = new OnlinePlanner[] { planner1, planner2,
				planner3, planner4, planner5, planner6, planner7,planner8};
		int[] maxTimeRequest = new int[] {75600};
		int[] timePointDuration = new int[] { 15, 30 };
		int[] maxPendingStops = new int[] { 6, 10 };

		PrintWriter exprLog = null;
		try {
			exprLog = new PrintWriter("Expr-instances-log.txt");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		//for (int d = 1; d <= 31; d++) {
		for(int d = 3; d <= 3; d++){
			String requestFN = "ins_day_" + d
					+ "_minSpd_5_maxSpd_60.txt";
			//for(int i = 0; i < timePointDuration.length; i++){
			for (int i = 0; i < 1; i++) {
				//for(int j = 0; j < maxTimeRequest.length; j++){
				for (int j = 0; j < 1; j++) {
					//for (int k = 0; k < planner.length; k++) {
					for (int k = 7; k <= 7; k++) {
						//for (int q = 0; q < maxPendingStops.length; q++) {
						for (int q = 0; q < 1; q++) {
							sim.run(maxTimeRequest[j], timePointDuration[i], requestFN, planner[k], maxPendingStops[q], dir);
							//sim.analyzeRequest(maxTimeRequest[j],requestFN, dir);
						}
					}
				}
			}
		}
		sim.finalize();
		exprLog.close();
		/*
		 * // sim.loadMapFromTextFile(
		 * "C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\Tokyo\\20090101\\data01000000\\reduceGraph.txt"
		 * ); // sim.loadParameters(
		 * "C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\Tokyo\\20090101\\config-parameters.txt"
		 * ); // sim.loadDepotParkings(
		 * "C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\Tokyo\\20090101\\data01000000\\depotparking1.txt"
		 * );
		 * 
		 * // String requestFN = "requests-people-10-parcel-10.ins2.txt"; //
		 * String requestFN = dir + "\\" + requestFN; String requestPath = dir +
		 * "\\" + requestFN; //String itinerariesFN = dir + "\\BaoxiangLi" +
		 * "\\" String itinerariesFN = dir + "\\GreedySharing" + "\\" //String
		 * itinerariesFN = dir + "\\GreedyDirectPeopleService" + "\\" //String
		 * itinerariesFN = "C:\\Users\\DHBK\\Downloads" + "\\" + requestFN + "."
		 * + depotParkingFN + ".itineraries.txt";//
		 * Itineraries-people-1-parcel-1.txt"; //String itinerariesFN = dir +
		 * "\\out.txt";
		 * 
		 * // String itinerariesFN = "result_requests-people-10-parcel-10.txt";
		 * // String itinerariesFN = "result_requests-0-1-2.txt";
		 * 
		 * //pbts.onlinealgorithmtimeunit.GreedyWithSharing planner = new
		 * pbts.onlinealgorithmtimeunit.GreedyWithSharing(sim);
		 * //pbts.onlinealgorithmtimeunit.PeopleDirectServiceGreedy planner =
		 * new pbts.onlinealgorithmtimeunit.PeopleDirectServiceGreedy(sim);
		 * //GreedyPeopleParcelInsertSharing planner = new
		 * GreedyPeopleParcelInsertSharing(sim); NaiveSequentialPlanner planner
		 * = new NaiveSequentialPlanner(sim);
		 * 
		 * sim.run(3000,10,requestFN,planner,dir);
		 * 
		 * 
		 * //PeopleDirectServiceGreedy planner = new
		 * PeopleDirectServiceGreedy(sim);
		 * 
		 * // GreedyWithSharingPreserveNextServicePoint planner = new //
		 * GreedyWithSharingPreserveNextServicePoint(sim);
		 * //GreedyDirectPeopleService planner = new
		 * GreedyDirectPeopleService(sim); //
		 * GreedyDirectPeopleDirectParcelServices planner = new //
		 * GreedyDirectPeopleDirectParcelServices(sim); sim.setPlanner(planner);
		 * 
		 * sim.initTimeHorizon();
		 * 
		 * sim.simulateDataFromFile(requestPath,1,2);
		 * sim.writeTaxiItineraries(itinerariesFN);
		 * 
		 * 
		 * sim.initVehicles(); sim.loadRequests(requestPath);
		 * 
		 * sim.relaxTimeWindowParcelRequests(); HashMap<Integer,
		 * ItineraryTravelTime> itineraries = sim
		 * .loadItineraries(itinerariesFN); sim.analyzeSolution(itineraries);
		 * 
		 * sim.finalize();
		 */
	}

}
