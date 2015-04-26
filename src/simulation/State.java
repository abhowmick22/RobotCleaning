package simulation;
public class State {

  private final int x;
  private final int y;

  public State(int left, int right) 
  {
    this.x = left;
    this.y = right;
  }

  public int getX() 
  { 
	  return x; 
  }
  public int getY() 
  { 
	  return y; 
  }
  
  public String print()
  {
	  return ("("+this.x+","+this.y+")");
  }

  public boolean equals(Object other) 
  {
  	if (other instanceof State) 
  	{
  		State otherPair = (State) other;
  		return ((  this.x == otherPair.x) && (this.y == otherPair.y));
  	}
  	return false;
  }
  
  public State copy ()
  {
	  return new State (this.getX(), this.getY());
  }
  
  public State copy (State otherState)
  {
	  return new State (otherState.getX(), otherState.getY());
  }
  
  protected Object clone() throws CloneNotSupportedException 
  {
      return super.clone();
  }
  
  public static void main (String [] args)
  {

		State X = new State(1,0); 
		//try{
		// State Y = (State) X.clone();
		// System.out.println(Y.getX());
		//} catch(CloneNotSupportedException c){}  
		State Y = X.copy();
		System.out.println(Y.getX());
  }
}