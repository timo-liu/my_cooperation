package my_cooperation;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.distribution.Beta;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Group implements Steppable {
	
	// variables to handle grade returning
	Beta grade_error_beta_func;
	public double grade_error_beta;
	public double grade_error_alpha;
	
	public Bag curr_agents = new Bag();
	public int group_id;
	public int x;
	public int y;
	
	public int group_count;
	
	public double group_payoff;

	public Group(int x, int y, int group_id) {
		super();
		this.x = x;
		this.y = y;
		this.group_id = group_id;
	}
	
	public void add_agent(Agent a) {
		this.curr_agents.add(a);
	}
	
	public void remove_agent(Agent a) {
		if (this.curr_agents.contains(a)) {
			this.curr_agents.remove(a);
		}
	}
	
	public void calc_group_payoff(Environment state) {
		
		float sum = 0;
		
		if(!this.curr_agents.isEmpty()) {
			for (Object b: this.curr_agents) {
				Agent a  = (Agent) b;
				sum += a.contribute();
			}
		}
		this.group_payoff = update_group_count_and_beta(state, sum/(double)this.group_count);
	}
	
	public double update_group_count_and_beta(Environment state, double group_payoff) {
		// TODO
		// Calculate a new beta/alpha based on the proportion of agents to state.min_agents_in_group
		if (this.group_count != this.curr_agents.numObjs) {
			// TODO Update grade_error_beta_func, otherwise leave it
		}
		
		if (state.letter_grades) {
			//TODO return some bucketed grades
			return 1.0;
		}
		else {
			return this.grade_error_beta_func.nextDouble();
		}
	}
	
	@Override
	public void step(SimState state) {
		calc_group_payoff((Environment) state);
	}

}
