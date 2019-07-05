package pbts.entities;

import java.util.*;
public class TimePointIndexRequestIDs {
	public TimePointIndex tpi;
	public ArrayList<Integer> requestIDs;
	
	public TimePointIndexRequestIDs(TimePointIndex tpi, ArrayList<Integer> R){
		this.tpi = tpi;
		this.requestIDs = R;
	}
}
