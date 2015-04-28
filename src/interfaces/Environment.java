package interfaces;
import java.util.List;
import java.util.Map;

import utils.InvalidActionException;
import utils.NoFreeSpaceException;
import utils.OccupiedCellException;
import utils.Pair;

/*
 * This represents the environment as a grid, in
 * which agents live.
 */
public interface Environment {
	
	
	// return a randomized initial location of agents in the environment
	public Map<Integer, Pair<Integer, Integer> > initAgentLocations(Map<Integer, String> agentTypes) 
			throws NoFreeSpaceException, OccupiedCellException;
	
	// initialize the transition probabilities, this interface is subject to change
	// pass in the number of actions, this will allot dominant probability to the intended
	// action and distribute the remaining probability over all other actions
	public void initTransitionProbs(double dominantProb);
	
	// return location of all agents in the environment (S,a) -> S'
	// based on the agent types (limited or partial observability)
	// Q-learning will converge to optimal even if new locations are stochastic (but probability of transition has to be deterministic)
	public Map<Integer, Pair<Integer, Integer> > getLocations(Map<Integer, Pair<Integer, Integer> > locations,
									Map<Integer, String> actions,
									Map<Integer, String> agentTypes) throws InvalidActionException;
	
	// specify list of agent actions
	// input is a map from agent id to action and map from agent id to agent type
	// returns a map of rewards from agent id to agent reward
	// r(S,a)
	// Returned rewards will be deterministic
	public Map<Integer, Double> getRewards(Map<Integer, Pair<Integer, Integer> > locations,
									Map<Integer, String> agentTypes);
	
	// update time of the environment, to be called by simulator
	// updates stuff such as dirt and other environment variables
	public void forwardTime();
	
	// allow to change type of a cell location in the environment
	public void setLocationType(Pair<Integer, Integer> location, int type);
	// allow to change probability of dirt at a cell location in the environment
	public void setLocationDirtProb(Pair<Integer, Integer> location, float prob);
	// get a list of free spaces in the environment
	public List<Pair<Integer, Integer>> getFreeSpaces();
	
	/* ------------ These were added for compatibility with Yannis' version -----------------*/
	// get list of all locations/states
	public Pair<Integer, Integer>[] getListOfStates();
	// get the width
	public int getFirstSize();
	// get the height
	public int getSecondSize();
	// get the goal state as decided by environment (a high reward sink)
	public Pair<Integer, Integer> getGoalStateSingleAgent();
	// get the goal state for factored agent (specifying a goal state for each agent)
	public Map<Integer, Pair<Integer, Integer>> getGoalStateFactoredAgent(
			Map<Integer, String> agentTypes, String map);
	// get actions allowed for agent in environment
	public int[] getAvailableActionIndices(String agentType);
	// get the names of actions allowed for this agent in environment
	public String[] getAvailableActions(String agentType);
	// get all possible actions in the environment
	public String[] getAllActions();
	// get action corresponding to an index
	public String getActionName (int action);
	// get action index corresponding to name
	public int getActionIndex(String action);
	// given an action return its integer index
	public int decodeAction (String action);
	// Go from (x,y) coordinates to State's index
	public int stateToIndex (Pair<Integer, Integer> state);
	// Go from state index to (x,y) coordinate
	public Pair<Integer, Integer> indexToState (int state);
	// printing functions 
	/*
	public void printTransitionTable();
	public void printRewards();
	*/
	
	
	
}
