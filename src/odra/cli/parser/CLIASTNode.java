package odra.cli.parser;

public abstract class CLIASTNode {
	public abstract Object accept(CLIASTVisitor vis, Object attr) throws Exception;
}
