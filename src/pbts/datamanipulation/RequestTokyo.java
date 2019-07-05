package pbts.datamanipulation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import pbts.entities.PeopleRequest;

public class RequestTokyo {
	public HashMap<Integer, HashMap> allRequests;
	RequestTokyo(){
		allRequests = new HashMap<Integer, HashMap>();
	}
	
	public void extractRequests(String fn){
		try{
			String fin = fn + "excerpt.txt";
			Scanner in = new Scanner(new File(fin));
			while(true){
				String str = in.nextLine();
				if(str == "UNLOCK TABLES")
					break;
				String[] arStr = str.split("\\,");
				for(int i = 9; i < arStr.length; i = i + 9){
					String[] timearStr = arStr[i].split(" ");
					String[] datearStr = timearStr[0].split("\\-");
					String year = datearStr[0].substring(1, datearStr[0].length());
					if(year.equals("2006") && datearStr[1].equals("06") && arStr[i+3].equals("\'E\'")){
						String fo = "E:\\Project\\pbts\\git_project\\SPT\\Tokyo\\requests-day-" + datearStr[2] + ".txt";
						PrintWriter out = new PrintWriter(new FileOutputStream(fo, true));
						String[] timeMillarStr = timearStr[1].split(":");
						String second = timeMillarStr[2].substring(0, timeMillarStr[2].length()-1);
						double timeMill = Integer.parseInt(timeMillarStr[0]) * 3600 + Integer.parseInt(timeMillarStr[1]) * 60 + Integer.parseInt(second);
						out.println(timeMill + " " + arStr[i+1] + " " + arStr[i+2]);
						out.close();
					}
				}
			}
			for(int i = 12; i < 20; i++){
				String fo = "E:\\Project\\pbts\\git_project\\SPT\\Tokyo\\requests-day-" + i + ".txt";
				PrintWriter out = new PrintWriter(new FileOutputStream(fo, true));
				out.println("-1");
			}
			in.close();
		}catch(Exception e){
			
		}
	}
	
	public void createRequestsInPeriodFile(String fn){
		for(int day = 13; day < 14; day++){
			int[] listPr = {4, 24, 48, 96, 288, 1440};
			int[] ranges = {21600, 3600, 1800, 900, 300, 60};
			for(int k = 0; k < listPr.length; k++){
				int[] reqs = new int[listPr[k]];
				for(int i = 0; i < reqs.length; i++)
					reqs[i] = 0;
				String fin = fn + "requests-day-" + day + ".txt";
				try{
					Scanner in = new Scanner(new File(fin));
					while(true){
						double timePoint = in.nextDouble();
						if(timePoint == -1)
							break;
						int pr = (int)timePoint/ranges[k];
						reqs[pr]++;
						in.nextLine();
					}
					in.close();
					PrintWriter out = new PrintWriter(fn + "reqs-periods-" + listPr[k] + "-day-" + day + ".txt");
					for(int i = 0; i < reqs.length; i++)
						out.println(reqs[i]);
					out.close();
				}
				catch(Exception e){
					
				}
			}
		}
	}
	
	public void createRequestInPeriodsFileForCheckHomoTrainTest(String fn){
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
		try{
			String fin = fn + "requests-day-13.txt";
			Scanner in = new Scanner(new File(fin));
			while(true){
				double timePoint = in.nextDouble();
				if(timePoint == -1)
					break;
				int ix = (int)timePoint/21600; 
				period4[ix]++;
				ix = (int)timePoint/3600;
				period24[ix]++;
				ix = (int)timePoint/900;
				period96[ix]++;
				ix = (int)timePoint/300;
				period288[ix]++;
				ix = (int)timePoint/60;
				period1440[ix]++;
				ix = (int)timePoint;
				period86400[ix]++;
				in.nextLine();
			}
			in.close();
		}
		catch(Exception e){
			
		}

		try{
			String datadir = fn + "\\reqs-onemins-4periods-day13.txt";
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
		try{
			String datadir = fn + "\\reqs-onemins-24periods-day13.txt";
			PrintWriter out= new PrintWriter(datadir);
			int max = 0;
		
			for(int i = 0; i < 1440; i++){
				if(period1440[i] > max)
					max = period1440[i];
			}
			for(int i = 0; i < max; i++){
				for(int j = 0; j < 24; j++){
					int cnt = 0;
					for(int k = j *60; k < (j+1)*60; k++){
						if(period1440[k] == i)
							cnt++;
					}
					out.println(i + " " + cnt);
				}
			}
			out.close();
		}catch(Exception e){
			
		}
		
		try{
			String datadir = fn + "\\reqs-onemins-96periods-day13.txt";
			PrintWriter out= new PrintWriter(datadir);
			int max = 0;
		
			for(int i = 0; i < 1440; i++){
				if(period1440[i] > max)
					max = period1440[i];
			}
			for(int i = 0; i < max; i++){
				for(int j = 0; j < 96; j++){
					int cnt = 0;
					for(int k = j *15; k < (j+1)*15; k++){
						if(period1440[k] == i)
							cnt++;
					}
					out.println(i + " " + cnt);
				}
			}
			out.close();
		}catch(Exception e){
			
		}
		
		try{
			String datadir = fn + "\\reqs-onemins-288periods-day13.txt";
			PrintWriter out= new PrintWriter(datadir);
			int max = 0;
		
			for(int i = 0; i < 1440; i++){
				if(period1440[i] > max)
					max = period1440[i];
			}
			for(int i = 0; i < max; i++){
				for(int j = 0; j < 288; j++){
					int cnt = 0;
					for(int k = j*5; k < (j+1)*5; k++){
						if(period1440[k] == i)
							cnt++;
					}
					out.println(i + " " + cnt);
				}
			}
			out.close();
		}catch(Exception e){
			
		}
	}
	
	public static void main(String[] arg){
		String fn = "E:\\Project\\pbts\\git_project\\SPT\\Tokyo\\";
		RequestTokyo rT = new RequestTokyo();
		//rT.extractRequests(fn);
		//rT.createRequestsInPeriodFile(fn);
		rT.createRequestInPeriodsFileForCheckHomoTrainTest(fn);
	}
}
