package my_cooperation;

import sim.util.Bag;
import sim.util.distribution.Normal;
import model.SimDataCollection;

public class Environment extends SimDataCollection {

	//experimental parameters
	public int num_groups = 13; //number of groups that exist
	public int num_agents_per_group = 5; // number of agents per group
	public int max_strikes = 3; //num strikes before leaving
	public double mean_tolerance = 1.0f;
	public double std_tolerance = 0.2f;
	public double mean_value = 1.0f;
	public double std_value = 0.2f;
	public double agent_std_value = 0.1f;
	public double prop_deviant = 0.2f;
	public double deviant_mean_tolerance = 2.0f;

	//GUI related parameters
	public int gridHeight = 100;
	public int gridWidth = 100;
	public int min_distance_groups = 5;

	//not tracked or input
	Bag groups = new Bag();
	Bag agents = new Bag();
	
	public boolean charts = false;
	
	public String prefix;

	//data to collect
	public int num_deviants = 0;
	public int num_standards = 0;
	public double avg_deviant_payoff; //average payoff across all deviants
	public double  avg_standard_payoff; //average payoff across all standard agents

	public static void main(String[] args) {
		Environment environment = new Environment("Python/RunFiles/adjusted_means.txt");
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
		for(int i = 0; i < num_groups; i++) {
			int random_x = random.nextInt(gridWidth);
			int random_y = random.nextInt(gridHeight);

			//			Bag b = sparseSpace.getMooreNeighbors(random_x, random_y, min_distance_groups, sparseSpace.BOUNDED, false);
			//			
			//			while(!b.isEmpty()) {
			//				random_x = random.nextInt(gridWidth);
			//				random_y = random.nextInt(gridHeight);
			//				b = sparseSpace.getMooreNeighbors(random_x, random_y, min_distance_groups, sparseSpace.BOUNDED, false);
			//			}
			
			Group g = new Group(random_x, random_y, i, this.prefix);
			groups.add(g);
			schedule.scheduleRepeating(g);
			//			sparseSpace.setObjectLocation(g, random_x, random_y);
		}
	}

	public void make_agents() {
		
		this.agents = new Bag();
		Normal deviant_tolerance_norm = new Normal(deviant_mean_tolerance, std_tolerance, random);
		Normal tolerance_norm = new Normal(mean_tolerance, std_tolerance, random);
		Normal value_norm = new Normal(mean_tolerance, std_tolerance, random);

		int i = 0;
		for(Object a:this.groups) {
			Group g = (Group) a;
			for(int j = 0; j < num_agents_per_group; j++) {
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

				double tolerance = tolerance_norm.nextDouble();
				double mean_value = value_norm.nextDouble();

				int type = random.nextBoolean(prop_deviant) ? 1:0;

				if (type == 1) {
					tolerance = deviant_tolerance_norm.nextDouble();
					num_deviants++;
				}
				else {
					num_standards++;
				}
				Agent agent = new Agent(this, tolerance, mean_value, agent_std_value, random_x, random_y, i, g._group_id, type, this.prefix);
				agents.add(agent);
				g.add_agent(agent);
				
				String prefix = "adjusted_means_Agent-";
				String write_directory = "Python/Agent_data/" + prefix;
				String[] tracked = {"group_count","group_id","id","type","num_standards_in_group","num_deviants_in_group","accumulated_payoff","mean_value","tolerance","deviant_mean_tolerance","num_groups"};
				Reporter r = new Reporter(agent, Agent.class, Integer.toString(i), tracked, write_directory);
				i++;
				
				schedule.scheduleRepeating(agent, 1, 1.0);
				schedule.scheduleRepeating(r, 2, 1.0);
				//				sparseSpace.setObjectLocation(agent, random_x, random_y);
			}
		}
	}


	public void start() {
		super.start();
		//		this.makeSpace(gridWidth, gridHeight);
		//System.out.println("Made space");
		make_groups();
		make_agents();
		// initialize the experimenter by calling initialize in the parent class
		Experimenter e = new Experimenter();
		e.event = schedule.scheduleRepeating(e, 3, 1.0);
	}

	public int getNum_groups() {
		return num_groups;
	}

	public void setNum_groups(int num_groups) {
		this.num_groups = num_groups;
	}

	public int getNum_agents_per_group() {
		return num_agents_per_group;
	}

	public void setNum_agents_per_group(int num_agents_per_group) {
		this.num_agents_per_group = num_agents_per_group;
	}

	public int getMax_strikes() {
		return max_strikes;
	}

	public void setMax_strikes(int max_strikes) {
		this.max_strikes = max_strikes;
	}

	public double getMean_tolerance() {
		return mean_tolerance;
	}

	public void setMean_tolerance(float mean_tolerance) {
		this.mean_tolerance = mean_tolerance;
	}

	public double getStd_tolerance() {
		return std_tolerance;
	}

	public void setStd_tolerance(float std_tolerance) {
		this.std_tolerance = std_tolerance;
	}

	public double getMean_value() {
		return mean_value;
	}

	public void setMean_value(float mean_value) {
		this.mean_value = mean_value;
	}

	public double getStd_value() {
		return std_value;
	}

	public void setStd_value(float std_value) {
		this.std_value = std_value;
	}

	public double getAgent_std_value() {
		return agent_std_value;
	}

	public void setAgent_std_value(float agent_std_value) {
		this.agent_std_value = agent_std_value;
	}

	public double getProp_deviant() {
		return prop_deviant;
	}

	public void setProp_deviant(float prop_deviant) {
		this.prop_deviant = prop_deviant;
	}

	public double getDeviant_mean_tolerance() {
		return deviant_mean_tolerance;
	}

	public void setDeviant_mean_tolerance(float deviant_mean_tolerance) {
		this.deviant_mean_tolerance = deviant_mean_tolerance;
	}

	public int getGridHeight() {
		return gridHeight;
	}

	public void setGridHeight(int gridHeight) {
		this.gridHeight = gridHeight;
	}

	public int getGridWidth() {
		return gridWidth;
	}

	public void setGridWidth(int gridWidth) {
		this.gridWidth = gridWidth;
	}

	public int getMin_distance_groups() {
		return min_distance_groups;
	}

	public void setMin_distance_groups(int min_distance_groups) {
		this.min_distance_groups = min_distance_groups;
	}

	public Bag getGroups() {
		return groups;
	}

	public void setGroups(Bag groups) {
		this.groups = groups;
	}

	public Bag getAgents() {
		return agents;
	}

	public void setAgents(Bag agents) {
		this.agents = agents;
	}

	public boolean isCharts() {
		return charts;
	}

	public void setCharts(boolean charts) {
		this.charts = charts;
	}

	public double getAvg_deviant_payoff() {
		return avg_deviant_payoff;
	}

	public void setAvg_deviant_payoff(float avg_deviant_payoff) {
		this.avg_deviant_payoff = avg_deviant_payoff;
	}

	public double getAvg_standard_payoff() {
		return avg_standard_payoff;
	}

	public void setAvg_standard_payoff(float avg_standard_payoff) {
		this.avg_standard_payoff = avg_standard_payoff;
	}

	public int getNum_deviants() {
		return num_deviants;
	}

	public void setNum_deviants(int num_deviants) {
		this.num_deviants = num_deviants;
	}

	public int getNum_standards() {
		return num_standards;
	}

	public void setNum_standards(int num_standards) {
		this.num_standards = num_standards;
	}

}
