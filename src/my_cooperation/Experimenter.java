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
		state.avg_deviant_payoff = 0;
		state.avg_standard_payoff = 0;
        return true;
    }
	
	@Override
	public void step(SimState state) {
		Environment estate = (Environment)state;
		reset(estate);
	    avg_acc_payoff(estate);
	    stop(estate);   
	}
	
	
	public void avg_acc_payoff(Environment state) {
		double deviant_sum = 0;
		double sum = 0;
		int standard_count = 0;
		Bag agents = state.agents;
        int n = 0;//variable for counting frozen agents
        for(int i=0; i< agents.numObjs; i++) {
            Agent a = (Agent)agents.objs[i];
            if(a.type == 0) {
            	sum += a.accumulated_payoff;
            }
            else if(a.type == 1) {
            	deviant_sum += a.accumulated_payoff;
            }
        }
        
        double deviant_avg = deviant_sum/((double)state.num_deviants);
        double standard_avg = sum/((double)state.num_standards);
        
        state.avg_deviant_payoff = deviant_avg;
        state.avg_standard_payoff = standard_avg;
        
       //double time = (double)state.schedule.getTime();//get the current time
       //this.upDateTimeChart(0,time, deviant_avg , true, 1000);//update the chart with up to a 1000 milisecond delay
       //this.upDateTimeChart(1,time, standard_avg, true, 1000);//update the chart with up to a 1000 milisecond delay
	}
}
