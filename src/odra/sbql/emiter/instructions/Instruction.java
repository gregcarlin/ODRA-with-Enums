package odra.sbql.emiter.instructions;

import odra.sbql.emiter.OpCodes;

public class Instruction {
	private Instruction prev = null;

	private Instruction next = null;

	protected OpCodes opcode;

	private int index = -1;

	public Instruction(OpCodes opcode) {
		this.opcode = opcode;
	}

	public OpCodes getOpcode() {
		return opcode;
	}

	@Override
	public String toString() {
		return opcode.name();
	}

	public void setIndex(int val) {
		this.index = val;
	}

	public int getIndex() {
		return this.index;
	}

	public boolean isConstant() {
		switch (opcode) {
			case ldcR:
			case ldcS:
			case ldI:
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg0) {
		return ((arg0 instanceof Instruction) && (((Instruction) arg0).opcode == this.opcode));
	}

	public final void nullifyPreviousAndNext() {
		this.setPrevious(null);
		this.setNext(null);
	}

	public final void setNext(Instruction instruction) {
		this.next = instruction;
	}

	public final void setPrevious(Instruction instruction) {
		this.prev = instruction;
	}

	public final Instruction getNext() {
		return this.next;
	}

	public final Instruction getPrevious() {
		return this.prev;
	}

	public final Instruction setAsNext(Instruction instruction, boolean nullify) {
		if (nullify) {
			instruction.nullifyPreviousAndNext();
		}
		this.next = instruction;
		instruction.prev = this;
		return instruction;
	}
	
	public Instruction copy(){
	    return new Instruction(this.opcode);
	}
}
