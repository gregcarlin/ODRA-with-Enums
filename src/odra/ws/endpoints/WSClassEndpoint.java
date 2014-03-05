package odra.ws.endpoints;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBClass;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DataObjectKind;
import odra.db.objects.meta.MBClass;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBProcedure;
import odra.sbql.results.runtime.Result;
import odra.system.config.ConfigDebug;
import odra.ws.common.InitializationDef;
import odra.ws.common.ParamDef;
import odra.ws.type.mappers.TypeMapperException;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Web service endpoint implementation for classes
 *
 * @version 2007-06-23
 * @since 2007-06-22
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class WSClassEndpoint extends WSEndpoint {

	// class being exposed (database entry)
	private DBClass dbClass;
	// class being exposed (metabase entry)
	private MBClass mbClass;
	// module context (exposed class is direct subobject of it)
	private DBModule module;

	// for cacheing purposes
	private Hashtable<String, MBProcedure> methods = null;


	public WSClassEndpoint(DBModule mod, String name)
			throws WSEndpointException {
		try {
			this.module = mod;
			OID dbOID = mod.findFirstByName(name, mod.getDatabaseEntry());
			this.dbClass = new DBClass(dbOID);
			OID mbOID = mod.findFirstByName(name, mod.getMetabaseEntry());
			this.mbClass = new MBClass(mbOID);

			if (ConfigDebug.ASSERTS) {
				assert this.dbClass.getObjectKind().getKindAsInt() == DataObjectKind.CLASS_OBJECT;

			}

		} catch (DatabaseException ex) {
			LogRecord entry = new LogRecord(Level.SEVERE,
					"Error on endpoint creation. ");
			entry.setThrown(ex);
			entry.setSourceClassName(this.getClass().getName());

			throw new WSEndpointException("Error on endpoint initialization");
		}
	}


	/* (non-Javadoc)
	 * @see odra.bridges.endpoints.ws.WSEndpoint#getExposedMetaObject()
	 */
	@Override
	public MBObject getExposedMetaObject() {
		return this.mbClass;
	}

	/**  Lists all class methods
	 * @return list of available @see MBProcedure
	 * @throws DatabaseException
	 */
	private Hashtable<String, MBProcedure> getMethods()
			throws DatabaseException {
		if (this.methods == null) {
			this.methods = new Hashtable<String, MBProcedure>();
			for (OID oid : this.mbClass.getMethods()) {
				MBProcedure temp = new MBProcedure(oid);

				if (temp.isValid()) {
					String tempName = this.options.getTargetNamespace() + "/"
							+ temp.getName();
					this.methods.put(tempName, temp);
				}
			}
		}

		return this.methods;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see odra.bridges.endpoints.ws.WSEndpoint#handleMessage(org.w3c.dom.Document,
	 *      org.w3c.dom.Element)
	 */
	@Override
	protected DocumentFragment handlePayload(Document context, Element payload,
			Hashtable<String, Object> map) throws WSEndpointException {
		try {
			MBProcedure mbProc = this.getMethods().get(map.get(METHOD_TO_CALL));

			if (mbProc == null) {
				throw new WSEndpointException("Uknown operation. ");

			}

			ArrayList<ParamDef> params = new ArrayList<ParamDef>();

			// since there is no way to create new class instance in ad-hoc manner
			// we gather here all required instances for all classes involved in this methods/procedure call
			InitializationDef preInitialization = new InitializationDef();

			if (payload.getChildNodes().getLength() > 0) {
				String lastName = payload.getChildNodes().item(0).getNodeName();

				for (int i = 0, j = 0; i < payload.getChildNodes().getLength(); i++) {
					Node node = payload.getChildNodes().item(i);

					String name = node.getNodeName();
					if (!name.equals(lastName)) {
						j++;
					}
					lastName = name;

					String value = this.typeMapper.mapXMLToOdra(mbProc
							.getArguments()[j], node, preInitialization);


					ParamDef def = new ParamDef();
					def.setName(name);
					def.setValue(value);

					params.add(def);

				}
			}

			Result res = this.sbqlHelper.callMethod(this.mbClass, mbProc, params
					.toArray(new ParamDef[0]), preInitialization);

			DocumentFragment fragment = context.createDocumentFragment();

			Element root = context.createElementNS(this.getOptions().getTargetNamespace(), mbProc.getName() + "Response");

			Element result = context.createElementNS(this.getOptions().getTargetNamespace(), mbProc.getName()
					+ "ResponseResult");
			root.appendChild(result);

			String wrapWith = sbqlHelper.filterTypeName(mbProc.getType());
			NodeList nodes = this.typeMapper.mapOdraResultToXML(context, res, wrapWith,
					this.getOptions().getTargetNamespace());

			while (nodes.getLength() != 0) {
				result.appendChild(nodes.item(0));
			}

			fragment.appendChild(root);

			return fragment;

		} catch (TypeMapperException ex) {
			throw new WSEndpointException("Message handling error", ex);

		} catch (DatabaseException ex) {

			throw new WSEndpointException("Message handling error", ex);
		}

	}

}
