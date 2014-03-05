package odra.sbql.external.ws;

import javax.wsdl.Definition;
import javax.wsdl.Message;

public class MessageWSDL {

	private String name;

	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}

	
	public MessageWSDL (Message msg, Definition def)
	{
		name = msg.getQName().getLocalPart();
	}
	
}
