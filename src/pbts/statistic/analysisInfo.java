package pbts.statistic;

import java.util.HashMap;

import pbts.entities.ItineraryTravelTime;
import pbts.simulation.SimulatorTimeUnit;

public class analysisInfo {

	public SimulatorTimeUnit sim = null;
	public analysisInfo(SimulatorTimeUnit sim){
		this.sim = sim;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String data_dir = "E:\\Project\\pbts\\git_project\\SPT\\";
		String itineraryFileName = data_dir + "SanFrancisco_std\\outputNewPlanner\\ins_day_1_minSpd_5_maxSpd_60.txt-plannerdynamicSARPandPredictionPlanner-maxPendingStops10-decisionTime15-itinerary.txt";
		HashMap<Integer, ItineraryTravelTime> itineraries = sim.loadItineraries(itineraryFileName);
		for(int key : itineraries.keySet()){
			ItineraryTravelTime itt = itineraries.get(key);
			int load = 0;
			double totalDistanceUnload = 0;
			for(int p = 0; p < itt.path.size() - 1; p++){
				int point1 = itt.path.get(p);
				int point2 = itt.path.get(p+1);
				if (itt.actions.get(p).equals("PICKUP_PEOPLE") || itt.actions.get(p).equals("PICKUP_PARCEL"))
					load++;
				else if (itt.actions.get(p).equals("DELIVERY_PEOPLE") || itt.actions.get(p).equals("DELIVERY_PARCEL"))
					load--;
				if(load == 0){
				}
			}
			
		}
		
	}
}
