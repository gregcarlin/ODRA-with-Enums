package odra.sbql.ast.declarations;

import odra.sbql.ast.ParserException;

/**
 * @author ksmialowicz
 * 
 */
public class SubviewViewBodySection extends ViewBodySection {

	public ViewDeclaration vd;

	public SubviewViewBodySection(ViewDeclaration vd) {
		this.vd = vd;
	}

	@Override
	public void putSelfInSection(ViewBody vb) throws ParserException {
		vb.addSubview(this);

	}

}
