package pbts.itineraryoptimizer;

import pbts.entities.ErrorMSG;
import pbts.entities.ItineraryTravelTime;
import pbts.entities.TimePointIndex;
import pbts.entities.Vehicle;
import pbts.enums.ErrorType;
import pbts.simulation.ServiceSequence;
import pbts.simulation.Simulator;
import pbts.simulation.SimulatorTimeUnit;
import pbts.simulation.Utility;

public class ItineraryOptimizer {
	public SimulatorTimeUnit sim;
	public int nbMovesPerformed;

	public ItineraryOptimizer(SimulatorTimeUnit sim) {
		this.sim = sim;
		nbMovesPerformed = 0;
	}

	public ExchangeRequestMove getBestDistanceExchange(Vehicle taxi1,
			TimePointIndex tpi1, Vehicle taxi2, TimePointIndex tpi2) {
		double minDis = 100000000;
		ExchangeRequestMove m = null;//

		// TimePointIndex new_tpi1 = new TimePointIndex(0, 0, 0);
		// TimePointIndex new_tpi2 = new TimePointIndex(0, 0, 0);
		//if(taxi1.ID == 78 && taxi2.ID == 573 || taxi1.ID == 573 && taxi2.ID == 78)
		//if(taxi1.ID == 573 || taxi2.ID == 573)	
			//sim.log.println(name() + "::getBestDistanceExchange, taxi1 = " + taxi1.ID + ", tpi1 = " + tpi1.toString() + 
				//", taxi2 = " + taxi2.ID + ", tpi2 = " + tpi2.toString());
		for (int i = 0; i < taxi1.indicesCompleteServiceSequence.size(); i++) {
			int i1 = taxi1.indicesCompleteServiceSequence.get(i);
			int point1 = sim
					.getLocationFromEncodedRequest(taxi1.remainRequestIDs
							.get(i1));
			int point01 = tpi1.point;
			int startTime1 = tpi1.timePoint;
			int idx1 = tpi1.indexPoint;
			if (i1 > 0) {
				int r1 = taxi1.remainRequestIDs.get(i1 - 1);
				point01 = sim.getLocationFromEncodedRequest(r1);
				idx1 = taxi1.mRequestID2Index.get(r1);
				startTime1 = taxi1.currentItinerary.getDepartureTime(idx1);
				//if(taxi1.ID == 78 && taxi2.ID == 573 || taxi1.ID == 573 && taxi2.ID == 78)
				if(taxi1.ID == 573 || taxi2.ID == 573)	
					sim.log.println(name() + "::getBestDistanceExchange, i1 = " + i1 + " -> update idx1 = " + idx1 + ", point01 = " +
				point01 + ", startTime1 = " + startTime1);
			}
			for (int j = 0; j < taxi2.indicesCompleteServiceSequence.size(); j++) {
				int i2 = taxi2.indicesCompleteServiceSequence.get(j);
				int point2 = sim
						.getLocationFromEncodedRequest(taxi2.remainRequestIDs
								.get(i2));
				int point02 = tpi2.point;
				int startTime2 = tpi2.timePoint;
				int idx2 = tpi2.indexPoint;
				if (i2 > 0) {
					int r2 = taxi2.remainRequestIDs.get(i2 - 1);
					point02 = sim.getLocationFromEncodedRequest(r2);
					if (taxi2.mRequestID2Index.get(r2) == null) {
						System.out.println(name()
								+ "::getBestDistanceExchange, taxi " + taxi2.ID
								+ " mRequestID2Index.get(" + r2
								+ ") is NULL???????");
						sim.log.println(name()
								+ "::getBestDistanceExchange, taxi " + taxi2.ID
								+ " mRequestID2Index.get(" + r2
								+ ") is NULL??????? I = "
								+ taxi2.currentItinerary.toString());

						sim.exit();
					}
					idx2 = taxi2.mRequestID2Index.get(r2);
					startTime2 = taxi2.currentItinerary.getDepartureTime(idx2);
					//if(taxi1.ID == 78 && taxi2.ID == 573 || taxi1.ID == 573 && taxi2.ID == 78)
					if(taxi1.ID == 573 || taxi2.ID == 573)	
						sim.log.println(name() + "::getBestDistanceExchange, taxi2 = " + taxi2.ID + ", r2 = " + r2 + 
								", remainRequestIDs = " + Utility.arr2String(taxi2.remainRequestIDs) + ", i2 = " + i2 + " -> update idx2 = " + idx2 + ", point02 = " +
							point02 + ", startTime2 = " + startTime2);
				}

				double d12 = sim.dijkstra.queryDistance(point01, point2);
				double d21 = sim.dijkstra.queryDistance(point02, point1);
				double d11 = sim.dijkstra.queryDistance(point01, point1);
				double d22 = sim.dijkstra.queryDistance(point02, point2);
				double d = d12 + d21 - d11 - d22;
				if (d < -sim.thresholdDeltaDistanceExchangeAccepted && d < minDis) {
					//System.out.println(name() + "::getBestDistanceExchange, discover d = " + d + " < 0: d11 = " + 
				//d11 + ", d22 = " + d22 + ", d12 = " + d12 + ", d21 = " + d21 + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					
					//sim.exit();
					
					boolean ok = false;
					ErrorMSG err = sim.checkTimeWindowService(taxi1, point01,
							startTime1, taxi2.remainRequestIDs, i2,
							taxi2.remainRequestIDs.size() - 1);
					if (err.err == ErrorType.NO_ERROR) {
						err = sim.checkTimeWindowService(taxi2, point02,
								startTime2, taxi1.remainRequestIDs, i1,
								taxi1.remainRequestIDs.size() - 1);
						ok = err.err == ErrorType.NO_ERROR;
					}
					if (ok) {
						//System.out.println(name() + "::getBestDistanceExchange, discover d = " + d + " < 0 AND FEASIBLE!!!!!!!");
						//sim.exit();
						
						minDis = d;
						if (m == null) {
							m = new ExchangeRequestMove();
							m.tpi1 = new TimePointIndex(0, 0, 0);// new_tpi1;
							m.tpi2 = new TimePointIndex(0, 0, 0);// new_tpi2;
						}
						m.taxi1 = taxi1;
						m.taxi2 = taxi2;
						m.index1 = i1;
						m.index2 = i2;
						m.tpi1.timePoint = startTime1;
						m.tpi1.indexPoint = idx1;
						m.tpi1.point = point01;
						m.tpi2.timePoint = startTime2;
						m.tpi2.indexPoint = idx2;
						m.tpi2.point = point02;
						m.eval = minDis;
						
						if(m.taxi1.currentItinerary.size() <= m.tpi1.indexPoint){
							System.out.println(name() + "::getBestDistanceExchange, EXCEPTION taxi1 " + m.taxi1.ID + " currentItinerary.sz = " + 
						m.taxi1.currentItinerary.size() + "  <  m.tpi1.indexPoint " + m.tpi1.indexPoint + "?????????");
							sim.log.println(name() + "::getBestDistanceExchange, EXCEPTION taxi1 " + m.taxi1.ID + " currentItinerary.sz = " + 
									m.taxi1.currentItinerary.size() + "  <  m.tpi1.indexPoint " + m.tpi1.indexPoint + "?????????");
							sim.log.println(name() + "::getBestDistanceExchange taxi1.currentItinerary = " + m.taxi1.currentItinerary.toString());
							sim.exit();
						}
						if(m.taxi2.currentItinerary.size() <= m.tpi2.indexPoint){
							System.out.println(name() + "::getBestDistanceExchange, EXCEPTION taxi2 " + m.taxi2.ID + " currentItinerary.sz = " + 
						m.taxi2.currentItinerary.size() + "  <  m.tpi2.indexPoint " + m.tpi2.indexPoint + "?????????");
							sim.log.println(name() + "::getBestDistanceExchange, EXCEPTION taxi2 " + m.taxi2.ID + " currentItinerary.sz = " + 
									m.taxi2.currentItinerary.size() + "  <  m.tpi2.indexPoint " + m.tpi2.indexPoint + "?????????");
							sim.log.println(name() + "::getBestDistanceExchange taxi2.currentItinerary = " + m.taxi2.currentItinerary.toString());
							sim.exit();
						}
					}
				}

			}
		}
		return m;
	}

	public void moveSARP2014(ExchangeRequestMove m) {
		int[] rid1 = new int[m.taxi1.remainRequestIDs.size() - m.index1];
		int[] rid2 = new int[m.taxi2.remainRequestIDs.size() - m.index2];
		for (int i = m.index1; i < m.taxi1.remainRequestIDs.size(); i++) {
			rid1[i - m.index1] = m.taxi1.remainRequestIDs.get(i);
		}
		for (int i = m.index2; i < m.taxi2.remainRequestIDs.size(); i++) {
			rid2[i - m.index2] = m.taxi2.remainRequestIDs.get(i);
		}
		//System.out.println(name() + "::move m.index1 = " + m.index1 + ", m.index2 = " + m.index2 + 
				//", m.taxi1 = " + m.taxi1.restAtParking() + ", m.taxi2 = " + m.taxi2.restAtParking());
		
		int p1 = m.taxi1.getFinalParking().locationID;
		int p2 = m.taxi2.getFinalParking().locationID;
		ServiceSequence ss1 = new ServiceSequence(rid1, 0, p1, 0);
		ServiceSequence ss2 = new ServiceSequence(rid2, 0, p2, 0);

		ItineraryTravelTime I1 = sim.establishItinerary(m.taxi1,
				m.tpi1.timePoint, m.tpi1.indexPoint, m.tpi1.point, ss2);
		ItineraryTravelTime I2 = sim.establishItinerary(m.taxi2,
				m.tpi2.timePoint, m.tpi2.indexPoint, m.tpi2.point, ss1);

		if(m.taxi1.ID == 573 || m.taxi2.ID == 573){
		sim.log.println(name() + "::move taxi1 = " + m.taxi1.ID + ", taxi1.I = "+ m.taxi1.currentItinerary.toString() + ", tpi1 = " + m.tpi1.toString());
		sim.log.println(name() + "::move taxi2 = " + m.taxi2.ID + ", taxi2.I = "+ m.taxi2.currentItinerary.toString() + ", tpi2 = " + m.tpi2.toString());
		sim.log.println(name() + "::move I1 = " + I1.toString() + ", ss1 = " + Utility.arr2String(ss1.rids));
		sim.log.println(name() + "::move I2 = " + I2.toString() + ", ss2 = " + Utility.arr2String(ss2.rids));
		}
		
		sim.admitNewItineraryWithoutStatusSARP2014(m.taxi1, m.tpi1.timePoint, m.tpi1.indexPoint,
				m.tpi1.point, I1, ss2);
		sim.admitNewItineraryWithoutStatusSARP2014(m.taxi2, m.tpi2.timePoint, m.tpi2.indexPoint,
				m.tpi2.point, I2, ss1);
		
		if(m.taxi1.ID == 573){
			sim.log.println(name() + "::move, after exchange, taxi1 = " + m.taxi1.ID + ", remainRequestIDs = " + 
		Utility.arr2String(m.taxi1.remainRequestIDs) + ", currentItinerary = " + m.taxi1.currentItinerary.toString());
		}
		if(m.taxi2.ID == 573){
			sim.log.println(name() + "::move, after exchange, taxi2 = " + m.taxi2.ID + ", remainRequestIDs = " + 
		Utility.arr2String(m.taxi2.remainRequestIDs) + ", currentItinerary = " + m.taxi2.currentItinerary.toString());
		}
	}
	
	public void move(ExchangeRequestMove m) {
		int[] rid1 = new int[m.taxi1.remainRequestIDs.size() - m.index1];
		int[] rid2 = new int[m.taxi2.remainRequestIDs.size() - m.index2];
		for (int i = m.index1; i < m.taxi1.remainRequestIDs.size(); i++) {
			rid1[i - m.index1] = m.taxi1.remainRequestIDs.get(i);
		}
		for (int i = m.index2; i < m.taxi2.remainRequestIDs.size(); i++) {
			rid2[i - m.index2] = m.taxi2.remainRequestIDs.get(i);
		}
		//System.out.println(name() + "::move m.index1 = " + m.index1 + ", m.index2 = " + m.index2 + 
				//", m.taxi1 = " + m.taxi1.restAtParking() + ", m.taxi2 = " + m.taxi2.restAtParking());
		
		int p1 = m.taxi1.getFinalParking().locationID;
		int p2 = m.taxi2.getFinalParking().locationID;
		ServiceSequence ss1 = new ServiceSequence(rid1, 0, p1, 0);
		ServiceSequence ss2 = new ServiceSequence(rid2, 0, p2, 0);

		ItineraryTravelTime I1 = sim.establishItinerary(m.taxi1,
				m.tpi1.timePoint, m.tpi1.indexPoint, m.tpi1.point, ss2);
		ItineraryTravelTime I2 = sim.establishItinerary(m.taxi2,
				m.tpi2.timePoint, m.tpi2.indexPoint, m.tpi2.point, ss1);

		if(m.taxi1.ID == 573 || m.taxi2.ID == 573){
		sim.log.println(name() + "::move taxi1 = " + m.taxi1.ID + ", taxi1.I = "+ m.taxi1.currentItinerary.toString() + ", tpi1 = " + m.tpi1.toString());
		sim.log.println(name() + "::move taxi2 = " + m.taxi2.ID + ", taxi2.I = "+ m.taxi2.currentItinerary.toString() + ", tpi2 = " + m.tpi2.toString());
		sim.log.println(name() + "::move I1 = " + I1.toString() + ", ss1 = " + Utility.arr2String(ss1.rids));
		sim.log.println(name() + "::move I2 = " + I2.toString() + ", ss2 = " + Utility.arr2String(ss2.rids));
		}
		
		sim.admitNewItineraryWithoutStatus(m.taxi1, m.tpi1.timePoint, m.tpi1.indexPoint,
				m.tpi1.point, I1, ss2);
		sim.admitNewItineraryWithoutStatus(m.taxi2, m.tpi2.timePoint, m.tpi2.indexPoint,
				m.tpi2.point, I2, ss1);
		
		if(m.taxi1.ID == 573){
			sim.log.println(name() + "::move, after exchange, taxi1 = " + m.taxi1.ID + ", remainRequestIDs = " + 
		Utility.arr2String(m.taxi1.remainRequestIDs) + ", currentItinerary = " + m.taxi1.currentItinerary.toString());
		}
		if(m.taxi2.ID == 573){
			sim.log.println(name() + "::move, after exchange, taxi2 = " + m.taxi2.ID + ", remainRequestIDs = " + 
		Utility.arr2String(m.taxi2.remainRequestIDs) + ", currentItinerary = " + m.taxi2.currentItinerary.toString());
		}
	}

	public void moveGreedyExchange(double startDecideTime) {
		//double t = System.currentTimeMillis();
		
		
		ExchangeRequestMove best_move = null;
		for (int i = 0; i < sim.vehicles.size() - 1; i++) {
			if((System.currentTimeMillis()-startDecideTime)*0.001 > sim.TimePointDuration){
				System.out.println(name() + "::moveGreedyExchange, decideTime expired!!!!!!!!!");
				break;
			}
			
			Vehicle taxi1 = sim.vehicles.get(i);
			TimePointIndex tpi1 = taxi1.getNextTimePointIndex(
					taxi1.lastIndexPoint, sim.T.currentTimePoint,
					sim.TimePointDuration);
			for (int j = i+1; j < sim.vehicles.size(); j++) {
				double t = System.currentTimeMillis();
				if((System.currentTimeMillis()-startDecideTime)*0.001 > sim.TimePointDuration){
					System.out.println(name() + "::moveGreedyExchange, decideTime expired!!!!!!!!!");
					break;
				}
				
				Vehicle taxi2 = sim.vehicles.get(j);
				TimePointIndex tpi2 = taxi2.getNextTimePointIndex(
						taxi2.lastIndexPoint, sim.T.currentTimePoint,
						sim.TimePointDuration);

				ExchangeRequestMove m = getBestDistanceExchange(taxi1, tpi1, taxi2, tpi2);
				if (m != null) {
					if (best_move == null) {
						best_move = m;
					} else {
						if (best_move.eval > m.eval) {
							best_move = m;
							System.out.println(name()
									+ "::moveExchangeGreedy, i = " + i
									+ ", j = " + j + " --> UPDATE best_move = "
									+ best_move.eval);
						}
					}
				}
			}
		}
		if (best_move != null) {
			System.out.println(name() + "::greedyExchange, best eval = "
					+ best_move.eval + ", nbMovesPerformed = "
					+ nbMovesPerformed);
			move(best_move);
			nbMovesPerformed++;
		} else {
			System.out
					.println(name()
							+ "::greedyExchange, best eval = NULL#############################"
							+ ", nbMovesPerformed = " + nbMovesPerformed);
		}

	}
	
	public void moveGreedyExchangeSARP2014(double startDecideTime) {
		//double t = System.currentTimeMillis();		
		ExchangeRequestMove best_move = null;
		for (int i = 0; i < sim.vehicles.size() - 1; i++) {
			if((System.currentTimeMillis()-startDecideTime)*0.001 > sim.TimePointDuration){
				System.out.println(name() + "::moveGreedyExchange, decideTime expired!!!!!!!!!");
				break;
			}
			
			Vehicle taxi1 = sim.vehicles.get(i);
			TimePointIndex tpi1 = taxi1.getNextTimePointIndex(
					taxi1.lastIndexPoint, sim.T.currentTimePoint,
					sim.TimePointDuration);
			for (int j = i+1; j < sim.vehicles.size(); j++) {
				double t = System.currentTimeMillis();
				if((System.currentTimeMillis()-startDecideTime)*0.001 > sim.TimePointDuration){
					System.out.println(name() + "::moveGreedyExchange, decideTime expired!!!!!!!!!");
					break;
				}
				
				Vehicle taxi2 = sim.vehicles.get(j);
				TimePointIndex tpi2 = taxi2.getNextTimePointIndex(
						taxi2.lastIndexPoint, sim.T.currentTimePoint,
						sim.TimePointDuration);

				ExchangeRequestMove m = getBestDistanceExchange(taxi1, tpi1, taxi2, tpi2);
				if (m != null) {
					if (best_move == null) {
						best_move = m;
					} else {
						if (best_move.eval > m.eval) {
							best_move = m;
							System.out.println(name()
									+ "::moveExchangeGreedy, i = " + i
									+ ", j = " + j + " --> UPDATE best_move = "
									+ best_move.eval);
						}
					}
				}
			}
		}
		if (best_move != null) {
			System.out.println(name() + "::greedyExchange, best eval = "
					+ best_move.eval + ", nbMovesPerformed = "
					+ nbMovesPerformed);
			moveSARP2014(best_move);
			nbMovesPerformed++;
		} else {
			System.out
					.println(name()
							+ "::greedyExchange, best eval = NULL#############################"
							+ ", nbMovesPerformed = " + nbMovesPerformed);
		}

	}

	public String name() {
		return "ItineraryOptimizer";
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
