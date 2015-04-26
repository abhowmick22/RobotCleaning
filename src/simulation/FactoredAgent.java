package simulation;
import utils.Pair;
import interfaces.Environment;

import java.util.*;

public class FactoredAgent 
{
	// list of agents
	private ArrayList<Agent> listOfAgents;
	// our copy of the environment
	private Environment environment;

	
	// Arraylist or Maps? 
	// They are used in the Q-table as indices
	// Going with Arrays for now
	//private Map<Integer, Pair<Integer,Integer>>[] listOfStates;
	//private Map <Integer,String>[] listOfActions;
	 private ArrayList<Map<Integer, Pair<Integer,Integer>>> listOfStates;
	 private ArrayList<Map <Integer,String>> listOfActions;	
	//private Map<Integer, Pair<Integer,Integer>> listOfStates;
	//private ArrayList<Map <Integer,String>> listOfActions;
	
	private double [][] Qtable;
	
	
	
	// "setters" to add agents 
	// to the factored agent
	// These methods help to have a 
	// factored agent with different types of agents. 
	// (Instead of having N identical ones)
	public void addAgent (Agent a)
	{
		this.listOfAgents.add(a);
		this.addStates(environment.getListOfStates(String agentType,a.getAgentId);
		this.addActions(environment.getActions(String agentType,a.getAgentId);
		this.Qtable = new double[this.listOfStates.size()][this.listOfActions.size()];
		
	}
	
	private void addStates (Pair<Integer, Integer>[] listOfNewStates, Integer agentId)
	{
		for (Map<Integer, Pair<Integer,Integer>> old_state : this.listOfStates)
			for (Pair<Integer, Integer> state : listOfNewStates)
				old_state.put(agentId, state);			
	}
	
	private void addActions (int[] listOfActions, Integer agentId)
	{
		for (Map <Integer,String> old_actions : this.listOfActions)
			for (int action : listOfActions)
				old_actions.put(agentId, action-decode-to-string);			
		
	}
	
	
	// Adds an array of agents to the factored 
	// agent, by adding them one by one
	public void addMultipleAgent (Agent[] agents)
	{
		for (Agent a : agents)
			addAgent(a);
	}
	
	public static void main (String[] args)
	{
		
	}
	
}
