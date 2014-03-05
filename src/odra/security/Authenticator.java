package odra.security;

import odra.db.*;

public class Authenticator {
	public static UserContext userPasswordAuthentication(String user, String password) throws AuthenticationException, DatabaseException {
		if (!AccountManager.hasUserAccount(user, password))
			throw new AuthenticationException("Access denied: Invalid user name or password");

		return new UserContext(user, user);
	}
	
	/**
	 * @param user
	 * @param password
	 * @param rolename
	 * @return
	 * @throws AuthenticationException
	 * @throws DatabaseException
	 */
	public static UserContext userPasswordAuthentication(String user, String password, String rolename) throws AuthenticationException, DatabaseException {
		if (!AccountManager.hasUserAccount(user, password, rolename))
			throw new AuthenticationException("Access denied: Invalid user name or password");

		return new UserContext(user, user, rolename);
	}
}
