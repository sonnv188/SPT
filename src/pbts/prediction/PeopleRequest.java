package pbts.prediction;

public class PeopleRequest extends Request{
	private int maxNumberStops;	// [OPTIONAL] maximum number of stops (pickup/delivery of other requests) between the people service
								// if maximumNumberStops = 0, then people want a direct service, no sharing
	private int maxTravelDistance;// [OPTIONAL] people can accept a shared ride (with parcels), 
								// but the total distance from pickup point and delivery point is at most maxTravelDistance
	
}
