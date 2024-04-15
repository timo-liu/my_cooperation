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
		state.num_deviants = 0;
		state.num_standards = 0;
        return true;
    }
	
	@Override
	public void step(SimState state) {
		Environment estate = (Environment)state;
			reset(estate);
			group_constitution(estate);
	    	avg_acc_payoff(estate);
	    	stop(estate);   
	}
	
	
	public void avg_acc_payoff(Environment state) {
		float deviant_sum = 0;
		float sum = 0;
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
        
        float deviant_avg = deviant_sum/((float)state.num_deviants);
        float standard_avg = sum/((float)state.num_standards);
        
        state.avg_deviant_payoff = deviant_avg;
        state.avg_standard_payoff = standard_avg;
        
       //double time = (double)state.schedule.getTime();//get the current time
       //this.upDateTimeChart(0,time, deviant_avg , true, 1000);//update the chart with up to a 1000 milisecond delay
       //this.upDateTimeChart(1,time, standard_avg, true, 1000);//update the chart with up to a 1000 milisecond delay
	}
	
	public void group_constitution(Environment state) {
		for(int i = 0; i < state.num_groups; i++ ) {
			int num_deviants = 0;
			int total = 0;
			Group g = (Group) state.groups.get(i);
			for (Object a:g.curr_agents) {
				Agent b = (Agent) a;
				total++;
				if (b.type == 1) {
					num_deviants++;
				}
			}
			state.group_totals[i] = total;
			state.group_deviants[i] = num_deviants;
		}
	}
}
