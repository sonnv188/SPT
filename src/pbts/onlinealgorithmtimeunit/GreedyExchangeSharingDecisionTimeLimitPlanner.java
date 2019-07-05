package pbts.onlinealgorithmtimeunit;

import java.io.PrintWriter;
import java.util.ArrayList;

import pbts.entities.ParcelRequest;
import pbts.entities.PeopleRequest;
import pbts.itineraryoptimizer.ExchangeRequestMove;
import pbts.itineraryoptimizer.ItineraryOptimizer;
import pbts.simulation.SimulatorTimeUnit;

public class GreedyExchangeSharingDecisionTimeLimitPlanner implements OnlinePlanner {
	public PrintWriter log = null;
	public SimulatorTimeUnit sim = null;

	public OnlinePeopleInsertion peopleInserter = null;
	public OnlineParcelInsertion parcelInserter = null;
	public ItineraryOptimizer IO;
	
	public GreedyExchangeSharingDecisionTimeLimitPlanner(SimulatorTimeUnit sim){
		this.sim = sim;
		if(sim != null)this.log = sim.log;
		peopleInserter = new PeopleInsertionGreedySharingDecisionTimeLimit(sim);
		parcelInserter = new ParcelInsertionGreedySharingDecisionTimeLimit(sim);
		IO = new ItineraryOptimizer(sim);
	}
	public void setSimulator(SimulatorTimeUnit sim){
		this.sim = sim;
		if(sim != null) log = sim.log;
	}
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
	public String name(){ return "GreedyExchangeSharingDecisionTimeLimitPlanner";}
	
	public void processPeopleRequests(ArrayList<PeopleRequest> pr) {
		// TODO Auto-generated method stub
		double t0 = System.currentTimeMillis();
		peopleInserter.processPeopleRequests(pr, t0);
		double t = System.currentTimeMillis() - t0;
		t = t*0.001;
		if(sim.maxDecideTimePeopleRequests < t) sim.maxDecideTimePeopleRequests = t;
		IO.moveGreedyExchange(t0);
	}

	public void processParcelRequests(ArrayList<ParcelRequest> pr) {
		// TODO Auto-generated method stub
		double t0 = System.currentTimeMillis();
		parcelInserter.processParcelRequests(pr,t0);
		double t = System.currentTimeMillis() - t0;
		t = t * 0.001;
		if(sim.maxDecideTimeParcelRequests < t) sim.maxDecideTimeParcelRequests = t;
		IO.moveGreedyExchange(t0);
	}

}
