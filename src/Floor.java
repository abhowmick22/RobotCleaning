import interfaces.Environment;

import java.util.List;
import java.util.Map;

import utils.Pair;
import interfaces.Environment;

/*
 * An implementation of Environment, models a floor in a building.
 * Properties : static, non adversarial
 */
public class Floor implements Environment{
	
	private Pair dimensions;
	private Cell[][] grid;

	@Override
	public List<Pair> getAgentLocations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float executeActions(Map<Integer, String> actions,
			Map<Integer, String> agentTypes) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void printModel() {
		// TODO Auto-generated method stub
		
	}

}
