package odra.sbql.ast.declarations;

import odra.sbql.ast.ParserException;

/**
 * @author ksmialowicz
 * 
 */
public abstract class ViewBodySection {

	/**
	 * Add self to correct section of view
	 * 
	 * @param vd
	 *            view body where will be add
	 */
	public abstract void putSelfInSection(ViewBody vb) throws ParserException;
}
