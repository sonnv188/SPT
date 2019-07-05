package pbts.simulation;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import pbts.entities.*;
import pbts.gismap.googlemaps.GoogleMapsQuery;

import java.util.*;
public class RoadMap {

	/**
	 * @param args
	 */
	
	public HashMap<Integer, ArrayList<Arc>> A;// A[i] is the set of adjacent arcs of vertex i
	public ArrayList<Integer> V;
	public ArrayList<Arc> Arcs;
	public HashMap<Integer, LatLng> mLatLng;
	public int n,m;
	public int maxID, minID;
	
	// squares
	public ArrayList<Square> squares;
	public Square[][] mSquares;
	//public HashMap<>
	public double min_lat = 1000000000;
	public double max_lat = -min_lat;
	public double min_lng = 1000000000;
	public double max_lng = -min_lng;
	public double dlat;
	public double dlng;
	public int rows;
	public int cols;
	
	public RoadMap(){
		
	}
	public Square findSquare(double lat, double lng){
		int idr = (int)Math.ceil((lat-min_lat)/dlat);
		int idc = (int)Math.ceil((lng-min_lng)/dlng);
		if(0 <= idr && idr < rows && 0 <= idc && idc < cols) return mSquares[idr][idc];
		return null;
	}
	public void setupSquare(int rows, int cols){
		this.rows = rows;
		this.cols = cols;
		
		min_lat = 1000000000;
		max_lat = -min_lat;
		min_lng = 1000000000;
		max_lng = -min_lng;
		
		for(int i = 0; i < V.size(); i++){
			int v = V.get(i);
			LatLng ll = mLatLng.get(v);
			if(min_lat > ll.lat) min_lat = ll.lat;
			if(max_lat < ll.lat) max_lat = ll.lat;
			if(min_lng > ll.lng) min_lng = ll.lng;
			if(max_lng < ll.lng) max_lng = ll.lng;
		}
		min_lat = min_lat - 0.001;
		max_lat = max_lat + 0.001;
		min_lng = min_lng - 0.001;
		max_lng = max_lng + 0.001;
		
		dlat = (max_lat - min_lat)/rows;
		dlng = (max_lng - min_lng)/cols;
		squares = new ArrayList<Square>();
		mSquares = new Square[rows][];
		for(int i = 0; i < rows; i++)
			mSquares[i] = new Square[cols];
		
		int id = -1;
		for(int i = 0; i < rows; i++){
			for(int j = 0; j < cols; j++){
				id++;
				Square SQ = new Square();
				SQ.ID = id;
				SQ.min_lat = min_lat + i*dlat;
				SQ.max_lat = SQ.min_lat + dlat;
				SQ.min_lng = min_lng + j*dlng;
				SQ.max_lng = SQ.min_lng + dlng;
				squares.add(SQ);
				mSquares[i][j] = SQ;
			}
		}
	}
	public void setTravelTime(Arc a, int t){
		a.t = t;
	}
	public long composeKeyOf2(long u, long v){
		return u * (maxID+1) + v;
	}
	private boolean checkAdd(HashSet<Arc> S, Arc a){
		Iterator it = S.iterator();
		while(it.hasNext()){
			Arc ai = (Arc)it.next();
			if(ai.begin == a.begin && ai.end == a.end) return false;
		}
		S.add(a);
		return true;
	}
	public int findNearestPoint(double lat, double lng, double threshold){
		double minDis = 100000000;
		int sel_v = -1;
		for(int i = 0; i < V.size(); i++){
			int v = V.get(i);
			LatLng ll = mLatLng.get(v);
			double d = GoogleMapsQuery.computeDistanceHaversineStatic(ll.lat, ll.lng, lat, lng);
			if(minDis > d){
				minDis = d; sel_v = v;
			}
		}
		if(minDis > threshold) return -1;// no point returned
		return sel_v;
	}
	private boolean withinWindow(double lat, double lng, double min_lat, double min_lng, double max_lat, double max_lng){
		return min_lat <= lat && lat <= max_lat && min_lng <= lng && lng <= max_lng;
	}
	public RoadMap extractSubMap(double min_lat, double min_lng, double max_lat, double max_lng){
		HashSet<Integer> nodes = new HashSet<Integer>();
		HashSet<Arc> arcs = new HashSet<Arc>();
		for(int i = 0; i < Arcs.size(); i++){
			Arc a = Arcs.get(i);
			LatLng llb = mLatLng.get(a.begin);
			LatLng lle = mLatLng.get(a.end);
			if(withinWindow(llb.lat,llb.lng,min_lat,min_lng,max_lat,max_lng) && 
					withinWindow(lle.lat,lle.lng,min_lat,min_lng,max_lat,max_lng))
				arcs.add(a);
		}
		Iterator it = arcs.iterator();
		while(it.hasNext()){
			Arc a = (Arc)it.next();
			nodes.add(a.begin);
			nodes.add(a.end);
		}
		RoadMap rm = new RoadMap();
		rm.V = new ArrayList<Integer>();
		rm.A = new HashMap<Integer, ArrayList<Arc>>();
		rm.Arcs = new ArrayList<Arc>();
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		it = nodes.iterator();
		int id = 0;
		while(it.hasNext()){
			int v = (Integer)it.next();
			id++;
			map.put(v, id);
			rm.V.add(id);
			rm.A.put(id, new ArrayList<Arc>());
		}
		it = arcs.iterator();
		while(it.hasNext()){
			Arc a = (Arc)it.next();
			int u = a.begin;
			int v = a.end;
			int nu = map.get(u);
			int nv = map.get(v);
			Arc na = new Arc(nu,nv,a.w);
			rm.Arcs.add(na);
			rm.A.get(nu).add(na);
		}
		rm.n = rm.V.size();
		rm.m = rm.Arcs.size();
		rm.minID = 1;
		rm.maxID = rm.n;
		rm.mLatLng = mLatLng;
		return rm;
	}
	public RoadMap contract(double threshold){
		HashSet<Integer> nodes = new HashSet<Integer>();
		HashSet<Arc> arcs = new HashSet<Arc>();
		HashMap<Integer, HashSet<Arc>> inA = new HashMap<Integer, HashSet<Arc>>();
		HashMap<Integer, HashSet<Arc>> outA = new HashMap<Integer, HashSet<Arc>>();
		pbts.gismap.googlemaps.GoogleMapsQuery G = new pbts.gismap.googlemaps.GoogleMapsQuery();
		int maxID = -1;
		for(int i = 0; i < V.size(); i++){
			nodes.add(V.get(i));
			inA.put(V.get(i), new HashSet<Arc>());
			outA.put(V.get(i), new HashSet<Arc>());
			maxID = maxID < V.get(i) ? V.get(i) : maxID;
		}
		int count = 0;
		for(int i = 0; i < Arcs.size(); i++){
			Arc a = Arcs.get(i);
			arcs.add(a);
			inA.get(a.end).add(a);
			outA.get(a.begin).add(a);
			if(a.w < threshold) count++;
		}
		System.out.println("Count = " + count);
		//if(true)return null;
		int I = 0;
		while(true){
			I++;
			Arc sel_a = null;
			Iterator it = arcs.iterator();
			//for(int i = 0; i < arcs.size(); i++){
			while(it.hasNext()){
				Arc a = (Arc)it.next();//arcs.get(i);
				if(a.w <= threshold){
					sel_a = a;break;
				}
			}
			if(sel_a == null) break;
			
			// contract sel_a
			int u = sel_a.begin;
			int v = sel_a.end;
			System.out.println("RoadMap::contract, it = " + I + ", nodes.sz = " + nodes.size() + 
					", maxID = " + maxID + ", sel_a = (" + u + "," + v + ")");
			if(u == v){
				System.out.println("RoadMap::contract, u = v???? failed");
				System.exit(-1);
			}
			LatLng latlngU = mLatLng.get(u);
			LatLng latlngV = mLatLng.get(v);
			LatLng newLatLng = new LatLng((latlngU.lat + latlngV.lat)/2,(latlngU.lng + latlngV.lng)/2);
			maxID++;
			nodes.add(maxID);
			nodes.remove(u);
			nodes.remove(v);
			mLatLng.put(maxID, newLatLng);
			arcs.remove(sel_a);
			inA.put(maxID, new HashSet<Arc>());
			outA.put(maxID, new HashSet<Arc>());
			it = inA.get(u).iterator();
			while(it.hasNext()){
				Arc a = (Arc)it.next();
				arcs.remove(a);
				int x = a.begin;
				if(x == v) continue;
				LatLng latlngX = mLatLng.get(x);
				outA.get(x).remove(a);
				double w = G.computeDistanceHaversine(latlngX.lat, latlngX.lng, 
						newLatLng.lat, newLatLng.lng)*1000;
				Arc na = new Arc(x,maxID,w);
				boolean ok = checkAdd(outA.get(x),na);
				
				//outA.get(x).add(na);
				if(ok) ok = checkAdd(inA.get(maxID),na);
				if(ok) ok = arcs.add(na);
			}
			it = outA.get(u).iterator();
			while(it.hasNext()){
				Arc a = (Arc)it.next();
				if(a == sel_a) continue;
				arcs.remove(a);
				int x = a.end;
				if(x == v) continue;
				LatLng latlngX = mLatLng.get(x);
				inA.get(x).remove(a);
				double w = G.computeDistanceHaversine(newLatLng.lat, newLatLng.lng,
						latlngX.lat, latlngX.lng)*1000;
				Arc na = new Arc(maxID,x,w);
				boolean ok = checkAdd(inA.get(x),na);
				if(ok) ok = checkAdd(outA.get(maxID),na);
				if(ok) arcs.add(na);
				//inA.get(x).add(na);
				//outA.get(maxID).add(na);
			}
			
			it = inA.get(v).iterator();
			while(it.hasNext()){
				Arc a = (Arc)it.next();
				if(sel_a == a) continue;
				arcs.remove(a);
				int x = a.begin;
				if(x == u) continue;
				LatLng latlngX = mLatLng.get(x);
				outA.get(x).remove(a);
				double w = G.computeDistanceHaversine(latlngX.lat, latlngX.lng, 
						newLatLng.lat, newLatLng.lng)*1000;
				Arc na = new Arc(x,maxID,w);
				boolean ok = checkAdd(outA.get(x),na);
				if(ok) ok = checkAdd(inA.get(maxID),na);
				if(ok) arcs.add(na);
				//outA.get(x).add(na);
				//inA.get(maxID).add(na);
			}
			it = outA.get(v).iterator();
			while(it.hasNext()){
				Arc a = (Arc)it.next();
				//if(a == sel_a) continue;
				arcs.remove(a);
				int x = a.end;
				if(x == u) continue;
				LatLng latlngX = mLatLng.get(x);
				inA.get(x).remove(a);
				double w = G.computeDistanceHaversine(newLatLng.lat, newLatLng.lng,
						latlngX.lat, latlngX.lng)*1000;
				Arc na = new Arc(maxID,x,w);
				boolean ok = checkAdd(inA.get(x),na);
				if(ok) ok = checkAdd(outA.get(maxID),na);
				if(ok) arcs.add(na);
				//inA.get(x).add(na);
				//outA.get(maxID).add(na);
			}
		}
		RoadMap rm = new RoadMap();
		rm.V = new ArrayList<Integer>();
		rm.A = new HashMap<Integer, ArrayList<Arc>>();
		rm.Arcs = new ArrayList<Arc>();
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		Iterator it = nodes.iterator();
		int id = 0;
		while(it.hasNext()){
			int v = (Integer)it.next();
			id++;
			map.put(v, id);
			mLatLng.put(id, mLatLng.get(v));
			rm.V.add(id);
			rm.A.put(id, new ArrayList<Arc>());
		}
		it = arcs.iterator();
		while(it.hasNext()){
			Arc a = (Arc)it.next();
			int u = a.begin;
			int v = a.end;
			int nu = map.get(u);
			int nv = map.get(v);
			Arc na = new Arc(nu,nv,a.w);
			rm.Arcs.add(na);
			rm.A.get(nu).add(na);
		}
		rm.n = rm.V.size();
		rm.m = rm.Arcs.size();
		rm.minID = 1;
		rm.maxID = rm.n;
		rm.mLatLng = mLatLng;
		return rm;
	}
	public void writeToFile(String fn){
		try{
			PrintWriter out = new PrintWriter(fn);
			for(int i = 0; i < V.size(); i++){
				int v = V.get(i);
				out.println(v + " " + mLatLng.get(v).lat + " " + mLatLng.get(v).lng);
			}
			out.println(-1);
			for(int i = 0; i < Arcs.size(); i++){
				Arc a = Arcs.get(i);
				out.println(a.begin + " " + a.end + " " + a.w);
			}
			out.println(-1);
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void writeToPolishMap(String fn, int zoomLevel){
		try{
			PrintWriter out = new PrintWriter(fn);
			BufferedReader in = new BufferedReader(new FileReader("polishheader.txt"));
    		
    		String line = "";
    		while((line=in.readLine())!=null){
    			out.println(line);
    			System.out.println(line);
    		}
    		
    		for(int i = 0; i < Arcs.size(); i++){
    			Arc a = Arcs.get(i);
    			double lat1 = mLatLng.get(a.begin).lat;
    			double lng1 = mLatLng.get(a.begin).lng;
    			double lat2 = mLatLng.get(a.end).lat;
    			double lng2 = mLatLng.get(a.end).lng;
    			
    			for(int z = 0; z <= zoomLevel; z++){
    				out.println("[POLYLINE]");
    				out.println("Type=0x1");
    				out.println("Label=" + "noname");
    				out.print("Data" + z + "=");
    				out.println("(" + lat1 + "," + lng1 + "),(" + lat2 + "," + lng2 + ")");
    				//out.println("(" + lng1 + "," + lat1 + "),(" + lng2 + "," + lat2 + ")");
    				out.println("[END]");
    			}
    		}
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void analyze(String fo){
		double t0 = System.currentTimeMillis();
		HashMap<String, Integer> mapLatLng2ID = new HashMap<String, Integer>();
		HashMap<String, Arc> mapArc = new HashMap<String, Arc>();
		ArrayList<Integer> newV = new ArrayList<Integer>();
		ArrayList<Arc> newArc = new ArrayList<Arc>();
		for(int i = 0; i < Arcs.size(); i++){
			Arc a = Arcs.get(i);
			int u = a.begin;
			int v = a.end;
			LatLng llu = mLatLng.get(u);
			LatLng llv = mLatLng.get(v);
			String keyu = llu.lat + "," + llu.lng;
			String keyv = llv.lat + "," + llv.lng;
			if(mapLatLng2ID.get(keyu) == null){
				mapLatLng2ID.put(keyu, u); 
				newV.add(u);
			}else{
				u = mapLatLng2ID.get(keyu);
			}
			
			if(mapLatLng2ID.get(keyv) == null){
				mapLatLng2ID.put(keyv, v);
				newV.add(v);
			}else{
				v = mapLatLng2ID.get(keyv);
			}
			if(u == v){
				System.out.println("RoadMap::analyze --> finish " + i + "/" + Arcs.size() + " NULL ARC w = " + a.w);
				continue;
			}
			String key = u + "," + v;
			Arc na = mapArc.get(key);
			if(na != null) continue;
			na = new Arc(u,v,a.w);
			newArc.add(na);
			mapArc.put(key, na);
			System.out.println("RoadMap::analyze --> finish " + i + "/" + Arcs.size() + 
					", newArc.sz = " + newArc.size() + ", newV.sz = " + newV.size());
		}
		
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for(int i = 0; i < newV.size(); i++)
			map.put(newV.get(i), i+1);
		try{
			PrintWriter out = new PrintWriter(fo);
			for(int i = 0; i < newV.size(); i++){
				int v = newV.get(i);
				LatLng ll = mLatLng.get(v);
				out.println(map.get(v) + " " + ll.lat + " " + ll.lng);
				System.out.println("write node " + i + "/"+ newV.size() + " --> " + map.get(v) + " " + ll.lat + " " + ll.lng);
			}
			out.println(-1);
			for(int i = 0; i < newArc.size(); i++){
				Arc a = newArc.get(i);
				out.println(map.get(a.begin) + " " + map.get(a.end) + " " + a.w);
				System.out.println("write arc " + i + "/"+ newArc.size() + " --> " + map.get(a.begin) + " " + map.get(a.end) + " " + a.w);
			}
			out.println(-1);
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		double t = System.currentTimeMillis() - t0;
		t = t *0.001;
		System.out.println("RoadMap::analyze --> V.sz = "+ V.size () + ", newV.sz = " + newV.size() + 
				", Arcs.sz = " + Arcs.size() + ", newArc.sz = " + newArc.size());
		System.out.println("RoadMap::analyze finished time = " + t);
	}
	public void loadData(String filename){
		try{
			
			Scanner in = new Scanner(new File(filename));
			n = 0;
			V = new ArrayList<Integer>();
			A = new HashMap<Integer, ArrayList<Arc>>();
			Arcs = new ArrayList<Arc>();
			mLatLng = new HashMap<Integer, LatLng>();
			double minDis = 999999999;
			double maxDis = -minDis;
			double min_lat = 100000;
			double max_lat = -min_lat;
			double min_lng = 100000;
			double max_lng = -min_lng;
			int minU = -1, minV = -1, maxU = -1, maxV = -1;
			maxID = -1; minID = 999999999;
			//String line = in.nextLine();
			//System.out.println(line); if(true) return;
			while(true){
				
				String line = in.nextLine();
				line = Utility.parserSpace(line);
				String[] s = line.split("\t");
				int u = Integer.valueOf(s[0]);//in.nextInt();
				if(u == -1) break;
				double lat = Double.valueOf(s[1]);//in.nextDouble();
				double lng = Double.valueOf(s[2]);//in.nextDouble();
				/*
				int u = in.nextInt();
				if(u == -1) break;
				double lat = in.nextDouble();
				double lng = in.nextDouble();
				*/
				LatLng ll = new LatLng(lat,lng);
				mLatLng.put(u, ll);
				n++;
				V.add(u);
				A.put(u,  new ArrayList<Arc>());
				if(u > maxID) maxID = u;
				if(u < minID) minID = u;
				
				min_lat = min_lat < lat ? min_lat : lat;
				max_lat = max_lat > lat ? max_lat : lat;
				min_lng = min_lng < lng ? min_lng : lng;
				max_lng = max_lng > lng ? max_lng : lng;
			}
			
			
			m = 0;
			while(true){
				String line = in.nextLine(); 
				line = Utility.parserSpace(line);
				String[] s = line.split("\t");
				
				int u = Integer.valueOf(s[0]);//in.nextInt();
				if(u == -1) break;
				int v = Integer.valueOf(s[1]);//in.nextInt();
				//System.out.println("RoadMap::loadData --> read arc(" + u + "," + v + ")");
				double w = Double.valueOf(s[2]);//in.nextDouble();
				/*
				int u = in.nextInt();
				if(u == -1) break;
				int v = in.nextInt();
				double w = in.nextDouble();
				*/
				if(u == v) continue;
				if(minDis > w){ minDis = w; minU = u; minV = v;}
				if(maxDis < w){ maxDis = w; maxU = u; maxV = v;}
				Arc a = new Arc(u,v,w);
				Arcs.add(a);
				m++;
				if(A.get(u) == null){
					System.out.println("RoadMap::loadData, A.get(" + u + ") = null");
					System.exit(-1);
				}
				A.get(u).add(a);
			}
			in.close();
			/*
			for(int i = 0; i < V.size(); i++){
				int v = V.get(i);
				System.out.print("A[" + v + "] = ");
				for(int j = 0; j < A.get(v).size(); j++){
					Arc a = A.get(v).get(j);
						System.out.print(a.end + ", ");
				}
				System.out.println();
			}
			*/
			System.out.println("n = " + V.size() + ", m = " + m + ", minDis = " + minDis + ", maxDis = " + maxDis + 
					", min edge = (" + minU + "," + minV + ")");
			System.out.println("RoadMap::loadMap --> min_lat = " + min_lat + ", max_lat = " + max_lat + ", min_lng = " + min_lng + ", max_lng = " + max_lng);
			System.out.println("RoadMap::loadMap --> maxEdge = " + mLatLng.get(maxU).lat + "," + 
			mLatLng.get(maxU).lng + " -- " + mLatLng.get(maxV).lat + "," + mLatLng.get(maxV).lng);
			
			//System.exit(-1);
		}catch(Exception ex){
			ex.printStackTrace();
		}

	}
	public Arc getArc(int u, int v){
		for(int i = 0; i < A.get(u).size(); i++){
			Arc a = A.get(u).get(i);
			if(a.end == v) return a;
		}
		return null;
	}

	public RoadMap refineMap(double lengthThrs){
		//for each arc (u,v) whose length > lengthThrs generate intermediate points between u and v so that
		// the length of each arc <= lengthThrs
		//HashMap<Arc, ArrayList<Integer>> map = new HashMap<Arc,ArrayList<Integer>>();
		int nID = maxID;
		ArrayList<Integer> newV = new ArrayList<Integer>();
		ArrayList<Arc> newArcs = new ArrayList<Arc>();
		HashMap<Integer, ArrayList<Arc>> newA = new HashMap<Integer, ArrayList<Arc>>();
		
		for(int i = 0;i < V.size(); i++)
			newV.add(V.get(i));
		
		ArrayList<Arc> biArcs = new ArrayList<Arc>();
		ArrayList<Arc> uniArcs = new ArrayList<Arc>();
		for(int i = 0; i < Arcs.size(); i++){
			Arc a = Arcs.get(i);
			int u = a.begin;
			int v = a.end;
			if(a.w > lengthThrs){
				boolean bidirectional = false;
				for(int j = 0; j < A.get(v).size(); j++){
					Arc ai = A.get(v).get(j);
					if(ai.end == u){
						bidirectional = true;
						break;
					}
				}
				if(bidirectional){
					biArcs.add(a);
				}else{
					uniArcs.add(a);
				}
			}else{
				newArcs.add(a);
			}
		}
		
		for(int i = 0; i < uniArcs.size(); i++){
			Arc a = uniArcs.get(i);
			int u = a.begin;
			int v = a.end;
			
			//if(a.w > lengthThrs){
				int nbPoints = (int)(a.w/lengthThrs);
				double dLat = (mLatLng.get(v).lat - mLatLng.get(u).lat)/(nbPoints+1);
				double dLng = (mLatLng.get(v).lng - mLatLng.get(u).lng)/(nbPoints+1);
				double dw = a.w/(nbPoints+1);
				//ArrayList<Integer> L = new ArrayList<Integer>();
				int last = u;
				for(int j = 0; j < nbPoints; j++){
					double lat = mLatLng.get(u).lat + (j+1)*dLat;
					double lng = mLatLng.get(u).lng + (j+1)*dLng;
					LatLng ll = new LatLng(lat,lng);
					nID++;
					Arc ai = new Arc(last,nID,dw);
					newArcs.add(ai);
					last = nID;
					mLatLng.put(nID, ll);
					newV.add(nID);
				}
				//maxID = nID;
				Arc ai = new Arc(last,v,dw);
				newArcs.add(ai);
				//map.put(a,L);
			//}else{
				//newArcs.add(a);
			//}
		}
		System.out.println("newV.sz = " + newV.size() + ", biArcs.sz = " + biArcs.size() + ", uniArcs.sz = " + uniArcs.size());
		for(int i = 0; i < biArcs.size(); i++){
			Arc a = biArcs.get(i);
			int u = a.begin;
			int v = a.end;
			
			//if(a.w > lengthThrs){
				int nbPoints = (int)(a.w/lengthThrs);
				double dLat = (mLatLng.get(v).lat - mLatLng.get(u).lat)/(nbPoints+1);
				double dLng = (mLatLng.get(v).lng - mLatLng.get(u).lng)/(nbPoints+1);
				double dw = a.w/(nbPoints+1);
				//ArrayList<Integer> L = new ArrayList<Integer>();
				int last = u;
				for(int j = 0; j < nbPoints; j++){
					double lat = mLatLng.get(u).lat + (j+1)*dLat;
					double lng = mLatLng.get(u).lng + (j+1)*dLng;
					LatLng ll = new LatLng(lat,lng);
					nID++;
					Arc ai1 = new Arc(last,nID,dw);
					newArcs.add(ai1);
					Arc ai2 = new Arc(nID,last,dw);
					newArcs.add(ai2);
					last = nID;
					mLatLng.put(nID, ll);
					newV.add(nID);
				}
				//maxID = nID;
				Arc ai1 = new Arc(last,v,dw);
				newArcs.add(ai1);
				Arc ai2 = new Arc(v,last,dw);
				newArcs.add(ai2);
				//map.put(a,L);
			//}else{
				//newArcs.add(a);
			//}
		}
		
		for(int i = 0; i < newV.size(); i++)
			newA.put(newV.get(i), new ArrayList<Arc>());
		
		for(int i = 0; i < newArcs.size(); i++){
			Arc a = newArcs.get(i);
			int u = a.begin;
			int v = a.end;
			newA.get(u).add(a);
		}
		int newMinID = minID;
		int newMaxID = nID;
		
		RoadMap newMap = new RoadMap();
		newMap.n = newV.size();
		newMap.m = newArcs.size();
		newMap.V = newV;
		newMap.Arcs = newArcs;
		newMap.A = newA;
		newMap.minID = newMinID;
		newMap.maxID = newMaxID;
		newMap.mLatLng = mLatLng;
		
		return newMap;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//RoadMap map = new RoadMap();
		//map.loadData("map-hanoi-connected.txt");
		//map.writeToPolishMap("map-hanoi-connected-polish.mp", 6);
		
		String fM = "SanFranciscoRoad-connected-contracted-5.txt"; 
		RoadMap M = new RoadMap();
		//M.loadData("/Users/dungpq/research/projects/prediction-based-transport-scheduling/data/map-hanoi.txt");
		M.loadData("Sanfrancisco\\" + fM);
		RoadMap newM = M.refineMap(50);
		newM.writeToFile("Sanfrancisco\\SanFranciscoRoad-connected-contracted-5-refine-50.txt");
		newM.writeToPolishMap("Sanfrancisco\\SanFranciscoRoad-connected-contracted-5-refine-50-polish.mp", 6);
		//M.writeToPolishMap("SanFranciscoRoad-connected-polish.mp", 6);
		
		
		//map.loadData("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\SanFrancisco-contracted.txt");
		/*
		int contractLevel = 5;
		RoadMap newMap = M.contract(contractLevel);
		newMap.writeToFile("SanFranciscoRoad-connected-contracted-" + contractLevel + ".txt");
		newMap.writeToPolishMap("SanFranciscoRoad-connected-contracted-" + contractLevel + "-polish.mp", 6);
		*/

		/*
		RoadMap map = new RoadMap();
		//map.loadData("map-hanoi-connected.txt");
		//map.writeToPolishMap("map-hanoi-connected-polish.mp", 6);
		
		//map.loadData("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\Tokyo\\20090101-3\\data01060000\\reduceGraph.txt");
		//map.writeToPolishMap("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\Tokyo\\20090101-3\\data01060000\\reduceGraph-polish.mp", 6);
		
		
		map.loadData("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\SanFranciscoRoad-connected.txt");
		//RoadMap newMap = map.contract(5);
		//RoadMap newMap = map.extractSubMap(37.786203, -122.506563, 37.804639, -122.474640);
		map.writeToPolishMap("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\SanfranciscoRoad-connected-polish.mp", 6);
		
		int contractLevel = 5;
		RoadMap newMap = map.contract(contractLevel);
		newMap.writeToFile("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\SanfranciscoRoad-connected-contracted-" + contractLevel + ".txt");
		newMap.writeToPolishMap("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\SanfranciscoRoad-connected-contracted-" + contractLevel + "-polish.mp", 6);
		//newMap.writeToFile("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\SanFrancisco-contracted-5.txt");
		*/

		//map.analyze("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\SanFrancisco-contracted.txt");
	}

}
