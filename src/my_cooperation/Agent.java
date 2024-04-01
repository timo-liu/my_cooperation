package my_cooperation;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.distribution.Normal;
import java.util.Random;

import cooperation.Agent;
import cooperation.Environment;

import java.lang.Math;

public class Agent implements Steppable {
	float tolerance; //on an agent by agent basis, how long can this agent "tolerate" a negative group output from other agents
	float mean_value; // mean value that an agent will contribute to a team
	float std_value; //stdev of value that an agent will contribute
	int strikes = 0; //setting strikes before leaving
	
	int id; // agent unique id
	int x; // x pos for visualization
	int y; // y pos for visualization
	int group_id; //current group identity
	int type; // type 1 = deviant, type 0 = standard
	
	double[] memory; //tracks familiarity between groups so that they are more likely to join groups that they haven't worked with already
	
	boolean in_group = true; //if the agent is in a group, and is unsatisfied, then leave. otherwise, try and find an open group
	
	float accumulated_payoff = 0; // trying to maximize this across time steps
	
	Normal my_distribution; //this agent's payoff distribution handler
	
	public Agent(Environment state, float tolerance, float mean_value, float std_value, int x, int y, int id, int group_id, int type) {
		// TODO Auto-generated constructor stub
		super();
		this.tolerance = tolerance;
		this.mean_value = mean_value;
		this.std_value = std_value;
		this.group_id = group_id;
		this.type = type;
		
		this.id = id;
		this.x = x;
		this.y = y;
		
		this.memory = new double[state.num_groups];
		
		my_distribution = new Normal(mean_value, std_value, state.random);	
	}
	
	public float contribute() {
		return (float) this.my_distribution.nextDouble();
	}
	
	public void evaluate(Environment state, float step_payoff) {
		
		float compared_payoff = step_payoff;
		
		if(this.in_group) {
			Group g = (Group) state.groups.get(this.group_id); //get the group, and see how many members there are
			float num_in_group = (float) g.curr_agents.size();
			compared_payoff = step_payoff/num_in_group;
			this.accumulated_payoff += compared_payoff;
			
			if (compared_payoff < this.mean_value && Math.abs(this.mean_value - compared_payoff) > this.tolerance) {
				this.strikes++;
				if(this.strikes >= state.max_strikes) {
					this.in_group = false;
					this.group_id = -1;
					g.remove_agent(this);
					this.strikes = 0;

				}
			}
		}
		else {
			if (compared_payoff < this.mean_value && Math.abs(this.mean_value - compared_payoff) > this.tolerance) {
				this.accumulated_payoff += compared_payoff;
				this.strikes++;
				if(this.strikes >= state.max_strikes) {

						Group g = find_new_group(state);
						if (g != null) {
							this.in_group = true;
							this.group_id = g.group_id;
							g.add_agent(this);
							}
				}	
			}
		}
	}
	
	
	//rework to make it meet a group
	public Group find_new_group(Environment state) {
		double[] chance_array = new double[state.num_groups];
		int randomnum = state.random.nextInt(99);
		
		double lower = 0;
		double higher = 0;
		
		double fSum = 0;
		
		for(int i = 0; i < this.memory.length; i++) {
			if(((Group)state.groups.get(i)).curr_agents.size() < state.num_agents_per_group) {
				fSum = fSum + this.memory[i];
			}
		}
		
		for(int i = 0; i < this.memory.length; i++) {
			if(((Group)state.groups.get(i)).curr_agents.size() < state.num_agents_per_group) {
				chance_array[i] = ((fSum - this.memory[i])/fSum) * 100.0;
			}
			else {
				chance_array[i] = 0.0;
			}
			
		}
		
		for(int i = 0; i < state.num_groups; i++) {
			lower = higher;
			higher = higher + chance_array[i];
			if(randomnum >= lower && randomnum < higher) {
				return ((Group)state.groups.get(i));
			}
		}
		return null;
	}
	
	public float get_step_payoff(Environment state) {
		if(this.in_group) {
			Group g = (Group) state.groups.get(this.group_id);
			return g.group_payoff;
		}
		else {
			return this.contribute();
		}
	}
	
	
	public void move(Environment state) {
		if(this.in_group) {
			Group g = (Group)state.groups.get(this.group_id);
			
			int random_x = state.random.nextInt(5) + g.x;
			int random_y = state.random.nextInt(5) + g.y;
			
			Bag b = state.sparseSpace.getObjectsAtLocation(random_x, random_y);
			
			while(!(b == null)) {
				random_x = state.random.nextInt(5) + g.x;
				random_y = state.random.nextInt(5) + g.y;
				b = state.sparseSpace.getObjectsAtLocation(random_x, random_y);
			}
			state.sparseSpace.setObjectLocation(this, random_x, random_y);
		}
		else {
			int random_x = state.random.nextInt(state.gridWidth);
			int random_y = state.random.nextInt(state.gridHeight);
			
			Bag b = state.sparseSpace.getObjectsAtLocation(random_x, random_y);
			
			while(!(b == null)) {
				random_x = state.random.nextInt(state.gridWidth);
				random_y = state.random.nextInt(state.gridHeight);
				b = state.sparseSpace.getObjectsAtLocation(random_x, random_y);
			}
			state.sparseSpace.setObjectLocation(this, random_x, random_y);
		}
	}

	@Override
	public void step(SimState state) {
		Environment e = (Environment) state;
		float step_payoff = get_step_payoff(e);
		evaluate(e, step_payoff);
		move(e);
	}

}
