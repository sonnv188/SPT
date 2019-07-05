package SARP2014;

public class Stop {
	public int reqId;
	public int id;
	public double lat; //lat
    public double lng;//long
    public int e;//early
    public int l;//later
    public boolean peopleSt;
    public boolean pickUpSt;
    public Stop(int id, int reqId, double lat, double lng, int e, int l, boolean pickUpSt, boolean peopleSt){
    	this.id = id;
    	this.reqId = reqId;
    	this.lat = lat;
    	this.lng = lng;
    	this.e = e;
    	this.l = l;
    	this.pickUpSt = pickUpSt;
    	this.peopleSt = peopleSt;
    }
}
