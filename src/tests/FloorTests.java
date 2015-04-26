package tests;

import java.util.HashMap;
import java.util.Map;

import simulation.Floor;
import utils.InvalidActionException;
import utils.NoFreeSpaceException;
import utils.OccupiedCellException;
import utils.Pair;
import interfaces.Environment;

public class FloorTests {
	
	// entities with following interfaces are must for simulation
	private static Environment env;
	
	// Simulation variables
	private static double dominantProbability = 0.85;
	
	public static void main(String[] args){
		
		env = new Floor(System.getProperty("user.dir") + "/src/" + args[0]);
		//TEST: FORWARD TIME ACCUMULATES DIRT
		
				//((Floor) env).printCurrentDirtState();
				((Floor) env).forwardTime();
				//((Floor) env).printCurrentDirtState();
				
				
				((Floor) env).initTransitionProbs(dominantProbability);
				// create agent types
				Map<Integer, String> agentTypes = new HashMap<Integer, String>();
				agentTypes.put(0, "viewer");
				agentTypes.put(1, "cleaner");
				agentTypes.put(2, "cleaner");
				try {
					Map<Integer, Pair<Integer, Integer>> locations = ((Floor) env).initAgentLocations(agentTypes);
					
					//((Floor) env).printAgentLocations();
					//((Floor) env).printCurrCellTypes();
					
					
					// Run one simulation step for testing motion
					//System.out.println("Before step");
					//System.out.println("Agent locations are " + locations.toString());
					//((Floor) env).printAgentLocations();
					//((Floor) env).printAgentLocationsOnGrid();
					
					// create action input
					Map<Integer, String> actions = new HashMap<Integer, String>();
					actions.put(0, "south");
					actions.put(1, "east");
					actions.put(2, "west");
					
					locations = ((Floor) env).getLocations(locations, actions, agentTypes);
					//System.out.println("Actions are " + actions.toString());
					//System.out.println("After step");
					//System.out.println("Agent locations are " + locations.toString());
					//((Floor) env).printAgentLocations();
					//((Floor) env).printAgentLocationsOnGrid();
					
					
					// Run one simulation step for testing function
					System.out.println("Before step");
					((Floor) env).printCurrentDirtState();
					((Floor) env).printAgentLocationsOnGrid();
					// create action input for actions
					actions.put(0, "observe");
					actions.put(1, "clean");
					actions.put(2, "clean");
					System.out.println("Actions are " + actions.toString());
					
					locations = ((Floor) env).getLocations(locations, actions, agentTypes);
					Map<Integer, Double> rewards = ((Floor) env).getRewards(locations, agentTypes);
					System.out.println("After step");
					System.out.println("Agent locations are " + locations.toString());
					System.out.println("Agent rewards are " + rewards.toString());
					((Floor) env).printCurrentDirtState();
					
					
				} catch (NoFreeSpaceException | OccupiedCellException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	}

}
