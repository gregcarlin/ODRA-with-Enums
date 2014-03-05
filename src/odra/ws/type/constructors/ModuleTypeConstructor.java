package odra.ws.type.constructors;
import java.util.Vector;

import odra.db.objects.data.DBModule;

/** Represets module Odra type.
 * 
 * @since 2007-06-16
 * @version 2007-06-24
 * 
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class ModuleTypeConstructor extends TypeConstructor {

	private Vector<TypeConstructor> types = new Vector<TypeConstructor>();

	protected ModuleTypeConstructor() {
	
	}
	
	/** Adds type to module
	 * @param type
	 */
	public void addType(TypeConstructor type) {
		this.types.add(type);
	}

	/* (non-Javadoc)
	 * @see odra.ws.type.constructors.TypeConstructor#construct(odra.db.objects.data.DBModule)
	 */
	@Override
	public void construct(DBModule context) throws TypeConstructorException {

		for (TypeConstructor t : this.types) {
			t.construct(context);
		}
			
	}
	
}
