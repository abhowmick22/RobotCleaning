package utils;

public class InvalidActionException extends Exception{
	public InvalidActionException(){
		super("Agent tried to execute an invalid action.");
	}
}
