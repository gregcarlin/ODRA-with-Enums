package odra.ws.type.mappers;


/** Implemented by all components, which need to use mappers internally.
 * 
 * @since 2007-06-20
 * @version  2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public interface ITypeMapperAware {
	/** Sets type napper
	 * @param mapper
	 */
	void setMapper(ITypeMapper mapper);
	
	/** Gets type mapper
	 * @return
	 */
	ITypeMapper getMapper();
}
