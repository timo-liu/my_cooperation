package my_cooperation;

import sim.engine.SimState;
import sim.util.Bag;
import sim.engine.Steppable;
import sim.engine.Stoppable;

public class Experimenter implements Steppable {
	
	public Stoppable event;
	
	public Experimenter() {

	}
	
	public void stop(Environment state) {
	}
	
	public boolean reset (Environment state) {
        return true;
    }
	
	@Override
	public void step(SimState state) {
		Environment estate = (Environment)state;
		reset(estate);
	    stop(estate);   
	}
}
