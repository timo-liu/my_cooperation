package my_cooperation;

import observer.Observer;
import sim.engine.SimState;
import sweep.ParameterSweeper;
import sweep.SimStateSweep;
import sim.util.Bag;
import sim.engine.Steppable;

public class Experimenter extends Observer implements Steppable {
	
	float avg_deviant_payoff;
	float  avg_standard_payoff;
	
	public Experimenter(String fileName, String folderName, SimStateSweep state, ParameterSweeper sweeper,
			String precision, String[] headers) {
		super(fileName, folderName, state, sweeper, precision, headers);
		// TODO Auto-generated constructor stub
	}
	
//	public void stop(Environment state) {
//		if(state.schedule.getSteps() >= state.simLength) {
//			event.stop();
//		}
//	}
	
	public boolean reset (SimState state) {
		avg_deviant_payoff = 0;
		avg_standard_payoff = 0;
        return true;
    }
	
	
	/**
	 * This method collects data for automated simulation sweeps.  Behind the scenes, data are stored in arrays
	 * that allow the calculations of means and standard deviations between simulation runs.
	 * @return
	 */
	public boolean nextInterval() {
		data.add(this.avg_deviant_payoff);
		data.add(this.avg_standard_payoff);
		return false;
	}

	@Override
	public void step(SimState state) {
	       super.step(state);
	       if(((Environment)state).paramSweeps && getdata) {
	    	   reset();
	    	   group_count((Environment)state);
	    	   avg_acc_payoff((Environment)state);
	    	   nextInterval();
	       }
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
        
        this.avg_deviant_payoff = deviant_avg;
        this.avg_standard_payoff = standard_avg;
        
       //double time = (double)state.schedule.getTime();//get the current time
       //this.upDateTimeChart(0,time, deviant_avg , true, 1000);//update the chart with up to a 1000 milisecond delay
       //this.upDateTimeChart(1,time, standard_avg, true, 1000);//update the chart with up to a 1000 milisecond delay
	}
	
	public void group_count(Environment state) {
		Bag agents = state.sparseSpace.allObjects;//get remaining agents
		
		for (Object b:agents) {
			if(b.getClass() == Group.class) {
				agents.remove(b);
			}
		}
		
		double [] data = new double[agents.numObjs];
		for(int i = 0;i<data.length;i++) {
			Agent a = (Agent)agents.objs[i];
			data[i] = a.group_id;
		}
		//this.upDateHistogramChart(0, (int)state.schedule.getSteps(), data, 100);
		
	}
}
