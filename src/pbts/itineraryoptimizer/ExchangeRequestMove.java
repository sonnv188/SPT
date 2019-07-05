package pbts.itineraryoptimizer;

import pbts.entities.TimePointIndex;
import pbts.entities.Vehicle;

public class ExchangeRequestMove {
	public Vehicle taxi1;
	public Vehicle taxi2;
	public int index1;// index of the service point in taxi1.remainRequestIDs
	public int index2;// index of the service point in taxi2.remainRequestIDs
	public TimePointIndex tpi1;
	public TimePointIndex tpi2;
	public double eval;
}
