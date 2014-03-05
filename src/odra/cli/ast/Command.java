package odra.cli.ast;

import odra.cli.parser.CLIASTVisitor;

public abstract class Command {
	public abstract Object accept(CLIASTVisitor vis, Object attr) throws Exception;
}
