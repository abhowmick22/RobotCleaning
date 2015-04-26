package simulation;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import utils.InvalidActionException;
import utils.NoFreeSpaceException;
import utils.OccupiedCellException;
import utils.Pair;
import interfaces.AgentGroup;
import interfaces.Environment;


/*
 * This is the main simulation engine. Controls the robots
 * and environments, collects and reports statistics.
 */
public class Simulator {
	
	// entities with following interfaces are must for simulation
	private static AgentGroup robots;
	private static Environment env;
	
	// Simulation variables
	private static long time;
	private static int NUM_ACTIONS = 4;
	private static double dominantProbability = 0.85;
	
	// Statistics
	private static long dirtCollected;
	private static long timeTaken;
	
	public static void main(String[] args) throws InvalidActionException{

		env = new Floor(System.getProperty("user.dir") + "/src/" + args[0]);
		FileWriter fw;
		try {
			fw = new FileWriter(new File("output").getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//((Floor) env).printDirtModel();				
		//((Floor) env).printCurrentDirtState();
		//((Floor) env).printCurrCellTypes();
		
		//TEST THAT FORWARD TIME ACCUMULATES DIRT
		
		//((Floor) env).printCurrentDirtState();
		//((Floor) env).forwardTime();
		//((Floor) env).printCurrentDirtState();
		
		
		((Floor) env).initTransitionProbs(4, dominantProbability);
		// create agent types
		Map<Integer, String> agentTypes = new HashMap<Integer, String>();
		agentTypes.put(0, "viewer");
		agentTypes.put(1, "cleaner");
		agentTypes.put(2, "cleaner");
		try {
			Map<Integer, Pair<Integer, Integer>> locations = ((Floor) env).initAgentLocations(agentTypes);
			
			//((Floor) env).printAgentLocations();
			//((Floor) env).printCurrCellTypes();
			
			/*
			// Run one simulation step for testing motion
			System.out.println("Before step");
			System.out.println("Agent locations are " + locations.toString());
			((Floor) env).printAgentLocations();
			((Floor) env).printAgentLocationsOnGrid();
			
			// create action input
			Map<Integer, String> actions = new HashMap<Integer, String>();
			actions.put(0, "south");
			actions.put(1, "east");
			actions.put(2, "west");
			
			locations = ((Floor) env).getLocations(locations, actions, agentTypes);
			System.out.println("Actions are " + actions.toString());
			System.out.println("After step");
			System.out.println("Agent locations are " + locations.toString());
			((Floor) env).printAgentLocations();
			((Floor) env).printAgentLocationsOnGrid();
			*/
			
			// Run one simulation step for testing function
			/*
			System.out.println("Before step");
			System.out.println("Agent locations are " + locations.toString());
			((Floor) env).printCurrentDirtState();
			
			// create action input
			Map<Integer, String> actions = new HashMap<Integer, String>();
			actions.put(0, "observe");
			actions.put(1, "clean");
			actions.put(2, "clean");
			System.out.println("Actions are " + actions.toString());
			
			locations = ((Floor) env).getLocations(locations, actions, agentTypes);
			Map<Integer, Float> rewards = ((Floor) env).getRewards(locations, agentTypes);
			System.out.println("After step");
			System.out.println("Agent locations are " + locations.toString());
			System.out.println("Agent rewards are " + rewards.toString());
			((Floor) env).printCurrentDirtState();
			*/
			
			
		} catch (NoFreeSpaceException | OccupiedCellException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}

}