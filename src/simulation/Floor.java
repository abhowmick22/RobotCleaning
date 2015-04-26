package simulation;
import interfaces.Environment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.distribution.EnumeratedDistribution;

import utils.InvalidActionException;
import utils.NoFreeSpaceException;
import utils.OccupiedCellException;
import utils.Pair;

/*
 * An implementation of Environment, models a floor in a building.
 * Properties : static, non adversarial
 * Every cell generates dirt per second according to its dirt probability
 * 
 */
public class Floor implements Environment{
	
	private Pair<Integer, Integer> dimensions;
	private Cell[][] grid;
	private List<Pair<Integer, Integer>> freeSpaces;
	private Map<Integer, Pair<Integer, Integer>> agentLocations;
	// Internal transition probability table
	// given an action, we have a prob. distribution over other actions
	private Map<String, EnumeratedDistribution<String>> transitionProbs;	
	// executing Functions
	// this method stores whether the agent is executing its actions
	// that is 'clean' for cleaner and 'observe' for viewer
	private Map<Integer, Boolean> executingFunction;
	
	
	private final float REWARD_CLEAN_WASTEFUL = (float) -0.5;	// reward for useful clean
	private final float REWARD_CLEAN_USEFUL = (float) 1.0;		// penalty for wasteful clean
	private final float REWARD_MOTION = (float) 0.0;				// no penalty on motion	
	private final float REWARD_NO_MOTION = (float) 0.0;			// no reward on no motion
	
	private final int NORTH_INDEX = 0;
	private final int SOUTH_INDEX = 1;
	private final int EAST_INDEX = 2;
	private final int WEST_INDEX = 3;
	
	private Map<Integer, String> actionsByIndex;
	private Set<String> motionActions;

	// custom comparator that reads in a model and initializes the model
	public Floor(String modelPath){
		init(modelPath);
	}
	
	private void init(String modelPath){
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(modelPath)));
			input.readLine();				// read file header
			this.dimensions = new Pair<Integer, Integer>();
			String[] dims = input.readLine().split(",");
			this.dimensions.setFirst(Integer.parseInt(dims[0]));
			this.dimensions.setSecond(Integer.parseInt(dims[1]));
			int width = dimensions.getFirst();
			int height = dimensions.getSecond();
			this.grid = new Cell[height][width];
			this.freeSpaces = new ArrayList<Pair<Integer, Integer>>();
			this.agentLocations = new HashMap<Integer, Pair<Integer, Integer>>();
			this.executingFunction = new HashMap<Integer, Boolean>();
			String line = null;
			
			input.readLine();
			input.readLine();
			for(int i=0 ; i<height; i++){
				line = input.readLine();
				String[] tokens = line.split(",");
				
				// number of cells returned should be equal to width
				for(int j=0; j<width; j++){
					int type = Integer.parseInt(tokens[j]);
					this.grid[i][j] = new Cell();
					this.grid[i][j].setCellType(type);
					if(type == 0)	freeSpaces.add(new Pair<Integer, Integer>(i, j));
				}
			}
			
			input.readLine();
			input.readLine();
			for(int i=0 ; i<height; i++){
				line = input.readLine();
				String[] tokens = line.split(",");
				
				// number of cells returned should be equal to width
				for(int j=0; j<width; j++){
					grid[i][j].setDirtProb(Float.parseFloat(tokens[j]));
				}
			}
			input.close();
			
			// update Actions by Index, should be done through a function
			this.actionsByIndex = new HashMap<Integer, String>();
			this.actionsByIndex.put(NORTH_INDEX, "north");
			this.actionsByIndex.put(SOUTH_INDEX, "south");
			this.actionsByIndex.put(EAST_INDEX, "east");
			this.actionsByIndex.put(WEST_INDEX, "west");
			
			this.forwardTime();
			motionActions = new HashSet<String>();
			motionActions.add("north");
			motionActions.add("south");
			motionActions.add("east");
			motionActions.add("west");
		
		} catch (FileNotFoundException e) {
			System.out.println("Model path not found.");
		} catch (IOException e) {
			System.out.println("Could not read model file.");
		}
	}
	
	// initialize the transition probabilities
	public void initTransitionProbs(int numActions, double dominantProb){
		this.transitionProbs = new HashMap<String, EnumeratedDistribution<String>>();
		double otherProb = (double) (1.0 - dominantProb) / (numActions - 1);
		
		// north
		List<org.apache.commons.math3.util.Pair<String, Double>> n = new ArrayList<org.apache.commons.math3.util.Pair<String, Double>>();
		for(int i=0; i < numActions; i++){
			if(i == NORTH_INDEX)
				n.add(new org.apache.commons.math3.util.Pair<String, Double>("north", dominantProb));
			else
				n.add(new org.apache.commons.math3.util.Pair<String, Double>(this.actionsByIndex.get(i), otherProb));
		}
		EnumeratedDistribution<String> ndist = new EnumeratedDistribution<String>(n);
		this.transitionProbs.put("north", ndist);
		
		// south
		List<org.apache.commons.math3.util.Pair<String, Double>> s = new ArrayList<org.apache.commons.math3.util.Pair<String, Double>>();
		for(int i=0; i < numActions; i++){
			if(i == SOUTH_INDEX)
				s.add(new org.apache.commons.math3.util.Pair<String, Double>("south", dominantProb));
			else
				s.add(new org.apache.commons.math3.util.Pair<String, Double>(this.actionsByIndex.get(i), otherProb));
		}
		EnumeratedDistribution<String> sdist = new EnumeratedDistribution<String>(s);
		this.transitionProbs.put("south", sdist);
		
		// east
		List<org.apache.commons.math3.util.Pair<String, Double>> e = new ArrayList<org.apache.commons.math3.util.Pair<String, Double>>();
		for(int i=0; i < numActions; i++){
			if(i == EAST_INDEX)
				e.add(new org.apache.commons.math3.util.Pair<String, Double>("east", dominantProb));
			else
				e.add(new org.apache.commons.math3.util.Pair<String, Double>(this.actionsByIndex.get(i), otherProb));
		}
		EnumeratedDistribution<String> edist = new EnumeratedDistribution<String>(e);
		this.transitionProbs.put("east", edist);
		
		// west
		List<org.apache.commons.math3.util.Pair<String, Double>> w = new ArrayList<org.apache.commons.math3.util.Pair<String, Double>>();
		for(int i=0; i < numActions; i++){
			if(i == WEST_INDEX)
				w.add(new org.apache.commons.math3.util.Pair<String, Double>("west", dominantProb));
			else
				w.add(new org.apache.commons.math3.util.Pair<String, Double>(this.actionsByIndex.get(i), otherProb));
		}
		EnumeratedDistribution<String> wdist = new EnumeratedDistribution<String>(w);
		this.transitionProbs.put("west", wdist);
		
	}
	
	
	
	@Override
	// based on agent types, we can try to come up with more
	// interesting initializations
	public Map<Integer, Pair<Integer, Integer>> initAgentLocations(Map<Integer, String> agentTypes) 
			throws NoFreeSpaceException, OccupiedCellException {
		int numAgents = agentTypes.size();
		int numFreeSpaces = this.freeSpaces.size();
		
		// First check if free spaces are available
		if( numAgents > numFreeSpaces){
			throw new NoFreeSpaceException();
		}
		
		// randomly permute free spaces
		Collections.shuffle(this.freeSpaces);
		
		// allot agents to these free spaces
		Map<Integer, Pair<Integer, Integer>> result = 
				new HashMap<Integer, Pair<Integer, Integer>>();
		
		for(Integer agentId : agentTypes.keySet()){
			Pair<Integer, Integer> freeCoordinate = freeSpaces.remove(0);
			int i = freeCoordinate.getFirst();
			int j = freeCoordinate.getSecond();
			Pair<Integer, Integer> location = new Pair<Integer, Integer>(i, j);
			if(grid[i][j].getCellType() == 0){
				result.put(agentId, location);
				grid[i][j].setCellType(2);
				grid[i][j].setAgentId(agentId);
				this.agentLocations.put(agentId, location);
			}
			else{
				throw new OccupiedCellException();
			}
		}
		return result;
	}

	@Override
	// return new coordinates based on transition probability table
	public Map<Integer, Pair<Integer, Integer>> getLocations(Map<Integer, Pair<Integer, Integer> > locations,
			Map<Integer, String> actions,
			Map<Integer, String> agentTypes) throws InvalidActionException {

		Map<Integer, Pair<Integer, Integer>> newLocations = new HashMap<Integer, Pair<Integer, Integer>>();
		for(Integer agentId : actions.keySet()){
			String action = actions.get(agentId);
			Pair<Integer, Integer> location = locations.get(agentId);
			String type = agentTypes.get(agentId);
			
			// Check if action is valid
			if(type.equals("cleaner") && action.equals("observe"))
				throw new InvalidActionException();
			if(type.equals("viewer") && action.equals("clean"))
				throw new InvalidActionException();		
			
			Pair<Integer, Integer> newLocation = null;
			try {
				// get the actual executed action (sampled from transition probability)
				// side-effect : update AgentLocation and whether it is executing function
				newLocation = getAgentLocation(location, action, agentId, type);
				newLocations.put(agentId, newLocation);
			} catch (InvalidActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return newLocations;
	}

	@Override
	// This is a stochastic function of state action pair
	// dirty state of the world is updated here on clean
	public Map<Integer, Float> getRewards(Map<Integer, Pair<Integer, Integer> > locations,
			Map<Integer, String> agentTypes) {
		// get reward for each location
		Map<Integer, Float> rewards = new HashMap<Integer, Float>();
		for(Integer agentId : locations.keySet()){
			String agentType = agentTypes.get(agentId);
			// Check if actions are valid
			
			float reward = (float) 0.0;
			if(this.grid[locations.get(agentId).getFirst()][locations.get(agentId).getSecond()].isDirty()){
				if(agentType.equals("cleaner") && executingFunction.get(agentId)){	// a cleaner is cleaning at a dirty location
					reward = REWARD_CLEAN_USEFUL;
					this.grid[locations.get(agentId).getFirst()][locations.get(agentId).getSecond()].setClean();
					this.executingFunction.put(agentId, false);
				}
			}
			// TODO: hand out rewards when viewers are doing useful observations
			rewards.put(agentId, reward);
		}		
		return rewards;
	}

	@Override
	public void setLocationType(Pair<Integer, Integer> location, int type) {
		grid[location.getFirst()][location.getSecond()].setCellType(type);
	}

	@Override
	public void setLocationDirtProb(Pair<Integer, Integer> location, float prob) {
		grid[location.getFirst()][location.getSecond()].setDirtProb(prob);
	}

	@Override
	public List<Pair<Integer, Integer>> getFreeSpaces() {
		return this.freeSpaces;
	}
	
	
	// return the agent location (new object) based on transition probability
	private Pair<Integer, Integer> getAgentLocation(Pair<Integer, Integer> location, String action, 
			int agentId, String agentType) throws InvalidActionException{
	
		Pair<Integer, Integer> currLocation = this.agentLocations.get(agentId);
		if(action.equals("clean") || action.equals("observe")){		
			this.executingFunction.put(agentId, true);			// set to executing action
		}
		else{											// deal with motion actions
			int i = currLocation.getFirst();
			int j = currLocation.getSecond();
			int dest;
			String actualAction = null;
			if(motionActions.contains(action ))
				actualAction = this.transitionProbs.get(action).sample();
			else
				actualAction = action;
				
			switch(actualAction){
			case "north":
				dest = Math.max(i - 1, 0);
				if(grid[dest][j].getCellType() == 0){	/* destination is free */
					currLocation.setFirst(dest);		/* update robot location */
					grid[dest][j].setCellType(2);		/* update dest cell info*/
					grid[dest][j].setAgentId(agentId);
					grid[i][j].setCellType(0);			/* update source cell info*/
				}
				break;
			case "south":
				dest = Math.min(i + 1, dimensions.getFirst() - 1);
				if(grid[dest][j].getCellType() == 0){	
					currLocation.setFirst(dest);
					grid[dest][j].setCellType(2);
					grid[dest][j].setAgentId(agentId);
					grid[i][j].setCellType(0);
				}
				break;
			case "east":
				dest = Math.min(j + 1, dimensions.getSecond() - 1);
				if(grid[i][dest].getCellType() == 0){	
					currLocation.setSecond(dest);
					grid[i][dest].setCellType(2);
					grid[i][dest].setAgentId(agentId);
					grid[i][j].setCellType(0);
				}
				break;
			case "west":
				dest = Math.max(j - 1, 0);
				if(grid[i][dest].getCellType() == 0){	
					currLocation.setSecond(dest);
					grid[i][dest].setCellType(2);
					grid[i][dest].setAgentId(agentId);
					grid[i][j].setCellType(0);
				}
				break;
			default:
				
			}
		}
		
		Pair<Integer, Integer> newLocation = new Pair<Integer, Integer>(currLocation.getFirst(),
														currLocation.getSecond());
		return newLocation;
	}

	@Override
	// For now it just updates dirt
	public void forwardTime() {
		for(int i=0; i<dimensions.getFirst(); i++){
			for(int j=0; j<dimensions.getSecond(); j++){
				if(grid[i][j].isDirty())	continue;			// dirt doesn't disappear on its own
				
				float prob = grid[i][j].getDirtProb();
				double rand = Math.random();
				if(rand < prob)		grid[i][j].setDirty();
			}
		}
		
	}
	
	/*DEBUG METHODS*/
	
	public void printDirtModel() {
		System.out.print("\n--------The Dirt Model is--------------\n");
		for(int i=0; i<dimensions.getFirst(); i++){
			for(int j=0; j<dimensions.getSecond(); j++){
				System.out.print(grid[i][j].getDirtProb() + " ");
			}
			System.out.println("");
		}
		System.out.print("\n---------------------------------------\n");
	}

	public void printCurrentDirtState() {
		// TODO Auto-generated method stub
		System.out.print("\n--------The current Dirt State is--------------\n");
		for(int i=0; i<dimensions.getFirst(); i++){
			for(int j=0; j<dimensions.getSecond(); j++){
				int dirt = grid[i][j].isDirty()?1:0;
				System.out.print(dirt + " ");
			}
			System.out.println("");
		}
		System.out.print("\n---------------------------------------\n");
	}

	public void printAgentLocations() {
		//private Map<Integer, Pair<Integer, Integer>> agentLocations;
		System.out.print("\n--------The agent locations are--------------\n");
		for(Integer agent : this.agentLocations.keySet()){
			Integer x= this.agentLocations.get(agent).getFirst();
			Integer y= this.agentLocations.get(agent).getSecond();
			System.out.println(agent + ": " + x + "," + y);
		}
		System.out.print("\n---------------------------------------\n");
		
	}
	
	public void printAgentLocationsOnGrid(){
		System.out.print("\n--------The agent locations on grid are--------------\n");
		for(int i=0; i<dimensions.getFirst(); i++){
			for(int j=0; j<dimensions.getSecond(); j++){
				if(grid[i][j].getCellType() == 2){
					int agentId = grid[i][j].getAgentId();
					System.out.print(agentId + " ");
				}
				else{
					System.out.print("-" + " ");
				}
			}
			System.out.println("");
		}
		System.out.print("\n---------------------------------------\n");
	}
	
	public void printCurrCellTypes() {
		// TODO Auto-generated method stub
		System.out.print("\n--------The current cell types are--------------\n");
		for(int i=0; i<dimensions.getFirst(); i++){
			for(int j=0; j<dimensions.getSecond(); j++){
				
				System.out.print(grid[i][j].getCellType() + " ");
			}
			System.out.println("");
		}
		
		System.out.print("\n---------------------------------------\n");
	}
	
}
