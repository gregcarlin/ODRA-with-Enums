package odra.sbql.emiter.instructions;

import odra.sbql.emiter.OpCodes;

public class ParametrizedInstruction extends Instruction {

	protected int param;
	
	public ParametrizedInstruction(OpCodes opcode, int param){
		super(opcode);
		this.param = param;
		
	}
	
	public void setParam(int param){
		this.param = param;
	}
	
	public int getParam(){
		return param;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.emiter.instructions.Instruction#copy()
	 */
	@Override
	public Instruction copy()
	{	    
	    return new ParametrizedInstruction(this.opcode, this.param);
	}

}
