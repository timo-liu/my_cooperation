package my_cooperation;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Bag;
import sim.util.distribution.Normal;
import sim.util.distribution.Beta;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.File;
//Writing data manually to file
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Math;

public class Agent implements Steppable {
	public Stoppable event;
	//to track
	public double tolerance; //on an agent by agent basis, how long can this agent "tolerate" a negative group output from other agents
	public double mean_value; // mean value that an agent will contribute to a team
	public double std_value; //stdev of value that an agent will contribute
	public int group_count; //num agents in group the agent is in at the current step
	public int group_id;
	public int id; // agent unique id
	public double accumulated_payoff = 0; // trying to maximize this across time step
	public int strikes = 0; //setting strikes before leaving
	public int x; // x pos for visualization
	public int y; // y pos for visualization
	//directory storing agent files
	public String write_directory = "Python/Agent_data/";
	public String file_path;
	public String step_grade = "n/a"; //default empty unless we are using grade, then grades will update with letters
	public Boolean loafing_detected = false;
	
	//Grade handling
	public String[] grade_array = {"F-", "F", "F+",
									"D-", "D", "D+",
									"C-", "C", "C+",
									"B-", "B", "B+",
									"A-", "A", "A+"};
	public int grade_mean_index; // 0 for F-, 1 for F, and so on
	public int grade_tolerance;
	
	//Agent level tracked?
	public int num_groups;
	
	Normal my_distribution; //this agent's payoff distribution handler
	// Beta my_distribution; //uncomment when I'm ready to use beta functions
	
	public Agent(Environment state, double tolerance, double mean_value, double std_value, int x, int y, int id, int group_id, String prefix) {
		// TODO Auto-generated constructor stub
		super();
		this.tolerance = tolerance;
		this.mean_value = mean_value;
		this.std_value = std_value;
		this.group_id = group_id;
		this.id = id;
		this.x = x;
		this.y = y;
		this.grade_tolerance = Math.min((int) (tolerance * 14.0), 4);
		this.grade_mean_index = percentage_to_grade(mean_value * 100.0);
		
		my_distribution = new Normal(mean_value, std_value, state.random);	
	}
	
	public double contribute() {
		return this.my_distribution.nextDouble();
	}
	
	public void evaluate(Environment state, double compared_payoff) {
		SimState s = (SimState) state;
		double current_steps = (double) s.schedule.getSteps();
		
		Group g = (Group) state.groups.get(this.group_id - 1);
		
		this.accumulated_payoff = this.accumulated_payoff + compared_payoff;
		
		if (!state.letter_grades) {
			if (compared_payoff < this.mean_value && Math.abs(this.mean_value - compared_payoff) > this.tolerance) {
				this.strikes++;
				this.loafing_detected = true;
				g.loafing_detected = true;
				if(this.strikes >= state.max_strikes) {
					//leave group, find another
					// if all groups full, make a new one
					
					// TODO impose a PUNISHMENT!!!!!!
					double punishment = state.divorce_constant * Math.log(current_steps); // for interpretation's sake, we are not actually punishing the students, don't investigate me IRB.
					this.accumulated_payoff = this.accumulated_payoff - punishment;
					
					g.remove_agent(this);
					this.strikes = 0;
					
					g = find_new_group(state, this.group_id);
					if (g != null) {
						this.group_id = g.group_id;
						g.add_agent(this);
						}
					else {
						state.make_new_group(this);
					}	
				}
			}
			else {
				g.loafing_detected = false;
				this.loafing_detected = false;
			}
		}
			else {
				int step_grade = percentage_to_grade(compared_payoff);
				this.step_grade = grade_array[step_grade];
				if (this.grade_mean_index > step_grade && step_grade < this.grade_mean_index - this.grade_tolerance) {
					this.loafing_detected = true;
					g.loafing_detected = true;
					this.strikes++;
					if(this.strikes >= state.max_strikes) {
						//leave group, find another
						// if all groups full, make a new one
						
						// TODO impose a PUNISHMENT!!!!!!
						double punishment = state.divorce_constant * Math.log(current_steps); // for interpretation's sake, we are not actually punishing the students, don't investigate me IRB.
						this.accumulated_payoff = this.accumulated_payoff - punishment;
						
						g.remove_agent(this);
						this.strikes = 0;
						
						g = find_new_group(state, this.group_id);
						if (g != null) {
							this.group_id = g.group_id;
							g.add_agent(this);
							}
						else {
							state.make_new_group(this);
						}	
					}
				}
				else {
					this.loafing_detected = false;
					g.loafing_detected = false;
				}
			}
	}
	
	public int percentage_to_grade(double percentage) {
		if (percentage >= 97.0) {
			return 14;
		}
		if (percentage >= 93.0) {
			return 13;
		}
		if (percentage >= 90.0) {
			return 12;
		}
		if (percentage >= 87.0) {
			return 11;
		}
		if (percentage >= 83.0) {
			return 10;
		}
		if (percentage >= 80.0) {
			return 9;
		}
		if (percentage >= 77.0) {
			return 8;
		}
		if (percentage >= 73.0) {
			return 7;
		}
		if (percentage >= 70.0) {
			return 6;
		}
		if (percentage >= 67.0) {
			return 5;
		}
		if (percentage >= 63.0) {
			return 4;
		}
		if (percentage >= 60.0) {
			return 3;
		}
		if (percentage >= 57.0) {
			return 2;
		}
		if (percentage >= 53.0) {
			return 1;
		}
		return 0;
	}
	
	//rework to make it meet a group
	public Group find_new_group(Environment state, int last_group) {
		double[] chance_array = new double[state.num_groups];
		int randomnum = state.random.nextInt(99);
		
		double lower = 0;
		double higher = 0;
		
		double fSum = 0;
		// not currently implemented to evenly distribute agents
		boolean everything_full = true;
		for (Object element : state.groups.objs) {
            Group g = (Group) element;
            if (g != null && g.group_count < state.max_agents_per_group && g.group_id != last_group) {
            	everything_full = false;
            }
        }
		if (everything_full) {
			return null;
		}
		else {
			int temp = state.random.nextInt(state.num_groups);
			while(temp == last_group || ((Group)state.groups.get(temp)).group_count >= state.max_agents_per_group ) {
				temp = state.random.nextInt(state.num_groups);
			}
			return ((Group)state.groups.get(temp));
		}
	}
	
	public double get_step_payoff(Environment state) {
		Group g = (Group) state.groups.get(this.group_id - 1);
		return g.group_payoff;
	}

	public int getNum_groups() {
		return num_groups;
	}
	
	public String getStep_grade() {
		return step_grade;
	}

	public void setStep_grade(String step_grade) {
		this.step_grade = step_grade;
	}

	public Boolean getLoafing_detected() {
		return loafing_detected;
	}

	public void setLoafing_detected(Boolean loafing_detected) {
		this.loafing_detected = loafing_detected;
	}

	public void setNum_groups(int num_groups) {
		this.num_groups = num_groups;
	}

	public void move(Environment state) {
			Group g = (Group)state.groups.get(this.group_id - 1);
			
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

	@Override
	public void step(SimState state) {
		Environment e = (Environment) state;
		double step_payoff = get_step_payoff(e);
		evaluate(e, step_payoff);
		// System.out.println("Stuck somehere?");
		//move(e);
	}
}
