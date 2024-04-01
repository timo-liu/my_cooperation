package my_cooperation;

import model.SimDataCollection;

public class eye_of_sauron extends SimDataCollection {

	public eye_of_sauron() {
		// TODO Auto-generated constructor stub
	}

	public eye_of_sauron(String fname) {
		super(fname);
		// TODO Auto-generated constructor stub
	}

	public eye_of_sauron(String[] args) {
		super(args);
		// TODO Auto-generated constructor stub
	}

	public eye_of_sauron(String fname, String[] splitparams, String[] splitkeys) {
		super(fname, splitparams, splitkeys);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setClasses() {
		// TODO Auto-generated method stub
		this.subclass = eye_of_sauron.class;
		this.agentclass =Agent.class;
	}

}
