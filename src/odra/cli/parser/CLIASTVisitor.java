package odra.cli.parser;

import odra.cli.ast.ConditionalCommand;
import odra.cli.ast.ConnectCommand;
import odra.cli.ast.SimpleCommand;

public interface CLIASTVisitor {
	public Object visitSimpleCommand(SimpleCommand vis, Object attr) throws Exception;
	public Object visitConnectCommand(ConnectCommand vis, Object attr) throws Exception;
	public Object visitConditionalCommand(ConditionalCommand vis, Object attr) throws Exception;
}
