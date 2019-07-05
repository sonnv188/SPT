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
import pbts.enums.VehicleStatus;
import pbts.simulation.ItineraryServiceSequence;
import pbts.simulation.ServiceSequence;
import pbts.simulation.SimulatorTimeUnit;
import pbts.simulation.Utility;

import java.io.*;
public class PeopleInsertionDirectFirstServiceGreedy implements
		OnlinePeopleInsertion {

	public SimulatorTimeUnit sim;
	public PrintWriter log;
	public SequenceOptimizer seqOptimizer = null;
	
	public PeopleInsertionDirectFirstServiceGreedy(SimulatorTimeUnit sim){
		this.sim = sim;
		this.log = sim.log;
		seqOptimizer = new SequenceOptimizer(sim, sim.maxPendingStops + 10);
	}
	public String name(){
		return "PeopleInsertionDirectFirstServiceGreedy";
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
			TaxiTimePointIndex ttpi = sim.availableTaxi(taxi, pr);
			if (ttpi != null)
				L.add(ttpi);
			
			double t = (System.currentTimeMillis() - t0)*0.001; 
			if(t > maxTime){
				System.out.println(name() + "::getAvailableTaxis, k = " + k + ", t = " + t + " EXPIRED maxTime = " + maxTime);
				break;
			}
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
		//System.out.println(name() + "::computePeopleInsertionSequence, maxTime = " + maxTime); System.exit(-1);
		int startIdx = 0;
		// int taxiID = 1;
		int nextStartPoint = tpi.point;// taxi.getNextStartPoint();
		int startTimePoint = tpi.timePoint;// taxi.getNextStartTimePoint();
		if(taxi.ID == sim.debugTaxiID){
			//String s = "";
			//for(int i = 0; i < remainRequestIDs.size(); i++)
				//s = s + remainRequestIDs.get(i) + ", ";
			log.println(name() + "::computePeopleInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + " tpi = " + tpi.toString() + 
					", keptReq = " + Utility.arr2String(keptReq) + ", remainrequestIDs = "
				+ Utility.arr2String(remainRequestIDs) + ", taxi.requestStatus = " + taxi.requestStatus());		
		}
		ArrayList<Integer> parkings = sim.collectAvailableParkings(taxi);
		ServiceSequence ss = null;
		
		int[] r = new int[keptReq.size() + 2];
		for(int i = 0; i < keptReq.size(); i++)
			r[i] = keptReq.get(i);
		r[r.length-2] = pr.id;
		r[r.length-1] = -pr.id;
		
		int[] t_sel_nod = seqOptimizer.computeShortestSequence(taxi, tpi, r, remainRequestIDs,maxTime);
		
			
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
		if(taxi.ID == sim.debugTaxiID){
			log.println(name() + "::computePeopleInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + 
					", OBTAIN sel_nod = " + Utility.arr2String(sel_nod));
			System.out.println(name() + "::computePeopleInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + 
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
					+ "parcel request = " + pr.id + ", begin call to sim.establishItinerary("
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
			//sim.exit();
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
			
			//sim.admitNewItinerary(taxi, tpi.timePoint, tpi.indexPoint, tpi.point, IS.I, IS.ss);
			sim.admitNewItineraryWithoutStatus(taxi, tpi.timePoint, tpi.indexPoint, tpi.point, IS.I, IS.ss);
			
			sim.nbParcelWaitBoarding++;
			if(taxi.ID == sim.debugTaxiID){
				sim.log.println(name() + "::insertParcelRequest, AFTER admit itinerary, currentItinerary = " + taxi.currentItinerary.toString());
				sim.log.println(name() + "::insertPeopleRequest, DEBUG taxi = " + taxi.ID + "\n AFTER AdmitItinerary, peopleOnBoard = " + 
						Utility.arr2String(taxi.peopleReqIDonBoard) + ", parcelOnBoard = " + 
								Utility.arr2String(taxi.parcelReqIDonBoard) + ", admit sequence = " + Utility.arr2String(IS.ss.rids) + 
								"\n keptReq = " + Utility.arr2String(keptReq) + ", remainrequestIDs = " + 
								Utility.arr2String(remainRequestIDs) + ", taxi.remainRequestIDs  " + Utility.arr2String(taxi.remainRequestIDs));
			}
			System.out.println(name()
					+ "::insertPeopleRequest, nbParcelWaitBoarding = "
					+ sim.nbParcelWaitBoarding + ", nbParcelComplete = "
					+ sim.nbParcelComplete + ", nbPeopleOnBoard = "
					+ sim.nbPeopleOnBoard + ", nbParcelRejects = "
					+ sim.nbParcelRejects + ", total ParcelRequests = "
					+ sim.allParcelRequests.size());
		}
	}

	public void processPeopleRequests(ArrayList<PeopleRequest> peoReq, double startDecideTime) {
		// TODO Auto-generated method stub
		System.out.println(name() + "::processPeopleRequests(peoReq.sz = " + peoReq.size() + ")");
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
				//if((System.currentTimeMillis()-startDecideTime)*0.001 > sim.TimePointDuration){	sim.nbPeopleRejects++;continue;}
		
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
