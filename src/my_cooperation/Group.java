package my_cooperation;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Group implements Steppable {
	
	public Bag curr_agents = new Bag();
	public int _group_id;
	public int x;
	public int y;
	
	public FileWriter write_to;
	//directory storing agent files
	public String write_directory = "Python/Group_data/";
	public String file_path;
	
	
	public int group_count;
	public int num_standards;
	public int num_deviants;
	
	public float group_payoff;

	public Group(int x, int y, int group_id, String prefix) {
		super();
		this.x = x;
		this.y = y;
		this._group_id = group_id;
		
		try {
			this.file_path = write_directory + prefix + "Group-" + group_id + ".txt";
			
			//check
			File file = new File(this.file_path);
			
			if (!file.exists()) { // Check if the file doesn't exist
		        this.write_to = new FileWriter(this.file_path);
		        this.write_to.write("group_count,group_id,num_standards,num_deviants,deviant_mean_tolerance,num_groups\n");
		        this.write_to.close();
		    } else {
		    	try(FileWriter fw = new FileWriter(this.file_path, true);
					    BufferedWriter bw = new BufferedWriter(fw);
					    PrintWriter out = new PrintWriter(bw))
					{
					//group_count,group_id,id,type,num_standards_in_group,num_deviants_in_group,accumulated_payoff
					    out.println("group_count,group_id,num_standards,num_deviants,deviant_mean_tolerance,num_groups");
					    out.close();
					} catch (IOException e) {
					    //exception handling left as an exercise for the reader
					}
		    }
		    } catch (IOException e) {
		    System.out.println("An error occurred.");
		    e.printStackTrace();
		    }
	}
	
	public void add_agent(Agent a) {
		this.curr_agents.add(a);
	}
	
	public void remove_agent(Agent a) {
		if (this.curr_agents.contains(a)) {
			this.curr_agents.remove(a);
		}
	}
	
	public void calc_group_payoff() {
		
		float sum = 0;
		
		if(!this.curr_agents.isEmpty()) {
			for (Object b: this.curr_agents) {
				Agent a  = (Agent) b;
				sum += a.contribute();
			}
		}
		this.group_payoff = sum;
	}
	
	public int[] group_constitution() {
		int[] t = new int[2];
		for(Object b: this.curr_agents) {
			Agent a = (Agent) b;
			if(a.type == 0){
				t[0]++;
			}
			else {
				t[1]++;
			}
		}
		return t;
	}
	
	public void writing(Environment env) {
		try(FileWriter fw = new FileWriter(this.file_path, true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw))
			{
			//group_count,group_id,id,type,num_standards_in_group,num_deviants_in_group,accumulated_payoff
			    out.println(String.format("%d,%d,%d,%d,%f,%d", 
                        this.group_count, 
                        this._group_id,
                        this.num_standards, 
                        this.num_deviants, 
                        env.deviant_mean_tolerance,
                        env.num_groups));
			    out.close();
			} catch (IOException e) {
			    //exception handling left as an exercise for the reader
			}
	}
	
	@Override
	public void step(SimState state) {
		calc_group_payoff();
		int[] t = group_constitution();
		this.num_standards = t[0];
		this.num_deviants = t[1];
		this.group_count = this.curr_agents.numObjs;
		writing((Environment) state);
		
	}

}
