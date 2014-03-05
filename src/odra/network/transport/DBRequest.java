package odra.network.transport;

import odra.system.config.ConfigDebug;

/**
 * This class represents requests sent from clients to databases.
 * Each request consists of two parts: 1) code of operation
 * that should be executed by the server, 2) table of
 * parameters for the command.
 *
 * @author raist
 */

public class DBRequest {
	public byte opcode;
	public String[] params;

	public DBRequest(byte opcode, String[] params) {
		if (ConfigDebug.ASSERTS) assert params != null;

		this.opcode = opcode;
		this.params = params;
	}

	public final static byte DUMP_STORE_RQST = 1; // params: none
	public final static byte DUMP_MODULE_RQST = 2;	// params: module_name
	public final static byte LOGIN_RQST = 3; // params: user_name, password
	public final static byte ADD_USER_RQST = 4; // params: user_name, password
	public final static byte LIST_RQST = 5; // params: module_name
	public final static byte EXISTS_MODULE_RQST = 6; // params: module_name
	public final static byte ADD_MODULE_RQST = 7; // params: source, parent_module_name
	public final static byte REMOVE_MODULE_RQST = 8; // params: module_name
	public final static byte EXECUTE_SBQL_RQST = 9; // params: source, context_module_name
	public final static byte COMPILE_RQST = 10; // params: module_name
	public final static byte DISASSEMBLE_RQST = 11; // params: procedure_name, parent_module_name
	public final static byte DUMP_MEMORY_RQST = 12; // params: none
	public final static byte LOAD_DATA_RQST = 13; // params: module, path, filter, params
	public final static byte ADD_INDEX_RQST = 14; // params: index_name, [rangeParams], source;
	public final static byte ALTER_PROCEDURE_BODY_RQST = 15; // params: procedure body source
	public final static byte REMOTE_GLOBAL_BIND_RQST = 16; // params: schema_name, bound_name
	public final static byte REMOVE_INDEX_RQST = 17; // params: index_name;
	public final static byte ADD_LINK_RQST = 18; // params: link name, schema, password, host, port, parent_module_name
	public final static byte EXPLAIN_OPTIMIZATION_RQST = 19;
	public final static byte EXPLAIN_JULIETCODE_RQST = 20;
	public final static byte EXPLAIN_TYPECHECKER_RQST = 21;

	public final static byte SHOW_OPTIMIZATION_RQST = 22;
	public final static byte SET_OPTIMIZATION_RQST = 23;//params: optmization type names
	public final static byte ADD_ENDPOINT_RQST = 24; // params: endpoint name, exposed object, state(on|off), path, service name, port name, ns
	public final static byte REMOVE_ENDPOINT_RQST = 25;


	public final static byte REMOTE_PROCEDURE_CALL_RQST = 26;
	public final static byte GET_METADATA_RQST = 27;

	public final static byte ADD_PROXY_RQST = 28;
	public final static byte ADD_VIEW_RQST = 29;
	public final static byte REMOVE_VIEW_RQST = 30;
	public final static byte EXPLAIN_PROCEDURE_RQST = 31;
	public final static byte EXEC_META_RQST = 32;

	public final static byte IS_INTEGER_OBJECT_RQST = 50;
	public final static byte GET_NAME_RQST = 51;
	public final static byte UPDATE_INTEGER_OBJECT_RQST = 52;

	public final static byte DEREF_INTEGER_OBJECT_RQST = 53;
	public final static byte DEREF_STRING_OBJECT_RQST = 54;
	public final static byte DEREF_REFERENCE_OBJECT_RQST = 55;
	public final static byte DEREF_DOUBLE_OBJECT_RQST = 56;
	public final static byte DEREF_DATE_OBJECT_RQST = 57;
	public final static byte DEREF_BOOLEAN_OBJECT_RQST = 58;
	public final static byte UPDATE_STRING_OBJECT_RQST = 59;
	public final static byte UPDATE_REFERENCE_OBJECT_RQST = 60;
	public final static byte UPDATE_DOUBLE_OBJECT_RQST = 61;
	public final static byte UPDATE_BOOLEAN_OBJECT_RQST = 62;
	public final static byte UPDATE_BINARY_OBJECT_RQST = 63;
	public final static byte UPDATE_DATE_OBJECT_RQST = 64;
	public final static byte IS_AGGREGATE_OBJECT_RQST = 65;
	public final static byte IS_BINARY_OBJECT_RQST = 66;
	public final static byte IS_BOOLEAN_OBJECT_RQST = 67;
	public final static byte IS_COMPLEX_OBJECT_RQST = 68;
	public final static byte IS_DOUBLE_OBJECT_RQST = 69;
	public final static byte IS_REFERENCE_OBJECT_RQST = 70;
	public final static byte IS_STRING_OBJECT_RQST = 71;
	public final static byte IS_DATE_OBJECT_RQST = 72;

	public final static byte HEAP_STRUCTURE_INIT_RQST = 73;// params: persistent?
	public final static byte HEAP_STRUCTURE_FRAGMENT_DATA_RQST = 74;// params: start byte, length
	public final static byte HEAP_STRUCTURE_FRAGMENT_TYPES_RQST = 75;// params: start byte, length

	public final static byte ADD_MODULE_AS_WRAPPER_RQST = 76;// params: module name. wrapper server host, wrapper server port, parent module name

	public final static byte MEMORY_GET_RQST = 77;
	public final static byte MEMORY_GC_RQST = 78;

	public final static byte DEREF_COMPLEX_OBJECT_RQST = 80;
	public final static byte COUNT_CHILDREN_RQST = 81;
	public final static byte GET_CHILD_AT = 82;

	public final static byte REFRESH_LINK_RQST = 85;

	public final static byte EXECUTE_OCL_RQST = 90; // params: source, context_module_name

	public final static byte PARSE_ONLY_RQST = 91;
	public final static byte TYPECHECK_ONLY_RQST = 92;
	public final static byte OPTIMIZE_ONLY_RQST = 93;

	public final static byte SUSPEND_ENDPOINT_RQST = 94;
	public final static byte RESUME_ENDPOINT_RQST = 95;

	public final static byte WHATIS_RQST = 96;
	public final static byte EXISTS_RQST = 97; // params:

	public final static byte VALIDATE_METABASE_SERIAL_RQST = 98;  // params: module name, serial

	public final static byte ADD_TMPINDEX_RQST = 99; // params: index_name, [rangeParams], source;

	public final static byte EXECUTE_REMOTE_SBQL_RQST = 100;

	//currently not used
	public final static byte ADD_MODULE_AS_CMU_RQST = 101;// params: module name.
	public final static byte ADD_MODULE_AS_PU_RQST = 102;// params: module name.
	//

	public final static byte ADD_INTERFACE_RQST = 103; // params: source, parent_module_name
	public final static byte REMOVE_INTERFACE_RQST = 104; // params: interface_name
	public final static byte ASSIGN_INTERFACE_RQST = 105;  // params: interface_name, global_object_name, parmod
	public final static byte UNASSIGN_INTERFACE_RQST = 106; // params: interface_name, global_object_name, parmod
	public final static byte REMOVE_LINK_RQST = 107;

	public final static byte ADD_GRIDLINK_RQST = 108; // params: link name, schema, password, peername, parent_module_name
	public final static byte REMOVE_GRIDLINK_RQST = 109; // params: link name, parent_module_name
	public final static byte JOINTOGRID_RQST = 110; // params: schema, parent_module_name
	public final static byte REMOVEFROMGRID_RQST = 111; // params: schema, parent_module_name
	public final static byte ADD_ROLE_RQST = 112;
	public final static byte GRANT_PRIVILEGE_RQST = 113;

	//testing only
	public final static byte SET_CLIENT_NAME = 114;

	public final static byte EXECUTE_REMOTE_COMMAND_RQST = 115; // params: command, parent_module_name
	public final static byte CONNECTTOGRID_RQST = 116; // params: jxtagroupname, peername, password, cmuuri, parent_module_name

	public static final byte PROMOTE_TO_PROXY_RQST = 117; // params: name, wsdl url
	public static final byte ADD_LINK_TO_SCHEMA_RQST = 118; // params: linkname, schema
	public static final byte REMOVE_LINK_FROM_SCHEMA_RQST = 119; // params: linkname, schema

	public static final byte REMOVE_PROXY_RQST = 120; // params: proxy name, parent module name
	public static final byte DEFRAGMENT_RQST = 121; //


}
