package odra.sbql.ast.statements;

import odra.sbql.ast.ASTNode;
import odra.sbql.debugger.compiletime.DebugNodeData;
import odra.sbql.debugger.compiletime.NoDebugNodeData;
import odra.sbql.emiter.JulietCode;

/**
 * Base class for all AST nodes being statements.
 * 
 * @author raist 
 * modifications: 28.12.06 radamus: flatten method added 08.06.07 radamus: node debug data added 08.06.07
 *         radamus: node Juliet code
 * 
 */

public abstract class Statement extends ASTNode {


	private DebugNodeData debug = NoDebugNodeData.NODEBUGDATA;

	private JulietCode julietCode;
	
	public Statement[] flatten() {
		return new Statement[] { this };
	}

	public final JulietCode getJulietCode() {
		return this.julietCode;
	}

	public final JulietCode setJulietCode(JulietCode julietCode) {
		return this.julietCode = julietCode;
	}

	/**
	 * @param debug the debug to set
	 */
	public void setDebug(DebugNodeData debug)
	{
	    this.debug = debug;
	}

	/**
	 * @return the debug
	 */
	public DebugNodeData getDebug()
	{
	    return debug;
	}
	
	
}