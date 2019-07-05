package pbts.datamanipulation;

public class ServedRequest {

	public int taxiID;
	public int pickupLocationID;
	public int pickupTime;
	public int deliveryLocationID;
	public int deliveryTime;
	
	public ServedRequest(int taxiID, int pickupLocationID, int pickupTime, int deliveryLocationID, int deliveryTime){
		this.taxiID = taxiID;
		this.pickupLocationID = pickupLocationID;
		this.pickupTime = pickupTime;
		this.deliveryLocationID = deliveryLocationID;
		this.deliveryTime = deliveryTime;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
