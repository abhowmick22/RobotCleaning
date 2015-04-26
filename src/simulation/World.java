package simulation;

import utils.Pair;


public class World {

	// state is pair <x,y>
	// default 3x3 table
	private Pair<Integer, Integer>[] listOfStates = new Pair [9];
	// size of X-dimension
	private int xSize = 3;
	// size of Y-dimension
	private int ySize = 3;
	
	// actions as integers
	// default 0:up, 1:down, 2:left, 3:right
	private int [] Actions = {0,1,2,3};
	// table lookup for name of actions
	private String[] ActionsNames = {"up", "down", "left", "right"};
		
	// The final state 
	private Pair<Integer, Integer> goalState;
		
	// walls: assume 2
	// Wall[i][j] means there is a wall between
	// i and j.
	// Default, only one wall between (0,0) and
	// (0,1)
	private Pair<Integer, Integer>[][] Walls = new Pair [1][2];
	
	
	// transition table
	// note that whenever there is a wall, we return 
	// the same state ( similarly to the outer limits ). 
	private double [][][] transitions = new double [listOfStates.length][Actions.length][listOfStates.length];
	
	// rewards table (just for states)
	//private double [][] rewards = new double [States.length][Actions.length];
	private double [] rewards = new double [listOfStates.length];

	
	//
	// CONSTRUCTORS
	//
	//default constructor
	// deterministic world
		public World ()
		{ 
			// default initialization of states
			for (int i=0; i<3;i++)
				for (int j=0; j<3; j++)
					listOfStates[(i*3)+j] = new Pair<Integer, Integer>(i,j); // create table of pairs	
			
			// default initialization of transitions
			for (Pair<Integer, Integer> state : listOfStates)
			{	
				// up actions
				if ((int)state.getFirst()<2)
				{
					Pair<Integer, Integer> temp = new Pair<Integer, Integer>((Integer)state.getFirst()+1, (Integer)state.getSecond());
					transitions[this.stateToIndex(state)][0][this.stateToIndex(temp)] = 1.0;
				}
				if ((int)state.getFirst()==2)
				{
					transitions[this.stateToIndex(state)][0][this.stateToIndex(state)] = 1.0;
				}
				
				//down actions
				if ((int)state.getFirst()>0)
				{
					Pair<Integer, Integer> temp = new Pair<Integer, Integer>((Integer)state.getFirst()-1, (Integer)state.getSecond());
					transitions[this.stateToIndex(state)][1][this.stateToIndex(temp)] = 1.0;
				}
				if ((int)state.getFirst()==0)
				{
					transitions[this.stateToIndex(state)][1][this.stateToIndex(state)] = 1.0;
				}
				
				//left actions
				if ((int)state.getSecond()>0)
				{
					Pair<Integer, Integer> temp = new Pair<Integer, Integer>((Integer)state.getFirst(), (Integer)state.getSecond()-1);
					transitions[this.stateToIndex(state)][2][this.stateToIndex(temp)] = 1.0;
				}
				if ((int)state.getSecond()==0)
				{
					transitions[this.stateToIndex(state)][2][this.stateToIndex(state)] = 1.0;
				}
				
				//right actions
				if ((int)state.getSecond()<2)
				{
					Pair<Integer, Integer> temp = new Pair<Integer, Integer>((Integer)state.getFirst(), (Integer)state.getSecond()+1);
					transitions[this.stateToIndex(state)][3][this.stateToIndex(temp)] = 1.0;
				}
				if ((int)state.getSecond()==2)
				{
					transitions[this.stateToIndex(state)][3][this.stateToIndex(state)] = 1.0;
				}
			}
			
			// init rewards
			this.rewards[this.stateToIndex(new Pair<Integer, Integer>(2,2))] = 100.0;
			
			// add a wall
			this.Walls[0][0] = new Pair<Integer, Integer>(0,0);
			this.Walls[0][1] = new Pair<Integer, Integer>(0,1);
			
			//this.goalState = new State (2,2);
		}
		
	// typical constructor
	public World (Pair<Integer, Integer> goalState)
	{
		this();
		//this.States = new Pair[size*size];
		//for (int i=0; i<size;i++)
		//	for (int j=0; j<size; j++)
		//		States[(i*size)+j] = new Pair(i,j); // create table of pairs		
		//this.xSize = this.ySize = size;
		this.goalState = goalState;
	}
	
	// get-er methods
	public Pair<Integer, Integer>[] getListOfStates()
	{
		return this.listOfStates;
	}
	public int getFirstSize()
	{
		return this.xSize;
	}
	public int getSecondSize()
	{
		return this.ySize;
	}
	
	public Pair<Integer, Integer> getGoalState()
	{
		return this.goalState;
	}
	
	public int[] getActions()
	{
		return this.Actions;
	}
	
	public String[] getActionsNames()
	{
		return this.ActionsNames;
	}
	
	public String getActionName (int action)
	{
		if ((action<0) || (action > (this.Actions.length-1)))
			return "No such action";
		else return this.ActionsNames[action];
	}
	
	public int decodeAction (String action)
	{
		int result = -1;
		switch (action)
		{
			case "up": result=  0;
				break;
			case "down" : result = 1;
				break;
			case "left" : result =  2;
				break;
			case "right" : result=  3;
				break;
		}	
		return result;
	}
	
	
	// Go from (x,y) coordinates to States indexes
	public int stateToIndex (Pair<Integer, Integer> state)
	{
		return (((int)state.getFirst()*this.xSize)+(int)state.getSecond());
	}
	
	// Go from int state to (x,y) coordinate
	public Pair<Integer, Integer> indexToState (int state)
	{
		int factor = state/this.xSize;
		return (new Pair<Integer, Integer> (factor, state-(this.xSize*factor)));
	}
	
	
	// return transition
	public Pair<Integer, Integer> transitionTo (Pair<Integer, Integer> q, int action )
	{
		Pair<Integer, Integer> state_prime = null;
		for (Pair<Integer, Integer> state_to : listOfStates)
		{
			if (this.transitions[this.stateToIndex(q)][action][this.stateToIndex(state_to)] == 1.0)
			{
				state_prime = state_to;
				break;
			}
		}
		// walll check
		//if ((this.Walls[0][0].equals(q)) && (this.Walls[0][1].equals(state_prime)))
		//	state_prime = q;
		
		return state_prime;
	}
	
	
	
	// return reward
	public double getReward (Pair<Integer, Integer> q)
	{
		return this.rewards[this.stateToIndex(q)];
	}
	// calculate rewards for viewers
		// assume: if at a state the reward is 100, i.e., there 
		// is dirt, then for all neighboring states the reward 
		// is 33 (or some other number). This reflects the fact
		// that viewers can see dirt from a distance. However,
		// whenever an agent requests for a reward, then this extra 
		// rewards are hidden to the cleaners. To simplify, we assume
		// that rewards are even for everyone, and odd for the viewers.
		
	// printing functions 
	public void printTransitionTable()
	{
		System.out.println("=======================");
		System.out.println("====  Transitions =====");
		System.out.println("=======================");
		for (int i=0; i<this.Actions.length; i++)
		{
			System.out.println("Action "+this.getActionName(i));
			
			for (int j=0; j<this.listOfStates.length ; j++)
				for (int k=0; k< this.listOfStates.length; k++)
					if (this.transitions[j][i][k] == 1.0)
						System.out.println(this.indexToState(j).print()+" -> "+this.indexToState(k).print());
			
			System.out.println();
		}
		System.out.println("===    End transitions   ===");
	}
	public void printRewards()
	{
		System.out.println("=======================");
		System.out.println("====  Rewards =====");
		System.out.println("=======================");	
		for (int i =this.xSize-1; i>= 0; i--)
		{
			for (int j = 0; j<this.ySize; j++)
			{
				System.out.print(this.getReward(new Pair<Integer, Integer>(i,j))+" ");
			}
			System.out.println();
		}
		
		System.out.println("===    End rewards   ===");
	}
	
	
	//unit testing
	public static void main (String[] args)
	{
		System.out.println("Testing the world");
		World test = new World();
		// test tables
		test.printTransitionTable();
		test.printRewards();
		//test actions
		System.out.println(test.getActionName(0)+" is "+test.decodeAction(test.getActionName(0)));
		// testing wall
		Pair<Integer, Integer> state = new Pair<Integer, Integer>(0,0);
		Pair<Integer, Integer> state_prime;
		state_prime = test.transitionTo(state, test.decodeAction("right"));
		System.out.println(state.print()+" -> "+test.decodeAction("right")+" -> "+state_prime.print());
	
		System.out.println();
		//testing reward and movement
		Pair<Integer, Integer> state1 = new Pair<Integer, Integer>(0,0);
		Pair<Integer, Integer> state_prime1;
		state_prime1 = test.transitionTo(state1, test.decodeAction("up"));
		System.out.println(state1.print()+" -> "+test.decodeAction("up")+" -> "+state_prime1.print());
		System.out.println(state_prime1.print()+" reward is "+test.getReward(state_prime1));
		
		state1 = test.transitionTo(state_prime1, test.decodeAction("up"));
		System.out.println(state_prime1.print()+" -> "+test.decodeAction("up")+" -> "+state1.print());
		System.out.println(test.getReward(state1));
		
		state_prime1 = test.transitionTo(state1, test.decodeAction("up"));
		System.out.println(state1.print()+" -> "+test.decodeAction("up")+" -> "+state_prime1.print());
		System.out.println(test.getReward(state_prime1));
		
		state1 = test.transitionTo(state_prime1, test.decodeAction("right"));
		System.out.println(state_prime1.print()+" -> "+test.decodeAction("right")+" -> "+state1.print());
		System.out.println(test.getReward(state1));
		
		state_prime1 = test.transitionTo(state1, test.decodeAction("right"));
		System.out.println(state1.print()+" -> "+test.decodeAction("right")+" -> "+state_prime1.print());
		System.out.println(test.getReward(state_prime1));
		
		state1 = test.transitionTo(state_prime1, test.decodeAction("right"));
		System.out.println(state_prime1.print()+" -> "+test.decodeAction("right")+" -> "+state1.print());
		System.out.println(test.getReward(state1));
		
	}
		
}

