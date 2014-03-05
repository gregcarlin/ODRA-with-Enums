package odra.filters;

import odra.db.DatabaseException;
import odra.db.OID;

/** 
 * This interface defines abstract behavior of data importing filters. 
 * 
 * @author Krzysztof Kaczmarski
 *
 */
public interface DataImporter {	
	
	/**
	 * Abstract method used by plugins system.
	 * This method is called when "load XXXX using PLUGIN_NAME(PARAMS)"
	 * is executed in Cli.
	 * 
	 * @param modname destination of import
	 * @param data must contain data to be interpreted
	 * @param params params to be parsed by importer
	 * @throws FilterException
	 */
	public void importData(String modname, String data, String params) throws FilterException;

	/**
	 * Imports into a given object. Source of import is given in specific ImportFilter constructor, which is depending
	 * on concrete implementation. This method may be called only once after filter creation. 
	 * 
	 * @param parent object inside which objects will be imported
	 * @return returns OID of a newly created object 
	 * @throws DatabaseException
	 * @throws FilterException
	 * @throws ShadowObjectException 
	 */
	OID [] importInto(OID parent) throws DatabaseException, FilterException, ShadowObjectException;
}

