package pbts.datamanipulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

public class CallCenterIsarelBank {
	public CallCenterIsarelBank(){
		
	}
	public void readData(String dir){
		try{
			String fn = dir + "january.txt";
			Scanner in = new Scanner(new File(fn));
			int nbDay = 31;
			int[][] results = new int[nbDay+1][86400];
			for(int i = 1; i <= nbDay; i++)
				for(int j = 0; j < 86400; j++)
					results[i][j] = 0;
			String str = "";
			str = in.nextLine();
			while(in.hasNextLine()){
				str = in.nextLine();
				String[] linearr = str.split("\t");
				String date = linearr[5].substring(linearr[5].length()-2, linearr[5].length());
				int day = Integer.parseInt(date);
				String[] time = linearr[6].split(":");
				int mill = Integer.parseInt(time[0])*3600 + Integer.parseInt(time[1])*60 + Integer.parseInt(time[2]);
				results[day][mill]++;
			}
			in.close();
			
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
		String dir = "E:\\Project\\data\\bankingIsarelData\\January\\";
		CallCenterIsarelBank rP = new CallCenterIsarelBank();
		rP.readData(dir);
	}
}
