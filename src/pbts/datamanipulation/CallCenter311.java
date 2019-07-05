package pbts.datamanipulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

public class CallCenter311 {
	public CallCenter311(){
		
	}
	public void readData(String dir){
		try{
			String fn = dir + "311CallCenterData.csv";
			BufferedReader br = new BufferedReader(new FileReader(fn));
			// skip the header
			br.readLine();
			int nbDay = 31;
			int[][] results = new int[nbDay+1][86400];
			for(int i = 1; i <= nbDay; i++)
				for(int j = 0; j < 86400; j++)
					results[i][j] = 0;
			String str = "";
			for(int i = 0; i < 1048577; i++){
				str = br.readLine();
				if(str == null || str.length() == 0)
					break;
				String[] column = str.split(",");
				System.out.println("column 0 " + column[0]);
				String[] date = column[0].split("/");
				System.out.println("date = " + date.toString());
				if(Integer.parseInt(date[0]) == 1){
					int day = Integer.parseInt(date[1]);
					System.out.println("day = " + day);
					String[] time = column[1].split(":");
					System.out.println("time = " + time.toString());
					int mill = Integer.parseInt(time[0])*3600 + Integer.parseInt(time[1])*60 + Integer.parseInt(time[2]);
					System.out.println("mill = " + mill);
					results[day][mill]++;
					System.out.println("4");
				}
			}
			br.close();
			
			for(int d = 1; d <= nbDay; d++){
				String fo = dir + "day" + d + ".txt";
		    	PrintWriter out = new PrintWriter(fo);
				for(int i = 0; i < results[d].length; i++){
					if(results[d][i] != 0){
						for(int j = 0; j < results[d][i]; j++)
							out.println(i);
					}
				}
				out.println("-1");
				out.close();
			}
		}catch(Exception e){
		
		}
	}
	
	public static void main(String[] args){
		String dir = "E:\\Project\\data\\callCenter311\\";
		CallCenter311 rP = new CallCenter311();
		rP.readData(dir);
	}
}
