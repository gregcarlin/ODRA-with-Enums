package odra.sbql.emiter;

import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBProcedure;
import odra.system.config.ConfigServer;

/**
 * EmiterFactory - factory of Juliet code generators (currently 'safe' or 'dynamic')
 * 
 * @author Radek Adamus since: 2006-11-20 last modified: 08.06.07 - debug generators added
 * @version 1.0
 */
public class EmiterFactory {

	public static IJulietCodeGenerator getJulietCodeGenerator(DBModule mod) {
		if (ConfigServer.TYPECHECKING) {
			return new SafeJulietCodeGenerator(mod);
		} 
		return new DynamicJulietCodeGenerator(mod);
		
	}
	public static IJulietCodeGenerator getMetaQueryJulietCodeGenerator(DBModule mod) {		 
		return new DynamicJulietCodeGenerator(mod);
		
	}
	public static IJulietCodeGenerator getProcedureJulietCodeGenerator(DBModule mod, MBProcedure proc) {
		if (ConfigServer.TYPECHECKING) {
			return new SafeJulietCodeGenerator(mod, proc);
		} 
		return new DynamicJulietCodeGenerator(mod, proc);
		
	}
}