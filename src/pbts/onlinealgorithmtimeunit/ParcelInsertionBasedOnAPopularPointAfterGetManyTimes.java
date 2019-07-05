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

public class ParcelInsertionBasedOnAPopularPointAfterGetManyTimes implements OnlineParcelInsertion {
	public SimulatorTimeUnit sim;
	public PrintWriter log;
	
	public ParcelInsertionBasedOnAPopularPointAfterGetManyTimes(SimulatorTimeUnit sim){
		this.sim = sim;
		this.log = sim.log;
	}
	public String name(){
		return "ParcelInsertionBasedOnAPopularPointAfterGetManyTimes";
	}
	
	/****[SonNV]
	 * Find nearest taxi for parcel insertion.
	 * @param:
	 * 		pr			:		parcel request.
	 * 		maxTime		:		max time allowed finding best taxi for parcel insertion.
	 */
	public TaxiTimePointIndex findTaxiForParcelInsertion(ParcelRequest pr, double maxTime){
		double minDis = 1000000000;
		double t0 = System.currentTimeMillis();
		TaxiTimePointIndex sel_ttpi = null;
		for(int i = 0; i < sim.vehicles.size(); i++){
			Vehicle taxi = sim.vehicles.get(i);
			if (taxi.status == VehicleStatus.STOP_WORK)
				continue;
			if (taxi.totalTravelDistance > sim.maxTravelDistance)
				continue;
			if (taxi.remainRequestIDs.size()+2 > sim.maxPendingStops)// consider also pickup and delivery points of pr
				continue;
			TaxiTimePointIndex ttpi = sim.estimatePickupPlusDeliveryDistanceTaxi(taxi, pr);
			//[SonNV]Compared distance to others and get the taxi which has shortest distance.
			if(ttpi != null)if(ttpi.estimation <  minDis){
				minDis = ttpi.estimation;
				sel_ttpi = ttpi;
			}
			double t = (System.currentTimeMillis() - t0)*0.001;
			if(t > maxTime){
				System.out.println(name() + "::findTaxiForParcelInsertion, i = " + i + "/" + sim.vehicles.size() + 
						", t = " + t + " EXPIRED maxTime = " + maxTime);
				break;
			}
		}
		double t = System.currentTimeMillis() - t0;
		t = t * 0.001;
		if(t > sim.maxTimeFindTaxiParcelInsertion) sim.maxTimeFindTaxiParcelInsertion = t;
		return sel_ttpi;
	}
	
	/****[SonNV]
	 * Compute a sequence of point consist of remain request points and new request point and nearest parking.
	 * @param:
	 * 		taxi			:		Taxi is chosen to add.
	 * 		tpi				: 		Information of the taxi.
	 * 		pr				:		New parcel request.
	 * 		keptReq			: 		Requests are kept.
	 * 		remainRequests	:		Remain requests.
	 */
	public ServiceSequence computeParcelInsertionSequence(Vehicle taxi,
			TimePointIndex tpi, ParcelRequest pr, ArrayList<Integer> keptReq, ArrayList<Integer> remainRequestIDs, int idx) {
		if(taxi.ID == sim.debugTaxiID){
			log.println(name() + "::computeParcelInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + " tpi = " + tpi.toString() + 
					", keptReq = " + Utility.arr2String(keptReq) + ", remainrequestIDs = "
				+ Utility.arr2String(remainRequestIDs) + ", taxi.requestStatus = " + taxi.requestStatus());
		}
		ArrayList<Integer> parkings = sim.collectAvailableParkings(taxi);
		ServiceSequence ss = null;
		
		//[SonNV] Create new remainRequests array consist of remain request and pr (pickup and delivery).
		int[] sel_nod = new int[remainRequestIDs.size() + 2];
		for(int i = 0; i <= idx; i++)
			sel_nod[i] = remainRequestIDs.get(i);
		//insert new request point into array .
		sel_nod[idx + 1] = pr.id;

		int newPickLocID = pr.pickupLocationID;
		//LatLng newPickLL = sim.map.mLatLng.get(newPickLocID);
		int newDeliveryLocID = pr.deliveryLocationID;
		//LatLng newDeliveryLL = sim.map.mLatLng.get(newDeliveryLocID);
		double minD = sim.estimateTravelingDistanceHaversine(newPickLocID, newDeliveryLocID);
		//double minD = sim.G.computeDistanceHaversine(newPickLL.lat, newPickLL.lng, newDeliveryLL.lat, newDeliveryLL.lng);
		int deliveryIdx = idx;
		for(int i = idx + 1; i < remainRequestIDs.size(); i++){
			int rid = remainRequestIDs.get(i);
			int curLocId = -1;
			ParcelRequest parR = sim.mParcelRequest.get(Math.abs(rid));
			if(parR != null){
				if(rid > 0)
					curLocId = parR.pickupLocationID;
				else
					curLocId = parR.deliveryLocationID;
			}
			else{
				PeopleRequest peoR = sim.mPeopleRequest.get(Math.abs(rid));
				if(rid > 0)
					curLocId = peoR.pickupLocationID;
				else
					curLocId = peoR.deliveryLocationID;
			}
			//LatLng curLL = sim.map.mLatLng.get(curLocId);
			//if(curLL == null){
			//	System.out.println(name() + "::computeParcelInsertionSequence, curLL is NULL");
			//}
			double D = sim.estimateTravelingDistanceHaversine(curLocId, newDeliveryLocID);
			//double D = sim.G.computeDistanceHaversine(curLL.lat, curLL.lng, newDeliveryLL.lat, newDeliveryLL.lng);
			if(D < minD){
				minD = D;
				deliveryIdx = i;
			}
		}
		
		for(int i = idx + 1; i <= deliveryIdx; i++)
			sel_nod[i + 1] = remainRequestIDs.get(i);
		sel_nod[deliveryIdx + 2] = -pr.id;
		for(int i = deliveryIdx + 1; i < remainRequestIDs.size(); i++)
			sel_nod[i + 2] = remainRequestIDs.get(i);
		
		//Compute distance from last point in remain request to parking. Then,the nearest parking is inserted.
		//Get last delivery point (last element in sel_nod)
		int endReq = sel_nod[sel_nod.length-1];
		int endLocID = -1;
		PeopleRequest peoR = sim.mPeopleRequest.get(Math.abs(endReq));
		if(peoR != null){
			if(endReq < 0) endLocID = peoR.deliveryLocationID; else endLocID = peoR.pickupLocationID;
		}else{
			ParcelRequest parR = sim.mParcelRequest.get(Math.abs(endReq));
			if(endReq < 0) endLocID = parR.deliveryLocationID; else endLocID = parR.pickupLocationID;
		}
		
		int sel_pk = -1;
		minD = 100000000;
		//LatLng endLL = sim.map.mLatLng.get(endLocID);
		for(int k = 0; k < parkings.size(); k++){
			int pk = parkings.get(k);
			LatLng pkLL = sim.map.mLatLng.get(pk);
			if(pkLL == null){
				System.out.println(name() + "::computeParcelInsertionSequence, pkLL is NULL");
			}
			double D = sim.estimateTravelingDistanceHaversine(endLocID, pk);
			//double D = sim.G.computeDistanceHaversine(endLL.lat, endLL.lng, pkLL.lat, pkLL.lng);
			if(D < minD){
				minD = D;
				sel_pk = pk;
			}
		}
		
		ss = new ServiceSequence(sel_nod, 0, sel_pk, minD);
		return ss;
	}

	public ItineraryServiceSequence computeItineraryParcelInsertion(
			Vehicle taxi, TimePointIndex next_tpi, ParcelRequest pr, ArrayList<Integer> keptReq, ArrayList<Integer> remainRequestIDs, int idx) {
		// compute best added itinerary when pr is inserted into taxi.
		ServiceSequence ss = computeParcelInsertionSequence(taxi, next_tpi,
				pr, keptReq, remainRequestIDs, idx);
		int fromIndex = next_tpi.indexPoint;
		int nextStartTimePoint = next_tpi.timePoint;
		int fromPoint = next_tpi.point;
		if (ss == null){
			System.out.println(name() + "::computeItineraryParcelInsertion, ss = NULL --> return NULL");
			sim.log.println(name() + "::computeItineraryParcelInsertion, taxi = " + taxi.ID + ", ss = NULL --> return NULL");
			return null;
		}
		//[SonNV]Establish itinerary based on sequence ss and a popular point.
		ItineraryTravelTime I = sim.establishItineraryWithAPopularPoint(taxi,
				nextStartTimePoint, fromIndex, fromPoint, ss, 2);
		
		if (I == null){
			System.out.println(name() + "::computeItineraryParcelInsertion, establishItinerary I = null");
			sim.log.println(name() + "::computeItineraryParcelInsertion, taxi = " + taxi.ID + ", establishItinerary I = null");
			return null;
		}
		I.setDistance(ss.distance);
		//ss.setDistance(I.getDistance());
		//ss.setParkingLocation(I.get(I.size()-1));

		if (I.getDistance() + taxi.totalTravelDistance > sim.maxTravelDistance){
			sim.log.println(name() + "::computeItineraryParcelInsertion, taxi = " + taxi.ID + ", taxi.totalDistance = " + 
		taxi.totalTravelDistance + " >  sim.maxTravelDistance " + sim.maxTravelDistance);
			
			return null;
		}

		return new ItineraryServiceSequence(taxi, I, ss);
	}

	public void insertParcelRequest(ParcelRequest pr, Vehicle taxi, TimePointIndex tpi, ArrayList<Integer> keptReq, 
			ArrayList<Integer> remainRequestIDs, int idx, double maxTime){
		ItineraryServiceSequence IS = computeItineraryParcelInsertion(taxi,tpi, pr, keptReq, remainRequestIDs, idx);
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

	/****[SonNV]
	 * Process parcel request inserted: 
	 * 		+ Find nearest taxi: status of all taxis is updated in decision time and the shortest distance from taxi to pickup and delivery point is calculated.
	 * 		+ Insert parcel:
	 * @param:
	 * 		parReq			:		list parcels need insert.
	 * 		startDecideTime :		decided time.
	 */
	public void processParcelRequests(ArrayList<ParcelRequest> parReq, double startDecideTime) {
		// TODO Auto-generated method stub
		System.out.println(name() + "::processParcelRequests(parReq.sz = " + parReq.size() + ")");
		for(int i = 0; i < parReq.size(); i++){
			//System.out.println("AAAAAAAAAAAAAAAAAAAAAA");
			double t = (System.currentTimeMillis()-startDecideTime)*0.001; 
			if(t > sim.maxTimeAllowedOnlineDecision){
				sim.nbParcelRejects++;
				System.out.println(name() + "::processParcelRequests, nbParcelRejects = " + sim.nbParcelRejects + 
						" DUE TO online decision time expired t = " + t + ", sim.maxTimeAllowedOnlineDecision = " + sim.maxTimeAllowedOnlineDecision);
				continue;
			}
			ParcelRequest pr = parReq.get(i);
			TaxiTimePointIndex ttpi = findTaxiForParcelInsertion(pr,sim.maxTimeAllowedFindingBestTaxiForParcel);
			if(ttpi.taxi.status == VehicleStatus.TRAVEL_WITHOUT_LOAD)
				sim.nbTaxisPickUpParcelOnBoard++;
			//System.out.println(name() + "::processParcelRequests(pr = " + pr.id + " --> found taxi " + ttpi.taxi.ID + ")");
			double t0 = System.currentTimeMillis();
			if(ttpi == null){
				sim.nbParcelRejects++;
				System.out.println(name() + "::processParcelRequests, nbParcelRejects = " + sim.nbParcelRejects + 
						"DUE TO no available taxi found");
			}else{
				if(ttpi.taxi.status == VehicleStatus.TRAVEL_WITHOUT_LOAD)
					sim.nbTaxisPickUpParcelOnBoard++;
				System.out.println(name() + "::processParcelRequests: nbTaxisPickUpParcelOnBoard = " + sim.nbTaxisPickUpParcelOnBoard);
				insertParcelRequest(pr,ttpi.taxi,ttpi.tpi,ttpi.keptRequestIDs, ttpi.remainRequestIDs, ttpi.idx,
					sim.maxTimeAllowedInsertOneParcel);
			}
			t = System.currentTimeMillis() - t0;
			t = t*0.001;
			if(t > sim.maxTimeOneParcelInsertion) sim.maxTimeOneParcelInsertion = t;
			
			System.out.println(name() + "::procesParcelRequests, sim.status = " + sim.getAcceptRejectStatus());
		}
	}

}
