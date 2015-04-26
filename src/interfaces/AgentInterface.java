package interfaces;
import utils.Pair;


/*
 * This represents an agent in the environment.
 */
public interface AgentInterface {
	
	// Retrieve the goal state of the agent
	public Pair<Integer, Integer> getGoalState ();
	
	//Pick action based on e-greedy policy: choose action based on max q-value with probability 1-e and choose a (uniform) random action with probability e
	public int pick_next_action (Pair<Integer, Integer> state, int maxruns, int currentrun);
	
	// take a single step in the Q-learning algorithm from a given state and action
	//	public void single_step (Pair<Integer, Integer> state, int action);
	// public void single_step (Pair<Integer, Integer> state) ;
	
	// run a single episode, from the start state till you reach the end state
	public void single_run (Pair<Integer, Integer> state, int maxruns, int currentrun); 
	
	// run "count" episodes from a start state till you reach the end state
	public void multiple_runs (int count, Pair<Integer, Integer> state, Environment env);
	
	// update Q-table given a state and action
	public void Q_update (Pair<Integer, Integer> currentState, int action);
	
	// update Q-table given a state, action, and end-state
	public void Q_update (Pair<Integer, Integer> currentState, int action, Pair<Integer, Integer> endState);

	// Print the Q-table
	public String printQtable ();
	
	// Print the visits table
	public String printVisittable ();

	// Print the transition table for the world
	// -- used for debugging
	public void printTransitionTableWorld();
	
	// Get agent's current state
	public Pair<Integer, Integer> getCurrentState();
	
	// get agent's id
	public int getAgentId();

	// get agent's type
	public String getAgentType();
	
	// setter for debugging
	public void setCurrentState(Pair<Integer, Integer> q);

	// Reset the Q-table so you can run simulations
	// from scratch
	public void resetQtable();
	
	// Reset Visits table
	public void resetVisittable();
	
	// Save Q-table to file
	public void SaveToFile();

	// Save Q-table to file while prefixing it with a string
	// -- used for "naming"the table, in case we are storing
	// multiple ones on a single file
	public void SaveToFileQ(String s);
	
	// Same as above but for Visits table
	public void SaveToFileVisits(String s);
	
	

}
