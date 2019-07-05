package pbts.onlinealgorithmtimeunit;

import java.util.*;

import pbts.algorithms.SequenceOptimizer;
import pbts.entities.*;
import pbts.enums.ErrorType;
import pbts.enums.VehicleStatus;
import pbts.simulation.ServiceSequence;

import java.io.*;
import java.util.ArrayList;



//import choco.Choco;
//import choco.cp.model.CPModel;
//import choco.kernel.model.variables.integer.IntegerVariable;
import pbts.simulation.*;

public class GreedyWithSharing implements OnlinePlanner {

	public PrintWriter log = null;
	public SimulatorTimeUnit sim = null;

	public SequenceOptimizer seqOptimizer = null;
	
	public GreedyWithSharing(SimulatorTimeUnit sim) {
		this.sim = sim;
		this.log = sim.log;
		seqOptimizer = new SequenceOptimizer(sim, sim.maxPendingStops + 10);
	}

	public ServiceSequence computeBestProfitsParcelInsertion(Vehicle taxi,
			TimePointIndex tpi, ParcelRequest pr) {
		int startIdx = 0;
		int idxDelivery = -1;
		int rid = -1;

		int nextStartPoint = tpi.point;// taxi.getNextStartPoint();
		int startTimePoint = tpi.timePoint;// taxi.getNextStartTimePoint();

		// if nextStartPoint is a location of a request, then startIdx must be 1
		if (taxi.remainRequestIDs.size() > 0) {
			int frid = taxi.remainRequestIDs.get(0);
			frid = Math.abs(frid);
			boolean ok = false;
			PeopleRequest peopleReq = sim.mPeopleRequest.get(frid);
			if (peopleReq != null) {
				if (nextStartPoint == peopleReq.pickupLocationID
						|| nextStartPoint == peopleReq.deliveryLocationID)
					ok = true;
			}
			if (!ok) {
				ParcelRequest parcelReq = sim.mParcelRequest.get(frid);
				if (parcelReq != null) {
					if (nextStartPoint == parcelReq.pickupLocationID
							|| nextStartPoint == parcelReq.deliveryLocationID)
						ok = true;
				}
			}
			if (ok)
				startIdx = 1;
		}

		PeopleRequest peopleReq = null;
		if (taxi.peopleReqIDonBoard.size() > 0) {
			rid = taxi.peopleReqIDonBoard.get(0);
			peopleReq = sim.mPeopleRequest.get(rid);

			for (int i = 0; i < taxi.remainRequestIDs.size(); i++) {
				if (taxi.remainRequestIDs.get(i) == -rid) {
					idxDelivery = i;
					break;
				}
			}
			if (sim.countStop.get(rid) + idxDelivery >= peopleReq.maxNbStops) {
				startIdx = idxDelivery + 1;
			}
		}

		int[] nod = new int[taxi.remainRequestIDs.size() + 2];
		// explore all possible position i1 (pickup), i2 (delivery) among
		// taxi.remainRequestIDs for inserting pr
		ArrayList<Integer> parkings = sim.collectAvailableParkings(taxi);
		double maxProfits = -sim.dijkstra.infinity;
		// int sel_pickup_index = -1;
		// int sel_delivery_index = -1;
		// int sel_pk = -1;
		ServiceSequence ss = null;
		double expectDistanceParcel = sim.dijkstra.queryDistance(
				pr.pickupLocationID, pr.deliveryLocationID);
		for (int i1 = startIdx; i1 <= taxi.remainRequestIDs.size(); i1++) {
			for (int i2 = i1; i2 <= taxi.remainRequestIDs.size(); i2++) {
				// establish new sequence of request ids stored in nod
				if (rid > 0) {
					if (i1 <= idxDelivery && i2 <= idxDelivery)// &&
																// sim.countStop.get(rid)
																// + idxDelivery
																// + 2 >
																// peopleReq.maxNbStops)
						continue;
				}
				int idx = -1;
				double profits = sim.getParcelRevenue(expectDistanceParcel);
				for (int k1 = 0; k1 < i1; k1++) {
					idx++;
					nod[idx] = taxi.remainRequestIDs.get(k1);
				}
				idx++;
				nod[idx] = pr.id;// insert pickup
				for (int k1 = 0; k1 < i2 - i1; k1++) {
					idx++;
					nod[idx] = taxi.remainRequestIDs.get(i1 + k1);
				}
				idx++;
				nod[idx] = -pr.id;// insert delivery
				for (int k1 = i2; k1 < taxi.remainRequestIDs.size(); k1++) {
					idx++;
					nod[idx] = taxi.remainRequestIDs.get(k1);
				}
				if (!sim.checkCapacity(taxi.parcelReqIDonBoard.size(), nod,
						Simulator.Qk))
					continue;

				// evaluate the insertion
				double D = sim.computeFeasibleDistance(nextStartPoint,
						startTimePoint, nod);
				if (D > sim.dijkstra.infinity - 1)
					continue;// constraints are violated
				if (D + taxi.totalTravelDistance > sim.maxTravelDistance)
					continue;
				for (int k = 0; k < parkings.size(); k++) {
					int pk = parkings.get(k);
					// double D = computeFeasibleDistance(nextStartPoint,
					// startTimePoint, nod, pk);
					// if(D > dijkstra.infinity - 1) continue;// constraints are
					// violated
					int endPoint = sim
							.getLocationFromEncodedRequest(nod[nod.length - 1]);
					D = D + sim.dijkstra.queryDistance(endPoint, pk);
					double extraDistance = D - taxi.remainDistance;
					profits = profits - sim.getCostFuel(extraDistance);
					if (profits > maxProfits) {
						maxProfits = profits;
						// sel_pickup_index = i1;
						// sel_delivery_index = i2;
						// sel_pk = pk;
						ss = new ServiceSequence(nod, profits, pk, D);
					}
				}
			}
		}

		return ss;
	}

	public String name() {
		return "GreedyWithSharing";
	}

	public ItineraryServiceSequence computeProfitsItineraryPeopleInsertionFirst(
			Vehicle taxi, TimePointIndex next_tpi, PeopleRequest pr, ArrayList<Integer> remainRequestIDs) {
		// compute best added itinerary when pr is inserted into taxi
		ServiceSequence ss = computeProfitsPeopleInsertionFirst(taxi, next_tpi,
				pr, remainRequestIDs);
		int fromIndex = next_tpi.indexPoint;
		int nextStartTimePoint = next_tpi.timePoint;
		int fromPoint = next_tpi.point;
		if (ss == null){
			System.out.println(name() + "::computeProfitsPeopleInsertionFirst, ss = NULL --> return NULL");
			return null;
		}
		/*
		// int taxiID = 47;
		int reqID = -1;
		if (taxi.currentItinerary != null)
			reqID = taxi.currentItinerary.getRequestID(fromIndex);
		if (taxi.ID == sim.debugTaxiID) {
			sim.log.println(name()
					+ "::computeProfitItineraryPeopleInsertionFirst DEBUG, peopleRequest = " + pr.id + ", ss = " + ss.getSequence()
					+ "begin call to sim.establishItinerary("
					+ nextStartTimePoint + "," + fromIndex + "," + reqID + ","
					+ fromPoint + ")");
		}
		ItineraryTravelTime I = sim.establishItinerary(taxi,
				nextStartTimePoint, fromIndex, reqID, fromPoint, ss);
		*/
		ItineraryTravelTime I = sim.establishItinerary(taxi,
				nextStartTimePoint, fromIndex,  fromPoint, ss);
		
		if (I == null){
			System.out.println(name() + "::computeProfitsItineraryPeopleInsertionFirst, establishItinerary I = null");
			return null;
		}
		I.setDistance(ss.distance);

		if (I.getDistance() + taxi.totalTravelDistance > sim.maxTravelDistance)
			return null;

		return new ItineraryServiceSequence(taxi, I, ss);
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
	public ItineraryServiceSequence computeItineraryPeopleInsertion(
			Vehicle taxi, TimePointIndex next_tpi, PeopleRequest pr, ArrayList<Integer> keptReq, ArrayList<Integer> remainRequestIDs) {
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

	public ItineraryServiceSequence computeBestProfitsItineraryPeopleInsertion(
			Vehicle taxi, TimePointIndex next_tpi, PeopleRequest pr, ArrayList<Integer> remainRequestIDs) {
		// compute best added itinerary when pr is inserted into taxi
		ServiceSequence ss = computeBestProfitsPeopleInsertion(taxi, next_tpi,
				pr, remainRequestIDs);
		int fromIndex = next_tpi.indexPoint;
		int nextStartTimePoint = next_tpi.timePoint;
		int fromPoint = next_tpi.point;
		if (ss == null){
			System.out.println(name() + "::computeBestProfitsItinerary, ss = NULL --> return NULL");
			return null;
		}
		/*
		// int taxiID = 47;
		int reqID = -1;
		if (taxi.currentItinerary != null)
			reqID = taxi.currentItinerary.getRequestID(fromIndex);
		if (taxi.ID == sim.debugTaxiID) {
			sim.log.println(name()
					+ "::computeBestProfitItineraryPeopleInsertion DEBUG, "
					+ "begin call to sim.establishItinerary("
					+ nextStartTimePoint + "," + fromIndex + "," + reqID + ","
					+ fromPoint + ")");
		}
		ItineraryTravelTime I = sim.establishItinerary(taxi,
				nextStartTimePoint, fromIndex, reqID, fromPoint, ss);
		
		*/
		ItineraryTravelTime I = sim.establishItinerary(taxi,
				nextStartTimePoint, fromIndex, fromPoint, ss);
		
		if (I == null){
			System.out.println(name() + "::computeBestProfitsItineraryPeopleInsertion, establishItinerary I = null");
			return null;
		}
		I.setDistance(ss.distance);

		if (I.getDistance() + taxi.totalTravelDistance > sim.maxTravelDistance)
			return null;

		return new ItineraryServiceSequence(taxi, I, ss);
	}

	/*
	 * public ItineraryServiceSequence
	 * computeBestProfitsItineraryParcelInsertion(Vehicle taxi, int
	 * nextStartTimePoint, int fromIndex, int fromPoint, ParcelRequest pr){ //
	 * compute best added itinerary when pr is inserted into taxi
	 * ServiceSequence ss = computeBestProfitsParcelInsertion(taxi, pr); if(ss
	 * == null) return null;
	 * 
	 * int reqID = -1; if(taxi.currentItinerary != null) reqID =
	 * taxi.currentItinerary.getRequestID(fromIndex);
	 * 
	 * ItineraryTravelTime I = sim.establishItinerary(taxi,
	 * nextStartTimePoint,fromIndex, reqID,fromPoint,ss); if(I == null) return
	 * null; return new ItineraryServiceSequence(taxi, I, ss); }
	 */

	public ServiceSequence computeBestProfitsPeopleInsertion(Vehicle taxi,
			TimePointIndex tpi, PeopleRequest pr, ArrayList<Integer> remainRequestIDs) {
		int startIdx = 0;
		// int taxiID = 1;
		int nextStartPoint = tpi.point;// taxi.getNextStartPoint();
		int startTimePoint = tpi.timePoint;// taxi.getNextStartTimePoint();

		// if nextStartPoint is a location of a request, then startIdx must be 1
		//if (taxi.remainRequestIDs.size() > 0) {
		if (remainRequestIDs.size() > 0) {
			int frid = taxi.remainRequestIDs.get(0);
			frid = Math.abs(frid);
			boolean ok = false;
			PeopleRequest peopleReq = sim.mPeopleRequest.get(frid);
			if (peopleReq != null) {
				if (nextStartPoint == peopleReq.pickupLocationID
						|| nextStartPoint == peopleReq.deliveryLocationID)
					ok = true;
			}
			if (!ok) {
				ParcelRequest parcelReq = sim.mParcelRequest.get(frid);
				if (parcelReq != null) {
					if (nextStartPoint == parcelReq.pickupLocationID
							|| nextStartPoint == parcelReq.deliveryLocationID)
						ok = true;
				}
			}
			if (ok)
				startIdx = 1;
		}

		if (taxi.peopleReqIDonBoard.size() > 0) {
			// taxi is carrying a passenger
			int rid = taxi.peopleReqIDonBoard.get(0);
			//for (int i = 0; i < taxi.remainRequestIDs.size(); i++) {
			for (int i = 0; i < remainRequestIDs.size(); i++) {
				//if (taxi.remainRequestIDs.get(i) == -rid) {
				if (remainRequestIDs.get(i) == -rid) {
					startIdx = i + 1;
					break;
				}
			}
			if (taxi.ID == sim.debugTaxiID) {
				System.out
						.println("SimulatorBookedRequest::computeBestProfitsPeopleInsertion, "
								+ "request on board = "
								+ rid
								+ ", remainRequestIDs = "
								+ taxi.getRemainRequestID(remainRequestIDs)
								+ ", startIdx = "
								+ startIdx + ", new people quest = " + pr.id);
				log.println("SimulatorBookedRequest::computeBestProfitsPeopleInsertion, "
						+ "request on board = "
						+ rid
						+ ", remainRequestIDs = "
						+ taxi.getRemainRequestID(remainRequestIDs)
						+ ", startIdx = "
						+ startIdx + ", new people quest = " + pr.id);

			}
		}

		//int[] nod = new int[taxi.remainRequestIDs.size() + 2];
		int[] nod = new int[remainRequestIDs.size() + 2];
		// explore all possible position i1 (pickup), i2 (delivery) among
		// taxi.remainRequestIDs for inserting pr
		ArrayList<Integer> parkings = sim.collectAvailableParkings(taxi);
		// double minExtraDistance = dijkstra.infinity;
		// int sel_pickup_index = -1;
		// int sel_delivery_index = -1;
		// int sel_pk = -1;
		double maxProfits = -sim.dijkstra.infinity;
		ServiceSequence ss = null;
		double expectDistancePeople = sim.dijkstra.queryDistance(
				pr.pickupLocationID, pr.deliveryLocationID);
		//for (int i1 = startIdx; i1 <= taxi.remainRequestIDs.size(); i1++) {
		for (int i1 = startIdx; i1 <= remainRequestIDs.size(); i1++) {	
			//int maxI2 = taxi.remainRequestIDs.size() < i1 + pr.maxNbStops ? taxi.remainRequestIDs
			int maxI2 = remainRequestIDs.size() < i1 + pr.maxNbStops ? remainRequestIDs
					.size() : i1 + pr.maxNbStops;
			for (int i2 = i1; i2 <= maxI2; i2++) {
				// for(int i2 = i1; i2 <= taxi.remainRequestIDs.size(); i2++){
				// for(int i2 = i1; i2 <= i1; i2++){
				// establish new sequence of request ids stored in nod
				int idx = -1;
				int pickup_idx = -1;
				int delivery_idx = -1;
				double profits = sim.getPeopleRevenue(expectDistancePeople);
				for (int k1 = 0; k1 < i1; k1++) {
					idx++;
					//nod[idx] = taxi.remainRequestIDs.get(k1);
					nod[idx] = remainRequestIDs.get(k1);
				}
				idx++;
				nod[idx] = pr.id;// insert pickup
				pickup_idx = idx;
				for (int k1 = 0; k1 < i2 - i1; k1++) {
					idx++;
					//nod[idx] = taxi.remainRequestIDs.get(i1 + k1);
					nod[idx] = remainRequestIDs.get(i1 + k1);
				}
				idx++;
				nod[idx] = -pr.id;// insert delivery
				delivery_idx = idx;
				//for (int k1 = i2; k1 < taxi.remainRequestIDs.size(); k1++) {
				for (int k1 = i2; k1 < remainRequestIDs.size(); k1++) {
					idx++;
					//nod[idx] = taxi.remainRequestIDs.get(k1);
					nod[idx] = remainRequestIDs.get(k1);
				}

				if (!sim.checkCapacity(taxi.parcelReqIDonBoard.size(), nod,
						Simulator.Qk))
					continue;

				// compute the distance of passenger service
				double distancePeople = 0;
				for (int k1 = pickup_idx; k1 < delivery_idx; k1++) {
					int u = sim.getLocationFromEncodedRequest(nod[k1]);
					int v = sim.getLocationFromEncodedRequest(nod[k1 + 1]);
					distancePeople = distancePeople
							+ sim.dijkstra.queryDistance(u, v);
				}
				if (distancePeople > pr.maxTravelDistance)
					continue;

				// check if travel distance of passenger on board exceeds
				// maximum distance allowed
				boolean ok = true;
				for (int k1 = 0; k1 < taxi.peopleReqIDonBoard.size(); k1++) {
					int pobReqID = taxi.peopleReqIDonBoard.get(k1);
					PeopleRequest pR = sim.mPeopleRequest.get(pobReqID);
					double d1 = sim.computeRemainTravelDistance(pobReqID, nod,
							nextStartPoint);
					Arc A = sim.map.getArc(taxi.lastPoint, nextStartPoint);
					double d2 = d1 + sim.accumulateDistance.get(pobReqID);
					if (A != null)
						d2 = d2 + A.w;
					if (d2 > pR.maxTravelDistance) {
						ok = false;
						break;
					}
				}
				if (!ok)
					continue;

				if (pickup_idx < delivery_idx - 1)// not direct delivery
					profits = profits
							- sim.getDiscount(expectDistancePeople,
									distancePeople);

				// evaluate the insertion

				double D = sim.computeFeasibleDistance(nextStartPoint,
						startTimePoint, nod);
				if (D > sim.dijkstra.infinity - 1)
					continue;// constraints are violated
				if (D + taxi.totalTravelDistance > sim.maxTravelDistance)
					continue;
				for (int k = 0; k < parkings.size(); k++) {
					int pk = parkings.get(k);

					// double D = computeFeasibleDistance(nextStartPoint,
					// startTimePoint, nod, pk);
					// if(D > dijkstra.infinity - 1) continue;// constraints are
					// violated
					int endPoint = sim
							.getLocationFromEncodedRequest(nod[nod.length - 1]);
					D = D + sim.dijkstra.queryDistance(endPoint, pk);
					double extraDistance = D - taxi.remainDistance;
					profits = profits - sim.getCostFuel(extraDistance);

					if (profits > maxProfits) {
						maxProfits = profits;
						// sel_pickup_index = i1;
						// sel_delivery_index = i2;
						// sel_pk = pk;
						ss = new ServiceSequence(nod, profits, pk, D);
						/*
						 * System.out.println(
						 * "SimulatorBookedRequest::computeBestPeopleInsertion, UPDATE ss = "
						 * + ss.profitEvaluation + ", sequence = " +
						 * ss.getSequence() + ", distance D = " + ss.distance);
						 * log.println(
						 * "SimulatorBookedRequest::computeBestPeopleInsertion, UPDATE ss = "
						 * + ss.profitEvaluation + ", sequence = " +
						 * ss.getSequence() + ", distance D = " + ss.distance);
						 */
					}
				}
			}
		}

		return ss;
	}

	public ServiceSequence computeProfitsPeopleInsertionFirst(Vehicle taxi,
			TimePointIndex tpi, PeopleRequest pr, ArrayList<Integer> remainRequestIDs) {
		int startIdx = 0;
		// int taxiID = 1;
		int nextStartPoint = tpi.point;// taxi.getNextStartPoint();
		int startTimePoint = tpi.timePoint;// taxi.getNextStartTimePoint();

		// if nextStartPoint is a location of a request, then startIdx must be 1
		//if (taxi.remainRequestIDs.size() > 0) {
		if (remainRequestIDs.size() > 0) {
			int frid = taxi.remainRequestIDs.get(0);
			frid = Math.abs(frid);
			boolean ok = false;
			PeopleRequest peopleReq = sim.mPeopleRequest.get(frid);
			if (peopleReq != null) {
				if (nextStartPoint == peopleReq.pickupLocationID
						|| nextStartPoint == peopleReq.deliveryLocationID)
					ok = true;
			}
			if (!ok) {
				ParcelRequest parcelReq = sim.mParcelRequest.get(frid);
				if (parcelReq != null) {
					if (nextStartPoint == parcelReq.pickupLocationID
							|| nextStartPoint == parcelReq.deliveryLocationID)
						ok = true;
				}
			}
			if (ok)
				startIdx = 1;
		}

		if (taxi.peopleReqIDonBoard.size() > 0) {
			// taxi is carrying a passenger
			int rid = taxi.peopleReqIDonBoard.get(0);
			//for (int i = 0; i < taxi.remainRequestIDs.size(); i++) {
			for (int i = 0; i < remainRequestIDs.size(); i++) {
				//if (taxi.remainRequestIDs.get(i) == -rid) {
				if (remainRequestIDs.get(i) == -rid) {
					startIdx = i + 1;
					break;
				}
			}
			if (taxi.ID == sim.debugTaxiID) {
				System.out
						.println("SimulatorBookedRequest::computeBestProfitsPeopleInsertion, "
								+ "request on board = "
								+ rid
								+ ", remainRequestIDs = "
								+ taxi.getRemainRequestID(remainRequestIDs)
								+ ", startIdx = "
								+ startIdx + ", new people quest = " + pr.id);
				log.println("SimulatorBookedRequest::computeBestProfitsPeopleInsertion, "
						+ "request on board = "
						+ rid
						+ ", remainRequestIDs = "
						+ taxi.getRemainRequestID(remainRequestIDs)
						+ ", startIdx = "
						+ startIdx + ", new people quest = " + pr.id);

			}
		}

		//int[] nod = new int[taxi.remainRequestIDs.size() + 2];
		int[] nod = new int[remainRequestIDs.size() + 2];
		// explore all possible position i1 (pickup), i2 (delivery) among
		// taxi.remainRequestIDs for inserting pr
		ArrayList<Integer> parkings = sim.collectAvailableParkings(taxi);
		// double minExtraDistance = dijkstra.infinity;
		// int sel_pickup_index = -1;
		// int sel_delivery_index = -1;
		// int sel_pk = -1;
		double maxProfits = -sim.dijkstra.infinity;
		ServiceSequence ss = null;
		double expectDistancePeople = sim.dijkstra.queryDistance(
				pr.pickupLocationID, pr.deliveryLocationID);
		//for (int i1 = startIdx; i1 <= taxi.remainRequestIDs.size(); i1++) {
		for (int i1 = startIdx; i1 <= startIdx; i1++) {	
			//int maxI2 = taxi.remainRequestIDs.size() < i1 + pr.maxNbStops ? taxi.remainRequestIDs
			//int maxI2 = remainRequestIDs.size() < i1 + pr.maxNbStops ? remainRequestIDs
					//.size() : i1 + pr.maxNbStops;
			for (int i2 = i1; i2 <= i1; i2++) {
				// for(int i2 = i1; i2 <= taxi.remainRequestIDs.size(); i2++){
				// for(int i2 = i1; i2 <= i1; i2++){
				// establish new sequence of request ids stored in nod
				int idx = -1;
				int pickup_idx = -1;
				int delivery_idx = -1;
				double profits = sim.getPeopleRevenue(expectDistancePeople);
				for (int k1 = 0; k1 < i1; k1++) {
					idx++;
					//nod[idx] = taxi.remainRequestIDs.get(k1);
					nod[idx] = remainRequestIDs.get(k1);
				}
				idx++;
				nod[idx] = pr.id;// insert pickup
				pickup_idx = idx;
				for (int k1 = 0; k1 < i2 - i1; k1++) {
					idx++;
					//nod[idx] = taxi.remainRequestIDs.get(i1 + k1);
					nod[idx] = remainRequestIDs.get(i1 + k1);
				}
				idx++;
				nod[idx] = -pr.id;// insert delivery
				delivery_idx = idx;
				//for (int k1 = i2; k1 < taxi.remainRequestIDs.size(); k1++) {
				for (int k1 = i2; k1 < remainRequestIDs.size(); k1++) {
					idx++;
					//nod[idx] = taxi.remainRequestIDs.get(k1);
					nod[idx] = remainRequestIDs.get(k1);
				}

				if (!sim.checkCapacity(taxi.parcelReqIDonBoard.size(), nod,
						Simulator.Qk))
					continue;

				// compute the distance of passenger service
				double distancePeople = 0;
				for (int k1 = pickup_idx; k1 < delivery_idx; k1++) {
					int u = sim.getLocationFromEncodedRequest(nod[k1]);
					int v = sim.getLocationFromEncodedRequest(nod[k1 + 1]);
					distancePeople = distancePeople
							+ sim.dijkstra.queryDistance(u, v);
				}
				if (distancePeople > pr.maxTravelDistance)
					continue;

				// check if travel distance of passenger on board exceeds
				// maximum distance allowed
				boolean ok = true;
				for (int k1 = 0; k1 < taxi.peopleReqIDonBoard.size(); k1++) {
					int pobReqID = taxi.peopleReqIDonBoard.get(k1);
					PeopleRequest pR = sim.mPeopleRequest.get(pobReqID);
					double d1 = sim.computeRemainTravelDistance(pobReqID, nod,
							nextStartPoint);
					Arc A = sim.map.getArc(taxi.lastPoint, nextStartPoint);
					double d2 = d1 + sim.accumulateDistance.get(pobReqID);
					if (A != null)
						d2 = d2 + A.w;
					if (d2 > pR.maxTravelDistance) {
						ok = false;
						break;
					}
				}
				if (!ok)
					continue;

				if (pickup_idx < delivery_idx - 1)// not direct delivery
					profits = profits
							- sim.getDiscount(expectDistancePeople,
									distancePeople);

				// evaluate the insertion

				double D = sim.computeFeasibleDistance(nextStartPoint,
						startTimePoint, nod);
				if (D > sim.dijkstra.infinity - 1)
					continue;// constraints are violated
				if (D + taxi.totalTravelDistance > sim.maxTravelDistance)
					continue;
				for (int k = 0; k < parkings.size(); k++) {
					int pk = parkings.get(k);

					// double D = computeFeasibleDistance(nextStartPoint,
					// startTimePoint, nod, pk);
					// if(D > dijkstra.infinity - 1) continue;// constraints are
					// violated
					int endPoint = sim
							.getLocationFromEncodedRequest(nod[nod.length - 1]);
					D = D + sim.dijkstra.queryDistance(endPoint, pk);
					double extraDistance = D - taxi.remainDistance;
					profits = profits - sim.getCostFuel(extraDistance);

					if (profits > maxProfits) {
						maxProfits = profits;
						// sel_pickup_index = i1;
						// sel_delivery_index = i2;
						// sel_pk = pk;
						ss = new ServiceSequence(nod, profits, pk, D);
						/*
						 * System.out.println(
						 * "SimulatorBookedRequest::computeBestPeopleInsertion, UPDATE ss = "
						 * + ss.profitEvaluation + ", sequence = " +
						 * ss.getSequence() + ", distance D = " + ss.distance);
						 * log.println(
						 * "SimulatorBookedRequest::computeBestPeopleInsertion, UPDATE ss = "
						 * + ss.profitEvaluation + ", sequence = " +
						 * ss.getSequence() + ", distance D = " + ss.distance);
						 */
					}
				}
			}
		}

		return ss;
	}
	public ServiceSequence computeParcelInsertionSequence(Vehicle taxi,
			TimePointIndex tpi, ParcelRequest pr, ArrayList<Integer> keptReq, ArrayList<Integer> remainRequestIDs) {
		int startIdx = 0;
		// int taxiID = 1;
		int nextStartPoint = tpi.point;// taxi.getNextStartPoint();
		int startTimePoint = tpi.timePoint;// taxi.getNextStartTimePoint();

		/*
		// if nextStartPoint is a location of a request, then startIdx must be 1
		//if (taxi.remainRequestIDs.size() > 0) {
		if (remainRequestIDs.size() > 0) {
			int frid = taxi.remainRequestIDs.get(0);
			frid = Math.abs(frid);
			boolean ok = false;
			PeopleRequest peopleReq = sim.mPeopleRequest.get(frid);
			if (peopleReq != null) {
				if (nextStartPoint == peopleReq.pickupLocationID
						|| nextStartPoint == peopleReq.deliveryLocationID)
					ok = true;
			}
			if (!ok) {
				ParcelRequest parcelReq = sim.mParcelRequest.get(frid);
				if (parcelReq != null) {
					if (nextStartPoint == parcelReq.pickupLocationID
							|| nextStartPoint == parcelReq.deliveryLocationID)
						ok = true;
				}
			}
			if (ok)
				startIdx = 1;
		}

		if (taxi.peopleReqIDonBoard.size() > 0) {
			// taxi is carrying a passenger
			int rid = taxi.peopleReqIDonBoard.get(0);
			//for (int i = 0; i < taxi.remainRequestIDs.size(); i++) {
			for (int i = 0; i < remainRequestIDs.size(); i++) {
				//if (taxi.remainRequestIDs.get(i) == -rid) {
				if (remainRequestIDs.get(i) == -rid) {
					startIdx = i + 1;
					break;
				}
			}
			if (taxi.ID == sim.debugTaxiID) {
				
			}
		}
		*/
		if(taxi.ID == sim.debugTaxiID){
			//String s = "";
			//for(int i = 0; i < remainRequestIDs.size(); i++)
				//s = s + remainRequestIDs.get(i) + ", ";
			log.println(name() + "::computeParcelInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + " tpi = " + tpi.toString() + 
					", keptReq = " + Utility.arr2String(keptReq) + ", remainrequestIDs = "
				+ Utility.arr2String(remainRequestIDs) + ", taxi.requestStatus = " + taxi.requestStatus());		
		}
		//int[] nod = new int[taxi.remainRequestIDs.size() + 2];
		//int[] nod = new int[remainRequestIDs.size() + 2];
		// explore all possible position i1 (pickup), i2 (delivery) among
		// taxi.remainRequestIDs for inserting pr
		ArrayList<Integer> parkings = sim.collectAvailableParkings(taxi);
		// double minExtraDistance = dijkstra.infinity;
		// int sel_pickup_index = -1;
		// int sel_delivery_index = -1;
		// int sel_pk = -1;
		//double maxProfits = -sim.dijkstra.infinity;
		ServiceSequence ss = null;
		//double expectDistancePeople = sim.dijkstra.queryDistance(
				//pr.pickupLocationID, pr.deliveryLocationID);
		
		int[] r = new int[remainRequestIDs.size() + 2];
		for(int i = 0; i < remainRequestIDs.size(); i++)
			r[i] = remainRequestIDs.get(i);
		r[r.length-2] = pr.id;
		r[r.length-1] = -pr.id;
		
		/*
		PermutationGenerator P = new PermutationGenerator(remainRequestIDs.size() + 2);
		P.generate();
		double minD = 1000000000;
		int[] sel_nod = null;
		for(int k = 0; k < P.size(); k++){
			int[] p = P.get(k);
			//nod = new int[p.length];
			for(int i = 0; i < p.length; i++)
				nod[i] = r[p[i]];
			
			if(!sim.checkLegalPermutationServiceSequence(nod, nod.length)) continue;
			
			if(taxi.ID == sim.debugTaxiID){
				log.print(name() + "::computeParcelInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + ", r = ");
				for(int i = 0; i < p.length; i++)
					log.print(r[i] + ",");
				log.println();
				log.print(name() + "::computeParcelInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + ", perm = "); 
				for(int i = 0; i < p.length; i++)
					log.print(p[i] + ",");
				log.println();
				log.print(name() + "::computeParcelInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + ", nod = "); 
				for(int i = 0; i < p.length; i++)
					log.print(nod[i] + ",");
				log.println();
				log.print(name() + "::computeParcelInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + ", sel_nod = "); 
				if(sel_nod ==  null) log.print("NULL"); else
				for(int i = 0; i < p.length; i++)
					log.print(sel_nod[i] + ",");
				log.println();
			}
			
			//if(!sim.checkLegalPickupDeliverySequence(nod)) continue;
			//if(!sim.checkCapacity(taxi.parcelReqIDonBoard.size(), nod,	Simulator.Qk)) continue;
			//if(!sim.checkMaxDistanceOfPeopleOnBoard(taxi, nod, nextStartPoint)) continue;
			//if(!sim.checkMaxDistanceOfPeopleRequests(nod)) continue;
			//if(!sim.checkNotPeoplePeopleSharing(taxi, nod)) continue;
			//if(!sim.checkMaxStopPeopleOnBoard(taxi, nod)) continue;
			//if(!sim.checkMaxStopPeopleRequest(nod)) continue;
			
			double D = sim.estimateDistance(nextStartPoint, nod);
			if(D < minD){
				minD = D;
				sel_nod = new int[nod.length];
				for(int ii = 0; ii < nod.length; ii++) sel_nod[ii] = nod[ii];
				
				if(taxi.ID == sim.debugTaxiID){
					log.print(name() + "::computeParcelInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + ", UPDATE sel_nod = "); 
					if(sel_nod ==  null) log.print("NULL"); else
					for(int i = 0; i < p.length; i++)
						log.print(sel_nod[i] + ",");
					log.println(" minD = " + minD);
				}
			}
		}
		*/
		int[] t_sel_nod = seqOptimizer.computeShortestSequence(taxi, tpi, keptReq, r, sim.maxTimeOneParcelInsertion);
		if(t_sel_nod == null){
			log.println(name() + "::computeParcelInsertionSequence taxi = " + taxi.ID + ", sel_nod = NULL NO SOLUTION?? " + 
		", taxi.requestStatus = " + taxi.requestStatus());
			return null;
		}
		int[] sel_nod = new int[t_sel_nod.length - keptReq.size()];
		//int idx = -1;
		//System.out.println(name() + "::computeParcelInsertionSequence, t_sel_nod = " + Utility.arr2String(t_sel_nod) + 
				//", remainrequestIDs = " + Utility.arr2String(remainRequestIDs));
		for(int i = keptReq.size(); i < t_sel_nod.length; i++){
			sel_nod[i-keptReq.size()] = t_sel_nod[i];
		}
		/*
		int[] t_sel_nod = seqOptimizer.computeShortestSequence(taxi, nextStartPoint, keptReq, r);
		if(t_sel_nod == null){
			log.println(name() + "::computeParcelInsertionSequence taxi = " + taxi.ID + ", sel_nod = NULL NO SOLUTION?? " + 
		", taxi.requestStatus = " + taxi.requestStatus());
			return null;
		}
		int[] sel_nod = new int[keptReq.size() + t_sel_nod.length];
		int idx = -1;
		for(int i = 0; i < keptReq.size(); i++){
			idx++;
			sel_nod[idx] = keptReq.get(i);
		}
		for(int i = 0; i < t_sel_nod.length; i++){
			idx++;
			sel_nod[idx] = t_sel_nod[i];
		}
		*/
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

	public ServiceSequence computePeopleInsertionSequence(Vehicle taxi,
			TimePointIndex tpi, PeopleRequest pr, ArrayList<Integer> keptReq, ArrayList<Integer> remainRequestIDs) {
		int startIdx = 0;
		// int taxiID = 1;
		int nextStartPoint = tpi.point;// taxi.getNextStartPoint();
		int startTimePoint = tpi.timePoint;// taxi.getNextStartTimePoint();

		/*
		// if nextStartPoint is a location of a request, then startIdx must be 1
		//if (taxi.remainRequestIDs.size() > 0) {
		if (remainRequestIDs.size() > 0) {
			int frid = taxi.remainRequestIDs.get(0);
			frid = Math.abs(frid);
			boolean ok = false;
			PeopleRequest peopleReq = sim.mPeopleRequest.get(frid);
			if (peopleReq != null) {
				if (nextStartPoint == peopleReq.pickupLocationID
						|| nextStartPoint == peopleReq.deliveryLocationID)
					ok = true;
			}
			if (!ok) {
				ParcelRequest parcelReq = sim.mParcelRequest.get(frid);
				if (parcelReq != null) {
					if (nextStartPoint == parcelReq.pickupLocationID
							|| nextStartPoint == parcelReq.deliveryLocationID)
						ok = true;
				}
			}
			if (ok)
				startIdx = 1;
		}

		if (taxi.peopleReqIDonBoard.size() > 0) {
			// taxi is carrying a passenger
			int rid = taxi.peopleReqIDonBoard.get(0);
			//for (int i = 0; i < taxi.remainRequestIDs.size(); i++) {
			for (int i = 0; i < remainRequestIDs.size(); i++) {
				//if (taxi.remainRequestIDs.get(i) == -rid) {
				if (remainRequestIDs.get(i) == -rid) {
					startIdx = i + 1;
					break;
				}
			}
			if (taxi.ID == sim.debugTaxiID) {
				
			}
		}
		*/
		if(taxi.ID == sim.debugTaxiID){
			//String s = "";
			//for(int i = 0; i < remainRequestIDs.size(); i++)
				//s = s + remainRequestIDs.get(i) + ", ";
			log.println(name() + "::computePeopleInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + " tpi = " + tpi.toString() + 
					", keptReq = " + Utility.arr2String(keptReq) + ", remainrequestIDs = "
				+ Utility.arr2String(remainRequestIDs) + ", taxi.requestStatus = " + taxi.requestStatus());		
		}
		//int[] nod = new int[taxi.remainRequestIDs.size() + 2];
		//int[] nod = new int[remainRequestIDs.size() + 2];
		// explore all possible position i1 (pickup), i2 (delivery) among
		// taxi.remainRequestIDs for inserting pr
		ArrayList<Integer> parkings = sim.collectAvailableParkings(taxi);
		// double minExtraDistance = dijkstra.infinity;
		// int sel_pickup_index = -1;
		// int sel_delivery_index = -1;
		// int sel_pk = -1;
		//double maxProfits = -sim.dijkstra.infinity;
		ServiceSequence ss = null;
		//double expectDistancePeople = sim.dijkstra.queryDistance(
				//pr.pickupLocationID, pr.deliveryLocationID);
		
		int[] r = new int[keptReq.size() + 2];
		for(int i = 0; i < keptReq.size(); i++)
			r[i] = keptReq.get(i);
		r[r.length-2] = pr.id;
		r[r.length-1] = -pr.id;
		
		/*
		PermutationGenerator P = new PermutationGenerator(remainRequestIDs.size() + 2);
		P.generate();
		double minD = 1000000000;
		int[] sel_nod = null;
		for(int k = 0; k < P.size(); k++){
			int[] p = P.get(k);
			//nod = new int[p.length];
			for(int i = 0; i < p.length; i++)
				nod[i] = r[p[i]];
			
			if(!sim.checkLegalPermutationServiceSequence(nod, nod.length)) continue;
			
			if(taxi.ID == sim.debugTaxiID){
				log.print(name() + "::computeParcelInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + ", r = ");
				for(int i = 0; i < p.length; i++)
					log.print(r[i] + ",");
				log.println();
				log.print(name() + "::computeParcelInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + ", perm = "); 
				for(int i = 0; i < p.length; i++)
					log.print(p[i] + ",");
				log.println();
				log.print(name() + "::computeParcelInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + ", nod = "); 
				for(int i = 0; i < p.length; i++)
					log.print(nod[i] + ",");
				log.println();
				log.print(name() + "::computeParcelInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + ", sel_nod = "); 
				if(sel_nod ==  null) log.print("NULL"); else
				for(int i = 0; i < p.length; i++)
					log.print(sel_nod[i] + ",");
				log.println();
			}
			
			//if(!sim.checkLegalPickupDeliverySequence(nod)) continue;
			//if(!sim.checkCapacity(taxi.parcelReqIDonBoard.size(), nod,	Simulator.Qk)) continue;
			//if(!sim.checkMaxDistanceOfPeopleOnBoard(taxi, nod, nextStartPoint)) continue;
			//if(!sim.checkMaxDistanceOfPeopleRequests(nod)) continue;
			//if(!sim.checkNotPeoplePeopleSharing(taxi, nod)) continue;
			//if(!sim.checkMaxStopPeopleOnBoard(taxi, nod)) continue;
			//if(!sim.checkMaxStopPeopleRequest(nod)) continue;
			
			double D = sim.estimateDistance(nextStartPoint, nod);
			if(D < minD){
				minD = D;
				sel_nod = new int[nod.length];
				for(int ii = 0; ii < nod.length; ii++) sel_nod[ii] = nod[ii];
				
				if(taxi.ID == sim.debugTaxiID){
					log.print(name() + "::computeParcelInsertionSequence, taxi " + taxi.ID + ", pr = " + pr.id + ", UPDATE sel_nod = "); 
					if(sel_nod ==  null) log.print("NULL"); else
					for(int i = 0; i < p.length; i++)
						log.print(sel_nod[i] + ",");
					log.println(" minD = " + minD);
				}
			}
		}
		*/
		int[] t_sel_nod = seqOptimizer.computeShortestSequence(taxi, tpi, r, remainRequestIDs, sim.maxTimeOnePeopleInsertion);
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
		/*
		int[] t_sel_nod = seqOptimizer.computeShortestSequence(taxi, nextStartPoint, keptReq, r);
		if(t_sel_nod == null){
			log.println(name() + "::computeParcelInsertionSequence taxi = " + taxi.ID + ", sel_nod = NULL NO SOLUTION?? " + 
		", taxi.requestStatus = " + taxi.requestStatus());
			return null;
		}
		int[] sel_nod = new int[keptReq.size() + t_sel_nod.length];
		int idx = -1;
		for(int i = 0; i < keptReq.size(); i++){
			idx++;
			sel_nod[idx] = keptReq.get(i);
		}
		for(int i = 0; i < t_sel_nod.length; i++){
			idx++;
			sel_nod[idx] = t_sel_nod[i];
		}
		*/
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

	public void processPeopleRequest(PeopleRequest pr) {
		// int taxiID = 727;
		ItineraryServiceSequence sel_IS = null;
		TimePointIndex sel_tpi = null;
		for (int k = 0; k < sim.vehicles.size(); k++) {
			Vehicle taxi = sim.vehicles.get(k);
			if (taxi.totalTravelDistance > sim.maxTravelDistance)
				continue;

			if (taxi.status == VehicleStatus.STOP_WORK)
				continue;
			if (taxi.remainRequestIDs.size() > sim.maxPendingStops)
				continue;

			TimePointIndex tpi = taxi.getNextTimePointIndex(
					taxi.lastIndexPoint, sim.T.currentTimePoint,
					Simulator.TimePointDuration);

			if (taxi.ID == sim.debugTaxiID) {
				log.println(name() + "::processPeopleRequest(" + pr.id
						+ ") DEBUG TAXI = " + taxi.ID
						+ ", nextStartTimePoint = " + tpi.timePoint
						+ ", nextStartPointIndex = " + tpi.indexPoint
						+ ", nextStartPoint = " + tpi.point);

			}
			ItineraryServiceSequence IS = computeBestProfitsItineraryPeopleInsertion(
					taxi, tpi, pr, taxi.remainRequestIDs);

			if (IS == null)
				continue;
			if (sel_IS == null) {
				sel_IS = IS;
				sel_tpi = tpi;
			} else {
				if (sel_IS.ss.profitEvaluation < IS.ss.profitEvaluation) {
					sel_IS = IS;
					sel_tpi = tpi;
					System.out.println(name()
							+ "::processPeopleRequest, UPDATE sel_taxi "
							+ IS.taxi.ID + ", sel_ss.profits = "
							+ sel_IS.ss.profitEvaluation);
				}
			}
		}

		if (sel_IS == null) {
			sim.nbPeopleRejects++;
			System.out.println(name() + "::processPeopleRequest --> request "
					+ pr.id + " is REJECTED, nbPeopleRejected = "
					+ sim.nbPeopleRejects + "/" + sim.allPeopleRequests.size());
			log.println(name() + "::processPeopleRequest --> request " + pr.id
					+ " is REJECTED");
			return;
		}
		Vehicle sel_taxi = sel_IS.taxi;

		if (sel_taxi.ID == sim.debugTaxiID) {
			log.println(name() + "::processPeopleRequest --> sel_tpi = "
					+ sel_tpi.toString());
		}
		ServiceSequence sel_ss = sel_IS.ss;
		int nextStartTimePoint = sel_tpi.timePoint;// sel_taxi.getNextStartTimePoint();
		int fromPoint = sel_tpi.point;// sel_taxi.getNextStartPoint();
		int fromIndex = sel_tpi.indexPoint;// sel_taxi.getNextStartPointIndex();
		System.out.println(name() + "::processPeopleRequest, sequence = "
				+ sel_ss.getSequence() + ", maxProfits = "
				+ sel_ss.profitEvaluation + ", sel_taxi = " + sel_taxi.ID
				+ ", nextStartTimePoint = " + nextStartTimePoint
				+ ", fromIndex = " + fromIndex + ", fromPoint = " + fromPoint);

		if (sel_taxi.ID == sim.debugTaxiID)
			log.println(name() + "::processPeopleRequest(" + pr.id + ") AT "
					+ sim.T.currentTimePoint + " engage taxi " + sel_taxi.ID
					+ " sequence = " + sel_ss.getSequence() + ", maxProfits = "
					+ sel_ss.profitEvaluation + ", sel_taxi = " + sel_taxi.ID
					+ ", nextStartTimePoint = " + nextStartTimePoint
					+ ", fromIndex = " + fromIndex + ", fromPoint = "
					+ fromPoint);

		// if(taxiID ==
		// sel_taxi.ID)log.println("SimulatorBookedRequest::processPeopleRequest, sequence = "
		// + sel_ss.getSequence() +
		// ", maxProfits = " + sel_ss.profitEvaluation +
		// ", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = " +
		// nextStartTimePoint +
		// ", fromIndex = " + fromIndex + ", fromPoint = " + fromPoint);
		// sim.admitNewItinerary(sel_taxi, nextStartTimePoint, fromIndex,
		// fromPoint, sel_ss);

		sim.admitNewItinerary(sel_IS.taxi, nextStartTimePoint, fromIndex,
				fromPoint, sel_IS.I, sel_IS.ss);

		if (sel_taxi.ID == sim.debugTaxiID)
			log.println(name() + "::processPeopleRequest(" + pr.id + ") AT "
					+ sim.T.currentTimePoint + " insert people to taxi "
					+ sel_taxi.ID + ", peopleOnBoards = "
					+ sel_taxi.getPeopleOnBoards() + ", peopleOnBoards = "
					+ sel_taxi.getPeopleOnBoards() + ", remainRequestIDs = "
					+ sel_taxi.getRemainRequestID());

		sim.nbPeopleWaitBoarding++;
		System.out.println(name()
				+ "::processPeopleRequest, nbPeopleWaitBoarding = "
				+ sim.nbPeopleWaitBoarding + ", nbPeopleComplete = "
				+ sim.nbPeopleComplete + ", nbPeopleOnBoard = "
				+ sim.nbPeopleOnBoard + ", nbPeopleRejects = "
				+ sim.nbPeopleRejects + ", total PeopleRequests = "
				+ sim.allPeopleRequests.size());

	}

	public void processPeopleRequestOld(PeopleRequest pr) {
		System.out.println("NOT USED");
		assert(false);
		// TODO Auto-generated method stub
		ServiceSequence sel_ss = null;
		Vehicle sel_taxi = null;
		// int taxiID = 1;
		for (int k = 0; k < sim.vehicles.size(); k++) {
			Vehicle taxi = sim.vehicles.get(k);
			if (taxi.remainRequestIDs.size() > sim.maxPendingStops)
				continue;
			TimePointIndex next_tpi = taxi.getNextTimePointIndex(
					taxi.lastIndexPoint, sim.T.currentTimePoint,
					Simulator.TimePointDuration);
			ServiceSequence ss = computeBestProfitsPeopleInsertion(taxi,
					next_tpi, pr,null);
			if (ss != null) {
				// System.out.println("SimulatorBookedRequest::processPeopleRequest, taxi "
				// + taxi.ID +
				// ", profits = " + ss.profitEvaluation);
				if (sel_ss == null) {
					sel_ss = ss;
					sel_taxi = taxi;
				} else {
					if (sel_ss.profitEvaluation < ss.profitEvaluation) {
						sel_ss = ss;
						sel_taxi = taxi;
						System.out
								.println("SimulatorBookedRequest::processPeopleRequest, UPDATE sel_taxi "
										+ sel_taxi.ID
										+ ", sel_ss.profits = "
										+ sel_ss.profitEvaluation);
						if (sim.debugTaxiID == sel_taxi.ID)
							log.println("SimulatorBookedRequest::processPeopleRequest, UPDATE sel_taxi "
									+ sel_taxi.ID
									+ ", sel_ss.profits = "
									+ sel_ss.profitEvaluation);
					}
				}
			}
		}
		if (sel_taxi == null) {
			sim.nbPeopleRejects++;
			System.out
					.println("SimulatorBookedRequest::processPeopleRequest --> request "
							+ pr.id
							+ " is REJECTED, nbPeopleRejected = "
							+ sim.nbPeopleRejects
							+ "/"
							+ sim.allPeopleRequests.size());
			log.println("SimulatorBookedRequest::processPeopleRequest --> request "
					+ pr.id + " is REJECTED");
			return;
		}
		int nextStartTimePoint = sel_taxi.getNextStartTimePoint();
		int fromPoint = sel_taxi.getNextStartPoint();
		int fromIndex = sel_taxi.getNextStartPointIndex();
		System.out
				.println("SimulatorBookedRequest::processPeopleRequest, sequence = "
						+ sel_ss.getSequence()
						+ ", maxProfits = "
						+ sel_ss.profitEvaluation
						+ ", sel_taxi = "
						+ sel_taxi.ID
						+ ", nextStartTimePoint = "
						+ nextStartTimePoint
						+ ", fromIndex = "
						+ fromIndex
						+ ", fromPoint = " + fromPoint);
		if (sim.debugTaxiID == sel_taxi.ID)
			log.println("SimulatorBookedRequest::processPeopleRequest, sequence = "
					+ sel_ss.getSequence()
					+ ", maxProfits = "
					+ sel_ss.profitEvaluation
					+ ", sel_taxi = "
					+ sel_taxi.ID
					+ ", nextStartTimePoint = "
					+ nextStartTimePoint
					+ ", fromIndex = "
					+ fromIndex
					+ ", fromPoint = "
					+ fromPoint);
		sim.admitNewItinerary(sel_taxi, nextStartTimePoint, fromIndex,
				fromPoint, sel_ss);

		sim.nbPeopleWaitBoarding++;
		System.out
				.println("SimulatorBookedRequest::processPeopleRequest, nbPeopleWaitBoarding = "
						+ sim.nbPeopleWaitBoarding
						+ ", nbPeopleComplete = "
						+ sim.nbPeopleComplete
						+ ", nbPeopleOnBoard = "
						+ sim.nbPeopleOnBoard
						+ ", nbPeopleRejects = "
						+ sim.nbPeopleRejects
						+ ", total PeopleRequests = "
						+ sim.allPeopleRequests.size());
	}

	/*
	 * public void processParcelRequestNew(ParcelRequest pr){
	 * ItineraryServiceSequence sel_IS = null; for(int k = 0; k <
	 * sim.vehicles.size(); k++){ Vehicle taxi = sim.vehicles.get(k);
	 * if(taxi.remainRequestIDs.size() > sim.maxPendingStops) continue;
	 * ItineraryServiceSequence IS =
	 * computeBestProfitsItineraryParcelInsertion(taxi,
	 * taxi.getNextStartTimePoint(), taxi.getNextStartPointIndex(),
	 * taxi.getNextStartPoint(), pr); if(IS == null) continue; if(sel_IS ==
	 * null){ sel_IS = IS; }else{ if(sel_IS.ss.profitEvaluation <
	 * IS.ss.profitEvaluation){ sel_IS = IS; System.out.println(name() +
	 * "::processParcelRequest, UPDATE sel_taxi " + IS.taxi.ID +
	 * ", sel_ss.profits = " + sel_IS.ss.profitEvaluation); } } }
	 * 
	 * if(sel_IS == null){ sim.nbPeopleRejects++; System.out.println(name() +
	 * "::processParcelRequest --> request " + pr.id +
	 * " is REJECTED, nbParcelRejected = " + sim.nbParcelRejects + "/" +
	 * sim.allParcelRequests.size());
	 * log.println("SimulatorBookedRequest::processParcelRequest --> request " +
	 * pr.id + " is REJECTED"); return; } Vehicle sel_taxi = sel_IS.taxi;
	 * ServiceSequence sel_ss = sel_IS.ss; int nextStartTimePoint =
	 * sel_taxi.getNextStartTimePoint(); int fromPoint =
	 * sel_taxi.getNextStartPoint(); int fromIndex =
	 * sel_taxi.getNextStartPointIndex(); System.out.println(
	 * "SimulatorBookedRequest::processParcelRequest, sequence = " +
	 * sel_ss.getSequence() + ", maxProfits = " + sel_ss.profitEvaluation +
	 * ", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = " +
	 * nextStartTimePoint + ", fromIndex = " + fromIndex + ", fromPoint = " +
	 * fromPoint); //if(taxiID == sel_taxi.ID)log.println(
	 * "SimulatorBookedRequest::processPeopleRequest, sequence = " +
	 * sel_ss.getSequence() + // ", maxProfits = " + sel_ss.profitEvaluation +
	 * //", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = " +
	 * nextStartTimePoint + //", fromIndex = " + fromIndex + ", fromPoint = " +
	 * fromPoint); //sim.admitNewItinerary(sel_taxi, nextStartTimePoint,
	 * fromIndex, fromPoint, sel_ss);
	 * 
	 * sim.admitNewItinerary(sel_IS.taxi, nextStartTimePoint, fromIndex,
	 * fromPoint, sel_IS.I, sel_IS.ss); sim.nbPeopleWaitBoarding++;
	 * System.out.println(name() +
	 * "::processParcelRequest, nbParcelWaitBoarding = " +
	 * sim.nbParcelWaitBoarding + ", nbParcelComplete = " + sim.nbParcelComplete
	 * + ", nbParcelOnBoard = " + sim.nbParcelOnBoard + ", nbParcelRejects = "+
	 * sim.nbParcelRejects + ", total ParcelRequests = " +
	 * sim.allParcelRequests.size());
	 * 
	 * }
	 */
	
	public TaxiTimePointIndex findTaxiForParcelInsertion(ParcelRequest pr){
		double minDis = 1000000000;
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
			if(ttpi.estimation <  minDis){
				minDis = ttpi.estimation;
				sel_ttpi = ttpi;
			}
		}
		
		return sel_ttpi;
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
			sim.log.close();
			System.exit(-1);
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
	public void insertPeopleRequest(PeopleRequest pr, Vehicle taxi, TimePointIndex tpi, ArrayList<Integer> keptReq, 
			ArrayList<Integer> remainRequestIDs){
		ItineraryServiceSequence IS = computeItineraryPeopleInsertion(taxi,tpi, pr, keptReq, remainRequestIDs);
		if(IS == null){
			System.out.println(name() + "::insertPeopleRequest, pr = " + pr.id + " IS = null");
			sim.nbParcelRejects++;
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
	
	public void processParcelRequests(ArrayList<ParcelRequest> parReq) {
		//System.exit(-1);
		System.out.println(name() + "::processParcelRequests(parReq.sz = " + parReq.size() + ")");
		for(int i = 0; i < parReq.size(); i++){
			//System.out.println("AAAAAAAAAAAAAAAAAAAAAA");
			ParcelRequest pr = parReq.get(i);
			TaxiTimePointIndex ttpi = findTaxiForParcelInsertion(pr);
			System.out.println(name() + "::processParcelRequests(pr = " + pr.id + " --> found taxi " + ttpi.taxi.ID + ")");
			insertParcelRequest(pr,ttpi.taxi,ttpi.tpi,ttpi.keptRequestIDs, ttpi.remainRequestIDs);
			System.out.println(name() + "::procesParcelRequests, sim.status = " + sim.getAcceptRejectStatus());
		}
	}
	
	//public void processParcelRequests(ArrayList<ParcelRequest> parReq) {
		//System.out.println(name() + "::processParcelRequests(parreq.sz = " + parReq.size() + "), -> process each parcel request");
	//}
	
	
	private int[] partition(HashSet[] domain) {
		return sim.allDiff.solve(domain);
	}

	public void insertFirstDirectPeopleRequest(PeopleRequest pr, Vehicle taxi,
			TimePointIndex tpi, ArrayList<Integer> remainRequestIDs) {
		// insert pr into first location from tpi.indexPoint
		// people service is direct, no sharing
		ItineraryServiceSequence IS = computeProfitsItineraryPeopleInsertionFirst(taxi,tpi, pr, remainRequestIDs);
		if(IS == null){
			System.out.println(name() + "::insertFirstDirectPeopleRequest, pr = " + pr.id + " IS = null");
			sim.nbPeopleRejects++;
			System.out.println(name() + "::insertFirstPeopleRequest --> request "
					+ pr.id + " is REJECTED due to sel_IS = null, nbPeopleRejected = "
					+ sim.nbPeopleRejects + "/" + sim.allPeopleRequests.size());
			log.println(name() + "::insertFirstPeopleRequest --> request " + pr.id
					+ " is REJECTED");
		}else{
			System.out.println(name() + "::insertFirstDirectPeopleRequest, pr = " + pr.id + " IS = NOT null");
			
			if(taxi.ID == sim.debugTaxiID){
				String Is = "NULL";
				if(taxi.currentItinerary != null) Is = taxi.currentItinerary.toString();
				log.println(name() + "::insertFirstDirectPeopleRequest, before admit itinerary, currentItinerary = " + Is);
			}
			sim.admitNewItinerary(taxi, tpi.timePoint, tpi.indexPoint, tpi.point, IS.I, IS.ss);
			if(taxi.ID == sim.debugTaxiID){
				String Is = "NULL";
				if(taxi.currentItinerary != null) Is = taxi.currentItinerary.toString();
				log.println(name() + "::insertFirstDirectPeopleRequest, after admit itinerary, currentItinerary = " + Is);
			}
			if(taxi.ID == sim.debugTaxiID){
				log.println(name() + "::insertFirstDirectPeopleRequest, taxi " + taxi.ID + ", pr = " + pr.id + ", request status = " + taxi.requestStatus());
			}
			sim.nbPeopleWaitBoarding++;
			System.out.println(name()
					+ "::processPeopleRequest, nbPeopleWaitBoarding = "
					+ sim.nbPeopleWaitBoarding + ", nbPeopleComplete = "
					+ sim.nbPeopleComplete + ", nbPeopleOnBoard = "
					+ sim.nbPeopleOnBoard + ", nbPeopleRejects = "
					+ sim.nbPeopleRejects + ", total PeopleRequests = "
					+ sim.allPeopleRequests.size());
			ErrorMSG err = sim.checkServiceSequence(taxi, tpi);
			if(err.err != ErrorType.NO_ERROR){
				sim.log.println(name() + "::insertFirstDirectPeopleRequest, taxi = " + taxi.ID + ", PeopleRequest pr = " + 
			pr.id + ", FINAL checkServiceSequence FIALED, " + taxi.requestStatus());
			}
		}
	}
	public void insertGreedyPeopleRequest(PeopleRequest pr, Vehicle taxi,
			TimePointIndex tpi, ArrayList<Integer> remainRequestIDs) {
		ItineraryServiceSequence sel_IS = null;
		TimePointIndex sel_tpi = null;
		System.out.println(name() + "::insertGreedyPeopleRequest, pr = " + pr.id);
		ItineraryServiceSequence IS = computeBestProfitsItineraryPeopleInsertion(
				taxi, tpi, pr, remainRequestIDs);
		if(IS == null) 
			System.out.println(name() + "::insertGreedyPeopleRequest, pr = " + pr.id + " IS = null");
		else
			System.out.println(name() + "::insertGreedyPeopleRequest, pr = " + pr.id + " IS NOT null");
		
		if (IS != null) {
			if (sel_IS == null) {
				sel_IS = IS;
				sel_tpi = tpi;
				
				System.out.println(name()
						+ "::processPeopleRequest, sel_IS == null --> UPDATE sel_taxi "
						+ IS.taxi.ID + ", sel_ss.profits = "
						+ sel_IS.ss.profitEvaluation);
			} else {
				if (sel_IS.ss.profitEvaluation < IS.ss.profitEvaluation) {
					sel_IS = IS;
					sel_tpi = tpi;
					System.out.println(name()
							+ "::processPeopleRequest, UPDATE sel_taxi "
							+ IS.taxi.ID + ", sel_ss.profits = "
							+ sel_IS.ss.profitEvaluation);
				}
			}
		}

		if (sel_IS == null) {
			sim.nbPeopleRejects++;
			System.out.println(name() + "::insertGreedyPeopleRequest --> request "
					+ pr.id + " is REJECTED due to sel_IS = null, nbPeopleRejected = "
					+ sim.nbPeopleRejects + "/" + sim.allPeopleRequests.size());
			log.println(name() + "::insertGreedyPeopleRequest --> request " + pr.id
					+ " is REJECTED");
			return;
		}
		Vehicle sel_taxi = sel_IS.taxi;

		if (sel_taxi.ID == sim.debugTaxiID) {
			log.println(name() + "::insertGreedyPeopleRequest --> sel_taxi = " + sel_taxi.ID + " sel_tpi = "
					+ sel_tpi.toString());
		}
		ServiceSequence sel_ss = sel_IS.ss;
		int nextStartTimePoint = sel_tpi.timePoint;// sel_taxi.getNextStartTimePoint();
		int fromPoint = sel_tpi.point;// sel_taxi.getNextStartPoint();
		int fromIndex = sel_tpi.indexPoint;// sel_taxi.getNextStartPointIndex();
		System.out.println(name() + "::insertGreedyPeopleRequest, sequence = "
				+ sel_ss.getSequence() + ", maxProfits = "
				+ sel_ss.profitEvaluation + ", sel_taxi = " + sel_taxi.ID
				+ ", nextStartTimePoint = " + nextStartTimePoint
				+ ", fromIndex = " + fromIndex + ", fromPoint = " + fromPoint);

		if (sel_taxi.ID == sim.debugTaxiID)
			log.println(name() + "::insertGreedyPeopleRequest(" + pr.id + ") AT "
					+ sim.T.currentTimePoint + " engage taxi " + sel_taxi.ID
					+ " sequence = " + sel_ss.getSequence() + ", maxProfits = "
					+ sel_ss.profitEvaluation + ", sel_taxi = " + sel_taxi.ID
					+ ", nextStartTimePoint = " + nextStartTimePoint
					+ ", fromIndex = " + fromIndex + ", fromPoint = "
					+ fromPoint);

		// if(taxiID ==
		// sel_taxi.ID)log.println("SimulatorBookedRequest::processPeopleRequest, sequence = "
		// + sel_ss.getSequence() +
		// ", maxProfits = " + sel_ss.profitEvaluation +
		// ", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = " +
		// nextStartTimePoint +
		// ", fromIndex = " + fromIndex + ", fromPoint = " + fromPoint);
		// sim.admitNewItinerary(sel_taxi, nextStartTimePoint, fromIndex,
		// fromPoint, sel_ss);

		if (sel_taxi.ID == sim.debugTaxiID){
			String I = "NULL";
			if(sel_taxi.currentItinerary != null) I = sel_taxi.currentItinerary.toString();
			log.println(name() + "::insertGreedyPeopleRequest START AdmitItinerary, pr = " + pr.id + " taxi = "+ 
		sel_taxi.ID + ", sel_tpi = " + sel_tpi.toString() + ", ss = " + sel_IS.ss.getSequence() + ", currentItinerary = " + I);
		}
		
		sim.admitNewItinerary(sel_IS.taxi, nextStartTimePoint, fromIndex,
				fromPoint, sel_IS.I, sel_IS.ss);

		
		if (sel_taxi.ID == sim.debugTaxiID)
			log.println(name() + "::insertGreedyPeopleRequest(" + pr.id + ") AT "
					+ sim.T.currentTimePoint + " insert people to taxi "
					+ sel_taxi.ID + ", peopleOnBoards = "
					+ sel_taxi.getPeopleOnBoards() + ", peopleOnBoards = "
					+ sel_taxi.getPeopleOnBoards() + ", remainRequestIDs = "
					+ sel_taxi.getRemainRequestID());

		sim.nbPeopleWaitBoarding++;
		System.out.println(name()
				+ "::processPeopleRequest, nbPeopleWaitBoarding = "
				+ sim.nbPeopleWaitBoarding + ", nbPeopleComplete = "
				+ sim.nbPeopleComplete + ", nbPeopleOnBoard = "
				+ sim.nbPeopleOnBoard + ", nbPeopleRejects = "
				+ sim.nbPeopleRejects + ", total PeopleRequests = "
				+ sim.allPeopleRequests.size());

	}

	public void processPeopleRequests(ArrayList<PeopleRequest> peoReq) {
		System.out.println(name() + "::processPeopleRequests(peoReq.sz = " + peoReq.size() + ")");
		HashMap<PeopleRequest, ArrayList<TaxiTimePointIndex>> D = new HashMap<PeopleRequest, ArrayList<TaxiTimePointIndex>>();
		HashMap<Vehicle, TaxiTimePointIndex> mTaxi2TimePointIndex = new HashMap<Vehicle, TaxiTimePointIndex>();

		for (int i = 0; i < peoReq.size(); i++) {
			PeopleRequest pr = peoReq.get(i);
			ArrayList<TaxiTimePointIndex> L = getAvailableTaxis(pr);
			D.put(pr, L);

			for (int j = 0; j < L.size(); j++) {
				TaxiTimePointIndex ttpi = L.get(j);
				mTaxi2TimePointIndex.put(ttpi.taxi, ttpi);
			}
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
				insertPeopleRequest(pr, ttpi.taxi, ttpi.tpi, ttpi.keptRequestIDs, ttpi.remainRequestIDs);
				
				System.out.println(name() + "::processPeopleRequests call to insertGreedyPeopleRequest(pr = " + pr.id + ", taxi = " + taxi.ID + ") finished");
				//log.println(name() + "::processPeopleRequests call to insertGreedyPeopleRequest(pr = " + pr.id + ", taxi = " + taxi.ID + ") finished");
				
			}
		}
		System.out.println(name() + "::procesPeopleRequests, sim.status = " + sim.getAcceptRejectStatus());
	}

	public ArrayList<TaxiTimePointIndex> getAvailableTaxis(PeopleRequest pr) {
		ArrayList<TaxiTimePointIndex> L = new ArrayList<TaxiTimePointIndex>();
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
			//System.out.println(name() + "::getAvailableTaxis, taxi[ " + k + "/" + sim.vehicles.size() + "] = " +
				//taxi.ID + ", currentItinerary.sz = " + taxi.currentItinerary.size());
			
		}
		return L;
	}

	public void processParcelRequest(ParcelRequest pr) {
		// TODO Auto-generated method stub
		// int taxiID = 727;

		ServiceSequence sel_ss = null;
		TimePointIndex sel_tpi = null;
		Vehicle sel_taxi = null;
		for (int k = 0; k < sim.vehicles.size(); k++) {
			Vehicle taxi = sim.vehicles.get(k);
			if (taxi.status == VehicleStatus.STOP_WORK)
				continue;
			if (taxi.totalTravelDistance > sim.maxTravelDistance)
				continue;
			if (taxi.remainRequestIDs.size() > sim.maxPendingStops)
				continue;
			TimePointIndex tpi = taxi.getNextTimePointIndex(
					taxi.lastIndexPoint, sim.T.currentTimePoint,
					Simulator.TimePointDuration);
			ServiceSequence ss = computeBestProfitsParcelInsertion(taxi, tpi,
					pr);
			if (ss != null) {
				// System.out.println("SimulatorBookedRequest::processPeopleRequest, taxi "
				// + taxi.ID +
				// ", profits = " + ss.profitEvaluation);
				if (sel_ss == null) {
					sel_ss = ss;
					sel_taxi = taxi;
					sel_tpi = tpi;
				} else {
					if (sel_ss.profitEvaluation < ss.profitEvaluation) {
						sel_ss = ss;
						sel_taxi = taxi;
						sel_tpi = tpi;
					}
				}
			}
		}
		if (sel_taxi == null) {
			sim.nbParcelRejects++;
			System.out
					.println("SimulatorBookedRequest::processParcelRequest --> request "
							+ pr.id
							+ " is REJECTED, nbParcleRejects = "
							+ sim.nbParcelRejects
							+ "/"
							+ sim.allParcelRequests.size());
			log.println("SimulatorBookedRequest::processParcelRequest --> request "
					+ pr.id + " is REJECTED");
			return;
		}
		int nextStartTimePoint = sel_tpi.timePoint;// sel_taxi.getNextStartTimePoint();
		int fromPoint = sel_tpi.point;// sel_taxi.getNextStartPoint();
		int fromIndex = sel_tpi.indexPoint;// sel_taxi.getNextStartPointIndex();

		System.out
				.println("SimulatorBookedRequest::processParcelRequest, sequence = "
						+ sel_ss.getSequence()
						+ ", maxProfits = "
						+ sel_ss.profitEvaluation
						+ ", sel_taxi = "
						+ sel_taxi.ID
						+ ", nextStartTimePoint = "
						+ nextStartTimePoint
						+ ", fromIndex = "
						+ fromIndex
						+ ", fromPoint = " + fromPoint);

		if (sim.debugTaxiID == sel_taxi.ID) {
			log.println("SimulatorBookedRequest::processParcelRequest(" + pr.id
					+ ") AT " + sim.T.currentTimePoint + " engage taxi "
					+ sel_taxi.ID + " sequence = " + sel_ss.getSequence()
					+ ", maxProfits = " + sel_ss.profitEvaluation
					+ ", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = "
					+ nextStartTimePoint + ", fromIndex = " + fromIndex
					+ ", fromPoint = " + fromPoint);
		}
		sim.admitNewItinerary(sel_taxi, nextStartTimePoint, fromIndex,
				fromPoint, sel_ss);

		if (sel_taxi.ID == sim.debugTaxiID)
			log.println(name() + "::processParcelRequest(" + pr.id + ") AT "
					+ sim.T.currentTimePoint + " insert parcel to taxi "
					+ sel_taxi.ID + ", parcelOnBoards = "
					+ sel_taxi.getParcelOnBoards() + ", peopleOnBoards = "
					+ sel_taxi.getPeopleOnBoards() + ", remainRequestIDs = "
					+ sel_taxi.getRemainRequestID());

		sim.nbParcelWaitBoarding++;
		System.out
				.println("SimulatorBookedRequest::processParcelRequest, nbParcelWaitBoarding = "
						+ sim.nbParcelWaitBoarding
						+ ", nbParcelComplete = "
						+ sim.nbParcelComplete
						+ ", nbParcelOnBoard = "
						+ sim.nbParcelOnBoard
						+ ", nbParcelRejects = "
						+ sim.nbParcelRejects
						+ ", total ParcelRequests = "
						+ sim.allParcelRequests.size());

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
