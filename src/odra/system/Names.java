package odra.system;

/**
 * The class holds names and name_ids of different system database
 * objects. Can be used by all JODRA server classes and NameIndex.
 *
 * @author tkowals
 */

public class Names {

	// system names needed for store initialization
	public final static int NAMEINDEX_ID = 0;
	public final static int ID2NAME_ID = 1;
	public final static int NAME2ID_ID = 2;
	public final static int NAME_ID = 3;
	public final static int PROPERTIES_ID = 4;
	public final static int RECORDCOUNT_ID = 5;
	public final static int SPLITBUCKET_ID = 6;
	public final static int LEVELBUCKETS_ID = 7;
	public final static int BUCKETCAPACITY_ID = 8;
	public final static int PERSPLITLOAD_ID = 9;
	public final static int PERMERGELOAD_ID = 10;
	public final static int BUCKET_ID = 11;
	public final static int INDEXSTR_ID = 12;
	// system names
	public final static int NESTER_ID = 13;
	public final static int RMT_NESTER_ID = 14;
	public final static int KIND_ID = 15;
	public final static int VALUE_ID = 16;
	public final static int ANNOTATION_ID = 17;
	public final static int RECORDTYPE_ID = 18;
	public final static int RECORDTYPEID_ID = 19;
	public final static int DATAACCESS_ID = 20;
	public final static int DATAACCESSID_ID = 21;
	public final static int GENERAL_ID = 22;
	public final static int INDEXTYPE_ID = 23;
	public final static int MIN_CARD_ID = 24;
	public final static int MAX_CARD_ID = 25;
	public final static int BYTECODE_ID = 26;
	public final static int CNSTPOOL_ID = 27;

	public final static int S_SYSINDICES_ID = 28;
	public final static int S_SYSUSERS_ID = 29;
	public final static int S_PRVLINKS_ID = 30;

	public final static int S_ENDPOINTS_ID = 31;
	public final static int S_PROXIES_ID = 32;

	public final static int HASHSEED_ID = 33;
	public final static int LHRNUM_ID = 34;

	public final static int KEYTYPEID_ID = 35;
	public final static int KEYVALUES_ID = 36;
	public final static int OBLIGATORYID_ID = 37;
	public final static int LOCALHOST_LINK = 38;
	public final static int TYPEID_ID = 39;
	public final static int DEBUG_ID = 40;
	public final static int METHODS_ID = 41;
	public final static int SUPERCLASSES_ID = 42;
	public final static int INSTANCE_NAME_ID = 43;
	public final static int VIEW_REF_ID = 44;
	public final static int ARGUMENTS_ID = 45;
	public final static int REFERENCE_ID = 46;
	public final static int AST_ID = 47;
	public final static int FIELDS_ID = 48;
	public final static int LOCALS_ID = 49;
	public final static int CATCH_ID = 50;
	public final static int GENPROCS_ID = 51;
	public final static int VIRTFLDS_ID = 52;
	public final static int SUBVIEWS_ID = 53;
	public final static int VIRTOBJREF_ID = 54;
	public final static int EXTENDS_ID = 55;
	public final static int STRUCTURE_ID = 56;
	public final static int SUPER_ID = 57;
	public final static int HOST_ID = 58;
	public final static int PORT_ID = 59;
	public final static int SCHEMA_ID = 60;
	public final static int FLAGS_ID = 61;
	public final static int S_SYSROLES_ID = 62;
	public final static int SEED_ID = 63;
	public final static int REVERSEID_ID = 64;
	public final static int PROXIED_OBJECT = 65;
	public final static int WSDL_LOCATION = 66;
	public final static int SERVICE_ADDRESS = 67;
	public final static int NAMESPACE = 68;
	public final static int OPERATIONS = 69;
	public final static int BINDING_TYPE = 70;
	public final static int OPERATION = 71;
	public final static int PROC = 72;
	public final static int BINDING_INFO = 73;
	public final static int OPERATION_NAME = 74;
	public final static int EXPOSED_OBJECT = 75;
	public final static int STATE = 76;
	public final static int RELATIVE_PATH = 77;
	public final static int PORTTYPE_NAME = 78;
	public final static int PORT_NAME = 79;
	public final static int SERVICE_NAME = 80;
	public final static int TARGET_NAMESPACE = 81;

	public final static String[] namesstr = {

		"$nameidx",
		"$id2name",
		"$name2id",
		"$name",
		"$properties",
		"$recordcount",
		"$splitbucket",
		"$levelbuckets",
		"$bucketcapacity",
		"$persplitload",
		"$permergeload",
		"$bucket",
		"$indexstr",
		"$nester",
		"$rmtnester",
		"$kind",
		"$value",
		"$ann",
		"$recordtype",
		"$recordtypeid",
		"$dataaccess",
		"$dataaccessid",
		"$general",
		"$indextype",
		"$mincard",
		"$maxcard",
		"$bytecode",
		"$cnstpool",
		"$sysindices",
		"$sysusers",
		"$prvlinks",
		"$endpoints",
		"$proxies",
		"$hashseed",
		"$lhrnum",
		"$keytypeid",
		"$keyvalues",
		"$obligatory",
		"$localhostlink",
		"$typeid",
		"$debug",
		"$methods",
		"$supercls",
		"$instancename",
		"$viewref",
		"$arguments",
		"$reference",
		"$ast",
		"$fields",
		"$locals",
		"$catch",
		"$genprcs",
		"$virtflds",
		"$subviews",
		"$vobjprcref",
		"$extends",
		"$structure",
		"$super",
		"$host",
		"$port",
		"$schema",
		"$flags",
		"$sysroles",
		"$seed",
		"$reverseid",
		"$proxiedObject",
		"$wsdlLocation",
		"$serviceAddress",
		"$namespace",
		"$operations",
		"$bindingType",
		"$operation",
		"$proc",
		"$bindingInfo",
		"$opname",
		"$exposedObject",
		"$state",
		"$relativePath",
		"$portTypeName",
		"$portName",
		"$serviceName",
		"$targetNamesapace"
	};

}

