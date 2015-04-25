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
	
	// return location of all agents in the environment
	// based on the agent types (limited or partial observability)
	public Map<Integer, Pair<Integer, Integer> > observeAgentLocations(
									Map<Integer, String> agentTypes);
	
	// specify list of agent actions
	// input is a map from agent id to action and map from agent id to agent type
	// returns a map of rewards from agent id to agent reward
	// side-effect: update locations of agents
	public Map<Integer, Float> executeActions(Map<Integer, String> actions, 
									Map<Integer, String> agentTypes) throws InvalidActionException;
	
	// allow to change type of a cell location in the environment
	public void setLocationType(Pair<Integer, Integer> location, int type);
	// allow to change probability of dirt at a cell location in the environment
	public void setLocationDirtProb(Pair<Integer, Integer> location, float prob);
	// get a list of free spaces in the environment
	public List<Pair<Integer, Integer>> getFreeSpaces();
	
	// print grid model that is learned so far
	public void printModel();
	
	
}
