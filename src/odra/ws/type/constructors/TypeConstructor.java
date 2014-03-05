package odra.ws.type.constructors;
import odra.db.objects.data.DBModule;
import odra.ws.type.mappers.ITypeMapper;
import odra.ws.type.mappers.ITypeMapperAware;


/** Abstraction layer for Odra model types
 * 
 * @since 2007-06-16
 * @version 2007-06-24
 * 
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public abstract class TypeConstructor implements ITypeMapperAware {
	
	private String name = "_unknown_";
	private int minCard = 1;
	private int maxCard = 1;
	
	protected ITypeMapper typeMapper = null;
	
	public int getMaxCard() {
		return this.maxCard;
	}
	public void setMaxCard(int maxCard) {
		this.maxCard = maxCard;
	}
	public int getMinCard() {
		return this.minCard;
	}
	public void setMinCard(int minCard) {
		this.minCard = minCard;
	}
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	/** Materializes abstract type reprsentation within given context
	 * @param context Module to crete type in
	 * @throws TypeConstructorException
	 */
	public abstract void construct(DBModule context) throws TypeConstructorException;
		
	
	/* (non-Javadoc)
	 * @see odra.ws.type.mappers.ITypeMapperAware#setMapper(odra.ws.type.mappers.ITypeMapper)
	 */
	public void setMapper(ITypeMapper mapper) {
		this.typeMapper = mapper;
	}
	
	/* (non-Javadoc)
	 * @see odra.ws.type.mappers.ITypeMapperAware#getMapper()
	 */
	public ITypeMapper getMapper() {
		return this.typeMapper;
	}
	
	
}
