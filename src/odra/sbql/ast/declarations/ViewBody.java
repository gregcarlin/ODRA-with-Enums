package odra.sbql.ast.declarations;

import java.util.Hashtable;
import java.util.Map;

import odra.db.schema.OdraViewSchema;
import odra.sbql.ast.ParserException;
import odra.sbql.ast.terminals.Name;

/**
 * @author ksmialowicz
 * modifications radamus
 * 
 */
public class ViewBody {

    	private VariableDeclaration virtualObject = null;
	private ProcedureDeclaration seedProcedure = null;
	private Map<OdraViewSchema.GenericNames, ProcedureDeclaration> genericProcs  = new Hashtable<OdraViewSchema.GenericNames, ProcedureDeclaration>();
	
	private FieldDeclaration fields;

	/**
	 * Empty body of view
	 */
	public ViewBody() {
	    fields = new EmptyFieldDeclaration();
	}

	public ViewBody(ViewBodySection[] vbs) throws ParserException {
	    	this();
		for (ViewBodySection section : vbs) {
			section.putSelfInSection(this);
		}
	}
	
	
	/**
	 * @param virtualObject
	 * @param seedProcedure
	 * @param genericProcs
	 * @param fields
	 */
	public ViewBody(VariableDeclaration virtualObject,
		ProcedureDeclaration seedProcedure,
		ProcedureDeclaration[] genericProcs,
		FieldDeclaration fields) {
	    this.virtualObject = virtualObject;
	    this.seedProcedure = seedProcedure;
	    
	    for(ProcedureDeclaration pd : genericProcs){
		assert OdraViewSchema.GenericNames.getGenericNameForString(pd.getName()) != null :"must be generic operator name";
		this.genericProcs.put(OdraViewSchema.GenericNames.getGenericNameForString(pd.getName()), pd);
	    }
	    
	    this.fields = fields;
	}

	public void addSubview(SubviewViewBodySection section) throws ParserException {
		this.addField(new ViewFieldDeclaration(section.vd));
	}

	public void addVirtualObjectDeclaration(VirtualObjectsDeclarationViewBodySection section) throws ParserException {
	    if (this.virtualObject == null) {
		this.virtualObject = section.getVirtualObjectDeclaration();
	} else {
		throw new ParserException("Multiple virtual objects declared.");
	}
	}
	
	
	public void addSeedProcedure(SeedProcedureViewBodySection vObj)
			throws ParserException {

		if (seedProcedure == null) {
			seedProcedure = new ProcedureDeclaration(vObj.getName(), new EmptyArgumentDeclaration(), vObj.getResult(),vObj.getStatement());
		} else {
			throw new ParserException("Multiple seed declared.");
		}
	}
	
	public void addVariableDeclaration(VariableDeclarationViewBodySection vds)
	{	    
	    this.addField(new VariableFieldDeclaration(vds.getDeclaration()));
	    
	}

	public void addOnRetrieve(OnRetrieveViewBodySection section)
			throws ParserException {
	    	if(this.virtualObject == null){
	    	throw new ParserException("Virtual objects must be declared before any generic method declaration");
	    	}
		if (genericProcs.get(OdraViewSchema.GenericNames.ON_RETRIEVE_NAME) == null) {
			genericProcs.put(OdraViewSchema.GenericNames.ON_RETRIEVE_NAME, new ProcedureDeclaration(new Name(OdraViewSchema.GenericNames.ON_RETRIEVE_NAME.toString()), new EmptyArgumentDeclaration(), procedureResult(this.virtualObject), section.getStatement()));
		} else {
			throw new ParserException("Multiple on_retrieve declared.");
		}

	}

	public void addOnUpdate(OnUpdateViewBodySection section) throws ParserException {
	    	if(this.virtualObject == null){
	    	    throw new ParserException("Virtual objects must be declared before any generic method declaration");
	    	}
		if (genericProcs.get(OdraViewSchema.GenericNames.ON_UPDATE_NAME) == null) {
		    genericProcs.put(OdraViewSchema.GenericNames.ON_UPDATE_NAME, new ProcedureDeclaration(new Name(OdraViewSchema.GenericNames.ON_UPDATE_NAME.toString()), genericArgumentDeclaration(section.getParamName(), this.virtualObject, 1,1), new ProcedureResult(), section.getStatement()));
		} else {
			throw new ParserException("Multiple on_update declared.");
		}

	}

	public void addOnNew(OnNewViewBodySection section) throws ParserException {
	    if(this.virtualObject == null){
	    	    throw new ParserException("Virtual objects must be declared before any generic method declaration");
	    	}
		if (genericProcs.get(OdraViewSchema.GenericNames.ON_NEW_NAME) == null) {
			genericProcs.put(OdraViewSchema.GenericNames.ON_NEW_NAME, new ProcedureDeclaration(new Name(OdraViewSchema.GenericNames.ON_NEW_NAME.toString()), genericArgumentDeclaration(section.getParamName(), this.virtualObject, 0, Integer.MAX_VALUE), new ProcedureResult(), section.getStatement()));
		} else {
			throw new ParserException("Multiple on_insert declared.");
		}

	}

	public void addOnDelete(OnDeleteViewBodySection section) throws ParserException {
		if (genericProcs.get(OdraViewSchema.GenericNames.ON_DELETE_NAME) == null) {
			genericProcs.put(OdraViewSchema.GenericNames.ON_DELETE_NAME, new ProcedureDeclaration(new Name(OdraViewSchema.GenericNames.ON_DELETE_NAME.toString()), new EmptyArgumentDeclaration(), new ProcedureResult(), section.getStatement()));
		} else {
			throw new ParserException("Multiple on_delete declared.");
		}

	}
	
	public void addOnNavigate(OnNavigateViewBodySection section) throws ParserException {
		
	    if(this.virtualObject == null){
	    	throw new ParserException("Virtual objects must be declared before any generic method declaration");
	    }
	    if (genericProcs.get(OdraViewSchema.GenericNames.ON_NAVIGATE_NAME) == null) {
			genericProcs.put(OdraViewSchema.GenericNames.ON_NAVIGATE_NAME, new ProcedureDeclaration(new Name(OdraViewSchema.GenericNames.ON_NAVIGATE_NAME.toString()), new EmptyArgumentDeclaration(), procedureResult(this.virtualObject), section.getStatement()));
		} else {
			throw new ParserException("Multiple on_navigate declared.");
		}

	}

	
	/**
	 * @return the virtualObjects
	 */
	public ProcedureDeclaration getSeedProcedure() {
	    return seedProcedure;
	}

	

	

	/**
	 * @return the virtualObject
	 */
	public VariableDeclaration getVirtualObjectDeclaration() {
	    return virtualObject;
	}

	public ProcedureDeclaration getGenericProcedure(OdraViewSchema.GenericNames name){
	    return this.genericProcs.get(name);
	}
	
	private void addField(SingleFieldDeclaration decl){
	    if(this.fields instanceof EmptyFieldDeclaration){
		this.fields = decl;
	    }else {
		this.fields = new SequentialFieldDeclaration(this.fields, decl);
	    }
	}


	/**
	 * @return the fields
	 */
	public SingleFieldDeclaration[] getFields() {
	    return fields.flattenFields();
	}
	
	public ProcedureDeclaration[] getGenericProcedures(){
	    return this.genericProcs.values().toArray(new ProcedureDeclaration[this.genericProcs.size()]);
	}
	
	private static ArgumentDeclaration genericArgumentDeclaration(Name paramName, VariableDeclaration virtualObjectDeclaration, int min, int max){
		return new SingleArgumentDeclaration(new VariableDeclaration(paramName, virtualObjectDeclaration.getType(), new CardinalityDeclaration(min,max), virtualObjectDeclaration.getReflevel()));
	 }
	    
	private static ProcedureResult procedureResult(VariableDeclaration virtualObjectDeclaration) {
		return new ProcedureResult(virtualObjectDeclaration.getType(),new CardinalityDeclaration(), virtualObjectDeclaration.getReflevel());
	}
}
