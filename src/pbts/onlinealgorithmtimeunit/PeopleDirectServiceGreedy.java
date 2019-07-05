package pbts.onlinealgorithmtimeunit;

import java.io.PrintWriter;
import java.util.ArrayList;

import pbts.entities.ParcelRequest;
import pbts.entities.PeopleRequest;
import pbts.simulation.SimulatorTimeUnit;

public class PeopleDirectServiceGreedy implements OnlinePlanner {
	public PrintWriter log = null;
	public SimulatorTimeUnit sim = null;

	public OnlinePeopleInsertion peopleInserter = null;
	public OnlineParcelInsertion parcelInserter = null;

	public PeopleDirectServiceGreedy(SimulatorTimeUnit sim){
		this.sim = sim;
		if(sim != null)this.log = sim.log;
		
		peopleInserter = new PeopleInsertionDirectFirstServiceGreedy(sim);
		parcelInserter = new ParcelInsertionPeopleDirectServiceGreedy(sim);
	}
	public void setSimulator(SimulatorTimeUnit sim){
		this.sim = sim;
		if(sim != null) log = sim.log;
	}
	public String name(){ return "PeopleDirectServiceGreedy";}
	public void processPeopleRequest(PeopleRequest pr) {
		// TODO Auto-generated method stub
		System.out.println(name() + "::processPeopleRequest NOT IMPLEMENTED");
		sim.exit();
	}

	public void processParcelRequest(ParcelRequest pr) {
		// TODO Auto-generated method stub
		System.out.println(name() + "::processParcelRequest NOT IMPLEMENTED");
		sim.exit();
	}

	public void processPeopleRequests(ArrayList<PeopleRequest> pr) {
		// TODO Auto-generated method stub
		double startDecideTime = System.currentTimeMillis();
		peopleInserter.processPeopleRequests(pr, startDecideTime);
	}

	public void processParcelRequests(ArrayList<ParcelRequest> pr) {
		// TODO Auto-generated method stub
		double startDecideTime = System.currentTimeMillis();
		parcelInserter.processParcelRequests(pr, startDecideTime);
	}

}
