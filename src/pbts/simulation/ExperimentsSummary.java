package pbts.simulation;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;




import pbts.onlinealgorithmtimeunit.GreedyPeopleParcelInsertSharing;
import pbts.onlinealgorithmtimeunit.NaiveSequentialPlanner;
import pbts.onlinealgorithmtimeunit.OnlinePlanner;
import pbts.onlinealgorithmtimeunit.PeopleDirectServiceGreedy;

public class ExperimentsSummary {

	public AnalysisTemplate readResults(int maxTimeRequest, int timePointDuration,
			String requestFN, OnlinePlanner planner, int maxNbPendingStops, String dir){
		
		String fn = dir + "\\" + requestFN + "-planner" + planner.name()
				+ "-maxTimeRequest" + maxTimeRequest + "-timePointDuration"
				+ timePointDuration + "-maxPendingStops" + maxNbPendingStops;
		String fnSummary = fn + "-summary.xml";
		AnalysisTemplate at = new AnalysisTemplate();
		try{
			File file = new File(fnSummary);
			DocumentBuilderFactory d = DocumentBuilderFactory.newInstance();
			DocumentBuilder dbFactory = d.newDocumentBuilder();

			Document doc = dbFactory.parse(file);
			Element node = (Element) doc.getElementsByTagName("results").item(0);
			
			Element e = (Element)node.getElementsByTagName("total-benefits").item(0);
			String val = e.getChildNodes().item(0).getNodeValue();
			at.benefits = Double.valueOf(val);
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return at;
	}
	
	public void summarize(){
		String dir = "SanFrancisco";
		// String dir =
		// "C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\tuan\\datacheck\\data01000000";
		SimulatorTimeUnit sim = new SimulatorTimeUnit();
		// String mapFN = "SanfranciscoRoad-connected-contracted-5.txt";// max
		// Arc length is now > 3000m
		String mapFN = "SanfranciscoRoad-connected-contracted-5-refine-50.txt";// max
																				// Arc
																				// length
																				// =
																				// 50m
		// String mapFN = "reduceGraph.txt";
		String configParametersFN = "config-parameters.txt";
		String depotParkingFN = "depot600-parkings54.txt";
		// String depotParkingFN = "depot1000-parkings54.txt";
		// String depotParkingFN = "depot2.txt";
		// String requestFN =
		// "request_people_parcel_day_1_minSpeed_5.0_maxSpeed_60.0.txt";
		// String requestFN = "requestsPeopleParcel.txt";

		sim.loadMapFromTextFile(dir + "\\" + mapFN);
		// System.exit(-1);
		// sim.loadRequests(dir + "\\request_day_1.txt");

		sim.loadParameters(dir + "\\" + configParametersFN);
		sim.loadDepotParkings(dir + "\\" + depotParkingFN);
		sim.initialize();

		GreedyPeopleParcelInsertSharing planner1 = new GreedyPeopleParcelInsertSharing(
				sim);
		PeopleDirectServiceGreedy planner2 = new PeopleDirectServiceGreedy(sim);
		NaiveSequentialPlanner planner3 = new NaiveSequentialPlanner(sim);
		OnlinePlanner[] planner = new OnlinePlanner[] { planner1, planner2,
				planner3 };
		int[] maxTimeRequest = new int[] {3600, 18000, 36000, 75600 };
		int[] timePointDuration = new int[] { 10, 30 };
		int[] maxPendingStops = new int[] { 6, 10 };

		PrintWriter exprLog = null;
		try {
			exprLog = new PrintWriter("Expr-instances-log.txt");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		for (int d = 1; d <= 31; d++) {
		//for(int d = 1; d <= 1; d++){
			String requestFN = "request_people_parcel_day_" + d
					+ "_minSpeed_5.0_maxSpeed_60.0.txt";
			for(int i = 0; i < timePointDuration.length; i++){
			//for (int i = 0; i < 1; i++) {
				for(int j = 0; j < maxTimeRequest.length; j++){
				//for (int j = 0; j < 1; j++) {
					for (int k = 0; k < planner.length; k++) {
						for (int q = 0; q < maxPendingStops.length; q++) {
							AnalysisTemplate at = readResults(maxTimeRequest[j], timePointDuration[i],
									requestFN, planner[k], maxPendingStops[q], dir);
							
							System.out.println(planner[k].name() + "\t" + maxPendingStops[q] + "\t" + at.benefits);
						}
					}
				}
			}
		}
		sim.finalize();
		exprLog.close();

	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ExperimentsSummary ES = new ExperimentsSummary();
		ES.summarize();
	}

}
