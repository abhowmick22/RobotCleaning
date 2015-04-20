
/*
 * This defines a basic cell in the grid representation
 * of the environment.
 */
public class Cell {
	
	private float dirtProb;			// probability of dirt
	private int cellType;				// the type of the cell (free, obstacle or occupied)		
	private int agentId;				// id of the present agent if occupied, last agent if free, -1 if never visited
	private boolean dirty;
	
	public Cell(){
		this.dirtProb = (float) 0.0;
		this.cellType = 0;
		this.agentId = -1;
		this.dirty = false;
	}
	
	// setters
	public void setCellType(int type){		/* 0->FREE, 1->OBSTACLE, 2->ROBOT*/
		this.cellType = type;
	}
	
	public void setDirtProb(float prob){
		this.dirtProb = prob;
	}
	
	public void setAgentId(int id){
		this.agentId = id;
	}
	
	public void setDirty(){
		this.dirty = true;
	}
	
	public void setClean(){
		this.dirty = false;
	}
	
	// getters
	public int getCellType(){
		return this.cellType;
	}
	
	public float getDirtProb(){
		return this.dirtProb;
	}
	
	public int getAgentId(){
		return this.agentId;
	}
	
	public boolean isDirty(){
		return this.dirty;
	}

}
