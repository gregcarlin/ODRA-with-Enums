package odra.sbql.emiter.instructions;

import odra.sbql.emiter.OpCodes;

public class BranchInstruction extends Instruction {
	private Instruction target;
	
	public BranchInstruction(OpCodes opcode, Instruction target){
		super(opcode); 
		this.target = target;
		
	}
	
	public void setTarget(Instruction target){
		this.target = target;
	}
	
	public Instruction getTarget(){
		return target;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.emiter.instructions.Instruction#copy()
	 */
	@Override
	public Instruction copy()
	{
	    
	    return new BranchInstruction(this.opcode, this.target);
	}
	
}
