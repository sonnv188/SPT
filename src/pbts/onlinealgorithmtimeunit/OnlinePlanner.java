package pbts.onlinealgorithmtimeunit;
import java.util.ArrayList;

import pbts.entities.*;
public interface OnlinePlanner {
	public void processPeopleRequest(PeopleRequest pr);
	public void processParcelRequest(ParcelRequest pr);
	public void processPeopleRequests(ArrayList<PeopleRequest> pr);
	public void processParcelRequests(ArrayList<ParcelRequest> pr);
	public String name();
}
