package odra.helpers;

import odra.db.DatabaseException;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBObject;
import odra.sbql.builder.BuilderUtils;


/**
 * 
 * 
 * @since 2007-04-23
 * @version 2007-04-23
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */

public class DatabaseHelper {
	/** Gets meta object name without throwing database exception
	 * @param obj
	 */
	public static String getName(MBObject obj) {
		String name = null;
		try {
			name = obj.getName();
		} catch (DatabaseException ex) {
			name = "";
		}
		
		return name;
	}

	public static void buildModule(DBModule module) throws DatabaseException {
		BuilderUtils.getModuleLinker().linkModule(module);
		BuilderUtils.getModuleCompiler().compileModule(module);
			
	}
}
