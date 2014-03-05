package odra.ws.bindings.soap;

import java.util.Hashtable;

import odra.ws.bindings.BindingInfo;


/**
 * Gathers specific SOAP 1.1 parameters
 * 
 * @since 2007-03-26
 * @version 2007-06-22
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class Soap11BindingInfo extends BindingInfo {

	private final static String soapActionKey = "soapAction";
	
	private String soapAction;

	/** Gets soap action parameter
	 * @return
	 */
	public String getSoapAction() {
		return this.soapAction;
	}

	/** Sets soap action parameter
	 * @param soapAction
	 */
	public void setSoapAction(String soapAction) {
		this.soapAction = soapAction;
	}
	
	/* (non-Javadoc)
	 * @see odra.bridges.bindings.BindingInfo#isValid()
	 */
	@Override 
	public boolean isValid() {
		return this.soapAction != null;
	}

	/* (non-Javadoc)
	 * @see odra.bridges.bindings.BindingInfo#getEntries()
	 */
	@Override
	public Hashtable<String, String> getEntries() {
		Hashtable<String, String> table = new Hashtable<String, String>();
		table.put(soapActionKey, this.soapAction);
		return table;
	}

	/* (non-Javadoc)
	 * @see odra.bridges.bindings.BindingInfo#load(java.util.Hashtable)
	 */
	@Override
	public void load(Hashtable<String, String> table) {
		if (table.containsKey("$"+soapActionKey)) {
			this.soapAction = table.get("$"+soapActionKey);
		} 
		
	}
}
