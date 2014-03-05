package odra.filters;

import odra.db.DatabaseException;
import odra.db.OID;


/**
 * (It is just a utility class for the whole object exporting.)
 * 
 * Performs default procedure of example object export. It is independent from export method, 
 * which is passed by a concrete export filter.
 * 
 * @author Krzysztof Kaczmarski
 *
 */public class ObjectExporter{
	
	public static void doExport( DataExporter filter, OID oid ) throws DatabaseException, FilterException
	{
		filter.initialize();
		filter.exportObject( oid );
		filter.finish();
	}
}
