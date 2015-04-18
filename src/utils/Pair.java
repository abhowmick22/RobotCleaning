package utils;

public class Pair<T, Y> {
	
	private T first;
	private Y second;
	
	public T getFirst(){return this.first;}
	public Y getSecond(){return this.second;}
	
	public void setFirst(T first){this.first = first;}
	public void setSecond(Y second){this.second = second;}
}
