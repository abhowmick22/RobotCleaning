package utils;

public class CorruptedStateException extends Exception {
	public CorruptedStateException(){
		super("Location of agent is not consistent with the world.");
	}
}
