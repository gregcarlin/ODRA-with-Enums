package odra.ws.type.constructors;

import odra.db.objects.data.DBModule;

/** Represents primitive Odra type
 * 
 * @since 2007-06-16
 * @version 2007-06-24
 * 
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class PrimitiveTypeConstructor extends TypeConstructor {

	protected PrimitiveTypeConstructor() { }
	
	/* (non-Javadoc)
	 * @see odra.ws.type.constructors.TypeConstructor#construct(odra.db.objects.data.DBModule)
	 */
	@Override
	public void construct(DBModule context) throws TypeConstructorException {
		
		if (this.typeMapper.mapOdra(this.getName()) == null) {
			throw new UnknownPrimitiveTypeException("");
		}
	}
	
	

}
