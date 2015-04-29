package simulation;
import interfaces.AgentInterface;
import interfaces.Environment;

import java.util.*;
import java.text.DecimalFormat;
import java.io.*;

import utils.InvalidActionException;
import utils.NoFreeSpaceException;
import utils.OccupiedCellException;
import utils.Pair;

public class IndependentLearners implements AgentInterface{
	
	
	private static PrintWriter out;
	// id of the agent
	private int agentId;
	// set to true so that agent ignores updating
	// Q-table. This way, Q-table is always 0, epsilon 
	// is also 0, so we are constantly choosing actions 
	// randomly
	private boolean actAsRandomAgent = false;

	private final DecimalFormat df = new DecimalFormat("#.##");
	private boolean debug = false;
	private boolean debug2 = false;
	private boolean debug3 = true;
	private boolean debugConvergence = true;
	private boolean saveStatistics = false;
	
	// These are unique to the agent
	private Pair<Integer, Integer> currentState;
	private int[] Types ={0,1};
	private String[] TypesName = {"cleaner", "viewer"};
	
	private int Type;
	private String TypeName;
	
	// These are going to be taken by the environment
	// when we initialize the agent
	// state is pair <x,y>
	
	// MAKE THE ENVIRONMENT STATIC TO BE SHARED BY 
	// THE TWO AGENTS
	private static Environment environment;
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
	private double epsilon = 0.0;
	
	private double threshPercent = 5.0;
	private double threshAbs = 1.0;
	private int countStableQ = 0;
	
	private int k = 0;
	// Q-table both global and local
	// make it private and add setter methods
	public double [][] Qtable = new double [0][0];// = new double [States.length][Actions.length];
	public double [][] QtablePrev = new double [0][0];
	public double [][] PrevDiffTable = new double [0][0];
	
	public double [][] QJointtable = new double [0][0]; // Joint q table
	
	// Alpha-Table: Tracks number of visits to each state
	public double [][] Visittable = new double [0][0];
	
	// CONSTRUCTORS
	// default constructor empty
	public IndependentLearners()
	{
		//this.Qtable = new double [0][0];
	}
	
	
	// constructor with normal inits
	public IndependentLearners (Environment env, String type, double alpha, 
			double gamma, Pair init_state, int agentId)
	{
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter("dump", false)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.agentId = agentId;
		this.environment = env;
		this.currentState = init_state;
		this.Type = this.stringTypeToInt(type);
		this.TypeName = type;
		this.listOfStates = env.getListOfStates();
		this.Actions = this.environment.getAvailableActionIndices(this.TypeName);
		this.ActionsNames = this.environment.getAvailableActions(this.TypeName);
		this.goalState = env.getGoalStateSingleAgent();
		this.alpha = alpha;
		this.gamma = gamma;
		//init Q-table
		this.Qtable = new double[this.listOfStates.length][this.Actions.length];
				
		for (int i=0; i< this.Qtable.length; i++)
			for (int j=0; j<this.Qtable[i].length; j++)
				this.Qtable[i][j] = 0.0;
		
		//Previous Q table init
		
				this.QtablePrev = new double[this.listOfStates.length][this.Actions.length];
						
				for (int i=0; i< this.QtablePrev.length; i++)
					for (int j=0; j<this.QtablePrev[i].length; j++)
						 this.QtablePrev[i][j] = 0.0;
						
						//Previous Difference Table
				this.PrevDiffTable = new double[this.listOfStates.length][this.Actions.length];
						
				for (int i=0; i< this.PrevDiffTable.length; i++)
					for (int j=0; j<this.PrevDiffTable[i].length; j++)
						this.PrevDiffTable[i][j] = 0.0;

		
		//init visits-Table
		this.Visittable = new double[this.listOfStates.length][this.Actions.length];
		for (int i=0; i< this.Visittable.length; i++)
			for (int j=0; j<this.Visittable[i].length; j++)
				this.Visittable[i][j] = 0.0;
		
		if (debug){
			out.println("Q-table initialized with size "+this.Qtable.length);
		    out.println("Visited-table initialized with size "+this.Visittable.length);
		}
	}

	@Override
	public Pair<Integer, Integer> getGoalState ()
	{
		return this.goalState;
	}
	
	@Override
	//Pick action based on e-greedy policy: choose action based on max q-value with probability 1-e and choose a (uniform) random action with probability e
	public int pick_next_action (Pair<Integer, Integer> state, int maxruns, int currentrun)
	{
		int result = -1;
		
		// find the max value of actions
		
		// first sort the array
		// max value will be last
		double [] temp = this.Qtable[this.environment.stateToIndex(state)].clone();
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
		int random_index = (int)(Math.random()* (counter + 1));
		//int random_index = (int)(Math.random()*counter + 1);
		//out.println("counter before loop is " + counter + " for state " + state.toString());
		//out.println("random index before loop is " + random_index + " for state " + state.toString());
		
		// find the index of the (random) max action and return it
		for (int i=0;i<this.Qtable[this.environment.stateToIndex(state)].length; i++)
		{
			if (this.Qtable[this.environment.stateToIndex(state)][i] == temp[temp.length-1])
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
		//System.out.println("random index after loop is " + random_index);
		}
		//out.println("result is " + result + " for state " + state.toString());
		return result;
	}
	
	
	public void single_step (Pair<Integer, Integer> state, int maxruns, int currentrun) 
	{
		
	}
	
	
	@Override
	public void single_run (Pair<Integer, Integer> state, int maxruns, int currentrun) 
	{
				
		//env.setAgentLocation(0, start_state);
		/*
		try {
			this.environment.clearAgentLocation(this.agentId);
			this.environment.setAgentLocation(this.agentId, state);
		} catch (OccupiedCellException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		//this.setCurrentState(state);
		if (debug)
			System.out.println("Agent "+this.getAgentId()+
					" start state is " + state.toString() + 
				" agent's location is " + this.getCurrentState().toString());

		Pair<Integer, Integer> current_state = state.copy();
		//Pair<Integer, Integer> current_state = start_state.copy();
		Pair<Integer, Integer> end_state;
		//System.out.println("currentrun: "+currentrun);
		//System.out.println("maxrun: "+maxruns);
		//if (debug)
			//System.out.print("current state = "+current_state.print());
		int next_action;
		
		//Pair goal = new Pair(2,2);
		if(debug2){
			System.out.println("Scenario starts =========== ");
			System.out.println("the goal state is " + this.getGoalState().toString());
		}
		//while (!current_state.equals(this.getGoalState())) //this.goalState)
		//while (k<=1000) // How many steps in one day is decided by no of 
		//states(3x3) and no of actions(6), take a loose bound to be sure!!
		//{
			this.environment.forwardTime();
			int currentrun_i = currentrun;
			int maxruns_i = maxruns;
			
			//System.out.print("current state = "+current_state.print());
			//System.out.print(" goal state = "+this.goalState.print());	
			//System.out.println("currentrun: "+currentrun_i);
			next_action = pick_next_action(current_state,maxruns_i,currentrun_i);
			//epsilon = epsilon/(double)k;
			
			if (debug3)
				System.out.println(" next action is "+next_action+" "+this.environment.getActionName(next_action));
			// should we do the current_state update here, and 
			// remove it from the Q_update, i.e., call Q_update
			// with state,action,state' ? What if the environment.transitionTo
			// returns two different results on the two calls? due to 
			// stochastic transitions?
			
			/*------------------- A wrapper for the interface --------------*/
			Map<Integer, Pair<Integer, Integer>> s = new HashMap<Integer, Pair<Integer, Integer>>();
			Map<Integer, String> a = new HashMap<Integer, String>();
			Map<Integer, String> type = new HashMap<Integer, String>();
			s.put(this.agentId, this.getCurrentState());//current_state);
			a.put(this.agentId, this.environment.getActionName(next_action));
			type.put(this.agentId, "cleaner");
			
			Map<Integer, Pair<Integer, Integer>> end;
			end_state = null;
			try {
				end = this.environment.getLocations(s, a, type);
				end_state = end.get(this.agentId);
			} catch (InvalidActionException e) {
				e.printStackTrace();
				System.out.println("You are committing a crime.");
			}
			
			/*-------------------- Wrapper ends ---------------*/
			
			if (debug)	
				System.out.println("end state is "+end_state.print());
			
			this.currentState = end_state.copy();
			//First update Q-value then visits to calculate alpha based on previous visits
			boolean converged = false;
			
			// if we are not an agent acting randomly
			// then makes sense to check the Q-tables for 
			// convergence
			if (!actAsRandomAgent)
			{
				if(countStableQ>=4 && currentrun>0)
				{
					//System.out.println ("Sanity check on run "+(currentrun-1));
					converged = true;
				}
			}
			Q_update (current_state,next_action,end_state,currentrun,converged);
			A_update (current_state,next_action);
			//if(debug2)
			//System.out.println(current_state.print() +"-> "+this.environment.getActionName(next_action)+"->"+end_state.print());
			current_state = end_state.copy();
			//current_state = this.environment.transitionTo(currentState, next_action);
			k= k+1;
			//
			//epsilon = epsilon/(double)k;
			//epsilon = epsilon*0.999;
			
		//}
		
		
		// Start the comparison of the Q-tables
		//
		boolean differenceFlag = true;
		boolean breakOuterLoop = false;
		//Q-table convergence check
		/*for (int i=0; i< this.Qtable.length; i++)
		{
			for (int j=0; j<this.Qtable[i].length; j++) 
			{   
				if(currentrun>9500)
					differenceFlag = true;
		        double currentDiff = Math.floor(Math.abs(Qtable[i][j]-QtablePrev[i][j]));
		        double percentDiff = 0.0;
		        if(currentrun>0)
		        {
		        if(PrevDiffTable[i][j] == 0.0 && currentDiff!=0.0)
		        	percentDiff = 1000000.0;// a very large number
		        else if (Math.abs((currentDiff-PrevDiffTable[i][j])) == 0.0) percentDiff = 0.0;
		        else percentDiff = 100.0*(Math.abs(currentDiff-PrevDiffTable[i][j])/PrevDiffTable[i][j]);
		        if((percentDiff) >= threshPercent)  //%change in difference to be considered as not changed 
		        {   
		        	
		        	differenceFlag = false;
		        }
		        }		        
		        
		        	        
		       System.out.println(" perdent diff is" + percentDiff );
		      //update the PrevDiffTable and currTable
		        PrevDiffTable[i][j] = currentDiff;
		        QtablePrev[i][j] = Qtable[i][j];
			}
		}
		//System.out.println(" difference flag is" + differenceFlag);
		
		if(differenceFlag = false)
        {
            countStableQ = countStableQ+1;
        }
		//if(differenceFlag == true && (currentrun>0)) // All values in cells did not change
		if(countStableQ>4 && currentrun>0)
		{
			System.out.println("Q table converged");
			System.out.println(" current run number" + currentrun);
			
			
		}*/
				
		//update the QTablePrev
		
		/*for (int i=0; i< this.Qtable.length; i++)
		{
			for (int j=0; j<this.Qtable[i].length; j++) 
			{
				
			}
		}*/
		
		// -----> No need for following behavior 
		// -----> if we are acting as random agent.
		//        No need for convergence check
		if (!actAsRandomAgent)
		{
			
			
		if (debugConvergence)
		{
			System.out.println("test");
			System.out.println(printQtable());
		}
		
		for (int i=0; i< this.Qtable.length; i++)
		{
			for (int j=0; j<this.Qtable[i].length; j++) 
			{   
				/*if(currentrun>9500)
					differenceFlag = true;*/
				
		        double currentDiff = Math.round((Math.abs(Qtable[i][j]-QtablePrev[i][j])));
		        //double percentDiff = 0.0;
		        if(currentrun>0)
			        if(currentDiff > threshAbs)  //%change in difference to be considered as not changed 
			        {   
			        	if (debugConvergence)
			        		System.out.println(" current diff is" + currentDiff );
			        	differenceFlag = false;
			        	breakOuterLoop = true;
			        	break;
			        }
		       // System.out.println(" current diff is" + currentDiff );
		        //QtablePrev[i][j] = Qtable[i][j];
			}	
			if(breakOuterLoop)
				break;
		}
		
		//update Q
		for (int i=0; i< this.Qtable.length; i++)
		{
			for (int j=0; j<this.Qtable[i].length; j++) 
			{ 
				QtablePrev[i][j] = Qtable[i][j];
			}
		}
		if (debugConvergence)
		{
			System.out.println(" difference flag is" + differenceFlag);
			System.out.println(" current run number" + currentrun);
		}
		if(differenceFlag == true && currentrun>0)
        {
            countStableQ = countStableQ+1;
        }
		if (differenceFlag == false && currentrun>0)
		{
			countStableQ = 0; //reset counter
		}
		if (debugConvergence)
			System.out.println(" CounterStableQ is" + countStableQ);
		//if(differenceFlag == true && (currentrun>0)) // All values in cells did not change
		if(countStableQ>=4 && currentrun>0)
		{
			System.out.print("Q table converged at ");
			System.out.println(" current run number" + currentrun);
		}
		
		
		
		//System.out.println(printQtable());
		
	}
	// ------> Ends behavior skipped by random agent
	// Close check for randomness
	// the following are common for either random or not
		
		if(debug2)
		{
			System.out.println("Scenario ends =========== ");
			System.out.println("Total number visits:" + k);
			System.out.println("epsilon:" + epsilon);
		}
			
		//this.currentState = currentState.copy();
	}
	
	
	@Override
	public void multiple_runs (int count, Pair<Integer, Integer> state)
	{
		for (int i=0; i<count; i++)
		{
			single_run(state,count,i);
			k=0;
			//System.out.println("countrun: "+count);
		}
		
	}
	
	@Override
	// DEPRICATED
	public void Q_update (Pair<Integer, Integer> currentState, int action)
	{	}
	
	@Override
	public void Q_update (Pair<Integer, Integer> currentState, int action, Pair<Integer, Integer> endState, int currentrun, boolean converged)
	{
		//if (debug)
			//System.out.println("Updating");
		
		double currentQ = this.Qtable[this.environment.stateToIndex(currentState)][action];
		//Calculate alpha
		double currentAlpha = 1/(this.Visittable[this.environment.stateToIndex(currentState)][action]+1.0);
		//currentQ = (1-this.alpha)*currentQ;
		// get the target state s'
		//State endState = this.environment.transitionTo(currentState, action);
		// get the reward of the end state s'
		/* --------------Wrapper for getRewards-----------------*/
		Map<Integer, Pair<Integer, Integer>> l = new HashMap<Integer, Pair<Integer, Integer>>();
		Map<Integer, String> t = new HashMap<Integer, String>();
		l.put(this.agentId, endState);
		t.put(this.agentId, "cleaner");
		double reward = this.environment.getRewards(l, t).get(this.agentId);
		
		//Check if we have found a reward
		// SOS 
		// (assumes 1, manually re-adjust as you play with worlds)
		if (reward > 0.0)
		{
			System.out.print("found reward in ");
			System.out.print(k+" steps and in my ");
			System.out.println(currentrun+"th run");
			
			if (saveStatistics)
			{
				PrintWriter out = null;
				try {
						out = new PrintWriter(new BufferedWriter(new 
								FileWriter("Statistics.txt",true)));
					
					//out.print("found reward in ");
					// print the current run, i.e., the day that you are in
					//out.print(currentrun+"\t");//"th run");
					// print the k, i.e., the steps that you have taken until 
					// you exit the loop... e.g., the steps till you found the reward
					out.print(k+"\t");//+" steps and in my ");
					//	if (converged)
					//		out.print("converged");
					out.println();
				}catch (IOException e) {
				    System.err.println(e);
				}finally{
				    if(out != null){
				        out.close();
				    }
				} 
			}
		}

		// ------> If acting as random, no need to update
		//         the Q-table -- we are not using it!
		if (!actAsRandomAgent)
		{
			// find the max value from the end state s'
			//List temp = Arrays.asList(this.Qtable[this.environment.stateToIndex(endState)]);
			//double max_step = (double) Collections.max(temp);
			double [] temp = this.Qtable[this.environment.stateToIndex(endState)].clone();
			Arrays.sort(temp);
			double max_step = temp[temp.length-1];
			
			//Calculate alpha
			//alpha = this.Visittable[this.environment.stateToIndex(endState)][action];
			
			// calculate new q-value
			this.Qtable[this.environment.stateToIndex(currentState)][action] =  
					currentQ + (currentAlpha*(reward + (this.gamma*max_step - currentQ) ));
			//this.Qtable[0][0] = 1000.0;
		}// ----> end of not needed behavior by random agent
		
	}
	
	public void A_update (Pair<Integer, Integer> currentState, int action)
	{
		double currentVisits = this.Visittable[this.environment.stateToIndex(currentState)][action];
		currentVisits = currentVisits+1;
		this.Visittable[this.environment.stateToIndex(currentState)][action] = currentVisits;
	}
	
	// conversions for types, from ints to strings and reverse
	public int stringTypeToInt (String type)
	{
		if (type.equals("cleaner")) return 0;
		else return 1;
	}
	public String intTypeToString (int type)
	{
		if (type == 0) return "cleaner";
		else return "viewer";
	}
	
	@Override
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
					qtable+=this.environment.getActionName(j)+" ";
				}
				qtable+="\r\n";
			}
			qtable += this.environment.indexToState(i).print()+" ";
			for (int j=0; j< this.Qtable[i].length; j++)
			{
				qtable += df.format(this.Qtable[i][j])+" ";
			}
			qtable += "\r\n";
		}
		return qtable;
	}
	
	@Override
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
					Visittable+=this.environment.getActionName(j)+" ";
				}
				Visittable+="\r\n";
			}
			Visittable += this.environment.indexToState(i).print()+" ";
			for (int j=0; j< this.Visittable[i].length; j++)
			{
				Visittable += df.format(this.Visittable[i][j])+" ";
			}
			Visittable += "\r\n";
		}
		return Visittable;
	}
	
	
	public void printTransitionTableenvironment()
	{
		//this.environment.printTransitionTable();
	}
	
	@Override
	public Pair<Integer, Integer> getCurrentState()
	{
		return this.currentState;
	}
	
	@Override
	// getter for agentId
	public int getAgentId(){
		return this.agentId;
	}
	
	public String getAgentType(){
		return this.TypeName;
	}
	
	@Override
	// setter for debugging
	public void setCurrentState(Pair<Integer, Integer> q)
	{
		this.currentState = q;
	}
	
	@Override
	public void resetQtable()
	{
		
		
		for (int i=0; i< this.Qtable.length; i++)
			for (int j=0; j<this.Qtable[i].length; j++)
				this.Qtable[i][j] = 0.0;
	}
	
	@Override
	public void resetVisittable()
	{
		for (int i=0; i< this.Visittable.length; i++)
			for (int j=0; j<this.Visittable[i].length; j++)
				this.Visittable[i][j] = 0.0;
	}
	
	@Override
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
	
	@Override
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
	
	@Override
	public void SaveToFileVisits(String s)
	{
		PrintWriter out = null;
		try {
		    out = new PrintWriter(new BufferedWriter(new FileWriter("Visitstables.txt", true)));
		    out.println(s+"\r\n"+this.printVisittable());
		    out.flush();
		}catch (IOException e) {
		    System.err.println(e);
		}finally{
		    if(out != null){
		        out.close();
		    }
		} 
	}
	
	public void setEpsilon (double e)
	{
		this.epsilon = e;
	}
	
	public boolean actsAsRandom ()
	{
		return this.actAsRandomAgent;
	}
	


	
	// conversions for action names, use the ones of the environment
	
	// unit testing 
	public static void main (String [] args) throws NoFreeSpaceException, OccupiedCellException
	{
		Environment env = new Floor(System.getProperty("user.dir") + "/src/" + args[0]);
		
		//environment.printTransitionTable();
		//environment.printRewards();
		
		Map<Integer, String> agentTypes = new HashMap<Integer, String>();
		Map<Integer, Pair<Integer, Integer>> start = new HashMap<Integer, Pair<Integer, Integer>>();
		Pair<Integer, Integer> start_stateA = new Pair<Integer, Integer>(3, 3);
		Pair<Integer, Integer> start_stateB = new Pair<Integer, Integer>(2, 1);
		//Map<Integer, Pair<Integer, Integer>> states = new HashMap<Integer, Pair<Integer, Integer>>();
		start.put(0, start_stateA);
		start.put(1, start_stateB);
		
		
		agentTypes.put(0, "cleaner");
		agentTypes.put(1, "viewer");
		
		IndependentLearners agentA = new IndependentLearners(env, "cleaner", 0.1, 0.9, start_stateA, 0);
		IndependentLearners agentB = new IndependentLearners(env, "viewer", 0.1, 0.9, start_stateB, 1);
		
		env.initTransitionProbs(1.0);
		env.initAgentLocations(agentTypes);
		//agent.printTransitionTableenvironment();
		agentA.printQtable();
		agentA.printVisittable();
		//agent.single_step(agent.getCurrentState(),agent.environment.decodeAction("right"));
		//agent.single_step(agent.getCurrentState());
		//agent.single_run(agent.getCurrentState());
		//System.out.println(agent.printQtable());
		String s = " ============== ";
		int runs = 300;
		int trials = 1;
		
		// If our agent is acting as random, set the 
		// epsilon to 0.0, so that we are always picking
		// a value from the Q-table. The Q-table is always
		// zero for random agent (i.e., never updated), thus
		// agent acts completely randomly
		//if (!agent.actsAsRandom()) 
		//	agent.setEpsilon(0.0);
		
		
		// clear out the file to write a fresh
		// run of trials
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter("Statistics.txt")));
		}catch (IOException e) {
		    System.err.println(e);
		}finally{
		    if(out != null){
		        out.close();
		    }
		}
		// start the trials... 
		// e.g., 10 trials for 30 runs each, means
		// 10 times of 30 days training, each time,
		// resetting the Q-table.
		IndependentLearners[] listOfAgents = {agentA,agentB};
		// first clear the agent locations from the world
		env.clearAgentLocation(0);
		env.clearAgentLocation(1);
		env.setAgentLocation(0, start_stateA);
		env.setAgentLocation(1, start_stateB);
		
		for (int i =0; i< trials; i++)
		{
			out = null;
			try {
				out = new PrintWriter(new BufferedWriter(new FileWriter("Statistics.txt",true)));
				out.println("===============");
				out.println("Trial = "+i);
				out.println("===============");
			}catch (IOException e) {
			    System.err.println(e);
			}finally{
			    if(out != null){
			        out.close();
			    }
			} 
			
			int max = 50;
			for (int steps = 0; steps<max;steps++)
			{
				//System.out.println("====== Step "+steps+"=========");
				for (IndependentLearners a : listOfAgents)
				{				
					//a.resetQtable();
					//a.resetVisittable();
					System.out.println("agent "+a.getAgentType()+" started from "+a.getCurrentState().print());
					a.single_run(start.get(a.getAgentId()), max, steps);
					System.out.println("agent "+a.getAgentType()+" went to "+a.getCurrentState().print());
					//a.singleStep(a.getCurrentState());
					//a.multiple_runs(runs, agentA.getCurrentState());
					System.out.println("-----------------");
				}
				//agentA.SaveToFileQ(s+"scenario "+trials+" for "+runs+" runs"+s);
				//agentA.SaveToFileVisits(s+"scenario "+trials+" for "+runs+" runs"+s);
		}
		}
	}

	@Override
	public void printTransitionTableWorld() {
		// TODO Auto-generated method stub
		
	}
	
}
