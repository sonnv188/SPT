package pbts.entities;

public class Request {
	public int id;
	public int type;
	public int timePoint;
	public int pickupLocId;
	public LatLng pickLL;
	public int earlyPickupTime;
	public int latePickupTime;
	public int pickDuration;
	public int deliveryLocId;
	public LatLng deliveryLL;
	public int earlyDeliveryTime;
	public int lateDeliveryTime;
	public int deliveryDuration;
	
	public Request(){
		
	}
}
