package odra.db.objects.meta;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * This class provides API for annotated object declarations stored in the metabase.
 * Annotated objects have been introduced to support XML namespaces.
 * They are not used from within SBQL. Only XML filters make use of them
 * (mainly to construct XML Schema equivalents in a jOdra database).
 * 
 * MBAnnotatedObjects are like MBVariableObjects. When they are dereferenced
 * they return a signature determined by the $value subobject. Apart from typical
 * values, annotated objects can also bear so called annotations. Annotations
 * make it possible to use the object as if the object had subobjects
 * (even though when the object is dereferenced it may yield a simple value).
 * Annotations are declared as typical MBVariableObjects.
 * 
 * @author raist
 */

// TODO: change the name to MBAnnotatedVariable

public class MBAnnotatedVariableObject extends MBObject {
	/**
	 * Initializes a new MBAnnotatedObject using the OID of an existing object.
	 * @param oid OID of an existing annotated object (or an empty complex object)
	 */
	public MBAnnotatedVariableObject(OID oid) throws DatabaseException {
		super(oid);
		
		if (ConfigDebug.ASSERTS) assert oid.isComplexObject() : "oid.isComplexObject() == true" +  oid.getObjectName() + " " + oid.getObjectKind().toString();
	}

	/**
	 * Initializes an empty complex object by creating its system-level sobobjects
	 * characteristic of annotated objects.
	 * @param mincard minimum cardinality
	 * @param maxcard maximum cardinality
	 * @param type name of type
	 * @param ref reference indicator (number of & in the type)
	 */
	public void initialize(int mincard, int maxcard, String type, int ref) throws DatabaseException {
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, MetaObjectKind.ANNOTATED_VARIABLE_OBJECT.kindAsInt());
		this.createMetaVariable(Names.namesstr[Names.VALUE_ID], mincard, maxcard, type, ref, oid);
		store.createComplexObject(store.addName(Names.namesstr[Names.ANNOTATION_ID]), oid, 0);
	}

	/**
	 * @return true if the object is really a procedure object
	 */
	public boolean isValid() throws DatabaseException {
		return getObjectKind() == MetaObjectKind.ANNOTATED_VARIABLE_OBJECT;
	}

	/**
	 * Creates a new annotation. Annotations are visible for SBQL queries
	 * as if they were subobjects.
	 * @param name name of the annotation
	 * @param mincard minimum cardinality
	 * @param maxcard maximum cardinality
	 * @param type name of the type
	 * @param ref reference indicator (number of &s)
	 * @return OID of the new MBVariableObject representing the new annotation
	 */
	public OID createAnnotation(String name, int mincard, int maxcard, String type, int ref) throws DatabaseException {
		return createMetaVariable(name, mincard, maxcard, type, ref, this.getAnnotationsRef());
	}

	/**
	 * @return OIDs of MBVariableObjects representing annotations
	 */
	public OID[] getAnnotations() throws DatabaseException {
		return getAnnotationsRef().derefComplex();
	}

	private OID createMetaVariable(String name, int mincard, int maxcard, String type, int ref, OID parent) throws DatabaseException {
	
		int nameid = store.addName(name);
		int typeid = getMetaBase().addMetaReference(type);

		OID strid = store.createComplexObject(nameid, parent, 0);
		new MBVariable(strid).initialize(typeid, mincard, maxcard, ref);

		return strid;
	}
	
	/***********************************
	 * debugging
	 * */
	
	public String dump(String indend) throws DatabaseException {
		DBModule module = getModule();
		
		MBVariable mbv = new MBVariable(getValueRef());
		
		String metastr = "\t" + oid.toString() + "\t\t" + indend + "#" + oid.getObjectNameId() + " (" + oid.getObjectName() + ")";
		metastr += " [" + this.getMinCard() + ".." + this.getMaxCard() + "]";
		metastr += " : &" + mbv.getRefIndicator() + " #" + mbv.getTypeNameId() + " (" + mbv.getTypeName() + ") [annotated variable]\n";

		OID[] args = getAnnotations();

		for (int i = 0; i < args.length; i++)
			metastr += new MBVariable(args[i]).dump(indend + " adn. " + i + ": ");

		return metastr;
	}

	/***********************************
	 * access to subobjects describing the object
	 * */
	
	public final OID getValueRef() throws DatabaseException {
		return oid.getChildAt(VALUE_POS);
	}

	private final OID getAnnotationsRef() throws DatabaseException {
		return oid.getChildAt(ANNOTATIONS_POS);
	}
	
	private final static int VALUE_POS = 1;
	private final static int ANNOTATIONS_POS = 2;	
}
