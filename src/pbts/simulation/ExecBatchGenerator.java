package pbts.simulation;

import java.io.PrintWriter;

import pbts.onlinealgorithmtimeunit.GreedyExchangeSharingDecisionTimeLimitPlanner;
import pbts.onlinealgorithmtimeunit.GreedyExchangeSharingPlanner;
import pbts.onlinealgorithmtimeunit.GreedyPeopleDirectExchangePlanner;
import pbts.onlinealgorithmtimeunit.GreedyPeopleDirectNoExchangePlanner;
import pbts.onlinealgorithmtimeunit.GreedySharingNoExchangeDecisionTimeLimitPlanner;
import pbts.onlinealgorithmtimeunit.NaiveSequentialDecisionTimeLimitPlanner;
import pbts.onlinealgorithmtimeunit.OnlinePlanner;

public class ExecBatchGenerator {

	public ExecBatchGenerator() {

	}

	public static void gen(String fn, boolean linux) {
		try {
			SimulatorTimeUnit simulator = new SimulatorTimeUnit();

			OnlinePlanner[] planners = new OnlinePlanner[] {
					// new GreedyExchangeSharingPlanner(simulator),
					// new GreedyExchangeSharingPlanner(simulator),
					// new GreedyPeopleDirectExchangePlanner(simulator),
					// new GreedyPeopleDirectNoExchangePlanner(simulator),
				    new NaiveSequentialDecisionTimeLimitPlanner(simulator),
					new GreedyExchangeSharingDecisionTimeLimitPlanner(simulator),
					new GreedySharingNoExchangeDecisionTimeLimitPlanner(
							simulator) };
			String[] algoname = new String[]{
					"NaiveInsertion",
					"GreedyExchange",
					"GreedyNoExchange"
			};
			
			//boolean linux = true;// false;
			//boolean linux = false;
			
			String slash = "\\";
			String dir = "C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\code\\pbts\\SanFrancisco_std";

			if (linux) {
				slash = "/";
				dir = "/home/khmt/dungpq/projects/pbts/SanFrancisco_std";
			}

			//String output_dir = dir + slash + "output";

			String mapFN = "SanfranciscoRoad-connected-contracted-5-refine-50.txt";
			String configParametersFN = "config-parameters.txt";
			String depotParkingFN = "depots1000-parkings34.txt";
			String requestFN = "ins_day_" + 1 + "_minSpd_5_maxSpd_60.txt";
			int maxNbPendsingStops = 10;
			int maxTimeReceiveRequest = 75600;
			int startSimulationTime = 21600;
			int decisionTime = 15;

			//PrintWriter progr = new PrintWriter("progressiveFiles.txt");
			//PrintWriter xmlFiles = new PrintWriter("xmlFiles.txt");
			
			PrintWriter out = new PrintWriter(fn);
			for (int d = 1; d <= 31; d++) {
				requestFN = "ins_day_" + d + "_minSpd_5_maxSpd_60.txt";
				//dir = dir + slash + "day" + d;
				String output_dir = dir + slash + "output" + slash + "day" + d;
				//progr.println("day " + d);
				PrintWriter progr = new PrintWriter(output_dir + slash + "progressiveFiles.txt");
				PrintWriter xmlFiles = new PrintWriter(output_dir + slash + "xmlFiles.txt");
				for (int i = 0; i < planners.length; i++) {
					String progressiveFile = requestFN
							+ "-planner" + planners[i].name()
							+ "-maxPendingStops" + maxNbPendsingStops
							+ "-decisionTime" + decisionTime + "-statistic-progress.txt"; 
					String xmlFile = requestFN
							+ "-planner" + planners[i].name()
							+ "-maxPendingStops" + maxNbPendsingStops
							+ "-decisionTime" + decisionTime + "-summary.xml"; 
					
					String filename = output_dir + slash + requestFN
							+ "-planner" + planners[i].name()
							+ "-maxPendingStops" + maxNbPendsingStops
							+ "-decisionTime" + decisionTime;
					out.println("java -jar pbts.jar " + " --mapFileName " + dir
							+ slash + mapFN + " --configFileName " + dir
							+ slash + configParametersFN
							+ " --depotParkingFileName " + dir + slash
							+ depotParkingFN + " --requestFileName " + dir
							+ slash + requestFN + " --plannerName "
							+ planners[i].name()
							+ " --progressiveStatisticFileName " + filename
							+ "-statistic-progress.txt"
							+ " --itineraryFileName " + filename
							+ "-itinerary.txt" + " --summaryFileName "
							+ filename + "-summary.xml"
							+ " --maxNbPendingStops " + maxNbPendsingStops
							+ " --maxTimeReceiveRequest "
							+ maxTimeReceiveRequest + " --startSimulationTime "
							+ startSimulationTime + " --decisionTime "
							+ decisionTime + "\n\n");
					progr.println(progressiveFile + "\t" + algoname[i]);
					xmlFiles.println(xmlFile + "\t" + algoname[i]);
				}
				progr.print("-1");
				progr.close();
				
				xmlFiles.print("-1");
				xmlFiles.close();
				//progr.println("-1");
			}
			
			//progr.println(-2);
			out.close();
			//xmlFiles.close();
			//progr.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//ExecBatchGenerator.gen("execute-batch-linux.txt",true);
		ExecBatchGenerator.gen("execute-batch-windows.txt",false);
	}

}
