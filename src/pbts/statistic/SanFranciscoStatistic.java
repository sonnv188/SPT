package pbts.statistic;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import pbts.gismap.Point;
import pbts.simulation.SimulatorTimeUnit;

public class SanFranciscoStatistic {
	public ArrayList<Point> pickupPoints;
	public ArrayList<Point> deliveryPoints;
	public ArrayList<Double> timePick;
	public ArrayList<Double> timeDelivery;
	
	public SanFranciscoStatistic(){
		ArrayList<Point> pickupPoints = new ArrayList<Point>();
		ArrayList<Point> deliveryPoints = new ArrayList<Point>();
		ArrayList<Double> timePick = new ArrayList<Double>();
		ArrayList<Double> timeDelivery = new ArrayList<Double>();
	}

	public double manhattanDistance(Point u, Point v){
		return Math.abs(u.getdLat() - v.getdLat()) + Math.abs(u.getdLong() - v.getdLong());
	}
	
	public void readRequestFile(String requestFileName){
		try{
			Scanner in = new Scanner(new File(requestFileName));
			while(true){
				int id = in.nextInt();
				if (id == -1)
					break;
				double t1 = in.nextDouble();
				double pickLat = in.nextDouble();
				double pickLong = in.nextDouble();
				double t2 = in.nextDouble();
				double delLat = in.nextDouble();
				double delLong = in.nextDouble();
				timePick.add(t1);
				timeDelivery.add(t2);
				Point p = new Point(pickLat, pickLong);
				Point d = new Point(delLat,delLong);
				pickupPoints.add(p);
				deliveryPoints.add(d);
			}
		}catch(Exception e){
			
		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub		
		String data_dir = "E:\\Project\\pbts\\git_project\\SPT\\";
		String requestFileName = data_dir + "SanFrancisco_std\\2010_03\\2010_03.trips";
		
		
	}
}
