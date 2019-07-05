package pbts.entities;

public class Square {

	public int ID;
	public int row_idx;
	public int col_idx;
	public double min_lat;
	public double min_lng;
	public double max_lat;
	public double max_lng;
	
	public boolean checkIn(double lat, double lng){
		return min_lat <= lat && lat <= max_lat && min_lng <= lng && lng <= max_lng;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
