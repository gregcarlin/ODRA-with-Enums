package odra.dbinstance.processes;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;

import org.apache.tools.zip.ExtraFieldUtils;

import odra.cli.CLI;
import odra.cli.ast.ConnectCommand;
import odra.cli.ast.DatabaseURL;
import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.IndicesException;
import odra.db.links.RemoteDefaultStoreOID;
import odra.db.links.encoders.RemoteQueryParameterDecoder;
import odra.db.objects.data.DBClass;
import odra.db.objects.data.DBLink;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBObject;
import odra.db.objects.data.DBProcedure;
import odra.db.objects.data.DBVirtualObjectsProcedure;
import odra.db.objects.data.DataObjectKind;
import odra.db.objects.data.ModuleDumper;
import odra.db.objects.meta.MBIndex;
import odra.db.objects.meta.MBLink;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBView;
import odra.db.objects.meta.MetaObjectKind;
import odra.dbinstance.DBInstance;
import odra.exceptions.rd.RDNetworkException;
import odra.filters.DataImporter;
import odra.filters.FilterException;
import odra.network.encoders.messages.MessageDecoder;
import odra.network.encoders.messages.MessageEncoder;
import odra.network.encoders.metabase.MetaEncoder;
import odra.network.encoders.results.QueryResultDecoder;
import odra.network.encoders.results.QueryResultEncoder;
import odra.network.encoders.signatures.SBQLPrimitiveSignatureDecoder;
import odra.network.encoders.stack.SBQLStackDecoder;
import odra.network.transport.DBConnection;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.sbql.SBQLException;
import odra.sbql.assembler.Disassembler;
import odra.sbql.ast.AST2TextQueryDumper;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.DeepCopyAST;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.serializer.ASTSerializer;
import odra.sbql.ast.statements.ExpressionStatement;
import odra.sbql.ast.statements.Statement;
import odra.sbql.builder.BuilderUtils;
import odra.sbql.builder.ModuleCompiler;
import odra.sbql.builder.ModuleConstructor;
import odra.sbql.builder.ModuleLinker;
import odra.sbql.builder.ModuleOrganizer;
import odra.sbql.emiter.EmiterFactory;
import odra.sbql.emiter.IJulietCodeGenerator;
import odra.sbql.emiter.JulietCode;
import odra.sbql.emiter.JulietGen;
import odra.sbql.emiter.OpCodes;
import odra.sbql.interpreter.InterpreterException;
import odra.sbql.interpreter.SBQLInterpreter;
import odra.sbql.interpreter.SBQLInterpreter.ExecutionMode;
import odra.sbql.optimizers.OptimizationException;
import odra.sbql.optimizers.OptimizationFramework;
import odra.sbql.optimizers.OptimizationSequence;
import odra.sbql.optimizers.Type;
import odra.sbql.optimizers.benchmark.Benchmark;
import odra.sbql.optimizers.queryrewrite.index.IndexOptimizerException;
import odra.sbql.parser.OCLLexer;
import odra.sbql.parser.OCLParser;
import odra.sbql.results.AbstractQueryResult;
import odra.sbql.results.compiletime.util.SignatureInfo;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.BooleanResult;
import odra.sbql.results.runtime.DateResult;
import odra.sbql.results.runtime.DoubleResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;
import odra.sbql.typechecker.SBQLProcedureTypeChecker;
import odra.sbql.typechecker.SBQLTypeChecker;
import odra.security.AccessControl;
import odra.security.AccessControlException;
import odra.security.AccountManager;
import odra.security.AuthenticationException;
import odra.security.RoleManager;
import odra.sessions.Session;
import odra.store.DefaultStore;
import odra.store.DefaultStoreOID;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.sbastore.ObjectManager;
import odra.system.Names;
import odra.system.Sizes;
import odra.system.config.ConfigServer;
import odra.util.HeapSorter;
import odra.virtualnetwork.BatchString;
import odra.virtualnetwork.RequestHandlerImpl;
import odra.virtualnetwork.pu.Repository;
import odra.wrapper.Wrapper;
import odra.wrapper.WrapperException;
import odra.ws.endpoints.EndpointState;
import odra.ws.endpoints.WSEndpointException;
import odra.ws.facade.IEndpointFacade;
import odra.ws.facade.IProxyFacade;
import odra.ws.facade.WSManagersFactory;
import odra.ws.facade.WSProxyException;
import odra.ws.facade.WSProxyType;

/**
 * A class describing server processes. Server processes represent
 * client connections at the server side. For each client connection,
 * one server process is constructed. Each server process has its own
 * sbql compiler, a private transient heap, set of privileges, etc.
 *
 * @author raist
 */
public class ServerProcess extends Thread {
	public DBInstance instance;

	private boolean loop; // indicates whether the thread should keep running or not
	private boolean notify = true; // indicates whether the thread should be notified of new data

	private Object monitor = new Object(); // used when putting the thread to sleep/waking it up
	private int errCnt = 0; // error counting

	private SocketChannel channel; // message pipe to the client

	private Vector<ByteBuffer> msgBuffers = new Vector(); // enqueues byte blocks received from the listener process

	private MessageDecoder msgDecoder = new MessageDecoder(); // decodes byte arrays into java objects
	private MessageEncoder msgEncoder = new MessageEncoder(); // encodes java objects into byte arrays

	protected QueryResultEncoder qresEncoder; // encodes query results
	protected QueryResultDecoder qresDecoder; // decodes query results

	private ByteBuffer lenBuffer = ByteBuffer.allocate(Sizes.INTVAL_LEN); // stores the length of a message
	private ByteBuffer msgBuffer = ByteBuffer.allocate(ConfigServer.MSG_DECODER_BUFFER); // stores the message


	private ModuleLinker linker;
	private ModuleCompiler compiler;
	private HeapSorter sorter;


	/**
	 * Initializes the server process (for child classes).
	 */
	protected ServerProcess(){}

	/**
	 * @param instance the database instance which initiated this server process
	 * @param channel socket channel responsible for ensuring communication with the client
	 */
	public ServerProcess(DBInstance instance, SocketChannel channel) throws DatabaseException {
		super("svrp-unknown");

		this.instance = instance;
		this.channel = channel;

		sendHelloString();
	}

	/**
	 * Sends the hello string to the client on instantiation. The string is verified by client's {@link DBConnection}.
	 *
	 * @throws DatabaseException
	 */
	private void sendHelloString() throws DatabaseException
	{
		try
		{
			ByteBuffer buffer = ByteBuffer.allocate(DBConnection.SERVER_HELLO_MESSAGE.getBytes().length);
			buffer.put(DBConnection.SERVER_HELLO_MESSAGE.getBytes());
			buffer.flip();
			channel.write(buffer);
		}
		catch (IOException exc)
		{
			if(ConfigServer.DEBUG_EXCEPTIONS)
				exc.printStackTrace();
			throw new DatabaseException(exc.getMessage());
		}
	}

	/**
	 * Returns the database instance which initiated this server process
	 */
	public DBInstance getInstance() {
		return instance;
	}

	/**
	 * Returns the socket channel responsible for ensuring communication with the client
	 */
	public SocketChannel getChannel() {
		return channel;
	}

	/**
	 * Initializes the interpreter and the type checker.
	 * Executed when a user is authenticated.
	 */
	public void initSBQL() throws DatabaseException {

		this.linker = BuilderUtils.getModuleLinker();
		this.compiler = BuilderUtils.getModuleCompiler();

		this.qresEncoder = new QueryResultEncoder(instance.getHostName(), Session.getUserContext().getModule());

		this.qresDecoder = new QueryResultDecoder();
	}

	/**
	 * The main thread loop. When woken up, analyzes the message buffer
	 * which stores parts of messages received from the client side.
	 * It takes the parts, frees the buffer, and passes them to a method
	 * responsible for message
	 */
	public void run() {

		loop = true;
	    Session.create();

		while (loop) {
			try {
				synchronized (monitor) {
					synchronized (msgBuffers) {
						notify = msgBuffers.size() == 0;
					}

					if (notify) {
						// put the thread to sleep
						monitor.wait();
						notify = false;
					}
				}
				// when the thread is woken up, check if there are messages
				// (eg. sbql queries) waiting to be processed.
				ByteBuffer buf;
				while (msgBuffers.size() > 0) {
					buf = msgBuffers.firstElement();
					msgBuffers.remove(0);
					feed(buf);
				}

				errCnt = 0;
			}
			catch (Exception ex) {
				ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Exception during SVRP execution", ex);

				if (errCnt++ > ConfigServer.SVRP_STOP_ON_ERRORS)
					shutdown();
			}
		}
		Session.close();

	}

	/**
	 * Executed when a stream of data arrives. A stream may contain a part of a
	 * message or even more than one messages. That is why we need special
	 * precautions here. When a complete message is identified, it is decoded
	 * and executed as a command.
	 * @param buf byte buffer containing part of message or several messages
	 */
	private void feed(ByteBuffer buf) throws IOException {
		Session.resetTime();
		// first, the lenBuffer must be filled. this buffer stores
		// an integer indicating the length of the whole message.
		while (lenBuffer.hasRemaining() && buf.hasRemaining())
			lenBuffer.put(buf.get());

		// when the lenBuffer is filled up, we can analyze the rest of the message
		if (!lenBuffer.hasRemaining()) {
			// read the length of the message stored in lenBuffer
			lenBuffer.position(0);
			int msgLen = lenBuffer.getInt();

			// if the message buffer is not too small, read the message to the last byte
			if (msgLen > ConfigServer.MSG_DECODER_BUFFER) {
				sendReply(new DBReply[] { DBReply.createERRReply("Request cannot be accepted (increase the MSG_DECODER_BUFFER to at least " + msgLen + " bytes)") });


			}
			else
				while (msgBuffer.position() < msgLen && buf.hasRemaining())
					msgBuffer.put(buf.get());

			// if the message is read, decode it. if not, wait for another part
			if (msgBuffer.position() == msgLen) {
				msgBuffer.flip();

				// decode the request, process it, encode the reply
				DBRequest[] reqs = msgDecoder.decodeRequestMessage(msgBuffer.array());
				DBReply[] reps = processRequests(reqs);

				sendReply(reps);

				if (buf.remaining() > 0)
					feed(buf);
			}
		}
	}

	private void sendReply(DBReply[] reps) throws IOException {
		byte[] bufarr = msgEncoder.encodeReplyMessage(reps);

		// send the reply (first comes its length)
		ByteBuffer outbuf = ByteBuffer.allocate(Sizes.INTVAL_LEN + bufarr.length);
		outbuf.putInt(bufarr.length);
		outbuf.put(bufarr);
		outbuf.flip();

		while (outbuf.remaining() > 0)
			channel.write(outbuf);

		lenBuffer.clear();
		msgBuffer.clear();
	}

	/**
	 * Called by the listener process when a message block addressed
	 * to a particular server process is retrieved. It enquees
	 * the data in the mssage buffer and wakes up the thread.
	 * @param msg byte buffer containing part of message or several messages
	 */
	public void messageReceived(ByteBuffer msg) {
		// copy the message
		ByteBuffer buf = ByteBuffer.allocate(msg.limit());
		buf.put(msg);

		buf.flip();

		 // enqueue it for the main thread loop
	    msgBuffers.add(buf);

		// this is to prevent situations where notify() precedes sleep()
		if (notify)
			synchronized (monitor) {
				// wake up the thread so that it could decode the new message block
				// and immediately return to the listener
				monitor.notify();
			}

	}

	/**
	 * Stops the server process.
	 */
	public void shutdown() {
		loop = false;
		synchronized (monitor) {
			monitor.notify();
		}
	}

	/* **************************************************************************
	 * This part is responsible for processing requests sent by clients.
	 */

	/**
	 * Called when new requests arrive. For every request there must be a reply sent back.
	 * @param requests array of decoded user requests
	 * @return array of replies which should be sent to the client
	 */
	//changed visibility by mich for grid process see RequestHandlerImpl
	protected DBReply[] processRequests(DBRequest[] requests) {
		Vector<DBReply> replies = new Vector<DBReply>();

		for (DBRequest r : requests) {
			if (!Session.isInitialized() && requests[0].opcode != DBRequest.LOGIN_RQST) {
				replies.addElement(DBReply.createERRReply("User has not been logged in", DBReply.STAT_SECURITY_ERROR));

				continue;
			}

			try {
				switch (r.opcode) {
					case DBRequest.ADD_MODULE_RQST:
						replies.addElement(execAddModule(r.params[0], r.params[1]));
						break;

					case DBRequest.REMOVE_MODULE_RQST:
						replies.addElement(execDropModule(r.params[0]));
						break;

					case DBRequest.DUMP_MODULE_RQST:
						replies.addElement(execDumpModule(r.params[0]));
						break;

					case DBRequest.ADD_INTERFACE_RQST:
						replies.addElement(execAddInterface(r.params[0], r.params[1]));
						break;

					case DBRequest.REMOVE_INTERFACE_RQST:
						replies.addElement(execRemoveInterface(r.params[0], r.params[1]));
						break;

					case DBRequest.ASSIGN_INTERFACE_RQST:
						replies.addElement(execAssignInterface(r.params[0], r.params[1], r.params[2]));
						break;

					case DBRequest.UNASSIGN_INTERFACE_RQST:
						replies.addElement(execUnassignInterface(r.params[0], r.params[1], r.params[2]));
						break;

					case DBRequest.DUMP_STORE_RQST:
						replies.addElement(execDumpStore());
						break;

					case DBRequest.DUMP_MEMORY_RQST:
						replies.addElement(execDumpMemory(r.params[0]));
						break;

					case DBRequest.DISASSEMBLE_RQST:
						replies.addElement(execDisassembleProcedure(r.params[0], r.params[1]));
						break;

					case DBRequest.EXECUTE_SBQL_RQST:
						replies.addElement(execSBQL(r.params[0], r.params[1], r.params[2], r.params[3], false));
						break;

					case DBRequest.EXECUTE_REMOTE_SBQL_RQST:
						replies.addElement(execRemoteSBQL(r.params[0], r.params[1], r.params[2], r.params[3], r.params[4],r.params[5] ));
						break;

					case DBRequest.EXECUTE_OCL_RQST:
						replies.addElement(execOCL(r.params[0], r.params[1], r.params[2], r.params[3]));
						break;

					case DBRequest.COMPILE_RQST:
						replies.addElement(execCompileModule(r.params[0]));
						break;

					case DBRequest.EXISTS_MODULE_RQST:
						replies.addElement(execExistsModule(r.params[0]));
						break;

					case DBRequest.ADD_USER_RQST:
						replies.addElement(execCreateUser(r.params[0], r.params[1]));
						break;

					case DBRequest.LIST_RQST:
						replies.addElement(execLs(r.params[0]));
						break;

					case DBRequest.LOGIN_RQST:
						replies.addElement(execLogin(r.params[0], r.params[1]));
						break;

					case DBRequest.LOAD_DATA_RQST:
						replies.addElement(execLoadData(r.params[0], r.params[1], r.params[2], r.params[3]));
						break;

					case DBRequest.ADD_INDEX_RQST:
					case DBRequest.ADD_TMPINDEX_RQST:
						replies.addElement(execAddIndex(r.opcode == DBRequest.ADD_TMPINDEX_RQST, r.params));
						break;

					case DBRequest.REMOVE_INDEX_RQST:
						replies.addElement(execRemoveIndex(r.params[0], r.params[1]));
						break;

					case DBRequest.ADD_ENDPOINT_RQST:
						replies.addElement(execAddEndpoint(r.params));
						break;

					case DBRequest.REMOVE_ENDPOINT_RQST:
						replies.addElement(execRemoveEndpoint(r.params[0], r.params[1]));
						break;

					case DBRequest.SUSPEND_ENDPOINT_RQST:
						replies.addElement(execSuspendEndpoint(r.params[0], r.params[1]));
						break;

					case DBRequest.RESUME_ENDPOINT_RQST:
						replies.addElement(execResumeEndpoint(r.params[0], r.params[1]));
						break;

					case DBRequest.ADD_PROXY_RQST:
						replies.addElement(execAddModuleAsProxy(r.params[0], r.params[1], r.params[2]));
						break;

					case DBRequest.REMOVE_PROXY_RQST:
						replies.addElement(execRemoveProxy(r.params[0], r.params[1]));
						break;

					case DBRequest.PROMOTE_TO_PROXY_RQST:
						replies.addElement(execPromoteToProxy(r.params[0], r.params[1], r.params[2], r.params[3], r.params[4]));
						break;

					case DBRequest.ADD_LINK_RQST:
						replies.addElement(execAddLink(r.params[0], r.params[1], r.params[2], r.params[3], r.params[4], r.params[5]));
						break;
					case DBRequest.REMOVE_LINK_RQST:
						replies.addElement(execRemovelink(r.params[0], r.params[1]));
						break;
					case DBRequest.ADD_LINK_TO_SCHEMA_RQST:
						replies.addElement(execAddLinkToSchema(r.params[0], r.params[1], r.params[2]));
						break;
					case DBRequest.REMOVE_LINK_FROM_SCHEMA_RQST:
						replies.addElement(execRemoveLinkFromSchema(r.params[0], r.params[1], r.params[2]));
						break;
					case DBRequest.ADD_GRIDLINK_RQST:
						replies.addElement(execAddGridLink(r.params[0], r.params[1], r.params[2], r.params[3], r.params[4]));
						break;
					case DBRequest.REMOVE_GRIDLINK_RQST:
						replies.addElement(execRemoveGridlink(r.params[0], r.params[1]));
						break;
					case DBRequest.JOINTOGRID_RQST:
						replies.addElement(execJoinToGrid(r.params[0], r.params[1]));
						break;
					case DBRequest.REMOVEFROMGRID_RQST:
						replies.addElement(execRemoveFromGrid(r.params[0], r.params[1]));
						break;
					case DBRequest.EXECUTE_REMOTE_COMMAND_RQST:
						replies.addElement(execExecuteRemoteCommand(r.params[0]));
						break;
					case DBRequest.CONNECTTOGRID_RQST:
						replies.addElement(execConnectToGrid(r.params[0], r.params[1], r.params[2], r.params[3], r.params[4]));
						break;

					case DBRequest.ALTER_PROCEDURE_BODY_RQST:
						replies.addElement(execAlterProcedureBody(r.params[0], r.params[1], r.params[2]));
						break;

					case DBRequest.REMOTE_GLOBAL_BIND_RQST:
						replies.addElement(execRemoteGlobalBind(r.params[0], r.params[1]));
						break;



					// remote store requests
					case DBRequest.IS_INTEGER_OBJECT_RQST:
						replies.addElement(execIsIntegerObject(r.params[0]));
						break;

					case DBRequest.IS_AGGREGATE_OBJECT_RQST:
						replies.addElement(execIsAggregateObject(r.params[0]));
						break;

					case DBRequest.IS_BINARY_OBJECT_RQST:
						replies.addElement(execIsBinaryObject(r.params[0]));
						break;

					case DBRequest.IS_BOOLEAN_OBJECT_RQST:
						replies.addElement(execIsBooleanObject(r.params[0]));
						break;

					case DBRequest.IS_COMPLEX_OBJECT_RQST:
						replies.addElement(execIsComplexObject(r.params[0]));
						break;

					case DBRequest.IS_DOUBLE_OBJECT_RQST:
						replies.addElement(execIsDoubleObject(r.params[0]));
						break;

					case DBRequest.IS_REFERENCE_OBJECT_RQST:
						replies.addElement(execIsReferenceObject(r.params[0]));
						break;

					case DBRequest.IS_STRING_OBJECT_RQST:
						replies.addElement(execIsStringObject(r.params[0]));
						break;

					case DBRequest.IS_DATE_OBJECT_RQST:
						replies.addElement(execIsDateObject(r.params[0]));
						break;

					case DBRequest.GET_NAME_RQST:
						replies.addElement(execGetName(r.params[0]));
						break;

					case DBRequest.UPDATE_INTEGER_OBJECT_RQST:
						replies.addElement(execUpdateIntegerObject(r.params[0], r.params[1]));
						break;

					case DBRequest.UPDATE_BINARY_OBJECT_RQST:
						replies.addElement(execUpdateBinaryObject(r.params[0], r.params[1]));
						break;

					case DBRequest.UPDATE_BOOLEAN_OBJECT_RQST:
						replies.addElement(execUpdateBooleanObject(r.params[0], r.params[1]));
						break;

					case DBRequest.UPDATE_DOUBLE_OBJECT_RQST:
						replies.addElement(execUpdateDoubleObject(r.params[0], r.params[1]));
						break;

					case DBRequest.UPDATE_REFERENCE_OBJECT_RQST:
						replies.addElement(execUpdateReferenceObject(r.params[0], r.params[1]));
						break;

					case DBRequest.UPDATE_STRING_OBJECT_RQST:
						replies.addElement(execUpdateStringObject(r.params[0], r.params[1]));
						break;

					case DBRequest.UPDATE_DATE_OBJECT_RQST:
						replies.addElement(execUpdateDateObject(r.params[0], r.params[1]));
						break;

					case DBRequest.EXPLAIN_OPTIMIZATION_RQST:
						replies.addElement(this.execExplainOptimization(r.params));
						break;
					case DBRequest.EXPLAIN_JULIETCODE_RQST:
						replies.addElement(this.execExplainJuliet(r.params));
						break;
					case DBRequest.EXPLAIN_TYPECHECKER_RQST:
					    	replies.addElement(this.execExplainTypechecker(r.params));
						break;
					case DBRequest.EXPLAIN_PROCEDURE_RQST:
						replies.addElement(this.execGetProcedureBody(r.params[0], r.params[1], r.params[2]));
						break;
					case DBRequest.SHOW_OPTIMIZATION_RQST:
						replies.addElement(this.execShowOptimization(Boolean.valueOf(r.params[0])));
						break;
					case DBRequest.SET_OPTIMIZATION_RQST:
						replies.addElement(this.execSetOptimization(r.params));
						break;

					case DBRequest.DEREF_INTEGER_OBJECT_RQST:
						replies.addElement(execDerefIntegerObject(r.params[0]));
						break;
					case DBRequest.DEREF_STRING_OBJECT_RQST:
						replies.addElement(execDerefStringObject(r.params[0]));
						break;
					case DBRequest.DEREF_REFERENCE_OBJECT_RQST:
						replies.addElement(execDerefReferenceObject(r.params[0]));
						break;
					case DBRequest.DEREF_BOOLEAN_OBJECT_RQST:
						replies.addElement(execDerefBooleanObject(r.params[0]));
						break;
					case DBRequest.DEREF_DOUBLE_OBJECT_RQST:
						replies.addElement(execDerefDoubleObject(r.params[0]));
						break;
					case DBRequest.DEREF_DATE_OBJECT_RQST:
						replies.addElement(execDerefDateObject(r.params[0]));
						break;
					case DBRequest.DEREF_COMPLEX_OBJECT_RQST:
						replies.addElement(execDerefComplexObject(r.params[0]));
						break;

					case DBRequest.REMOTE_PROCEDURE_CALL_RQST:
						replies.addElement(execRemoteProcedureCall(r.params[0], r.params[1], r.params[2]));//module, procrefernce, stack
						break;

					case DBRequest.GET_METADATA_RQST:
						replies.addElement( execGetMetadata(r.params[0]) );
						break;
					case DBRequest.COUNT_CHILDREN_RQST:
						replies.addElement( execCountChildren(r.params[0]));
						break;
					case DBRequest.GET_CHILD_AT:
						replies.addElement( execGetChildAt(r.params[0], r.params[1]));
						break;

					//heap structure requests
					case DBRequest.HEAP_STRUCTURE_INIT_RQST:
						replies.addElement(execHeapStructurePersistentInit(Boolean.valueOf(r.params[0])));
						break;
					case DBRequest.HEAP_STRUCTURE_FRAGMENT_DATA_RQST:
						replies.addElement(execHeapStructureFragmentData(Integer.parseInt(r.params[0]), Integer.parseInt(r.params[1])));
						break;
					case DBRequest.HEAP_STRUCTURE_FRAGMENT_TYPES_RQST:
						replies.addElement(execHeapStructureFragmentTypes(Integer.parseInt(r.params[0]), Integer.parseInt(r.params[1])));
						break;

					//add wrapper module
					case DBRequest.ADD_MODULE_AS_WRAPPER_RQST:
						replies.addElement(execAddModuleAsWrapper(r.params[0], r.params[1], Integer.parseInt(r.params[2]), Integer.parseInt(r.params[3]), r.params[4]));
						break;
					case DBRequest.ADD_VIEW_RQST:
						replies.addElement(execAddView(r.params[0], r.params[1]));
						break;
					case DBRequest.REMOVE_VIEW_RQST:
						replies.addElement(execDropView(r.params[0], r.params[1]));
						break;

					//momory monitor
					case DBRequest.MEMORY_GET_RQST:
						replies.addElement(execMemoryGet());
						break;
					case DBRequest.MEMORY_GC_RQST:
						replies.addElement(execMemoryGc());
						break;
					case DBRequest.REFRESH_LINK_RQST:
						replies.addElement( execRefreshLink(r.params[0], r.params[1]) );
						break;
					case DBRequest.VALIDATE_METABASE_SERIAL_RQST:
						replies.addElement( execValidateMetaBaseSerial(r.params[0], r.params[1]) );
						break;

					case DBRequest.PARSE_ONLY_RQST:
						replies.addElement(this.execParseOnly(r.params));
						break;
					case DBRequest.TYPECHECK_ONLY_RQST:
						replies.addElement(this.execTypecheckOnly(r.params));
						break;
					case DBRequest.OPTIMIZE_ONLY_RQST:
						replies.addElement(this.execOptimizeOnly(r.params));
						break;

					case DBRequest.WHATIS_RQST:
						replies.addElement(this.execWhatis(r.params[1], r.params[0]));
						break;
					case DBRequest.EXISTS_RQST:
						replies.addElement(this.execExists(r.params[2], r.params[0], r.params[1]));
						break;
					case DBRequest.EXEC_META_RQST:
						replies.addElement(this.execMeta(r.params[0], r.params[1]));
						break;

					case DBRequest.SET_CLIENT_NAME:
						replies.addElement(this.execSetClientName(r.params[0]));
						break;
					case DBRequest.ADD_ROLE_RQST:
						replies.addElement(execAddRole(r.params[0]));
						break;

					case DBRequest.GRANT_PRIVILEGE_RQST:
						replies.addElement(execGrantPrivilege(r.params));
						break;
						
					case DBRequest.DEFRAGMENT_RQST:
						replies.addElement( execDefragment() );
						break;
					default:
						replies.addElement(DBReply.createERRReply("Invalid request", DBReply.STAT_NETWORK_ERROR));
						break;
				}
			}
			catch (InterpreterException ex) {
				if(ConfigServer.DEBUG_EXCEPTIONS)
					ex.printStackTrace();

				try {
					byte[] data = encodeSourcePositionBag(ex);
					replies.addElement(DBReply.createERRReply(ex.getMessage(), data, DBReply.STAT_RUNTIME_ERROR));
				}
				catch (RDNetworkException e) {
					ConfigServer.getLogWriter().getLogger().log(java.util.logging.Level.WARNING, "Exception during SVRP command execution", ex);
					replies.addElement(DBReply.createERRReply("Protocol error : " + ex.getMessage(), DBReply.STAT_NETWORK_ERROR));
				}
			}
			catch(OptimizationException ex) {
				if(ConfigServer.DEBUG_EXCEPTIONS)
					ex.printStackTrace();
				try{
        				byte[] data = encodeSourcePositionBag(ex);
        				replies.addElement(DBReply.createERRReply(ex.getMessage(), data, DBReply.STAT_OPTMIZATION_ERROR));
				}catch (RDNetworkException e) {
					ConfigServer.getLogWriter().getLogger().log(java.util.logging.Level.WARNING, "Exception during SVRP command execution", ex);
					replies.addElement(DBReply.createERRReply("Protocol error : " + ex.getMessage(), DBReply.STAT_NETWORK_ERROR));
				}
			}
			catch (IndicesException ex) {
				if(ConfigServer.DEBUG_EXCEPTIONS)
					ex.printStackTrace();

				replies.addElement(DBReply.createERRReply(ex.getMessage(), DBReply.STAT_INTERNAL_ERROR));
			}
			catch (SBQLException ex) {
				if(ConfigServer.DEBUG_EXCEPTIONS)
					ex.printStackTrace();

				try {
					byte[] data = encodeSourcePositionBag(ex);
					replies.addElement(DBReply.createERRReply("" + ex.getMessage(), data, DBReply.STAT_COMPILATION_ERROR));
				}
				catch (RDNetworkException e) {
					ConfigServer.getLogWriter().getLogger().log(java.util.logging.Level.WARNING, "Exception during SVRP command execution", ex);
					replies.addElement(DBReply.createERRReply("Protocol error : " + ex.getMessage(), DBReply.STAT_NETWORK_ERROR));
				}
			}

			catch (AuthenticationException ex) {
				if(ConfigServer.DEBUG_EXCEPTIONS)
					ex.printStackTrace();

				replies.addElement(DBReply.createERRReply(ex.getMessage(), DBReply.STAT_SECURITY_ERROR));
			}
			catch (DatabaseException ex) {
				if(ConfigServer.DEBUG_EXCEPTIONS)
					ex.printStackTrace();

				replies.addElement(DBReply.createERRReply(ex.getMessage(), DBReply.STAT_DATABASE_ERROR));
			}
			catch (FilterException ex) {
				if(ConfigServer.DEBUG_EXCEPTIONS)
					ex.printStackTrace();

				replies.addElement(DBReply.createERRReply(ex.getMessage(), DBReply.STAT_INTERNAL_ERROR));
			}
			catch(WrapperException ex) {
				if(ConfigServer.DEBUG_EXCEPTIONS)
					ex.printStackTrace();

				replies.addElement(DBReply.createERRReply(ex.getMessage(), DBReply.STAT_WRAPPER_ERROR));
			}
			catch (ArrayIndexOutOfBoundsException ex) {
				if(ConfigServer.DEBUG_EXCEPTIONS)
					ex.printStackTrace();

				replies.addElement(DBReply.createERRReply("Invalid request parameters (" + ex.getMessage() + ")", DBReply.STAT_NETWORK_ERROR));
			}
			catch (WSProxyException ex)
			{
				if (ConfigServer.DEBUG_EXCEPTIONS)
					ex.printStackTrace();

				replies.addElement(DBReply.createERRReply(ex.getMessage(), DBReply.STAT_PROXY_ERROR));
			}
			catch (WSEndpointException ex)
			{
				if (ConfigServer.DEBUG_EXCEPTIONS)
					ex.printStackTrace();

				replies.addElement(DBReply.createERRReply(ex.getMessage(), DBReply.STAT_PROXY_ERROR));
			}
			catch (Exception ex) {
				if(ConfigServer.DEBUG_EXCEPTIONS)
					ex.printStackTrace();

				replies.addElement(DBReply.createERRReply("Unknown error : " + ex.getMessage(), DBReply.STAT_INTERNAL_ERROR));

				ConfigServer.getLogWriter().getLogger().log(java.util.logging.Level.WARNING, "Exception during SVRP command execution", ex);
			}
		}

		return  replies.toArray(new DBReply[replies.size()]);
	}

	private DBReply execRemoteSBQL(String prog, String modname, String derefMode, String testMode, String rawStack, String rawSignatures)
			throws Exception
	{

		DBModule mod = Database.getModuleByName(modname);
		if (!mod.isModuleLinked())
			linker.linkModule(mod);
		if (!mod.isModuleCompiled())
			compiler.compileModule(mod);

		// decode parameters
		SBQLStackDecoder decoder = new SBQLStackDecoder(new RemoteQueryParameterDecoder());
		Vector<AbstractQueryResult> stack = decoder.decodeStack(rawStack.getBytes());
		Session.getCurrent().setRemoteResultsStack(stack);

		// decode signatures
		SBQLPrimitiveSignatureDecoder signDecoder = new SBQLPrimitiveSignatureDecoder();
		ArrayList<SignatureInfo>  parmsigantures = signDecoder.decode(rawSignatures.getBytes());
		Session.getCurrent().setParmSigantures(parmsigantures);

		return execSBQL(prog, modname, derefMode, testMode, true);
	}


	private DBReply execValidateMetaBaseSerial(String module, String rserial) throws DatabaseException
	{
		DBModule mod = Database.getModuleByName(module);
		Long serial = new Long(rserial);

		if (serial.compareTo(mod.getSerial()) == 0)
			return DBReply.createOKReply();

		return DBReply.createERRReply("Stale MetaBase", DBReply.STAT_STALE_METABASE_ERROR);
	}

	private final DBReply execRemoteDereference(String encrmtref) throws DatabaseException, RDNetworkException {
		byte[] rawencref = encrmtref.getBytes();

		ReferenceResult rres = (ReferenceResult) qresDecoder.decodeResult(rawencref);
		RemoteDefaultStoreOID roid = (RemoteDefaultStoreOID) rres.value;

		ReferenceResult lres = new ReferenceResult(new DefaultStoreOID(roid.internalOID(), (DefaultStore) Database.getStore()));

		// ???

		byte[] rawres = qresEncoder.encodeResult(lres);

		return DBReply.createOKReply(rawres);
	}

	private final DBReply execRemoteGlobalBind(String modname, String bndname) throws DatabaseException, RDNetworkException {
		BagResult bag = new BagResult();

		DBModule mod = Database.getModuleByName(modname);

		OID found = mod.findFirstByName(bndname, mod.getDatabaseEntry());

		if (found != null) {
			if (found.isAggregateObject()) {
				OID[] objs = found.derefComplex();

				for (OID oid : objs)
					bag.addElement(new ReferenceResult(oid));
			}
			else
				bag.addElement(new ReferenceResult(found));
		}

		byte[] rawres = qresEncoder.encodeResult(bag.elementsCount() == 1 ? bag.elementAt(0) : bag);

		return DBReply.createOKReply(rawres);
	}

	/**
	 * Called to autenthicate a user.
	 * @param user name of the user
	 * @param password password of the user
	 * @return DBReply object with OK status, sent to the client
	 */

	private final DBReply execLogin(String user, String password) throws AuthenticationException, DatabaseException {
		Session.initialize(user, password);
		initSBQL();

		this.setName("svrp-" + user);

		return DBReply.createOKReply();
	}


	private DBReply execDefragment() throws DatabaseException, RDNetworkException {
		StringResult strres = new StringResult(((DefaultStore) Database.getStore()).staticDefragmentation());
		
		return DBReply.createOKReply(qresEncoder.encodeResult(strres));
	}
	
	/**
	 * Prints the tree of objects (for debugging purposes)
	 * @return DBReply object, with a single StringResult, sent to the client
	 */
	private final DBReply execDumpStore() throws DatabaseException, RDNetworkException {
		StringResult strres = new StringResult(Database.getStore().dump());

		return DBReply.createOKReply(qresEncoder.encodeResult(strres));
	}

	/**
	 * Shows the heap with its allocation blocks and their statuses
	 * @return
	 */
	private final DBReply execDumpMemory(String verbose) throws DatabaseException, RDNetworkException {
		StringResult strres = new StringResult(Database.getStore().dumpMemory(Boolean.parseBoolean(verbose)));

		return DBReply.createOKReply(qresEncoder.encodeResult(strres));
	}

	/**
	 * Prints the content of a module (for debugging purposes)
	 * @param modname global name of a module
	 * @return DBReply object, with a single StringResult, sent to the client
	 */
	private final DBReply execDumpModule(String modname) throws DatabaseException, RDNetworkException {
		DBModule mod = Database.getModuleByName(modname);

		ModuleDumper dumper = new ModuleDumper(mod);
		StringResult strres = new StringResult(dumper.dump());

		return DBReply.createOKReply(qresEncoder.encodeResult(strres));
	}
	
	/**
	 * Called when module existence should be checked
	 * @param modname global name of a module
	 * @return DBReply object sent to the client
	 */
	private final DBReply execExistsModule(String modname) throws DatabaseException {
		Database.getModuleByName(modname);

		return DBReply.createOKReply();
	}

	/**
	 * Deletes a module from the database. Modules that import the module being deleted,
	 * are invalidated (need recompilation)
	 * @param modname global name of a module
	 * @return DBReply object sent to the client
	 */
	private final DBReply execDropModule(String modname) throws DatabaseException, WSProxyException {
		DBModule mod = Database.getModuleByName(modname);

		// cleanup meta information if module is web service proxy
		if(ConfigServer.WS)
		{
			IProxyFacade pm = WSManagersFactory.createProxyManager();

			if (pm == null) {
				return DBReply.createERRReply("Web service proxy manager driver not found. ");
			}

			if (pm.isProxy(mod.getOID())) {
				pm.removeProxy(mod.getOID());
			} else {
				OID[] imodoids = mod.getOID().getReferencesPointingAt();

				for (OID oid : imodoids) {
					new DBModule(oid.getParent().getParent()).setModuleCompiled(false);
					new DBModule(oid.getParent().getParent()).setModuleLinked(false);
				}
			}
		}

		for (OID submodule: mod.getSubmodules())
			execDropModule(modname + "." + submodule.getObjectName());

		mod.deleteModule();

		return DBReply.createOKReply();
	}

	/**
	 * Links and compiles a module.
	 * @param modname global name of the module that should be compiled
	 */
	private final DBReply execCompileModule(String modname) throws DatabaseException, WSProxyException {
		DBModule mod = Database.getModuleByName(modname);

		IProxyFacade pm = WSManagersFactory.createProxyManager();

		if ((pm != null) && (pm.isProxy(mod.getOID())))
		{
			return DBReply.createERRReply("Proxy modules can not be compiled");
		}

		linker.linkModule(mod);
		compiler.compileModule(mod);

		return DBReply.createOKReply();
	}

	/**
	 * Creates a new user account. It constructs a new user module
	 * and records user's name and password
	 * @param user name of the account
	 * @param password password of the user
	 */
	private final DBReply execCreateUser(String user, String password) throws DatabaseException {
		Database.getSystemModule().createSubmodule(user);

		AccountManager.registerUserAccount(user, password);

		return DBReply.createOKReply();
	}

	/**
	 * Lists runtime database objects belonging to the module given as a parameter.
	 * The list takes the form of pairs <object_kind_name, object_name>.
	 * The reply is encoded as a bag of such pairs (structures).
	 * @param modname global name of a module
	 * @return DBReply object containing a bag of 2-element structures
	 */
	private final DBReply execLs(String modname) throws DatabaseException, RDNetworkException {
		DBModule mod = Database.getModuleByName(modname);

		BagResult bag = new BagResult();

		// list submodules
		OID[] submods = mod.getSubmodules();
		for (int i = 0; i < submods.length; i++) {
			DBModule m = new DBModule(submods[i]);

			StringResult objres = new StringResult("M");
			StringResult nameres = new StringResult(m.getName() + (m.isModuleCompiled() ? "" : "*"));
			StructResult strres = new StructResult(objres, nameres);

			bag.addElement(strres);
		}

		// list other runtime objects
		OID[] children = mod.getDatabaseEntry().derefComplex();

		for (int i = 0; i < children.length; i++) {
			StringResult objres = new StringResult(new DBObject(children[i]).getObjectKind().getKindAsString());
			StringResult nameres = new StringResult(children[i].getObjectName());
			StructResult strres = new StructResult(objres, nameres);

			bag.addElement(strres);
		}

		// create a reply object
		byte[] strarr = qresEncoder.encodeResult(bag);

		return DBReply.createOKReply(strarr);
	}

	private final DBReply execAddInterface(String src, String parmod) throws Exception {
		System.out.println("add interface " + src + " " + parmod);

		DBModule mod = Database.getModuleByName(parmod);

		BagResult bag = new BagResult();
		bag.addElement(new StringResult(parmod)); // global name of the interface

		byte[] rawres = qresEncoder.encodeResult(bag);

		return DBReply.createOKReply(rawres);
	}

	private final DBReply execRemoveInterface(String src, String parmod) throws Exception {
		return null;
	}

	private final DBReply execAssignInterface(String iname, String oname, String parmod) throws Exception {
		return null;
	}

	private final DBReply execUnassignInterface(String iname, String oname, String parmod) throws Exception {
		return null;
	}

	/**
	 * Creates a new module in the database
	 * @param src source of the module
	 * @param parmod name of the parent module
	 * @return object with OK status, sent to the client
	 */
	private final DBReply execAddModule(String src, String parmod) throws Exception {
		DBModule mod = Database.getModuleByName(parmod);
		ModuleConstructor constructor = new ModuleConstructor(mod);

		ASTNode node = BuilderUtils.parseSBQL(parmod, src);

		// TODO type checking
		node.accept(constructor, null);
		// return the name of the new module
		String glbname = constructor.getConstructedModule().getModuleGlobalName();
		Session.removeModuleFromSession(glbname);
		BagResult bag = new BagResult();
		bag.addElement(new StringResult(glbname));

		byte[] rawres = qresEncoder.encodeResult(bag);

		return DBReply.createOKReply(rawres);
	}

	/**
	 * Performs typcchecking on a node, provided <code>ConfigServer.TYPECHECKING == true</code>. Otherwise,
	 * an original node is returned.
	 * @param module module
	 * @param node <code>ASTNode</code>
	 * @return typechecked node
	 * @throws DatabaseException
	 */
	private final ASTNode doTypechecking(DBModule module, ASTNode node, boolean isParmDependent) throws DatabaseException
	{
		if(ConfigServer.TYPECHECKING)
		{
		    SBQLTypeChecker checker = new SBQLTypeChecker(module,isParmDependent);
			node = checker.typecheckAdHocQuery(node);
			//System.out.println ( new ASTLinksDumper().dumpAST(node) );
//			System.out.println(((Expression) node).sign.dump(""));
		}

		return node;
	}

	private final ASTNode doTypechecking(DBModule module, ASTNode node) throws DatabaseException
	{
		return this.doTypechecking(module, node,  false);
	}

	/**
	 * Performs typechecking on a node, provided <code>ConfigServer.TYPECHECKING == true</code>. Otherwise,
	 * an original node is returned.
	 * @param module module
	 * @param node <code>ASTNode</code>
	 * @return typechecked node
	 * @throws DatabaseException
	 */
	private final ASTNode doOCLTypechecking(DBModule module, ASTNode node) throws DatabaseException
	{
		if(ConfigServer.TYPECHECKING)
		{
		    SBQLTypeChecker checker = new SBQLProcedureTypeChecker(module);
			node = checker.typecheckAdHocQuery(node);
//			System.out.println(((Expression) node).sign.dump(""));
		}

		return node;
	}

	/**
	 * Performs optimization on a node, provided <code>ConfigServer.TYPECHECKING == true</code>. Otherwise,
	 * an original node is returned. Also, no optimization is performed if a session optimizer sequence is empty.
	 * <br />
	 * The method should be called after the {@link #checker} initialization.
	 *
	 * @param module module
	 * @param node <code>ASTNode</code>
	 * @param optimizationSequence {@link OptimizationSequence} to be applied
	 * @return optimized node
	 * @throws Exception
	 */
	private final ASTNode doOptimization(DBModule module, ASTNode node, OptimizationSequence optimizationSequence) throws Exception
	{
		ASTNode copy = node;

		if(ConfigServer.TYPECHECKING)
		{
			OptimizationFramework optimizationFramework = new OptimizationFramework(new SBQLTypeChecker(module));
			optimizationFramework.setOptimizationSequence(optimizationSequence);
			copy = optimizationFramework.optimize(node, module);
		}

		return copy;
	}

	private final ASTNode doFinalDereference(DBModule mod, ASTNode node) throws Exception{
	    if(ConfigServer.TYPECHECKING){
		assert node instanceof Expression: "expression";
		return new SBQLTypeChecker(mod).performFinalDereference((Expression)node, true);

	    }
	    return node;
	}

	private final ASTNode doFinalOCLDereference(DBModule mod, ASTNode node) throws Exception{
	    if(ConfigServer.TYPECHECKING){
		if(node instanceof ExpressionStatement){
		    ExpressionStatement stmt = (ExpressionStatement)node;
		    stmt.setExpression(new SBQLTypeChecker(mod).performFinalDereference(((ExpressionStatement)node).getExpression(),false));
		    return stmt;
		}
	    }
	    return node;
	}

	/**
	 * Evaluates a node.
	 *
	 * @param module module
	 * @param node <code>ASTNode</code>
	 * @param deref dereference result?
	 * @return result
	 * @throws Exception
	 */
	private final Result doExecution(DBModule module, ASTNode node, boolean deref, boolean isParmDependent) throws Exception
	{
	    	IJulietCodeGenerator generator = EmiterFactory.getJulietCodeGenerator(module);
		//IJulietCodeGenerator generator = EmiterFactory.getJulietCodeGeneratorRestoringSubstitutedViews(module);
		SBQLInterpreter interpreter = new SBQLInterpreter(module);
		generator.generate(node);

		// appends deref to the query result (if requested)
		JulietCode code = generator.getCode();
		code.emit(OpCodes.commitStmt);
		byte[] byteCode;
		if(ConfigServer.TYPECHECKING) {
		    byteCode = code.getByteCode();
		}
		else
		    byteCode = deref ? JulietGen.genDynDeref(code).getByteCode() : code.getByteCode();
		byte[] cnstPool = generator.getConstantPool().getAsBytes();

		if (isParmDependent)
			interpreter.injectRemoteResults();

		interpreter.runCode(byteCode, cnstPool);

		Result res = interpreter.getResult();
		assert !interpreter.hasResult() : "inconsistent result stack";
		return res;
	}

	private final Result doExecution(DBModule module, ASTNode node, boolean deref) throws Exception
	{
		return this.doExecution(module, node, deref, false);
	}

	/**
	 * Evaluates a node.
	 *
	 * @param module module
	 * @param node <code>ASTNode</code>
	 * @param deref dereference result?
	 * @return result
	 * @throws Exception
	 */
	private final Result doOCLExecution(DBModule module, ASTNode node, boolean deref) throws Exception
	{
	    	IJulietCodeGenerator generator = EmiterFactory.getJulietCodeGenerator(module);
		SBQLInterpreter interpreter = new SBQLInterpreter(module);

		if (node instanceof ExpressionStatement)
			node = ((ExpressionStatement) node).getExpression();
		generator.generate(node);

		// appends deref to the query result (if requested)
		JulietCode code = generator.getCode();
		byte[] byteCode = (deref && node instanceof Expression) ? JulietGen.genDynDeref(code).getByteCode() : code.getByteCode();
		byte[] cnstPool = generator.getConstantPool().getAsBytes();

		interpreter.runCode(byteCode, cnstPool);

		return interpreter.hasResult()? interpreter.getResult() : new BagResult();
	}

	/**
	 * Test an sbql ad-hoc query for optimization.
	 * <br />
	 * An evaluation is the same as in case of a regular evaluation, but additional times are measured and
	 * prepended to a result of the original query (a final result os a <code>BagResult</code> composed of
	 * a <code>StringReault</code> with the times and the original result).
	 *
	 * @param prog sbql source code to be executed
	 * @param modname global name of a module which should be used as a context of the execution
	 * @param deref dereference result (on/off)
	 * @param onlytimes display only evaluation times (on/off)
	 * @return result
	 * @throws Exception
	 *
	 * @author jacenty
	 */
	private final Result benchmarkSBQL(DBModule module, ASTNode node, boolean deref, boolean onlytimes) throws Exception
	{
		long typecheckingStart = System.nanoTime();
		node = doTypechecking(module, node);
		long typecheckingStop = System.nanoTime();

		long optimizationStart = System.nanoTime();
		node = doOptimization(module, node, Session.getCurrent().getOptimizationSequence());
		long optimizationStop = System.nanoTime();

		long executionStart = System.nanoTime();
		Result result = doExecution(module, node, deref);
		long executionStop = System.nanoTime();

		long typechecking = typecheckingStop - typecheckingStart;
		long optimization = optimizationStop - optimizationStart;
		long execution = executionStop - executionStart;
					
		int nanoToMili = 1000000;
		typechecking = typechecking / nanoToMili;
		optimization = optimization / nanoToMili;
		execution = execution / nanoToMili;
		
		BagResult testResult = new BagResult();
		if (!onlytimes) testResult.addAll(result);
		testResult.addElement(new BinderResult(Benchmark.TIME_TYPECHECKING, new IntegerResult((int) typechecking)));
		testResult.addElement(new BinderResult(Benchmark.TIME_OPTIMIZED_OPTIMIZATION, new IntegerResult((int) optimization)));
		testResult.addElement(new BinderResult(Benchmark.TIME_OPTIMIZED_EXECUTION, new IntegerResult((int) execution)));
		
		return testResult;
	}

	private final DBReply compareTestSBQL(DBModule module, ASTNode node, String query, String testMode) throws Exception
	{

		int memoryUsedInitial = 0;
		int memoryUsedUnoptimized = 0;
		int memoryUsedOptimized = 0;

		DefaultStore store = null;
		if(module.isWrapper())
			store = module.getWrapper().getStore(Session.getCurrent()).getTransientStore();
		if(store != null)
			memoryUsedInitial = store.getUsedMemory();
		
		ASTNode unOptimizedNode = DeepCopyAST.copyWithoutSign(node);
		boolean simple = testMode.equals("comparesimple");

		long referenceStart = System.nanoTime();
		unOptimizedNode = doTypechecking(module, unOptimizedNode);

		unOptimizedNode = doOptimization(module, unOptimizedNode, Session.getCurrent().getOptimizationReferenceSequence());

		Result result = doExecution(module, unOptimizedNode, simple);
		long referenceStop = System.nanoTime();

		if(store != null)
			memoryUsedUnoptimized = store.getUsedMemory();

		long typecheckingStart = System.nanoTime();
		node = doTypechecking(module, node, false);
		long typecheckingStop = System.nanoTime();

		long optimizationStart = System.nanoTime();
		node = doOptimization(module, node, Session.getCurrent().getOptimizationSequence());
		long optimizationStop = System.nanoTime();

		long optimizedExecutionStart = System.nanoTime();
		Result optResult = doExecution(module, node, simple);
		long optimizedExecutionStop = System.nanoTime();
		if(store != null)
			memoryUsedOptimized = store.getUsedMemory();

		long typechecking = typecheckingStop - typecheckingStart;
		long optimization = optimizationStop - optimizationStart;
		long reference = referenceStop - referenceStart;
		long optimizedExecution = optimizedExecutionStop - optimizedExecutionStart;

		int nanoToMili = 1000000;
		typechecking = typechecking / nanoToMili;
		optimization = optimization / nanoToMili;
		reference = reference / nanoToMili;
		optimizedExecution = optimizedExecution / nanoToMili;
		
		String unoptquery = new AST2TextQueryDumper().dumpAST(unOptimizedNode);
		String optquery = new AST2TextQueryDumper().dumpAST(node);

		BagResult testResult = new BagResult();
		testResult.addElement(new BinderResult(Benchmark.TEST_MODE, new StringResult(testMode)));
		testResult.addElement(new BinderResult(Benchmark.OPTIMIZATION_SEQUENCE_REFERENCE, new StringResult(Session.getCurrent().getOptimizationReferenceSequence().toString())));
		testResult.addElement(new BinderResult(Benchmark.OPTIMIZATION_SEQUENCE_APPLIED, new StringResult(Session.getCurrent().getOptimizationSequence().toString())));
		testResult.addElement(new BinderResult(Benchmark.QUERY_RAW, new StringResult(query)));
		testResult.addElement(new BinderResult(Benchmark.QUERY_REFERENCE, new StringResult(unoptquery)));
		testResult.addElement(new BinderResult(Benchmark.QUERY_OPTIMIZED, new StringResult(optquery)));
		testResult.addElement(new BinderResult(Benchmark.TIME_TYPECHECKING, new IntegerResult((int)typechecking)));
		testResult.addElement(new BinderResult(Benchmark.TIME_REFERENCE, new IntegerResult((int)reference)));
		testResult.addElement(new BinderResult(Benchmark.TIME_OPTIMIZED, new IntegerResult((int)(optimization + optimizedExecution))));
		testResult.addElement(new BinderResult(Benchmark.TIME_OPTIMIZED_OPTIMIZATION, new IntegerResult((int)optimization)));
		testResult.addElement(new BinderResult(Benchmark.TIME_OPTIMIZED_EXECUTION, new IntegerResult((int)optimizedExecution)));

		double ratio = ((double)reference) / ((double) (typechecking + optimization + optimizedExecution));
		double percentage = (Math.rint(((ratio - 1) * 10000)) / 100);

		String comment = "- speed increase!";
		String effect = "faster";
		if(ratio <= 1)
		{
			comment = "- no speed increase :(";
			effect = "slower";
		}

		testResult.addElement(new BinderResult(Benchmark.RESULT_RATIO, new DoubleResult(ratio)));
		testResult.addElement(new BinderResult(Benchmark.RESULT_PERCENTAGE, new DoubleResult(percentage)));
		testResult.addElement(new BinderResult(Benchmark.RESULT_COMMENT, new StringResult("Speed comparision : " + percentage + " % (" + ratio + " times " + " " + effect + ") " + comment)));

		//TODO: check if result is identical regardless order
		byte[] rawres = qresEncoder.encodeResult(result);
		byte[] rawoptres = qresEncoder.encodeResult(optResult);
		
		boolean compareError = false;
		
		if(Arrays.equals(rawres, rawoptres))
			testResult.addElement(new BinderResult(Benchmark.RESULT_COMPARISON, new StringResult(Benchmark.RESULT_COMPARISON_OK)));
		else if(rawres.length == rawoptres.length)
		{
			Arrays.sort(rawres);
			Arrays.sort(rawoptres);
			if(Arrays.equals(rawres, rawoptres))
				testResult.addElement(new BinderResult(Benchmark.RESULT_COMPARISON, new StringResult(Benchmark.RESULT_COMPARISON_UNCERTAIN)));
			else {
				testResult.addElement(new BinderResult(Benchmark.RESULT_COMPARISON, new StringResult(Benchmark.RESULT_COMPARISON_ERROR)));
				compareError = true;
			}
		}
		else {
			testResult.addElement(new BinderResult(Benchmark.RESULT_COMPARISON, new StringResult(Benchmark.RESULT_COMPARISON_ERROR)));
			compareError = true;
		}
		
		if(store != null)
		{
			testResult.addElement(new BinderResult(Benchmark.STORE_USED_FOR_REFERENCE, new IntegerResult(memoryUsedUnoptimized - memoryUsedInitial)));
			testResult.addElement(new BinderResult(Benchmark.STORE_USED_FOR_OPTIMIZED, new IntegerResult(memoryUsedOptimized - memoryUsedUnoptimized)));
			testResult.addElement(new BinderResult(Benchmark.STORE_USED_RATIO, new DoubleResult((double)(memoryUsedOptimized - memoryUsedUnoptimized) / (double)(memoryUsedUnoptimized - memoryUsedInitial))));
		}
	
		
		if (compareError)
			return DBReply.createERRReply(testResult.toString(), DBReply.STAT_COMPARE_TEST_ERROR);
		else
			return DBReply.createOKReply(qresEncoder.encodeResult(testResult));	

	}

	/**
	 * Executes an sbql ad-hoc query.
	 * @param deref dereference result (on/off)
	 * @param isParmDependent TODO
	 * @param prog sbql source code to be executed
	 * @param modname global name of a module which should be used as a context of the execution
	 *
	 * @return result
	 * @throws Exception
	 *
	 * @author jacenty
	 */
	private final Result execSBQL(DBModule module, ASTNode node, boolean deref, boolean isParmDependent) throws Exception
	{
		node = doTypechecking(module, node, isParmDependent);
		if(deref)
		    node = doFinalDereference(module, node);
		node = doOptimization(module, node, Session.getCurrent().getOptimizationSequence());

		Result result = doExecution(module, node, deref, isParmDependent );

		return result;
	}

	/**
	 * Executes an sbql ad-hoc query.
	 * <br>
	 * The method distinguishes between a regular execution mode and a test mode. Depending on
	 * a <code>testMode</code> param, an actual execution is forwarded to appropriate methods.
	 *
	 * @param prog sbql source code to be executed
	 * @param modname global name of a module which should be used as a context of the execution
	 * @param derefMode dereference result (on/off)
	 * @param isParmDependent TODO
	 * @author jacenty
	 */
	private final DBReply execSBQL(String prog, String modname, String derefMode, String testMode, boolean isParmDependent) throws Exception
	{
		if(!derefMode.equals("on") && !derefMode.equals("off"))
			throw new InterpreterException("Unsupported autoderef mode '" + derefMode + "'");
		if(!testMode.equals("compare") && !testMode.equals("comparesimple") && !testMode.equals("plain") && !testMode.equals("plaintimes") && !testMode.equals("off"))
			throw new InterpreterException("Unsupported test mode '" + testMode + "'");

		DBModule module = Database.getModuleByName(modname);
		if(!module.isModuleLinked())
			linker.linkModule(module);
		if(!module.isModuleCompiled())
			compiler.compileModule(module);

		ASTNode node = BuilderUtils.parseSBQL(modname, prog);
		if(node instanceof ExpressionStatement){
			node = ((ExpressionStatement)node).getExpression();
		}
		if(node instanceof Expression)
		{
			Result result;
			boolean deref = derefMode.equals("on");

			//FIXME This is temporary to clear previous wrapper results. The process should be controlled within a transaction, probably.
			if(module.isWrapper())
				module.getWrapper().clearResult(Session.getCurrent());

			if (testMode.equals("plain") || testMode.equals("plaintimes"))
				result = benchmarkSBQL(module, node, deref, testMode.equals("plaintimes"));
			else if (testMode.startsWith("compare"))
				return compareTestSBQL(module, node, prog, testMode);
			else
				result = execSBQL(module, node, deref, isParmDependent);

			byte[] rawres = qresEncoder.encodeResult(result);
			return DBReply.createOKReply(rawres);
		}

		return DBReply.createERRReply("Unsupported SBQL program", DBReply.STAT_NETWORK_ERROR);
	}

	/**
	 * Executes an ocl ad-hoc query.
	 *
	 * @param prog ocl source code to be executed
	 * @param modname global name of a module which should be used as a context of the execution
	 * @param deref dereference result (on/off)
	 * @return result
	 * @throws Exception
	 *
	 * @author mariusz
	 */
	private final Result execOCL(DBModule module, ASTNode node, boolean deref) throws Exception
	{
		node = doOCLTypechecking(module, node);
		if(deref){
		    node = doFinalOCLDereference(module, node);
		}
		Result result = doOCLExecution(module, node, deref);

		return result;
	}

	/**
	 * Executes an ocl ad-hoc query.
	 * <br>
	 * The method distinguishes between a regular execution mode and a test mode. Depending on
	 * a <code>testMode</code> param, an actual execution is forwarded to appropriate methods.
	 *
	 * @param prog sbql source code to be executed
	 * @param modname global name of a module which should be used as a context of the execution
	 * @param derefMode dereference result (on/off)
	 *
	 * @author mariusz
	 */
	private final DBReply execOCL(String prog, String modname, String derefMode, String testMode) throws Exception
	{
		if(!derefMode.equals("on") && !derefMode.equals("off"))
			throw new InterpreterException("Unsupported autoderef mode '" + derefMode + "'");
		if(!testMode.equals("compare") && !testMode.equals("comparesimple") && !testMode.equals("plain") && !testMode.equals("off"))
			throw new InterpreterException("Unsupported test mode '" + testMode + "'");

		DBModule module = Database.getModuleByName(modname);
		if(!module.isModuleLinked())
			linker.linkModule(module);
		if(!module.isModuleCompiled())
			compiler.compileModule(module);

		ASTNode node = (ASTNode) new OCLParser(new OCLLexer(new StringReader(prog)), modname).parse().value;


			Result result;
			boolean deref = derefMode.equals("on");

				result = execOCL(module, node, deref);

			byte[] rawres = qresEncoder.encodeResult(result);
			return DBReply.createOKReply(rawres);


	}

	/** added by radamus
	 * explain the optimization result query
	 * @param query sbql query to be executed
	 * @param the optimization kind to explain
	 * @param modname global name of a module which should be used as a context of the optimization process
	 */
	private final DBReply execExplainOptimization(String[] params) throws Exception
	{
		if(ConfigServer.TYPECHECKING)
		{
			DBModule mod = Database.getModuleByName(params[params.length - 1]);
			if(!mod.isModuleLinked())
				linker.linkModule(mod);
			if(!mod.isModuleCompiled())
				compiler.compileModule(mod);
			ASTNode node = BuilderUtils.parseSBQL(params[params.length - 2]);
			if(node instanceof ExpressionStatement){
				node = ((ExpressionStatement)node).getExpression();
			}
			if(node instanceof Expression)
			{
				try
				{
				    	SBQLTypeChecker checker = new SBQLTypeChecker(mod);
					node = checker.typecheckAdHocQuery(node);
					OptimizationFramework opfrm = new OptimizationFramework(checker);
					for(int i = 0; i < params.length - 2; i++)
						opfrm.add(params[i]);

					node = opfrm.optimize(node, mod);

					// BindingLevelDumper dumper = new BindingLevelDumper();
					// node.accept(dumper, null);
					AST2TextQueryDumper astd = new AST2TextQueryDumper();
					node.accept(astd, null);
					String optquery = astd.getQuery();
					return DBReply.createOKReply(optquery.getBytes());
				}
				catch(OptimizationException exc)
				{
					if(ConfigServer.DEBUG_EXCEPTIONS)
						exc.printStackTrace();

					return DBReply.createERRReply(exc.getMessage(), DBReply.STAT_OPTMIZATION_ERROR);
				}
			}

			return DBReply.createERRReply("Unsupported SBQL program", DBReply.STAT_NETWORK_ERROR);
		}

		return DBReply.createERRReply("Optimizer require a safe (typechecked) query mode", DBReply.STAT_NETWORK_ERROR);
	}

	/**
	 * Explains query typechecking.
	 * @param params command parameters
	 *
	 * @author jacenty
	 */
	private final DBReply execExplainTypechecker(String[] params) throws Exception
	{
		if(ConfigServer.TYPECHECKING)
		{
			DBModule mod = Database.getModuleByName(params[params.length - 1]);
			if(!mod.isModuleLinked())
				linker.linkModule(mod);
			if(!mod.isModuleCompiled())
				compiler.compileModule(mod);

			ASTNode node = BuilderUtils.parseSBQL(params[params.length - 2]);
			if(node instanceof ExpressionStatement){
				node = ((ExpressionStatement)node).getExpression();
			}
			if(node instanceof Expression)
			{
				try
				{
				    	SBQLTypeChecker checker = new SBQLTypeChecker(mod);
					node = checker.typecheckAdHocQuery(node);

					AST2TextQueryDumper astd = new AST2TextQueryDumper();
					node.accept(astd, null);
					String query = astd.getQuery();
					return DBReply.createOKReply(query.getBytes());
				}
				catch (OptimizationException exc)
				{
					if(ConfigServer.DEBUG_EXCEPTIONS)
						exc.printStackTrace();

					return DBReply.createERRReply(exc.getMessage(), DBReply.STAT_NETWORK_ERROR);
				}
			}

			return DBReply.createERRReply("Unsupported SBQL program", DBReply.STAT_NETWORK_ERROR);
		}
		return DBReply.createERRReply("Typechecking should be enabled for this command", DBReply.STAT_NETWORK_ERROR);
	}
	private final DBReply execGetProcedureBody(String type,  String procedureSubPathName, String modname) throws Exception{
	    DBModule mod = Database.getModuleByName(modname);

	    Statement body;

	    if(ConfigServer.TYPECHECKING && type.equals("typecheck") ){
		DBProcedure proc = BuilderUtils.getDataProcedure(mod, procedureSubPathName);
		body = (Statement)BuilderUtils.deserializeAST(proc.getDebugCode());
	    }else {
		MBProcedure proc = BuilderUtils.getMetaProcedure(mod, procedureSubPathName);
		body = (Statement)BuilderUtils.deserializeAST(proc.getAST());
	    }

	    String sbody = new AST2TextQueryDumper().dumpAST(body);

	    return DBReply.createOKReply(sbody.getBytes());



	}

	private final DBReply execMeta(String query, String modname) throws Exception{
	    DBModule mod = Database.getModuleByName(modname);

	    if(!mod.isModuleLinked())
		linker.linkModule(mod);
	    if(!mod.isModuleCompiled())
		compiler.compileModule(mod);
	    ASTNode node = BuilderUtils.parseSBQL(modname, query);
	    if(node instanceof ExpressionStatement)
	    	node = ((ExpressionStatement)node).getExpression();
	    IJulietCodeGenerator generator = EmiterFactory.getMetaQueryJulietCodeGenerator(mod);
	    SBQLInterpreter interpreter = new SBQLInterpreter(mod, ExecutionMode.Meta);
	    generator.generate(node);
	    JulietCode code = generator.getCode();
	    byte[] byteCode = JulietGen.genDynDeref(code).getByteCode() ;
	    byte[] cnstPool = generator.getConstantPool().getAsBytes();

	    interpreter.runCode(byteCode, cnstPool);

	    byte[] rawres = qresEncoder.encodeResult(interpreter.getResult());
	    return DBReply.createOKReply(rawres);
	}
	/**
	 * Parses the query and returns its serialized form.
	 * @param params command parameters
	 *
	 * @author jacenty
	 */
	private final DBReply execParseOnly(String[] params) throws Exception
	{
		DBModule mod = Database.getModuleByName(params[2]);
		if(!mod.isModuleLinked())
			linker.linkModule(mod);
		if(!mod.isModuleCompiled())
			compiler.compileModule(mod);

		boolean sbql = params[0].equalsIgnoreCase("sbql");
		ASTNode node;
		if(sbql)
			node = BuilderUtils.parseSBQL(params[2], params[1]);
		else
			node = (ASTNode)new OCLParser(new OCLLexer(new StringReader(params[1])), params[2]).parse().value;

		if(node instanceof Expression || node instanceof Statement)
			return DBReply.createOKReply(new ASTSerializer().writeAST(node, true));

		return DBReply.createERRReply("Unsupported SBQL program", DBReply.STAT_NETWORK_ERROR);
	}

	/**
	 * Typechecks the query and returns its serialized form.
	 * @param params command parameters
	 *
	 * @author jacenty
	 */
	private final DBReply execTypecheckOnly(String[] params) throws Exception
	{
		if(ConfigServer.TYPECHECKING)
		{
			DBModule mod = Database.getModuleByName(params[3]);
			if(!mod.isModuleLinked())
				linker.linkModule(mod);
			if(!mod.isModuleCompiled())
				compiler.compileModule(mod);

			boolean sbql = params[0].equalsIgnoreCase("sbql");
			ASTNode node;
			if(sbql)
				node = BuilderUtils.parseSBQL(params[2]);
			else
				node = (ASTNode)new OCLParser(new OCLLexer(new StringReader(params[2])), params[3]).parse().value;

			if(node instanceof Expression || node instanceof Statement)
			{
				SBQLTypeChecker checker;
				if(node instanceof Expression)
					checker = new SBQLTypeChecker(mod);
				else
					checker = new SBQLProcedureTypeChecker(mod);

				boolean autoderef = params[1].equalsIgnoreCase("on");
				node = checker.typecheckAdHocQuery(node);

				return DBReply.createOKReply(new ASTSerializer().writeAST(node, true));
			}

			return DBReply.createERRReply("Unsupported SBQL program", DBReply.STAT_NETWORK_ERROR);
		}

		return DBReply.createERRReply("Typechecking should be enabled for this command", DBReply.STAT_NETWORK_ERROR);
	}

	/**
	 * Optimizes the query and returns its serialized form.
	 * @param params command parameters
	 *
	 * @author jacenty
	 */
	private final DBReply execOptimizeOnly(String[] params) throws Exception
	{
		if(ConfigServer.TYPECHECKING)
		{
			DBModule mod = Database.getModuleByName(params[3]);
			if(!mod.isModuleLinked())
				linker.linkModule(mod);
			if(!mod.isModuleCompiled())
				compiler.compileModule(mod);

			boolean sbql = params[0].equalsIgnoreCase("sbql");
			ASTNode node;
			if(sbql)
				node = BuilderUtils.parseSBQL(params[2]);
			else
				node = (ASTNode)new OCLParser(new OCLLexer(new StringReader(params[2])), params[3]).parse().value;

			if(node instanceof Expression || node instanceof Statement)
			{
				try
				{
					SBQLTypeChecker checker;
					if(node instanceof Expression)
						checker = new SBQLTypeChecker(mod);
					else
						checker = new SBQLProcedureTypeChecker(mod);

					boolean autoderef = params[1].equalsIgnoreCase("on");
					node = checker.typecheckAdHocQuery(node);

					OptimizationSequence sequence = new OptimizationSequence();
					for(int i = 4; i < params.length; i++)
						sequence.addType(Type.getTypeForString(params[i]));
					node = doOptimization(mod, node, sequence);

					return DBReply.createOKReply(new ASTSerializer().writeAST(node, true));
				}
				catch (OptimizationException exc)
				{
					if(ConfigServer.DEBUG_EXCEPTIONS)
						exc.printStackTrace();

					return DBReply.createERRReply(exc.getMessage(), DBReply.STAT_NETWORK_ERROR);
				}
			}

			return DBReply.createERRReply("Unsupported SBQL program", DBReply.STAT_NETWORK_ERROR);
		}

		return DBReply.createERRReply("Typechecking should be enabled for this command", DBReply.STAT_NETWORK_ERROR);
	}

	/**
	 * added by radamus explain the juliet code
	 *
	 * @param query
	 *          sbql query to assemble
	 * @param modname
	 *          global name of a module which should be used as a context of the
	 *          optimization process
	 */
	private final DBReply execExplainJuliet(String[] params) throws Exception {

			DBModule mod = Database.getModuleByName(params[params.length - 1]);
			if(!mod.isModuleLinked())
				linker.linkModule(mod);
			if(!mod.isModuleCompiled())
				compiler.compileModule(mod);
			ASTNode node = BuilderUtils.parseSBQL(params[params.length - 2]);
			if(node instanceof ExpressionStatement)
				node = ((ExpressionStatement)node).getExpression();
			if (node instanceof Expression) {
				if (ConfigServer.TYPECHECKING){
				    	SBQLTypeChecker checker = new SBQLTypeChecker(mod);
					node = checker.typecheckAdHocQuery(node);

					OptimizationFramework opfrm = new OptimizationFramework(checker);
					opfrm.setOptimizationSequence(Session.getCurrent().getOptimizationSequence());
					node = opfrm.optimize(node, mod);
				}
				IJulietCodeGenerator generator = EmiterFactory.getJulietCodeGenerator(mod);
				generator.generate(node);
				byte[] code = generator.getCode().getByteCode();
				return DBReply.createOKReply(code);
			}
			return DBReply.createERRReply("Unsupported SBQL program", DBReply.STAT_NETWORK_ERROR);
	}



	/**
	 * Dissasembles a procedure
	 * @param procname name of a procedure that should be disassembled
	 * @param modname module containing the procedure
	 */
	private final DBReply execDisassembleProcedure(String procname, String modname) throws Exception {
		DBModule mod = Database.getModuleByName(modname);

		OID proid = mod.findFirstByName(procname, mod.getDatabaseEntry());

		if (proid == null)
			return DBReply.createERRReply("Procedure '" + modname + "." + procname + "' cannot be found", DBReply.STAT_RUNTIME_ERROR);

		DBProcedure dbproc = new DBProcedure(proid);
		Disassembler dis = new Disassembler(dbproc.getBinaryCode());
		String codestr = dis.decode();

		byte[] rawres = qresEncoder.encodeResult(new StringResult(codestr));

		return DBReply.createOKReply(rawres);
	}

	private final DBReply execLoadData(String modname, String data, String filter, String params) throws Exception {
		for (String s : ConfigServer.plugins.keySet()) {
			if (s.equals(filter)) {
				DataImporter plug = (DataImporter) Class.forName(ConfigServer.plugins.get(s)).newInstance();

				plug.importData(modname, data, params);

				return DBReply.createOKReply();
			}
		}

		throw new FilterException("Filter '" + filter + "' cannot be used", null);
	}

	private final DBReply execAlterProcedureBody(String procGlobalName, String code, String modname) throws Exception {

		ASTNode node = BuilderUtils.parseSBQL(modname, code);
		if(node instanceof Expression)
			node = new ExpressionStatement((Expression)node);
		BuilderUtils.alterProcedureBody(modname, procGlobalName, (Statement)node);


		return DBReply.createOKReply();
	}

	/**
	 * TODO: javadoc
	 */
	private final DBReply execAddIndex(boolean temporary, String[] params) throws Exception {
    	String idxname = params[0];
    	String modname = params[params.length - 1];
    	String prog = params[params.length - 2];
    	DBModule mod = Database.getModuleByName(modname);
    	IJulietCodeGenerator generator = EmiterFactory.getJulietCodeGenerator(mod);
    	odra.db.indices.IndexManager iman = new odra.db.indices.IndexManager(linker, compiler, generator);
    	Vector<String> rangeParam = new Vector<String>(params.length - 3);
    	for (int i = 1; i < params.length - 2; i++)
    		rangeParam.add(params[i]);

    	iman.createIndex(idxname, temporary, rangeParam.toArray(new String[rangeParam.size()]), prog, modname);

    	return DBReply.createOKReply();
	}

	/**
	 * Deletes a index from the database. Modules that import the module in which
	 * index is being deleted are invalidated (need recompilation)
	 * @param modname global name of a module
	 * @param idxname index name
	 * @return DBReply object sent to the client
	 */
	private final DBReply execRemoveIndex(String idxname, String modname) throws Exception {
	    	DBModule mod = Database.getModuleByName(modname);
	    	IJulietCodeGenerator generator = EmiterFactory.getJulietCodeGenerator(mod);
		odra.db.indices.IndexManager iman = new odra.db.indices.IndexManager(linker, compiler,  generator);


		OID[] imodoids = mod.getOID().getReferencesPointingAt();

		for (OID oid : imodoids) {
			new DBModule(oid.getParent().getParent()).setModuleCompiled(false);
			new DBModule(oid.getParent().getParent()).setModuleLinked(false);
		}

		iman.removeIndex(idxname, modname);

		return DBReply.createOKReply();
	}

	/**
	 * Creates endpoint for given object
	 * @param params parameters passed with the request
	 */
	private final DBReply execAddEndpoint(String[] params) throws Exception {

		IEndpointFacade em = WSManagersFactory.createEndpointManager();

		if (em == null) {
			return DBReply.createERRReply("Web service endpoint manager driver not found. ");
		}

		//Session.getUserContext().getUserName(),
		// params: endpoint name, exposed object, state(on|off), path, portType name, port name, service name, ns
		String endpointName = params[0];
		String exposedObjectName = params[1];
		String stateStr = params[2];
		String path = params[3];
		String portTypeName = params[4];
		String portName = params[5];
		String serviceName = params[6];
		String ns = params[7];
		String modname = params[params.length - 1];

		// $endpoints global object
		DBModule mod = Database.getModuleByName(modname);

		OID exposedObject = mod.findFirstByName(exposedObjectName, mod.getMetabaseEntry());

		if (exposedObject == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("Object '");
			sb.append(exposedObjectName);
			sb.append("' cannot be exposed as web service endpoint. ");
			String msg = sb.toString();

			return DBReply.createERRReply(msg);
		}

		EndpointState state = EndpointState.UNKNOWN;
		if (stateStr.equalsIgnoreCase("STARTED")) {
			state = EndpointState.STARTED;
		} else if (stateStr.equalsIgnoreCase("STOPPED")) {
			state = EndpointState.STOPPED;
		}

		try {
			em.createEndpoint(endpointName, exposedObject, state, path, portTypeName, portName, serviceName, ns);
			return DBReply.createOKReply();

		} catch (Exception ex) {
			return DBReply.createERRReply(ex.getMessage());

		}

	}

	/**
	 * Deletes the endpoint from the database.
	 * @param modname global name of a module
	 * @param idxname index name
	 * @return DBReply object sent to the client
	 */
	private final DBReply execRemoveEndpoint(String endpointName, String modname) throws Exception {
		IEndpointFacade em = WSManagersFactory.createEndpointManager();

		if (em == null) {
			return DBReply.createERRReply("Web service endpoint manager driver not found. ");
		}

		em.removeEndpoint(endpointName);

		return DBReply.createOKReply();
	}

	/**
	 * Deletes the endpoint from the database.
	 * @param modname global name of a module
	 * @param idxname index name
	 * @return DBReply object sent to the client
	 */
	private final DBReply execSuspendEndpoint(String endpointName, String modname) throws Exception {
		IEndpointFacade em = WSManagersFactory.createEndpointManager();

		if (em == null) {
			return DBReply.createERRReply("Web service endpoint manager driver not found. ");
		}
		em.suspendEndpoint(endpointName);

		return DBReply.createOKReply();
	}

	/**
	 * Deletes the endpoint from the database.
	 * @param modname global name of a module
	 * @param idxname index name
	 * @return DBReply object sent to the client
	 */
	private final DBReply execResumeEndpoint(String endpointName, String modname) throws Exception {
		IEndpointFacade em = WSManagersFactory.createEndpointManager();

		if (em == null) {
			return DBReply.createERRReply("Web service endpoint manager driver not found. ");
		}

		em.resumeEndpoint(endpointName);

		return DBReply.createOKReply();
	}

	private final DBReply execRemoveProxy(String proxyName, String parModName) throws Exception {
		DBModule module = null;

		try {
			IProxyFacade pm = WSManagersFactory.createProxyManager();

			if (pm == null ) {
				return DBReply.createERRReply("Web service proxy manager driver not found. ");
			}

			DBModule parentModule = Database.getModuleByName(parModName);

			OID proxyOID = parentModule.findFirstByName(proxyName, parentModule.getDatabaseEntry());
			if (proxyOID == null) {
				byte[] rawres = qresEncoder.encodeResult(new StringResult("Proxy '" + proxyName + "' does not exist."));
				return DBReply.createOKReply(rawres);
			}

			pm.removeProxy(proxyOID);

			byte[] rawres = qresEncoder.encodeResult(new StringResult("Proxy '" + proxyName + "' removed."));
			return DBReply.createOKReply(rawres);



		} catch (Exception ex) {

			return DBReply.createERRReply("Problem removing proxy named '" + proxyName + "'.");

		}
	}

	/**
	 * Creates proxy module.
	 * @param proxyModName name of proxy module to be created
	 * @param wsdlUrl url of wsdl contract
	 * @param parModName parent module
	 * @return DBReply object sent to the client
	 */
	private final DBReply execAddModuleAsProxy(String proxyModName, String wsdlUrlString, String parModName) throws Exception {
		DBModule module = null;

		try {
			IProxyFacade pm = WSManagersFactory.createProxyManager();

			if (pm == null ) {
				return DBReply.createERRReply("Web service proxy manager driver not found. ");
			}

			URL wsdlLocation = tryParseURL(wsdlUrlString);

			if (wsdlLocation == null)
			{
				return DBReply.createERRReply("Incorrect WSDL contract URL provided.");
			}

			DBModule parentModule = Database.getModuleByName(parModName);

			// exitence check
			try {
				Database.getModuleByName(parModName+"."+proxyModName);
				throw new WSProxyException("Module with given name already exists. ");

			} catch (DatabaseException ex) {


			}
			module = new DBModule(parentModule.createSubmodule(proxyModName));
			pm.buildStub(WSProxyType.ModuleProxy, module.getOID(), wsdlLocation);

			byte[] rawres = qresEncoder.encodeResult(new StringResult("Module '" + module.getModuleGlobalName() + "' created."));
			return DBReply.createOKReply(rawres);

		} catch (Exception ex) {
			if (module != null) {
				execDropModule(module.getModuleGlobalName());
			}

			return DBReply.createERRReply(ex.getMessage());

		}

	}



	/**
	 * Promote class to proxy.
	 * @param objectName candidate to be proxied name
	 * @param wsdlUrl url of wsdl contract
	 * @param parModName parent module
	 * @return DBReply object sent to the client
	 */
	private final DBReply execPromoteToProxy(String objectName, String wsdlUrlString,
			String port, String servicem, String parModName) throws Exception {
		DBModule module = null;

		try {
			IProxyFacade pm = WSManagersFactory.createProxyManager();

			if (pm == null ) {
				return DBReply.createERRReply("Web service proxy manager driver not found. ");
			}

			URL wsdlLocation = tryParseURL(wsdlUrlString);

			if (wsdlLocation == null)
			{
				return DBReply.createERRReply("Incorrect WSDL contract URL provided.");
			}

			DBModule parentModule = Database.getModuleByName(parModName);
			OID oid = parentModule.findFirstByName(objectName, parentModule.getDatabaseEntry());

			if (oid == null) {
				return DBReply.createERRReply("Object to promote not found. ");

			}

			DBObject object = new DBObject(oid);

			switch (object.getObjectKind().getKindAsInt()) {
				case DataObjectKind.CLASS_OBJECT:
					DBClass clasObject = new DBClass(oid);
					pm.promoteStub(WSProxyType.ClassProxy, clasObject.getOID(), wsdlLocation);
					break;
				default:
					return DBReply.createERRReply("Only class can be promoted to proxy. ");
			}

			byte[] rawres = qresEncoder.encodeResult(new StringResult("'"+objectName + "' promoted. to proxy. "));
			return DBReply.createOKReply(rawres);

		} catch (Exception ex) {
			if (module != null) {
				execDropModule(module.getModuleGlobalName());
			}

			return DBReply.createERRReply(ex.getMessage());

		}



	}

	private final DBReply execAddLink(String lnkname, String schema, String passwd, String host, String port, String modname) throws Exception {

		odra.db.links.LinkManager.getInstance().createLink(lnkname, Database.getModuleByName(modname), host, Integer.parseInt(port), schema, passwd);

		return DBReply.createOKReply();
	}

	private final DBReply execRemovelink(String lnkname, String modname)
	{
		try
		{
			odra.db.links.LinkManager.getInstance().removeLink( lnkname , modname );
		}
		catch (RDNetworkException e)
		{
			return DBReply.createERRReply("exception when removing link");
		}

		return DBReply.createOKReply();
	}

	private final DBReply execRefreshLink(String lnkname, String modname) throws Exception {

		DBModule mod = Database.getModuleByName(modname);

		OID linkOID = mod.findFirstByName(lnkname, mod.getDatabaseEntry());

		if (linkOID == null)
			return DBReply.createERRReply("DBLink '" + modname + "." + lnkname + "' cannot be found");

		DBLink dbLink = new DBLink(linkOID);
		odra.db.links.LinkManager.getInstance().refreshLinkMetadata(dbLink);

		return DBReply.createOKReply();
	}

	private final DBReply execAddLinkToSchema(String lnkname, String schema, String modname) throws Exception {

		DBModule module = Database.getModuleByName(modname);
		if(!module.isModuleLinked())
			linker.linkModule(module);
		if(!module.isModuleCompiled())
			compiler.compileModule(module);

		OID schemaoid;
		try
		{
			schemaoid = ((ReferenceResult) execSBQL(module, BuilderUtils.parseSBQL(modname, schema + ";"), false, false).elementAt(0)).value;
		}
		catch (RDNetworkException e)
		{
			return DBReply.createERRReply("valid schema expected");
		}

		odra.db.links.LinkManager.getInstance().addLinkToSchema(lnkname, schemaoid, module);

		return DBReply.createOKReply();
	}

	private final DBReply execRemoveLinkFromSchema(String lnkname, String schema, String modname) throws Exception
	{

		DBModule module = Database.getModuleByName(modname);
		if(!module.isModuleLinked())
			linker.linkModule(module);
		if(!module.isModuleCompiled())
			compiler.compileModule(module);

		OID schemaoid;
		try
		{
			schemaoid = ((ReferenceResult) execSBQL(module, BuilderUtils.parseSBQL(modname, schema + ";"), false, false).elementAt(0)).value;
		}
		catch (RDNetworkException e)
		{
			return DBReply.createERRReply("valid schema expected");
		}

		odra.db.links.LinkManager.getInstance().removeLinkFromSchema(lnkname, schemaoid, module);

		return DBReply.createOKReply();
	}

	private final DBReply execAddGridLink(String gridlnkname, String gridschema, String gridpasswd, String gridhost, String modname) throws Exception {

		if (!ConfigServer.JXTA) throw new DatabaseException("Grid not initialized");

		odra.db.links.LinkManager.getInstance().createGridLink(gridlnkname, Database.getModuleByName(modname), gridhost, gridschema, gridpasswd);

		return DBReply.createOKReply();
	}

	private final DBReply execRemoveGridlink(String gridlnkname, String modname) throws Exception {

		if (!ConfigServer.JXTA) throw new DatabaseException("JXTA not initialized");
		try
		{
			odra.db.links.LinkManager.getInstance().removeGridLink( gridlnkname , modname );
		}
		catch (RDNetworkException e)
		{
			return DBReply.createERRReply("exception when removing gridlink");
		}

		return DBReply.createOKReply();
	}

	//notify cmu about contributing module
	private final DBReply execJoinToGrid(String contribSchema, String modname) throws Exception {

		if (!ConfigServer.JXTA) throw new DatabaseException("JXTA not initialized");

		Session currses = odra.sessions.Session.getCurrent();
		DBModule currmod = Database.getModuleByName(modname);

		//args are flipped
		Repository.getInstance().putManagementRequest(Repository.MGMT_JOINTOGRID,
				new String[] {contribSchema, currses.usrctx.getUserName()});
		RequestHandlerImpl.getImpl().prepareConnection("admin", "admin");

		return DBReply.createOKReply();
	}

	private final DBReply execRemoveFromGrid(String contribSchema, String modname) throws Exception {
		if (!ConfigServer.JXTA) throw new DatabaseException("JXTA not initialized");

		Session currses = odra.sessions.Session.getCurrent();
		DBModule currmod = Database.getModuleByName(modname);

		//args are flipped
		Repository.getInstance().putManagementRequest(Repository.MGMT_REMOVEFROMGRID,
				new String []{contribSchema, currses.usrctx.getUserName()});
		RequestHandlerImpl.getImpl().prepareConnection("admin", "admin");

		return DBReply.createOKReply();
	}


	//TODO needs strong refactor
	private CLI cli = null;
	private final DBReply execExecuteRemoteCommand(String command) throws Exception {

		if (cli == null) {
			cli = new CLI();
			ConnectCommand cmd = new ConnectCommand(new DatabaseURL(
					"admin","admin", "127.0.0.1", ConfigServer.LSNR_PORT));
			cli.visitConnectCommand(cmd, null);
		}

		BatchString batch = new BatchString(command);
		Vector<String> commands = batch.read();

		for (String cmd : commands )
			cli.executeCommand(cmd);

		return DBReply.createOKReply();
	}

	private final DBReply execConnectToGrid(String jxtagroupname, String peername, String passwd, String URI, String modname) throws Exception {

		if (!ConfigServer.JXTA) throw new DatabaseException("JXTA not initialized");

		return DBReply.createOKReply();
	}


	private byte[] encodeSourcePositionBag(SBQLException ex) throws RDNetworkException {
		BagResult bag = new BagResult();

		bag.addElement(new StringResult(ex.getModule()));
		bag.addElement(new IntegerResult(ex.getLine()));
		bag.addElement(new IntegerResult(ex.getColumn()));

		return qresEncoder.encodeResult(bag);
	}

	// remote store methods
	private final DBReply execGetName(String nameidstr) throws DatabaseException, RDNetworkException {

		//was
		//int objid = Integer.parseInt(nameidstr);
		//byte[] rawres = qresEncoder.encodeResult(new StringResult(Database.getNameIndex().id2name(objid)));

		//changed by mich 14-04
		byte[] rawres = qresEncoder.encodeResult( new StringResult(string2OID(nameidstr).getObjectName()));

		return DBReply.createOKReply(rawres);
	}

	private final OID string2OID(String objidstr) throws DatabaseException, RDNetworkException {
		int objid = Integer.parseInt(objidstr);

		return new odra.store.DefaultStoreOID(objid, (DefaultStore) Database.getStore());
	}

	private final DBReply execIsAggregateObject(String objidstr) throws DatabaseException, RDNetworkException {
		return DBReply.createOKReply(qresEncoder.encodeResult(new BooleanResult(string2OID(objidstr).isAggregateObject())));
	}

	private final DBReply execIsComplexObject(String objidstr) throws DatabaseException, RDNetworkException {
		return DBReply.createOKReply(qresEncoder.encodeResult(new BooleanResult(string2OID(objidstr).isComplexObject())));
	}

	private final DBReply execIsDoubleObject(String objidstr) throws DatabaseException, RDNetworkException {
		return DBReply.createOKReply(qresEncoder.encodeResult(new BooleanResult(string2OID(objidstr).isDoubleObject())));
	}

	private final DBReply execIsStringObject(String objidstr) throws DatabaseException, RDNetworkException {
		return DBReply.createOKReply(qresEncoder.encodeResult(new BooleanResult(string2OID(objidstr).isStringObject())));
	}

	private final DBReply execIsDateObject(String objidstr) throws DatabaseException, RDNetworkException {
		return DBReply.createOKReply(qresEncoder.encodeResult(new BooleanResult(string2OID(objidstr).isDateObject())));
	}

	private final DBReply execIsBooleanObject(String objidstr) throws DatabaseException, RDNetworkException {
		return DBReply.createOKReply(qresEncoder.encodeResult(new BooleanResult(string2OID(objidstr).isBooleanObject())));
	}

	private final DBReply execIsBinaryObject(String objidstr) throws DatabaseException, RDNetworkException {
		return DBReply.createOKReply(qresEncoder.encodeResult(new BooleanResult(string2OID(objidstr).isBinaryObject())));
	}

	private final DBReply execIsReferenceObject(String objidstr) throws DatabaseException, RDNetworkException {
		return DBReply.createOKReply(qresEncoder.encodeResult(new BooleanResult(string2OID(objidstr).isReferenceObject())));
	}

	private final DBReply execIsIntegerObject(String objidstr) throws DatabaseException, RDNetworkException {
		return DBReply.createOKReply(qresEncoder.encodeResult(new BooleanResult(string2OID(objidstr).isIntegerObject())));
	}

	private final DBReply execUpdateIntegerObject(String objidstr, String valstr) throws DatabaseException, RDNetworkException {
		string2OID(objidstr).updateIntegerObject(Integer.parseInt(valstr));

		return DBReply.createOKReply(qresEncoder.encodeResult(new BagResult()));
	}

	private final DBReply execUpdateDoubleObject(String objidstr, String valstr) throws DatabaseException, RDNetworkException {
		string2OID(objidstr).updateDoubleObject(Double.parseDouble(valstr));

		return DBReply.createOKReply(qresEncoder.encodeResult(new BagResult()));
	}

	private final DBReply execUpdateBooleanObject(String objidstr, String valstr) throws DatabaseException, RDNetworkException {
		string2OID(objidstr).updateBooleanObject(Boolean.parseBoolean(valstr));

		return DBReply.createOKReply(qresEncoder.encodeResult(new BagResult()));
	}

	private final DBReply execUpdateStringObject(String objidstr, String valstr) throws DatabaseException, RDNetworkException {
		string2OID(objidstr).updateStringObject(valstr);

		return DBReply.createOKReply(qresEncoder.encodeResult(new BagResult()));
	}

	private final DBReply execUpdateBinaryObject(String objidstr, String valstr) throws DatabaseException, RDNetworkException {
		string2OID(objidstr).updateBinaryObject(valstr.getBytes());

		return DBReply.createOKReply(qresEncoder.encodeResult(new BagResult()));
	}

	private final DBReply execUpdateReferenceObject(String objidstr, String valstr) throws DatabaseException, RDNetworkException {
		string2OID(objidstr).updateReferenceObject(string2OID(valstr));

		return DBReply.createOKReply(qresEncoder.encodeResult(new BagResult()));
	}

	private final DBReply execUpdateDateObject(String objidstr, String valstr) throws DatabaseException, RDNetworkException {
		string2OID(objidstr).updateDateObject(new Date(Long.parseLong(valstr)));

		return DBReply.createOKReply(qresEncoder.encodeResult(new BagResult()));
	}

	private final DBReply execDerefIntegerObject(String objidstr) throws DatabaseException, RDNetworkException {
		return DBReply.createOKReply(qresEncoder.encodeResult(new IntegerResult(string2OID(objidstr).derefInt())));
	}

	private final DBReply execDerefStringObject(String objidstr) throws DatabaseException, RDNetworkException {
		return DBReply.createOKReply(qresEncoder.encodeResult(new StringResult(string2OID(objidstr).derefString())));
	}

	private final DBReply execDerefReferenceObject(String objidstr) throws DatabaseException, RDNetworkException {
		return DBReply.createOKReply(qresEncoder.encodeResult(new ReferenceResult(string2OID(objidstr).derefReference())));
	}

	private final DBReply execDerefDoubleObject(String objidstr) throws DatabaseException, RDNetworkException {
		return DBReply.createOKReply(qresEncoder.encodeResult(new DoubleResult(string2OID(objidstr).derefDouble())));
	}

	private final DBReply execDerefBooleanObject(String objidstr) throws DatabaseException, RDNetworkException {
		return DBReply.createOKReply(qresEncoder.encodeResult(new BooleanResult(string2OID(objidstr).derefBoolean())));
	}

	private final DBReply execDerefDateObject(String objidstr) throws DatabaseException, RDNetworkException {
		return DBReply.createOKReply(qresEncoder.encodeResult(new DateResult(string2OID(objidstr).derefDate())));
	}

	private final DBReply execDerefComplexObject(String objidstr) throws DatabaseException, RDNetworkException {
		OID[] oids = string2OID(objidstr).derefComplex();
		StructResult strres = new StructResult();
		for(OID oid: oids)
			strres.addField(new ReferenceResult(oid));
		return DBReply.createOKReply(qresEncoder.encodeResult(strres));
	}

	private final DBReply execCountChildren(String objidstr) throws DatabaseException, RDNetworkException{
		return DBReply.createOKReply(qresEncoder.encodeResult(new IntegerResult(string2OID(objidstr).countChildren())));
	}

	private final DBReply execGetChildAt(String parentidstr, String childnumstr) throws RDNetworkException, DatabaseException{
		int childnum = Integer.parseInt(childnumstr);
		//that sholud be RemoteReferenceResult
		return DBReply.createOKReply(qresEncoder.encodeResult(new ReferenceResult(string2OID(parentidstr).getChildAt(childnum))));
	}

	private final DBReply execRemoteProcedureCall(String modname, String procidstr, String remoteStack) throws DatabaseException, RDNetworkException {

		DBModule mod = Database.getModuleByName(modname);
		if(!mod.isModuleLinked())
			linker.linkModule(mod);
		if(!mod.isModuleCompiled())
			compiler.compileModule(mod);

		SBQLInterpreter interpreter = new SBQLInterpreter(mod);
		//decode procedure reference
		Result procref = new RemoteQueryParameterDecoder().decodeResult(procidstr.getBytes());
		assert procref instanceof ReferenceResult;
		//decode params
		SBQLStackDecoder decoder = new SBQLStackDecoder(new RemoteQueryParameterDecoder());
		//push params
		Vector<AbstractQueryResult> stack = decoder.decodeStack(remoteStack.getBytes());
		for(AbstractQueryResult res : stack){
			interpreter.setResult((Result)res);

		}

		JulietCode callcode;
		if(new DBVirtualObjectsProcedure(((ReferenceResult)procref).value).isValid()){
		    callcode = JulietGen.genCallVirtualObjects();
		}else {
		    callcode = JulietGen.genProcedureCall();
		  //push number of parameters
		    interpreter.setResult(new IntegerResult(stack.size()));
		}



		//push procedure reference
		interpreter.setResult(procref);
		interpreter.runCode(callcode.getByteCode(), null);
		return DBReply.createOKReply(qresEncoder.encodeResult(interpreter.getResult()));
	}


	/**
	 * Creates a reply for sending a metadata
	 *
	 * @param schema for which the metadata is requested
	 * @return
	 *
	 */
	private final DBReply execGetMetadata(String schema)  throws DatabaseException, RDNetworkException
	{
		DBModule module = Database.getModuleByName(schema);

		if(!module.isModuleLinked())
			linker.linkModule(module);

		byte[] rawres = new MetaEncoder().encodeMeta(module, module.getMetabaseEntry() );

		return DBReply.createOKReply(rawres);
	}

	/**
	 * Creates a reply for sending a persistent heap length.
	 *
	 * @param persistent persistent? (else: transient)
	 * @throws DatabaseException
	 * @throws RDNetworkException
	 *
	 * @author jacenty
	 */
	private final DBReply execHeapStructurePersistentInit(boolean persistent) throws DatabaseException, RDNetworkException {
		DefaultStore store;

		try
		{
			if(persistent)
				store = (DefaultStore)Database.getStore();
			else
				store = (DefaultStore)Session.getTransientStore();

			ObjectManager manager = store.getObjectManager();
			RevSeqFitMemManager allocator = (RevSeqFitMemManager)manager.getMemoryManager();
			sorter = new HeapSorter(allocator);
			byte[] rawres = qresEncoder.encodeResult(new IntegerResult(sorter.getByteCount()));
			return DBReply.createOKReply(rawres);
		}
		catch(ClassCastException exc) {
			throw new RDNetworkException("The current system architecture does not support store structure visualization (" + exc + ")");
		}
	}

	/**
	 * Creates a reply for sending a persistent heap structure fragment.
	 *
	 * @param startByte start byte
	 * @param length length
	 * @return <code>DBReply</code>
	 *
	 * @author jacenty
	 */
	private final DBReply execHeapStructureFragmentData(int startByte, int length)
	{
		byte[] bytes = sorter.getFragment(startByte, length);
		return DBReply.createOKReply(bytes);
	}

	/**
	 * Creates a reply for sending a persistent heap structure fragment types.
	 *
	 * @param startByte start byte
	 * @param length length
	 * @return <code>DBReply</code>
	 *
	 * @author jacenty
	 */
	private final DBReply execHeapStructureFragmentTypes(int startByte, int length)
	{
		byte[] bytes = sorter.getFragmentTypes(startByte, length);
		return DBReply.createOKReply(bytes);
	}

	/**
	 * Creates a reply for creating a wrapper module request.
	 *
	 * @param moduleName module to be created name
	 * @param host wrapper server host
	 * @param port wrapper server port
	 * @param mode wrapper mode
	 * @param parentModuleName parent module name
	 * @return <code>DBReply</code>
	 * @throws Exception
	 *
	 * @author jacenty
	 */
	private final DBReply execAddModuleAsWrapper(String moduleName, String host, int port, int mode, String parentModuleName) throws Exception
	{
		DBModule parentModule = Database.getModuleByName(parentModuleName);
		DBModule module = new DBModule(parentModule.createSubmodule(moduleName));

		module.setWrapper(
			new Wrapper(
				Session.getUserContext().getUserName(),
				host,
				port,
				mode,
				Database.getModuleByName(module.getModuleGlobalName())));

		byte[] rawres = qresEncoder.encodeResult(new StringResult("Module '" + module.getModuleGlobalName() + "' created."));

		return DBReply.createOKReply(rawres);
	}



	private final DBReply execAddView(String src, String module) throws Exception
	{
		DBModule mod = Database.getModuleByName(module);
		ModuleConstructor constructor = new ModuleConstructor(null);
		constructor.setConstructedModule(mod);
		ASTNode node = BuilderUtils.parseSBQL( module, src);

		node.accept(constructor, null);
		mod.setModuleLinked(false);
		mod.setModuleCompiled(false);
		byte[] rawres = qresEncoder.encodeResult(new StringResult("View created successfully."));
		return DBReply.createOKReply(rawres);
	}

	private final DBReply execDropView(String viewName, String module) throws Exception
	{

		DBModule mod = Database.getModuleByName(module);
		ModuleOrganizer org = new ModuleOrganizer(mod, false);
		org.deleteView(viewName);
		byte[] rawres = qresEncoder.encodeResult(new StringResult("View '" + viewName + "' removed successfully."));
		return DBReply.createOKReply(rawres);

	}
	/**
	 * Creates a reply for showing an optimization sequence.
	 *
	 * @param reference show reference optimization sequence?
	 * @return <code>DBReply</code>
	 * @throws Exception
	 *
	 * @author jacenty
	 */
	private final DBReply execShowOptimization(boolean reference) throws Exception
	{
		OptimizationSequence sequence;
		if(reference)
			sequence = Session.getCurrent().getOptimizationReferenceSequence();
		else
			sequence = Session.getCurrent().getOptimizationSequence();
		byte[] rawres = sequence.toString().getBytes();
		return DBReply.createOKReply(rawres);
	}

	/**
	 * Creates a reply for setting an optimization sequence.
	 *
	 * @param params optimization types
	 * @return <code>DBReply</code>
	 * @throws Exception
	 *
	 * @author jacenty
	 */
	private final DBReply execSetOptimization(String[] params) throws Exception
	{
		OptimizationSequence sequence;
		if(Boolean.valueOf(params[0]))
			sequence = Session.getCurrent().getOptimizationReferenceSequence();
		else
			sequence = Session.getCurrent().getOptimizationSequence();

		for(int i = 1; i < params.length; i++)
		{
			try
			{
				sequence.addType(Type.getTypeForString(params[i]));
			}
			catch(OptimizationException exc)
			{
				return DBReply.createERRReply(exc.getMessage());
			}
		}

		return DBReply.createOKReply();
	}

	/**
	 * Creates a reply for memory (Java heap) state.
	 *
	 * @return <code>DBReply</code>
	 * @throws Exception
	 *
	 * @author jacenty
	 */
	private final DBReply execMemoryGet() throws Exception
	{
		Runtime runtime = Runtime.getRuntime();
		long curTotal = runtime.totalMemory();
		long curFree = runtime.freeMemory();

		BagResult result = new BagResult();
		result.addElement(new IntegerResult((int)curTotal));
		result.addElement(new IntegerResult((int)curFree));

		return DBReply.createOKReply(qresEncoder.encodeResult(result));
	}

	/**
	 * Creates a reply for garbage collection
	 *
	 * @return <code>DBReply</code>
	 * @throws Exception
	 *
	 * @author jacenty
	 */
	private final DBReply execMemoryGc() throws Exception
	{
		Runtime.getRuntime().gc();

		return DBReply.createOKReply();
	}

	/**
	 * Helper method for parsing URIs
	 * @param uriString
	 * @return URI instance for correct URI string; null for incorrect URI
	 */
	private final URL tryParseURL(String uriString)
	{
		URL url = null;
		try
		{
			url = new URL(uriString);
		}
		catch (MalformedURLException ex) { }

		return url;
	}

	/**
	 * Examines the name and returns appropriate information.
	 *
	 * @param moduleName module name
	 * @param name name to check.
	 *
	 * @author jacenty
	 */
	private final DBReply execWhatis(String moduleName, String name) throws Exception
	{
		DBModule module = Database.getModuleByName(moduleName);

		OID[] submodules = module.getSubmodules();
		for(OID submodule : submodules)
			if(new DBModule(submodule).getName().equals(name))
				return DBReply.createOKReply(qresEncoder.encodeResult(new StringResult("'" + name + "' is a submodule")));

		String message = "Name '" + name + "' not found in module " + module.getModuleGlobalName();

		OID[] metaOids = module.getMetabaseEntry().derefComplex();
		for(OID metaOid : metaOids)
		{
			if(metaOid.getObjectName().equals(name))
			{
				MBObject metaObject = new MBObject(metaOid);
				MetaObjectKind kind = metaObject.getObjectKind();

				switch (kind)
				{
					case PRIMITIVE_TYPE_OBJECT:
						message = "Name '" + name + "' is a primitive object";
						break;
					case STRUCT_OBJECT:
						message = "Name '" + name + "' is a structure";
						break;
					case VARIABLE_OBJECT:
						message = "Name '" + name + "' is a variable";
						break;
					case TYPEDEF_OBJECT:
						message = "Name '" + name + "' is type definition";
						break;
					case CLASS_OBJECT:
						message = "Name '" + name + "' is a class";
						break;
					case PROCEDURE_OBJECT:
						message = "Name '" + name + "' is a procedure";
						break;
					case LINK_OBJECT:
						message = "Name '" + name + "' is a link";
						break;
					case BINARY_OPERATOR_OBJECT:
						message = "Name '" + name + "' is a binary operator";
						break;
					case UNARY_OPERATOR_OBJECT:
						message = "Name '" + name + "' is an unary operator";
						break;
					case VIEW_OBJECT:
						message = "Name '" + name + "' is a view";
						break;
					case VIRTUAL_VARIABLE_OBJECT:
						message = "Name '" + name + "' is a virtual variable";
						break;
					case INDEX_OBJECT:
						message = "Name '" + name + "' is an index";
						break;
					case ENDPOINT_OBJECT:
						message = "Name '" + name + "' is an endpoint";
						break;
					case ANNOTATED_VARIABLE_OBJECT:
						message = "Name '" + name + "' is an annotated variable";
						break;

					default:
						message = "Name '" + name + "' is an unknown object";
						break;
				}
			}
		}

		return DBReply.createOKReply(qresEncoder.encodeResult(new StringResult(message)));
	}

	private final DBReply execSetClientName(String name){
		if  (name.equals("grid_client"))
			this.qresEncoder = new QueryResultEncoder(odra.virtualnetwork.facade.Config.repoIdentity, Session.getUserContext().getModule(), true);
		return DBReply.createOKReply();
	}

	/**
	 * Examines if the name of the given type exists.
	 *
	 * @param moduleName module name
	 * @param type object type
	 * @param name object name
	 *
	 * @author jacenty
	 */
	private final DBReply execExists(String moduleName, String type, String name) throws Exception
	{
		boolean exists = false;

		DBModule module = Database.getModuleByName(moduleName);

		if(type.equals("module"))
		{
			OID[] submodules = module.getSubmodules();
			for(OID submodule : submodules)
				if(new DBModule(submodule).getName().equals(name))
				{
					exists = true;
					break;
				}
		}
		else if(type.equals("user"))
		{
			DBModule adminModule = Database.getModuleByName("admin");

			OID agg = adminModule.findFirstByNameId(Names.S_SYSUSERS_ID, adminModule.getDatabaseEntry());
			OID[] users = agg.derefComplex();
			for(OID oid : users) {
				if(oid.derefComplex()[0].derefString().equals(name))
				{
					exists = true;
					break;
				}
			}
		}
		else if(type.equals("view"))
		{
			OID metaOid = module.findFirstByName(name, module.getMetabaseEntry());
			exists = metaOid != null && new MBView(metaOid).isValid();
		}
		else if(type.equals("index"))
		{
			OID metaOid = module.findFirstByName(name, module.findFirstByNameId(Names.S_SYSINDICES_ID, module.getDatabaseEntry()));
			exists = metaOid != null && new MBIndex(metaOid).isValid();
		}
		else if(type.equals("endpoint"))
		{
			IEndpointFacade em = WSManagersFactory.createEndpointManager();
			exists = em.endpointExist(name);
		}
		else if(type.equals("link"))
		{
			OID metaOid = module.findFirstByName(name, module.getMetabaseEntry());
			exists = metaOid != null && new MBLink(metaOid).isValid();
		}

		return DBReply.createOKReply(qresEncoder.encodeResult(new BooleanResult(exists)));
	}

	private final DBReply execAddRole(String rolename) throws DatabaseException, RDNetworkException {
		RoleManager.registerSystemRole(rolename);
		return DBReply.createOKReply(rolename.getBytes());
	}

	private final DBReply execGrantPrivilege(String[] params) throws DatabaseException, RDNetworkException, AccessControlException {

		String role 	= params[0];
		String typeStr 	= params[1];
		String obj 		= params[2];
		int mode  = AccessControl.getModeAsInt(params[3]);
		int value = AccessControl.getValueAsInt(params[4]);
		OID object=null;
		int type = Integer.parseInt(typeStr);

		if (!obj.equals("")){

			OID mod ;
			String strObj, strLoc;
			int idx = obj.lastIndexOf('.');
			strObj = obj.substring(idx+1,obj.length());
			strLoc = obj.substring(0,idx);
			System.out.println("strObj " +strObj);
			System.out.println("strLoc "+ strLoc);

			mod = Database.getModuleByName(strLoc).getOID();
			int numOfchildren = mod.getChildAt(10).countChildren();
			OID data[] = mod.getChildAt(10).derefComplex();

			for(OID child: data){
				if(child.getObjectName().equals(strObj) && child.getChildAt(0).derefInt() == type ) {
					object = child;
					AccessControl.grantPrivilegeToRole(role, type, object, mode, value, 0);
					return DBReply.createOKReply();
				}

			}
			return DBReply.createERRReply("Can't find object", DBReply.STAT_DATABASE_ERROR);

		} else object = null;
		AccessControl.grantPrivilegeToRole(role, type, object, mode, value, 0);

		return DBReply.createOKReply();
	}
}
