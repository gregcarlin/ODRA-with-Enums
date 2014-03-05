package odra.ws.type.constructors;

import odra.db.objects.data.DBModule;
import odra.ws.type.mappers.ITypeMapper;
/** Responsible for creating and initialiing all kinds of @see odra.ws.type.constructors.TypeConstructor
 * 
 * @since 2007-06-22
 * @version 2007-06-24
 * 
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class TypeConstructorFactory extends TypeConstructor {
	
	
	/** Creates forwarding quasi type abstraction
	 * @return
	 */
	public static ForwardingTypeConstructor createForwardingTypeConstructor(ITypeMapper mapper) {
		ForwardingTypeConstructor tmp = new ForwardingTypeConstructor();
		tmp.setMapper(mapper);
		return tmp;
	}
	
	/** Creates module abstraction
	 * @return
	 */
	public static ModuleTypeConstructor createModuleTypeConstructor(ITypeMapper mapper) {
		ModuleTypeConstructor tmp = new ModuleTypeConstructor();
		tmp.setMapper(mapper);
		return tmp;
	}

	/** Creates named type abstraction
	 * @return
	 */
	public static NamedTypeConstructor createNamedTypeConstructor(ITypeMapper mapper) {
		NamedTypeConstructor tmp = new NamedTypeConstructor();
		tmp.setMapper(mapper);
		return tmp;
	}
	
	/** Creates primitive type abstraction
	 * @return
	 */
	public static PrimitiveTypeConstructor createPrimitiveTypeConstructor(ITypeMapper mapper) {
		PrimitiveTypeConstructor tmp = new PrimitiveTypeConstructor();
		tmp.setMapper(mapper);
		return tmp;
	}
	
	/** Creates record type abstraction
	 * @return
	 */
	public static RecordTypeConstructor createRecordTypeConstructor(ITypeMapper mapper) {
		RecordTypeConstructor tmp =  new RecordTypeConstructor();
		tmp.setMapper(mapper);
		return tmp;
	}

	/* (non-Javadoc)
	 * @see odra.ws.type.constructors.TypeConstructor#construct(odra.db.objects.data.DBModule)
	 */
	@Override
	public  void construct(DBModule context) throws TypeConstructorException {
		throw new TypeConstructorException("Cannot call this method. ");
		
	}
	
}
