package odra.db.schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
  * OdraViewSchema
  * Transfer object to convey the information about 
 * View in the store independent format
 * 
 * @author radamus
 */
public class OdraViewSchema extends OdraObjectSchema {

    private OdraVariableSchema virtualObject;

    private OdraProcedureSchema seed; // seed procedure description

    private List<OdraProcedureSchema> genericProcedures; // generic

    // procedures
    // descriptions

    private List<OdraViewSchema> subViews; // nested sub-views description

    private List<OdraVariableSchema> variables; // view variables

    /**
     * @param viewName -
     *                the managerial name of the view
     * @param seedprocedure -
     *                seed generator procedure
     */
    public OdraViewSchema(String viewName, OdraProcedureSchema seedprocedure) {
	super(viewName);
	this.seed = seedprocedure;
	this.subViews = new Vector<OdraViewSchema>();
	this.genericProcedures = new Vector<OdraProcedureSchema>();
	this.variables = new Vector<OdraVariableSchema>();
    }

    /**
     * @param virtualObject
     *                schema description to set
     */
    public void setVirtualObject(OdraVariableSchema virtualObject) {
	this.virtualObject = virtualObject;
    }

    /**
     * @return the virtualObject
     */
    public OdraVariableSchema getVirtualObject() {
	return virtualObject;
    }

    private void addVariable(OdraVariableSchema var) {
	this.variables.add(var);
    }

    /**
     * adds sub-view description
     * 
     * @param sview -
     *                sub-view description
     */
    public OdraVariableSchema[] geVariables() {
	return variables.toArray(new OdraVariableSchema[variables.size()]);
    }

    /**
     * returns the name of the virtual object
     */
    public String getVirtualObjectName() {
	assert this.virtualObject != null : "virtual object must be set";
	return this.virtualObject.getName();
    }

    /**
     * adds the generic procedure to the view schema
     * 
     * @param genericProcedure -
     *                generic view procedure description
     */
    public void addGenericProcedure(OdraProcedureSchema genericProcedure) {
	this.genericProcedures.add(genericProcedure);
    }

    /**
     * gets the generic procedure info from the view schema
     * 
     * @return genericProcedure - generic view procedure description
     */
    public OdraProcedureSchema getGenericProcedure(GenericNames name) {
	for (OdraProcedureSchema proc : genericProcedures) {
	    if (proc.getPname().compareTo(name.toString()) == 0)
		return proc;
	}
	return null;
    }

    /**
     * adds sub-view description
     * 
     * @param sview -
     *                sub-view description
     */
    private void addSubView(OdraViewSchema sview) {
	subViews.add(sview);
    }

    /**
     * adds sub-view description
     * 
     * @param sview -
     *                sub-view description
     */
    public OdraViewSchema[] getSubViews() {
	return subViews.toArray(new OdraViewSchema[subViews.size()]);
    }

    /**
     * @return the virtualObject
     */
    public OdraProcedureSchema getSeed() {
	return seed;
    }

    /**
     * @param vName
     *                the view name to set
     */
    public void setVName(String vName) {
	this.setName(vName);
    }

    /**
     * @return the genericProcedures
     */
    public OdraProcedureSchema[] getGenericProcedures() {
	return genericProcedures.toArray(new OdraProcedureSchema[genericProcedures.size()]);
    }

    /**
     * @return the virtual object type name
     */
    public String getVirtualObjectTypeName() {
	assert this.virtualObject != null : "virtual object must be set first";
	return this.virtualObject.getTName();
    }

    /**
     * @return the vName
     */
    public String getViewName() {
	return getName();
    }

    public final static String SEED_PROCEDURE_NAME = "seed";

    public final static String DEFAULT_PARAM_NAME = "value";

    public static enum GenericNames {
	ON_RETRIEVE_NAME("on_retrieve"), ON_UPDATE_NAME("on_update"), ON_NEW_NAME(
		"on_new"), ON_DELETE_NAME("on_delete"), ON_NAVIGATE_NAME(
		"on_navigate");

	private String internalName;

	private GenericNames(String internalName) {
	    this.internalName = internalName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	    return this.internalName;
	}

	public static GenericNames getGenericNameForString(String name) {
	    return kinds.get(name);
	}

	private final static Map<String, GenericNames> kinds = new HashMap<String, GenericNames>();

	static {
	    for (GenericNames kind : GenericNames.values()) {
		kinds.put(kind.internalName, kind);
	    }
	}

    }

    /**
     * @param accept
     */
    public void addViewField(OdraObjectSchema objInfo) {
	if (objInfo instanceof OdraVariableSchema) {
	    addVariable((OdraVariableSchema) objInfo);
	} else if (objInfo instanceof OdraViewSchema) {
	    addSubView((OdraViewSchema) objInfo);
	}

    }

}
