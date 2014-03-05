package odra.sbql.emiter;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.emiter.exceptions.CompiletimeExceptionTable;

/** An interface to Juliet code generator
 * IJulietCodeGenerator
 * @author Radek Adamus
 *last modified: 2007-03-09
 *@version 1.0
 */
public interface IJulietCodeGenerator {
	void generate(ASTNode node) throws SBQLException;
	void generateWithDebug(ASTNode node) throws SBQLException;
	JulietCode getCode();
	ConstantPool getConstantPool();
	CompiletimeExceptionTable getCatchTable() throws SBQLException;
}
