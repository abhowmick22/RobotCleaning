package utils;

public class NoFreeSpaceException extends Exception{

	public NoFreeSpaceException(){
		super("Number of agents exceeds the free space available");
	}
}
