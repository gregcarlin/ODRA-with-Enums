package odra.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import odra.cli.ast.ConditionalCommand;
import odra.cli.ast.ConnectCommand;
import odra.cli.ast.DatabaseURL;
import odra.cli.ast.SimpleCommand;
import odra.cli.batch.Batch;
import odra.cli.batch.BatchException;
import odra.cli.gui.ast.ASTVisualizerController;
import odra.cli.gui.memmonitor.MemoryMonitorController;
import odra.cli.gui.opt.CLIOptSequenceFrame;
import odra.cli.parser.CLIASTVisitor;
import odra.cli.parser.CLILexer;
import odra.cli.parser.CLIParser;
import odra.exceptions.rd.RDCompareTestException;
import odra.exceptions.rd.RDCompilationException;
import odra.exceptions.rd.RDDatabaseException;
import odra.exceptions.rd.RDException;
import odra.exceptions.rd.RDInternalError;
import odra.exceptions.rd.RDNetworkException;
import odra.exceptions.rd.RDOptimizationException;
import odra.exceptions.rd.RDRuntimeException;
import odra.exceptions.rd.RDSecurityException;
import odra.exceptions.rd.RDWrapperException;
import odra.filters.XML.XMLResultPrinter;
import odra.network.encoders.results.QueryResultDecoder;
import odra.network.transport.DBConnection;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.sbql.assembler.Disassembler;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.serializer.ASTDeserializer;
import odra.sbql.optimizers.OptimizationSequence;
import odra.sbql.optimizers.Type;
import odra.sbql.optimizers.benchmark.GregBenchmark;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.BooleanResult;
import odra.sbql.results.runtime.RawResultPrinter;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;
import odra.system.config.ConfigClient;
import odra.system.config.ConfigDebug;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * This class represents a command line interface to jOdra.
 * It supports single-line and multi-line commands. It order to switch
 * to multi-line mode, you write a '\' in a separate line.
 * When you finish with entering the text, you write a single '.'
 * in a separate line.
 *
 * The command parser is a little bit elaborate, since I'm not curently
 * sure what will be the final complexity of cli commands. At the present time
 * it seems that applying cup/jflex to this task would be overkill.
 * On the other hand StringTokenizer would probably be too simple.
 * That's why we have here a down-recursive parser written from scratch.
 *
 * @author raist
 */

public class CLI implements CLIASTVisitor {
    private boolean loop = true;
    private String currmod = "";

    /** CLI variables */
    protected Hashtable<CLIVariable, String> vars = new Hashtable<CLIVariable, String>();
    /** current directory */
    private String directory = System.getProperty("user.dir");

//	private NavigatorController navigator = new NavigatorController(this);
//	private InquirerController inquirer = new InquirerController(this);
    private MemoryMonitorController memoryMonitor = new MemoryMonitorController();
    private ASTVisualizerController astvisualizer = new ASTVisualizerController();
    private CLIOptSequenceFrame optSequenceFrame;

    /**
     * Replaces the System.out
     */
    protected StringBuffer outputBuffer = new StringBuffer();
    protected DBConnection db;

    protected String NEW_LINE = System.getProperty("line.separator");

    public CLI()
    {
        autoconnect();
        initialize();
    }

    protected void autoconnect()
    {
        if(!ConfigClient.CONNECT_AUTO)
            return;

        try
        {
            Thread.sleep(500);
            this.visitConnectCommand(
                new ConnectCommand(
                    new DatabaseURL(
                        ConfigClient.CONNECT_USER,
                        ConfigClient.CONNECT_PASSWORD,
                        ConfigClient.CONNECT_HOST,
                        ConfigClient.CONNECT_PORT)
                    ),
                    null);
        }
        catch(Exception exc)
        {
            if(ConfigDebug.DEBUG_EXCEPTIONS)
                exc.printStackTrace();

            outputBuffer.append(exc.getMessage() + NEW_LINE);
        }
    }

    /**
     * Initializes the environment.
     *
     */
    protected void initialize() {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException exc)
        {
            exc.printStackTrace();
        }
        catch (InstantiationException exc)
        {
            exc.printStackTrace();
        }
        catch (IllegalAccessException exc)
        {
            exc.printStackTrace();
        }
        catch (UnsupportedLookAndFeelException exc)
        {
            exc.printStackTrace();
        }

        for(CLIVariable variable : CLIVariable.values())
            setVar(variable.getName(), variable.getDefaultState());
    }

    public DBConnection getConnection() {
        return db;
    }

    /**
     * Executes a single CLI command.
     * <br />
     * A method is directly extracted from {@link #begin()} for enabling batch processing (code reuse).
     *
     * @param command CLI command string
     * @return true if there was no errors, false otherwise
     * @author jacenty
     */
    public synchronized boolean executeCommand(String command)
    {
        if(command == null || command.trim().length() == 0)
            return true;

        boolean result = false;
        try
        {
            CLIParser parser = new CLIParser(new CLILexer(command));
            parser.parseCommand().accept(this, null);

            // Send the output buffer to the System.out
            System.out.print(getOutput());
            result = true;
        }
        catch (BatchException ex)
        {
            if(ConfigDebug.DEBUG_EXCEPTIONS)
                ex.printStackTrace();

            outputBuffer.append("*** Batch error : " + ex.getMessage() + NEW_LINE);
        }
        catch (CLISyntaxErrorException ex)
        {
            if(ConfigDebug.DEBUG_EXCEPTIONS)
                ex.printStackTrace();

            outputBuffer.append("*** CLI syntax error : " + ex.getMessage() + NEW_LINE);
        }
        catch (RDRuntimeException ex)
        {
            if(ConfigDebug.DEBUG_EXCEPTIONS)
                ex.printStackTrace();

            if(ex.getModule().equals(""))
                outputBuffer.append("*** Runtime error : " + ex.getMessage() + NEW_LINE);
            else
                outputBuffer.append("Runtime error in " + ex.getModule() + " at line " + ex.getLine() + ", column " + ex.getColumn() + ": " + ex.getMessage() + NEW_LINE);
        }
        catch (RDCompilationException ex)
        {
            if(ConfigDebug.DEBUG_EXCEPTIONS)
                ex.printStackTrace();

            if(ex.getModule().equals(""))
                outputBuffer.append("*** SBQL error : " + ex.getMessage() + NEW_LINE);
            else
                outputBuffer.append("*** SBQL error in " + ex.getModule() + " at line " + ex.getLine() + ", column " + ex.getColumn() + ": " + ex.getMessage() + NEW_LINE);
        }
        catch (RDSecurityException ex)
        {
            if(ConfigDebug.DEBUG_EXCEPTIONS)
                ex.printStackTrace();

            if(ex.getModule().equals(""))
                outputBuffer.append("*** Security error : " + ex.getMessage() + NEW_LINE);
            else
                outputBuffer.append("*** Security error in " + ex.getModule() + " at line " + ex.getLine() + ", column " + ex.getColumn() + ": " + ex.getMessage() + NEW_LINE);
        }
        catch (RDInternalError ex)
        {
            if(ConfigDebug.DEBUG_EXCEPTIONS)
                ex.printStackTrace();

            outputBuffer.append("*** Internal error : " + ex.getMessage() + NEW_LINE);
        }
        catch (RDDatabaseException ex)
        {
            if(ConfigDebug.DEBUG_EXCEPTIONS)
                ex.printStackTrace();

            outputBuffer.append("*** Database error : " + ex.getMessage() + NEW_LINE);
        }
        catch (RDNetworkException ex)
        {
            if(ConfigDebug.DEBUG_EXCEPTIONS)
                ex.printStackTrace();

            outputBuffer.append("*** Network error : " + ex.getMessage() + NEW_LINE);
        }
        catch (RDOptimizationException ex)
        {
            if(ConfigDebug.DEBUG_EXCEPTIONS)
                ex.printStackTrace();

            outputBuffer.append("*** Optimization error : " + ex.getMessage() + NEW_LINE);
        }
        catch (RDWrapperException ex)
        {
            if(ConfigDebug.DEBUG_EXCEPTIONS)
                ex.printStackTrace();

            outputBuffer.append("*** Wrapper error : " + ex.getMessage() + NEW_LINE);
        }
        catch (RDCompareTestException ex)
        {
            if(ConfigDebug.DEBUG_EXCEPTIONS)
                ex.printStackTrace();

            outputBuffer.append("*** Compare test error : " + ex.getMessage() + NEW_LINE);
        }
        catch (RDException ex)
        {
            if(ConfigDebug.DEBUG_EXCEPTIONS)
                ex.printStackTrace();

            outputBuffer.append("*** Unknown error : " + ex.getMessage() + NEW_LINE);
        }
        catch (IOException ex)
        {
            if(ConfigDebug.DEBUG_EXCEPTIONS)
                ex.printStackTrace();

            outputBuffer.append("*** Network error : " + ex.getMessage() + NEW_LINE);
            resetCurrMod();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return result;
    }

    /**
    *
    * @param args 
    *
    * @throws IOException 
     * @throws BatchException 
    */
    public void begin(String[] args) throws IOException, BatchException {
    	
    	bootstrap(args);
    	begin();
    }
    
    
    /**
    *
    * Please report all changes made to this method to Mariusz.
    * @param args 
    *
    * @throws IOException 
     * @throws BatchException 
    */
    public void begin() throws IOException{
    	    
        outputBuffer.append("Welcome to ODRA (J2)!" + NEW_LINE);
        System.out.print(getOutput());

        while (loop) {
            outputBuffer.append(currmod + "> ");
            System.out.print(getOutput());

            StringBuffer buf = new StringBuffer();
            char ch;
            boolean multiline = false;

            InputStreamReader reader = new InputStreamReader(System.in);

            while (true) {
                ch = (char)reader.read();

                if (ch == '\\' && !multiline) {
                    ch = (char)reader.read();

                    if (ch == '\r' || ch == '\n')
                        multiline = true;
                    else
                        buf.append('\\');

                    buf.append(ch);
                }
                else if (ch == '.' && multiline) {
                    ch = (char)reader.read();

                    if (ch == '\r' || ch == '\n') {
                        buf.deleteCharAt(buf.length() - 1);
                        break;
                    }
                    else {
                        buf.append('.');
                        buf.append(ch);
                    }
                }
                else if (ch == '\n' && !multiline)
                    break;
                else
                    buf.append(ch);

                if (ch == '\n' && multiline)
                {
                    outputBuffer.append(">");
                    System.out.print(getOutput());
                }
            }

            if (buf.length() > 0)
                executeCommand(buf.toString().trim());
        }

        memoryMonitor.close();

        execDisconnect();
        outputBuffer.append("Disconnected from server...");
        System.out.println(getOutput());

        outputBuffer.append("Bye!");
        System.out.print(getOutput());

        System.exit(0);
    }

    /**
	 * @param args
     * @throws IOException 
     * @throws BatchException 
	 */
	private void bootstrap(String[] args) throws BatchException, IOException {
		if(ConfigClient.CLI_BOOTSTRAP_USEBATCH){			
			if(args.length == 2 && args[0].equals("--batch")){
				outputBuffer.append("Reading batch file on startup...");
				outputBuffer.append(NEW_LINE);
				execBatch(new String[] {args[1]});
				System.out.println(getOutput());
			}
			if(ConfigClient.CLI_BOOTSTRAP_BATCHONLY)
				System.exit(0);
		}
		
		
	}

	protected void checkConnection() throws IOException {
        if (db == null || !db.isConnected())
            throw new IOException("CLI is not currently connected to any databases.");
    }

    /**************************
     * Implementation of the CLIASTVisitor interface
     */

    public Object visitConnectCommand(ConnectCommand cmd, Object attr) throws Exception {
        currmod = "";

        db = new DBConnection(cmd.url.host, cmd.url.port);
        db.connect();

        DBRequest req = new DBRequest(DBRequest.LOGIN_RQST, new String[] { cmd.url.user, cmd.url.password });
        DBReply rep = db.sendRequest(req);

        currmod = cmd.url.user;

        return null;
    }

    public Object visitConditionalCommand(ConditionalCommand vis, Object attr) throws Exception
    {
        if((Boolean)vis.getCondition().accept(this, attr))
            return vis.getExecution().accept(this, attr);

        return null;
    }

    public Object visitSimpleCommand(SimpleCommand cmd, Object attr) throws Exception {
        switch (cmd.cmdid) {
            case SimpleCommand.QUIT_CMD:
                execQuit();
                break;

            case SimpleCommand.SHOW_CMD:
                execShow(cmd.data);
                break;

            case SimpleCommand.SET_CMD:
                execSet(cmd.data);
                break;

            case SimpleCommand.DISCONNECT_CMD:
                checkConnection();
                execDisconnect();
                break;

            case SimpleCommand.COMPILE_CMD:
                checkConnection();
                if(cmd.data[0].equals("."))
                	execCompile(new String[] { currmod  });
                else
                	execCompile(new String[] { currmod + "." + cmd.data[0] });
                break;

            case SimpleCommand.ADD_INTERFACE_CMD:
                checkConnection();
                execAddInterface(cmd.data, currmod);
                break;

            case SimpleCommand.ADD_MODULE_CMD:
                checkConnection();
                execAddModule(cmd.data, currmod);
                break;

            case SimpleCommand.REMOVE_MODULE_CMD:
                checkConnection();
                execRemoveModule(cmd.data);
                break;

            case SimpleCommand.DUMP_STORE_CMD:
                checkConnection();
                execDumpStore();
                break;

            case SimpleCommand.DUMP_MODULE_CMD:
                checkConnection();
                execDumpModule(cmd.data);
                break;

            case SimpleCommand.DUMP_MEMORY_CMD:
                checkConnection();
                execDumpMemory(cmd.data);
                break;

            case SimpleCommand.CMDOTDOT_CMD:
                checkConnection();
                execCmDotDot();
                break;

            case SimpleCommand.CM_CMD:
                checkConnection();
                execCm(cmd.data);
                break;

            case SimpleCommand.LS_CMD:
                checkConnection();
                execLs(currmod);
                break;

            case SimpleCommand.DISASSEMBLE_CMD:
                checkConnection();
                execDisassemble(cmd.data);
                break;

            case SimpleCommand.NAVIGATOR_CMD:
                execNavigator();
                break;

            case SimpleCommand.INQUIRER_CMD:
                execInquirer();
                break;

            case SimpleCommand.PWM_CMD:
                checkConnection();
                execPwm();
                break;

            case SimpleCommand.HELP_CMD:
                execHelp();
                break;

            case SimpleCommand.ADD_USER_CMD:
                checkConnection();
                execAddUser(cmd.data);
                break;

            case SimpleCommand.SBQL_CMD:
                checkConnection();

                // Check current syntax
                String currentSyntax = getVar(CLIVariable.getVariableForName(SYNTAX_CLI_VAR));
                if(currentSyntax.equals(SYNTAX_TYPE_SBQL)) {
                    execSbql(cmd.data);
                }
                else if(currentSyntax.equals(SYNTAX_TYPE_OCL)) {
                    execOcl(cmd.data);
                }
                else {
                    throw new Exception ("Unknown syntax type: " + currentSyntax);
                }
                break;

            case SimpleCommand.LOAD_CMD:
                checkConnection();
                execLoad(cmd.data);
                break;

            case SimpleCommand.ADD_INDEX_CMD:
            case SimpleCommand.ADD_TMPINDEX_CMD:
                checkConnection();
                execAddIndex(cmd.cmdid == SimpleCommand.ADD_TMPINDEX_CMD, cmd.data);
                break;

            case SimpleCommand.REMOVE_INDEX_CMD:
                checkConnection();
                execRemoveIndex(cmd.data);
                break;

            case SimpleCommand.ADD_LINK_CMD:
                checkConnection();
                execAddLink(cmd.data);
                break;

            case SimpleCommand.ADD_LINK_TO_SCHEMA_CMD:
                checkConnection();
                execAddLinkToSchema(cmd.data);
                break;

            case SimpleCommand.REMOVE_LINK_FROM_SCHEMA_CMD:
                checkConnection();
                execRemoveLinkFromSchema(cmd.data);
                break;

            case SimpleCommand.ADD_GRIDLINK_CMD:
                checkConnection();
                execAddGridLink(cmd.data);
                break;
            case SimpleCommand.JOINTOGRID_CMD:
                checkConnection();
                execJoinToGrid(cmd.data);
                break;
            case SimpleCommand.REMOVEFROMGRID_CMD:
                checkConnection();
                execRemoveFromGrid(cmd.data);
                break;

            case SimpleCommand.ALTER_PROCEDURE_BODY_CMD:
                checkConnection();
                execAddProcedureBody(cmd.data);
                break;
            case SimpleCommand.EXPLAIN_OPTIMIZATION_CMD:
                checkConnection();
                execExplainOptimization(cmd.data);
                break;
            case SimpleCommand.EXPLAIN_JULIET_CODE_CMD:
                checkConnection();
                execExplainJulietCode(cmd.data);
                break;
            case SimpleCommand.EXPLAIN_TYPECHECKER_CMD:
                checkConnection();
                execExplainTypechecker(cmd.data);
                break;
            case SimpleCommand.EXPLAIN_PROCEDURE_CMD:
                checkConnection();
                execExplainProcedure(cmd.data);
                break;
            case SimpleCommand.ADD_MODULE_AS_WRAPPER_CMD:
                checkConnection();
                execAddModuleAsWrapper(cmd.data, currmod);
                break;
            case SimpleCommand.ADD_MODULE_AS_SDWRAPPER_CMD:
                checkConnection();
                execAddModuleAsWrapper(cmd.data, currmod);
                break;
            case SimpleCommand.ADD_MODULE_AS_SWARDWRAPPER_CMD:
                checkConnection();
                execAddModuleAsWrapper(cmd.data, currmod);
                break;

            case SimpleCommand.BATCH_CMD:
                checkConnection();
                execBatch(cmd.data);
                break;
            case SimpleCommand.CD_CMD:
                execCd(cmd.data);
                break;
            case SimpleCommand.PWD_CMD:
                execPwd();
                break;

            case SimpleCommand.MEMMONITOR_CMD:
                checkConnection();
                execMemoryMonitor(cmd.data);
                break;
            case SimpleCommand.ADD_ENDPOINT_CMD:
                checkConnection();
                execAddEndpoint(cmd.data);
                break;
            case SimpleCommand.ADD_MODULE_AS_PROXY_CMD:
                checkConnection();
                execAddModuleAsProxy(cmd.data, currmod);
                break;
            case SimpleCommand.REMOVE_PROXY_CMD:
            	checkConnection();
                execRemoveProxy(cmd.data, currmod);
                break;
            case SimpleCommand.PROMOTE_TO_PROXY_CMD:
                checkConnection();
                execPromoteToProxy(cmd.data, currmod);
                break;

            case SimpleCommand.REMOVE_ENDPOINT_CMD:
                checkConnection();
                execRemoveEndpoint(cmd.data);
                break;

            case SimpleCommand.SUSPEND_ENDPOINT_CMD:
                checkConnection();
                execSuspendEndpoint(cmd.data);
                break;

            case SimpleCommand.RESUME_ENDPOINT_CMD:
                checkConnection();
                execResumeEndpoint(cmd.data);
                break;

            case SimpleCommand.BENCHMARK_CMD:
                checkConnection();
                execBenchmark(cmd.data);
                break;
            case SimpleCommand.ADD_VIEW_CMD:
                checkConnection();
                this.execAddView(cmd.data, currmod);
                break;
            case SimpleCommand.REMOVE_VIEW_CMD:
                checkConnection();
                this.execRemoveView(cmd.data, currmod);
                break;
            case SimpleCommand.REFRESH_LINK_CMD:
                checkConnection();
                this.execRefreshLink(cmd.data, currmod);
                break;
            case SimpleCommand.REMOVE_LINK_CMD:
                checkConnection();
                this.execRemoveLink(cmd.data, currmod);
                break;
            case SimpleCommand.REMOVE_GRIDLINK_CMD:
                checkConnection();
                this.execRemoveGridLink(cmd.data, currmod);
                break;

            case SimpleCommand.AST_VISUALIZER_CMD:
                execASTVisualizer();
                break;

            case SimpleCommand.WHATIS_CMD:
                String whatis = execWhatis(cmd.data, currmod);
                outputBuffer.append(whatis + NEW_LINE);
                break;

            case SimpleCommand.EXISTS_CMD:
                String[] serverParams = new String[3];
                System.arraycopy(cmd.data, 1, serverParams, 0, 3);//the first param is local echo
                boolean exists = execExists(serverParams, currmod);
                if(Boolean.parseBoolean(cmd.data[0]))
                    outputBuffer.append(exists + NEW_LINE);
                return exists;

            case SimpleCommand.ECHO_CMD:
                execEcho(cmd.data);
                break;

            case SimpleCommand.OPTIMIZATION_CMD:
                execOptimization();
                break;
            case SimpleCommand.SET_OPTIMIZATION_CMD:
                execSetOptimization(cmd.data);
                break;
            case SimpleCommand.META_CMD:
                execMeta(cmd.data);
                break;
            case SimpleCommand.ADD_ROLE_CMD:
				checkConnection();
				execAddRole(cmd.data);
				break;
		
			case SimpleCommand.GRANT_PRIVILEGE_CMD:
				checkConnection();
				execGrantPrivilege(cmd.data);
				break;
			case SimpleCommand.DEFRAGMENT_CMD:
				checkConnection();
				execDefragment();
        }

        return null;
    }



    /**************************
     * Implementation of particular simple commands. These methods construct
     * requests, send them to servers, and receive replies.
     */

    public String execShow(String[] data) throws IOException, RDException {
        if (ConfigDebug.ASSERTS) assert data.length == 2 : "params: variable name, echo";

        boolean echo = Boolean.parseBoolean(data[1]);

        String result = null;
        if(data[0].equals(OptimizationSequence.OPTIMIZATION) || data[0].equals(OptimizationSequence.REFOPTIMIZATION))
        {
            DBRequest req = new DBRequest(
                DBRequest.SHOW_OPTIMIZATION_RQST,
                new String[] {new Boolean(data[0].equals(OptimizationSequence.REFOPTIMIZATION)).toString()});
            DBReply rep = db.sendRequest(req);
            result = new String(rep.getRawResult());

            if(echo)
                outputBuffer.append("optimization: " + result);
        }
        else
        {
            try
            {
                result = getVar(CLIVariable.getVariableForName(data[0]));
                if(echo)
                    outputBuffer.append(result);
            }
            catch(AssertionError error)
            {
                if(echo)
                    outputBuffer.append(error.getMessage());
            }
        }

        if(echo)
            outputBuffer.append(NEW_LINE);

        return result;
    }

    /**
     * Sets the server-side optimization sequence
     *
     * @param data command parameters
     * @throws RDException
     * @throws IOException
     *
     * @author jacenty
     */
    public void execSetOptimization(String[] data) throws IOException, RDException
    {
        for(int i = 1; i < data.length; i++)
        {
            try
            {
                Type.getTypeForString(data[i]);
            }
            catch(AssertionError error)
            {
                outputBuffer.append(error.getMessage() + NEW_LINE);
                return;
            }
        }

        DBRequest req = new DBRequest(DBRequest.SET_OPTIMIZATION_RQST, data);
        db.sendRequest(req);
    }

    public void execSet(String[] data) {

        if(data[0].equals(SYNTAX_CLI_VAR))
        {
            // Upper case the syntax name
            data[1] = data[1].toUpperCase();
        }

        if(ConfigDebug.ASSERTS)
            assert data.length == 2 : "params: variable name, variable value";

        setVar(data[0], data[1]);
    }

    protected void execSbql(String[] data) throws RDException, IOException {
        DBRequest req = new DBRequest(DBRequest.EXECUTE_SBQL_RQST, new String[] { data[0], currmod, getVar(CLIVariable.AUTODEREF), getVar(CLIVariable.TEST)});
        DBReply rep = db.sendRequest(req);

        byte[] rawres = rep.getRawResult();

        QueryResultDecoder decoder = new QueryResultDecoder();
        Result res = decoder.decodeResult(rawres);

        if (getVar(CLIVariable.OUTPUT).equals("default")) {
            RawResultPrinter printer = new RawResultPrinter();
            printer.setOutputEncoding(getVar(CLIVariable.ENCODING));
            outputBuffer.append(printer.print(res));
            outputBuffer.append(NEW_LINE);
        }
        else if (getVar(CLIVariable.OUTPUT).equals("xml")) {
            XMLResultPrinter printer = new XMLResultPrinter();
            printer.setOutputEncoding(getVar(CLIVariable.ENCODING));
            outputBuffer.append(printer.print(res));
            outputBuffer.append(NEW_LINE);
        }
        else
            throw new RDInternalError("Unsupported query result printing mode");
    }

    /**
     * Executes OCL code.
     * @param data
     * @throws RDException
     * @throws IOException
     */
    protected void execOcl(String[] data) throws RDException, IOException {
        DBRequest req = new DBRequest(DBRequest.EXECUTE_OCL_RQST, new String[] { data[0], currmod, getVar(CLIVariable.AUTODEREF), getVar(CLIVariable.TEST)});
        DBReply rep = db.sendRequest(req);

        byte[] rawres = rep.getRawResult();

        QueryResultDecoder decoder = new QueryResultDecoder();
        Result res = decoder.decodeResult(rawres);

        if (getVar(CLIVariable.OUTPUT).equals("default")) {
            RawResultPrinter printer = new RawResultPrinter();
            printer.setOutputEncoding(getVar(CLIVariable.ENCODING));
            outputBuffer.append(printer.print(res));
            outputBuffer.append(NEW_LINE);
        }
        else if (getVar(CLIVariable.OUTPUT).equals("xml")) {
            XMLResultPrinter printer = new XMLResultPrinter();
            printer.setOutputEncoding(getVar(CLIVariable.ENCODING));
            outputBuffer.append(printer.print(res));
            outputBuffer.append(NEW_LINE);
        }
        else
            throw new RDInternalError("Unsupported query result printing mode");
    }

    protected void execDisassemble(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 1 : "params: procedure name";

        DBRequest req = new DBRequest(DBRequest.DISASSEMBLE_RQST, new String[] { data[0], currmod });
        DBReply rep = db.sendRequest(req);

        byte[] rawres = rep.getRawResult();
        QueryResultDecoder decoder = new QueryResultDecoder();
        StringResult strres = (StringResult) decoder.decodeResult(rawres);

        outputBuffer.append(strres.value);
        outputBuffer.append(NEW_LINE);
    }

    protected void execQuit() throws RDException {
        loop = false;
    }

    protected void execNavigator() throws RDException, IOException {
//		navigator.openNavigator();
    }

    protected void execInquirer() throws RDException, IOException {
//		inquirer.openEditableInquirer();
    }

    protected void execDumpStore() throws RDException, IOException {
        DBRequest req = new DBRequest(DBRequest.DUMP_STORE_RQST, new String[] { });
        DBReply rep = db.sendRequest(req);

        byte[] rawres = rep.getRawResult();

        QueryResultDecoder decoder = new QueryResultDecoder();
        StringResult strres = (StringResult) decoder.decodeResult(rawres);

        outputBuffer.append(strres.value);
        outputBuffer.append(NEW_LINE);
    }

    protected void execDumpModule(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 1 : "params: module name";

        String mstr;

        if (data[0].equals("."))
            mstr = currmod;
        else if (data[0].equals("system"))
            mstr = "system";
        else
            mstr = currmod + "." + data[0];

        DBRequest req = new DBRequest(DBRequest.DUMP_MODULE_RQST, new String[] { mstr });
        DBReply rep = db.sendRequest(req);

        byte[] rawres = rep.getRawResult();

        QueryResultDecoder decoder = new QueryResultDecoder();
        StringResult strres = (StringResult) decoder.decodeResult(rawres);

        outputBuffer.append(strres.value);
        outputBuffer.append(NEW_LINE);
    }

    protected void execDumpMemory(String[] data) throws RDException, IOException {
        DBRequest req = new DBRequest(DBRequest.DUMP_MEMORY_RQST, new String[] { data[0] });
        DBReply rep = db.sendRequest(req);

        byte[] rawres = rep.getRawResult();

        QueryResultDecoder decoder = new QueryResultDecoder();
        StringResult strres = (StringResult) decoder.decodeResult(rawres);

        outputBuffer.append(strres.value);
        outputBuffer.append(NEW_LINE);
    }

    protected void execAddUser(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 2 : "params: user name, password";

        DBRequest req = new DBRequest(DBRequest.ADD_USER_RQST, new String[] { data[0], data[1] });
        DBReply rep = db.sendRequest(req);
    }

    public void execCompile(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 1: "params: module";

        DBRequest req = new DBRequest(DBRequest.COMPILE_RQST, new String[] { data[0] });
        DBReply rep = db.sendRequest(req);
    }

    protected void execCm(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 1 : "params: module name";

        String mstr = currmod + "." + data[0];

        DBRequest req = new DBRequest(DBRequest.EXISTS_MODULE_RQST, new String[] { mstr });
        DBReply rep = db.sendRequest(req);

        currmod = mstr;
    }

    protected void execCmDotDot() throws RDException, IOException {
        int idx = currmod.lastIndexOf('.');

        if (idx >= 0)
            currmod = currmod.substring(0, idx);
    }

    protected void execRemoveModule(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 1 : "params: type name";

        String mstr = currmod + "." + data[0];

        DBRequest req = new DBRequest(DBRequest.REMOVE_MODULE_RQST, new String[] { mstr });
        DBReply rep = db.sendRequest(req);
    }

    protected void execRemoveView(String[] data, String parmod) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 1 : "params: view name";


        DBRequest req = new DBRequest(DBRequest.REMOVE_VIEW_RQST, new String[] {  data[0], parmod });
        DBReply rep = db.sendRequest(req);
        QueryResultDecoder decoder = new QueryResultDecoder();

        outputBuffer.append(((StringResult) decoder.decodeResult(rep.getRawResult())).value);
        outputBuffer.append(NEW_LINE);

    }
    protected void execPwm() throws RDException {
        outputBuffer.append(currmod);
        outputBuffer.append(NEW_LINE);
    }

    public String execAddInterface(String[] data, String parmod) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 1 : "params: source";

        DBRequest req = new DBRequest(DBRequest.ADD_INTERFACE_RQST, new String[] { data[0], parmod } );
        DBReply rpl = db.sendRequest(req);

        byte[] rawres = rpl.getRawResult();

        QueryResultDecoder decoder = new QueryResultDecoder();
        BagResult bag = (BagResult) decoder.decodeResult(rawres);

        StringResult str = (StringResult) bag.elementAt(0);

        return str.value;
    }

    public String execAddModule(String[] data, String parmod) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 1 : "params: source";

        DBRequest req = new DBRequest(DBRequest.ADD_MODULE_RQST, new String[] { data[0], parmod } );
        DBReply rpl = db.sendRequest(req);

        byte[] rawres = rpl.getRawResult();

        QueryResultDecoder decoder = new QueryResultDecoder();
        BagResult bag = (BagResult) decoder.decodeResult(rawres);

        StringResult str = (StringResult) bag.elementAt(0);

        return str.value;
    }
    public String execAddView(String[] data, String parmod) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 1 : "params: source";

        DBRequest req = new DBRequest(DBRequest.ADD_VIEW_RQST, new String[] { data[0], parmod } );
        DBReply rpl = db.sendRequest(req);

        byte[] rawres = rpl.getRawResult();


        QueryResultDecoder decoder = new QueryResultDecoder();
        String str = ((StringResult) decoder.decodeResult(rpl.getRawResult())).value;



        outputBuffer.append(str);
        outputBuffer.append(NEW_LINE);

        return str;
    }
    public void execLs(String cmod) throws RDException, IOException {
        DBRequest req = new DBRequest(DBRequest.LIST_RQST, new String[] { cmod });
        DBReply rpl = db.sendRequest(req);

        byte[] rawres = rpl.getRawResult();

        QueryResultDecoder decoder = new QueryResultDecoder();
        BagResult bag = (BagResult) decoder.decodeResult(rawres);

        StructResult stcres;
        StringResult strres;

        Result[] res = bag.elementsToArray();
        for (int i = 0; i < res.length; i++) {
            stcres = (StructResult) res[i];

            strres = (StringResult) stcres.fieldAt(0);
            outputBuffer.append(strres.value + "\t");

            strres = (StringResult) stcres.fieldAt(1);
            outputBuffer.append(strres.value);
            outputBuffer.append(NEW_LINE);
        }
    }

    protected void execDisconnect() throws IOException {
        db.close();
        currmod = "";
    }

    protected void execHelp() throws IOException {
        for (String hstr : help) {
            outputBuffer.append(hstr);
            outputBuffer.append(NEW_LINE);
        }
    }

    /******************************************
     * Auxilliary methods
     */

    protected String getSource(byte rqst, String[] args) throws RDException, IOException {
        DBRequest req = new DBRequest(rqst, args);
        DBReply rpl = db.sendRequest(req);

        byte[] rawres = rpl.getRawResult();

        QueryResultDecoder decoder = new QueryResultDecoder();
        StringResult strres = (StringResult) decoder.decodeResult(rawres);

        return strres.value;
    }

    protected String launchEditor(String src) throws RDException {
        String text = null;

        File tmpf = null;

        try {
            tmpf = File.createTempFile("" + System.currentTimeMillis(), ".tmp");

            FileOutputStream fo = new FileOutputStream(tmpf);
            fo.write(src.getBytes());
            fo.close();

            Process proc = Runtime.getRuntime().exec(ConfigClient.TEXT_EDITOR + " " + tmpf.getAbsolutePath());
            proc.waitFor();

            byte[] buf = new byte[(int) tmpf.length()];

            FileInputStream fi = new FileInputStream(tmpf);
            fi.read(buf);
            fi.close();

            text = new String(buf);

            try {
                tmpf.delete();
            }
            catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }
        catch (InterruptedException ex) {
            outputBuffer.append("*** Cannot launch the editor" + NEW_LINE);
        }
        catch (IOException ex) {
            outputBuffer.append("*** Cannot launch the editor" + NEW_LINE);
        }

        return text;
    }

    protected void execLoad(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 3 : "params: path, filter, params";

        // read the document being imported
        byte[] doc;

        try {
            File f = establishFilePath(data[0]);
            try
            {
                Document document = new SAXBuilder().build(f);
                XMLOutputter xmlOutputter = new XMLOutputter();
                String xml = xmlOutputter.outputString(document).trim();

                if (xml.length() > Integer.MAX_VALUE)
                    throw new IOException("The input file is too big");

                doc = xml.getBytes();
            }
            catch(JDOMException exc)
            {
                throw new IOException(exc.getMessage(), exc);
            }
        }
        catch (IOException ex) {
            outputBuffer.append(ex.getMessage());
            outputBuffer.append(NEW_LINE);
            return;
            // FIXME
        }

        // send the request
        DBRequest req = new DBRequest(DBRequest.LOAD_DATA_RQST, new String[] { currmod, new String(doc), data[1], data[2] });
        DBReply rep = db.sendRequest(req);
    }

/*	private final void execCreateIndex(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 2 : "params: index_name, query";

        DBRequest req = new DBRequest(DBRequest.CREATE_INDEX_RQST, new String[] { data[0], data[1], currmod });

        DBReply rep = db.sendRequest(req);
    }
*/

    protected void execAddLink(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 5 : "params: link name, schema, password, host, port";

        DBRequest req = new DBRequest(DBRequest.ADD_LINK_RQST, new String[] { data[0], data[1], data[2], data[3], data[4], currmod });
        DBReply rep = db.sendRequest(req);
    }

    protected void execAddGridLink(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 4 : "params: link name, schema, password, peername";

        DBRequest req = new DBRequest(DBRequest.ADD_GRIDLINK_RQST, new String[] { data[0], data[1], data[2], data[3], currmod });
        DBReply rep = db.sendRequest(req);
    }

    protected void execAddLinkToSchema(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 2 : "params: link name, schema";

        DBRequest req = new DBRequest(DBRequest.ADD_LINK_TO_SCHEMA_RQST, new String[] { data[0], data[1], currmod });
        DBReply rep = db.sendRequest(req);
    }

    protected void execRemoveLinkFromSchema(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 2 : "params: link name, schema";

        DBRequest req = new DBRequest(DBRequest.REMOVE_LINK_FROM_SCHEMA_RQST, new String[] { data[0], data[1], currmod });
        DBReply rep = db.sendRequest(req);
    }

    protected void execJoinToGrid(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 1 : "params: schema";

        DBRequest req = new DBRequest(DBRequest.JOINTOGRID_RQST, new String[] { data[0], currmod });
        DBReply rep = db.sendRequest(req);
    }

    protected void execRemoveFromGrid(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 1 : "params: schema";

        DBRequest req = new DBRequest(DBRequest.REMOVEFROMGRID_RQST, new String[] { data[0], currmod });
        DBReply rep = db.sendRequest(req);
    }

    protected void execExecuteRemoteCommad(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 1 : "params: command";

        DBRequest req = new DBRequest(DBRequest.EXECUTE_REMOTE_COMMAND_RQST, new String[] { data[0], currmod });
        DBReply rep = db.sendRequest(req);
    }

    protected void execConnectToGrid(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 4 : "params: jxtagroupname, peername, password, cmuuri";

        DBRequest req = new DBRequest(DBRequest.CONNECTTOGRID_RQST, new String[] { data[0], data[1], data[2], data[3], currmod });
        DBReply rep = db.sendRequest(req);
    }

    protected void execAddIndex(boolean temporary, String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length >= 2 : "params: index_name, rangeParams, query";

        String[] reqParams = new String[data.length + 1];
        for(int i = 0; i < data.length ; i++)
            reqParams[i] = data[i];
        reqParams[reqParams.length - 1] = currmod;

        DBRequest req = new DBRequest(temporary?DBRequest.ADD_TMPINDEX_RQST:DBRequest.ADD_INDEX_RQST, reqParams);

        DBReply rep = db.sendRequest(req);
    }

    protected void execAddEndpoint(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 8 : "params: endpoint name, exposed object, state(started|stopped), path, portType name, port name, service name, ns";

        String[] reqParams = new String[data.length + 1];
        for(int i = 0; i < data.length ; i++)
            reqParams[i] = data[i];
        reqParams[reqParams.length - 1] = currmod;

        DBRequest req = new DBRequest(DBRequest.ADD_ENDPOINT_RQST, reqParams);

        DBReply rep = db.sendRequest(req);
    }


    /**
     *  Executes a command for adding a web service proxy module.
     * @param data Options
     * @param parmod Parent Module
     * @throws RDException
     * @throws IOException
     */
    protected void execAddModuleAsProxy(String[] data, String parmod) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 2 : "params: module name, wsdl url, parent module name";

        DBRequest req = new DBRequest(DBRequest.ADD_PROXY_RQST, new String[] {data[0], data[1], parmod});
        DBReply rpl = db.sendRequest(req);

        byte[] rawres = rpl.getRawResult();

        QueryResultDecoder decoder = new QueryResultDecoder();
        StringResult result = (StringResult)decoder.decodeResult(rawres);
    }

    protected void execRemoveProxy(String[] data, String parmod) throws RDException, IOException {
    	if (ConfigDebug.ASSERTS) assert data.length == 1 : "params: proxy name, parent module";

    	DBRequest req = new DBRequest(DBRequest.REMOVE_PROXY_RQST, new String[] { data[0], parmod} );
    	DBReply rpl = db.sendRequest(req);

    	byte[] rawres = rpl.getRawResult();

    	QueryResultDecoder decoder = new QueryResultDecoder();
    	StringResult result = (StringResult)decoder.decodeResult(rawres);

    }
    protected void execPromoteToProxy(String[] data, String parmod) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 4 : "params: module name, wsdl url, port, service, parent module name";

        DBRequest req = new DBRequest(DBRequest.PROMOTE_TO_PROXY_RQST, new String[] {data[0], data[1], data[2], data[3], parmod});
        DBReply rpl = db.sendRequest(req);

        byte[] rawres = rpl.getRawResult();

        QueryResultDecoder decoder = new QueryResultDecoder();
        StringResult result = (StringResult)decoder.decodeResult(rawres);
    }

    protected void execRemoveIndex(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 1 : "params: index name";

        DBRequest req = new DBRequest(DBRequest.REMOVE_INDEX_RQST, new String[] { data[0] , currmod });

        DBReply rep = db.sendRequest(req);

    }

    protected void execRemoveEndpoint(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 1 : "params: endpoint name";

        DBRequest req = new DBRequest(DBRequest.REMOVE_ENDPOINT_RQST, new String[] { data[0] , currmod });

        DBReply rep = db.sendRequest(req);

    }

    protected void execSuspendEndpoint(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 1 : "params: endpoint name";

        DBRequest req = new DBRequest(DBRequest.SUSPEND_ENDPOINT_RQST, new String[] { data[0] , currmod });

        DBReply rep = db.sendRequest(req);

    }

    protected void execResumeEndpoint(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 1 : "params: endpoint name";

        DBRequest req = new DBRequest(DBRequest.RESUME_ENDPOINT_RQST, new String[] { data[0] , currmod });

        DBReply rep = db.sendRequest(req);

    }

    protected void execAddProcedureBody(String[] data) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length == 2 : "params: procedure name, procedure source";

        DBRequest req = new DBRequest(DBRequest.ALTER_PROCEDURE_BODY_RQST, new String[] { data[0], data[1], currmod });

        DBReply rep = db.sendRequest(req);
    }

    protected void execExplainOptimization(String[] data) throws RDException, IOException {
        String[] reqParams = new String[data.length + 1];
        for(int i = 0; i < data.length ; i++)
            reqParams[i] = data[i];
        reqParams[reqParams.length - 1] = currmod;

        DBRequest req = new DBRequest(DBRequest.EXPLAIN_OPTIMIZATION_RQST, reqParams);
        DBReply rep = db.sendRequest(req);
        String query = new String(rep.getRawResult());
        outputBuffer.append(query + NEW_LINE);
    }

    protected void execExplainJulietCode(String[] data) throws RDException, IOException {
        String[] reqParams = new String[data.length + 1];
        for(int i = 0; i < data.length ; i++)
            reqParams[i] = data[i];
        reqParams[reqParams.length - 1] = currmod;

        DBRequest req = new DBRequest(DBRequest.EXPLAIN_JULIETCODE_RQST, reqParams);
        DBReply rep = db.sendRequest(req);
        Disassembler dis = new Disassembler(rep.getRawResult());
        String codestr = dis.decode();
        outputBuffer.append(codestr + NEW_LINE);
    }

    protected void execExplainTypechecker(String[] data) throws RDException, IOException {
        String[] reqParams = new String[data.length + 1];
        for(int i = 0; i < data.length ; i++)
            reqParams[i] = data[i];
        reqParams[reqParams.length - 1] = currmod;

        DBRequest req = new DBRequest(DBRequest.EXPLAIN_TYPECHECKER_RQST, reqParams);
        DBReply rep = db.sendRequest(req);
        String query = new String(rep.getRawResult());
        outputBuffer.append(query + NEW_LINE);
    }

    protected void execExplainProcedure(String[] data) throws RDException, IOException {
        String[] reqParams = new String[data.length + 1];
        for(int i = 0; i < data.length ; i++)
            reqParams[i] = data[i];
        reqParams[reqParams.length - 1] = currmod;

        DBRequest req = new DBRequest(DBRequest.EXPLAIN_PROCEDURE_RQST, reqParams);
        DBReply rep = db.sendRequest(req);
        String query = new String(rep.getRawResult());
        outputBuffer.append(query + NEW_LINE);
    }

    public ASTNode execParseOnly(String[] data) throws RDException, IOException {
        String[] reqParams = new String[data.length + 1];
        for(int i = 0; i < data.length ; i++)
            reqParams[i] = data[i];
        reqParams[reqParams.length - 1] = currmod;

        DBRequest req = new DBRequest(DBRequest.PARSE_ONLY_RQST, reqParams);
        DBReply rep = db.sendRequest(req);
        ASTNode query = new ASTDeserializer().readAST(rep.getRawResult());
        return query;
    }

    public ASTNode execTypecheckOnly(String[] data) throws RDException, IOException {
        String[] reqParams = new String[data.length + 1];
        for(int i = 0; i < data.length ; i++)
            reqParams[i] = data[i];
        reqParams[reqParams.length - 1] = currmod;

        DBRequest req = new DBRequest(DBRequest.TYPECHECK_ONLY_RQST, reqParams);
        DBReply rep = db.sendRequest(req);
        ASTNode query = new ASTDeserializer().readAST(rep.getRawResult());
        return query;
    }

    public ASTNode execOptimizeOnly(String[] data) throws RDException, IOException {
        String[] reqParams = new String[data.length + 1];
        reqParams[0] = data[0];
        reqParams[1] = data[1];
        reqParams[2] = data[2];
        reqParams[3] = currmod;

        for(int i = 3; i < data.length ; i++)
            reqParams[i + 1] = data[i];

        DBRequest req = new DBRequest(DBRequest.OPTIMIZE_ONLY_RQST, reqParams);
        DBReply rep = db.sendRequest(req);
        ASTNode query = new ASTDeserializer().readAST(rep.getRawResult());
        return query;
    }

    protected void execRefreshLink(String[] data, String parmod) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length >= 1 : "params: link_name";

        DBRequest req = new DBRequest(DBRequest.REFRESH_LINK_RQST, new String[] {  data[0], parmod });
        DBReply rep = db.sendRequest(req);
    }

    protected void execRemoveLink(String[] data, String parmod) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length >= 1 : "params: link_name";

        DBRequest req = new DBRequest(DBRequest.REMOVE_LINK_RQST, new String[] {  data[0], parmod });
        DBReply rep = db.sendRequest(req);
    }

    protected void execRemoveGridLink(String[] data, String parmod) throws RDException, IOException {
        if (ConfigDebug.ASSERTS) assert data.length >= 1 : "params: link_name";

        DBRequest req = new DBRequest(DBRequest.REMOVE_GRIDLINK_RQST, new String[] {  data[0], parmod });
        DBReply rep = db.sendRequest(req);
    }

    /**
     * Executes 'whatis' command.
     *
     * @param data
     * @param parmod
     * @throws RDException
     * @throws IOException
     */
    protected String execWhatis(String[] data, String parmod) throws RDException, IOException
    {
        if (ConfigDebug.ASSERTS)
            assert data.length == 1 : "params: object_name";

        DBRequest req = new DBRequest(DBRequest.WHATIS_RQST, new String[] {data[0], parmod});
        DBReply rep = db.sendRequest(req);

        return ((StringResult)rep.getResult()).value + NEW_LINE;
    }

    /**
     * Executes 'exists' command.
     *
     * @param data
     * @param parmod
     * @throws RDException
     * @throws IOException
     */
    protected boolean execExists(String[] data, String parmod) throws RDException, IOException
    {
        if (ConfigDebug.ASSERTS)
            assert data.length == 3 : "params: positive, type, object_name";

        DBRequest req = new DBRequest(DBRequest.EXISTS_RQST, new String[] {data[1], data[2], parmod});
        DBReply rep = db.sendRequest(req);

        boolean exists = ((BooleanResult)rep.getResult()).value;
        if(!Boolean.parseBoolean(data[0]))
            exists = !exists;
        return exists;
    }

    /**
     * Executes 'echo' command.
     *
     * @param data command arguments
     */
    protected void execEcho(String[] data)
    {
        if (ConfigDebug.ASSERTS)
            assert data.length == 1 : "params: message";

        outputBuffer.append(data[0] + NEW_LINE);
    }

    /**
     * Executes 'optimization' command.
     */
    protected void execOptimization()
    {
        if(optSequenceFrame == null)
            optSequenceFrame = new CLIOptSequenceFrame(this);

        optSequenceFrame.setVisible(true);
    }
    
    protected void execDefragment() throws RDException, IOException {

        DBRequest req = new DBRequest(DBRequest.DEFRAGMENT_RQST, new String[] { });
        DBReply rep = db.sendRequest(req);
        
        byte[] rawres = rep.getRawResult();

        QueryResultDecoder decoder = new QueryResultDecoder();
        StringResult strres = (StringResult) decoder.decodeResult(rawres);

        outputBuffer.append(strres.value);
        outputBuffer.append(NEW_LINE);
    }

    public void setVar(String varname, String varval) {
        try
        {
            CLIVariable variable = CLIVariable.getVariableForName(varname);
            if(variable.isStateValid(varval))
                vars.put(variable, varval);
            else
                outputBuffer.append("**** Undefined state '" + varval + "' for variable '" + varname + "'." + NEW_LINE);
        }
        catch(AssertionError error)
        {
            outputBuffer.append("**** " + error.getMessage() + NEW_LINE);
        }
    }

    public String getVar(CLIVariable variable) {
        return vars.get(variable);
    }

    public final static void main(String[] args) throws Exception {
        java.lang.System.setProperty("apple.laf.useScreenMenuBar", "true");
        java.lang.System.setProperty("apple.awt.antialiasing", "false");
        java.lang.System.setProperty("apple.awt.textantialiasing", "false");
        java.lang.System.setProperty("com.apple.mrj.application.apple.menu.about.name", "CLI");

        new CLI().begin(args);
    }

    protected static String[] help = {
        "  help - shows this help",
        "  connect <user>/<password>@<host>:<port> - connects to a database",
        "  disconnect - disconnects from a database",
        "  echo <message> - echoes the <message>",
        "  whatis <name> - checks the <name> and displays its meaning in the current module",
        "  ls - shows a list of objects declared in the current module",
        "  [not] exists <module | index | user | endpoint | view | link > <name> - checks if the <name> of the given type exists in the current module",
        "  if [not] exists <module | index | user | endpoint | view | link> <name> then <CLI command> - checks if the <name> of the given type exists in the current module, if true executes <CLI command>",
        "  cm <submodule> - changes the current module (like cd in filesystems)",
        "  cm .. - changes the current module to the parent one (like cd .. in filesystems)",
        "  pwm - shows the current module (like pwd in filesystems)",
        "  add module <module source code> - creates a new module",
        "  add module <module name> as wrapper on <host>:<port> - creates a new module based on a wrapper listening on a server and a port provided",
        "  add module <module name> as proxy on <wsdl> - creates a new module based on a proxy based on supplied wsdl contract ",
        "  remove module <module name> - deletes a module",
        "  remove proxy <proxy name> - removes proxy from object pointed by name",
        "  compile <module name> - compiles a module (recursively)",
        "  dump store - shows internal structures of the data store",
        "  dump module <module name> - shows internal structures of a module",
        "  add user <user name> identified by \"<password>\" - creates a new user account ",
        "  add index <index_name> [ (<type> [ | <type> ... ] ) ] on <creating_query>; - creates new index on data described by creating_query",
        "  remove index <index_name> - deletes an index",
        "  add endpoint <endpoint name> on <object to expose name> with (state=<STARTED|STOPPED>, path=<relative url>, portType=<portType name>, port=<port name>, service=<service name>, ns=<namespace> - creates web service endpoint",
        "  remove endpoint <endpoint name> - removes given web service endpoint ",
        "  disassemble <procedure name> - shows procedure's byte code",
        "  \\ - initiates multiline editing mode",
        "  . - ends multiline editing mode",
        "  set <variable name> <value> - sets the value of an internal CLI variable",
        "  show <variable name> - shows the value of an internal CLI variable ",
        "  set optimization <opt> [ | <opt1> | <opt2> ...] - sets the optimization sequence ('none' resets)",
        "  show optimization - shows the current optimization sequence",
        "  cd <path> - changes the working directory in a local filesystem",
        "  pwd - shows the working directory in a local filesystem",
        "  batch <path> - executes a batch file",
        "  explain optimization <opt> [ | <opt1> | <opt2> ...] : <query>; - presents a result of an optmimization sequence on a query",
        "  explain julietcode : <query>; - presents a bytecode generated for a query",
        "  explain typechecker : <query>; - presents a typechecked query",
        "  memmonitor - shows a client memory monitor",
        "  memmonitor <user>/<password>@<host>:<port> - shows a server memory monitor",
        "  astvisualizer - opens a graphical tool for syntax tree visualization",
        "  benchmark <n> <query>; - performs an optimization benchmark <n> times and writes results to a CSV file",
        "  defragment - optimizes data store for faster allocation of memory",
    };

    /**
     * Returns the output of a command instead of printing it on the screen
     * @return string containing the output
     */
    public String getOutput() {
        String output = outputBuffer.toString();

        outputBuffer = new StringBuffer();

        return output;
    }

    /**
     * Resets current module.
     *
     */
    public void resetCurrMod() {
        currmod = "";
    }

    /**
     * Clears an output buffer (actually creates a new one) which is used as an replacement for System.out.
     * @return
     */
    public void clearOutputBuffer() {
        outputBuffer = new StringBuffer();
    }

    /**
     * Gets current module.
     * @return
     */
    public String getCurrMod() {
        return currmod;
    }

    /**
     * Executes a command for adding a wrapper module.
     *
     * @param data command parameters
     * @param parmod parent module
     * @return response
     * @throws RDException
     * @throws IOException
     *
     * @author jacenty
     */
    protected String execAddModuleAsWrapper(String[] data, String parmod) throws RDException, IOException {
        if(ConfigDebug.ASSERTS)
            assert data.length == 4 : "params: module name, wrapper server host, wrapper server port, wrapper mode";

        DBRequest req = new DBRequest(DBRequest.ADD_MODULE_AS_WRAPPER_RQST, new String[] {data[0], data[1], data[2], data[3], parmod});
        DBReply rpl = db.sendRequest(req);

        byte[] rawres = rpl.getRawResult();

        QueryResultDecoder decoder = new QueryResultDecoder();
        StringResult result = (StringResult)decoder.decodeResult(rawres);

        return result.value;
    }


    /**
     * Executes a command for a batch file execution.
     *
     * @param data command parameters
     * @throws IOException
     *
     * @author jacenty
     * @throws IOException
     * @throws BatchException
     */
    public synchronized void execBatch(String[] data) throws IOException, BatchException
    {
        if(ConfigDebug.ASSERTS)
            assert data.length == 1 : "params: batch file path";

        Batch batch = new Batch(establishFilePath(data[0]));

        String errorMessage = null;
        if(!batch.exists())
            errorMessage = "Cannot find file '" + batch + "'.";
        else if(batch.isDirectory())
            errorMessage = "'" + batch + "' is a directory.";
        else if(!batch.canRead())
            errorMessage = "Cannot read file '" + batch + "'.";

        if(errorMessage != null)
        {
            outputBuffer.append("**** " + errorMessage + NEW_LINE);
            return;
        }

        Vector<String> newCommands = batch.read();
        outputBuffer.append(newCommands.size() + " command(s) read from '" + batch + "'." + NEW_LINE);
        System.out.print(getOutput());

        for(String command: newCommands)
        {
            if(batch.verbose())
            {
                outputBuffer.append("******* processing: " + command + " *******" + NEW_LINE);
                System.out.print(getOutput());
            }
            int errors = 0;
            if(!executeCommand(command))
            {
            	errors ++;
        		if(errors >= batch.getErrorNumber()) 
        			throw new BatchException("******* too many errors {"+ errors +"}, batch processing stopped *******" + NEW_LINE);

            }
        }
    }

    /**
     * Executes a command for changing a working directory.
     *
     * @param data command parameters
     * @return new working directory
     * @throws IOException
     *
     * @author jacenty
     */
    protected File execCd(String[] data) throws IOException
    {
        if(ConfigDebug.ASSERTS)
            assert data.length == 1 : "params: directory path";

        String path = data[0];
        File dir = new File(path);
        if(!dir.isAbsolute())
        {
            boolean isRoot = false;
            for(File root : File.listRoots())
                if(dir.getCanonicalFile().equals(root))
                {
                    isRoot = true;
                    break;
                }

            if(!isRoot)
            {
                String oldPath = getWorkingDirectory().getCanonicalPath();
                if(!oldPath.endsWith(File.separator))
                    oldPath += File.separator;
                dir = new File(oldPath + path);
            }
        }

        dir = dir.getCanonicalFile();

        String errorMessage = null;
        if(!dir.exists())
            errorMessage = "Cannot find directory '" + data[0] + "'.";
        else if(!dir.isDirectory())
            errorMessage = "'" + data[0] + "' is not a valid directory.";
        else if(!dir.canRead())
            errorMessage = "Cannot read directory '" + data[0] + "'.";

        if(errorMessage != null)
        {
            outputBuffer.append("**** " + errorMessage + NEW_LINE);
            return getWorkingDirectory();
        }

        this.directory = dir.getCanonicalPath();
        return dir;
    }

    /**
     * Executes a command for printing a working directory.
     *
     * @throws IOException
     * @author jacenty
     */
    protected void execPwd() throws IOException
    {
        outputBuffer.append(getWorkingDirectory().getCanonicalPath() + NEW_LINE);
    }

    /**
     * Returns a current working direcotry.
     *
     * @return current working direcotry
     */
    public final File getWorkingDirectory()
    {
        return new File(directory);
    }

    /**
     * Executes a command for showing a memory monitor.
     *
     * @param data command parameters
     * @author jacenty
     */
    protected void execMemoryMonitor(String[] data)
    {
        memoryMonitor.openMonitor(data);
    }

    /**
     * Executes a command for query benchmarking.
     *
     * @param data command parameters
     * @throws Exception
     * @author jacenty
     */
    protected void execBenchmark(String[] data) throws Exception
    {
        if(!(getVar(CLIVariable.TEST).equals("plaintimes") || getVar(CLIVariable.TEST).equals("compare") || getVar(CLIVariable.TEST).equals("comparesimple")))
            throw new CLISyntaxErrorException("Set 'plaintimes' or 'compare' or 'comparesimple' test before running 'benchmark'.");

        new GregBenchmark(this, data[0], Integer.parseInt(data[1]), Boolean.parseBoolean(data[2])).start();
    }

    protected void execASTVisualizer() {
        astvisualizer.openVisualizer(this);
    }
    protected void execMeta(String[] data) throws RDException, IOException {
        DBRequest req = new DBRequest(DBRequest.EXEC_META_RQST, new String[] { data[0], currmod});
        DBReply rep = db.sendRequest(req);

        byte[] rawres = rep.getRawResult();

        QueryResultDecoder decoder = new QueryResultDecoder();
        Result res = decoder.decodeResult(rawres);

        if (getVar(CLIVariable.OUTPUT).equals("default")) {
            RawResultPrinter printer = new RawResultPrinter();
            printer.setOutputEncoding(getVar(CLIVariable.ENCODING));
            outputBuffer.append(printer.print(res));
            outputBuffer.append(NEW_LINE);
        }
        else if (getVar(CLIVariable.OUTPUT).equals("xml")) {
            XMLResultPrinter printer = new XMLResultPrinter();
            printer.setOutputEncoding(getVar(CLIVariable.ENCODING));
            outputBuffer.append(printer.print(res));
            outputBuffer.append(NEW_LINE);
        }
        else
            throw new RDInternalError("Unsupported query result printing mode");
    }

    protected void execAddRole(String[] data) throws RDException, IOException {


	DBRequest req = new DBRequest(DBRequest.ADD_ROLE_RQST, new String[] { data[0] });
	DBReply rep = db.sendRequest(req);
    }
    protected void execGrantPrivilege(String[] data) throws RDException, IOException {
	DBRequest req = new DBRequest(DBRequest.GRANT_PRIVILEGE_RQST, new String[] { data[0], data[1], data[2], data[3], data[4] });
	DBReply rep = db.sendRequest(req);

	}
    /**
     * Determines the file path according to the path inputted by a user.
     *
     * @param inputPath inpput file string entered by user
     * @return actual file
     * @throws IOException
     */
    private File establishFilePath(String inputPath) throws IOException
    {
        File file = new File(inputPath);
        if(!file.isAbsolute())
        {
            String path = getWorkingDirectory().getCanonicalPath();
            if(!path.endsWith(File.separator))
                path += File.separator;
            file = new File(path + inputPath);
        }

        return file;
    }

    // ------------------------ Code related to the syntax switching - START

    /**
     * Keyword used in CLI.
     */
    public static final String SYNTAX_CLI_VAR = "syntax";

    public static final String SYNTAX_TYPE_OCL = "OCL";
    public static final String SYNTAX_TYPE_SBQL = "SBQL";

    // ------------------------ Code related to the syntax switching - END
}
