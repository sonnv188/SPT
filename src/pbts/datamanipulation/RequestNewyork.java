package pbts.datamanipulation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class RequestNewyork {
	public RequestNewyork(){
		
	}
	
	public void readData(String dir){
		String fn = dir + "train.csv";
		BufferedReader br = null;
		String line = null;
		int[] results = new int[86400];
		for(int i = 0; i < results.length; i++)
			results[i] = 0;
		try {
			br = new BufferedReader(new FileReader(fn));
			// skip the header
			br.readLine();
			String fo = dir + "day1-01-2016.txt";
	    	PrintWriter out = new PrintWriter(fo);
	    	
			while ((line = br.readLine()) != null) {
				String[] column = line.split(",");
				if(column[2].replaceAll("\"", "").indexOf("2016-01-01") != -1){
					String time = column[2].split(" ")[1];
					String[] str = time.split(":");
					int timemili = Integer.parseInt(str[0])*3600 + Integer.parseInt(str[1])*60 + Integer.parseInt(str[2]);
					results[timemili]++;
				}
			}
			for(int i = 0; i < results.length; i++){
				if(results[i] != 0){
					for(int j = 0; j < results[i]; j++)
						out.println(i);
				}
			}
			out.println("-1");
			out.close();
		}catch(Exception e){
		
		}
	}
	
	public static void main(String[] args){
		String dir = "E:\\Project\\data\\NYCyellowCab\\train\\";
		RequestNewyork rP = new RequestNewyork();
		rP.readData(dir);
	}
}
