package pbts.statistic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

import pbts.simulation.AnalysisTemplate;

public class Statistic {

	public static String dilimiter = "/";
	//public static String dilimiter = "\\";
	
	public void genPlotFile(String fn, ArrayList<Integer> x, ArrayList<Double> y){
		try{
			PrintWriter out = new PrintWriter(fn);
			String sx = "[";
			String sy = "[";
			for(int i = 0;i < x.size()-1; i++){
				sx = sx + x.get(i) + ",";
				sy = sy + y.get(i) + ",";
			}
			sx = sx + x.get(x.size()-1) + "]";
			sy = sy + y.get(y.size()-1) + "]";
			out.println("x = " + sx + ";");
			out.println("y = " + sy + ";");
			out.println("plot(x,y)");
			out.close();
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void genPlotFile(String fn, String mBenefitFile, String mCostFile, String mDistanceFile, String mRequestFile, int sz){
		try{
			Scanner in = new Scanner(new File(fn));
			String line;
			line = in.nextLine();
			ArrayList<Integer> timePoints = new ArrayList<Integer>();
			ArrayList<Double> benefits = new ArrayList<Double>();
			ArrayList<Double> travelDistances = new ArrayList<Double>();
			ArrayList<Double> revenuePeoples = new ArrayList<Double>();
			ArrayList<Double> revenueParcels = new ArrayList<Double>();
			ArrayList<Double> costs = new ArrayList<Double>();
			ArrayList<Double> discounts = new ArrayList<Double>();
			ArrayList<Integer> peopleProcesseds = new ArrayList<Integer>();
			ArrayList<Integer> parcelProcesseds = new ArrayList<Integer>();
			//line = in.nextLine();
			while(in.hasNextLine()){
				line = in.nextLine();
				System.out.println(line);
				String[] s = line.split("\t");
				int timePoint = Integer.valueOf(s[0].trim());
				double benefit = Double.valueOf(s[1].trim());
				double revenueParcel = Double.valueOf(s[2].trim());
				double revenuePeople = Double.valueOf(s[3].trim());
				double cost = Double.valueOf(s[4].trim());
				double distance = Double.valueOf(s[5].trim());
				double discount = Double.valueOf(s[6].trim());
				int nbParcelProcessed = Integer.valueOf(s[17].trim());
				int nbPeopleProcessed = Integer.valueOf(s[18].trim());
				
				timePoints.add(timePoint);
				benefits.add(benefit);
				revenueParcels.add(revenueParcel);
				revenuePeoples.add(revenuePeople);
				costs.add(cost);
				travelDistances.add(distance);
				discounts.add(discount);
				parcelProcesseds.add(nbParcelProcessed);
				peopleProcesseds.add(nbPeopleProcessed);
				
				//line = in.nextLine();				
			}
			in.close();
			
			int d = timePoints.size()/(sz-1);
			ArrayList<Integer> r_timePoints = new ArrayList<Integer>();
			ArrayList<Double> r_benefits = new ArrayList<Double>();
			ArrayList<Double> r_travelDistances = new ArrayList<Double>();
			ArrayList<Double> r_revenuePeoples = new ArrayList<Double>();
			ArrayList<Double> r_revenueParcels = new ArrayList<Double>();
			ArrayList<Double> r_costs = new ArrayList<Double>();
			ArrayList<Double> r_discounts = new ArrayList<Double>();
			ArrayList<Integer> r_peopleProcesseds = new ArrayList<Integer>();
			ArrayList<Integer> r_parcelProcesseds = new ArrayList<Integer>();
			ArrayList<Double> r_requests = new ArrayList<Double>();
			for(int i = 0; i < timePoints.size(); i++){
				if(i % d == 0){
					r_timePoints.add(timePoints.get(i));
					r_benefits.add(benefits.get(i));
					r_travelDistances.add(travelDistances.get(i));
					r_revenueParcels.add(revenueParcels.get(i));
					r_revenuePeoples.add(revenuePeoples.get(i));
					r_costs.add(costs.get(i));
					r_discounts.add(discounts.get(i));
					r_peopleProcesseds.add(peopleProcesseds.get(i));
					r_parcelProcesseds.add(parcelProcesseds.get(i));
					double r = peopleProcesseds.get(i) + parcelProcesseds.get(i);
					r_requests.add(r);
				}
			}
			
			genPlotFile(mBenefitFile, r_timePoints, r_benefits);
			genPlotFile(mCostFile, r_timePoints, r_costs);
			genPlotFile(mDistanceFile, r_timePoints, r_travelDistances);
			genPlotFile(mRequestFile, r_timePoints, r_requests);
			
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public StatisticInfo collectReducedInfo(String dir, String filename, String algo, int sz){
		try{
			filename = dir + Statistic.dilimiter + filename;
			Scanner in = new Scanner(new File(filename));
			String line;
			line = in.nextLine();
			ArrayList<Integer> timePoints = new ArrayList<Integer>();
			ArrayList<Double> benefits = new ArrayList<Double>();
			ArrayList<Double> travelDistances = new ArrayList<Double>();
			ArrayList<Double> revenuePeoples = new ArrayList<Double>();
			ArrayList<Double> revenueParcels = new ArrayList<Double>();
			ArrayList<Double> costs = new ArrayList<Double>();
			ArrayList<Double> discounts = new ArrayList<Double>();
			ArrayList<Integer> peopleProcesseds = new ArrayList<Integer>();
			ArrayList<Integer> parcelProcesseds = new ArrayList<Integer>();
			//line = in.nextLine();
			while(in.hasNextLine()){
				line = in.nextLine();
				System.out.println(line);
				String[] s = line.split("\t");
				int timePoint = Integer.valueOf(s[0].trim());
				double benefit = Double.valueOf(s[1].trim());
				double revenueParcel = Double.valueOf(s[2].trim());
				double revenuePeople = Double.valueOf(s[3].trim());
				double cost = Double.valueOf(s[4].trim());
				double distance = Double.valueOf(s[5].trim());
				double discount = Double.valueOf(s[6].trim());
				int nbParcelProcessed = Integer.valueOf(s[17].trim());
				int nbPeopleProcessed = Integer.valueOf(s[18].trim());
				
				timePoints.add(timePoint);
				benefits.add(benefit);
				revenueParcels.add(revenueParcel);
				revenuePeoples.add(revenuePeople);
				costs.add(cost);
				travelDistances.add(distance);
				discounts.add(discount);
				parcelProcesseds.add(nbParcelProcessed);
				peopleProcesseds.add(nbPeopleProcessed);
				//System.out.println("linr = " + line + ",\n benefits(" + benefits.size() + ")  = " + benefits.get(benefits.size()-1));
				//line = in.nextLine();				
			}
			in.close();
			
			int d = timePoints.size()/(sz-1);
			ArrayList<Integer> r_timePoints = new ArrayList<Integer>();
			ArrayList<Double> r_benefits = new ArrayList<Double>();
			ArrayList<Double> r_travelDistances = new ArrayList<Double>();
			ArrayList<Double> r_revenuePeoples = new ArrayList<Double>();
			ArrayList<Double> r_revenueParcels = new ArrayList<Double>();
			ArrayList<Double> r_costs = new ArrayList<Double>();
			ArrayList<Double> r_discounts = new ArrayList<Double>();
			ArrayList<Integer> r_peopleProcesseds = new ArrayList<Integer>();
			ArrayList<Integer> r_parcelProcesseds = new ArrayList<Integer>();
			ArrayList<Double> r_requests = new ArrayList<Double>();
			for(int i = 0; i < timePoints.size(); i++){
				if(i % d == 0){
					r_timePoints.add(timePoints.get(i));
					r_benefits.add(benefits.get(i));
					r_travelDistances.add(travelDistances.get(i));
					r_revenueParcels.add(revenueParcels.get(i));
					r_revenuePeoples.add(revenuePeoples.get(i));
					r_costs.add(costs.get(i));
					r_discounts.add(discounts.get(i));
					r_peopleProcesseds.add(peopleProcesseds.get(i));
					r_parcelProcesseds.add(parcelProcesseds.get(i));
					double r = peopleProcesseds.get(i) + parcelProcesseds.get(i);
					r_requests.add(r);
					
					//System.out.println("benefit.get(" + i + ") = " + benefits.get(i) + ", " + r_benefits.get(r_benefits.size()-1));
				}
			}
			
			return new StatisticInfo(r_timePoints, r_benefits, r_travelDistances, r_revenuePeoples, 
					r_revenueParcels, r_costs, r_discounts, r_peopleProcesseds, r_parcelProcesseds);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}
	
	public void genPlotFile(String algo, int sz){
		String fn = algo + ".txt";
		String fnBenefit = algo + "_benefit" + ".m";
		String fnCost = algo + "_cost.m";
		String fnDistance = algo + "_distance.m";
		String fnReq = algo + "_requets.m";
		genPlotFile(fn, fnBenefit, fnCost, fnDistance, fnReq, sz);
	}
	public void genPlotFile(String[] algo, String fn, ArrayList<Integer>[] x, ArrayList<Double>[] y, String xlabel, String ylabel){
		try{
			PrintWriter out = new PrintWriter(fn);
			int n = x.length;
			for(int i = 0; i < n; i++){
				ArrayList<Integer> xi = x[i];
				ArrayList<Double> yi = y[i];
				String sx = "[";
				String sy = "[";
				for(int j = 0; j < xi.size()-1; j++){
					sx = sx + xi.get(j) + ",";
					sy = sy + yi.get(j) + ",";
				}
				sx = sx + xi.get(xi.size()-1) + "]";
				sy = sy + yi.get(yi.size()-1) + "]";
			
				out.println("x" + i + " = " + sx + ";");
				out.println("y" + i + " = " + sy + ";");
			}
			String s = "plot(";
			for(int i = 0; i < n-1; i++){
				s = s + "x" + i + "," + "y" + i + ",";
			}
			int i = n-1;
			s = s + "x" + i + "," + "y" + i + ");";
			out.println(s);
			
			s = "legend(";
			for(int  j = 0; j < algo.length-1; j++){
				s = s + "'" + algo[j] + "',"; 
			}
			s = s + "'" + algo[algo.length-1] + "','location','southeast');";
			
			out.println(s);
			
			out.println("xlabel('" + xlabel + "');");
			out.println("ylabel('" + ylabel + "');");
			
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void genPlotFile(String dir, String[] filename, String[] algo, int sz){
		StatisticInfo[] I = new StatisticInfo[algo.length];
		for(int i = 0; i < algo.length; i++){
			I[i] = collectReducedInfo(dir,filename[i], algo[i], sz);
		}
		String f_benefits = dir + Statistic.dilimiter + "benefits.m";
		String f_cost = dir + Statistic.dilimiter + "cost.m";
		String f_distance = dir + Statistic.dilimiter + "distance.m";
		//String f_request = "requests.m";
		ArrayList<Integer>[] x_benefits = new ArrayList[algo.length];
		ArrayList<Double>[] y_benefits = new ArrayList[algo.length];
		
		ArrayList<Integer>[] x_cost = new ArrayList[algo.length];
		ArrayList<Double>[] y_cost = new ArrayList[algo.length];
		
		ArrayList<Integer>[] x_distance = new ArrayList[algo.length];
		ArrayList<Double>[] y_distance = new ArrayList[algo.length];
		
		//ArrayList<Integer>[] x_request = new ArrayList[algo.length];
		//ArrayList<Integer>[] y_request = new ArrayList[algo.length];
		
		for(int i = 0; i < algo.length; i++){
			x_benefits[i] = I[i].timePoints;
			y_benefits[i] = I[i].benefits;
			x_cost[i] = I[i].timePoints;
			y_cost[i] = I[i].costs;
			x_distance[i] = I[i].timePoints;
			y_distance[i] = I[i].travelDistances;
			//x_request[i] = I[i].timePoints;			
		}
		genPlotFile(algo,f_benefits,x_benefits,y_benefits,"time (s)","benefits ($)");
		genPlotFile(algo,f_cost,x_cost,y_cost,"time (s)","cost ($)");
		genPlotFile(algo,f_distance,x_distance,y_distance,"time (s)","travel distance (km)");
	}

	public static void genPlot(String[] args){
		Statistic stat = new Statistic();
		String dir = "E:\\Project\\pbts\\git_project\\SPT\\SanFrancisco_std\\day1temp";
		String progressiveFN = "progressiveFile.txt";
		for(int i = 0; i < args.length; i++){
			if(args[i].equals("--dir"))
					dir = args[i+1];
			else if(args[i].equals("--progressiveFile"))
				progressiveFN = args[i+1];
		}
		
		System.out.println("Statistic, dir = " + dir);
		System.out.println("Statistic, progressiveFile = " + progressiveFN);
		
		ArrayList<String> L1 = new ArrayList<String>();
		ArrayList<String> L2 = new ArrayList<String>();

		try{
			Scanner in = new Scanner(new File(dir + Statistic.dilimiter + progressiveFN));
			while(in.hasNextLine()){
				String line = in.nextLine();
				if(line.equals("-1")) break;
				String[] s= line.split("\t");
				L1.add(s[0].trim());
				L2.add(s[1].trim());
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		String[] filename = new String[L1.size()];
		String[] algo = new String[L2.size()];
		for(int i = 0; i < L1.size(); i++){
			filename[i] = L1.get(i);
			algo[i] = L2.get(i);
		}
		//String[] filename = new String[]{"Naive","GreedyDirectNoExchange",
		//		"GreedySharingNoExchange","GreedySharingExchange"};
		
		//String[] algo = new String[]{"Naive","GreedyDirectNoExchange",
		//		"GreedySharingNoExchange","GreedySharingExchange"};
		
		//String[] algo = new String[]{"GreedyPeopleParcelInsertSharing-statistic.txt"};
		stat.genPlotFile(dir,filename, algo,200);
		
		System.out.println("Statistic, dir = " + dir);
		System.out.println("Statistic, progressiveFile = " + progressiveFN);

	}
	
	public AnalysisTemplate getStatisticInfo(String xmlFile){
		try{
			File f = new File(xmlFile);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(f);
			
			double benefits = 0;
			double traveldistance = 0;
			int passenger_requests = 0;
			int parcel_requests = 0;
			double revenue_passenger = 0;
			double revenue_parcel = 0;
			int nbShares = 0;
			double discount = 0;
			double fuel = 0;
			
			NodeList nl = doc.getElementsByTagName("total-benefits");
			benefits = Double.valueOf(doc.getElementsByTagName("total-benefits").item(0).getChildNodes().item(0).getNodeValue());
			traveldistance = Double.valueOf(doc.getElementsByTagName("travel-distance").item(0).getChildNodes().item(0).getNodeValue());
			passenger_requests = Integer.valueOf(doc.getElementsByTagName("served-people").item(0).getChildNodes().item(0).getNodeValue());
			parcel_requests = Integer.valueOf(doc.getElementsByTagName("served-parcels").item(0).getChildNodes().item(0).getNodeValue());
			revenue_passenger = Double.valueOf(doc.getElementsByTagName("revenue-people").item(0).getChildNodes().item(0).getNodeValue());
			revenue_parcel = Double.valueOf(doc.getElementsByTagName("revenue-parcel").item(0).getChildNodes().item(0).getNodeValue());
			nbShares = Integer.valueOf(doc.getElementsByTagName("shared-people-services").item(0).getChildNodes().item(0).getNodeValue());
			discount = Double.valueOf(doc.getElementsByTagName("total-discount").item(0).getChildNodes().item(0).getNodeValue());
			fuel = Double.valueOf(doc.getElementsByTagName("fuel").item(0).getChildNodes().item(0).getNodeValue());
			
			
			AnalysisTemplate at = new AnalysisTemplate();
			at.benefits = benefits;
			at.travelDistance = traveldistance;
			at.nbServedPassengers = passenger_requests;
			at.nbServedParcels = parcel_requests;
			at.revenueParcels = revenue_parcel;
			at.revenuePassengers = revenue_passenger;
			at.discount = discount;
			at.fuelCost = fuel;
			at.nbSharedPeopleService = nbShares;
			
			return at;
			//System.out.println(benefitsStr);
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}
	public static void genStatisticXMLInfo(String[] args){
		Statistic stat = new Statistic();
		String dir = "E:\\Project\\pbts\\git_project\\SPT\\SanFrancisco_std\\day1temp";
		String xmlFN = "xmlFiles.txt";
		for(int i = 0; i < args.length; i++){
			if(args[i].equals("--dir"))
					dir = args[i+1];
			else if(args[i].equals("--xmlFile"))
				xmlFN = args[i+1];
		}
		
		System.out.println("Statistic, dir = " + dir);
		System.out.println("Statistic, xmlFile = " + xmlFN);
		ArrayList<String> L1 = new ArrayList<String>();
		ArrayList<String> L2 = new ArrayList<String>();

		try{
			Scanner in = new Scanner(new File(dir + Statistic.dilimiter + xmlFN));
			while(in.hasNextLine()){
				String line = in.nextLine();
				if(line.equals("-1")) break;
				String[] s= line.split("\t");
				L1.add(s[0].trim());
				L2.add(s[1].trim());
			}
			
			for(int i = 0; i < L1.size(); i++){
				String xmlFile = L1.get(i);
				String algo = L2.get(i);
				AnalysisTemplate at = stat.getStatisticInfo(dir + Statistic.dilimiter + xmlFile);
				System.out.println(algo + "\n" + at.toString());
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		String[] filename = new String[L1.size()];
		String[] algo = new String[L2.size()];
		for(int i = 0; i < L1.size(); i++){
			filename[i] = L1.get(i);
			algo[i] = L2.get(i);
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//Statistic.genPlot(args);
		//Statistic.genStatisticXMLInfo(args);
		String dir = "E:\\Project\\pbts\\git_project\\SPT\\SanFrancisco_std\\outputtemp\\day21\\benefits.m";
		String dir2 = "E:\\Project\\pbts\\git_project\\SPT\\SanFrancisco_std\\outputtemp\\day21\\out.txt";
		try{
			Scanner in = new Scanner(new File(dir));
			String id = in.nextLine();
			String[] x0 = id.split(",");
			id = in.nextLine();
			String[] y0 = id.split(",");
			id = in.nextLine();
			String[] x1 = id.split(",");
			id = in.nextLine();
			String[] y1 = id.split(",");
			id = in.nextLine();
			String[] x2 = id.split(",");
			id = in.nextLine();
			String[] y2 = id.split(",");
			in.close();
			PrintWriter out = new PrintWriter(dir2);
			String pr = "";
			for(int i = 0; i < x0.length; i++){
				pr +="[" + x0[i] + "," + y0[i] + "],";
			}
			out.println(pr);
			pr = "";
			for(int i = 0; i < x0.length; i++){
				pr +="[" + x1[i] + "," + y1[i] + "],";
			}
			out.println(pr);
			pr = "";
			for(int i = 0; i < x0.length; i++){
				pr +="[" + x2[i] + "," + y2[i] + "],";
			}
			out.println(pr);
			out.close();
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
