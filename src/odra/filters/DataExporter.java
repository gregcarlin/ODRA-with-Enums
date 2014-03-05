package odra.filters;

import odra.db.DatabaseException;
import odra.db.OID;

/** 
 * This interface defines abstract behavior of object exporting filters. 
 * 
 * @author Krzysztof Kaczmarski
 *
 */
public interface DataExporter{
 
	/**
	 * This method must initialize ExportFilter machine. Must be called exactly once.
	 * @param om ObjectManager (used only to retrieve kind of object from OID, to be removed)
	 */
	void initialize() throws FilterException;

	/**
	 * Exports given object. Destination of export is given in ExportFilter constructor, which is depending
	 * on concrete implementation. This method may be called many times after initialization. 
	 * 
	 * @param oid object to be exported
	 * @throws DatabaseException
	 * @throws FilterException
	 */
	void exportObject(OID oid) throws DatabaseException, FilterException;
	
	/**
	 * Finishes export procedure. Must be called exactly once at the end of export procedure.
	 * 
	 * @throws FilterException
	 */
	void finish() throws FilterException;

}
