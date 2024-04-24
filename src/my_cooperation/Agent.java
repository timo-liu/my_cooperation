package my_cooperation;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Bag;
import sim.util.distribution.Normal;
import java.util.Random;

import java.lang.Math;

public class Agent implements Steppable {
	//to track
	public Stoppable event;
	public double tolerance; //on an agent by agent basis, how long can this agent "tolerate" a negative group output from other agents
	public double mean_value; // mean value that an agent will contribute to a team
	public double std_value; //stdev of value that an agent will contribute
	public int group_count; //num agents in group the agent is in at the current step
	public int group_id;
	public int id; // agent unique id
	public int type; // type 1 = deviant, type 0 = standard
	public int num_standards_in_group; //tracking the number of standards in their group if they're in one
	public int num_deviants_in_group; // tracking the number of deviants in their group if they're in one
	public boolean in_group = true; //if the agent is in a group, and is unsatisfied, then leave. otherwise, try and find an open group
	public double accumulated_payoff = 0; // trying to maximize this across time step
	public int strikes = 0; //setting strikes before leaving
	public int x; // x pos for visualization
	public int y; // y pos for visualization[
	public boolean skip_step = false;
	
	double[] memory; //tracks familiarity between groups so that they are more likely to join groups that they haven't worked with already
	
	Normal my_distribution; //this agent's payoff distribution handler
	
	public Agent(Environment state, double tolerance, double mean_value, double std_value, int x, int y, int id, int group_id, int type) {
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
	
	public double contribute() {
		return this.my_distribution.nextDouble();
	}
	
	public void evaluate(Environment state, double step_payoff) {
		
		double compared_payoff = step_payoff;
		
		if(this.in_group) {
			Group g = (Group) state.groups.get(this.group_id); //get the group, and see how many members there are
			double num_in_group = g.curr_agents.size();
			compared_payoff = step_payoff/num_in_group;
			if (!this.skip_step) {
				this.accumulated_payoff += compared_payoff;
			}
			else {
				this.skip_step = false;
			}
			
			if (compared_payoff < this.mean_value && Math.abs(this.mean_value - compared_payoff) > this.tolerance) {
				this.strikes++;
				if(this.strikes >= state.max_strikes) {
					this.skip_step = true;
					this.in_group = false;
					this.group_id = -1;
					g.remove_agent(this);
					this.strikes = 0;

				}
			}
		}
		else {
			if (compared_payoff < this.mean_value && Math.abs(this.mean_value - compared_payoff) > this.tolerance) {
				if (!this.skip_step) {
					this.accumulated_payoff += compared_payoff;
				}
				else {
					this.skip_step = false;
				}
				this.strikes++;
				if(this.strikes >= state.max_strikes) {

						Group g = find_new_group(state);
						if (g != null) {
							this.skip_step = true;
							this.in_group = true;
							this.group_id = g._group_id;
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
	
	public double get_step_payoff(Environment state) {
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
			
//			Bag b = state.sparseSpace.getObjectsAtLocation(random_x, random_y);
//			
//			while(!(b == null)) {
//				random_x = state.random.nextInt(5) + g.x;
//				random_y = state.random.nextInt(5) + g.y;
//				b = state.sparseSpace.getObjectsAtLocation(random_x, random_y);
//			}
//			state.sparseSpace.setObjectLocation(this, random_x, random_y);
		}
		else {
			int random_x = state.random.nextInt(state.gridWidth);
			int random_y = state.random.nextInt(state.gridHeight);
			
//			Bag b = state.sparseSpace.getObjectsAtLocation(random_x, random_y);
//			
//			while(!(b == null)) {
//				random_x = state.random.nextInt(state.gridWidth);
//				random_y = state.random.nextInt(state.gridHeight);
//				b = state.sparseSpace.getObjectsAtLocation(random_x, random_y);
//			}
//			state.sparseSpace.setObjectLocation(this, random_x, random_y);
		}
	}
	
	public void count_group(Environment state) {
		if (this.in_group) {
			Group g = (Group) state.groups.get(this.group_id);
			this.group_count = g.curr_agents.numObjs;
			int[] t = g.group_constitution();
			this.num_deviants_in_group = t[1];
			this.num_standards_in_group = t[0];
		}
		else {
			this.group_count = 0;
			this.num_deviants_in_group = 0;
			this.num_standards_in_group = 0;
		}
	}

	@Override
	public void step(SimState state) {
		Environment e = (Environment) state;
		double step_payoff = get_step_payoff(e);
		evaluate(e, step_payoff);
		//move(e);
		count_group(e);
	}

//	public double getTolerance() {
//		return tolerance;
//	}
//
//	public void setTolerance(float tolerance) {
//		this.tolerance = tolerance;
//	}
//
//	public double getMean_value() {
//		return mean_value;
//	}
//
//	public void setMean_value(float mean_value) {
//		this.mean_value = mean_value;
//	}
//
//	public double getStd_value() {
//		return std_value;
//	}
//
//	public void setStd_value(float std_value) {
//		this.std_value = std_value;
//	}
//
//	public int getGroup_count() {
//		return group_count;
//	}
//
//	public void setGroup_count(int group_count) {
//		this.group_count = group_count;
//	}
//
//	public int getGroup_id() {
//		return group_id;
//	}
//
//	public void setGroup_id(int group_id) {
//		this.group_id = group_id;
//	}
//
//	public int getId() {
//		return id;
//	}
//
//	public void setId(int id) {
//		this.id = id;
//	}
//
//	public int getType() {
//		return type;
//	}
//
//	public void setType(int type) {
//		this.type = type;
//	}
//
//	public int getNum_standards_in_group() {
//		return num_standards_in_group;
//	}
//
//	public void setNum_standards_in_group(int num_standards_in_group) {
//		this.num_standards_in_group = num_standards_in_group;
//	}
//
//	public int getNum_deviants_in_group() {
//		return num_deviants_in_group;
//	}
//
//	public void setNum_deviants_in_group(int num_deviants_in_group) {
//		this.num_deviants_in_group = num_deviants_in_group;
//	}
//
//	public boolean isIn_group() {
//		return in_group;
//	}
//
//	public void setIn_group(boolean in_group) {
//		this.in_group = in_group;
//	}
//
//	public double getAccumulated_payoff() {
//		return accumulated_payoff;
//	}
//
//	public void setAccumulated_payoff(float accumulated_payoff) {
//		this.accumulated_payoff = accumulated_payoff;
//	}
//	
//	public Stoppable getEvent() {
//		return event;
//	}
//	public void setEvent(Stoppable event) {
//		this.event = event;
//	}

}
