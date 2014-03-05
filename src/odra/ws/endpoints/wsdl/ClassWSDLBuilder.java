package odra.ws.endpoints.wsdl;

import java.util.ArrayList;
import java.util.List;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBClass;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBProcedure;
import odra.helpers.DatabaseHelper;
import odra.ws.endpoints.WSEndpointOptions;


/**
 * Assembles WSDL contract definition for class object
 * 
 * @since 2007-06-22
 * @version 2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 * 
 */
public class ClassWSDLBuilder extends AbstractClassProcWSDLBuilder {


	private MBClass clas = null;

	/** Sets object for which contract will be built
	 * @param object
	 */
	public void setObjectContext(MBObject object) throws WSDLBuilderException {
		try {
			MBClass tmp = new MBClass(object.getOID());
			if (tmp.isValid()) {
				this.clas = tmp;
			} else {
				throw new WSDLBuilderException(
						"Not valid procedure object used. ");
			}
		} catch (DatabaseException ex) {
			throw new WSDLBuilderException("Internal database error. ", ex);
		}

	}

	/**
	 * Dispatcher Double dispatch pattern in order to keep generating wsdl
	 * contract in super class WSEndpoint
	 * 
	 * @param options
	 * @return
	 */
	@Override
	public String createDefinition(WSEndpointOptions options)
			throws WSDLBuilderException {
		if (this.clas == null) {
			throw new WSDLBuilderException(
					"Procedure need to be injected before first use. ");
		}

		// compile & link module (because we need to know return type for
		// procedures)
		try {
			DBModule module = this.clas.getModule();
			DatabaseHelper.buildModule(module);

			WSEndpointOptions newOptions;

			String serviceName = null;
			String portTypeName = null;

			boolean isServiceNameCorrect = options.getServiceName() != null
					&& !options.getServiceName().trim().equals("");
			boolean isPortTypeCorrect = options.getPortTypeName() != null
					&& !options.getPortTypeName().trim().equals("");
			boolean isPortCorrect = options.getPortName() != null
				&& !options.getPortName().trim().equals("");
			if (!isServiceNameCorrect && !isPortTypeCorrect && !isPortCorrect) {
				serviceName = this.clas.getName() + "Service";
				portTypeName = this.clas.getName() + "Port";
				newOptions = WSEndpointOptions.create(
						options.getEndpointName(), options.getRelativePath(), options.getPortName(),
						portTypeName, serviceName, options.getState(), options
								.getTargetNamespace());

			} else if (!isServiceNameCorrect || !isPortTypeCorrect || !isPortCorrect) {
				throw new WSDLBuilderException(
						"Port and service need to be null both or none.  ");

			} else {
				newOptions = options;

			}

			// create list with all methods
			List<MBProcedure> list = new ArrayList<MBProcedure>();

			for (OID oid : this.clas.getMethods()) {
				MBProcedure mbProc = new MBProcedure(oid);
				if (mbProc.isValid()) {
					list.add(mbProc);
				} else {
					throw new WSDLBuilderException(String.format(
							"Class {0} contains invalid methods. ", this.clas
									.getName()));
				}
			}

			return this.createDefinition(newOptions, list);

		} catch (DatabaseException ex) {
			throw new WSDLBuilderException(
					"Module need to be compiled before exposure but error occured while building it",
					ex);

		}

	}

	
}
