package my_cooperation;

import sim.util.Bag;
import sim.util.distribution.Normal;
import sim.util.distribution.Beta;
import model.SimDataCollection;

public class Environment extends SimDataCollection {

	//experimental parameters pre 7/11/24
//	public int num_groups = 13; //number of groups that exist
//	public int num_agents_per_group = 5; // number of agents per group
//	public int max_strikes = 3; //num strikes before leaving
//	public double mean_tolerance = 1.0f;
//	public double std_tolerance = 0.2f;
//	public double mean_value = 1.0f;
//	public double std_value = 0.2f;
//	public double agent_std_value = 0.1f;
//	public double prop_deviant = 0.2f;
//	public double deviant_mean_tolerance = 2.0f;
	
	//experimental parameters post 7/11/24
	// experimental condition related parameters
	public String prefix; // Where to save all these data to
	public boolean letter_grades = false; // use buckets?
	
	public double grading_error_alpha;
	public double grading_error_beta;
	
	public double divorce_constant; // constant modifier to the cost of divorce
	public int max_strikes; // number of strikes before divorce is considered
	
	// Agent related parameters
	public int num_agents;
	public double agent_tolerance_alpha;
	public double agent_tolerance_beta;
	public double agent_effort_alpha;
	public double agent_effort_beta;
	public double agent_std_effort; // one letter grade std? this is within agent std since always drawn from normal for agent
	
	
	// Group related parameters
	public int min_agents_per_group;
	public int max_agents_per_group;
	

	//GUI related parameters
	public int gridHeight = 100;
	public int gridWidth = 100;
	public int min_distance_groups = 5;

	//not tracked or input
	Bag groups = new Bag();
	Bag agents = new Bag();
	
	public boolean charts = false;
	

	//data to collect
	public int num_groups;
	public int num_deviants = 0;
	public int num_standards = 0;
	public double avg_deviant_payoff; //average payoff across all deviants
	public double  avg_standard_payoff; //average payoff across all standard agents

	public static void main(String[] args) {
		Environment environment = new Environment("Python/RunFiles/adjusted_sweeps.txt");
	}
	
	//constructor 
	public Environment(String filename) {
		super(filename);
	}

	public Environment() {
		super();
	}

	@Override
	public void setClasses() {
		this.subclass = Environment.class;
		this.agentclass = Agent.class;
	}

	public void make_groups() {
		// make the groups first
		this.groups = new Bag();
		num_groups = num_agents / min_agents_per_group;
		
		for(int i = 1; i <= num_groups; i++) {
			int random_x = random.nextInt(gridWidth);
			int random_y = random.nextInt(gridHeight);

			//			Bag b = sparseSpace.getMooreNeighbors(random_x, random_y, min_distance_groups, sparseSpace.BOUNDED, false);
			//			
			//			while(!b.isEmpty()) {
			//				random_x = random.nextInt(gridWidth);
			//				random_y = random.nextInt(gridHeight);
			//				b = sparseSpace.getMooreNeighbors(random_x, random_y, min_distance_groups, sparseSpace.BOUNDED, false);
			//			}
			
			String[] group_tracked = {"group_count", "group_payoff", "step_grade", "loafing_detected"};
			String[] group_header = {"num_agents", "min_agents_per_group", "max_agents_per_group", "letter_grades" , "grading_error_alpha", "grading_error_beta", "agent_effort_alpha", "agent_effort_beta", "agent_std_effort", "divorce_constant", "agent_tolerance_alpha", "agent_tolerance_beta", "currseed"};
			String group_write_directory = "Python/Group_data/adjusted_sweeps_Group-";
			
			Group g = new Group(this, random_x, random_y, i);
			groups.add(g);
			schedule.scheduleRepeating(g);
			Reporter r = new Reporter(this, g, Group.class, Integer.toString(i), group_tracked, group_header, group_write_directory);
			schedule.scheduleRepeating(r, 2, 1.0);
			//			sparseSpace.setObjectLocation(g, random_x, random_y);
		}
	}

	public void make_agents() {
		this.agents = new Bag();
		Beta tolerance_beta = new Beta(agent_tolerance_alpha, agent_tolerance_beta, random);
		
		Beta effort_beta = new Beta(agent_effort_alpha, agent_effort_beta, random);

		
		int i = 1;
		for(Object a:this.groups) {
			Group g = (Group) a;
			for(int j = 0; j < min_agents_per_group; j++) {
				int random_x = random.nextInt(5) + g.x;
				int random_y = random.nextInt(5) + g.y;

				//				Bag b = sparseSpace.getObjectsAtLocation(random_x, random_y);
				//				
				//				while(!(b == null)) {
				//					random_x = random.nextInt(gridWidth);
				//					random_y = random.nextInt(gridHeight);
				//					b = sparseSpace.getObjectsAtLocation(random_x, random_y);
				//				}

				//Agent(Environment state, float tolerance, float mean_value, float std_value, int x, int y, int id, int group_id) {
				double tolerance = tolerance_beta.nextDouble();
				double mean_value = effort_beta.nextDouble();
				Agent agent = new Agent(this, tolerance, mean_value, agent_std_effort, random_x, random_y, i, g.group_id, this.prefix);
				agents.add(agent);
				g.add_agent(agent);
				
				String prefix = "adjusted_sweeps_Agent-";
				String write_directory = "Python/Agent_data/" + prefix;
				String[] tracked = {"group_id","id","accumulated_payoff","mean_value","tolerance", "strikes", "step_grade", "loafing_detected", "grade_mean_index", "grade_tolerance"};
				String[] header = {"num_agents", "min_agents_per_group", "max_agents_per_group", "letter_grades" , "grading_error_alpha", "grading_error_beta", "agent_effort_alpha", "agent_effort_beta", "agent_std_effort", "divorce_constant", "agent_tolerance_alpha", "agent_tolerance_beta", "currseed"};
				Reporter r = new Reporter(this, agent, Agent.class, Integer.toString(i), tracked, header, write_directory);
				i++;
				
				schedule.scheduleRepeating(agent, 1, 1.0);
				schedule.scheduleRepeating(r, 2, 1.0);
				//				sparseSpace.setObjectLocation(agent, random_x, random_y);
			}
		}
	}
	
	public void make_new_group(Agent a) {
		//Make a new group, and add the agent to it
		int random_x = random.nextInt(gridWidth);
		int random_y = random.nextInt(gridHeight);
		
		String[] group_tracked = {"group_count", "group_payoff", "step_grade", "loafing_detected"};
		String[] group_header = {"num_agents", "min_agents_per_group", "max_agents_per_group", "letter_grades" , "grading_error_alpha", "grading_error_beta", "agent_effort_alpha", "agent_effort_beta", "agent_std_effort", "divorce_constant", "agent_tolerance_alpha", "agent_tolerance_beta", "currseed"};
		String group_write_directory = "Python/Group_data/adjusted_sweeps_Group-";
		
		int i = this.groups.numObjs + 1;
		Group g = new Group(this, random_x, random_y, i);
		groups.add(g);
		schedule.scheduleRepeating(g);
		Reporter r = new Reporter(this, g, Group.class, Integer.toString(i), group_tracked, group_header, group_write_directory);
		schedule.scheduleRepeating(r, 2, 1.0);
		g.add_agent(a);
		a.group_id = i;
		this.num_groups ++;
		System.out.println("Made group");
	}

	public boolean viability_check() {
		//agent tolerance checks
		if((this.agent_tolerance_alpha == 1. || this.agent_tolerance_beta == 1.) && this.agent_tolerance_alpha != this.agent_tolerance_beta) {
			System.out.println("uniformitivity check where alpha || beta == 1 failed");
			return false;
		}
		if(this.agent_tolerance_alpha >= this.agent_tolerance_beta) {
			System.out.println("Tolerance alpha >= tolerance beta");
			return false;
		}
		// effort checks
		if((this.agent_effort_beta >= this.agent_effort_alpha)) {
			System.out.println("Effort beta >= effort alpha");
			return false;
		}
		else {
			return true;
		}
	}
	
	public void start() {
		if(! viability_check()) {
			System.out.println("Weakling eliminated");
		}
		else {
			super.start();
//			this.makeSpace(gridWidth, gridHeight);
			//System.out.println("Made space");
			make_groups();
			make_agents();
			// initialize the experimenter by calling initialize in the parent class
			Experimenter e = new Experimenter();
			e.event = schedule.scheduleRepeating(e, 3, 1.0);
		}
	}

}
