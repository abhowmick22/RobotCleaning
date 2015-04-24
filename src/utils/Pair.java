package utils;

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
	
	public String toString() {return "("+this.first.toString()+","+this.second.toString()+")";}
	
	//unit testing
	public static void main (String []args)
	{
		Pair<Integer,Integer> coordinate= new Pair<Integer,Integer>(10,20);
		System.out.println(coordinate.toString());
	}
}
