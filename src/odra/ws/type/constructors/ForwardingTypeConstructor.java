package odra.ws.type.constructors;

import odra.db.objects.data.DBModule;

/** Represents quasi type used for delayed linking purposes.
 * 
 * @since 2007-06-16
 * @version 2007-06-24
 * 
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class ForwardingTypeConstructor extends TypeConstructor {

	protected TypeConstructor target;
	
	protected ForwardingTypeConstructor() { }
	
	/** Gets destination type 
	 * @return
	 */
	public TypeConstructor getTarget() {
		return this.target;
	}



	/** Sets destination type 
	 * @param target
	 */
	public void setTarget(TypeConstructor target) {
		this.target = target;
	}


	
	/* (non-Javadoc)
	 * @see odra.ws.type.constructors.TypeConstructor#construct(odra.db.objects.data.DBModule)
	 */
	@Override
	public void construct(DBModule context) throws TypeConstructorException {
		if (this.target == null) {
			throw new TypeConstructorException("Target must be set before starting construction. ");
		}
		this.target.construct(context);
		
	}

}
