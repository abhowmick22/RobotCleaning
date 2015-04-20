import interfaces.AgentGroup;
import interfaces.Environment;


/*
 * This is the main simulation engine. Controls the robots
 * and environments, collects and reports statistics.
 */
public class Simulator {
	
	// entities with following interfaces are must for simulation
	private static AgentGroup robots;
	private static Environment environment;
	
	// Simulation variables
	private static long time;
	
	// Statistics
	private static long dirtCollected;
	private static long timeTaken;
	
	public static void main(String[] args){

		environment = new Floor(args[0]);
		
		
	}

}