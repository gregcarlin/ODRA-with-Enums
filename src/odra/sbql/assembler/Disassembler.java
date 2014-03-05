package odra.sbql.assembler;

import java.nio.*;

import odra.sbql.emiter.OpCodes;

public class Disassembler {
	private byte[] code;
		
	public Disassembler(byte[] code) {
		this.code = code;
	}	
	
	public String decode() {		
		ByteBuffer buffer = ByteBuffer.wrap(code);
		
		StringBuffer str = new StringBuffer();
		
		while (buffer.hasRemaining()) {
			int pos = buffer.position();
			int oc = buffer.getInt();
			int par = buffer.getInt();
			OpCodes opcode = OpCodes.getOpCode(oc);
			str.append("\t").append(int2paddedstr(pos, 8)).append(" ").append(opcode.name());
			
			switch (opcode) {
				// instructions with a parameter
				case ldE:
				case ldSE:	
				case crRef:
				case crS:
				case crR:
				case crCpx:
				case crB:
				case crAgg:
				case crAggCard:
				case crI:
				case bind:
				case bindTop:
				case crAggI:
				case crAggS:
				case crAggB:
				case crAggR:
				case crAggRef:
				case crAggCpx:
				case crAggD:
				case crDyn:
				case bra:
				case braTrue:
				case braFalse:
				case ldcR:
				case ldcS:
				case ldI:
				case iinc:
				case idec:
				case as:
				case colAs:
				case grAs:
				case modPush:
				case fnd:
				case ifnSack:
				case enterBinder:
				case dynCast:
				case atmost:
				case atleast:
				case remoteAsyncQuery:
				case waitForAsync:
					str.append(" ").append(par);
					break;

				// instructions without a parameter
				case insPrt2:
				case insPrt3:
				case swap:
				case ldLE:
				case dup:
				case pop:
				case ldcR_0:
				case dsEnv:
				case crnEnv:
				case dsnEnv:
				case grI:
				case grR:
				case grS:
				case grEqI:
				case grEqR:
				case grEqS:
				case loI:
				case loR:
				case loS:					
				case loEqI:
				case loEqR:
				case loEqS:
				case eqI:
				case eqR:
				case eqB:
				case eqS:
				case eqRef:
				case eqStruct:
				case nEqI:
				case nEqR:
				case nEqB:
				case nEqS:
				case nEqRef:
				case nEqStruct:
				case dynNeg:
				case and:
				case or:
				case addI:
				case addR:
				case conS:
				case subI:
				case subR:
				case mulI:
				case mulR:
				case divI:
				case divR:
				case remI:
				case remR:
				case ldFalse:
				case ldTrue:
				case ldBag:
				case crCntr:					
				case incCntr:
				case extr:
				case cnt:
				case exist:
				case negI:
				case negR:
				case notB:
				case matchString:
				case notMatchString:
				case ldCntr:
				case endCntr:
				case crpd: 
				case fltn:
				case i2r:
				case r2i:
				case i2s:
				case r2s:
				case d2s:
				case b2s:
				case s2i:
				case s2r:
				case s2b:
				case derefI:
				case derefR:
				case derefB:
				case derefS:
				case derefCpx: 
				case derefRef:
				case derefColI:
				case derefColR:
				case derefColB:
				case derefColS:
				case derefColCpx: 
				case derefColRef:
				case dynDeref:
				case ret:
				case retv:
				case storeI:
				case storeB:
				case storeR:
				case storeS:
				case storeRef:
				case storeRevRef:
				case dynStore:
				case call:	
				case bswap2:
				case dup2:
				case dup_x1:
				case dup_x2:
				case in:
				case intersect:
				case diff:
				case unique:
				case dyn2r:
				case dyn2i:
				case dyn2s:
				case dyn2b:
				case orby:
				case union:
				case crvid:
				case rstCntr:
				case dynAdd:	
				case dynSub:
				case dynMul:
				case dynDiv:
				case dynRem:
				case dynGr:
				case dynLo:
				case dynGrEq:
				case dynLoEq:
				case dynEq:
				case dynNEq:
				case dynOr:
				case dynAnd:
				case dynNot:
				case del:
				case delChildren:
				case move:
				case moveCol:
				case max:
				case min:
				case avg:
				case insPrt:
				case rbdCntr:
				case single:
				case nonEmpty:
				case rng:
				case ref:
				case colRef:
				case dynRef:
				case modPop:
				case crLE:
				case initLoc:
				case parallelUnion:
					
					//
				case ifVirt:
				case ldvGenPrc:
				case retSub:
				case exception:
				case random:
				case randomObj:
				case crInstRef:
				case virtMove:
				case instof:
				case castClass:
				case reflName:
				case reflParent:
				case reflBind:
				case external:
				case execsql:
				case remoteQuery:
				case athrow:
				case commitStmt:
				case abortTrans:
				case beginTrans:
				case rename:
					break;
	
				//dates
				case crD:
				case eqD:
				case grEqD:
				case loD:
				case grD:
				case loEqD:
				case nEqD:
				case storeD:
				case derefD:
				case dyn2d:
				case s2d:
				case derefColD:
				case dateprec:
					break;
				case ldcD:
					str.append(" ").append(par);
					break;
				

				default:
					assert false : "Unknown opcode " + opcode + " (" + opcode.name() + ")";
			}
			
			if (buffer.hasRemaining())
				str.append(NEW_LINE);
		}

		return str.toString();
	}

	private String int2paddedstr(int val, int pos) {
		String str = "" + val;

		for (int i = str.length(); i < pos; i++)
			str = "0" + str;
		
		return str;
	}
	
	private final String NEW_LINE = System.getProperty("line.separator");
}
