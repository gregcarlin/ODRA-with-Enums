package odra.sbql.assembler;

import java.nio.ByteBuffer;
import java.util.Hashtable;

import odra.sbql.emiter.OpCodes;
import odra.system.Sizes;

/**
 * This class represents a simple code emiter for Joliet assembler.
 * Each line of Joliet assembler can contain:
 * 
 * LABEL - i.e. 
 * :loop
 * OPERATION - i.e.
 * ldBag 
 * OPERATION WITH PARAMETER - i.e.
 * ldcI 32
 * OPERATION<bra*> WITH LABEL - i.e.
 * braTrue :loop
 * INSTR_NUMBER OPERATION - i.e.
 * 5 ldBag
 * INSTR_NUMBER OPERATION WITH PARAMETER - i.e.
 * 0024 ldcI 32
 * INSTR_NUMBER OPERATION<bra*> WITH LABEL - i.e.
 * 24 braTrue :loop 
 * or nothing!
 *       
 * note: instruction number starts with 0 and increases by 8 (offset count)
 * branches can be done with labels or instruction numbers
 *             
 * @author tkowals
 */

public class AssemblerEmiter {
	private AssemblerLexer lexer;
	private AssemblerToken token;

	private ByteBuffer bbuf;
	private int index = 0;
	
	private Hashtable<String, Integer> labels = new Hashtable<String, Integer>();
	
	public AssemblerEmiter(String str) throws AssemblerSyntaxErrorException {
		lexer = new AssemblerLexer(str);
		token = lexer.nextToken();
		while (token.kind != AssemblerToken.EOF)
			getLabels();
		
		bbuf = ByteBuffer.allocate(index);
		
		lexer = new AssemblerLexer(str);
		index = 0;
		token = lexer.nextToken();

		while (token.kind != AssemblerToken.EOF)
			getSingleInstructionCode();
		
	}

	// method for accessing assembler Bytecode! 
	public byte[] getByteCode() {
		return bbuf.array();
	}

	private void getLabels() throws AssemblerSyntaxErrorException {
			
		while (token.kind == AssemblerToken.EOL) accept();
		
		switch (token.kind) {
		case AssemblerToken.INTEGER_LITERAL:
			accept(); 
		case AssemblerToken.OPERATION:
			accept();
			index += Sizes.INTVAL_LEN * 2;
			break;
		case AssemblerToken.LABEL:
			labels.put(token.value, index);
			accept();
			break;
		case AssemblerToken.EOF:
			break;					
		default:
			throw new AssemblerSyntaxErrorException("'" + AssemblerToken.spell(token.kind) + "'not expected.");
		}
		accept();
		
	}
		
	private void getSingleInstructionCode() throws AssemblerSyntaxErrorException {

		while (token.kind == AssemblerToken.EOL) accept();
		
		switch (token.kind) {
		case AssemblerToken.INTEGER_LITERAL:
			int value = Integer.parseInt(token.value); 			 			
			if (value != index) 
				throw new AssemblerSyntaxErrorException("Instruction nr '" + index + "' expected not: '" + token.value + "'");
 			accept();		 

		case AssemblerToken.OPERATION:
			getInstruction();
			index += Sizes.INTVAL_LEN * 2;
			break;
		
		case AssemblerToken.LABEL:
			accept();
			break;
			
		case AssemblerToken.EOF:
			break;			
		
		default:
			throw new AssemblerSyntaxErrorException("'" + AssemblerToken.spell(token.kind) + "'not expected.");
		}
				
	}	
	
	private void getInstruction() throws AssemblerSyntaxErrorException {
		
		switch (token.kind) {	 
		case AssemblerToken.OPERATION:
		    OpCodes opcode = OpCodes.getOpCode(token.value);
			if (opcode== null)
				throw new AssemblerSyntaxErrorException("'" + token.value + "' not recognized as opcode");
			bbuf.putInt(opcode.getCode());
			if ((token.value.charAt(0) == 'b') && (token.value.charAt(1) == 'r') && (token.value.charAt(2) == 'a')) {
				accept();
				getBranchParameter();
			} else {
				accept();
				getParameter();
			}
			break;			 
		default:
			throw new AssemblerSyntaxErrorException("'" + AssemblerToken.spell(token.kind) + "' not expected. Expected code instruction");
		}
		
		switch (token.kind) {
		case AssemblerToken.EOL:
			accept();
		case AssemblerToken.EOF:
		break;
		 default:
			 throw new AssemblerSyntaxErrorException("'" + AssemblerToken.spell(token.kind) + "' not expected.");
		}	
	}
	
	private void getBranchParameter() throws AssemblerSyntaxErrorException {		
		
		switch (token.kind) {	 
		case AssemblerToken.INTEGER_LITERAL:
			bbuf.putInt(Integer.parseInt(token.value));		
			break;			 
		
		case AssemblerToken.LABEL:
			if (!labels.containsKey(token.value))
				throw new AssemblerSyntaxErrorException("Label '" + token.value + "' not recognized");
			bbuf.putInt(labels.get(token.value));
			break;
		default:
			throw new AssemblerSyntaxErrorException("'" + AssemblerToken.spell(token.kind) + "'not expected. Expected branch parameter");
		}
		
		accept();		
	}

	private void getParameter() throws AssemblerSyntaxErrorException {

		switch (token.kind) {
		case AssemblerToken.INTEGER_LITERAL:
			bbuf.putInt(Integer.parseInt(token.value));
			accept();
			break; 
		case AssemblerToken.EOL:
		case AssemblerToken.EOF:
			bbuf.putInt(0);
			break;			 
		default:
			throw new AssemblerSyntaxErrorException("'" + AssemblerToken.spell(token.kind) + "'not expected.");
		}
			
	}	

	private final void accept() throws AssemblerSyntaxErrorException {
		token = lexer.nextToken();
	}

}
