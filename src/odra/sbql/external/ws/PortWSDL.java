package odra.sbql.external.ws;

import java.util.Iterator;
import java.util.List;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;

public class PortWSDL {
	private OperationWSDL[] operations;
	private String portName;
	private String soapAddress;
	
	public void setOperations(OperationWSDL[] operations) {
		this.operations = operations;
	}
	public OperationWSDL[] getOperations() {
		return operations;
	}

	
	public void setPortName(String portName) {
		this.portName = portName;
	}
	public String getPortName() {
		return portName;
	}
	
		public void setSoapAddress(String soapAddress) {
		this.soapAddress = soapAddress;
	}
	public String getSoapAddress() {
		return soapAddress;
	}
	
	public PortWSDL(PortType portType, Definition def, DocumentWSDL documentWSDL)
	{
		List ops = portType.getOperations();
		portName = portType.getQName().getLocalPart();
		
		operations = new OperationWSDL[ops.size()];
		Iterator opsIterator = ops.iterator();
		int i=0;
		while (opsIterator.hasNext())
        	{
    			Operation operation = (Operation)opsIterator.next();
    			OperationWSDL tmpOp = new OperationWSDL(operation, def, documentWSDL, this);
        		operations[i] = tmpOp;
        		i++;
        	}
	}
	
}
