package pbts.entities;

/*
 * Represent next Time&Point on an itinerary
 */
public class TimePointIndex {

	public int timePoint;// departure time point
	public int point;// point id of the road map
	public int indexPoint;// index of the point on current itinerary
	public int indexRemainRequestIDs = -1; //[SonNV] number of PICKUP or DELIVERY point in I itinerary which is passed in decision time.
											//= index of last point in remainRequestIDs which is passed in decision time.
	public TimePointIndex(int time, int point, int idx){
		this.timePoint = time;
		this.point = point;
		this.indexPoint = idx;
	}
	public String toString(){
		return "TimePoint[" + timePoint + "] indexPoint[" + indexPoint + "] point[" + point + "]";
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
