package my_cooperation;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.distribution.Normal;
import java.util.Random;
import java.lang.Math;

public class Agent implements Steppable {
	float tolerance; //on an agent by agent basis, how long can this agent "tolerate" a negative group output from other agents
	float mean_value; // mean value that an agent will contribute to a team
	float std_value; //stdev of value that an agent will contribute
	
	int id; // agent unique id
	int x; // x pos for visualization
	int y; // y pos for visualization
	int group_id; //current group identity
	int type; // type 1 = deviant, type 0 = standard
	
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
		
		my_distribution = new Normal(mean_value, std_value, state.random);	
	}
	
	public float contribute() {
		return (float) this.my_distribution.nextDouble();
	}
	
	public void evaluate(Environment state, float step_payoff) {
		
		this.accumulated_payoff += step_payoff;
		float compared_payoff = step_payoff;
		
		if(this.in_group) {
			Group g = (Group) state.groups.get(this.group_id); //get the group, and see how many members there are
			float num_in_group = (float) g.curr_agents.size();
			compared_payoff = step_payoff/num_in_group;
			
			if (compared_payoff < this.mean_value && Math.abs(this.mean_value - compared_payoff) > this.tolerance) {
				this.in_group = false;
				this.group_id = -1;
				g.remove_agent(this);
			}
		}
		else {
			if (compared_payoff < this.mean_value && Math.abs(this.mean_value - compared_payoff) > this.tolerance) {
				for(Object b:state.groups) {
					Group g = (Group) b;
					if (g.curr_agents.size() < state.num_agents_per_group) {
						this.in_group = true;
						this.group_id = g.group_id;
						g.add_agent(this);
						break;
					}
				}
			}
		}
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
