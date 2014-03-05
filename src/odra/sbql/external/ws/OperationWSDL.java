package odra.sbql.external.ws;

import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Output;

public class OperationWSDL {
	private String opName;
	private MessageWSDL messageIn;
	private MessageWSDL messageOut;
	public PortWSDL port;

	public void setOpName(String opName) {
		this.opName = opName;
	}

	public String getOpName() {
		return opName;
	}

	public void setMessageIn(MessageWSDL messageIn) {
		this.messageIn = messageIn;
	}

	public MessageWSDL getMessageIn() {
		return messageIn;
	}

	public void setMessageOut(MessageWSDL messageOut) {
		this.messageOut = messageOut;
	}

	public MessageWSDL getMessageOut() {
		return messageOut;
	}
	
	public OperationWSDL(Operation op, Definition def, DocumentWSDL documentWSDL, PortWSDL portWSDL)
	{
		Input in = op.getInput();
		messageIn = new MessageWSDL(in.getMessage(), def);
		Output out = op.getOutput();
		messageOut = new MessageWSDL(out.getMessage(), def);
		opName = op.getName();		
		port = portWSDL;
		documentWSDL.operations.put(opName, this);
	}
	
}
