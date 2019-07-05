package pbts.onlinealgorithmtimeunit;

import java.util.ArrayList;

import pbts.entities.PeopleRequest;

public interface OnlinePeopleInsertion {
	public void processPeopleRequests(ArrayList<PeopleRequest> pr, double startDecideTime);
}
