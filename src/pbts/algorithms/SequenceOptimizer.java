package pbts.algorithms;

import java.util.ArrayList;

import pbts.entities.Arc;
import pbts.entities.ErrorMSG;
import pbts.entities.LatLng;
import pbts.entities.PeopleRequest;
import pbts.entities.TimePointIndex;
import pbts.entities.Vehicle;
import pbts.enums.ErrorType;
import pbts.gismap.googlemaps.GoogleMapsQuery;
import pbts.simulation.Simulator;
import pbts.simulation.Utility;

public class SequenceOptimizer {

	public Simulator sim;
	public int maxLen;
	private int[] x;// decision variables (sequence)
	private boolean[] mark; // mark for permutation
	private int[] point;// sequence of locationID correspond to x (point.length
						// = x.length + 1, point[0] is startPoint)
	private int n;// actual size of the sequence
	private double best;
	private double cost;
	private int[] opt_x;
	private ArrayList<Integer> reqSeq = null;
	private ArrayList<Integer> keptReq = null;
	private int nbKept = 0;
	private GoogleMapsQuery G = null;
	private Vehicle taxi = null;
	private TimePointIndex tpi = null;
	private int startPoint = -1;
	public boolean foundSolution = false;
	public double t0;
	public double maxTime;
	
	public SequenceOptimizer(Simulator sim, int maxLen) {
		this.sim = sim;
		this.maxLen = maxLen;
		x = new int[maxLen];
		opt_x = new int[maxLen];
		mark = new boolean[maxLen];
		point = new int[maxLen + 1];
		G = new GoogleMapsQuery();
	}

	private double cost(int u, int v) {
		LatLng llu = sim.map.mLatLng.get(u);
		LatLng llv = sim.map.mLatLng.get(v);
		return G.computeDistanceHaversine(llu.lat, llu.lng, llv.lat, llv.lng);
	}

	public boolean checkLegalPermutationServiceSequence(Vehicle taxi,
			int[] seq, int sz) {

		for (int i = 0; i < taxi.parcelReqIDonBoard.size(); i++) {
			int r = taxi.parcelReqIDonBoard.get(i);
			boolean ok = false;
			for (int j = 0; j < sz; j++) {
				if (seq[j] == -r) {
					ok = true;
					break;
				}// pickup request has a corresponding delivery in the sequence
					// seq
			}
			if (!ok)
				return false;
		}
		for (int i = 0; i < taxi.peopleReqIDonBoard.size(); i++) {
			int r = taxi.peopleReqIDonBoard.get(i);
			boolean ok = false;
			for (int j = 0; j < sz; j++) {
				if (seq[j] == -r) {
					ok = true;
					break;
				}// pickup request has a corresponding delivery in the sequence
					// seq
			}
			if (!ok)
				return false;
		}

		for (int i = 0; i < sz; i++) {
			if (seq[i] < 0) {// delivery
				for (int j = i + 1; j < sz; j++)
					if (seq[j] == -seq[i])
						return false;// a delivery is before corresponding
										// pickup
				boolean ok = false;
				for (int j = 0; j < taxi.peopleReqIDonBoard.size(); j++) {
					if (taxi.peopleReqIDonBoard.get(j) == -seq[i]) {
						ok = true;
						break;
					}
				}
				if (!ok)
					for (int j = 0; j < taxi.parcelReqIDonBoard.size(); j++) {
						if (taxi.parcelReqIDonBoard.get(j) == -seq[i]) {
							ok = true;
							break;
						}
					}
				if (!ok)
					for (int j = 0; j < i; j++) {
						if (seq[j] == -seq[i]) {
							ok = true;
							break;
						}
					}
				if (!ok)
					return false;// a delivery has no corresponding pickup
			} else {
				boolean ok = false;
				for (int j = i + 1; j < sz; j++)
					if (seq[j] == -seq[i]) {// a pickup must be followed by a
											// delivery
						ok = true;
						break;
					}
				if (!ok)
					return false;
			}
		}
		return true;
	}

	public boolean checkLegalServiceSequenceForRequest(Vehicle taxi, int rid,
			int k) {
		// x[0...k-1] is already a legal service sequence
		// check if rid can be inserted at kth position of x so that x is a
		// legal service sequence
		if (rid < 0) {// delivery point
			// there must be a corresponding pickup point of
			// taxi.parcelReqOnBoard OR x[0..k-1]
			for (int i = 0; i < taxi.parcelReqIDonBoard.size(); i++)
				if (taxi.parcelReqIDonBoard.get(i) == -rid)
					return true;
			for(int i = 0; i < taxi.peopleReqIDonBoard.size(); i++)
				if(taxi.peopleReqIDonBoard.get(i) == -rid)
					return true;
			for (int i = 0; i < keptReq.size(); i++)
				if (keptReq.get(i) == -rid)
					return true;
			for (int i = 0; i < k; i++)
				if (x[i] == -rid)
					return true;
			return false;
		} else {

		}

		return true;
	}

	public boolean checkNotPeoplePeopleSharing(Vehicle taxi, int[] nod, int sz) {
		if (taxi.peopleReqIDonBoard.size() > 1)
			return false;
		if (taxi.peopleReqIDonBoard.size() == 1) {
			int r = taxi.peopleReqIDonBoard.get(0);
			boolean delivery = false;
			for (int j = 0; j < sz; j++) {
				if (nod[j] == -r) {
					delivery = true;
					break;
				} else {
					if (nod[j] > 0) {
						PeopleRequest pr = sim.mPeopleRequest.get(nod[j]);
						if (pr != null && !delivery)
							return false;// pickup another people pr while not
											// deliverying people on board
					}
				}
			}
		}
		int nbPickupPeople = 0;
		for (int i = 0; i < sz; i++) {
			if (nod[i] > 0) {
				PeopleRequest pr = sim.mPeopleRequest.get(nod[i]);
				if (pr != null) {// pickup people
					nbPickupPeople++;
					if (nbPickupPeople > 1)
						return false;
				}
			} else {
				PeopleRequest pr = sim.mPeopleRequest.get(-nod[i]);
				if (pr != null)
					nbPickupPeople--;// delivery people
			}
		}
		return true;
	}

	public boolean checkMaxStopPeopleOnBoard(Vehicle taxi, int[] nod, int sz) {
		if (taxi.peopleReqIDonBoard.size() == 0)
			return true;
		if (taxi.peopleReqIDonBoard.size() > 1)
			return false;
		int r = taxi.peopleReqIDonBoard.get(0);
		PeopleRequest pr = sim.mPeopleRequest.get(r);
		int countStops = sim.countStop.get(r); // number of stops r has already
												// traversed
		for (int i = 0; i < sz; i++) {
			if (nod[i] == -r) {
				return true;
			} else {
				countStops++;
				if (countStops > pr.maxNbStops)
					return false;
			}
		}
		return true;
	}

	public boolean checkMaxStopPeopleRequest(int[] nod, int sz) {
		int countStops = -1;
		PeopleRequest pr = null;
		for (int i = 0; i < sz; i++) {
			if (nod[i] > 0) {
				PeopleRequest pri = sim.mPeopleRequest.get(nod[i]);
				if (pri != null) {// pickup people --> init countStops by 0
					pr = pri;
					countStops = 0;
				} else {// pickup parcels
					if (countStops >= 0)
						countStops++;
				}
			} else {
				PeopleRequest pri = sim.mPeopleRequest.get(-nod[i]);
				if (pri != null) {
					if (countStops > pr.maxNbStops)
						return false;
					countStops = -1;// delivery people, reset countStops
					pr = null;
				} else {// delivery parcels
					if (countStops >= 0)
						countStops++;
				}
			}
		}
		return true;
	}

	public boolean checkMaxDistanceOfPeopleOnBoard(Vehicle taxi, int[] nod,
			int sz, int nextStartPoint) {
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
		return ok;
	}

	public String name() {
		return "SequenceOptimizer";
	}

	public String partialSolution(int k) {
		String s = "";
		for (int i = 0; i <= k; i++)
			s = s + x[i] + ",";
		return s;
	}

	private void TRY(int k) {
		double t = (System.currentTimeMillis() - t0)*0.001; 
		if(t > maxTime && foundSolution){
			System.out.println(name() + "::TRY(" + k + ") t = " + t + " EXPIRED maxTime = " + maxTime);// + " System.exit(-1)");
			//System.exit(-1);
			return;
		}
		
		// try values for x[k]
		for (int v = 0; v < reqSeq.size(); v++) {
			if (!mark[v]) {
				x[k] = reqSeq.get(v);
				//if (taxi.ID == sim.debugTaxiID)
					//sim.log.println(name() + "::TRY(" + k + ") seq = "
						//	+ partialSolution(k));
					//System.out.println(name() + "::TRY(" + k + ") seq = "
						//+ partialSolution(k));
				if (!checkLegalServiceSequenceForRequest(taxi, x[k], k)) {
					//if (taxi.ID == sim.debugTaxiID)
						//sim.log.println(name() + "::TRY(" + k + ") seq = "
							//	+ partialSolution(k)
								//+ " checkLegalServiceSequenceForRequest(x[" + k + "] = " + x[k] + ") -> NOT LEGAL --> continue");
						//System.out.println(name() + "::TRY(" + k + ") seq = "
							//+ partialSolution(k)
							//+ " checkLegalServiceSequenceForRequest(x[" + k + "] = " + x[k] + ") -> NOT LEGAL --> continue");
					continue;
				}

				// if(!checkMaxDistanceOfPeopleOnBoard(taxi, x, k+1,
				// startPoint)) continue;
				// if(!checkMaxStopPeopleOnBoard(taxi, x, k+1)) continue;
				// if(!checkMaxStopPeopleRequest(x, k+1)) continue;
				// if(!checkNotPeoplePeopleSharing(taxi, x, k+1)) continue;

				point[k + 1] = sim.getLocationID(x[k]);
				cost = cost + cost(point[k], point[k + 1]);
				mark[v] = true;
				if (k == n - 1) {
					ErrorMSG err = sim.checkServiceSequence(taxi, tpi, x, keptReq.size(), n);

					if (err.err == ErrorType.NO_ERROR) {
						if (cost < best) {
							best = cost;
							for (int i = 0; i < n; i++)
								opt_x[i] = x[i];
							foundSolution = true;
							//System.out.println(name() + "::TRY(" + k
							//		+ ") UPDATE BEST " + best);
							if (taxi.ID == sim.debugTaxiID) {
								sim.log.println(name() + "::TRY(" + k
										+ ") UPDATE BEST " + best
										+ ", opt_x = "
									+ Utility.arr2String(opt_x, n));
							}
						}
					} else {
						if (taxi.ID == sim.debugTaxiID) {
							//sim.log.println(name()	+ "::TRY("+ k+ ") --> checkServiceSequence FAILED, taxi.requestStatus = "
									//+ taxi.requestStatus() + ", x = "
									//+ Utility.arr2String(x, n));
						}
					}
				} else {
					if (cost < best)
						TRY(k + 1);
					else{
						//System.out.println(name() + "::TRY(" + k + ") --> BOUND!!!!!!!!!!!!!!");
					}
				}
				cost -= cost(point[k], point[k + 1]);
				mark[v] = false;
			}
		}
	}
	
	private void TRYMANHATTAN(int k) {
		double t = (System.currentTimeMillis() - t0)*0.001; 
		if(t > maxTime){
			//System.out.println(name() + "::TRY(" + k + ") t = " + t + " EXPIRED maxTime = " + maxTime);// + " System.exit(-1)");
			//System.exit(-1);
			return;
		}
		
		// try values for x[k]
		for (int v = 0; v < reqSeq.size(); v++) {
			if (!mark[v]) {
				x[k] = reqSeq.get(v);
				//if (taxi.ID == sim.debugTaxiID)
					//sim.log.println(name() + "::TRY(" + k + ") seq = "
						//	+ partialSolution(k));
					//System.out.println(name() + "::TRY(" + k + ") seq = "
						//+ partialSolution(k));
				if (!checkLegalServiceSequenceForRequest(taxi, x[k], k)) {
					//if (taxi.ID == sim.debugTaxiID)
						//sim.log.println(name() + "::TRY(" + k + ") seq = "
							//	+ partialSolution(k)
								//+ " checkLegalServiceSequenceForRequest(x[" + k + "] = " + x[k] + ") -> NOT LEGAL --> continue");
						//System.out.println(name() + "::TRY(" + k + ") seq = "
							//+ partialSolution(k)
							//+ " checkLegalServiceSequenceForRequest(x[" + k + "] = " + x[k] + ") -> NOT LEGAL --> continue");
					continue;
				}

				// if(!checkMaxDistanceOfPeopleOnBoard(taxi, x, k+1,
				// startPoint)) continue;
				// if(!checkMaxStopPeopleOnBoard(taxi, x, k+1)) continue;
				// if(!checkMaxStopPeopleRequest(x, k+1)) continue;
				// if(!checkNotPeoplePeopleSharing(taxi, x, k+1)) continue;

				point[k + 1] = sim.getLocationID(x[k]);
				cost = cost + cost(point[k], point[k + 1]);
				mark[v] = true;
				if (k == n - 1) {
					ErrorMSG err = sim.checkServiceSequence(taxi, tpi, x, keptReq.size(), n);

					if(taxi.ID == 2){
						int ab = 0;
					}
					if (err.err == ErrorType.NO_ERROR) {
						if (cost < best) {
							best = cost;
							for (int i = 0; i < n; i++)
								opt_x[i] = x[i];
							foundSolution = true;
							//System.out.println(name() + "::TRY(" + k
							//		+ ") UPDATE BEST " + best);
							if (taxi.ID == sim.debugTaxiID) {
								sim.log.println(name() + "::TRY(" + k
										+ ") UPDATE BEST " + best
										+ ", opt_x = "
									+ Utility.arr2String(opt_x, n));
							}
						}
					} else {
						if (taxi.ID == sim.debugTaxiID) {
							//sim.log.println(name()	+ "::TRY("+ k+ ") --> checkServiceSequence FAILED, taxi.requestStatus = "
									//+ taxi.requestStatus() + ", x = "
									//+ Utility.arr2String(x, n));
						}
					}
				} else {
					if (cost < best){
						TRYMANHATTAN(k + 1);
					}
					else{
						//System.out.println(name() + "::TRY(" + k + ") --> BOUND!!!!!!!!!!!!!!");
					}
				}
				cost -= cost(point[k], point[k + 1]);
				mark[v] = false;
			}
		}
	}

	public int[] computeShortestSequence(Vehicle taxi, TimePointIndex tpi,
			ArrayList<Integer> keptReq, int[] reqSeq, double maxTime) {
		ArrayList<Integer> r = new ArrayList<Integer>();
		for (int i = 0; i < reqSeq.length; i++)
			r.add(reqSeq[i]);
		return computeShortestSequence(taxi, tpi, keptReq, r, maxTime);

	}
	public int[] computeShortestSequence(Vehicle taxi, TimePointIndex tpi,
			int[] keptReq, ArrayList<Integer> reqSeq, double maxTime) {
		ArrayList<Integer> r = new ArrayList<Integer>();
		for (int i = 0; i < keptReq.length; i++)
			r.add(keptReq[i]);
		return computeShortestSequence(taxi, tpi, r, reqSeq, maxTime);

	}
	
	public int[] computeShortestSequenceManhattan(Vehicle taxi, TimePointIndex tpi,
			int[] keptReq, ArrayList<Integer> reqSeq, double maxTime) {
		ArrayList<Integer> r = new ArrayList<Integer>();
		for (int i = 0; i < keptReq.length; i++)
			r.add(keptReq[i]);
		return computeShortestSequenceManhattan(taxi, tpi, r, reqSeq, maxTime);

	}

	private String arr2String(ArrayList<Integer> a) {
		String s = "";
		for (int i = 0; i < a.size(); i++)
			s = s + a.get(i) + ",";
		return s;
	}

	public int[] computeShortestSequence(Vehicle taxi, TimePointIndex tpi,
			ArrayList<Integer> keptReq, ArrayList<Integer> reqSeq, double maxTime) {
		// input: keptReq: list of requests kept (do not change this order)
		// input: reqSeq is the input sequence requests id, reqSeq[i] < 0 means
		// delivery, reqSeq[i] > 0 means pickup
		// output: re-order reqSeq so that that path from startPoint go through
		// reqSeq is minimal
		this.maxTime = maxTime;
		this.tpi = tpi;
		int startPoint = tpi.point;
		System.out.println(name() + "::computeShortestSequence(taxi = "
				+ taxi.ID + ", startPoint = " + startPoint + ", kept = " + Utility.arr2String(keptReq) + ", reqSeq = "
				+ arr2String(reqSeq) + ", maxTime = " + maxTime);
		if (taxi.ID == sim.debugTaxiID) {
			sim.log.println(name() + "::computeShortestSequence(taxi = "
					+ taxi.ID + ") requestStarus = " + taxi.requestStatus()
					+ ", kept = " + Utility.arr2String(keptReq) + ", remain = "
					+ Utility.arr2String(reqSeq) + ", tpi = " + tpi.toString());
		}
		this.keptReq = keptReq;
		this.reqSeq = reqSeq;
		nbKept = keptReq.size();
		n = reqSeq.size() + keptReq.size();
		
		this.startPoint = startPoint;
		this.taxi = taxi;
		for (int i = 0; i < reqSeq.size(); i++)
			mark[i] = false;
		best = 1000000000;
		cost = 0;
		foundSolution = false;
		point[0] = startPoint;
		for (int i = 0; i < keptReq.size(); i++) {// keep all elements of
													// keptReq in the resulting
													// sequence
			x[i] = keptReq.get(i);
			point[i + 1] = sim.getLocationID(x[i]);
			cost += cost(point[i], point[i + 1]);
		}
		int k = keptReq.size();
		if(reqSeq.size() == 0){
			foundSolution = true;
			for(int i = 0; i < n; i++) opt_x[i] = x[i];
		}
		t0 = System.currentTimeMillis();
		TRY(k);
		if (!foundSolution) {
			System.out.println(name() + "::computeShortestSequence(taxi = "
					+ taxi.ID + ", startPoint = " + startPoint + ", seq = "
					+ arr2String(reqSeq) + " NO SOLUTION");
			System.out.println("People on boards = "
					+ arr2String(taxi.peopleReqIDonBoard));
			System.out.println("Parcel on boards = "
					+ arr2String(taxi.parcelReqIDonBoard));

			sim.log.println(name() + "::computeShortestSequence(taxi = "
					+ taxi.ID + ", startPoint = " + startPoint + ", kept = "
					+ Utility.arr2String(keptReq) + ", remain = "
					+ arr2String(reqSeq) + " NO SOLUTION"
					+ ", taxi.requestStatus = " + taxi.requestStatus());
			sim.log.println("People on boards = "
					+ arr2String(taxi.peopleReqIDonBoard));
			sim.log.println("Parcel on boards = "
					+ arr2String(taxi.parcelReqIDonBoard));
			// sim.log.close();
			// System.exit(-1);
			return null;
		}
		int[] s = new int[n];
		for (int i = 0; i < n; i++)
			s[i] = opt_x[i];
		return s;

	}
	
	public int[] computeShortestSequenceManhattan(Vehicle taxi, TimePointIndex tpi,
			ArrayList<Integer> keptReq, ArrayList<Integer> reqSeq, double maxTime) {
		// input: keptReq: list of requests kept (do not change this order)
		// input: reqSeq is the input sequence requests id, reqSeq[i] < 0 means
		// delivery, reqSeq[i] > 0 means pickup
		// output: re-order reqSeq so that that path from startPoint go through
		// reqSeq is minimal
		this.maxTime = maxTime;
		this.tpi = tpi;
		int startPoint = tpi.point;
		System.out.println(name() + "::computeShortestSequence(taxi = "
				+ taxi.ID + ", startPoint = " + startPoint + ", kept = " + Utility.arr2String(keptReq) + ", reqSeq = "
				+ arr2String(reqSeq) + ", maxTime = " + maxTime);
		if (taxi.ID == sim.debugTaxiID) {
			sim.log.println(name() + "::computeShortestSequence(taxi = "
					+ taxi.ID + ") requestStarus = " + taxi.requestStatus()
					+ ", kept = " + Utility.arr2String(keptReq) + ", remain = "
					+ Utility.arr2String(reqSeq) + ", tpi = " + tpi.toString());
		}
		this.keptReq = keptReq;
		this.reqSeq = reqSeq;
		nbKept = keptReq.size();
		n = reqSeq.size() + keptReq.size();
		
		this.startPoint = startPoint;
		this.taxi = taxi;
		for (int i = 0; i < reqSeq.size(); i++)
			mark[i] = false;
		best = 1000000000;
		cost = 0;
		foundSolution = false;
		point[0] = startPoint;
		for (int i = 0; i < keptReq.size(); i++) {// keep all elements of
													// keptReq in the resulting
													// sequence
			x[i] = keptReq.get(i);
			point[i + 1] = sim.getLocationID(x[i]);
			cost += cost(point[i], point[i + 1]);
		}
		int k = keptReq.size();
		if(reqSeq.size() == 0){
			foundSolution = true;
			for(int i = 0; i < n; i++) opt_x[i] = x[i];
		}
		t0 = System.currentTimeMillis();
		TRYMANHATTAN(k);
		if (!foundSolution) {
			System.out.println(name() + "::computeShortestSequence(taxi = "
					+ taxi.ID + ", startPoint = " + startPoint + ", seq = "
					+ arr2String(reqSeq) + " NO SOLUTION");
			System.out.println("People on boards = "
					+ arr2String(taxi.peopleReqIDonBoard));
			System.out.println("Parcel on boards = "
					+ arr2String(taxi.parcelReqIDonBoard));

			sim.log.println(name() + "::computeShortestSequence(taxi = "
					+ taxi.ID + ", startPoint = " + startPoint + ", kept = "
					+ Utility.arr2String(keptReq) + ", remain = "
					+ arr2String(reqSeq) + " NO SOLUTION"
					+ ", taxi.requestStatus = " + taxi.requestStatus());
			sim.log.println("People on boards = "
					+ arr2String(taxi.peopleReqIDonBoard));
			sim.log.println("Parcel on boards = "
					+ arr2String(taxi.parcelReqIDonBoard));
			// sim.log.close();
			// System.exit(-1);
			return null;
		}
		int[] s = new int[n];
		for (int i = 0; i < n; i++)
			s[i] = opt_x[i];
		return s;

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
