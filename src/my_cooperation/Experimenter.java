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
		state.groups = new Bag();
		state.agents = new Bag();
        return true;
    }
	
	@Override
	public void step(SimState state) {
		Environment estate = (Environment)state;
			reset(estate);
			group_count(estate);
	    	avg_acc_payoff(estate);
	    	stop(estate);   
	}
	
	
	public void avg_acc_payoff(Environment state) {
		float deviant_sum = 0;
		int deviant_count = 0;
		float sum = 0;
		int standard_count = 0;
		Bag agents = state.agents;
        int n = 0;//variable for counting frozen agents
        for(int i=0; i< agents.numObjs; i++) {
            Agent a = (Agent)agents.objs[i];
            if(a.type == 0) {
            	standard_count++;
            	sum += a.accumulated_payoff;
            }
            else if(a.type == 1) {
            	deviant_count++;
            	deviant_sum += a.accumulated_payoff;
            }
        }
        
        float deviant_avg = deviant_sum/(float)deviant_count;
        float standard_avg = sum/(float)standard_count;
        
        state.avg_deviant_payoff = deviant_avg;
        state.avg_standard_payoff = standard_avg;
        
       //double time = (double)state.schedule.getTime();//get the current time
       //this.upDateTimeChart(0,time, deviant_avg , true, 1000);//update the chart with up to a 1000 milisecond delay
       //this.upDateTimeChart(1,time, standard_avg, true, 1000);//update the chart with up to a 1000 milisecond delay
	}
	
	public void group_count(Environment state) {
		double [] data = new double[state.agents.numObjs];
		for(int i = 0;i<data.length;i++) {
			Agent a = (Agent)state.agents.objs[i];
			data[i] = a.group_id;
		}
		//this.upDateHistogramChart(0, (int)state.schedule.getSteps(), data, 100);
		
	}
}
