package odra.sbql.ast.serializer;

/**
 * IASTDescriptor
 * codes of AST nodes 
 * used by AST serializator/deserializator
 * @author Radek Adamus
 *last modified: 2007-02-02
 *@version 1.0
 */
interface IASTDescriptor {
	public static final byte AS_EXPRESSION = 1;
	public static final byte ASSIGN_EXPRESSION = 2;
	public static final byte AVG_EXPRESSION = 3;
	public static final byte BAG_EXPRESSION = 4;
	public static final byte BLOCK_STATEMENT = 5;
	public static final byte BOOLEAN_EXPRESSION = 6;
	public static final byte COMMA_EXPRESSION = 7;
	public static final byte COUNT_EXPRESSION = 8;
	public static final byte DEREF_EXPRESSION = 9;
	public static final byte DOT_EXPRESSION = 10;
	public static final byte EMPTY_EXPRESSION = 11;
	public static final byte EMPTY_STATEMENT = 12;
	public static final byte EQUALITY_EXPRESSION = 13;
	public static final byte EXECSQL_EXPRESSION = 14;
	public static final byte EXISTS_EXPRESSION = 15;
	public static final byte EXPRESSION_STATEMENT = 16;
	public static final byte FORALL_EXPRESSION = 17;
	public static final byte FORSOME_EXPRESSION = 18;
	public static final byte FOREACH_STATEMENT = 19;
	public static final byte GROUPAS_EXPRESSION = 20;
	public static final byte IFELSE_STATEMENT = 21;
	public static final byte IF_STATEMENT = 22;
	public static final byte IN_EXPRESSION = 23;
	public static final byte INTEGER_EXPRESSION = 24;
	public static final byte INTERSECT_EXPRESSION = 25;
	public static final byte JOIN_EXPRESSION = 26;
	public static final byte MAX_EXPRESSION = 27;
	public static final byte MIN_EXPRESSION = 28;
	public static final byte MINUS_EXPRESSION = 29;
	public static final byte NAME_EXPRESSION = 30;
	public static final byte ORDERBY_EXPRESSION = 31;
	public static final byte PROCCALL_EXPRESSION = 32;
	public static final byte RANGE_EXPRESSION = 33;
	public static final byte REAL_EXPRESSION = 34;
	public static final byte REF_EXPRESSION = 35;
	public static final byte RETURN_WITHOUT_VALUE_STATEMENT = 36;
	public static final byte RETURN_WITH_VALUE_STATEMENT = 37;
	public static final byte SEQUENTIAL_STATEMENT = 38;
	public static final byte SEQUENTIAL_EXPRESSION = 39;
	public static final byte SIMPLE_BINARY_EXPRESSION = 40;
	public static final byte SIMPLE_UNARY_EXPRESSION = 41;
	public static final byte STRING_EXPRESSION = 42;
	public static final byte STRUCT_EXPRESSION = 43;
	public static final byte SUM_EXPRESSION = 44;
	public static final byte TO_BAG_EXPRESSION = 45;
	public static final byte TO_BOOLEAN_EXPRESSION = 46;
	public static final byte TO_INTEGER_EXPRESSION = 47;
	public static final byte TO_REAL_EXPRESSION = 48;
	public static final byte TO_SINGLE_EXPRESSION = 49;
	public static final byte TO_STRING_EXPRESSION = 50;
	public static final byte UNION_EXPRESSION = 51;
	public static final byte UNIQUE_EXPRESSION = 52;
	public static final byte VARIABLE_DECLARATION_STATEMENT = 53;
	public static final byte WHERE_EXPRESSION = 54;
	public static final byte IF_THEN_ELSE_EXPRESSION = 55;
	public static final byte IF_THEN_EXPRESSION = 56;
	public static final byte CREATE_EXPRESSION = 57;
	public static final byte CREATE_PERMANENT_EXPRESSION = 58;
	public static final byte CREATE_TEMPORAL_EXPRESSION = 59;
	public static final byte CREATE_LOCAL_EXPRESSION = 60;
	public static final byte WHILE_STATEMENT = 61;
	public static final byte DO_WHILE_STATEMENT = 62;
	public static final byte FOR_STATEMENT = 63;
	public static final byte INSERT_EXPRESSION = 64;
	public static final byte BREAK_STATEMENT = 65;
	public static final byte CONTINUE_STATEMENT = 66;
	public static final byte DATE_EXPRESSION = 67;
	public static final byte DATEPREC_EXPRESSION = 68;
	public static final byte TO_DATE_EXPRESSION = 69;
	public static final byte RANDOM_EXPRESSION = 70;
	public static final byte INSTANCEOF_EXPRESSION = 71;
	public static final byte CAST_EXPRESSION = 72;
	public static final byte CLOSE_BY_EXPRESSION = 73;
	public static final byte CLOSE_UNIQUE_BY_EXPRESSION = 74;
	public static final byte LEAVES_BY_EXPRESSION = 75;
	public static final byte LEAVES_UNIQUE_BY_EXPRESSION = 76;
	public static final byte DELETE_EXPRESSION = 77;
	public static final byte INSERT_COPY_EXPRESSION = 78;
	public static final byte EXTERNAL_PROCCALL_EXPRESSION = 79; //TW
	public static final byte EXTERNAL_NAME_EXPRESSION = 80; //TW	
//	public static final byte MATCH_STRING_EXPRESSION = 81;
	public static final byte REMOTE_QUERY_EXPRESSION = 82;
	public static final byte ATMOST_EXPRESSION = 83;
	public static final byte TRY_CATCH_FINALLY_STATEMENT = 84;
	public static final byte THROW_STATEMENT = 86;
	public static final byte PARALLEL_UNION_EXPRESSION = 87;
	public static final byte ATLEAST_EXPRESSION = 88;
	public static final byte ABORT_STATEMENT = 89;
	public static final byte RANGE_AS_EXPRESSION = 90;
	public static final byte SERIALIZE_OID_EXPRESSION = 91;
	public static final byte DESERIALIZE_OID_EXPRESSION = 92;
	public static final byte RENAME_EXPRESSION = 93;
	
}
