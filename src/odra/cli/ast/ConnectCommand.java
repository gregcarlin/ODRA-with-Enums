package odra.cli.ast;

import odra.cli.parser.CLIASTVisitor;

public class ConnectCommand extends Command {
	public DatabaseURL url;
	
	public ConnectCommand(DatabaseURL url) {
		this.url = url;
	}
	
	public Object accept(CLIASTVisitor vis, Object attr) throws Exception {
		return vis.visitConnectCommand(this, attr);
	}
}
