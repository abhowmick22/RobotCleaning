package utils;

/* Primarily used for representing coordinates, but can be used for other purposes also*/
public class Pair<T, Y> {
	
	private T first;
	private Y second;
	
	public Pair(T f, Y s){
		this.first = f;
		this.second = s;
	}
	public Pair() {	}

	public T getFirst(){return this.first;}
	public Y getSecond(){return this.second;}
	
	public void setFirst(T first){this.first = first;}
	public void setSecond(Y second){this.second = second;}
	
	
	public String print(){
		return ("("+this.getFirst().toString() + ","+ this.getSecond().toString() + ")");
	}
	

	public boolean equals(Object other) 
	{
	 	if (other instanceof Pair) 
	  	{
	  		Pair<?, ?> otherPair = (Pair<?, ?>) other;
	  		return ((  this.getFirst().equals(otherPair.getFirst())) && 
	  				(this.getSecond().equals(otherPair.getSecond())));
	  	}
	  	return false;
	}
	  
	  public Pair<T, Y> copy ()
	  {
		  return new Pair<T, Y> (this.getFirst(), this.getSecond());
	  }
	  
	  public Pair<T, Y> copy (Pair<T, Y> otherPair)
	  {
		  return new Pair<T, Y> (otherPair.getFirst(), otherPair.getSecond());
	  }
	  
	  protected Object clone() throws CloneNotSupportedException 
	  {
	      return super.clone();
	  }
	
	public String toString() {return "("+this.getFirst().toString()+","+this.getSecond().toString()+")";}
	
	//unit testing
	public static void main (String []args)
	{
		Pair<Integer,Integer> coordinate= new Pair<Integer,Integer>(10,20);
		System.out.println(coordinate.toString());
	}
}
