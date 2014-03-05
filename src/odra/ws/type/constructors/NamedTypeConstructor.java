package odra.ws.type.constructors;

import odra.db.DatabaseException;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MetabaseManager;

/** Represents named Odra type 
 * 
 * @since 2007-06-16
 * @version 2007-06-24
 * 
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class NamedTypeConstructor extends ForwardingTypeConstructor {

	protected NamedTypeConstructor() { }

	/* (non-Javadoc)
	 * @see odra.ws.type.constructors.ForwardingTypeConstructor#construct(odra.db.objects.data.DBModule)
	 */
	@Override
	public void construct(DBModule context) throws TypeConstructorException {
		super.construct(context);				
		new MetabaseManager(context).createMetaTypeDef(this.getName(), this.target.getName());					
	}
	

}
