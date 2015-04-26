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
		
		
				
		
		
		
	}

}