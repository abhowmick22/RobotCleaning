package simulation;
import utils.InvalidActionException;
import utils.Pair;
import interfaces.Environment;
import interfaces.AgentInterface;
//import utils.State;



import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.*;



public class FactoredAgent //implements AgentInterface{
{
	// list of agents
	private ArrayList<Agent> listOfAgents;
	
	// id of the agent
	private int agentId;
	
	private final DecimalFormat df = new DecimalFormat("#.##");
	private boolean debug = false;
	private boolean debug2 = true;

	//private int[] Types ={0,1};
	//private String[] TypesName = {"cleaner", "viewer"};	
	private  Map<Integer, String> agentTypes;
	//private String TypeName;

	// These are unique to the agent
	// The current and final states 
	private Map<Integer, Pair<Integer,Integer>> currentState;
	private Map<Integer, Pair<Integer,Integer>> goalState;

	private Environment environment;

	// size of X-dimension
	//private int xSize;
	// size of Y-dimension
	//private int ySize;
	
	// actions as integers
	// default 0:up, 1:down, 2:left, 3:right
	//private int [] Actions;
	// table lookup for name of actions
	//private String[] ActionsNames;

	// List of states and actions for the Q-Table
	private ArrayList<Map<Integer, Pair<Integer,Integer>>> listOfStates;
	private ArrayList<Map <Integer,String>> listOfActions;	

		
	// initial alpha and gamma and initial epsilon for calculating the Q-table
	private double alpha = 1;
	private double gamma = 0.9; 
	private double epsilon = 0.3;
	
	private int k = 0;


	// Q-table both global and local
	public double [][] Qtable = new double [0][0];// = new double [States.length][Actions.length];
	//public double [][] QJointtable = new double [0][0]; // Joint q table
	
	// Alpha-Table: Tracks number of visits to each state
	public double [][] Visittable = new double [0][0];
	
	// CONSTRUCTORS
	// default constructor empty
	public FactoredAgent() {}
	
	
	// constructor with normal inits
	public FactoredAgent (Environment env, 
			//String type, 
			double alpha, double gamma, 
			Map<Integer, Pair<Integer,Integer>> init_state, 
			int agentId,
			Agent a)
	{
		this.agentId = agentId;
		this.environment = env;
		this.currentState = init_state;
		// call to inserting an agent in the list 
		this.addAgent(a);
		
		//this.Type = this.stringTypeToInt(type);
		//this.TypeName = type;
		//this.listOfStates = env.getListOfStates();
		//this.Actions = this.environment.getAvailableActionIndices(this.TypeName);
		//this.ActionsNames = this.environment.getAvailableActions(this.TypeName);
		this.alpha = alpha;
		this.gamma = gamma;
		//init Q-table
		this.Qtable = new double[this.listOfStates.size()][this.listOfActions.size()];
				
		for (int i=0; i< this.Qtable.length; i++)
			for (int j=0; j<this.Qtable[i].length; j++)
				this.Qtable[i][j] = 0.0;
		
		//init visits-Table
		this.Visittable = new double[this.listOfStates.size()][this.listOfActions.size()];
		for (int i=0; i< this.Visittable.length; i++)
			for (int j=0; j<this.Visittable[i].length; j++)
				this.Visittable[i][j] = 0.0;
		
		if (debug)
			System.out.println("Q-table initialized with size "+this.Qtable.length);
		    System.out.println("Visited-table initialized with size "+this.Visittable.length);
	}

	
	
	
	// "setters" to add agents 
	// to the factored agent
	// These methods help to have a 
	// factored agent with different types of agents. 
	// (Instead of having N identical ones)
	public void addAgent (Agent a)
	{
		this.listOfAgents.add(a);
		this.addStates(environment.getListOfStates(),a.getAgentId());
		this.addActions(environment.getAvailableActions(a.getAgentType()),a.getAgentId());	
		this.goalState.put(a.getAgentId(), a.getGoalState()) ;
		this.agentTypes.put(a.getAgentId(), a.getAgentType());
	}
	
	private void addStates (Pair<Integer, Integer>[] listOfNewStates, Integer agentId)
	{
		for (Map<Integer, Pair<Integer,Integer>> old_state : this.listOfStates)
			for (Pair<Integer, Integer> state : listOfNewStates)
				old_state.put(agentId, state);			
	}
	
	private void addActions (String[] listOfActions, Integer agentId)
	{
		for (Map <Integer,String> old_actions : this.listOfActions)
			for (String action : listOfActions)
				old_actions.put(agentId, action);			
		
	}
	
	
	// Adds an array of agents to the factored 
	// agent, by adding them one by one
	public void addMultipleAgent (Agent[] agents)
	{
		for (Agent a : agents)
			this.addAgent(a);
	}
	

	
	//@Override
	public Map<Integer, Pair<Integer,Integer>>  getGoalState ()
	{
		return this.goalState;
	}
	
	
	public int factoredStateToIndex(Map<Integer, Pair<Integer,Integer>>  state)
	{   
		return this.listOfStates.indexOf(state);
	}
	
	public int factoredActionToIndex(Map<Integer, String>  action)
	{   
		return this.listOfActions.indexOf(action);
	}
	//@Override
	//Pick action based on e-greedy policy: choose action based on max q-value with probability 1-e and choose a (uniform) random action with probability e
	public Map <Integer,String> pick_next_action (Map<Integer, Pair<Integer,Integer>>  state, int maxruns, int currentrun)
	{
		int result = -1;
		
		// find the max value of actions
		
		// first sort the array
		// max value will be last
		double [] temp = this.Qtable[this.factoredStateToIndex(state)].clone();
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
		for (int i=0;i<this.Qtable[this.factoredStateToIndex(state)].length; i++)
		{
			if (this.Qtable[this.factoredStateToIndex(state)][i] == temp[temp.length-1])
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
		return this.listOfActions.get(result);
	}

	
	
	
	//@Override
	public void single_run (Map<Integer, Pair<Integer,Integer>>  state, int maxruns, int currentrun) 
	{
		Map<Integer, Pair<Integer,Integer>> current_state = new HashMap<Integer, Pair<Integer,Integer>> (); 
		current_state.putAll(state); 
		Map<Integer, Pair<Integer,Integer>> end_state = new HashMap<Integer, Pair<Integer,Integer>> (); ;
		
		//System.out.println("currentrun: "+currentrun);
		//System.out.println("maxrun: "+maxruns);
		if (debug)
			System.out.print("current state = "+current_state.toString());
		Map <Integer,String> next_action;
		
		//Pair goal = new Pair(2,2);
		if(debug2)
			System.out.println("Scenario starts =========== ");
		
		while (!current_state.equals(this.getGoalState())) //this.goalState)
		{
			int currentrun_i = currentrun;
			int maxruns_i = maxruns;
			
			if (debug)
			{
				System.out.print("current state = "+current_state.toString());
				//System.out.print(" goal state = "+this.goalState.print());	
			}
			//System.out.println("currentrun: "+currentrun_i);
			next_action = pick_next_action(current_state,maxruns_i,currentrun_i);
			//epsilon = epsilon/(double)k;
			
			if (debug)
				System.out.println(" next action is "+next_action.toString());
			// should we do the current_state update here, and 
			// remove it from the Q_update, i.e., call Q_update
			// with state,action,state' ? What if the environment.transitionTo
			// returns two different results on the two calls? due to 
			// stochastic transitions?
			/*if (next_action == 3) 
			{	
				Pair temp = new Pair(1,0);
				current_state.print();
				if (current_state.equals(temp))
					end_state = this.environment.transitionTo(current_state, next_action);
				else 
					end_state = this.environment.transitionTo(new Pair(current_state.getX(), current_state.getY()), next_action);
			}			
			else*/		
			
			
			if (debug)	
				System.out.println("end state is "+end_state.toString());
			
			//First update Q-value then visits to calculate alpha based on previous visits
			
			Q_update (current_state,next_action,end_state);
			A_update (current_state,next_action);
			
			if(debug2)
			System.out.println(current_state.toString() +"-> "+next_action.toString()+"->"+end_state.toString());
			current_state = end_state;
			//current_state = this.environment.transitionTo(currentState, next_action);
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
	
	
	//@Override
	public void multiple_runs (int count, Map<Integer, Pair<Integer,Integer>> state)
	{
		for (int i=0; i<count; i++)
		{
			
			single_run(state,count,i);
			//System.out.println("countrun: "+count);
		}
		
	}
	
	//@Override
	public void Q_update (Map<Integer, Pair<Integer,Integer>> currentState, Map <Integer,String> action)
	{
		if (debug)
			System.out.println("Updating");
		
		double currentQ = this.Qtable[this.factoredStateToIndex(currentState)][this.factoredActionToIndex(action)];
		double currentAlpha = 1/(this.Visittable[this.factoredStateToIndex(currentState)][this.factoredActionToIndex(action)]+1.0);
		//currentQ = (1-this.alpha)*currentQ;
		// get the target state s'
		
		
		Map<Integer, Pair<Integer,Integer>> endState = null;
		try {
			endState = this.environment.getLocations(currentState, action, this.agentTypes);
		} catch (InvalidActionException e) {
			e.printStackTrace();
			System.out.println("FOCK OFF!");
		}
		
		Map<Integer, Double> rewards = this.environment.getRewards(currentState, action);
		double reward = 0.0;
		for (Double value : rewards.values())
			reward+=value;
		
		// find the max value from the end state s'
		//List temp = Arrays.asList(this.Qtable[this.environment.stateToIndex(endState)]);
		//double max_step = (double) Collections.max(temp);
		double [] temp = this.Qtable[this.factoredStateToIndex(currentState)].clone();
		Arrays.sort(temp);
		double max_step = temp[temp.length-1];
		
		// calculate new q-value
		this.Qtable[this.factoredStateToIndex(currentState)][this.factoredActionToIndex(action)] =  
				currentQ + (currentAlpha*(reward + (this.gamma*max_step - currentQ) ));
		//this.Qtable[0][0] = 1000.0;
	}
	
	//@Override
	public void Q_update (Map<Integer, Pair<Integer,Integer>> currentState, Map <Integer,String> action, Map<Integer, Pair<Integer,Integer>> endState)
	{
		if (debug)
			System.out.println("Updating");
		
		double currentQ = this.Qtable[this.factoredStateToIndex(currentState)][this.factoredActionToIndex(action)];
		//Calculate alpha
		double currentAlpha = 1/(this.Visittable[this.factoredStateToIndex(currentState)][this.factoredActionToIndex(action)]+1.0);
		//currentQ = (1-this.alpha)*currentQ;
		// get the target state s'
		//State endState = this.environment.transitionTo(currentState, action);
		// get the reward of the end state s'
		Map<Integer, Double> rewards = this.environment.getRewards(currentState, action);
		double reward = 0.0;
		for (Double value : rewards.values())
			reward+=value;
		

		// find the max value from the end state s'
		//List temp = Arrays.asList(this.Qtable[this.environment.stateToIndex(endState)]);
		//double max_step = (double) Collections.max(temp);
		double [] temp = this.Qtable[this.factoredStateToIndex(currentState)].clone();
		Arrays.sort(temp);
		double max_step = temp[temp.length-1];
		
		//Calculate alpha
		//alpha = this.Visittable[this.environment.stateToIndex(endState)][action];
		
		// calculate new q-value
		this.Qtable[this.factoredStateToIndex(currentState)][this.factoredActionToIndex(action)] =  
				currentQ + (currentAlpha*(reward + (this.gamma*max_step - currentQ) ));
		//this.Qtable[0][0] = 1000.0;
	}
	
	public void A_update (Map<Integer, Pair<Integer,Integer>> currentState, Map <Integer,String> action)
	{
		double currentVisits = 
				this.Visittable[this.factoredStateToIndex(currentState)][this.factoredActionToIndex(action)];
		currentVisits = currentVisits+1;
		this.Visittable[this.factoredStateToIndex(currentState)][this.factoredActionToIndex(action)] = currentVisits;
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
	
	//@Override
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
	
	//@Override
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
	
	//@Override
	public Map<Integer, Pair<Integer,Integer>>  getCurrentState()
	{
		return this.currentState;
	}
	
	//@Override
	// getter for agentId
	public int getAgentId()
	{
		return this.agentId;
	}
	
	//public String getAgentType()
	//{
	//	return this.TypeName;
	//}
	
	//@Override
	// setter for debugging
	public void setCurrentState(Map<Integer, Pair<Integer,Integer>>  q)
	{
		this.currentState = q;
	}
	
	//@Override
	public void resetQtable()
	{
		for (int i=0; i< this.Qtable.length; i++)
			for (int j=0; j<this.Qtable[i].length; j++)
				this.Qtable[i][j] = 0.0;
	}
	
	//@Override
	public void resetVisittable()
	{
		for (int i=0; i< this.Visittable.length; i++)
			for (int j=0; j<this.Visittable[i].length; j++)
				this.Visittable[i][j] = 0.0;
	}
	
	//@Override
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
	
	//@Override
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
	
	//@Override
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
	// conversions for action names, use the ones of the environment
	
	// unit testing 
	public static void main (String [] args)
	{
		Environment env = new Floor(System.getProperty("user.dir") + "/src/" + args[0]);
		
		//environment.printTransitionTable();
		//environment.printRewards();
		Pair<Integer, Integer> state = 
				new Pair<Integer, Integer>(env.getFirstSize()-1,env.getSecondSize()-1);
		Agent agent = new Agent(env, "cleaner", 0.1, 0.9, state, 0);
		//agent.printTransitionTableenvironment();
		agent.printQtable();
		agent.printVisittable();
		System.out.println("=================");
		//agent.single_step(agent.getCurrentState(),agent.environment.decodeAction("right"));
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

	//@Override
	public void printTransitionTableWorld() {
		// TODO Auto-generated method stub
		
	}

	
	
	
	
	
}
