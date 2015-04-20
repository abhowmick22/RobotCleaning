package utils;

public class OccupiedCellException extends Exception{
	public OccupiedCellException(){
		super("The cell from freeSpaces list is occupied. Cannot insert agent.");
	}
}
