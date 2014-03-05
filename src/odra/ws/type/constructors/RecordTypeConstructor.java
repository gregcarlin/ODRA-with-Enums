package odra.ws.type.constructors;
import java.util.Vector;

import odra.db.DatabaseException;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MetabaseManager;

/** Represents record Odra type
 * 
 * @since 2007-06-16
 * @version 2007-06-24
 * 
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class RecordTypeConstructor extends TypeConstructor {
	private Vector<ParameterDeclaration> parameters = new Vector<ParameterDeclaration>();

	public RecordTypeConstructor() { }
	
	/** Adds field parameter
	 * @param name field name
	 * @param minCard minimum occurence
	 * @param maxCard maximum occurence
	 * @param type field type
	 */
	public void addParameter(String name, int minCard, int maxCard, TypeConstructor type) {
		this.parameters.add(new ParameterDeclaration(name, minCard, maxCard, type));
	}
	
	/* (non-Javadoc)
	 * @see odra.ws.type.constructors.TypeConstructor#construct(odra.db.objects.data.DBModule)
	 */
	@Override
	public void construct(DBModule context) throws TypeConstructorException {
		for (ParameterDeclaration p : this.parameters) {
			p.getType().construct(context);
			
		}
		try {
			MBStruct record = new MBStruct(new MetabaseManager(context).createMetaStruct(0));
			// update name
			this.setName(record.getName());
					
			for (ParameterDeclaration p : this.parameters) {
				record.createField(p.getName(), p.getMinCard(), p.getMaxCard(), p.getTypeName(), 0);
			}
			
		} catch (DatabaseException ex) {
			throw new TypeConstructorException("Error while creating anonymous structure type. ", ex);

		}
			
	}
	
	private class ParameterDeclaration {
		private String name;
		private int minCard;
		private int maxCard;
		private TypeConstructor type;
		
		public final static int UNBOUNDED = -1;
				
		public ParameterDeclaration(String name, int minCard, int maxCard, TypeConstructor type) {
			this.name = name;
			this.minCard = minCard;
			this.maxCard = maxCard;
			this.type = type;
		}
		
		public String getName() {
			return this.name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public TypeConstructor getType() {
			return this.type;
		}
		public void setType(TypeConstructor type) {
			this.type = type;
		}
		
		public String getTypeName() {
			return this.type.getName();
		}

		public int getMaxCard() {
			if (this.maxCard == UNBOUNDED) {
				return Integer.MAX_VALUE;
			} else {
				return this.maxCard;
			}
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
		
		
	}
	
	
}
