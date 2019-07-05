package pbts.onlinealgorithmtimeunit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import pbts.algorithms.SequenceOptimizer;
import pbts.entities.ErrorMSG;
import pbts.entities.ItineraryTravelTime;
import pbts.entities.LatLng;
import pbts.entities.ParcelRequest;
import pbts.entities.PeopleRequest;
import pbts.entities.TaxiTimePointIndex;
import pbts.entities.TimePointIndex;
import pbts.entities.Vehicle;
import pbts.enums.ErrorType;
import pbts.enums.VehicleAction;
import pbts.enums.VehicleStatus;
import pbts.simulation.ItineraryServiceSequence;
import pbts.simulation.ServiceSequence;
import pbts.simulation.SimulatorTimeUnit;
import pbts.simulation.Utility;

import java.io.*;
public class PeopleInsertionGreedySharing implements
		OnlinePeopleInsertion {

	public SimulatorTimeUnit sim;
	public PrintWriter log;
	public SequenceOptimizer seqOptimizer = null;
	
	public PeopleInsertionGreedySharing(SimulatorTimeUnit sim){
		this.sim = sim;
		this.log = sim.log;
		seqOptimizer = new SequenceOptimizer(sim, sim.maxPendingStops + 10);
	}
	public String name(){
		return "PeopleInsertionGreedySharing";
	}
	public TaxiTimePointIndex availableTaxi(Vehicle taxi, PeopleRequest pr) {
		// find delivery people location
		int locID = -1;
		int idxLocID = taxi.findLastIndexPeopleDeliveryRemainRequetsIDs();// -1;
		
		TimePointIndex tpi = taxi.getNextTimePointIndex(taxi.lastIndexPoint,
				sim.T.currentTimePoint, sim.TimePointDuration);
		if (taxi.ID == sim.debugTaxiID) {
			System.out.println(name() + "::availableTaxi, taxi = " + taxi.ID
					+ ", pr = " + pr.id + ", T.currentTimePoint = "
					+ sim.T.currentTimePoint + ", tpi = " + tpi.toString());
			sim.log.println(name() + "::availableTaxi, taxi = " + taxi.ID
					+ ", pr = " + pr.id + ", T.currentTimePoint = "
					+ sim.T.currentTimePoint + ", tpi = " + tpi.toString() + ", idxLocID = " + idxLocID + ", remainRequestIDs = " + Utility.arr2String(taxi.remainRequestIDs));
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
		if(taxi.ID == sim.debugTaxiID){
			sim.log.println(name() + "::availableTaxi, idxLocID = " + idxLocID + ", tpi.indexRemainRequestIDs = " + tpi.indexRemainRequestIDs);
		}
		
		int timePoint = -1;
		int point = -1;
		int indexPoint = -1;
		int lastIndexPeopleDelivery = taxi.findLastIndexPeopleDeliveryItinerary(taxi.lastIndexPoint);
		if(taxi.ID == sim.debugTaxiID){
			sim.log.println(name() + "::availableTaxi lastIndexPeopleDelivery = " + lastIndexPeopleDelivery);
		}
		if(lastIndexPeopleDelivery >= 0){
			timePoint = taxi.currentItinerary.getDepartureTime(lastIndexPeopleDelivery);
			point = taxi.currentItinerary.get(lastIndexPeopleDelivery);
			indexPoint = lastIndexPeopleDelivery;
		
			if(taxi.ID == sim.debugTaxiID)
			sim.log.println(name() + "::availableTaxi taxi = " + taxi.ID + ", people request = " + pr.id + ", lastIndexPeopleDelivery = " + lastIndexPeopleDelivery + 
					" UPDATE timePoint = " + timePoint + ", point = " + point + ", indexPoint = " + indexPoint);
		}
		
		ArrayList<Integer> R = new ArrayList<Integer>();// new remain requestIDs
		ArrayList<Integer> KR = new ArrayList<Integer>();// kept remain request sequences
		idxLocID = idxLocID < taxi.remainRequestIDs.size()-1 ? idxLocID : taxi.remainRequestIDs.size()-1;
		if(taxi.ID == sim.debugTaxiID){
			sim.log.println(name() + "::availableTaxi, taxi = " + taxi.ID + ", peopleReq = " + pr.id + ", idxLocID = " + idxLocID + ", taxi.remainRequestIDs.size()  = " + 
		taxi.remainRequestIDs.size());
		}
		for(int i = 0; i <= idxLocID; i++)
			KR.add(taxi.remainRequestIDs.get(i));
		for(int i = idxLocID+1; i < taxi.remainRequestIDs.size(); i++)
			R.add(taxi.remainRequestIDs.get(i));
		if(taxi.ID == sim.debugTaxiID){
			sim.log.println(name() + "::availableTaxi, taxi = " + taxi.ID + ", peopleReq = " + pr.id + ", keep KR = " + Utility.arr2String(KR) + ", remain R = " + Utility.arr2String(R));
		}
		

		if (timePoint < tpi.timePoint) {
			timePoint = tpi.timePoint;
			point = tpi.point;
			indexPoint = tpi.indexPoint;
			if(taxi.ID == sim.debugTaxiID){
				sim.log.println(name() + "::availableTaxi, update point = " + point + ", indexPoint = " + indexPoint + ", timePoint = " + timePoint);
			}
		}

		/*
		LatLng pickup = sim.map.mLatLng.get(pr.pickupLocationID);
		LatLng ll = sim.map.mLatLng.get(point);
		 double d = sim.G.computeDistanceHaversine(ll.lat, ll.lng, pickup.lat, pickup.lng);
		 d = d * 1.5;// approximate factor
		 */
		
		//double d = sim.estimateTravelingDistance(point, pr.pickupLocationID);
		
		double d = sim.dijkstra.queryDistance(point, pr.pickupLocationID);
		double t = sim.getTravelTime(d, sim.maxSpeedms);
		tpi = new TimePointIndex(timePoint, point, indexPoint);

		if(taxi.ID == sim.debugTaxiID && pr.id == 184014){
			sim.log.println(name() + "::availableTaxi CHECK DISTANCE --> taxi " + taxi.ID + ", people request " + pr.id + ", check distance from " +
		point + " to " + pr.pickupLocationID + ", distance = " + d + ", travel time = " + t + ", timePoint = " + timePoint);
			//sim.exit();
		}
		
		if (t + timePoint <= pr.latePickupTime)
			return new TaxiTimePointIndex(taxi, tpi, R, KR);

		return null;
	}

	public ArrayList<TaxiTimePointIndex> getAvailableTaxis(PeopleRequest pr, double maxTime) {
		ArrayList<TaxiTimePointIndex> L = new ArrayList<TaxiTimePointIndex>();
		double t0 = System.currentTimeMillis();
		for (int k = 0; k < sim.vehicles.size(); k++) {
			Vehicle taxi = sim.vehicles.get(k);
			if (taxi.status == VehicleStatus.STOP_WORK)
				continue;
			if (taxi.totalTravelDistance > sim.maxTravelDistance)
				continue;
			if (taxi.remainRequestIDs.size() > sim.maxPendingStops)
				continue;
			if (taxi.hasPeopleTobePickuped()) 
				continue;
			
			TaxiTimePointIndex ttpi = availableTaxi(taxi, pr);
			if (ttpi != null)
				L.add(ttpi);
			
			if((System.currentTimeMillis() - t0)*0.001 > maxTime) break;
			//System.out.println(name() + "::getAvailableTaxis, taxi[ " + k + "/" + sim.vehicles.size() + "] = " +
				//taxi.ID + ", currentItinerary.sz = " + taxi.currentItinerary.size());
			
		}
		return L;
	}
	private int[] partition(HashSet[] domain) {
		return sim.allDiff.solve(domain);
	}
	public ServiceSequence computePeopleInsertionSequence(Vehicle taxi,
			TimePointIndex tpi, PeopleRequest pr, ArrayList<Integer> keptReq, ArrayList<Integer> remainRequestIDs,
			double maxTime) {
		if(sim.hasPeopleDeliveryRequest(remainRequestIDs)){
			System.out.println(name() + "::computePeopleInsertionSequence, taxi = " + taxi.ID + ", PeopleRequest = " + pr.id + ", FAILED "
					+ "due to existence of people delivery request in remainRequestIDs " + Utility.arr2String(remainRequestIDs) + 
					", keptRequests = " + Utility.arr2String(keptReq) + " --> exit(-1)");
			sim.exit();
		}
		int startIdx = 0;
		// int taxiID = 1;
		int nextStartPoint = tpi.point;// taxi.getNextStartPoint();
		int startTimePoint = tpi.timePoint;// taxi.getNextStartTimePoint();
		if(taxi.ID == sim.debugTaxiID){
			//String s = "";
			//for(int i = 0; i < remainRequestIDs.size(); i++)
				//s = s + remainRequestIDs.get(i) + ", ";
			sim.log.println(name() + "::computePeopleInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + " tpi = " + tpi.toString() + 
					", keptReq = " + Utility.arr2String(keptReq) + ", remainrequestIDs = "
				+ Utility.arr2String(remainRequestIDs) + ", taxi.requestStatus = " + taxi.requestStatus());		
		}
		ArrayList<Integer> parkings = sim.collectAvailableParkings(taxi);
		ServiceSequence ss = null;
		
		ArrayList<Integer> L = new ArrayList<Integer>();
		int[] r = new int[keptReq.size()];
		for(int i = 0; i < keptReq.size(); i++)
			r[i] = keptReq.get(i);
		//r[r.length-2] = pr.id;
		//r[r.length-1] = -pr.id;
		//for(int i = 0; i < r.length; i++){
			//L.add(r[i]);
		//}
		L.add(pr.id);
		L.add(-pr.id);
		for(int i = 0; i < remainRequestIDs.size(); i++){
			L.add(remainRequestIDs.get(i));
		}
		//int[] t_r  = new int[0];
		int[] t_sel_nod = seqOptimizer.computeShortestSequence(taxi, tpi, r, L,maxTime);
		
			
		if(t_sel_nod == null){
			log.println(name() + "::computePeopleInsertionSequence taxi = " + taxi.ID + ", sel_nod = NULL NO SOLUTION?? " + 
		", taxi.requestStatus = " + taxi.requestStatus());
			return null;
		}
		int[] sel_nod = new int[t_sel_nod.length - keptReq.size()];
		//int idx = -1;
		System.out.println(name() + "::computePeopleInsertionSequence, t_sel_nod = " + Utility.arr2String(t_sel_nod) + 
				", remainrequestIDs = " + Utility.arr2String(remainRequestIDs) + ", keptReq = " + Utility.arr2String(keptReq));
		for(int i = keptReq.size(); i < t_sel_nod.length; i++){
			sel_nod[i-keptReq.size()] = t_sel_nod[i];
		}
		System.out.println(name() + "::computePeopleInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + 
				", OBTAIN sel_nod = " + Utility.arr2String(sel_nod));
		//System.exit(-1);;
		if(taxi.ID == sim.debugTaxiID){
			log.println(name() + "::computePeopleInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + 
					", OBTAIN sel_nod = " + Utility.arr2String(sel_nod));
			
			
		}
		int sel_pk = -1;
		double minD = 100000000;
		int endReq = sel_nod[sel_nod.length-1];
		//System.out.println(name() + "::computePeopleInsertionSequence, endReq = " + endReq);
		int endLocID = -1;
		PeopleRequest peoR = sim.mPeopleRequest.get(Math.abs(endReq));
		if(peoR != null){
			if(endReq < 0) endLocID = peoR.deliveryLocationID; else endLocID = peoR.pickupLocationID;
		}else{
			ParcelRequest parR = sim.mParcelRequest.get(Math.abs(endReq));
			if(endReq < 0) endLocID = parR.deliveryLocationID; else endLocID = parR.pickupLocationID;
		}
		
		LatLng endLL = sim.map.mLatLng.get(endLocID);
		for(int k = 0; k < parkings.size(); k++){
			int pk = parkings.get(k);
			LatLng pkLL = sim.map.mLatLng.get(pk);
			if(pkLL == null){
				System.out.println(name() + "::computePeopleInsertionSequence, pkLL is NULL");
			}
			double D = sim.G.computeDistanceHaversine(endLL.lat, endLL.lng, pkLL.lat, pkLL.lng);
			if(D < minD){
				minD = D;
				sel_pk = pk;
			}
		}
		ss = new ServiceSequence(sel_nod, 0, sel_pk, minD);
		return ss;
	}

	public ItineraryServiceSequence computeItineraryPeopleInsertion(
			Vehicle taxi, TimePointIndex next_tpi, PeopleRequest pr, ArrayList<Integer> keptReq, 
			ArrayList<Integer> remainRequestIDs, double maxTime) {
		// compute best added itinerary when pr is inserted into taxi
		ServiceSequence ss = computePeopleInsertionSequence(taxi, next_tpi,
				pr, keptReq, remainRequestIDs,maxTime);
		int fromIndex = next_tpi.indexPoint;
		int nextStartTimePoint = next_tpi.timePoint;
		int fromPoint = next_tpi.point;
		if (ss == null){
			System.out.println(name() + "::computeItineraryPeopleInsertion, ss = NULL --> return NULL");
			sim.log.println(name() + "::computeItineraryPeopleInsertion, taxi = " + taxi.ID + ", ss = NULL --> return NULL");
			return null;
		}
		/*
		// int taxiID = 47;
		int reqID = -1;
		if (taxi.currentItinerary.size() > 0)
			reqID = taxi.currentItinerary.getRequestID(fromIndex);
		if (taxi.ID == sim.debugTaxiID) {
			sim.log.println(name()
					+ "::computeItineraryPeopleInsertion DEBUG, "
					+ "people request = " + pr.id + ", begin call to sim.establishItinerary("
					+ nextStartTimePoint + "," + fromIndex + "," + reqID + ","
					+ fromPoint + "), ss = " + ss.getSequence());
		}
		
		ItineraryTravelTime I = sim.establishItinerary(taxi,
				nextStartTimePoint, fromIndex, reqID, fromPoint, ss);
		*/
		ItineraryTravelTime I = sim.establishItinerary(taxi,
				nextStartTimePoint, fromIndex, fromPoint, ss);
		
		
		if (I == null){
			System.out.println(name() + "::computeItineraryPeopleInsertion, establishItinerary I = null");
			sim.log.println(name() + "::computeItineraryPeopleInsertion, taxi = " + taxi.ID + ", establishItinerary I = null");
			return null;
		}
		I.setDistance(ss.distance);

		if (I.getDistance() + taxi.totalTravelDistance > sim.maxTravelDistance){
			sim.log.println(name() + "::computeItineraryPeopleInsertion, taxi = " + taxi.ID + ", taxi.totalDistance = " + 
		taxi.totalTravelDistance + " >  sim.maxTravelDistance " + sim.maxTravelDistance);
			
			return null;
		}

		return new ItineraryServiceSequence(taxi, I, ss);
	}

	public void insertPeopleRequest(PeopleRequest pr, Vehicle taxi, TimePointIndex tpi, ArrayList<Integer> keptReq, 
			ArrayList<Integer> remainRequestIDs, double maxTime){
		ItineraryServiceSequence IS = computeItineraryPeopleInsertion(taxi,tpi, pr, keptReq, remainRequestIDs,maxTime);
		if(IS == null){
			System.out.println(name() + "::insertPeopleRequest, pr = " + pr.id + " IS = null");
			sim.nbPeopleRejects++;
			System.out.println(name() + "::insertPeopleRequest --> request "
					+ pr.id + " is REJECTED due to sel_IS = null, nbPeopleRejected = "
					+ sim.nbPeopleRejects + "/" + sim.allPeopleRequests.size());
			sim.log.println(name() + "::insertParcelRequest --> request " + pr.id
					+ " is REJECTED --> System.exit(-1)");
			//sim.log.close();
			//System.exit(-1);
			sim.exit();
		}else{
			System.out.println(name() + "::insertPeopleRequest, pr = " + pr.id + " IS = NOT null");
			ErrorMSG err = sim.checkServiceSequence(taxi, keptReq, IS.ss.rids, IS.ss.rids.length); 
			if(err.err != ErrorType.NO_ERROR){
				System.out.println(name() + "::insertPeopleRequest, pr = " + pr.id + ", taxi = " + taxi.ID + ", IS not LEGAL?????" + "\" + "
						+ "\n peopleOnBoard = " + Utility.arr2String(taxi.peopleReqIDonBoard) + ", parcelOnBoard = " + 
						Utility.arr2String(taxi.parcelReqIDonBoard) + ", sequence = " + Utility.arr2String(IS.ss.rids) + 
						"\n keptReq = " + Utility.arr2String(keptReq) + ", remainrequestIDs = " + 
						Utility.arr2String(remainRequestIDs) + ", taxi.remainRequestIDs  " + Utility.arr2String(taxi.remainRequestIDs));
				
				
				//sim.log.close();
				//System.exit(-1);;
				sim.exit();
			}
			if(taxi.ID == sim.debugTaxiID)
				sim.log.println(name() + "::insertPeopleRequest, DEBUG taxi = " + taxi.ID + "\n BEFORE AdmitItinerary, peopleOnBoard = " + 
			Utility.arr2String(taxi.peopleReqIDonBoard) + ", parcelOnBoard = " + 
					Utility.arr2String(taxi.parcelReqIDonBoard) + ", admit sequence = " + Utility.arr2String(IS.ss.rids) + 
					"\n keptReq = " + Utility.arr2String(keptReq) + ", remainrequestIDs = " + 
					Utility.arr2String(remainRequestIDs) + ", taxi.remainRequestIDs  " + Utility.arr2String(taxi.remainRequestIDs));
			/*
			System.out.println(name() + "::insertPeopleRequest, DEBUG taxi = " + taxi.ID + "\n BEFORE AdmitItinerary, peopleOnBoard = " + 
					Utility.arr2String(taxi.peopleReqIDonBoard) + ", parcelOnBoard = " + 
							Utility.arr2String(taxi.parcelReqIDonBoard) + ", admit sequence = " + Utility.arr2String(IS.ss.rids) + 
							"\n keptReq = " + Utility.arr2String(keptReq) + ", remainrequestIDs = " + 
							Utility.arr2String(remainRequestIDs) + ", taxi.remainRequestIDs  " + Utility.arr2String(taxi.remainRequestIDs));
			*/
			//sim.admitNewItinerary(taxi, tpi.timePoint, tpi.indexPoint, tpi.point, IS.I, IS.ss);
			sim.admitNewItineraryWithoutStatus(taxi, tpi.timePoint, tpi.indexPoint, tpi.point, IS.I, IS.ss);
			
			sim.nbPeopleWaitBoarding++;
			if(taxi.ID == sim.debugTaxiID){
				sim.log.println(name() + "::insertParcelRequest, AFTER admit itinerary, currentItinerary = " + taxi.currentItinerary.toString());
				sim.log.println(name() + "::insertPeopleRequest, DEBUG taxi = " + taxi.ID + "\n AFTER AdmitItinerary, peopleOnBoard = " + 
						Utility.arr2String(taxi.peopleReqIDonBoard) + ", parcelOnBoard = " + 
								Utility.arr2String(taxi.parcelReqIDonBoard) + ", admit sequence = " + Utility.arr2String(IS.ss.rids) + 
								"\n keptReq = " + Utility.arr2String(keptReq) + ", remainrequestIDs = " + 
								Utility.arr2String(remainRequestIDs) + ", taxi.remainRequestIDs  " + Utility.arr2String(taxi.remainRequestIDs));
			}
			System.out.println(name()
					+ "::insertPeopleRequest, status = " + sim.getAcceptRejectStatus());
			
			//sim.exit();
		}
	}

	public void processPeopleRequests(ArrayList<PeopleRequest> peoReq, double startDecideTime) {
		// TODO Auto-generated method stub
		System.out.println(name() + "::processPeopleRequests(peoReq.sz = " + peoReq.size() + ")");
		//sim.exit();
		HashMap<PeopleRequest, ArrayList<TaxiTimePointIndex>> D = new HashMap<PeopleRequest, ArrayList<TaxiTimePointIndex>>();
		HashMap<Vehicle, TaxiTimePointIndex> mTaxi2TimePointIndex = new HashMap<Vehicle, TaxiTimePointIndex>();
		double t0 = System.currentTimeMillis();
		for (int i = 0; i < peoReq.size(); i++) {
			PeopleRequest pr = peoReq.get(i);
			ArrayList<TaxiTimePointIndex> L = getAvailableTaxis(pr,sim.maxTimeAllowedCollectAvailableTaxisForPeople);
			D.put(pr, L);

			for (int j = 0; j < L.size(); j++) {
				TaxiTimePointIndex ttpi = L.get(j);
				mTaxi2TimePointIndex.put(ttpi.taxi, ttpi);
			}
		}
		double t = System.currentTimeMillis() - t0;
		t = t*0.001;
		if(t > sim.maxTimeCollectAvailableTaxisPeopleInsertion) sim.maxTimeCollectAvailableTaxisPeopleInsertion = t;
		
		ArrayList<PeopleRequest> rejectReq = new ArrayList<PeopleRequest>();
		for(int i = 0; i < peoReq.size(); i++){
			PeopleRequest pr = peoReq.get(i);
			if(D.get(pr).size() == 0){
				rejectReq.add(pr);
			}
		}
		for(int i = 0; i < rejectReq.size(); i++){
			PeopleRequest pr = rejectReq.get(i);
			int idx = peoReq.indexOf(pr);
			peoReq.remove(idx);
			System.out.println(name() + "::processPeopleRequests REJECT people request " + pr.id + " due to UNREACHABLE!!!!!!!!");
			sim.nbPeopleRejects++;
		}
		
		HashSet<Integer>[] domain = new HashSet[peoReq.size()];
		for (int i = 0; i < peoReq.size(); i++) {
			PeopleRequest pr = peoReq.get(i);
			domain[i] = new HashSet<Integer>();
			for (int j = 0; j < D.get(pr).size(); j++) {
				TaxiTimePointIndex ttpi = D.get(pr).get(j);
				domain[i].add(sim.mTaxiID2Index.get(ttpi.taxi.ID));
			}
		}
		for(int i = 0; i < domain.length; i++){
			System.out.println(name() + "::processPeopleRequests, domain[" + i + "].sz = " + domain[i].size());
		}
		System.out.println(name() + "::processPeopleRequests, start paritioning....");
		int[] s = partition(domain);
		// request peoReq[i] is assigned to taxi vehicles.get(s[i]);
		if (s == null) {
			// REJECT ALL
			sim.nbPeopleRejects += peoReq.size();
			System.out.println(name()
					+ "::processPeopleRequests --> REJECT All, nbRejected = "
					+ sim.nbPeopleRejects);
		} else {
			System.out.print(name() + "::processPeopleRequests finish paritioning, s = ");
			for(int ii = 0; ii < s.length; ii++) System.out.print(s[ii] + " ");
			System.out.println();
			
			for (int i = 0; i < peoReq.size(); i++) {
				if((System.currentTimeMillis()-startDecideTime)*0.001 > sim.TimePointDuration){
					sim.nbPeopleRejects++;
					continue;
				}
				PeopleRequest pr = peoReq.get(i);
				Vehicle taxi = sim.vehicles.get(s[i]);
				TaxiTimePointIndex ttpi = mTaxi2TimePointIndex.get(taxi);
				System.out.println(name() + "::processPeopleRequests call to insertGreedyPeopleRequest(pr = " + pr.id + ", taxi = " + taxi.ID + ").......");
				if(taxi.ID == sim.debugTaxiID){
					log.println(name() + "::processPeopleRequests call to insertPeopleRequest(pr = " + pr.id + 
							", taxi = " + taxi.ID + ") " + taxi.requestStatus() + ", keptReq = " + 
							Utility.arr2String(ttpi.keptRequestIDs) + ", remainReqs = " + Utility.arr2String(ttpi.remainRequestIDs));
				}
				
				//insertGreedyPeopleRequest(pr, taxi, ttpi.tpi, ttpi.remainRequestIDs);
				//insertFirstDirectPeopleRequest(pr, taxi, ttpi.tpi, ttpi.remainRequestIDs);
				t0 = System.currentTimeMillis();
				insertPeopleRequest(pr, ttpi.taxi, ttpi.tpi, ttpi.keptRequestIDs, ttpi.remainRequestIDs,
						sim.maxTimeAllowedInsertOnePeople);
				t = System.currentTimeMillis() - t0;
				t = t * 0.001;
				if(t > sim.maxTimeOnePeopleInsertion) sim.maxTimeOnePeopleInsertion = t;
				System.out.println(name() + "::processPeopleRequests call to insertGreedyPeopleRequest(pr = " + pr.id + ", taxi = " + taxi.ID + ") finished");
				//log.println(name() + "::processPeopleRequests call to insertGreedyPeopleRequest(pr = " + pr.id + ", taxi = " + taxi.ID + ") finished");
				
			}
		}
		System.out.println(name() + "::procesPeopleRequests, sim.status = " + sim.getAcceptRejectStatus());

	}

}
