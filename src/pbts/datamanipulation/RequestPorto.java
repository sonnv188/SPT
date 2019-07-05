package pbts.datamanipulation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class RequestPorto {
	public RequestPorto(){
		
	}
	public void readData(String dir){
		for(int d = 1; d <=1; d++){
			String fn = dir + "train.csv";
			BufferedReader br = null;
			String line = null;
			int[] results = new int[86400];
			ArrayList<String> lats = new ArrayList<String>();
			ArrayList<String> lngs = new ArrayList<String>();
			ArrayList<String> temp = new ArrayList<String>();
			String dat = "";
			int n = 0;
			for(int i = 0; i < results.length; i++)
				results[i] = 0;
			try {
				br = new BufferedReader(new FileReader(fn));
				// skip the header
				br.readLine();
				String fo = dir + "day" + d + ".txt";
				String fc = dir + "day" + d + "-coordinate.txt";
		    	PrintWriter out = new PrintWriter(fo);
		    	PrintWriter out2 = new PrintWriter(fc);
		    	
				while ((line = br.readLine()) != null) {
					if(n > 500)
						break;
					String[] column = line.split(",");
					if(column[1].replaceAll("\"", "").equals("A") && column[6].replaceAll("\"", "").equals("A")){
						long unix = Long.parseLong(column[5].replaceAll("\"", ""));
						Date date = new Date(unix*1000L); 
						// format of the date
						SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
						jdf.setTimeZone(TimeZone.getTimeZone("GMT+1"));
						String java_date = jdf.format(date);
					    Date date1 = jdf.parse(java_date);
					    if(date1.getDay() == d && date1.getMonth() == 7){
					    	int tm = date1.getHours()*3600 + date1.getMinutes()*60 + date1.getSeconds();
					    	results[tm]++;
					    	if(column.length > 9 && column[8].length() > 3 && column[9].length() > 2){
					    		String t = column[8] + "," + column[9];
								if(temp.contains(t))
									continue;
								lats.add(column[8]);
								lngs.add(column[9]);
								temp.add(t);
								int period = tm/300;
								dat += period + ",";
								n++;
							}
					    }
					}
				}
				for(int i = 0; i < results.length; i++){
					if(results[i] != 0){
						for(int j = 0; j < results[i]; j++)
							out.println(i);
					}
				}
				for(int i = 0; i < lats.size(); i++){
					out2.print(lats.get(i) + ",");
				}
				for(int i = 0; i < lngs.size(); i++){
					out2.print(lngs.get(i) + ",");
				}
				out2.println(dat);
				
				out.println("-1");
				out.close();
				out2.println(lats.size());
				out2.close();
			}catch(Exception e){
				System.out.println("Exception " + e);
			}
		}
	}
	
	public static void main(String[] args){
		String dir = "E:\\Project\\data\\Porto\\train\\";
		RequestPorto rP = new RequestPorto();
		rP.readData(dir);
	}
}
