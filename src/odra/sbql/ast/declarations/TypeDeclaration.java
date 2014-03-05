package odra.sbql.ast.declarations;

public abstract class TypeDeclaration extends Declaration {	
	private String typeName = "<unknown>";
	/**
	 * @author stencel
	 * @category Used only by VIDE OCL parser. Should be ignored by others.
	 */
	public boolean VIDE_isCollection = false;
	/**
	 * @param typeName the typeName to set
	 */
	public void setTypeName(String typeName) {
	    this.typeName = typeName;
	}
	/**
	 * @return the typeName
	 */
	public String getTypeName() {
	    return typeName;
	}
}
