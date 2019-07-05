package pbts.statistic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import pbts.entities.PeopleRequest;
import pbts.simulation.SimulatorTimeUnit;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.stat.inference.*;

public class StatisticInfo {
	ArrayList<Integer> timePoints = null;//new ArrayList<Integer>();
	ArrayList<Double> benefits = null;//new ArrayList<Double>();
	ArrayList<Double> travelDistances = null;//new ArrayList<Double>();
	ArrayList<Double> revenuePeoples = null;//new ArrayList<Double>();
	ArrayList<Double> revenueParcels = null;//new ArrayList<Double>();
	ArrayList<Double> costs = null;//new ArrayList<Double>();
	ArrayList<Double> discounts = null;//new ArrayList<Double>();
	ArrayList<Integer> peopleProcesseds = null;//new ArrayList<Integer>();
	ArrayList<Integer> parcelProcesseds = null;//new ArrayList<Integer>();
	public ArrayList<PeopleRequest> allPeopleRequests;
	public ArrayList<Double> dividedPoints;
	double minSumErr = 10000000;
	ArrayList<Double> dPoints;
	public StatisticInfo(){
		this.allPeopleRequests = new ArrayList<PeopleRequest>();
		this.dividedPoints = new ArrayList<Double>();
	}
	public StatisticInfo(ArrayList<Integer> timePoints, 
			ArrayList<Double> benefits,
			ArrayList<Double> travelDistances, 
			ArrayList<Double> revenuePeoples,
			ArrayList<Double> revenueParcels, 
			ArrayList<Double> costs,
			ArrayList<Double> discounts, 
			ArrayList<Integer> peopleProcesseds, 
			ArrayList<Integer> parcelProcesseds){
		
		this.timePoints = timePoints;
		this.benefits = benefits;
		this.travelDistances = travelDistances;
		this.revenueParcels = revenueParcels;
		this.revenuePeoples = revenuePeoples;
		this.costs = costs;
		this.discounts = discounts;
		this.peopleProcesseds = peopleProcesseds;
		this.parcelProcesseds = parcelProcesseds;
		this.allPeopleRequests = new ArrayList<PeopleRequest>();
		
	}
	
	public void createRequestInPeriodsFile(String dir, String city, int nbDay, String type){
		int[] listPeriods = {4, 24, 48, 96, 288, 1440, 86400};
		int[] listMeasures = {21600, 3600, 1800, 900, 300, 60, 1};
		int[] period4 = new int[4];
		int[] period24 = new int[24];
		int[] period48 = new int[48];
		int[] period96 = new int[96];
		int[] period288 = new int[288];
		int[] period1440 = new int[1440];
		int[] period86400 = new int[86400];
		for(int i = 0; i < 4; i++)
			period4[i] = 0;
		for(int i = 0; i < 24; i++)
			period24[i] = 0;
		for(int i = 0; i < 48; i++)
			period48[i] = 0;
		for(int i = 0; i < 96; i++)
			period96[i] = 0;
		for(int i = 0; i < 288; i++)
			period288[i] = 0;
		for(int i = 0; i < 1440; i++)
			period1440[i] = 0;
		for(int i = 0; i < 86400; i++)
			period86400[i] = 0;
		for(int i = 0; i < allPeopleRequests.size(); i++){
			PeopleRequest pr = allPeopleRequests.get(i);
			period4[pr.timePoint/21600]++;
			period24[pr.timePoint/3600]++;
			period48[pr.timePoint/1800]++;
			period96[pr.timePoint/900]++;
			period288[pr.timePoint/300]++;
			period1440[pr.timePoint/60]++;
			period86400[pr.timePoint/1]++;
		}
		try{
			String datadir = dir + "\\taxiDemands\\" + city + "\\reqs-period4-" + type + ".txt";
			PrintWriter out= new PrintWriter(datadir);
			for(int i = 0; i < 4; i++)
				out.println(i + " " + period4[i]/nbDay);
			out.close();
		}catch(Exception e){
			
		}
		try{
			String datadir = dir + "\\taxiDemands\\" + city + "\\reqs-period24-" + type + ".txt";
			PrintWriter out= new PrintWriter(datadir);
			for(int i = 0; i < 24; i++)
				out.println(i + " " + period24[i]/nbDay);
			out.close();
		}catch(Exception e){
			
		}
		try{
			String datadir = dir + "\\taxiDemands\\" + city + "\\reqs-period48-" + type + ".txt";
			PrintWriter out= new PrintWriter(datadir);
			for(int i = 0; i < 48; i++)
				out.println(i + " " + period48[i]/nbDay);
			out.close();
		}catch(Exception e){
			
		}
		try{
			String datadir = dir + "\\taxiDemands\\" + city + "\\reqs-period96-" + type + ".txt";
			PrintWriter out= new PrintWriter(datadir);
			for(int i = 0; i < 96; i++)
				out.println(i + " " + period96[i]/nbDay);
			out.close();
		}catch(Exception e){
			
		}
		try{
			String datadir = dir + "\\taxiDemands\\" + city + "\\reqs-period288-" + type + ".txt";
			PrintWriter out= new PrintWriter(datadir);
			for(int i = 0; i < 288; i++)
				out.println(i + " " + period288[i]/nbDay);
			out.close();
		}catch(Exception e){
			
		}
		try{
			String datadir = dir + "\\taxiDemands\\" + city + "\\reqs-period1440-" + type + ".txt";
			PrintWriter out= new PrintWriter(datadir);
			for(int i = 0; i < 1440; i++)
				out.println(i + " " + period1440[i]/nbDay);
			out.close();
		}catch(Exception e){
			
		}
		try{
			String datadir = dir + "\\taxiDemands\\" + city + "\\reqs-period86400-" + type + ".txt";
			PrintWriter out= new PrintWriter(datadir);
			for(int i = 0; i < 86400; i++)
				out.println(i + " " + period86400[i]/nbDay);
			out.close();
		}catch(Exception e){
			
		}
		
	}

	public void createRequestInPeriodsFileForTestDiggle(SimulatorTimeUnit sim, 
			String dir, String city){
		int[] listPeriods = {288};
		int[] period288 = new int[288];

		for(int i = 0; i < 288; i++)
			period288[i] = 0;

		String lat = "";
		String lng = "";
		String dat = "";
		int k = 0;
		Random r = new Random();
		for(int i = 0; i < allPeopleRequests.size(); i++){
			PeopleRequest pr = allPeopleRequests.get(i);
			int period = pr.timePoint/300;
			int a = r.nextInt(10);
			if(a > 4 && a < 6){
				lat += sim.map.mLatLng.get(pr.pickupLocationID).lat + "L,";
				lng += sim.map.mLatLng.get(pr.pickupLocationID).lng + "L,";
				dat += period + "L,";
				k++;
			}
		}
		System.out.println(k);
		try{
			String datadir = dir + "\\taxiDemands\\" + city + "\\Diggle-reqs-period288-" + city + ".txt";
			PrintWriter out= new PrintWriter(datadir);
			out.println(lat + lng + dat);
			out.close();
		}catch(Exception e){
			
		}
		
	}
	public void createRequestInPeriodsFileTrainTest(String dir){
		int[] listPeriods = {4, 24, 96, 288, 1440, 86400};
		int[] listMeasures = {21600, 3600, 900, 300, 60, 1};
		int[] period4 = new int[4];
		int[] period24 = new int[24];
		int[] period96 = new int[96];
		int[] period288 = new int[288];
		int[] period1440 = new int[1440];
		int[] period86400 = new int[86400];
		for(int i = 0; i < 4; i++)
			period4[i] = 0;
		for(int i = 0; i < 24; i++)
			period24[i] = 0;
		for(int i = 0; i < 96; i++)
			period96[i] = 0;
		for(int i = 0; i < 288; i++)
			period288[i] = 0;
		for(int i = 0; i < 1440; i++)
			period1440[i] = 0;
		for(int i = 0; i < 86400; i++)
			period86400[i] = 0;
		for(int i = 0; i < allPeopleRequests.size(); i++){
			PeopleRequest pr = allPeopleRequests.get(i);
			int ix = pr.timePoint/21600; 
			period4[ix]++;
			ix = pr.timePoint/3600;
			period24[ix]++;
			ix = pr.timePoint/900;
			period96[ix]++;
			ix = pr.timePoint/300;
			period288[ix]++;
			ix = pr.timePoint/60;
			period1440[ix]++;
			ix = pr.timePoint;
			period86400[ix]++;
		}
		try{
			String datadir = dir + "\\taxiDemands\\reqs-period4-avg-day1-Train.txt";
			PrintWriter out= new PrintWriter(datadir);
			for(int i = 0; i < 4; i++)
				out.println(period4[i]);
			out.close();
		}catch(Exception e){
			
		}
		try{
			String datadir = dir + "\\taxiDemands\\reqs-period24-avg-day1-Train.txt";
			PrintWriter out= new PrintWriter(datadir);
			for(int i = 0; i < 24; i++)
				out.println(period24[i]);
			out.close();
		}catch(Exception e){
			
		}
		try{
			String datadir = dir + "\\taxiDemands\\reqs-period96-avg-day1-Train.txt";
			PrintWriter out= new PrintWriter(datadir);
			for(int i = 0; i < 96; i++)
				out.println((int)period96[i]);
			out.close();
		}catch(Exception e){
			
		}
		try{
			String datadir = dir + "\\taxiDemands\\reqs-period288-avg-day1-Train.txt";
			PrintWriter out= new PrintWriter(datadir);
			for(int i = 0; i < 288; i++)
				out.println(period288[i]);
			out.close();
		}catch(Exception e){
			
		}

//		try{
//			String datadir = dir + "\\taxiDemands\\reqs-period86400-avg-day1-21-Train.txt";
//			PrintWriter out= new PrintWriter(datadir);
//			for(int i = 0; i < 86400; i++)
//				out.println(period86400[i]);
//			out.close();
//		}catch(Exception e){
//			
//		}
		
	}
	
	public void createRequestInPeriodsFileForCheckHomoTrainTest(String dir){
		int[] listPeriods = {4, 24, 96, 288, 1440, 86400};
		int[] listMeasures = {21600, 3600, 900, 300, 60, 1};
		int[] period4 = new int[4];
		int[] period24 = new int[24];
		int[] period96 = new int[96];
		int[] period288 = new int[288];
		int[] period1440 = new int[1440];
		int[] period86400 = new int[86400];
		for(int i = 0; i < 4; i++)
			period4[i] = 0;
		for(int i = 0; i < 24; i++)
			period24[i] = 0;
		for(int i = 0; i < 96; i++)
			period96[i] = 0;
		for(int i = 0; i < 288; i++)
			period288[i] = 0;
		for(int i = 0; i < 1440; i++)
			period1440[i] = 0;
		for(int i = 0; i < 86400; i++)
			period86400[i] = 0;
		for(int i = 0; i < allPeopleRequests.size(); i++){
			PeopleRequest pr = allPeopleRequests.get(i);
			int ix = pr.timePoint/21600; 
			period4[ix]++;
			ix = pr.timePoint/3600;
			period24[ix]++;
			ix = pr.timePoint/900;
			period96[ix]++;
			ix = pr.timePoint/300;
			period288[ix]++;
			ix = pr.timePoint/60;
			period1440[ix]++;
			ix = pr.timePoint;
			period86400[ix]++;
		}
//		try{
//			String datadir = dir + "\\taxiDemands\\reqs-period4-avg-day1-21-Train.txt";
//			PrintWriter out= new PrintWriter(datadir);
//			for(int i = 0; i < 4; i++)
//				out.println(period4[i]/21);
//			out.close();
//		}catch(Exception e){
//			
//		}
//		try{
//			String datadir = dir + "\\taxiDemands\\reqs-period24-avg-day1-21-Train.txt";
//			PrintWriter out= new PrintWriter(datadir);
//			for(int i = 0; i < 24; i++)
//				out.println(period24[i]/21);
//			out.close();
//		}catch(Exception e){
//			
//		}
//		try{
//			String datadir = dir + "\\taxiDemands\\reqs-period96-avg-day1-21-Train.txt";
//			PrintWriter out= new PrintWriter(datadir);
//			for(int i = 0; i < 96; i++)
//				out.println(period96[i]/21);
//			out.close();
//		}catch(Exception e){
//			
//		}
//		try{
//			String datadir = dir + "\\taxiDemands\\reqs-period288-avg-day1-21-Train.txt";
//			PrintWriter out= new PrintWriter(datadir);
//			for(int i = 0; i < 288; i++)
//				out.println(period288[i]/21);
//			out.close();
//		}catch(Exception e){
//			
//		}
		try{
			String datadir = dir + "\\taxiDemands\\reqs-onemins-4periods-day1-Train.txt";
			PrintWriter out= new PrintWriter(datadir);
			int max = 0;
		
			for(int i = 0; i < 1440; i++){
				if(period1440[i] > max)
					max = period1440[i];
			}
			for(int i = 0; i < max; i++){
				for(int j = 0; j < 4; j++){
					int cnt = 0;
					for(int k = j *360; k < (j+1)*360; k++){
						if(period1440[k] == i)
							cnt++;
					}
					out.println(i + " " + cnt);
				}
			}
			out.close();
		}catch(Exception e){
			
		}
//		try{
//			String datadir = dir + "\\taxiDemands\\reqs-period86400-avg-day1-21-Train.txt";
//			PrintWriter out= new PrintWriter(datadir);
//			for(int i = 0; i < 86400; i++)
//				out.println(period86400[i]);
//			out.close();
//		}catch(Exception e){
//			
//		}
		
	}
	//tao file thong ke so request trong 1 giay va thoi gian den cua cac request
	public void createSecondToRequests(String dir, int day, String city){
		try{
			String datadir = null;
			PrintWriter out;
			int min = 19 * 3600;
			int max = 20 * 3600;
			int[] L = {300, 600, 900, 1800, 3600};
			for(int k = 0; k < L.length; k++){
				int g = 3600/L[k];
				for(int m = 0; m < g; m++){
					int sd = min + m*L[k];
					datadir = dir + "\\taxiDemands\\" + city + "\\timepoint-day" + day + "-p" + sd/L[k] + "n" + 86400/L[k] + "-kst.txt";
					out = new PrintWriter(datadir);
					double t = 0;
					for(int i = 0; i < allPeopleRequests.size(); i++){
						if(allPeopleRequests.get(i).timePoint != t && allPeopleRequests.get(i).timePoint >= sd && allPeopleRequests.get(i).timePoint < (sd+L[k])){
							double a = allPeopleRequests.get(i).timePoint - sd;
							double delta = (double)(a/L[k]);
							out.println(delta);
							t = allPeopleRequests.get(i).timePoint;
						}
					}
					out.close();
				}
			}
			datadir = dir + "\\taxiDemands\\" + city + "\\second-to-requests-day" + day + ".txt";
			out= new PrintWriter(datadir);
			int[] reqs = new int[86400];
			for(int i = 0; i < 86400; i++)
				reqs[i] = 0;
			for(int i = 0; i < allPeopleRequests.size(); i++){
				reqs[allPeopleRequests.get(i).timePoint]++;
			}
			
			for(int i = 0; i < 86400; i++){
				out.println(reqs[i]);
			}
			out.close();
		}
		catch(Exception e){
			
		}
	}
	
	//tinh Rij theo Brown 2005
	public void createRij(String dir, int day, String city){
		try{
			int[] L = {300, 600, 900, 1800, 3600};
			for(int g = 0; g < L.length;g++){
				String datadir = dir + "\\taxiDemands\\" + city + "\\Rij-day" + day + "-" + (86400/L[g]) + "periods-LogTest.txt";
				PrintWriter out= new PrintWriter(datadir);
				
				int blocks = 86400 / L[g];
				int[] J = new int[blocks];
				HashMap<Integer, ArrayList<Integer>> T = new HashMap<Integer, ArrayList<Integer>>();
				for(int i = 0; i < blocks; i++){
					ArrayList<Integer> Tij = new ArrayList<Integer>();
					Tij.add(0);
					T.put(i, Tij);
					J[i] = 0;
				}
				for(int i = 0; i < allPeopleRequests.size(); i++){
					int pr = allPeopleRequests.get(i).timePoint / L[g];
					ArrayList<Integer> Tij = T.get(pr);
					int t = allPeopleRequests.get(i).timePoint - pr*L[g];
					if(!Tij.contains(t)){
						J[pr]++;
						Tij.add(t);
					}
					T.put(pr, Tij);
				}
				HashMap<Integer, ArrayList<Double>> R = new HashMap<Integer, ArrayList<Double>>();
				for(int i = 0; i < blocks; i++){
					ArrayList<Double> rij = new ArrayList<Double>();
					rij.add(0.0);
					R.put(i, rij);
				}
				for(int i = 0; i < blocks; i++){
					ArrayList<Integer> Tij = T.get(i);
					for(int j = 1; j <= J[i]; j++){
						double lg = (double)Math.log((double)(L[g]-T.get(i).get(j))/(L[g]-T.get(i).get(j-1)));
						double rj = (double)(J[i] + 1 - j) * (0 - lg);
						ArrayList<Double> rij = R.get(i);
						rij.add(rj);
						R.put(i, rij);
	 				}
				}
				for(int i = 0; i < blocks; i++){
					out.println("blocks i = " + i);
					for(int j = 1; j < R.get(i).size();j++){
						out.println(R.get(i).get(j));
					}
				}
				out.close();
			}
		}
		catch(Exception e){
			
		}
	}
	public void readPortoDataFile(String fin){
		try{
			Scanner in = new Scanner(new File(fin));
			
			while(true){
				int time = in.nextInt();
				if(time == -1)
					break;
				PeopleRequest pr = new PeopleRequest(-1, -1);
				pr.timePoint = time;
				this.allPeopleRequests.add(pr);
			}
		}catch(Exception e){
			
		}
	}
	
	public double ExponentialKolmogorovSmirnovTest(int m, ArrayList<Double> timePoint){
		if(timePoint.size() <= 2)
			return -2;
		double T = 72000/m;
		double[] tp = new double[timePoint.size()];
		tp[0] = 0;
		double sum = 0;
		for(int k = 1; k < timePoint.size(); k++){
			tp[k] = (double)timePoint.get(k)/T - tp[k-1];
			sum = sum + tp[k];
		}
		
		RealDistribution exSamples = new ExponentialDistribution(sum/timePoint.size());
		KolmogorovSmirnovTest kst = new KolmogorovSmirnovTest();
		boolean is = kst.kolmogorovSmirnovTest(exSamples, tp, 0.1);
		if(is){
			System.out.println("m = " + m + ", period = " + timePoint.get(0)/m + " is poisson = " + is);
			return kst.kolmogorovSmirnovStatistic(exSamples, tp);
		}
		else
			return -1;
	}
	
	public double CUKolmogorovSmirnovTest(int m, ArrayList<Double> timePoint){
		if(timePoint.size() <= 2)
			return -2;
		double T = 72000/m;
		double[] tp = new double[timePoint.size()];
		for(int k = 0; k < timePoint.size(); k++){
			tp[k] = (double)timePoint.get(k);
		}
		
		RealDistribution uniSamples = new UniformRealDistribution(0,T);
		KolmogorovSmirnovTest kst = new KolmogorovSmirnovTest();
		boolean is = kst.kolmogorovSmirnovTest(uniSamples, tp, 0.1);
		if(is){
			//System.out.println("m = " + m + ", period = " + timePoint.get(0)/m + " is poisson = " + is);
			return kst.kolmogorovSmirnovStatistic(uniSamples, tp);
		}
		else
			return -1;
	}
	
	public void evaluateNbPeriods(String dir, String day, String city){
		double minD = Double.MAX_VALUE;
		int maxTime = 86400;
		int optPeriods = -1;
		try{
			String datadir = dir + "\\taxiDemands\\" + city + "\\evaluate" + day + "-CULogTest.txt";
			PrintWriter out= new PrintWriter(datadir);
			out.println("city = " + city + ", day = " + day);
			out.close();
		}
		catch(Exception e){
			
		}
		for(int m = 1; m <= 150; m++){
			try{
				String datadir = dir + "\\taxiDemands\\" + city + "\\evaluate" + day + "-CULogTest.txt";
				PrintWriter out= new PrintWriter(new FileOutputStream(datadir, true));
				out.println("m = " + m + "==============================================");
				out.close();
			}
			catch(Exception e){
				
			}
			double T = maxTime/m;
			int valid = 0;
			double D = 0;
			for(int pr = 0; pr < m; pr++){
				ArrayList<Double> tpList = new ArrayList<Double>();
				for(int i = 0; i < allPeopleRequests.size(); i++){
					double tp = allPeopleRequests.get(i).timePoint;
					if(tp >= pr*T && tp < (pr+1)*T){
						tp = tp - pr*T;
						if(!tpList.contains(tp))
							tpList.add(tp);
					}
				}
				double Di = CUKolmogorovSmirnovTest(m, tpList);
				//double Di = CUKolmogorovSmirnovTestRandomDividedPoints(T, tpList);
				if(Di < 0)
					valid++;
				else
					D += Di;
				try{
					String datadir = dir + "\\taxiDemands\\" + city + "\\evaluate" + day + "-CULogTest.txt";
					PrintWriter out= new PrintWriter(new FileOutputStream(datadir, true));
					out.println("m = " + m + ", pr = " + pr + ", Di = " + Di + ", valid = " + valid + ", rate = " + valid*100/m);
					out.close();
				}
				catch(Exception e){
					
				}
			}
			//D = D/m;
			if(valid < 0.3*m){
				minD = D;
				optPeriods = m;
				System.out.println("minD = " + minD + ", opt = " + optPeriods);
			}
		}
		
		int T = 300;
		int np = maxTime/T;
		int[] smoothPeriods = new int[np];
		for(int i = 0; i < np; i++)
			smoothPeriods[i] = 0;
		for(int i = 0; i < allPeopleRequests.size(); i++){
			int pr = allPeopleRequests.get(i).timePoint/T;
			smoothPeriods[pr]++;
		}
		try{
			String datadir = dir + "\\taxiDemands\\" + city + "\\EvaluateDividedPoint-evaluate" + day + "-" + T + "-CULogTest.txt";
			PrintWriter out= new PrintWriter(datadir);
			out.println("city = " + city + ", day = " + day + ", T = " + T);
			String str = "";
			for(int i = 0; i < smoothPeriods.length; i++)
				str = str + smoothPeriods[i] +", ";
			out.println(str);
			out.close();
		}
		catch(Exception e){
			
		}
		int length = maxTime/optPeriods;
		for(int i = 1; i < maxTime - 1; i++) {
			if(i % length == 0)
				dividedPoints.add((double)i);
		}
		double err = linearParameters(dir, day, city, maxTime, smoothPeriods, "Evaluate");
		try{
			String datadir = dir + "\\taxiDemands\\" + city + "\\evaluate" + day + "-CULogTest.txt";
			PrintWriter out= new PrintWriter(new FileOutputStream(datadir, true));
			out.println("minD = " + minD + ", opt = " + optPeriods);
			out.close();
			System.out.println("minD = " + minD + ", opt = " + optPeriods);
		}
		catch(Exception e){
			
		}
	}
	
	public double CUKolmogorovSmirnovTestRandomDividedPoints(double T, ArrayList<Double> timePoint){
		if(timePoint.size() <= 2)
			return -2;
		double[] tp = new double[timePoint.size()];
		for(int k = 0; k < timePoint.size(); k++){
			tp[k] = (double)timePoint.get(k)/T;
		}
		
		RealDistribution uniSamples = new UniformRealDistribution();
		KolmogorovSmirnovTest kst = new KolmogorovSmirnovTest();
		boolean is = kst.kolmogorovSmirnovTest(uniSamples, tp, 0.1);
		if(is){
			//System.out.println("T = " + T + " is poisson = " + is);
			return kst.kolmogorovSmirnovStatistic(uniSamples, tp);
		}
		else
			return -1;
	}
	
	public void dividedInterval(String dir, String city, String day, int l, int u, int count){
		Random r = new Random();
		int point = 0;
		if(u-l <= 2000)
			return;
		while(point <= 900 || u - l - point <= 900) {
			point = r.nextInt((u-l));
		}
		point = point + l;

		ArrayList<Double> tpList1 = new ArrayList<Double>();
		ArrayList<Double> tpList2 = new ArrayList<Double>();
		for(int i = 0; i < allPeopleRequests.size(); i++){
			double tp = allPeopleRequests.get(i).timePoint;
			if(tp >= l && tp < point){
				tp = tp - l;
				if(!tpList1.contains(tp))
					tpList1.add(tp);
			}
			if(tp >= point && tp < u){
				tp = tp - point;
				if(!tpList2.contains(tp))
					tpList2.add(tp);
			}
		}

		double D1 = CUKolmogorovSmirnovTestRandomDividedPoints(point - l, tpList1);
		double D2 = CUKolmogorovSmirnovTestRandomDividedPoints(u - point, tpList2);
		
/*		try{
			String datadir = dir + "\\taxiDemands\\" + city + "\\RandomDividedPoint-evaluate" + day + "-CULogTest.txt";
			PrintWriter out= new PrintWriter(new FileOutputStream(datadir, true));
			out.println("nb divided points = " + dividedPoints.size() + ", D1 = " + D1 + ", D2 = " + D2);
			out.close();
		}
		catch(Exception e){
			
		}*/
		
		if(D1 < 0 || D2 < 0){
			if(count < 700){
				dividedInterval(dir, city, day, l, u, count + 1);
			}
		}
		else{
			dividedPoints.add((double)point);
			int T = 300;
			int np = 86400/300;
			int[] smoothPeriods = new int[np];
			for(int i = 0; i < np; i++)
				smoothPeriods[i] = 0;
			for(int i = 0; i < allPeopleRequests.size(); i++){
				int pr = allPeopleRequests.get(i).timePoint/300;
				smoothPeriods[pr]++;
			}
			double err = linearParameters(dir, day, city, 86400, smoothPeriods, "Random");
			if(err < minSumErr){
				System.out.println("nbP = " + dividedPoints.size() + ", sum errs = " + err);
				minSumErr = err;
				dPoints = new ArrayList<Double>(dividedPoints);
				try{
					String datadir = dir + "\\taxiDemands\\" + city + "\\RandomDividedPoint-evaluate" + day + "-" + T + "-CULogTest.txt";
					PrintWriter out= new PrintWriter(new FileOutputStream(datadir, true));
					out.println("nb divided points = " + dividedPoints.size() + ", min sum errors = " + minSumErr);
					for(int i = 0; i < dividedPoints.size(); i++)
						out.println(dividedPoints.get(i));
					out.close();
				}
				catch(Exception e){
					
				}
			}
			dividedInterval(dir, city, day, l, point, 0);
			dividedInterval(dir, city, day, point, u, 0);
		}
	}
	
	public void evaluateNbPeriodsRandomDividedPoints(String dir, String day, String city, int maxTime){
		int T = 300;
		int np = maxTime/T;
		int[] smoothPeriods = new int[np];
		for(int i = 0; i < np; i++)
			smoothPeriods[i] = 0;
		for(int i = 0; i < allPeopleRequests.size(); i++){
			int pr = allPeopleRequests.get(i).timePoint/T;
			smoothPeriods[pr]++;
		}
		try{
			String datadir = dir + "\\taxiDemands\\" + city + "\\RandomDividedPoint-evaluate" + day + "-" + T + "-CULogTest.txt";
			PrintWriter out= new PrintWriter(datadir);
			out.println("city = " + city + ", day = " + day + ", T = " + T);
			String str = "";
			for(int i = 0; i < smoothPeriods.length; i++)
				str = str + smoothPeriods[i] +", ";
			out.println(str);
			out.close();
		}
		catch(Exception e){
			
		}
		int nbP = 0;

		
		for(int i = 0; i < 1000; i++){
			dividedInterval(dir, city, day, 0, maxTime, 0);
			int nb = dividedPoints.size();
			double err = linearParameters(dir, day, city, maxTime, smoothPeriods, "Random");
			if(err < minSumErr){
				System.out.println("nbP = " + nb + ", sum errs = " + err);
				minSumErr = err;
				dPoints = new ArrayList<Double>(dividedPoints);
			}
			dividedPoints.clear();
		}
		try{
			String datadir = dir + "\\taxiDemands\\" + city + "\\RandomDividedPoint-evaluate" + day + "-" + T + "-CULogTest.txt";
			PrintWriter out= new PrintWriter(new FileOutputStream(datadir, true));
			out.println("nb divided points = " + dividedPoints.size() + ", min sum errors = " + minSumErr);
			out.close();
		}
		catch(Exception e){
			
		}
		
	}
	
	public double linearParameters(String dir, String day, String city, int maxTime, int[] smoothPeriods, String type){
		int m = dividedPoints.size();
		Collections.sort(dividedPoints);
		int T = 300;
		int nb = maxTime/T;
		
		double[] ak = new double[m + 1];
		double[] bk = new double[m + 1];
		dividedPoints.add(maxTime-1.0);
		dividedPoints.add(0, 0.0);
		double x1 = 0;
		double y1 = smoothPeriods[0];
		double x2 = dividedPoints.get(1);
		double y2 = smoothPeriods[(int)x2/T];
		ak[0] = (y2-y1)/(x2);
		bk[0] = y1;
		
		for(int i = 1; i <= m; i++){
			x1 = x2;
			y1 = y2;
			x2 = dividedPoints.get(i+1);
			y2 = smoothPeriods[(int)x2/T];
			ak[i] = (y2 - y1) / (x2 - x1);
			bk[i] = (y1 * x2 - y2 * x1) / (x2 - x1);
		}
//		double xL = maxTime-1;
//		double yL = smoothPeriods[nb-1];
//		ak[m+1] = (yL - y2) / (xL - x2);
//		bk[m+1] = (y2 * xL - yL * x2) / (xL - x2);
		
		double sumErrors = 0;
		int pr = 0;
		int t = 0;
		for(int i = 0; i < nb; i++){
			for(int k = t; k < dividedPoints.size() - 1; k++){
				double l = dividedPoints.get(k);
				double u = dividedPoints.get(k+1);
				if(i*T >= l && i*T < u){
					pr = k;
					break;
				}
			}
			t = pr;
			double y0 = ak[pr]*i*T + bk[pr];
			sumErrors += Math.abs(y0 - smoothPeriods[i]);
		}
		
		try{
			String datadir = dir + "\\taxiDemands\\" + city + "\\"+ type + "DividedPoint-evaluate" + day + "-" + T + "-CULogTest.txt";
			PrintWriter out= new PrintWriter(new FileOutputStream(datadir, true));
			out.println("nb divided points = " + dividedPoints.size() + ", min sum errors = " + sumErrors);
			for(int i = 0; i < dividedPoints.size(); i++)
				out.println(dividedPoints.get(i));
			
			out.println("==================ak, bk====================");
			for(int i = 0; i < m+1; i++){
				out.println(ak[i] + "*x + " + bk[i]);
			}
			out.close();
		}
		catch(Exception e){
			
		}
		dividedPoints.remove(dividedPoints.size()-1);
		dividedPoints.remove(0);
		return sumErrors;
	}
	
	public void testHomogeneity(String dir, String day, String city){
		try{
			String datadir = dir + "\\taxiDemands\\" + city + "\\testHomo.txt";
			PrintWriter out= new PrintWriter(datadir);
			out.println("city = " + city + ", day = " + day);
			out.close();
		}
		catch(Exception e){
			
		}
		int n = allPeopleRequests.size();
		double sigmaX2 = 0;
		double sigmaT = 0;
		for(int i = 1; i < n-1; i++){
			if(allPeopleRequests.get(i).timePoint == allPeopleRequests.get(i-1).timePoint)
				continue;
			sigmaX2 += Math.pow((allPeopleRequests.get(i+1).timePoint - 2*allPeopleRequests.get(i).timePoint
					+ allPeopleRequests.get(i-1).timePoint), 2);
			sigmaT += allPeopleRequests.get(i).timePoint;
		}
		sigmaT += allPeopleRequests.get(0).timePoint;
		sigmaT -= allPeopleRequests.get(n-1).timePoint;
		sigmaT -= (n-1)*allPeopleRequests.get(n-1).timePoint/2;
		sigmaX2 += Math.pow((allPeopleRequests.get(1).timePoint - 2*allPeopleRequests.get(0).timePoint
				+ 0), 2);
		double sigma = Math.sqrt(sigmaX2 /(2*n - 2));
		double LR = sigmaT/(n*sigma*Math.sqrt((n-1)/12));
		try{
			String datadir = dir + "\\taxiDemands\\" + city + "\\testHomo.txt";
			PrintWriter out= new PrintWriter(new FileOutputStream(datadir, true));
			out.println("LR = " + LR);
			out.close();
		}
		catch(Exception e){
			
		}
		
	}
	
	public void creatDatFileForAmpl2004(String dir, String day, String city){
		try{
			int m = 20;
			int T = 72000/m;
			for(int i = 0; i < allPeopleRequests.size() - 1; i++){
				if(allPeopleRequests.get(i+1).timePoint == allPeopleRequests.get(i).timePoint){
					allPeopleRequests.remove(i+1);
				}
			}
			int n = allPeopleRequests.size() - 1;
			String datadir = dir + "\\taxiDemands\\" + city + "\\" + city + "_" + m + "periods_2004.dat";
			PrintWriter out= new PrintWriter(datadir);
			String str = "";
			for(int i = 0; i < m; i++)
				str = str + i + " ";
			str += m + ";";
			out.println("set A := " + str);
			
			str = "";
			for(int i = 1; i < m-1; i++)
				str = str + i + " ";
			str += (m-1) + ";";
			out.println("set A1 := " + str);
			
			str = "";
			for(int i = 1; i < m; i++)
				str = str + i + " ";
			str += m + ";";
			out.println("set A2 := " + str);
			
			out.println("set K := 0 1 2 3;");
			
			str = "";
			for(int i = 0; i < n; i++)
				str = str + i + " ";
			str += n + ";";
			out.println("set T := " + str);
			
			out.println("set W := " + str);
			
			str = "";
			for(int i = 1; i < n; i++)
				str = str + i + " ";
			str += n + ";";
			out.println("set T1 := " + str);
			
			out.println("param m := " + m + ";");
			
			out.println("param n := " + n + ";");
			
			out.println("param: arrTime :=");
			
			for(int i = 0; i < n; i++){
				out.println(i + "  " + allPeopleRequests.get(i).timePoint);
			}
			out.println(n + "  " + allPeopleRequests.get(n).timePoint + ";");
			int[] w = new int[n+1];
			for(int i = 0; i < allPeopleRequests.size(); i++){
				w[i] = (int)(allPeopleRequests.get(i).timePoint / T) + 1;
			}
			
			out.println("param: kArrT:= ");
			for(int i = 0; i < n; i++){
				out.println(i + "  " + w[i]);
			}
			out.println(n + "  " + w[n] + ";");
			out.println("param: knotPs := ");
			for(int i = 0; i <= m - 1; i++){
				int kn = i*T;
				out.println(i + "  " + kn);
			}
			int kn = m*T;
			out.println(m + "  " + kn + ";");
			out.close();
		}
		catch(Exception e){
			
		}
	}
	
	public void creatDatFileForAmpl2013(String dir, String day, String city){
		try{
			int m = 40;
			int T = 72000/m;
			for(int i = 0; i < allPeopleRequests.size() - 1; i++){
				if(allPeopleRequests.get(i+1).timePoint == allPeopleRequests.get(i).timePoint){
					allPeopleRequests.remove(i+1);
				}
			}
			int n = allPeopleRequests.size() - 1;
			String datadir = dir + "\\taxiDemands\\" + city + "\\" + city + "_" + m + "periods_2013.dat";
			PrintWriter out= new PrintWriter(datadir);
			String str = "";
			for(int i = 0; i < m; i++)
				str = str + i + " ";
			str += m + ";";
			out.println("set A := " + str);
			
			str = "";
			for(int i = 1; i < m-1; i++)
				str = str + i + " ";
			str += (m-1) + ";";
			out.println("set A1 := " + str);
			
			str = "";
			for(int i = 1; i < m; i++)
				str = str + i + " ";
			str += m + ";";
			out.println("set A2 := " + str);
			
			out.println("set K := 0 1 2 3;");
			
			str = "";
			for(int i = 0; i < n; i++)
				str = str + i + " ";
			str += n + ";";
			out.println("set T := " + str);
			
			out.println("set W := " + str);
			
			str = "";
			for(int i = 1; i < n; i++)
				str = str + i + " ";
			str += n + ";";
			out.println("set T1 := " + str);
			
			out.println("param m := " + m + ";");
			
			out.println("param n := " + n + ";");
			
			out.println("param: arrTime :=");
			
			for(int i = 0; i < n; i++){
				out.println(i + "  " + allPeopleRequests.get(i).timePoint/100);
			}
			out.println(n + "  " + allPeopleRequests.get(n).timePoint/100 + ";");
			int[] w = new int[n+1];
			for(int i = 0; i < allPeopleRequests.size(); i++){
				w[i] = (int)(allPeopleRequests.get(i).timePoint / T) + 1;
			}
			
			out.println("param: kArrT:= ");
			for(int i = 0; i < n; i++){
				out.println(i + "  " + w[i]);
			}
			out.println(n + "  " + w[n] + ";");
			out.println("param: knotPs := ");
			for(int i = 0; i <= m - 1; i++){
				int kn = i*T;
				out.println(i + "  " + kn/100);
			}
			int kn = m*T;
			out.println(m + "  " + kn/100 + ";");
			
			int[] period20 = new int[m];

			for(int i = 0; i < m; i++)
				period20[i] = 0;
			for(int i = 0; i < allPeopleRequests.size(); i++){
				PeopleRequest pr = allPeopleRequests.get(i);
				int ix = pr.timePoint/T;
				period20[ix]++;
			}
			out.println("param: nKnotPs := ");
			for(int i = 1; i <= m-1; i++)
				out.println(i + " " + period20[i-1]);
			out.println(m + " " + period20[m-1] + ";");
			out.close();
		}
		catch(Exception e){
			
		}
	}
	
	public void createDataFileForEstimatingKHomoSpatioTemporalPP(){
		SimulatorTimeUnit sim = new SimulatorTimeUnit();
		
		String data_dir = "E:\\Project\\pbts\\git_project\\SPT\\";
		
		String mapFileName = data_dir + "SanFrancisco_std\\SanfranciscoRoad-connected-contracted-5-refine-50.txt";
		String configFileName = data_dir + "SanFrancisco_std\\config-parameters.txt";
		String depotParkingFileName = data_dir + "SanFrancisco_std\\depots1000-parkings34.txt";
		sim.loadMapFromTextFile(mapFileName);
		sim.loadParameters(configFileName);
		sim.loadDepotParkings(depotParkingFileName);
		sim.initialize();
		String type = "PPSARP";
		StatisticInfo stInfo = new StatisticInfo();
		String city = "SanFrancisco";
		String xcor = "";
		String ycor = "";
		String pr = "";
		String day = "1";
		//calculate arc
		String requestFileName = data_dir + "SanFrancisco_std\\ins_day_" + day +"_minSpd_5_maxSpd_60.txt";
		sim.loadRequestsFullTime(requestFileName);
		for(int i = 0; i < sim.allPeopleRequests.size(); i++){
			PeopleRequest p = sim.allPeopleRequests.get(i);
			int inter = p.timePoint/900;
			xcor += sim.map.mLatLng.get(p.pickupLocationID).lat + "L, ";
			ycor += sim.map.mLatLng.get(p.pickupLocationID).lng + "L, ";
			pr += inter + "L, ";
		}
		String outStr = "`fmd` <- structure(c(";
		outStr += xcor + ycor + pr;
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SimulatorTimeUnit sim = new SimulatorTimeUnit();
		
		String data_dir = "E:\\Project\\pbts\\git_project\\SPT\\";
		
		String mapFileName = data_dir + "SanFrancisco_std\\SanfranciscoRoad-connected-contracted-5-refine-50.txt";
		String configFileName = data_dir + "SanFrancisco_std\\config-parameters.txt";
		String depotParkingFileName = data_dir + "SanFrancisco_std\\depots1000-parkings34.txt";
		sim.loadMapFromTextFile(mapFileName);
		sim.loadParameters(configFileName);
		sim.loadDepotParkings(depotParkingFileName);
		sim.initialize();
		//String type = "PPSARP";
		StatisticInfo stInfo = new StatisticInfo();
		String city = "Porto";
		//create file with SF data
		for(int i = 1; i <= 1; i++){
			String day = "" + i;
			//calculate arc
			String requestFileName = data_dir + "SanFrancisco_std\\ins_day_" + day +"_minSpd_5_maxSpd_60.txt";
			sim.loadRequestsFullTime(requestFileName);
			for(int k = 0; k < sim.allPeopleRequests.size(); k++)
				stInfo.allPeopleRequests.add(sim.allPeopleRequests.get(k));
			
			//String requestFileName = "E:\\Project\\pbts\\git_project\\SPT\\taxiDemands\\" + city + "\\day" + day + ".txt";
			//stInfo.readPortoDataFile(requestFileName);
			//String itineraryFileName1 = data_dir + "SanFrancisco_std\\testcript2_bk\\ins_day_" + day + "_minSpd_5_maxSpd_60.txt-plannerGreedyExchangeSharingDecisionTimeLimitPlanner-maxPendingStops10-decisionTime15-itinerary.txt";
			//String itineraryFileName2 = data_dir + "SanFrancisco_std\\testcript2_bk\\ins_day_" + day + "_minSpd_5_maxSpd_60.txt-plannerGreedyExSharingDecisionTimeLimitAndBestParkingPlanner-maxPendingStops10-decisionTime15-itinerary.txt";
			
			//stInfo.analysisUnloadDistance(sim, data_dir, itineraryFileName1, itineraryFileName2, day);
			//stInfo.compareDistance(sim, data_dir, itineraryFileName1, itineraryFileName2, day, type);
			//stInfo.analysisNumberOfBeneficialTaxis(sim, data_dir, itineraryFileName1, itineraryFileName2, day);
			
			
			//stInfo.createSecondToRequests(data_dir, i);
			//stInfo.createRij(data_dir, i, city);
			//stInfo.evaluateNbPeriods(data_dir, day, city);
			int maxTime = 86400;
			stInfo.evaluateNbPeriodsRandomDividedPoints(data_dir, day, city, maxTime);
			//stInfo.creatDatFileForAmpl2013(data_dir, day, city);
			
		}
		//stInfo.testHomogeneity(data_dir, "1", city);
		stInfo.createRequestInPeriodsFileForTestDiggle(sim, data_dir, city);
		//stInfo.createRequestInPeriodsFileTrainTest(data_dir);
		//sim.createArcFileBasedOnReqs();
		//sim.createDataFileForSARP();
		
		
		/*for(int i = 2; i <= 2; i++){
			String day = "" + i;
			String requestFileName = "E:\\Project\\pbts\\git_project\\SPT\\taxiDemands\\" + city + "\\day" + day + ".txt";
			stInfo.readPortoDataFile(requestFileName);
			//stInfo.createRij(data_dir, i, city);
			stInfo.createSecondToRequests(data_dir, i, city);		
			
		}*/
		
	}

}
