package pbts.prediction.behrouz.fixedrate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.random.RandomDataGenerator;

public class FixedRateSampler {

	private String regionPointMapPath;
	private String dprPath;
	private String prPath;

	private HashMap<String, ArrayList<Integer>> rpMap;
	private HashMap<Integer, HashMap<String, Double>> pr;
	private HashMap<Integer, HashMap<Integer, HashMap<String, Double>>> dpr;

	private RandomDataGenerator rand;

	public FixedRateSampler() {
		this("pr.csv", "dpr.csv", "region_point_map.csv");
	}

	public FixedRateSampler(String prPath, String dprPath, String prmPath) {
		this.prPath = prPath;
		this.dprPath = dprPath;
		this.regionPointMapPath = prmPath;

		this.rpMap = null;
		this.pr = null;
		this.dpr = null;

		readRegionPointMap();
		readDprRates();
		readPrRates();

		rand = new RandomDataGenerator();
	}

	private void readRegionPointMap() {

		this.rpMap = new HashMap<String, ArrayList<Integer>>();
		BufferedReader br = null;
		String line = null;

		try {
			br = new BufferedReader(new FileReader(this.regionPointMapPath));
			// skip the header
			br.readLine();
			while ((line = br.readLine()) != null) {
				String[] columns = line.split(",");
				int point_id = Integer.parseInt(columns[0]);
				String region_id = columns[1];
				if (!rpMap.containsKey(region_id))
					rpMap.put(region_id, new ArrayList<Integer>());
				rpMap.get(region_id).add(point_id);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void readDprRates() {

		this.dpr = new HashMap<Integer, HashMap<Integer, HashMap<String, Double>>>();
		BufferedReader br = null;
		String line = null;

		try {
			br = new BufferedReader(new FileReader(this.dprPath));
			// skip the header
			br.readLine();
			while ((line = br.readLine()) != null) {
				String[] columns = line.split(",");
				int day = Integer.parseInt(columns[0]);
				int period = Integer.parseInt(columns[1]);
				String region_id = columns[2];
				double rate = Double.parseDouble(columns[3]);
				if (!dpr.containsKey(day))
					dpr.put(day,
							new HashMap<Integer, HashMap<String, Double>>());
				HashMap<Integer, HashMap<String, Double>> pr = dpr.get(day);
				if (!pr.containsKey(period))
					pr.put(period, new HashMap<String, Double>());
				pr.get(period).put(region_id, rate);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void readPrRates() {
		this.pr = new HashMap<Integer, HashMap<String, Double>>();
		BufferedReader br = null;
		String line = null;

		try {
			br = new BufferedReader(new FileReader(this.prPath));
			// skip the header
			br.readLine();
			while ((line = br.readLine()) != null) {
				String[] columns = line.split(",");
				int period = Integer.parseInt(columns[0]);
				String region_id = columns[1];
				double rate = Double.parseDouble(columns[2]);
				if (!pr.containsKey(period))
					pr.put(period, new HashMap<String, Double>());
				pr.get(period).put(region_id, rate);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Integer> getRequests(int period) {
		HashMap<String, Double> regionRates = pr.get(period);
		return getRequests(regionRates);
	}

	public ArrayList<Integer> getRequests(int day, int period) {
		HashMap<String, Double> regionRates = dpr.get(day).get(period);
		return getRequests(regionRates);
	}

	private ArrayList<Integer> getRequests(HashMap<String, Double> regionRates) {
		ArrayList<Integer> pointIds = new ArrayList<Integer>();
		for (String region_id : rpMap.keySet())
			if (regionRates.containsKey(region_id)) {
				double rate = regionRates.get(region_id);
				if (rate > 0) {
					int numberOfRequests = (int) this.rand.nextPoisson(rate);
					if (numberOfRequests > 0) {
						ArrayList<Integer> points = this.rpMap.get(region_id);
						int numPoints = points.size();
						for (int i = 0; i < numberOfRequests; i++) {
							int pointIndex = this.rand
									.nextInt(0, numPoints - 1);
							pointIds.add(points.get(pointIndex));
						}
					}
				}
			}
		return pointIds;
	}
}
