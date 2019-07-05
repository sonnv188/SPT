package SARP2014;

import java.io.PrintWriter;
import java.util.ArrayList;

import pbts.algorithms.SequenceOptimizer;
import pbts.entities.ErrorMSG;
import pbts.entities.ItineraryTravelTime;
import pbts.entities.LatLng;
import pbts.entities.ParcelRequest;
import pbts.entities.Parking;
import pbts.entities.PeopleRequest;
import pbts.entities.TaxiTimePointIndex;
import pbts.entities.TimePointIndex;
import pbts.entities.Vehicle;
import pbts.enums.ErrorType;
import pbts.itineraryoptimizer.ItineraryOptimizer;
import pbts.onlinealgorithmtimeunit.ParcelInsertionGreedySharingDecisionTimeLimit;
import pbts.onlinealgorithmtimeunit.PeopleInsertionGreedySharingDecisionTimeLimit;
import pbts.simulation.ItineraryServiceSequence;
import pbts.simulation.ServiceSequence;
import pbts.simulation.SimulatorTimeUnit;
import pbts.simulation.Utility;

public class dynamicSARPplanner {
	public PrintWriter log = null;
	public SimulatorTimeUnit sim = null;

	public ItineraryOptimizer IO;
	public SequenceOptimizer seqOptimizer = null;
	
	public String name(){ return "dynamicSARPplanner";}
	
	public dynamicSARPplanner(SimulatorTimeUnit sim, SequenceOptimizer seqOptimizer){
		this.sim = sim;
		if(sim != null)
			this.log = sim.log;
		IO = new ItineraryOptimizer(sim);
		this.seqOptimizer = seqOptimizer;//new SequenceOptimizer(sim, sim.maxPendingStops + 12);
	}
	public void setSimulator(SimulatorTimeUnit sim){
		this.sim = sim;
		if(sim != null) log = sim.log;
	}
	
	public ArrayList<Integer> removeOneRequestInSequence(ArrayList<ParcelRequest> markReq, ArrayList<Integer> s){
		for(int i = 0; i < s.size(); i++){
			PeopleRequest peoR = sim.mPeopleRequest.get(Math.abs(s.get(i-1)));
			if(peoR != null){
				if(s.get(i-1) < 0){
					point1 = peoR.deliveryLocationID;
					early1 = peoR.earlyDeliveryTime;
					late1 = peoR.lateDeliveryTime;
				}
				else{
					point1 = peoR.pickupLocationID;
					early1 = peoR.earlyPickupTime;
					late1 = peoR.latePickupTime;
				}
			}else{
				ParcelRequest parR = mParcelRequest.get(Math.abs(stSequence.get(i-1)));
				if(stSequence.get(i-1) < 0){
					point1 = parR.deliveryLocationID;
					early1 = parR.earlyDeliveryTime;
					late1 = parR.lateDeliveryTime;
				}
				else{
					point1 = parR.pickupLocationID;
					early1 = parR.earlyPickupTime;
					late1 = parR.latePickupTime;
				}
			}
		}
	}
	
	public ArrayList<Integer> generateNeighborSolution(int step, ArrayList<ParcelRequest> markReq, double startDecideTime, int startTimePoint, int nextStartPoint, Vehicle taxi, ArrayList<Integer> s){
		for(int i = 0; i < sim.allParcelRequests.size(); i++){
			ParcelRequest par = sim.allParcelRequests.get(i);
			if(!sim.insertedParcelRequests.contains(par) && !markReq.contains(par)){
				ArrayList<Integer> rm_s = removeOneRequestInSequence(markReq, s);
			}
		}
	}
	public void NeighborhoodSearchSARP2014(double startDecideTime, int startTimePoint, int nextStartPoint, Vehicle taxi, ArrayList<Integer> s, double T0){
		double t0 = System.currentTimeMillis();
		double t = t0;
		ArrayList<Integer> s_best = new ArrayList<Integer>(s);
		ArrayList<Integer> s_neighbor = new ArrayList<Integer>(s);
		int MAX = 100;
		ArrayList<ParcelRequest> markReq = new ArrayList<ParcelRequest>();
		int step = 0;
		while((t - t0)*0.001 < startDecideTime){
			s_neighbor = generateNeighborSolution(step, markReq, startDecideTime, startTimePoint, nextStartPoint, taxi, s);
			step++;
		}
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
			//ArrayList<Integer> parkings = sim.collectAvailableParkings(taxi);
			ServiceSequence ss = null;
			
			ArrayList<Integer> L = null;
			
			int[] r = new int[keptReq.size()];
			for(int i = 0; i < keptReq.size(); i++)
				r[i] = keptReq.get(i);
			//r[r.length-2] = pr.id;
			//r[r.length-1] = -pr.id;
			//for(int i = 0; i < r.length; i++){
				//L.add(r[i]);
			//}
			if(taxi.pendingParcelReqs.size() == 12){
				L = sim.checkAllThePositionToInsertPeople(startTimePoint, nextStartPoint, taxi, pr);
				if(L != null){
					sim.nbParcelWaitBoarding +=6;
					taxi.pendingParcelReqs.clear();
					double T0 = sim.calculateTotalTravelManhattanDistanceForPeople(startTimePoint, nextStartPoint, taxi, L);
					NeighborhoodSearchSARP2014(maxTime, startTimePoint, nextStartPoint, taxi, L, T0);
				}
				else{
					L = new ArrayList<Integer>();
					L.add(pr.id);
					L.add(-pr.id);
				}

			}
			else{
				L = new ArrayList<Integer>();
				L.add(pr.id);
				L.add(-pr.id);
				for(int i = 0; i < remainRequestIDs.size(); i++){
					L.add(remainRequestIDs.get(i));
				}
			}
			
			int[] sel_nod = new int[L.size()];
			for(int i = 0; i < L.size(); i++){
				sel_nod[i] = L.get(i);
			}
			//int[] t_r  = new int[0];
			/*int[] t_sel_nod = seqOptimizer.computeShortestSequenceManhattan(taxi, tpi, r, L,maxTime);
			
			
			if(t_sel_nod == null){
				//log.println(name() + "::computePeopleInsertionSequence taxi = " + taxi.ID + ", sel_nod = NULL NO SOLUTION?? " + 
			//", taxi.requestStatus = " + taxi.requestStatus());
				return null;
			}

			//int idx = -1;
			System.out.println(name() + "::computePeopleInsertionSequence, t_sel_nod = " + Utility.arr2String(t_sel_nod) + 
					", remainrequestIDs = " + Utility.arr2String(remainRequestIDs) + ", keptReq = " + Utility.arr2String(keptReq));
			for(int i = keptReq.size(); i < t_sel_nod.length; i++){
				sel_nod[i-keptReq.size()] = t_sel_nod[i];
			}*/
			System.out.println(name() + "::computePeopleInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + 
					", OBTAIN sel_nod = " + Utility.arr2String(sel_nod));
			//System.exit(-1);;
			if(taxi.ID == sim.debugTaxiID){
				log.println(name() + "::computePeopleInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + 
						", OBTAIN sel_nod = " + Utility.arr2String(sel_nod));
				
				
			}
			
			ArrayList<Integer> parkings = sim.collectAvailableParkings(taxi);
			
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
			
			for(int k = 0; k < parkings.size(); k++){
				int pk = parkings.get(k);
				double D = sim.estimateTravelingDistanceManhattan(pk, endLocID);
				//double D = sim.G.computeDistanceHaversine(endLL.lat, endLL.lng, pkLL.lat, pkLL.lng);
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

			ItineraryTravelTime I = sim.establishItinerary(taxi,
					nextStartTimePoint, fromIndex, fromPoint, ss);
			
			
			if (I == null){
				System.out.println(name() + "::computeItineraryPeopleInsertion, establishItinerary I = null");
				sim.log.println(name() + "::computeItineraryPeopleInsertion, taxi = " + taxi.ID + ", establishItinerary I = null");
				return null;
			}
			ss.setDistance(I.getDistance());
			//ss.setParkingLocation(I.get(I.size()-1));
			
			double d = I.getDistance() + taxi.totalTravelDistance;
			if (I.getDistance() + taxi.totalTravelDistance > sim.maxTravelDistance){
				System.out.println(name() + "::computeItineraryPeopleInsertion return null DUE TO new total travel distance = " + d +
			" exceed maximum travel distance = " + sim.maxTravelDistance);
				sim.log.println(name() + "::computeItineraryPeopleInsertion, taxi = " + taxi.ID + ", taxi.totalDistance = " + 
			taxi.totalTravelDistance + " >  sim.maxTravelDistance " + sim.maxTravelDistance);
				
				//return null;
			}
			
			return new ItineraryServiceSequence(taxi, I, ss);
		}
		
	public void insertPeopleRequest(PeopleRequest pr, double maxTime){
		double t0 = System.currentTimeMillis();
		TaxiTimePointIndex ttpi = sim.getNearestAvailableTaxiForPeopleSARP2014(seqOptimizer, pr, maxTime);
		double t= System.currentTimeMillis() - t0;
		t = t *0.001;
		if(t > sim.maxTimeCollectAvailableTaxisPeopleInsertion) sim.maxTimeCollectAvailableTaxisPeopleInsertion = t;
		
		if(ttpi == null){
			sim.nbPeopleComplete++;
			System.out.println(name() + "::insertPeopleRequest(pr = " + pr.id + "), nbPeopleRejects = " + sim.nbPeopleRejects + " DUE TO no available taxi found");
		}else{
			t0 = System.currentTimeMillis();
			boolean ok = false;
			ArrayList<Integer> pending = new ArrayList<Integer>(ttpi.taxi.pendingParcelReqs);
			if(ttpi.taxi.pendingParcelReqs.size() == 12){
				ok = true;
			}
			ItineraryServiceSequence IS = computeItineraryPeopleInsertion(ttpi.taxi, ttpi.tpi, pr, 
					ttpi.keptRequestIDs, ttpi.remainRequestIDs, maxTime);
			if(IS == null){
				System.out.println(name() + "::insertPeopleRequest, pr = " + pr.id + " IS = null");
				sim.nbPeopleRejects++;
				if(ok == true){
					for(int i = 0; i < sim.vehicles.size(); i++){
						Vehicle tx = sim.vehicles.get(i);
						if(tx.ID == ttpi.taxi.ID){
							tx.pendingParcelReqs = new ArrayList<Integer>(pending);
							break;
						}
					}
				}
				System.out.println(name() + "::insertPeopleRequest --> request "
						+ pr.id + " is REJECTED due to sel_IS = null, nbPeopleRejected = "
						+ sim.nbPeopleRejects + "/" + sim.allPeopleRequests.size());
				//sim.log.println(name() + "::insertParcelRequest --> request " + pr.id
					//	+ " is REJECTED --> System.exit(-1)");
				//sim.log.close();
				//System.exit(-1);
				//sim.exit();
			}else{
				System.out.println(name() + "::insertPeopleRequest, pr = " + pr.id + " IS = NOT null");
				ErrorMSG err = sim.checkServiceSequence(ttpi.taxi, ttpi.keptRequestIDs, IS.ss.rids, IS.ss.rids.length); 
				if(err.err != ErrorType.NO_ERROR){
					System.out.println(name() + "::insertPeopleRequest, pr = " + pr.id + ", taxi = " + ttpi.taxi.ID + ", IS not LEGAL?????" + "\" + "
							+ "\n peopleOnBoard = " + Utility.arr2String(ttpi.taxi.peopleReqIDonBoard) + ", parcelOnBoard = " + 
							Utility.arr2String(ttpi.taxi.parcelReqIDonBoard) + ", sequence = " + Utility.arr2String(IS.ss.rids) + 
							"\n keptReq = " + Utility.arr2String(ttpi.keptRequestIDs) + ", remainrequestIDs = " + 
							Utility.arr2String(ttpi.remainRequestIDs) + ", taxi.remainRequestIDs  " + Utility.arr2String(ttpi.taxi.remainRequestIDs));
					
					
					//sim.log.close();
					//System.exit(-1);;
					sim.exit();
				}
				if(ttpi.taxi.ID == sim.debugTaxiID)
					sim.log.println(name() + "::insertPeopleRequest, DEBUG taxi = " + ttpi.taxi.ID + "\n BEFORE AdmitItinerary, peopleOnBoard = " + 
				Utility.arr2String(ttpi.taxi.peopleReqIDonBoard) + ", parcelOnBoard = " + 
						Utility.arr2String(ttpi.taxi.parcelReqIDonBoard) + ", admit sequence = " + Utility.arr2String(IS.ss.rids) + 
						"\n keptReq = " + Utility.arr2String(ttpi.keptRequestIDs) + ", remainrequestIDs = " + 
						Utility.arr2String(ttpi.remainRequestIDs) + ", taxi.remainRequestIDs  " + Utility.arr2String(ttpi.taxi.remainRequestIDs));
				/*
				System.out.println(name() + "::insertPeopleRequest, DEBUG taxi = " + taxi.ID + "\n BEFORE AdmitItinerary, peopleOnBoard = " + 
						Utility.arr2String(taxi.peopleReqIDonBoard) + ", parcelOnBoard = " + 
								Utility.arr2String(taxi.parcelReqIDonBoard) + ", admit sequence = " + Utility.arr2String(IS.ss.rids) + 
								"\n keptReq = " + Utility.arr2String(keptReq) + ", remainrequestIDs = " + 
								Utility.arr2String(remainRequestIDs) + ", taxi.remainRequestIDs  " + Utility.arr2String(taxi.remainRequestIDs));
				*/
				//sim.admitNewItinerary(taxi, tpi.timePoint, tpi.indexPoint, tpi.point, IS.I, IS.ss);
				sim.admitNewItineraryWithoutStatusSARP2014(ttpi.taxi, ttpi.tpi.timePoint, ttpi.tpi.indexPoint, ttpi.tpi.point, IS.I, IS.ss);
				sim.nbPeopleWaitBoarding++;
				if(ttpi.taxi.ID == sim.debugTaxiID){
					sim.log.println(name() + "::insertParcelRequest, AFTER admit itinerary, currentItinerary = " + ttpi.taxi.currentItinerary.toString());
					sim.log.println(name() + "::insertPeopleRequest, DEBUG taxi = " + ttpi.taxi.ID + "\n AFTER AdmitItinerary, peopleOnBoard = " + 
							Utility.arr2String(ttpi.taxi.peopleReqIDonBoard) + ", parcelOnBoard = " + 
									Utility.arr2String(ttpi.taxi.parcelReqIDonBoard) + ", admit sequence = " + Utility.arr2String(IS.ss.rids) + 
									"\n keptReq = " + Utility.arr2String(ttpi.keptRequestIDs) + ", remainrequestIDs = " + 
									Utility.arr2String(ttpi.remainRequestIDs) + ", taxi.remainRequestIDs  " + Utility.arr2String(ttpi.taxi.remainRequestIDs));
				}
				System.out.println(name()
						+ "::insertPeopleRequest, status = " + sim.getAcceptRejectStatus());
				
				//sim.exit();
			}
		
			t = System.currentTimeMillis() - t0;
			t = t *0.001;
			if(t > sim.maxTimeOnePeopleInsertion) 
				sim.maxTimeOnePeopleInsertion = t;
			
		}
	}
	
	public ServiceSequence computeParcelInsertionSequence(Vehicle taxi,
			TimePointIndex tpi, ParcelRequest parL, ArrayList<Integer> keptReq, ArrayList<Integer> remainRequestIDs) {
		ArrayList<Integer> parkings = sim.collectAvailableParkings(taxi);
		ServiceSequence ss = null;
		
		//[SonNV] Create new remainRequests array consist of remain request and pr (pickup and delivery).
		int[] sel_nod = new int[remainRequestIDs.size() + 2];
		for(int i = 0; i < remainRequestIDs.size(); i++)
			sel_nod[i] = remainRequestIDs.get(i);
		//insert into end of array.
		sel_nod[remainRequestIDs.size()] = parL.id;
		sel_nod[remainRequestIDs.size() + 1] = -parL.id;
		int sel_pk = -1;
		double minD = 100000000;
		//[SonNV] Get location id of last point in remain requests. In remain requests array, the last element is new parcel request.
		int endLocID = parL.deliveryLocationID;
		
		//[SonNV]Compute distance from last point in remain request to parking. Then,the nearest parking is inserted.
		LatLng endLL = sim.map.mLatLng.get(endLocID);
		for(int k = 0; k < parkings.size(); k++){
			int pk = parkings.get(k);
			LatLng pkLL = sim.map.mLatLng.get(pk);
			if(pkLL == null){
				System.out.println(name() + "::computeParcelInsertionSequence, pkLL is NULL");
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

	public ItineraryServiceSequence computeItineraryParcelInsertion(
			Vehicle taxi, TimePointIndex next_tpi, ParcelRequest parL, ArrayList<Integer> keptReq, ArrayList<Integer> remainRequestIDs) {
		// compute best added itinerary when pr is inserted into taxi.
		ServiceSequence ss = computeParcelInsertionSequence(taxi, next_tpi,
				parL, keptReq, remainRequestIDs);
		int fromIndex = next_tpi.indexPoint;
		int nextStartTimePoint = next_tpi.timePoint;
		int fromPoint = next_tpi.point;
		if (ss == null){
			System.out.println(name() + "::computeItineraryParcelInsertion, ss = NULL --> return NULL");
			sim.log.println(name() + "::computeItineraryParcelInsertion, taxi = " + taxi.ID + ", ss = NULL --> return NULL");
			return null;
		}
		
		//[SonNV]Establish itinerary based on sequence ss.
		ItineraryTravelTime I = sim.establishItinerary(taxi,
				nextStartTimePoint, fromIndex, fromPoint, ss);
		
		if (I == null){
			System.out.println(name() + "::computeItineraryParcelInsertion, establishItinerary I = null");
			sim.log.println(name() + "::computeItineraryParcelInsertion, taxi = " + taxi.ID + ", establishItinerary I = null");
			return null;
		}
		I.setDistance(ss.distance);

		if (I.getDistance() + taxi.totalTravelDistance > sim.maxTravelDistance){
			sim.log.println(name() + "::computeItineraryParcelInsertion, taxi = " + taxi.ID + ", taxi.totalDistance = " + 
		taxi.totalTravelDistance + " >  sim.maxTravelDistance " + sim.maxTravelDistance);
			
			return null;
		}

		return new ItineraryServiceSequence(taxi, I, ss);
	}
	
	public void insertParcelRequest(ParcelRequest parL, Vehicle taxi, TimePointIndex tpi, ArrayList<Integer> keptReq, 
			ArrayList<Integer> remainRequestIDs, double maxTime){
		ItineraryServiceSequence IS = computeItineraryParcelInsertion(taxi,tpi, parL, keptReq, remainRequestIDs);
		if(IS == null){
			System.out.println(name() + "::insertParcelRequest " + " IS = null");
			//sim.nbParcelRejects ++;
			for(int i = 0; i < sim.insertedParcelRequests.size(); i++){
				ParcelRequest pr = sim.insertedParcelRequests.get(i);
				if(pr.id == parL.id){
					sim.insertedParcelRequests.remove(i);
					sim.nbParcelRequestsProcessed --;
					break;
				}
			}
			System.out.println(name() + "::insertParcelRequest --> request "
					+ " is REJECTED due to sel_IS = null, nbPeopleRejected = "
					+ sim.nbPeopleRejects + "/" + sim.allPeopleRequests.size());
			sim.log.println(name() + "::insertParcelRequest --> request "
					+ " is REJECTED --> System.exit(-1)");
			//sim.log.close();
			//System.exit(-1);
		}else{
			System.out.println(name() + "::insertParcelRequest, " + " IS = NOT null");
			ErrorMSG err = sim.checkServiceSequence(taxi, keptReq, IS.ss.rids, IS.ss.rids.length); 
			if(err.err != ErrorType.NO_ERROR){
				System.out.println(name() + "::insertParcelRequest, pr = "  + ", taxi = " + taxi.ID + ", IS not LEGAL?????" + "\" + "
						+ "\n peopleOnBoard = " + Utility.arr2String(taxi.peopleReqIDonBoard) + ", parcelOnBoard = " + 
						Utility.arr2String(taxi.parcelReqIDonBoard) + ", sequence = " + Utility.arr2String(IS.ss.rids) + 
						"\n keptReq = " + Utility.arr2String(keptReq) + ", remainrequestIDs = " + 
						Utility.arr2String(remainRequestIDs) + ", taxi.remainRequestIDs  " + Utility.arr2String(taxi.remainRequestIDs));
				
				
				sim.log.close();
				System.exit(-1);;
			}
			if(taxi.ID == sim.debugTaxiID)
				sim.log.println(name() + "::insertParcelRequest, DEBUG taxi = " + taxi.ID + "\n peopleOnBoard = " + 
			Utility.arr2String(taxi.peopleReqIDonBoard) + ", parcelOnBoard = " + 
					Utility.arr2String(taxi.parcelReqIDonBoard) + ", admit sequence = " + Utility.arr2String(IS.ss.rids) + 
					"\n keptReq = " + Utility.arr2String(keptReq) + ", remainrequestIDs = " + 
					Utility.arr2String(remainRequestIDs) + ", taxi.remainRequestIDs  " + Utility.arr2String(taxi.remainRequestIDs));
			
			//sim.admitNewItinerary(taxi, tpi.timePoint, tpi.indexPoint, tpi.point, IS.I, IS.ss);
			sim.admitNewItineraryWithoutStatus(taxi, tpi.timePoint, tpi.indexPoint, tpi.point, IS.I, IS.ss);
			sim.nbParcelWaitBoarding ++;
			if(taxi.ID == sim.debugTaxiID){
				sim.log.println(name() + "::insertParcelRequest, AFTER admit itinerary, currentItinerary = " + taxi.currentItinerary.toString());
				
			}
			System.out.println(name()
					+ "::insertParcelRequest, nbParcelWaitBoarding = "
					+ sim.nbParcelWaitBoarding + ", nbParcelComplete = "
					+ sim.nbParcelComplete + ", nbPeopleOnBoard = "
					+ sim.nbPeopleOnBoard + ", nbParcelRejects = "
					+ sim.nbParcelRejects + ", total ParcelRequests = "
					+ sim.allParcelRequests.size());
		}
	}
	
	public void processPeopleRequests(ArrayList<PeopleRequest> pr) {
		// TODO Auto-generated method stub
		double startDecideTime = System.currentTimeMillis();
		for(int i = 0; i < pr.size(); i++){
			double t = (System.currentTimeMillis()-startDecideTime)*0.001;
			if(t > sim.maxTimeAllowedOnlineDecision){
				sim.nbPeopleRejects++;
				System.out.println(name() + "::processPeopleRequests, nbPeopleRejects = " + sim.nbPeopleRejects + 
						" DUE TO online decision time expired i = " + i + "/" + pr.size() + ", t = " + t + 
						", maxTimeAllowedOnlineDecision = " + sim.maxTimeAllowedOnlineDecision);
				continue;
			}
			PeopleRequest pri = pr.get(i);
			insertPeopleRequest(pri, sim.maxTimeAllowedInsertOnePeople);
		}
		double t1 = System.currentTimeMillis() - startDecideTime;
		t1 = t1*0.001;
		if(sim.maxDecideTimePeopleRequests < t1) sim.maxDecideTimePeopleRequests = t1;
	}
	
	public void processParcelRequests(ParcelRequest parReq, Vehicle taxi) {
		// TODO Auto-generated method stub
		double startDecideTime = System.currentTimeMillis();
			double t = (System.currentTimeMillis()-startDecideTime)*0.001; 
			TaxiTimePointIndex ttpi = sim.estimatePickupPlusDeliveryDistanceTaxi(taxi, parReq);
			//TaxiTimePointIndex ttpi = sim.estimatePickupPlusDeliveryDistanceTaxi(taxi, pr);
			double t0 = System.currentTimeMillis();
			insertParcelRequest(parReq,ttpi.taxi,ttpi.tpi,ttpi.keptRequestIDs, ttpi.remainRequestIDs,
					sim.maxTimeAllowedInsertOneParcel);
			t = System.currentTimeMillis() - t0;
			t = t*0.001;
			if(t > sim.maxTimeOneParcelInsertion) sim.maxTimeOneParcelInsertion = t;
			
			System.out.println(name() + "::procesParcelRequests, sim.status = " + sim.getAcceptRejectStatus());
		//IO.moveGreedyExchangeSARP2014(startDecideTime);
	}
}
