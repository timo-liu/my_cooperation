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
	public boolean loafing_detected = false;
	public double group_payoff;
	public String step_grade = "n/a";
	
	public String[] grade_array = {"F-", "F", "F+",
			"D-", "D", "D+",
			"C-", "C", "C+",
			"B-", "B", "B+",
			"A-", "A", "A+"};

	public Group(Environment state, int x, int y, int group_id) {
		super();
		this.x = x;
		this.y = y;
		this.group_id = group_id;
		this.grade_error_beta_func = new Beta(state.grading_error_alpha, state.grading_error_beta, state.random);
	}
	
	public void add_agent(Agent a) {
		this.curr_agents.add(a);
		this.group_count = this.curr_agents.size();
	}
	
	public void remove_agent(Agent a) {
		if (this.curr_agents.contains(a)) {
			this.curr_agents.remove(a);
			this.group_count = this.curr_agents.size();
		}
	}
	
	public void calc_group_payoff(Environment state) {
		double sum = 0;
		
		if(!this.curr_agents.isEmpty()) {
			for (Object b: this.curr_agents) {
				Agent a  = (Agent) b;
				sum += a.contribute();
			}
		}
		this.group_payoff = update_group_count_and_beta(state, sum);
	}
	
	public double update_group_count_and_beta(Environment state, double group_payoff) {
		// TODO
		// Calculate a new beta/alpha based on the proportion of agents to state.min_agents_in_group
		if (this.group_count != this.curr_agents.numObjs) {
			this.group_count = this.curr_agents.numObjs;
			double new_alpha = state.grading_error_alpha - (25.0 * Math.max(0, (state.min_agents_per_group - this.group_count)));
			this.grade_error_beta_func = new Beta(state.grading_error_alpha, state.grading_error_beta, state.random); //TODO add function for modifying based on group prop
		}
		group_payoff = group_payoff/(double)this.group_count;
		if(state.letter_grades) {
			double unnormed = (this.grade_error_beta_func.nextDouble() + 0.5) * group_payoff;
			//normalizing to upper bound
			double normed = (unnormed) * 100.0;
			
			this.step_grade = grade_array[percentage_to_grade(normed)];
			
			return normed;
		}
		else {
			return this.grade_error_beta_func.nextDouble() * group_payoff;
		}
	}
	
	@Override
	public void step(SimState state) {
		calc_group_payoff((Environment) state);
	} // Generate getters and setters past here
	
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
	
	public int getGroup_count() {
		return group_count;
	}

	public void setGroup_count(int group_count) {
		this.group_count = group_count;
	}

	public double getGroup_payoff() {
		return group_payoff;
	}

	public void setGroup_payoff(double group_payoff) {
		this.group_payoff = group_payoff;
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

}
