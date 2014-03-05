package odra.wrapper.generator;

/**
 * XML schema elements. 
 * @author jacenty
 * @version   2006-12-03
 * @since   2006-05-20
 */
public enum Schema
{ 
	DATABASE ("database"), 
	TABLE ("table"), 
	COLUMN ("column"), 
	INDEX ("index"),
	INDEX_COLUMN ("index-column"),
	FOREIGN_KEY ("foreign-key"),
	FOREIGN_TABLE ("foreign-table"),
	NAME ("name"),
	NULLABLE ("nullable"),
	TYPE ("type"),
	SIZE ("size"),
	SCALE ("scale"),
	DEFAULT ("default"),
	DESCRIPTION ("description"),
	REFERENCE ("reference"),
	PAGES ("pages"),
	CARDINALITY ("cardinality"),
	FILTER_CONDITION ("filter-condition"),
	LOCAL ("local"),
	FOREIGN ("foreign"),
	UNIQUE ("unique"),
	BEST_ROW_ID ("best-row-id"),
	BEST_ROW_ID_COLUMN ("best-row-id-column");
	
	private final String name;
	
	Schema(String name)
	{
		this.name = name;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
};