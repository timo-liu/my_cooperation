package my_cooperation;

import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.BufferedWriter;
import java.io.File;
// Just Manually Writing stuff
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;

public class Reporter implements Steppable {
	
	Environment state;
	Object reported;
	Class reported_class;
	String unique_id;
	String[] tracked;
	String[] header;	
	// All the file stuff
	String file_prefix;
	String file_path;
	FileWriter write_to;
	File file;
	int track_rate;
	
	// Object reported - The reported object, typically an Agent, passed as "this"
	// Class reported_class - Class of the reported object, e.g. Agent
	// String unique_id - Some unique id the user can assign to the reporter 
	// String[] tracked - A string array of which variables you want to track, should have public get functions for all
	// String file_prefix - Where you want the file saved to relative to the project, as file_prefix + unique_id.txt
	
	public Reporter(Environment state, Object reported, Class reported_class, String unique_id, String[] tracked, String[] header, String file_prefix, int track_rate) {
		this.state = state;
		this.track_rate = track_rate;
		this.reported  = reported;
		this.reported_class = reported_class;
		this.unique_id = unique_id;
		this.tracked = tracked;
		this.header = header;
		this.file_prefix = file_prefix;
		create_file();
	}
	
	public void create_file() {
		try {
			this.file_path = this.file_prefix + this.unique_id +  ".txt";
			
			//check
			this.file = new File(this.file_path);
			
			if (!file.exists()) { // Check if the file doesn't exist
		        this.write_to = new FileWriter(this.file_path);
		        for(int i = 0; i<this.header.length; i++) {
					this.write_to.write("# " + this.header[i] + ": " + get_result(this.header[i], true) + '\n');
				}
		        this.write_to.write(String.join(",", this.tracked) + ",steps" + '\n');
		        this.write_to.close();
		    } else {
		    	write_header();
		    	try(FileWriter fw = new FileWriter(this.file_path, true);
					    BufferedWriter bw = new BufferedWriter(fw);
					    PrintWriter out = new PrintWriter(bw))
					{
					//group_count,group_id,id,type,num_standards_in_group,num_deviants_in_group,accumulated_payoff
					    out.println(String.join(",", this.tracked) + ",steps");
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
	
	// pulling from syntax from Aviva's code but simpler just to get things to work - Timo
	public String get_result(String res, boolean from_state) {
		String p = "";
		if(!from_state){
			if(this.reported != null) {
				try {
					Field f = this.reported_class.getField(res);
					// things that don't handle this well will just have to be dealt with elsewhere
					p = String.valueOf(f.get(this.reported)); //Real simple like, simply don't pass your data as arrays - Timo
				} catch(NoSuchFieldException e) {
					// that's okay, this will just have to be dealt with in the subclass
				} catch(IllegalAccessException e) {
					// this is also hypothetically okay, but should tell the user
					System.out.println("Unable to access " + res + ".");
				}
			}
			return(p);
		}
		else {
			if(this.state != null) {
				try {
					Field f = Environment.class.getField(res);
					// things that don't handle this well will just have to be dealt with elsewhere
					p = String.valueOf(f.get(this.state)); //Real simple like, simply don't pass your data as arrays - Timo
				} catch(NoSuchFieldException e) {
					// that's okay, this will just have to be dealt with in the subclass
				} catch(IllegalAccessException e) {
					// this is also hypothetically okay, but should tell the user
					System.out.println("Unable to access " + res + ".");
				}
			}
		}
		return(p);
	}
	
	public void write_results() {
		String results = "";
		for(int i = 0; i<this.tracked.length; i++) {
			if(i==0) {
				results += get_result(this.tracked[i], false);
			}
			else {
				results += "," + get_result(this.tracked[i], false);
			}
		}
		
		//Adding step info
		results += Integer.toString((int) this.state.schedule.getSteps());
		
		
		try(FileWriter fw = new FileWriter(this.file_path, true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw))
			{
			//group_count,group_id,id,type,num_standards_in_group,num_deviants_in_group,accumulated_payoff
			    out.println(results);
			    out.close();
			} catch (IOException e) {
			    //exception handling left as an exercise for the reader
			}
	}
	
	public void write_header() {
		try(FileWriter fw = new FileWriter(this.file_path, true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw))
			{
				for(int i = 0; i<this.header.length; i++) {
					out.println("# " + this.header[i] + ": " + get_result(this.header[i], true));
				}
			    out.close();
			} catch (IOException e) {
			    //exception handling left as an exercise for the reader
			}
	}
	
	@Override
	// step, should be called last, one level before the Experimenter, I guess :shrug: - Timo
	public void step(SimState arg0) {
		if ((int) this.state.schedule.getSteps() % this.track_rate == 0) {
			write_results();
		}
	}

}
