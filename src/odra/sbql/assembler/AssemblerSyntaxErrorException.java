package odra.sbql.assembler;

import odra.exceptions.OdraException;

/**
 * This exception class is used to signal syntax errors detected
 * during parsing of assembler.
 * @author raist, tkowals
 *
 */

public class AssemblerSyntaxErrorException extends OdraException {

	public AssemblerSyntaxErrorException(String msg) {
		super(msg);
	}
}
