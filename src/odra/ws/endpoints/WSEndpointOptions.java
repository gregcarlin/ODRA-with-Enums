package odra.ws.endpoints;

import odra.db.DatabaseException;
import odra.db.objects.data.DBEndpoint;


/**
 * Read only endpoint options decorator.
 *  
 * @since 2007-03-19
 * @version 2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */

public class WSEndpointOptions  {

	public static final String STATE = "state";
	public static final String RELATIVE_PATH = "relativePath";
	public static final String SERVICE_NAME = "serviceName";
	public static final String PORTTYPE_NAME = "portTypeName";
	public static final String PORT_NAME = "portName";
	public static final String TARGET_NAMESPACE = "targetNamespace";

	// options
	private String endpointName = null;
	private String relativePath = null;
	private String serviceName = null;
	private String portTypeName = null;
	private String portName = null;
	private EndpointState state = null;	
	private String targetNamespace = null;


	/** Load options from their database (persistent) representation
	 * @param dbEndpoint
	 * @throws DatabaseException
	 */
	public void load(DBEndpoint dbEndpoint) throws DatabaseException {
		this.endpointName = dbEndpoint.getName();
		this.relativePath = dbEndpoint.getRelativePath();
		this.serviceName = dbEndpoint.getServiceName();
		this.portTypeName = dbEndpoint.getPortTypeName();
		this.portName = dbEndpoint.getPortName();
		this.state = dbEndpoint.getState();
		this.targetNamespace = dbEndpoint.getTargetNamespace();
	}


	/**
	 * @return
	 */
	public boolean isValid() {
		return this.endpointName != null && this.relativePath != null && this.serviceName != null && this.portTypeName != null 
			&& this.state != EndpointState.UNKNOWN && this.targetNamespace != null;
	}


	public String getEndpointName() {
		return this.endpointName;
	}

	public String getPortTypeName() {
		return this.portTypeName;
	}

	public String getRelativePath() {
		return this.relativePath;
	}

	public String getServiceName() {
		return this.serviceName;
	}

	public EndpointState getState() {
		return this.state;
	}

	public String getTargetNamespace() {
		return this.targetNamespace;
	}
	
	public String getPortName() {
		return this.portName;
	}
	
	
	/** Creates read-only parameters representation
	 * @param endpointName
	 * @param relativePath
	 * @param serviceName
	 * @param portTypeName
	 * @param state
	 * @param targetNamespace
	 * @return
	 */
	public static WSEndpointOptions create(String endpointName, String relativePath, String portName, String portTypeName, 
			String serviceName, EndpointState state, String targetNamespace) {
		
		WSEndpointOptions opts = new WSEndpointOptions();
		opts.endpointName = endpointName;
		opts.relativePath = relativePath;
		opts.serviceName = serviceName;
		opts.portTypeName = portTypeName;
		opts.portName = portName;
		opts.state = state;
		opts.targetNamespace = targetNamespace;
		return opts;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WSEndpointOptions) {
			WSEndpointOptions tmp = (WSEndpointOptions) obj;
			if (tmp.endpointName == this.endpointName && 
				tmp.relativePath == this.relativePath &&
				tmp.serviceName == this.serviceName &&
				tmp.portTypeName == this.portTypeName &&
				tmp.portName == this.portName &&
				tmp.state == this.state &&
				tmp.targetNamespace == this.targetNamespace) {
				return true;
				
			} else {
				return false;
				
			}
			
		} else {
			return false;
			
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.endpointName.hashCode() ^ this.portTypeName.hashCode() ^ this.relativePath.hashCode() ^ this.portName.hashCode()
		 	^ this.serviceName.hashCode() ^ this.state.hashCode() ^ this.targetNamespace.hashCode();
	}

}
