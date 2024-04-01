package my_cooperation;

import java.awt.Color;

import spaces.Spaces;
import sweep.GUIStateSweep;
import sweep.SimStateSweep;

public class GUI extends GUIStateSweep {

	public GUI(SimStateSweep state, int gridWidth, int gridHeight, Color backdrop, Color agentDefaultColor,
			boolean agentPortrayal) {
		super(state, gridWidth, gridHeight, backdrop, agentDefaultColor, agentPortrayal);
		// TODO Auto-generated constructor stub
	}
	
	
	public GUI(SimStateSweep state) {
		super(state);
		// TODO Auto-generated constructor stub
	}

	
	public static void main(String[] args) {
//		String[] title2 = {"Proportion of Deviants per Group"};//A string array, where every entry is the title of a chart
//		String[] x2 = {"Group ids"};//A string array, where every entry is the x-axis title
//		String[] y2 = {"Counts"};//A string array, where every entry is the y-axis title
//		GUI.initializeArrayHistogramChart(1, title2, x2, y2, new int[10]);
//	
//		String[] title = {"Deviant average Payoffs", "Standard average Payoffs"};//A string array, where every entry is the title of a chart
//		String[] x = {"Time Steps", "Time Steps"};//A string array, where every entry is the x-axis title
//		String[] y = {"Average Accumulated Payoff", "Average Accumulated Payoff"};//A string array, where every entry is the y-axis title
//		//AgentsGUI.initializeArrayTimeSeriesChart(number of charts, chart titles, x-axis titles, y-axis titles);
//		GUI.initializeArrayTimeSeriesChart(2, title, x, y);//creates as many charts as indicated by the first number.
//		//All arrays must have the same number of elements as the number of charts
		
		GUI.initialize(Environment.class,Experimenter.class, GUI.class, 400, 400, Color.WHITE, Color.blue, true, Spaces.SPARSE);
	}
}
