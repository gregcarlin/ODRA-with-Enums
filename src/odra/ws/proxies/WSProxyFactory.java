package odra.ws.proxies;

import org.apache.commons.lang.NotImplementedException;

import odra.db.DatabaseException;
import odra.db.objects.data.DBObject;
import odra.db.objects.data.DBProxy;
import odra.db.objects.data.DataObjectKind;
import odra.ws.facade.WSProxyException;
import odra.ws.type.mappers.ITypeMapper;
import odra.ws.type.mappers.literal.LiteralTypeMapper;

/**
 * Web service proxy factory
 *
 * @since 2007-06-20
 * @version 2008-01-26
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class WSProxyFactory {
	// currently it is fixed to literal, it may need to be determined through parameter if more mappers will be
	// introduced
	private static ITypeMapper literal;

	private WSProxyFactory()  {
	}

	static {
		literal = new LiteralTypeMapper();
	}

	/**
	 * Creates module based web service proxy
	 * @param dbProxy
	 * @return
	 */
	public static WSProxy createProxy(DBProxy dbProxy) throws WSProxyException {
		try {
			WSProxy result = null;
			DBObject object = new DBObject(dbProxy.getProxiedObject());
			switch (object.getObjectKind().getKindAsInt()) {
				case DataObjectKind.MODULE_OBJECT:
					result = new WSModuleProxy(dbProxy.getOID());
					break;
				case DataObjectKind.CLASS_OBJECT:
					result = new WSClassProxy(dbProxy.getOID());
					break;
				default:
					throw new NotImplementedException();
			}
			result.setMapper(literal);
			return result;

		} catch (DatabaseException ex) {
			throw new WSProxyException("Error while creating proxy object. ", ex);
		}
	}


}
