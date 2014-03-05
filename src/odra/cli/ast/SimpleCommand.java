package odra.cli.ast;

import odra.cli.parser.CLIASTVisitor;

public class SimpleCommand extends Command {
	public int cmdid;
	public String[] data;

	public SimpleCommand(int cmdid) {
		this.cmdid = cmdid;
		this.data = new String[0];
	}

	public SimpleCommand(int cmdid, String[] data) {
		this.cmdid = cmdid;
		this.data = data;
	}

	@Override
	public Object accept(CLIASTVisitor vis, @SuppressWarnings("unused") Object attr) throws Exception {
		return vis.visitSimpleCommand(this, null);
	}

	public final static int QUIT_CMD = 1;
	public final static int CONNECT_CMD = 2;
	public final static int DISCONNECT_CMD = 3;
	public final static int HELP_CMD = 4;
	public final static int CM_CMD = 5;
	public final static int LS_CMD = 6;
	public final static int PWM_CMD = 7;
	public final static int CMDOTDOT_CMD = 8;

	public final static int SBQL_CMD = 9;
	public final static int DUMP_STORE_CMD = 10;
	public final static int COMPILE_CMD = 11;

	public final static int ADD_MODULE_CMD = 12;
	public final static int REMOVE_MODULE_CMD = 13;
	public final static int DUMP_MODULE_CMD = 14;

	public final static int ALTER_PROCEDURE_BODY_CMD = 15;
	public final static int DISASSEMBLE_CMD = 16;

	public final static int ADD_USER_CMD = 17;

	public final static int DUMP_MEMORY_CMD = 18;

	public final static int LOAD_CMD = 19;

	public final static int ADD_INDEX_CMD = 20;

	public final static int SHOW_CMD = 21;
	public final static int SET_CMD = 22;

	public final static int ADD_LINK_CMD = 23;

	public final static int EXPLAIN_OPTIMIZATION_CMD = 24;
	public final static int EXPLAIN_JULIET_CODE_CMD = 25;
	public final static int EXPLAIN_TYPECHECKER_CMD = 26;

	public final static int NAVIGATOR_CMD = 27;
	public final static int INQUIRER_CMD = 28;

	public final static int ADD_MODULE_AS_WRAPPER_CMD = 29;
	public final static int ADD_MODULE_AS_SDWRAPPER_CMD = 30;
	public final static int ADD_MODULE_AS_SWARDWRAPPER_CMD = 31;

	public final static int REMOVE_INDEX_CMD = 32;

	public final static int BATCH_CMD = 33;
	public final static int CD_CMD = 34;
	public final static int PWD_CMD = 35;

	public final static int MEMMONITOR_CMD = 36;

	public final static int ADD_ENDPOINT_CMD = 37;
	public final static int REMOVE_ENDPOINT_CMD = 38;

	public final static int BENCHMARK_CMD = 39;

	public final static int ADD_MODULE_AS_PROXY_CMD = 40;
	public final static int ADD_VIEW_CMD = 41;
	public final static int REMOVE_VIEW_CMD = 42;

	public final static int REFRESH_LINK_CMD = 43;

	public final static int AST_VISUALIZER_CMD = 44;

	public final static int SUSPEND_ENDPOINT_CMD = 45;
	public final static int RESUME_ENDPOINT_CMD = 46;

	public final static int WHATIS_CMD = 47;
	public final static int EXISTS_CMD = 48;
	public final static int ECHO_CMD = 49;

	public final static int OPTIMIZATION_CMD = 50;
	public final static int SET_OPTIMIZATION_CMD = 51;

	public final static int ADD_TMPINDEX_CMD = 52;
	public final static int EXPLAIN_PROCEDURE_CMD = 53;

	//currently not used
	public final static int ADD_MODULE_AS_CMU_CMD = 54;
	public final static int ADD_MODULE_AS_PU_CMD = 55;
	//

	public final static int META_CMD = 56;

	public final static int ADD_INTERFACE_CMD = 57;
	public final static int REMOVE_INTERFACE_CMD = 58;
	public final static int ASSIGN_INTERFACE_CMD = 59;
	public final static int UNASSIGN_INTERFACE_CMD = 60;

	public final static int REMOVE_LINK_CMD = 61;

	public final static int ADD_GRIDLINK_CMD = 62;
	public final static int REMOVE_GRIDLINK_CMD = 63;
	public final static int JOINTOGRID_CMD = 64;
	public final static int REMOVEFROMGRID_CMD = 65;
	public final static int CONNECTTOGRID_CMD = 66;

	public static final int PROMOTE_TO_PROXY_CMD = 67;
	public final static int ADD_ROLE_CMD = 68;
	public final static int GRANT_PRIVILEGE_CMD = 69;

	public final static int ADD_LINK_TO_SCHEMA_CMD = 70;
	public final static int REMOVE_LINK_FROM_SCHEMA_CMD = 71;

	public final static int REMOVE_PROXY_CMD = 72;

	public static final int DEFRAGMENT_CMD = 73;
}
