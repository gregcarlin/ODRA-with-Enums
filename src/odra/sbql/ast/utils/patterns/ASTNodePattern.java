package odra.sbql.ast.utils.patterns;

import odra.sbql.ast.ASTNode;

public class ASTNodePattern implements Pattern {

	Class<? extends ASTNode> findType;	
	
	public ASTNodePattern(Class<? extends ASTNode> findType) {
		this.findType = findType;
	}
	
	public boolean matches(Object obj) {	
		return findType.isInstance(obj);
	}

}
