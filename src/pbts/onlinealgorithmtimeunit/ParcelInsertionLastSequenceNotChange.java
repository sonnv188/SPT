package pbts.onlinealgorithmtimeunit;

import java.io.PrintWriter;
import java.util.ArrayList;

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

public class ParcelInsertionLastSequenceNotChange implements OnlineParcelInsertion {

	public SimulatorTimeUnit sim;
	public PrintWriter log;
	public ParcelInsertionLastSequenceNotChange(SimulatorTimeUnit sim){
		this.sim = sim;
		this.log = sim.log;
				
	}
	public String name(){ return "ParcelInsertionLastSequenceNotChange";}
	
	public TaxiTimePointIndex findTaxiForParcelInsertion(ParcelRequest pr){
		double minDis = 1000000000;
		TaxiTimePointIndex sel_ttpi = null;
		double t0 = System.currentTimeMillis();
		for(int i = 0; i < sim.vehicles.size(); i++){
			Vehicle taxi = sim.vehicles.get(i);
			if (taxi.status == VehicleStatus.STOP_WORK)
				continue;
			if (taxi.totalTravelDistance > sim.maxTravelDistance)
				continue;
			if (taxi.remainRequestIDs.size()+2 > sim.maxPendingStops)// consider also pickup and delivery points of pr
				continue;
			TaxiTimePointIndex ttpi = sim.estimatePickupPlusDeliveryDistanceTaxi(taxi, pr);
			if(ttpi.estimation <  minDis){
				minDis = ttpi.estimation;
				sel_ttpi = ttpi;
			}
		}
		double t = System.currentTimeMillis() - t0;
		t = t * 0.001;
		if(t > sim.maxTimeFindTaxiParcelInsertion) sim.maxTimeFindTaxiParcelInsertion = t;
		return sel_ttpi;
	}
	
	public ServiceSequence computeParcelInsertionSequence(Vehicle taxi,
			TimePointIndex tpi, ParcelRequest pr, ArrayList<Integer> keptReq, ArrayList<Integer> remainRequestIDs) {
		int startIdx = 0;
		// int taxiID = 1;
		int nextStartPoint = tpi.point;// taxi.getNextStartPoint();
		int startTimePoint = tpi.timePoint;// taxi.getNextStartTimePoint();

		if(taxi.ID == sim.debugTaxiID){
			log.println(name() + "::computeParcelInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + " tpi = " + tpi.toString() + 
					", keptReq = " + Utility.arr2String(keptReq) + ", remainrequestIDs = "
				+ Utility.arr2String(remainRequestIDs) + ", taxi.requestStatus = " + taxi.requestStatus());		
		}
		ArrayList<Integer> parkings = sim.collectAvailableParkings(taxi);
		ServiceSequence ss = null;
		int[] r = new int[remainRequestIDs.size() + 2];
		for(int i = 0; i < remainRequestIDs.size(); i++)
			r[i] = remainRequestIDs.get(i);
		r[r.length-2] = pr.id;
		r[r.length-1] = -pr.id;
		
		int[] t_sel_nod = new int[r.length + keptReq.size()];//seqOptimizer.computeShortestSequence(taxi, nextStartPoint, keptReq, r);
		for(int i = 0; i < keptReq.size(); i++)
			t_sel_nod[i] = keptReq.get(i);
		for(int i = 0; i < r.length; i++)
			t_sel_nod[i + keptReq.size()] = r[i];
		
			
		if(t_sel_nod == null){
			log.println(name() + "::computeParcelInsertionSequence taxi = " + taxi.ID + ", sel_nod = NULL NO SOLUTION?? " + 
		", taxi.requestStatus = " + taxi.requestStatus());
			return null;
		}
		int[] sel_nod = new int[t_sel_nod.length - keptReq.size()];
		for(int i = keptReq.size(); i < t_sel_nod.length; i++){
			sel_nod[i-keptReq.size()] = t_sel_nod[i];
		}
		if(taxi.ID == sim.debugTaxiID){
			log.println(name() + "::computeParcelInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + 
					", OBTAIN sel_nod = " + Utility.arr2String(sel_nod));
			System.out.println(name() + "::computeParcelInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + 
					", OBTAIN sel_nod = " + Utility.arr2String(sel_nod));
			
		}
		int sel_pk = -1;
		double minD = 100000000;
		int endReq = sel_nod[sel_nod.length-1];
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
			Vehicle taxi, TimePointIndex next_tpi, ParcelRequest pr, ArrayList<Integer> keptReq, ArrayList<Integer> remainRequestIDs) {
		// compute best added itinerary when pr is inserted into taxi
		ServiceSequence ss = computeParcelInsertionSequence(taxi, next_tpi,
				pr, keptReq, remainRequestIDs);
		int fromIndex = next_tpi.indexPoint;
		int nextStartTimePoint = next_tpi.timePoint;
		int fromPoint = next_tpi.point;
		if (ss == null){
			System.out.println(name() + "::computeItineraryParcelInsertion, ss = NULL --> return NULL");
			sim.log.println(name() + "::computeItineraryParcelInsertion, taxi = " + taxi.ID + ", ss = NULL --> return NULL");
			return null;
		}
		/*
		// int taxiID = 47;
		int reqID = -1;
		if (taxi.currentItinerary.size() > 0)
			reqID = taxi.currentItinerary.getRequestID(fromIndex);
		if (taxi.ID == sim.debugTaxiID) {
			sim.log.println(name()
					+ "::computeItineraryParcelInsertion DEBUG, "
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

	public void insertParcelRequest(ParcelRequest pr, Vehicle taxi, TimePointIndex tpi, ArrayList<Integer> keptReq, 
			ArrayList<Integer> remainRequestIDs){
		ItineraryServiceSequence IS = computeItineraryParcelInsertion(taxi,tpi, pr, keptReq, remainRequestIDs);
		if(IS == null){
			System.out.println(name() + "::insertParcelRequest, pr = " + pr.id + " IS = null");
			sim.nbParcelRejects++;
			System.out.println(name() + "::insertParcelRequest --> request "
					+ pr.id + " is REJECTED due to sel_IS = null, nbPeopleRejected = "
					+ sim.nbPeopleRejects + "/" + sim.allPeopleRequests.size());
			sim.log.println(name() + "::insertParcelRequest --> request " + pr.id
					+ " is REJECTED --> System.exit(-1)");
			//sim.log.close();
			//System.exit(-1);
		}else{
			System.out.println(name() + "::insertParcelRequest, pr = " + pr.id + " IS = NOT null");
			ErrorMSG err = sim.checkServiceSequence(taxi, keptReq, IS.ss.rids, IS.ss.rids.length); 
			if(err.err != ErrorType.NO_ERROR){
				System.out.println(name() + "::insertParcelRequest, pr = " + pr.id + ", taxi = " + taxi.ID + ", IS not LEGAL?????" + "\" + "
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
			sim.nbParcelWaitBoarding++;
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

	public void processParcelRequests(ArrayList<ParcelRequest> parReq, double startDecideTime) {
		// TODO Auto-generated method stub
		System.out.println(name() + "::processParcelRequests(parReq.sz = " + parReq.size() + ")");
		for(int i = 0; i < parReq.size(); i++){
			//System.out.println("AAAAAAAAAAAAAAAAAAAAAA");
			if((System.currentTimeMillis()-startDecideTime)*0.001 > sim.TimePointDuration){
				sim.nbParcelRejects++;
				continue;
			}
			ParcelRequest pr = parReq.get(i);
			TaxiTimePointIndex ttpi = findTaxiForParcelInsertion(pr);
			System.out.println(name() + "::processParcelRequests(pr = " + pr.id + " --> found taxi " + ttpi.taxi.ID + ")");
			double t0 = System.currentTimeMillis();
			insertParcelRequest(pr,ttpi.taxi,ttpi.tpi,ttpi.keptRequestIDs, ttpi.remainRequestIDs);
			double t = System.currentTimeMillis() - t0;
			t = t*0.001;
			if(t > sim.maxTimeOneParcelInsertion) sim.maxTimeOneParcelInsertion = t;
			System.out.println(name() + "::procesParcelRequests, sim.status = " + sim.getAcceptRejectStatus());
		}
	}

}
