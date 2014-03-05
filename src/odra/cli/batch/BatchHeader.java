package odra.cli.batch;

/**
 * Batch header definitions.
 * 
 * @author jacenty
 * @version 2007-09-18
 * @since 2007-07-05
 */
enum BatchHeader
{
	ENCODING("encoding"),
	VERBOSE("verbose"),
	COMPACT("compact"),
	STOP_ON_ERROR("stop_on_error");
	
	private final String name;
	private BatchHeader(String name)
	{
		this.name = name;
	}
	
	static BatchHeader getBatchHeaderForName(String name)
	{
		for(BatchHeader header : values())
			if(header.name.equals(name))
				return header;
		
		throw new AssertionError("Undefined batch header: '" + name + "'...");
	}
	
	public String toString(){
	    return name;
	}
}