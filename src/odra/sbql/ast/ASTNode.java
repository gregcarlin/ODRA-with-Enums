package odra.sbql.ast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import odra.sbql.SBQLException;
import odra.sbql.ast.terminals.Name;
import odra.sbql.ast.terminals.Operator;
import odra.system.config.ConfigDebug;
import odra.transactions.ITransactionCapabilities;
import odra.transactions.ast.IASTTransactionCapabilities;
import odra.transactions.ast.ITransactionCapableASTNode;
import odra.transactions.ast.TransactionCapableASTNode;

/**
 * ASTNode is a base class of all AST nodes.
 * 
 * @author raist, edek (transactions capable)
 */
public abstract class ASTNode implements Serializable, ITransactionCapableASTNode {

	public int line;

	public int column;
	
	private transient ArrayList<String> links_decoration;

	private final ITransactionCapableASTNode transASTNodeImpl;

	private ITransactionCapableASTNode transASTNodeParent;

	protected ASTNode(IASTTransactionCapabilities capsASTTransaction) {
		this.transASTNodeImpl = TransactionCapableASTNode.getInstance(this, capsASTTransaction);
		this.transASTNodeParent = this.transASTNodeImpl;
	}

	protected ASTNode() {
		this(null);
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		throw new ParserException("Accept unimplemented in '" + this.getClass().getCanonicalName() + "'", vis.getSourceModuleName(), this.line, this.column);
	}

	@Override
	public String toString() {
		if (this instanceof Operator) {
			return getClass().getCanonicalName() + ": " + ((Operator) this).spell();
		} else if (this instanceof Name) {
			return getClass().getCanonicalName() + ": " + ((Name) this).value();
		} else {
			AST2TextQueryDumper dumper = new AST2TextQueryDumper();
			try {
				dumper.dumpAllAST(this);
			//	this.accept(dumper, null);
				return getClass().getCanonicalName() + ": " + dumper.getQuery();
			} catch (Exception exc) {
				exc.printStackTrace();
				return "";
			}
		}
	}

	public final boolean isTransactionCapable() {
		return this.isTransactionCapableMainASTNode() || this.hasTransactionCapableParentASTNode();
	}

	public final ITransactionCapabilities getTransactionCapabilities() {
		this.checkTransactionCapableImplementation();
		return this.transASTNodeImpl.getTransactionCapabilities();
	}

	public final ASTNode getASTNodeContainer() {
		this.checkTransactionCapableImplementation();
		return this.transASTNodeImpl.getASTNodeContainer();
	}

	public final IASTTransactionCapabilities getASTTransactionCapabilities() {
		this.checkTransactionCapableImplementation();
		return this.transASTNodeImpl.getASTTransactionCapabilities();
	}

	public final boolean hasTransactionCapableParentASTNode() {
		return this.transASTNodeParent != null && !this.isTransactionCapableMainASTNode();
	}

	public final boolean isTransactionCapableMainASTNode() {
		return this.transASTNodeImpl != null && this.transASTNodeParent == this.transASTNodeImpl;
	}

	public final ITransactionCapableASTNode getTransactionCapableParentASTNode() {
		return this.transASTNodeParent;
	}

	public final void setTransactionCapableParentASTNode(ITransactionCapableASTNode parentNode) {
		this.transASTNodeParent = parentNode;
	}

	private final static String FEATURE_UNIMPLEMENTED = "Should never occur. The requested feature should be implemented by subclasses";

	public List<ITransactionCapableASTNode> getTransactionCapableChildrenASTNodes() {
		throw new ParserException(FEATURE_UNIMPLEMENTED);
	}

	private final void checkTransactionCapableImplementation() {
		if (ConfigDebug.ASSERTS) {
			assert this.isTransactionCapable() : ITransactionCapableASTNode.class + " implementation has not been set";
		}
	}
	
	public void addLinkDecoration(String link)
	{
		if( links_decoration == null )
			links_decoration = new ArrayList<String>();
		
			links_decoration.add(link);
	}
	
	public void addLinkDecoration(String []links)
	{
	    	assert links != null : "links != null";
		if( links_decoration == null )
			links_decoration = new ArrayList<String>();
		
		if (links.length > 0 )
				links_decoration.addAll( Arrays.asList(links) );
	}
	
	public String[] getLinksDecoration()
	{
		if ( links_decoration == null )
			return null;
		
		return links_decoration.toArray( new String[0] );
	}
	
	public int getLinksDecorationCount()
	{
		if ( links_decoration == null )
			return 0;
		return links_decoration.size();
	}
	
	
	
}