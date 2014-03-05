package odra.ws.proxies;

import java.net.URL;
import java.util.Hashtable;

import odra.db.DatabaseException;
import odra.db.objects.data.DBProxy;
import odra.ws.facade.WSBindingType;


/**
 * Read only proxy options decorator.
 *  
 * @since 2007-04-09
 * @version 2007-06-23
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */

public class WSProxyOptions  {

	public static final String SERVER_ADDRESS = "serviceAddress";
	
	// options
	private URL serviceAddress = null;
	private WSBindingType bindingType = null;
	private Hashtable<String, OperationInfo> operations = null;	
	private String ns = null;
	
	/** Loads options from their database representation
	 * @param dbProxy
	 * @throws DatabaseException
	 */
	public void load(DBProxy dbProxy) throws DatabaseException {
		this.serviceAddress = dbProxy.getServiceAddress();
		this.operations = dbProxy.getOperations();
		this.bindingType = dbProxy.getBindingType();
		this.ns  = dbProxy.getNamespace();
	}


	/**
	 * @return
	 */
	public boolean isValid() {
		return this.serviceAddress != null;
	}


	/**
	 * @return
	 */
	public URL getServiceAddress() {
		return this.serviceAddress;
	}

	/**
	 * @param procName
	 * @return
	 */
	public OperationInfo getOperationInfo(String procName) {
		if (this.operations.containsKey(procName)) {
			return this.operations.get(procName);
			
		} else {
			return null;
			
		}
	}
	
	/**
	 * @return
	 */
	public WSBindingType getBindingType() {
		return this.bindingType;
		
	}
	
	/**
	 * @return
	 */
	public String getNamespace() {
		return this.ns;
	}
	
	

}
