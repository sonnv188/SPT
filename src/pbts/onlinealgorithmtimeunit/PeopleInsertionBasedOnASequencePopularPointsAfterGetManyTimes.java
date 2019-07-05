package pbts.onlinealgorithmtimeunit;

import java.io.PrintWriter;
import java.util.ArrayList;

import pbts.entities.ErrorMSG;
import pbts.entities.ItineraryTravelTime;
import pbts.entities.PeopleRequest;
import pbts.entities.TaxiTimePointIndex;
import pbts.entities.TimePointIndex;
import pbts.entities.Vehicle;
import pbts.enums.ErrorType;
import pbts.enums.VehicleStatus;
import pbts.prediction.behrouz.fixedrate.FixedRateSampler;
import pbts.simulation.ItineraryServiceSequence;
import pbts.simulation.ServiceSequence;
import pbts.simulation.SimulatorTimeUnit;
import pbts.simulation.Utility;

public class PeopleInsertionBasedOnASequencePopularPointsAfterGetManyTimes implements OnlinePeopleInsertion {
	public SimulatorTimeUnit sim;
	public PrintWriter log;
	private FixedRateSampler frs;
	
	public PeopleInsertionBasedOnASequencePopularPointsAfterGetManyTimes(SimulatorTimeUnit sim){
		this.sim = sim;
		this.log = sim.log;
		this.frs = new FixedRateSampler();
	}
	public String name(){
		return "PeopleInsertionBasedOnASequencePopularPointsAfterGetManyTimes";
	}

	public ServiceSequence computePeopleInsertionSequence(Vehicle taxi,
			TimePointIndex tpi, PeopleRequest pr, ArrayList<Integer> keptReq, ArrayList<Integer> remainRequestIDs) {
		if(taxi.ID == sim.debugTaxiID){
			log.println(name() + "::computePeopleInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + " tpi = " + tpi.toString() + 
					", keptReq = " + Utility.arr2String(keptReq) + ", remainrequestIDs = "
				+ Utility.arr2String(remainRequestIDs) + ", taxi.requestStatus = " + taxi.requestStatus());		
		}
		//ArrayList<Integer> parkings = sim.collectAvailableParkings(taxi);
		ServiceSequence ss = null;
		
		//[SonNV] Create new sequence consist of pr (pickup and delivery) and remain requests.
		int[] sel_nod = new int[2 + remainRequestIDs.size()];
		
		//insert new people request point into sequence.
		sel_nod[0] = pr.id;
		sel_nod[1] = -pr.id;
		
		//insert remain request into sequence
		for(int i = 0; i < remainRequestIDs.size(); i++)
			sel_nod[i + 2] = remainRequestIDs.get(i);

		ss = new ServiceSequence(sel_nod, 0, -1, -1);
		return ss;
	}

	public ItineraryServiceSequence computeItineraryPeopleInsertion(
			Vehicle taxi, TimePointIndex next_tpi, PeopleRequest pr, 
			ArrayList<Integer> keptReq, ArrayList<Integer> remainRequestIDs, double maxTime) {
		// compute best added itinerary when pr is inserted into taxi
		ServiceSequence ss = computePeopleInsertionSequence(taxi, next_tpi,
				pr, keptReq, remainRequestIDs);
		int fromIndex = next_tpi.indexPoint;
		int nextStartTimePoint = next_tpi.timePoint;
		int fromPoint = next_tpi.point;
		if (ss == null){
			System.out.println(name() + "::computeItineraryPeopleInsertion, ss = NULL --> return NULL");
			sim.log.println(name() + "::computeItineraryPeopleInsertion, taxi = " + taxi.ID + ", ss = NULL --> return NULL");
			return null;
		}
		//Establish itinerary based on sequence ss and a sequence popular points.
		ItineraryTravelTime I = sim.establishItineraryWithAroundPopularPoints(taxi, nextStartTimePoint, fromIndex, fromPoint, ss, 2);
		
		if (I == null){
			System.out.println(name() + "::computeItineraryPeopleInsertion, establishItinerary I = null");
			sim.log.println(name() + "::computeItineraryPeopleInsertion, taxi = " + taxi.ID + ", establishItinerary I = null");
			return null;
		}
		//I.setDistance(ss.distance);
		ss.setDistance(I.getDistance());
		ss.setParkingLocation(I.get(I.size()-1));

		if (I.getDistance() + taxi.totalTravelDistance > sim.maxTravelDistance){
			sim.log.println(name() + "::computeItineraryPeopleInsertion, taxi = " + taxi.ID + ", taxi.totalDistance = " + 
		taxi.totalTravelDistance + " >  sim.maxTravelDistance " + sim.maxTravelDistance);
			
			return null;
		}

		return new ItineraryServiceSequence(taxi, I, ss);
	}

	public void insertPeopleRequest(PeopleRequest pr, double maxTime){
		double t0 = System.currentTimeMillis();
		TaxiTimePointIndex ttpi = sim.getNearestAvailableTaxiForPeople(pr, sim.maxTimeAllowedInsertOnePeople);
		double t= System.currentTimeMillis() - t0;
		t = t *0.001;
		if(t > sim.maxTimeCollectAvailableTaxisPeopleInsertion) sim.maxTimeCollectAvailableTaxisPeopleInsertion = t;
		
		if(ttpi == null){
			sim.nbPeopleRejects++;
			System.out.println(name() + "::insertPeopleRequest(pr = " + pr.id + "), nbPeopleRejects = " + sim.nbPeopleRejects + " DUE TO no available taxi found");
		}else{
			if(ttpi.taxi.status == VehicleStatus.TRAVEL_WITHOUT_LOAD)
				sim.nbTaxisPickUpPeopleOnBoard++;
			System.out.println("::getNearestAvailableTaxiForPeople: " + "nbTaxisPickUpPeopleOnBoard = " + sim.nbTaxisPickUpPeopleOnBoard);
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

	public void processPeopleRequests(ArrayList<PeopleRequest> prList,
			double startDecideTime) {
		// TODO Auto-generated method stub
		for(int i = 0; i < prList.size(); i++){
			double t = (System.currentTimeMillis()-startDecideTime)*0.001;
			if(t > sim.maxTimeAllowedOnlineDecision){
				sim.nbPeopleRejects++;
				System.out.println(name() + "::processPeopleRequests, nbPeopleRejects = " + sim.nbPeopleRejects + 
						" DUE TO online decision time expired i = " + i + "/" + prList.size() + ", t = " + t + 
						", maxTimeAllowedOnlineDecision = " + sim.maxTimeAllowedOnlineDecision);
				continue;
			}
			PeopleRequest pr = prList.get(i);
			insertPeopleRequest(pr, sim.maxTimeAllowedInsertOnePeople);
		}
	}
}
