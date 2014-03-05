package odra.ws.endpoints.wsdl;

import java.util.ArrayList;
import java.util.List;

import odra.db.DatabaseException;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBProcedure;
import odra.helpers.DatabaseHelper;
import odra.ws.endpoints.WSEndpointOptions;


/**
 * Assembles WSDL contract definitions.
 * 
 * @since 2006-12-12
 * @version 2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class ProcWSDLBuilder extends AbstractClassProcWSDLBuilder {

	private MBProcedure procedure = null;
	/** Sets object for which contract is built
	 * @param object
	 */
	public void setObjectContext(MBObject object) throws WSDLBuilderException {
		try {
			MBProcedure tmp = new MBProcedure(object.getOID());
			if (tmp.isValid()) {
				this.procedure = tmp;
			} else {
				throw new WSDLBuilderException("Not valid procedure object used. ");
			}
		} catch (DatabaseException ex) {
			throw new WSDLBuilderException("Internal database error. ", ex);
		}
	
	}
	

	/* (non-Javadoc)
	 * @see odra.ws.builders.WSDLBuilder#createDefinition(odra.ws.endpoints.WSEndpointOptions)
	 */
	@Override
	public String createDefinition(WSEndpointOptions options) throws WSDLBuilderException {
		if (this.procedure == null) {
			throw new WSDLBuilderException("Procedure need to be injected before first use. ");
		}
		
		// compile & link module (because we need to know return type for procedures)
		try {
			DBModule module = this.procedure.getModule();
			DatabaseHelper.buildModule(module);

			List<MBProcedure> list = new ArrayList<MBProcedure>();
			list.add(this.procedure);
			
			WSEndpointOptions newOptions;
			
			String serviceName = null;
			String portTypeName = null;
			String portName = null;
			try {
				boolean isServiceNameCorrect = options.getServiceName() != null && !options.getServiceName().trim().equals("");
				boolean isPortTypeCorrect = options.getPortTypeName() != null && !options.getPortTypeName().trim().equals("");
				boolean isPortCorrect = options.getPortName() != null && !options.getPortName().trim().equals("");
				if (!isServiceNameCorrect && !isPortTypeCorrect && !isPortCorrect) {
					serviceName = this.procedure.getName() + "Service";
					portTypeName = this.procedure.getName() + "Port";
					portName = this.procedure.getName() + "Service";
					newOptions = WSEndpointOptions.create(options.getEndpointName(), options.getRelativePath(), 
							portName, portTypeName, serviceName, 
							options.getState(), options.getTargetNamespace());
					
				} else if  (!isServiceNameCorrect || !isPortTypeCorrect || !isPortCorrect) {
					throw new WSDLBuilderException("Port and service need to be null both or none. ");
					
				} else {
					newOptions = options;
					
				}
					
				return this.createDefinition(newOptions, list);
				
			} catch (DatabaseException e) {
				throw new WSDLBuilderException("Unknown error while creating WSDL contract document", e);		
				
			}
			
		} catch (DatabaseException ex) {
			throw new WSDLBuilderException("Module need to be compiled before exposure but error occured while building it", ex);
			
		}
		
	}
	
	

}
