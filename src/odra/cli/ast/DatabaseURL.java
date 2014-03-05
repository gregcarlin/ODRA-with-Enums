package odra.cli.ast;

public class DatabaseURL {
	public String user, password, host;
	public int port;
	
	public DatabaseURL(String user, String password, String host, int port) {
		this.user = user;
		this.password = password;
		this.host = host;
		this.port = port;
	}
}
