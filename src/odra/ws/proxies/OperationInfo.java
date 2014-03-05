package odra.ws.proxies;

import odra.ws.bindings.BindingInfo;


/**
 * Abstracts information chunk for remote operation
 * 
 * @since 2007-03-25
 * @version 2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class OperationInfo {

	private BindingInfo bindingInfo;
	private String name;

	/** Gets specific binding info connected with that operation
	 * @return
	 */
	public BindingInfo getBindingInfo() {
		return this.bindingInfo;
	}

	/** Sets specific binding info connected with that operation
	 * @param bindingInfo
	 */
	public void setBindingInfo(BindingInfo bindingInfo) {
		this.bindingInfo = bindingInfo;
	}

	/** Gets operation name
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/** Sets operation name
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	
	
	
	
}
