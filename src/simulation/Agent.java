package simulation;
import interfaces.Environment;

import java.util.*;
import java.text.DecimalFormat;
import java.io.*;

import utils.Pair;

public class Agent {

	private final DecimalFormat df = new DecimalFormat("#.##");
	private boolean debug = false;
	private boolean debug2 = true;
	
	// These are unique to the agent
	private Pair<Integer, Integer> currentState;
	private int[] Types ={0,1};
	private String[] TypesName = {"cleaner", "viewer"};
	
	private int Type;
	private String TypeName;
	
	// These are going to be taken by the world
	// when we initialize the agent
	// state is pair <x,y>
	private World world;
	private Environment environment;
	private Pair<Integer, Integer>[] listOfStates;
	// size of X-dimension
	private int xSize;
	// size of Y-dimension
	private int ySize;
	
	// actions as integers
	// default 0:up, 1:down, 2:left, 3:right
	private int [] Actions;
	// table lookup for name of actions
	private String[] ActionsNames;
		
	// The final state 
	private Pair<Integer, Integer> goalState;
		
	// initial alpha and gamma and initial epsilon for calculating the Q-table
	private double alpha = 1;
	private double gamma = 0.9; 
	private double epsilon = 0.3;
	
	private int k = 0;
	// Q-table both global and local
	// make it private and add setter methods
	public double [][] Qtable = new double [0][0];// = new double [States.length][Actions.length];
	public double [][] QJointtable = new double [0][0]; // Joint q table
	
	// Alpha-Table: Tracks number of visits to each state
	public double [][] Visittable = new double [0][0];
	
	// CONSTRUCTORS
	// default constructor empty
	public Agent()
	{
		//this.Qtable = new double [0][0];
	}
	
	
	// constructor with normal inits
	public Agent (World world,Environment env, String type, double alpha, double gamma, Pair init_state)
	{
		this.world = world;
		this.environment = env;
		this.currentState = init_state;
		this.Type = this.stringTypeToInt(type);
		this.TypeName = type;
		this.listOfStates = world.getListOfStates();
		this.Actions = env.getActions();
		this.ActionsNames = env.getActionNames();
		this.goalState = env.getGoalState();
		this.alpha = alpha;
		this.gamma = gamma;
		//init Q-table
		this.Qtable = new double[this.listOfStates.length][this.Actions.length];
				
		for (int i=0; i< this.Qtable.length; i++)
			for (int j=0; j<this.Qtable[i].length; j++)
				this.Qtable[i][j] = 0.0;
		
		//init visits-Table
		this.Visittable = new double[this.listOfStates.length][this.Actions.length];
		for (int i=0; i< this.Visittable.length; i++)
			for (int j=0; j<this.Visittable[i].length; j++)
				this.Visittable[i][j] = 0.0;
		
		if (debug)
			System.out.println("Q-table initialized with size "+this.Qtable.length);
		    System.out.println("Visited-table initialized with size "+this.Visittable.length);
	}

	
	public Pair<Integer, Integer> getGoalState ()
	{
		return this.goalState;
	}
	
	//Pick action based on e-greedy policy: choose action based on max q-value with probability 1-e and choose a (uniform) random action with probability e
	public int pick_next_action (Pair<Integer, Integer> state, int maxruns, int currentrun)
	{
		int result = -1;
		
		// find the max value of actions
		
		// first sort the array
		// max value will be last
		double [] temp = this.Qtable[this.world.stateToIndex(state)].clone();
		Arrays.sort(temp);
		
		//Generate a random number double between 0 and 1
		
		double rand = Math.random();
		
		//do the count method instead of rand
		
		//double maxtrials = epsilon*maxruns;
		//System.out.println("maxrun: "+maxtrials);
		//System.out.println("currentrun: "+currentrun);
		//Choose action randomly with e probability
		if(rand<epsilon)
		{
			Random randActionIndex = new Random();
			result = randActionIndex.nextInt(temp.length-1);
			//System.out.println("QAction:"+result);
			
		}
		
		//Choose action based on max Q value with (1-e) prob
		else{
		// count the number of max values 
		// and choose randomly if there are multiple actions
		int counter = 0;
		int index = (temp.length)-1;
		while( (index-1 >= 0) && (temp[index-1] == temp[index] ))
		{
			counter++;
			index--;
		}
		int random_index = (int)(Math.random() * (counter + 1));
		
		// find the index of the (random) max action and return it
		for (int i=0;i<this.Qtable[this.world.stateToIndex(state)].length; i++)
		{
			if (this.Qtable[this.world.stateToIndex(state)][i] == temp[temp.length-1])
			{
				if (random_index == 0)
				{
					result = i;
					break;
				}
				else
				{
					random_index--;
				}
				
			}
		}
		
		}
		return result;
	}
	
	public void single_step (Pair<Integer, Integer> state, int action) 
	{
		Pair<Integer, Integer> current_state = state;
		int next_action;
		
			//next_action = pick_next_action(current_state);
			//next_action = action;
			
			// should we do the current_state update here, and 
			// remove it from the Q_update, i.e., call Q_update
			// with state,action,state' ? What if the world.transitionTo
			// returns two different results on the two calls? due to 
			// stochastic transitions?
			//Q_update (current_state,next_action);
			//current_state = this.world.transitionTo(current_state, next_action);
		
	}
	
	public void single_step (Pair<Integer, Integer> state) 
	{
		Pair<Integer, Integer> current_state = state;
		int next_action;
		
			//next_action = pick_next_action(current_state);
			// should we do the current_state update here, and 
			// remove it from the Q_update, i.e., call Q_update
			// with state,action,state' ? What if the world.transitionTo
			// returns two different results on the two calls? due to 
			// stochastic transitions?
			//Q_update (current_state,next_action);
			//current_state = this.world.transitionTo(current_state, next_action);
		
	}
	
	public void single_run (Pair<Integer, Integer> state, int maxruns, int currentrun) 
	{
		Pair<Integer, Integer> current_state = state.copy();
		Pair<Integer, Integer> end_state;
		
		//System.out.println("currentrun: "+currentrun);
		//System.out.println("maxrun: "+maxruns);
		if (debug)
			System.out.print("current state = "+current_state.print());
		int next_action;
		
		//Pair goal = new Pair(2,2);
		if(debug2)
			System.out.println("Scenario starts =========== ");
		
		while (!current_state.equals(this.getGoalState())) //this.goalState)
		{
			int currentrun_i = currentrun;
			int maxruns_i = maxruns;
			
			if (debug)
			{
				System.out.print("current state = "+current_state.print());
				//System.out.print(" goal state = "+this.goalState.print());	
			}
			//System.out.println("currentrun: "+currentrun_i);
			next_action = pick_next_action(current_state,maxruns_i,currentrun_i);
			//epsilon = epsilon/(double)k;
			
			if (debug)
				System.out.println(" next action is "+next_action+" "+this.world.getActionName(next_action));
			// should we do the current_state update here, and 
			// remove it from the Q_update, i.e., call Q_update
			// with state,action,state' ? What if the world.transitionTo
			// returns two different results on the two calls? due to 
			// stochastic transitions?
			/*if (next_action == 3) 
			{	
				Pair temp = new Pair(1,0);
				current_state.print();
				if (current_state.equals(temp))
					end_state = this.world.transitionTo(current_state, next_action);
				else 
					end_state = this.world.transitionTo(new Pair(current_state.getX(), current_state.getY()), next_action);
			}			
			else*/			
			end_state = this.world.transitionTo(current_state, next_action);
			if (debug)	
				System.out.println("end state is "+end_state.print());
			
			//First update Q-value then visits to calculate alpha based on previous visits
			
			Q_update (current_state,next_action,end_state);
			A_update (current_state,next_action);
			
			if(debug2)
			System.out.println(current_state.print() +"-> "+this.world.getActionName(next_action)+"->"+end_state.print());
			current_state = end_state;
			//current_state = this.world.transitionTo(currentState, next_action);
			k= k+1;
			//
			//epsilon = epsilon/(double)k;
			epsilon = epsilon*0.999;
			
		}
		
		if(debug2)
			System.out.println("Scenario ends =========== ");
		//System.out.println("Total number visits:" + k);
		//System.out.println("epsilon:" + epsilon);
			
	}
	
	
	
	public void multiple_runs (int count, Pair<Integer, Integer> state)
	{
		for (int i=0; i<count; i++)
		{
			
			single_run(state,count,i);
			//System.out.println("countrun: "+count);
		}
		
	}
	
	
	public void Q_update (Pair<Integer, Integer> currentState, int action)
	{
		if (debug)
			System.out.println("Updating");
		
		double currentQ = this.Qtable[this.world.stateToIndex(currentState)][action];
		double currentAlpha = 1/(this.Visittable[this.world.stateToIndex(currentState)][action]+1.0);
		//currentQ = (1-this.alpha)*currentQ;
		// get the target state s'
		Pair<Integer, Integer> endState = this.world.transitionTo(currentState, action);
		// get the reward of the end state s'
		double reward = this.world.getReward(endState);
		
		// find the max value from the end state s'
		//List temp = Arrays.asList(this.Qtable[this.world.stateToIndex(endState)]);
		//double max_step = (double) Collections.max(temp);
		double [] temp = this.Qtable[this.world.stateToIndex(endState)].clone();
		Arrays.sort(temp);
		double max_step = temp[temp.length-1];
		
		// calculate new q-value
		this.Qtable[this.world.stateToIndex(currentState)][action] =  
				currentQ + (currentAlpha*(reward + (this.gamma*max_step - currentQ) ));
		//this.Qtable[0][0] = 1000.0;
	}
	
	public void Q_update (Pair<Integer, Integer> currentState, int action, Pair<Integer, Integer> endState)
	{
		if (debug)
			System.out.println("Updating");
		
		double currentQ = this.Qtable[this.world.stateToIndex(currentState)][action];
		//Calculate alpha
		double currentAlpha = 1/(this.Visittable[this.world.stateToIndex(currentState)][action]+1.0);
		//currentQ = (1-this.alpha)*currentQ;
		// get the target state s'
		//State endState = this.world.transitionTo(currentState, action);
		// get the reward of the end state s'
		double reward = this.world.getReward(endState);
		
		// find the max value from the end state s'
		//List temp = Arrays.asList(this.Qtable[this.world.stateToIndex(endState)]);
		//double max_step = (double) Collections.max(temp);
		double [] temp = this.Qtable[this.world.stateToIndex(endState)].clone();
		Arrays.sort(temp);
		double max_step = temp[temp.length-1];
		
		//Calculate alpha
		//alpha = this.Visittable[this.world.stateToIndex(endState)][action];
		
		// calculate new q-value
		this.Qtable[this.world.stateToIndex(currentState)][action] =  
				currentQ + (currentAlpha*(reward + (this.gamma*max_step - currentQ) ));
		//this.Qtable[0][0] = 1000.0;
	}
	
	public void A_update (Pair<Integer, Integer> currentState, int action)
	{
		double currentVisits = this.Visittable[this.world.stateToIndex(currentState)][action];
		currentVisits = currentVisits+1;
		this.Visittable[this.world.stateToIndex(currentState)][action] = currentVisits;
	}
	// conversions for types, from ints to strings and reverse
	public int stringTypeToInt (String type)
	{
		if (type.equals("Cleaner")) return 0;
		else return 1;
	}
	public String intTypeToString (int type)
	{
		if (type == 0) return "Cleaner";
		else return "Viewer";
	}
	
	public String printQtable ()
	{
		String qtable = "";
		for (int i=0; i<this.Qtable.length; i++)
		{
			if (i==0)
			{
				qtable+="   ";
				for (int j=0; j< this.Qtable[i].length; j++)
				{
					qtable+=this.world.getActionName(j)+" ";
				}
				qtable+="\r\n";
			}
			qtable += this.world.indexToState(i).print()+" ";
			for (int j=0; j< this.Qtable[i].length; j++)
			{
				qtable += df.format(this.Qtable[i][j])+" ";
			}
			qtable += "\r\n";
		}
		return qtable;
	}
	
	public String printVisittable ()
	{
		String Visittable = "";
		for (int i=0; i<this.Visittable.length; i++)
		{
			if (i==0)
			{
				Visittable+="   ";
				for (int j=0; j< this.Visittable[i].length; j++)
				{
					Visittable+=this.world.getActionName(j)+" ";
				}
				Visittable+="\r\n";
			}
			Visittable += this.world.indexToState(i).print()+" ";
			for (int j=0; j< this.Visittable[i].length; j++)
			{
				Visittable += df.format(this.Visittable[i][j])+" ";
			}
			Visittable += "\r\n";
		}
		return Visittable;
	}
	
	
	
	public void printTransitionTableWorld()
	{
		this.world.printTransitionTable();
	}
	
	public Pair<Integer, Integer> getCurrentState()
	{
		return this.currentState;
	}
	// setter for debugging
	public void setCurrentState(Pair<Integer, Integer> q)
	{
		this.currentState = q;
	}
	
	public void resetQtable()
	{
		for (int i=0; i< this.Qtable.length; i++)
			for (int j=0; j<this.Qtable[i].length; j++)
				this.Qtable[i][j] = 0.0;
	}
	
	public void resetVisittable()
	{
		for (int i=0; i< this.Visittable.length; i++)
			for (int j=0; j<this.Visittable[i].length; j++)
				this.Visittable[i][j] = 0.0;
	}
	
	public void SaveToFile()
	{
		PrintWriter out = null;
		try {
		    out = new PrintWriter(new BufferedWriter(new FileWriter("Qtables", true)));
		    out.println(this.printQtable());
		}catch (IOException e) {
		    System.err.println(e);
		}finally{
		    if(out != null){
		        out.close();
		    }
		} 
	}
	
	public void SaveToFileQ(String s)
	{
		PrintWriter out = null;
		try {
		    out = new PrintWriter(new BufferedWriter(new FileWriter("Qtables.txt", true)));
		    out.println(s+"\r\n"+this.printQtable());
		}catch (IOException e) {
		    System.err.println(e);
		}finally{
		    if(out != null){
		        out.close();
		    }
		} 
	}
	
	public void SaveToFileVisits(String s)
	{
		PrintWriter out = null;
		try {
		    out = new PrintWriter(new BufferedWriter(new FileWriter("Visitstables.txt", true)));
		    out.println(s+"\r\n"+this.printVisittable());
		}catch (IOException e) {
		    System.err.println(e);
		}finally{
		    if(out != null){
		        out.close();
		    }
		} 
	}
	// conversions for action names, use the ones of the world
	
	// unit testing 
	public static void main (String [] args)
	{
		World world = new World(new Pair<Integer, Integer> (2,2));
		Environment env = new Floor(System.getProperty("user.dir") + "/src/" + args[0]);
		
		//world.printTransitionTable();
		//world.printRewards();
		Pair<Integer, Integer> state = 
				new Pair<Integer, Integer>(env.getDimensions().getFirst()-1,env.getDimensions().getSecond()-1);
		Agent agent = new Agent(world, env, "cleaner", 0.1, 0.9, state);
		//agent.printTransitionTableWorld();
		agent.printQtable();
		agent.printVisittable();
		System.out.println("=================");
		//agent.single_step(agent.getCurrentState(),agent.world.decodeAction("right"));
		//agent.single_step(agent.getCurrentState());
		//agent.single_run(agent.getCurrentState());
		//System.out.println(agent.printQtable());
		String s = " ============== ";
		int runs = 100;
		for (int i =0; i< 1; i++)
		{
			agent.resetQtable();
			agent.resetVisittable();
			agent.multiple_runs(runs, agent.getCurrentState());	
			agent.SaveToFileQ(s+"scenario "+i+" for "+runs+" runs"+s);
			agent.SaveToFileVisits(s+"scenario "+i+" for "+runs+" runs"+s);
		}
		
	}
	
}
