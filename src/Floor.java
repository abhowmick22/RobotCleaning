import interfaces.Environment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	Map<Integer, Pair<Integer, Integer>> agentLocations;
	
	private final float REWARD_CLEAN_WASTEFUL = (float) -0.5;	// reward for useful clean
	private final float REWARD_CLEAN_USEFUL = (float) 1.0;		// penalty for wasteful clean
	private final float REWARD_MOTION = (float) 0.0;				// no penalty on motion	
	private final float REWARD_NO_MOTION = (float) 0.0;			// no reward on no motion

	// custom comparator that reads in a model and initializes the model
	public Floor(String modelPath){
		init(modelPath);
	}
	
	private void init(String modelPath){
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(modelPath)));
			input.readLine();				// read file header
			String[] dims = input.readLine().split(",");
			this.dimensions.setFirst(Integer.parseInt(dims[0]));
			this.dimensions.setSecond(Integer.parseInt(dims[1]));
			int width = dimensions.getFirst();
			int height = dimensions.getSecond();
			grid = new Cell[height][width];
			freeSpaces = new ArrayList<Pair<Integer, Integer>>();
			agentLocations = new HashMap<Integer, Pair<Integer, Integer>>();
			String line = null;
			
			input.readLine();
			input.readLine();
			for(int i=0 ; i<height; i++){
				line = input.readLine();
				String[] tokens = line.split(",");
				
				// num of cells returned should be equal to width
				for(int j=0; j<width; j++){
					int type = Integer.parseInt(tokens[j]);
					grid[i][j].setCellType(type);
					if(type == 0)	freeSpaces.add(new Pair<Integer, Integer>(i, j));
				}
			}
			
			input.readLine();
			input.readLine();
			for(int i=0 ; i<height; i++){
				line = input.readLine();
				String[] tokens = line.split(",");
				
				// num of cells returned should be equal to width
				for(int j=0; j<width; j++){
					grid[i][j].setDirtProb(Float.parseFloat(tokens[j]));
				}
			}
			input.close();
		
		} catch (FileNotFoundException e) {
			System.out.println("Model path not found.");
		} catch (IOException e) {
			System.out.println("Could not read model file.");
		}
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
				this.agentLocations.put(agentId, location);
			}
			else{
				throw new OccupiedCellException();
			}
		}
		return result;
	}

	@Override
	// return proper coordinates if type is "viewer", negative values if type is "cleaner" 
	public Map<Integer, Pair<Integer, Integer>> observeAgentLocations(Map<Integer, String> agentTypes) {
		Map<Integer, Pair<Integer, Integer>> observations = 
				new HashMap<Integer, Pair<Integer, Integer>>();
		for(Integer agentId : agentTypes.keySet()){
			String agentType = agentTypes.get(agentId);
			Pair<Integer, Integer> trueLocation = agentLocations.get(agentId);
			Pair<Integer, Integer> resultLocation = new Pair<Integer, Integer>();
			if(agentType.equals("viewer")){				/* return true location*/
				resultLocation.setFirst(trueLocation.getFirst());
				resultLocation.setSecond(trueLocation.getSecond());
			}
			else if(agentType.equals("cleaner")){		
				resultLocation.setFirst(-1);
				resultLocation.setSecond(-1);
			}
			observations.put(agentId, resultLocation);
		}
		return observations;
	}

	@Override
	// TODO: Implement
	public Map<Integer, Float> executeActions(Map<Integer, String> actions,
			Map<Integer, String> agentTypes) throws InvalidActionException {
		// execute action for each specified agent
		Map<Integer, Float> rewards = new HashMap<Integer, Float>();
		for(Integer agentId : agentTypes.keySet()){
			String agentType = agentTypes.get(agentId);
			String action = actions.get(agentId);
			if(!agentType.equals("cleaner") && action.equals("clean"))
				throw new InvalidActionException();
			rewards.put(agentId, getReward(agentType, action, agentId));	/* updates locations as a side-effect*/
		}		
		return rewards;
	}

	@Override
	public void printModel() {
		// TODO Auto-generated method stub
		
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
	
	
	private float getReward(String agentType, String action, int agentId) throws InvalidActionException{
		Pair<Integer, Integer> currLocation = agentLocations.get(agentId);
		float reward = (float) 0.0;
		if(action.equals("clean")){		/* deal with clean action*/	
			if(grid[currLocation.getFirst()][currLocation.getSecond()].isDirty())
				reward = REWARD_CLEAN_USEFUL;			
			else
				reward = REWARD_CLEAN_WASTEFUL;			
		}
		else{											/* deal with motion actions */
			int i = currLocation.getFirst();
			int j = currLocation.getSecond();
			int dest;
			reward = REWARD_NO_MOTION;
			switch(action){
			case "north":
				dest = Math.max(i - 1, 0);
				if(grid[dest][j].getCellType() == 0){	/* destination is free */
					currLocation.setFirst(dest);		/* update robot location */
					grid[dest][j].setCellType(2);		/* update dest cell info*/
					grid[dest][j].setAgentId(agentId);	/* update dest cell info*/
					grid[i][j].setCellType(0);			/* update source cell info*/
					reward = REWARD_MOTION;
				}
				break;
			case "south":
				dest = Math.min(i + 1, dimensions.getFirst() - 1);
				if(grid[dest][j].getCellType() == 0){	
					currLocation.setFirst(dest);
					grid[dest][j].setCellType(2);
					grid[dest][j].setAgentId(agentId);
					grid[i][j].setCellType(0);
					reward = REWARD_MOTION;
				}
				break;
			case "east":
				dest = Math.min(j + 1, dimensions.getSecond() - 1);
				if(grid[i][dest].getCellType() == 0){	
					currLocation.setSecond(dest);
					grid[i][dest].setCellType(2);
					grid[i][dest].setAgentId(agentId);
					grid[i][j].setCellType(0);
					reward = REWARD_MOTION;
				}
				break;
			case "west":
				dest = Math.max(j - 1, 0);
				if(grid[i][dest].getCellType() == 0){	
					currLocation.setSecond(dest);
					grid[i][dest].setCellType(2);
					grid[i][dest].setAgentId(agentId);
					grid[i][j].setCellType(0);
					reward = REWARD_MOTION;
				}
				break;
			default:
				
			}
		}
		return reward;
		
	}
	
}
