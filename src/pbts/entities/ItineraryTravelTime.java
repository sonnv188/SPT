package pbts.entities;

import java.util.*;

import pbts.enums.*;

public class ItineraryTravelTime extends Itinerary{

	/**
	 * @param args
	 */
	//private ArrayList<Integer> arrTime;// arrTime.get(i) is the arrival time point of vehicle at point path.get(i);
	//private ArrayList<Integer> depTime;// depTime.get(i) is the departure time point of vehicle from point path.get(i);
	public HashMap<Integer, Integer> arrTime; //[SonNV] arrival time point of the vehicle at point path.get(i).
	public HashMap<Integer, Integer> depTime;
	
	//[SonNV]requestID: -1, 10, -1, 20, -1, -1, 10, 20, 30 ... (requestID[i] is id of request at point path.get(i).
	//[SonNV]Positive values is PICKUP or DELIVERY action, negative values is PASS action.
	public ArrayList<Integer> requestID;// ID of the request that the vehicle services
	public ArrayList<VehicleAction> actions;//[SonNV] actions[i] is kind of action at path.get(i).
	public HashMap<Integer, VehicleStatus> mStatus;
	
	
	public ItineraryTravelTime(ArrayList<Integer> path, HashMap<Integer,Integer> arrTime, HashMap<Integer,Integer> depTime,
			ArrayList<Integer> requestID, ArrayList<VehicleAction> actions){
		super(path);
		this.arrTime = arrTime;this.depTime = depTime;
		this.requestID = requestID;
		this.actions = actions;
	}
	public ItineraryTravelTime(int[] path,
			ArrayList<Integer> requestID, ArrayList<VehicleAction> actions){
		super(path);
		this.arrTime = new HashMap<Integer, Integer>();
		this.depTime = new HashMap<Integer, Integer>();
		this.requestID = requestID;
		this.actions = actions;
	}
	public ItineraryTravelTime(){
		super();//this.path = new ArrayList<Integer>();
		this.arrTime = new HashMap<Integer, Integer>();
		this.depTime = new HashMap<Integer, Integer>();
		this.requestID = new ArrayList<Integer>();
		this.actions = new ArrayList<VehicleAction>();
		//this.mStatus = new HashMap<Integer, VehicleStatus>();
	}
	public HashSet<Integer> collectRequestID(){
		HashSet<Integer> S = new HashSet<Integer>();
		if(requestID == null) return S;
		for(int i = 0; i < requestID.size(); i++)
			if(requestID.get(i) != -1) 
				S.add(Math.abs(requestID.get(i)));
		return S;
	}
	public HashSet<Integer> collectRequestID(int fromIdx){
		HashSet<Integer> S = new HashSet<Integer>();
		if(requestID == null) return S;
		for(int i = fromIdx; i < requestID.size(); i++)
			if(requestID.get(i) != -1) 
				S.add(Math.abs(requestID.get(i)));
		return S;
	}
	public int findLastDeliveryIndexPoint(int fromIndex){
		int idx = path.size()-1;
		while(idx >= fromIndex+1){
			if(requestID.get(idx) > 0 && 
					actions.get(idx) == VehicleAction.DELIVERY_PARCEL || actions.get(idx) == VehicleAction.DELIVERY_PEOPLE) 
				return idx;
			idx--;
		}
		
		return -1;
	}
	public void addRequestID(int id){
		requestID.add(id);
	}
	public void addAction(VehicleAction act){
		actions.add(act);
	}
	public void setAction(VehicleAction act,  int idx){
		actions.set(idx, act);
	}
	public void addPoint(int v){
		path.add(v);
	}
	public int getArrivalTime(int idx){
		if(arrTime.get(idx) != null) return arrTime.get(idx);
		else return -1;
	}
	public int getDepartureTime(int idx){ if(depTime.get(idx) != null) return depTime.get(idx); else return -1;}
	public int getTravelTime(int fromIndex){
		// return the travel time from node path.get(fromIndex) to path.get(fromIndex+1);
		return getArrivalTime(fromIndex+1) - getDepartureTime(fromIndex);
	}
	public void setArrivalTime(int idx, int t){
		arrTime.put(idx, t);
	}
	public void setDepartureTime(int idx, int t){
		depTime.put(idx, t);
	}
	public VehicleAction getAction(int idx){
		if(idx >= actions.size()){
		
		}
		return actions.get(idx);
	}
	
	public int getRequestID(int idx){ return requestID.get(idx);}
	public void removeAction(int idx){ actions.remove(idx);}
	public void removeRequestID(int idx){ requestID.remove(idx);}
	
	public void writeToFile(java.io.PrintWriter f){
		for(int i = 0; i < size(); i++){
			int v = get(i);
			String s = Vehicle.getActionDescription(getAction(i));
			int rID = getRequestID(i);
			f.println("point[" + i + "] = " + v + ", arrTime = " + getArrivalTime(i) + ", depTime = " + getDepartureTime(i) + ", action " + s + ", rID " + rID);
		}
	}
	public String toString(int b, int e){
		String s = "";
		for(int i = b; i <= e; i++){
			s = s + i + " : " + get(i) + " arr = " + getArrivalTime(i) + " dep = " + getDepartureTime(i) + 
					" act = " + Vehicle.getActionDescription(getAction(i)) +  " rid = " + getRequestID(i) + "\n";
		}
		return s;
	}
	public String toString(){
		return toString(0,size()-1);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<Integer> L = new ArrayList<Integer>();
		L.add(1);
		L.add(2);
		L.add(3);
		L.add(4);
		
		L.remove(2);
		for(int i = 0; i < L.size(); i++)
			System.out.print(L.get(i) + ", ");
		System.out.println();
	}

}
