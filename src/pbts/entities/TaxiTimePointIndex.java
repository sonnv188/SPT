package pbts.entities;
import java.util.*;
public class TaxiTimePointIndex {
	public Vehicle taxi;
	public TimePointIndex tpi;
	public ArrayList<Integer> remainRequestIDs;
	public ArrayList<Integer> keptRequestIDs;
	public double estimation;//distance from a point in remain request ID list to new request point.
	public int idx;//index of the point in remain request ID list.
	
	public TaxiTimePointIndex(Vehicle taxi, TimePointIndex tpi, ArrayList<Integer> remainRequestIDs,
			ArrayList<Integer> keptRequestIDs){
		this.taxi = taxi;
		this.tpi = tpi;
		this.remainRequestIDs = remainRequestIDs;
		this.keptRequestIDs = keptRequestIDs;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
