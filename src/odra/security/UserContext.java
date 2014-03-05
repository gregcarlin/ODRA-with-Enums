package odra.security;

public class UserContext {	
	private String user;
	private String module;
	private String rolename;
	public int accessMode = 0; 
	
	public UserContext(String user, String module) {
		this.user = user;
		this.module = module;
	}
	
	public UserContext(String user, String module, String rolename) {
		this.user = user;
		this.module = module;
		this.rolename = rolename;
	}
	
	public String getUserName() {
		return user;
	}

	public String getModule() {
		return module;
	}
	public String getRoleName() {
		return rolename;
	}
}
