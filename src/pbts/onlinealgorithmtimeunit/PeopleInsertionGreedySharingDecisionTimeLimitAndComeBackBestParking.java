package pbts.onlinealgorithmtimeunit;

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
import pbts.simulation.ItineraryServiceSequence;
import pbts.simulation.ServiceSequence;
import pbts.simulation.SimulatorTimeUnit;
import pbts.simulation.Utility;

public class PeopleInsertionGreedySharingDecisionTimeLimitAndComeBackBestParking implements OnlinePeopleInsertion {

	public SimulatorTimeUnit sim;
	public PrintWriter log;
	public SequenceOptimizer seqOptimizer = null;
	
	public PeopleInsertionGreedySharingDecisionTimeLimitAndComeBackBestParking(SimulatorTimeUnit sim){
		this.sim = sim;
		this.log = sim.log;
		seqOptimizer = new SequenceOptimizer(sim, sim.maxPendingStops + 10);
	}
	public String name(){
		return "PeopleInsertionGreedySharingDecisionTimeLimitAndComeBackBestParking";
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
		double rate = 0;
		int endReq = sel_nod[sel_nod.length-1];
		//System.out.println(name() + "::computePeopleInsertionSequence, endReq = " + endReq);
		int endLocID = -1;
		int lastTime = -1;
		PeopleRequest peoR = sim.mPeopleRequest.get(Math.abs(endReq));
		if(peoR != null){
			if(endReq < 0){
				endLocID = peoR.deliveryLocationID;
				lastTime = peoR.lateDeliveryTime;
			}
			else{
				endLocID = peoR.pickupLocationID;
				lastTime = peoR.latePickupTime;
			}
		}else{
			ParcelRequest parR = sim.mParcelRequest.get(Math.abs(endReq));
			if(endReq < 0){
				endLocID = parR.deliveryLocationID;
				lastTime = parR.lateDeliveryTime;
			}
			else{ 
				endLocID = parR.pickupLocationID;
				lastTime = parR.latePickupTime;
			}
		}
		lastTime = sim.calculateTotalTimeTravelOfSequence(startTimePoint, nextStartPoint, taxi, sel_nod);
		if(lastTime > 85500)
			lastTime = 85500;
		LatLng endLL = sim.map.mLatLng.get(endLocID);
		int nPp = 0;
		int nDeparture = 0;
		for(int i = 0; i < sim.lstParkings.size(); i++){
			Parking park = sim.lstParkings.get(i);
			if(park.load < park.capacity){
				LatLng pkLL = sim.map.mLatLng.get(park.locationID);
				double D = sim.G.computeDistanceHaversine(endLL.lat, endLL.lng, pkLL.lat, pkLL.lng);
				if(D < minD){
					minD = D;
					sel_pk = park.locationID;
					nDeparture = park.nTaxisDeparture;
				}
			}
		}
		for(int i = 0; i < sim.lstParkings.size(); i++){
			Parking park = sim.lstParkings.get(i);
			if(park.load < park.capacity){
				LatLng pkLL = sim.map.mLatLng.get(park.locationID);
				double D = sim.G.computeDistanceHaversine(endLL.lat, endLL.lng, pkLL.lat, pkLL.lng);
				for(int period = 0; period < lastTime / 900 + 1; period++){
					nPp += park.nPpNearInPeriod.get(period);
				}
				
				if(nPp != 0){
					if((double)(park.nTaxisDeparture / nPp) > rate && D <= minD * 1.5){
						sel_pk = park.locationID;
						rate = (double)(park.nTaxisDeparture / nPp);
						nDeparture = park.nTaxisDeparture;
					}
				}
			}
		}
		int depotId = sim.mTaxi2Depot.get(taxi.ID);
		if(depotId != sel_pk){
			LatLng depotLL = sim.map.mLatLng.get(depotId);
			double d2 = sim.G.computeDistanceHaversine(endLL.lat, endLL.lng, depotLL.lat, depotLL.lng);
			if(taxi.nComeBackDepot > nDeparture && d2 <= minD * 1.5){
				sel_pk = depotId;
				minD = d2;
			}
		}
		
		if(depotId == sel_pk)
			taxi.nComeBackDepot++;
		
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
		ItineraryTravelTime I = sim.establishItineraryWithAPopularPoint(taxi,
				nextStartTimePoint, fromIndex, fromPoint, ss, 1);
		
		
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
		TaxiTimePointIndex ttpi = sim.getNearestAvailableTaxiForPeople(pr, maxTime);
		double t= System.currentTimeMillis() - t0;
		t = t *0.001;
		if(t > sim.maxTimeCollectAvailableTaxisPeopleInsertion) sim.maxTimeCollectAvailableTaxisPeopleInsertion = t;
		
		if(ttpi == null){
			sim.nbPeopleComplete++;
			System.out.println(name() + "::insertPeopleRequest(pr = " + pr.id + "), nbPeopleRejects = " + sim.nbPeopleRejects + " DUE TO no available taxi found");
		}else{
			t0 = System.currentTimeMillis();
			ItineraryServiceSequence IS = computeItineraryPeopleInsertion(ttpi.taxi, ttpi.tpi, pr, 
					ttpi.keptRequestIDs, ttpi.remainRequestIDs, maxTime);
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
				sim.admitNewItineraryWithoutStatus(ttpi.taxi, ttpi.tpi.timePoint, ttpi.tpi.indexPoint, ttpi.tpi.point, IS.I, IS.ss);
				
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
	public void processPeopleRequests(ArrayList<PeopleRequest> pr,
		double startDecideTime) {
		// TODO Auto-generated method stub
		
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
			//double t0 = System.currentTimeMillis();
			insertPeopleRequest(pri, sim.maxTimeAllowedInsertOnePeople);
			//t = System.currentTimeMillis() - t0;
			//t = t *0.001;
			//if(t > sim.maxTimeOnePeopleInsertion) sim.maxTimeOnePeopleInsertion = t;
		}
	}
	
	public static void main(String[] args) {
	// TODO Auto-generated method stub
	
	}

}
