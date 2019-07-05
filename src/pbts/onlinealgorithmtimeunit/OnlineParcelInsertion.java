package pbts.onlinealgorithmtimeunit;

import java.util.ArrayList;

import pbts.entities.ParcelRequest;

public interface OnlineParcelInsertion {
	public void processParcelRequests(ArrayList<ParcelRequest> pr, double startDecideTime);
}
