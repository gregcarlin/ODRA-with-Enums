package odra.db.objects.data;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * This class represents a special kind of database objects created to satisfy
 * the need for representing XML namespaces and other XML-related tiddlywinks.
 * The main difference between annotated objects and normal SBA objects
 * is their special behaviour when they are dereferenced. Although all
 * annotated objects are complex objects, the result of the dereference operation 
 * is a result of dereferencing a special-purpose subobject $value.
 * Other subobjects (called 'annotations') are available for the bind()
 * and nested() operations, so in this case annotated objects
 * act almost as if they were complex objects.
 * 
 * @author raist
 */

public class DBAnnotatedObject extends DBObject {
	/**
	 * Creates a new 'wrapper' to an annotated object.
	 * The object must already exist. It can be created using
	 * a normal createComplexObject() operation.
	 * @param oid of the complex object.
	 */
	public DBAnnotatedObject(OID oid) throws DatabaseException {
		super(oid);
		
		if (ConfigDebug.ASSERTS) assert oid.isComplexObject();
	}

	/**
	 * Checks if the oid really points at an annotated object.
	 */
	public boolean isValid() throws DatabaseException {
		return getObjectKind().getKindAsInt() > DataObjectKind.LINK_OBJECT;
	}

	/***********************************
	 * initialization
	 * */

	/**
	 * Initializes an empty complex object by creating its sobobjects
	 * characteristic of annotated objects.
	 * @param a value of the object when it gets dereferenced
	 */
	public void initialize(String value) throws DatabaseException {
		store.createIntegerObject(Database.getNameIndex().addName(Names.namesstr[Names.KIND_ID]), oid, DataObjectKind.ANNOTATED_STRING_OBJECT);
		store.createStringObject(Database.getNameIndex().addName(Names.namesstr[Names.VALUE_ID]), oid, value, 0);
		store.createComplexObject(Database.getNameIndex().addName(Names.namesstr[Names.ANNOTATION_ID]), oid, 0);
	}
	
	/**
	 * @see DBAnnotatedObject#initialize(String)
	 */
	public void initialize(int value) throws DatabaseException {
		store.createIntegerObject(Database.getNameIndex().addName(Names.namesstr[Names.KIND_ID]), oid, DataObjectKind.ANNOTATED_INTEGER_OBJECT);
		store.createIntegerObject(Database.getNameIndex().addName(Names.namesstr[Names.VALUE_ID]), oid, value);
		store.createComplexObject(Database.getNameIndex().addName(Names.namesstr[Names.ANNOTATION_ID]), oid, 0);
	}
	
	/**
	 * @see DBAnnotatedObject#initialize(String)
	 */	
	public void initialize(double value) throws DatabaseException {
		store.createIntegerObject(Database.getNameIndex().addName(Names.namesstr[Names.KIND_ID]), oid, DataObjectKind.ANNOTATED_REAL_OBJECT);
		store.createDoubleObject(Database.getNameIndex().addName(Names.namesstr[Names.VALUE_ID]), oid, value);
		store.createComplexObject(Database.getNameIndex().addName(Names.namesstr[Names.ANNOTATION_ID]), oid, 0);
	}

	/**
	 * @see DBAnnotatedObject#initialize(String)
	 */
	public void initialize(boolean value) throws DatabaseException {
		store.createIntegerObject(Database.getNameIndex().addName(Names.namesstr[Names.KIND_ID]), oid, DataObjectKind.ANNOTATED_BOOLEAN_OBJECT);
		store.createBooleanObject(Database.getNameIndex().addName(Names.namesstr[Names.VALUE_ID]), oid, value);
		store.createComplexObject(Database.getNameIndex().addName(Names.namesstr[Names.ANNOTATION_ID]), oid, 0);	
	}
	
	/**
	 * @see DBAnnotatedObject#initialize(String)
	 */	
	public void initialize(OID value) throws DatabaseException {
		store.createIntegerObject(Database.getNameIndex().addName(Names.namesstr[Names.KIND_ID]), oid, DataObjectKind.ANNOTATED_REFERENCE_OBJECT);
		store.createReferenceObject(Database.getNameIndex().addName(Names.namesstr[Names.VALUE_ID]), oid, value);
		store.createComplexObject(Database.getNameIndex().addName(Names.namesstr[Names.ANNOTATION_ID]), oid, 0);		
	}

	/**
	 * @see DBAnnotatedObject#initialize(String)
	 */
	public void initialize() throws DatabaseException {
		store.createIntegerObject(Database.getNameIndex().addName(Names.namesstr[Names.KIND_ID]), oid, DataObjectKind.ANNOTATED_COMPLEX_OBJECT);
		store.createComplexObject(Database.getNameIndex().addName(Names.namesstr[Names.VALUE_ID]), oid, 0);
		store.createComplexObject(Database.getNameIndex().addName(Names.namesstr[Names.ANNOTATION_ID]), oid, 0);		
	}	
	
	/***********************************
	 * new annotations
	 * */

	/**
	 * Creates a new annotation being an integer object.
	 * @param name name of the annotation
	 * @param value value of the annotation
	 * @return OID of the annotation
	 */
	public OID addIntegerAnnotation(String name, int value) throws DatabaseException {
		return store.createIntegerObject(Database.getNameIndex().addName(name), findAggregatedAnnotation(name), value);
	}

	/**
	 * @see DBAnnotatedObject#addIntegerAnnotation(String, int)
	 */
	public OID addStringAnnotation(String name, String value) throws DatabaseException {
		return store.createStringObject(Database.getNameIndex().addName(name), findAggregatedAnnotation(name), value, 0);
	}

	/**
	 * @see DBAnnotatedObject#addIntegerAnnotation(String, int)
	 */
	public OID addRealAnnotation(String name, double value) throws DatabaseException {
		return store.createDoubleObject(Database.getNameIndex().addName(name), findAggregatedAnnotation(name), value);
	}

	/**
	 * @see DBAnnotatedObject#addIntegerAnnotation(String, int)
	 */
	public OID addBooleanAnnotation(String name, boolean value) throws DatabaseException {
		return store.createBooleanObject(Database.getNameIndex().addName(name), findAggregatedAnnotation(name), value);
	}

	/**
	 * @see DBAnnotatedObject#addIntegerAnnotation(String, int)
	 */
	public OID addReferenceAnnotation(String name, OID value) throws DatabaseException {
		return store.createReferenceObject(Database.getNameIndex().addName(name), findAggregatedAnnotation(name), value);
	}

	public OID addPointerAnnotation(String name, OID value) throws DatabaseException {
		return store.createPointerObject(Database.getNameIndex().addName(name), findAggregatedAnnotation(name), value);		
	}

	/**
	 * @see DBAnnotatedObject#addIntegerAnnotation(String, int)
	 */
	public OID addComplexAnnotation(String name) throws DatabaseException {
		return store.createComplexObject(Database.getNameIndex().addName(name), findAggregatedAnnotation(name), 0);
	}

	/***********************************
	 * other operations
	 * */

	/**
	 * Finds an aggregate object holding annotations of the name given as a parameter.
	 * @param name name of the aggregate object
	 * @return OID of the aggregate object that has been found
	 */
	public OID getAggregatedAnnotationByName(String name) throws DatabaseException {
		return store.findFirstByNameId(store.getNameId(name), getAnnotationsRef());
	}

	/**
	 * Returns the list of all annotated objects. The OIDs returned are aggregate
	 * objects, so they must be dereferenced to get the real annotations.
	 * @return list of aggregate objects carrying annotations.
	 */
	public OID[] getAggregatedAnnotations() throws DatabaseException {
		return getAnnotationsRef().derefComplex();
	}
	
	/**
	 * Checks if an aggregated object of the name given already exists.
	 * If not, is is created.
	 * @param name name of the aggregate object
	 * @return OID of the aggregate object
	 */
	private final OID findAggregatedAnnotation(String name) throws DatabaseException {
		int nameid = Database.getNameIndex().addName(name);

		OID pnt = store.findFirstByNameId(nameid, getAnnotationsRef());

		return pnt == null ? getAnnotationsRef().createAggregateChild(Database.getNameIndex().addName(name), 0) : pnt;
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
