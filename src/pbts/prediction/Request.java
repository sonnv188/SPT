package pbts.prediction;

public class Request {
	private int id;
	private RequestType type;
	private int timeCall; // in seconds (i.e., time call is 10:30am -> timeCall = 10*3600 + 30*60 = 36000 + 1800 = 37800) 
	private double latPickup;// latitude of pickup location
	private double lngPickup;// longitude of pickup location
	private double latDelivery;// latitude of delivery location
	private double lngDelivery;// longitude of delivery location
	private int earlyPickup;// in seconds 
	private int latePickup;// in seconds: earlyPickup <= pickupTimePoint <= latePickup
	private int earlyDelivery;// in seconds
	private int lateDelivery;// in seconds: earlyDelivery <= deliveryTimePoint <= lateDelivery
}
