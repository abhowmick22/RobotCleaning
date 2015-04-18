package interfaces;
import java.io.File;
import java.util.List;
import java.util.Map;

import utils.Pair;

/*
 * This represents the environment as a grid, in
 * which agents live.
 */
public interface Environment {
	
	// return location of all agents in the environment
	// use this after execute actions
	public List<Pair> getAgentLocations();
	
	// specify list of agent actions, returns resulting reward
	// input is a map from agent id to action and map from agent id to agent type
	public float executeActions(Map<Integer, String> actions, 
									Map<Integer, String> agentTypes);
	
	// print grid model
	public void printModel();
	
	
}
