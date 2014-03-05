package odra.sbql.emiter;

import java.nio.ByteBuffer;
import java.util.Stack;
import java.util.Vector;

import odra.sbql.emiter.instructions.BranchInstruction;
import odra.sbql.emiter.instructions.Instruction;
import odra.sbql.emiter.instructions.ParametrizedInstruction;
import odra.system.Sizes;

public class JulietCode {
	private Instruction start = null;

	private Instruction end = null;

	private  Stack<BranchInstruction> breaks;

	private  Stack<BranchInstruction> continues;
	private  Vector<Instruction> returns;
	
	
	
	public JulietCode copy(){
	    JulietCode copy = new JulietCode();
	    Instruction i = this.getStart();
	    while(i != null){
		Instruction icopy = i.copy();
		if(this.breaks.contains(i)){
		    copy.emitBreakInstruction();
		}else if(this.continues.contains(i)){
		    copy.emitContinueInstruction();
		}else if(this.returns.contains(i)){
		    copy.emitReturnHandle();
		}
		else
		    copy.emit(icopy);
		i = i.getNext();
		
	    }
	 
	    return copy;
	}
	/**
	 * Create an empty Juliet Code with no instructions
	 * 
	 */
	public JulietCode() {
		this.start = null;
		this.end = null;
		this.breaks = new Stack<BranchInstruction>();
		this.continues = new Stack<BranchInstruction>();
		this.returns = new Vector<Instruction>();
	}

	/**
	 * Create new Juliet Code with one instruction
	 * 
	 * @param i -
	 *           first instruction in the list
	 */
	public JulietCode(Instruction i) {
		this();
		this.start = i;
		this.end = i;
	}

	/**
	 * Create new Juliet code based on existing code
	 * 
	 * @param il -
	 *           existing code
	 */
	public JulietCode(JulietCode il) {
		this();
		this.append(il);
	}

	/**
	 * @return first instruction in the code
	 */
	public Instruction getStart() {
		return start;
	}

	/**
	 * @return first instruction in the code
	 */
	public Instruction getEnd() {
		return end;
	}

	/**
	 * removes all instructions
	 */
	public void clean() {
		this.start = null;
		this.end = null;
	}

	/**
	 * Check if the code is empty
	 * 
	 * @return true if empty, false otherwise
	 */
	public boolean isEmpty() {
		return this.start == null;
	}

	/**
	 * Insert non-parametrized instruction at the end of the code
	 * 
	 * @param opcode -
	 *           opcode of instruction to add
	 * @return (mereged) code
	 */
	public JulietCode emit(OpCodes opcode) {
		return this.emit(new Instruction(opcode));
	}

	/**
	 * Insert parametrized instruction at the end of the code
	 * 
	 * @param opcode -
	 *           opcode of instruction to add
	 * @param param -
	 *           parameter of instruction to add
	 * @return (mereged) code
	 */
	public JulietCode emit(OpCodes opcode, int param) {
		return this.emit(new ParametrizedInstruction(opcode, param));
	}

	/**
	 * Insert branch instruction at the end of the code
	 * 
	 * @param opcode -
	 *           opcode of branch instruction to add
	 * @param target -
	 *           target of instruction to branch to
	 * @return (mereged) code
	 */
	public JulietCode emit(OpCodes opcode, Instruction target) {
		return this.emit(new BranchInstruction(opcode, target));
	}

	/**
	 * Insert branch instruction for break expression at the end of the code Perform updateBreaks to validate branch
	 * 
	 * @return (mereged) code
	 */
	public JulietCode emitBreakInstruction() {
		BranchInstruction br = new BranchInstruction(OpCodes.bra, null);
		breaks.push(br);
		return this.emit(br);
	}

	/**  emits a handle for optional further inject 'before return' finalize code
	 * @return
	 */
	public JulietCode emitReturnHandle() {
		Instruction br = new Instruction(OpCodes.nop);
		returns.add(br);
		return this.emit(br);
	}
	/**
	 * Insert branch instruction for continue expression at the end of the code Perform updateContinues to validate
	 * branch
	 * 
	 * @return (mereged) code
	 */
	public JulietCode emitContinueInstruction() {
		BranchInstruction br = new BranchInstruction(OpCodes.bra, null);
		continues.push(br);
		return this.emit(br);
	}

	/**
	 * Insert instruction at the end of the code
	 * 
	 * @param instruction -
	 *           instruction to add
	 * @return (mereged) code
	 */
	public JulietCode emit(Instruction instruction) {
		if (this.isEmpty()) {
			this.start = instruction;
			this.end = instruction;
			instruction.nullifyPreviousAndNext();
		} else {
			this.end = this.end.setAsNext(instruction, true);
		}
		return this;
	}

	/**
	 * Insert Juliet code at the end of this code
	 * 
	 * @param il -
	 *           Juliet code to add
	 * @return (mereged) code
	 */
	public JulietCode append(JulietCode il) {
		if (il.equals(this)) {
			assert false : "unable to append itself";
			return this;
		}
		if (!il.isEmpty()) {
			if (isEmpty()) {
				this.start = il.start;
			} else {
				this.end.setAsNext(il.start, false);
			}
			end = il.end;

			while (!il.breaks.empty()) {
				BranchInstruction instruction = il.breaks.pop();
				breaks.push(instruction);
			}
			while (!il.continues.empty()) {
				BranchInstruction instruction = il.continues.pop();
				continues.push(instruction);
			}		
			for(Instruction i : il.returns){
			    this.returns.add(i);
			}
			il.returns.clear();
		}
		return this;
	}
	
	
	/**
	 * Complete return code
	 * 
	 * @param beforeReturnCode - the code to add before return
	 * for each return the copy of the param code is inserted 
	 */
	public void fixReturn(JulietCode beforeReturnCode) {
	     for(Instruction reti : this.returns){
		 if(beforeReturnCode.isEmpty()) return;
		 JulietCode copy = beforeReturnCode.copy();
		 if(reti.getPrevious() != null){
		     reti.getPrevious().setNext(copy.getStart());
		     copy.getStart().setPrevious(reti.getPrevious());
		 }else
		     copy.getStart().setPrevious(null);
		 
		 reti.setPrevious(copy.getEnd());
		 copy.getEnd().setNext(reti);
		 
		 
		 while (!copy.breaks.empty()) {
			BranchInstruction instruction = copy.breaks.pop();
			breaks.push(instruction);
		}
		while (!copy.continues.empty()) {
			BranchInstruction instruction = copy.continues.pop();
			continues.push(instruction);
		}	
		 
	     }
		
	}
	
	/**
	 * Updates all unspecified break and continue branch instructions
	 * 
	 * @param breakTarg -
	 *           instruction to branch to for break
	 * @param continueTarg - -
	 *           instruction to branch to for continue
	 */
	public boolean backPatch(Instruction breakTarg, Instruction continueTarg) {
	    boolean result = backPatchBreaks(breakTarg); 
		result =  backPatchContinues(continueTarg) || result; 
		return result;
	}

	/**
	 * Updates all unspecified break branch instructions
	 * 
	 * @param breakDestination -
	 *           instruction to branch to
	 */

	private boolean backPatchBreaks(Instruction breakDestination) {
	    	if(breaks.empty()) return false;
		while (!breaks.empty()) {
			breaks.pop().setTarget(breakDestination);
		}
	    	return true;
	}

	/**
	 * Updates all unspecified continue branch instructions
	 * 
	 * @param continueDestination -
	 *           instruction to branch to
	 */
	private boolean backPatchContinues(Instruction continueDestination) {
	    	if(continues.empty()) return false;
		while (!continues.empty()) {
			continues.pop().setTarget(continueDestination);
		}
		return true;
	}


	/**
	 * Convers Juliet code into byte[]
	 * 
	 * @return code as byte[]
	 */
	public byte[] getByteCode() {
	    	if(isEmpty())
	    	    return new byte[0];
	   
		int index = this.generateInstructionIndexes();
		
		
		
		ByteBuffer bbuf = ByteBuffer.allocate(index);

		Instruction curr = this.start;
		while (curr != null) {
			OpCodes opcode = curr.getOpcode();
			/**
			 * skip nops
			 */
			if (!opcode.equals(OpCodes.nop)) {
				bbuf.putInt(opcode.getCode());
				if (curr instanceof ParametrizedInstruction) {
					bbuf.putInt(((ParametrizedInstruction) curr).getParam());
				} else if (curr instanceof BranchInstruction) {
					int offset = ((BranchInstruction) curr).getTarget().getIndex();
					bbuf.putInt(offset);
				} else {
					bbuf.putInt(0);
				}
			}
			curr = curr.getNext();
		}
		return bbuf.array();
	}
	private int generateInstructionIndexes(){
	    int index = 0;
		Instruction curr = this.start;
		// TODO: to refactor with an iterator which encapsulates traversing the instruction list 
		/**
		 * calculate bytecode length and number instructions
		 */
		while (curr != null) {
			curr.setIndex(index);
			/**
			 * skip nops
			 */
			if (curr.getOpcode() != OpCodes.nop) {
				index = index + INSTRUCTION_LENGTH;
			}
			curr = curr.getNext();
		}

		
		return index;
	}
	
	/**
	 * @return the number of instructions in the JulietCode
	 */
	public int size(){		
		if(isEmpty())
			return 0;
		int size = 0;
		Instruction i = this.start;
		do {
			size ++;
			i = i.getNext();
		}while(i != null);
		return size;
	}
	
	public final static int INSTRUCTION_LENGTH = (2 * Sizes.INTVAL_LEN); // [operand + param]

	
	
	
}