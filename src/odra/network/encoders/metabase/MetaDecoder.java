package odra.network.encoders.metabase;

import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.logging.Level;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBLink;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MetaBase;
import odra.db.objects.meta.MetaObjectKind;
import odra.db.objects.meta.MetabaseManager;
import odra.db.schema.OdraProcedureSchema;
import odra.db.schema.OdraVariableSchema;
import odra.db.schema.OdraViewSchema;
import odra.db.schema.ProcArgument;
import odra.exceptions.rd.RDMetaDecodeException;
import odra.exceptions.rd.RDNetworkException;
import odra.sbql.builder.DatabaseLinkMetaReferenceLinker;
import odra.system.config.ConfigServer;

/**	
 *	This class deserializes Meta Data
 *
 *	The format of replies is defined in MetaEncoder class 
 *	@see odra.network.transport.MetaEncoder
 *		
 * @author murlewski
 */

public class MetaDecoder {
	private ByteBuffer buf;

	private MetaBase metaBase;
	private MetabaseManager metamanager;

	private DBLink link;

	private int noEntries;

	// <remote name, local name>
	//private Hashtable<String, String> structsNames;

	private Hashtable<String, OID> metaStructs;

	/**
	 * Default constructor for MetaDecoder
	 * 
	 * @param linkDB
	 *           DBLink which points to remote schema.
	 */
	public MetaDecoder(MetaBase meta) {
		metaBase = meta;
		metamanager = new MetabaseManager(metaBase);
		metaStructs = new Hashtable<String, OID>();
	}

	/**
	 * The main method responsible for decoding MetaBase from array of bytes.
	 * 
	 * @param data
	 *           byte array which contains raw MetaBase
	 * @throws RDNetworkException
	 * @throws DatabaseException
	 */
	public MetaBase decodeMeta(byte[] data) throws RDMetaDecodeException {
		try {
			metamanager.deleteMetaBaseContent();

			buf = ByteBuffer.wrap(data);

			noEntries = buf.getInt();
			long serial = buf.getLong();
			int structsNo = buf.getInt();
			this.createMetaStructsSkeletons(structsNo);
			for (int i = 0; i < noEntries; i++) {
				decodeEntry(metaBase.getMetabaseEntry());
			}

			metaBase.setSerial( serial );			
			new DatabaseLinkMetaReferenceLinker().bindMetaBaseReferences(metaBase);
			
		} catch (Exception e) {
			ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Exception during metadata decoding", e);
			ConfigServer.getLogWriter().flushConsole();

			if (ConfigServer.DEBUG_EXCEPTIONS) e.printStackTrace();

			throw new RDMetaDecodeException(e.getMessage());
		}

		return metaBase;
	}

	

	private void decodeEntry(OID p) throws RDMetaDecodeException, DatabaseException
	{

		int metaType = buf.getInt();
		MetaObjectKind kind = MetaObjectKind.getKindForInteger(metaType);
		switch (kind)
		{
			case VARIABLE_OBJECT:
				decodeMBVariable();
				break;
			case PRIMITIVE_TYPE_OBJECT:
				decodeMBPrimitive();
				break;
			case STRUCT_OBJECT:
				decodeStruct_Object();
				break;
			case TYPEDEF_OBJECT:
				decodeTypeDef_Object();
				break;
			case PROCEDURE_OBJECT:
				decodeMBProcedure();
				break;
			case VIEW_OBJECT:
				decodeMBView();
				break;
			default:
				throw new RDMetaDecodeException("unknown meta type, type=" + metaType);

		}
	}

	private OID decodeMBProcedure() throws RDMetaDecodeException, DatabaseException {
	    OdraProcedureSchema procInfo = this.readProcedure();

		OID procOID = metamanager.createMetaProcedure(procInfo);
		

		return procOID;
	}

	private OdraProcedureSchema readProcedure() throws RDMetaDecodeException, DatabaseException {
			    
		
		String name = this.readString();

		int min = buf.getInt();
		int max = buf.getInt();
		int refs = buf.getInt();

		
		String remoteType = this.readString();
		String typeName = this.getLocalTypeName(remoteType);
		
		// read AST
		int astLen = buf.getInt();
		byte[] astBody = new byte[astLen];
		buf.get(astBody);

		 
		int argsNo = buf.getInt();
		ProcArgument[] args = new ProcArgument[argsNo];  
		
		for (int i = 0; i < argsNo; i++) {
			if (buf.getInt() != MetaObjectKind.VARIABLE_OBJECT.kindAsInt()) throw new RDMetaDecodeException(
						"unknown procedure argument, expected variable");

			args[i] = new ProcArgument(readVariable());
		}
		OdraProcedureSchema procSchema = new OdraProcedureSchema(name, typeName, args, astBody, min, max, refs);
		return procSchema;
	}

	private OID decodeMBVariable() throws RDMetaDecodeException, DatabaseException {
		OdraVariableSchema vi = readVariable();

		OID oid = metamanager.createMetaVariable(vi);

		return oid;
	}

	private OdraVariableSchema readVariable() throws DatabaseException, RDMetaDecodeException {
			    
		int min = buf.getInt();
		int max = buf.getInt();

		// name		
		String name = this.readString();
		// type		
		String remoteType = this.readString();
		String type = this.getLocalTypeName(remoteType);
		
		int ref = buf.getInt();
		OdraVariableSchema ovs = new OdraVariableSchema(name, type, min, max, ref);
		
		return ovs;
	}

	private void decodeMBPrimitive() throws RDMetaDecodeException {
		int kind = buf.getInt();

		throw new RDMetaDecodeException("decode MBPrimitive is not implemented");
	}

	private void decodeStruct_Object() throws RDMetaDecodeException, DatabaseException {
		
		String name = this.readString();
		int filedsNO = buf.getInt();

		OID mbStruct = this.metaStructs.get(name);
//		if(mbStruct == null)
//		    throw new RDMetaDecodeException("unknown structure name " + name);

		

		for (int i = 0; i < filedsNO; i++) {
			if (buf.getInt() != MetaObjectKind.VARIABLE_OBJECT.kindAsInt()) throw new RDMetaDecodeException(
						"unknown structure ,  expected variable");

			OdraVariableSchema vi = readVariable();
			if(mbStruct != null)
			    new MBStruct(mbStruct).createField(vi.getName(), vi.getMinCard(), vi.getMaxCard(), vi.getTName(), vi.getRefLevel());

		}
	}

	private void decodeTypeDef_Object() throws RDMetaDecodeException, DatabaseException {
		// TODO dla struktury jest nowa nazwa wygenerowana

		// name		
		String name = this.readString();

		// type
		String type = this.readString();

		boolean isDistinct = buf.get() > 0 ? (boolean) true : (boolean) false;

		String localTypeName = this.getLocalTypeName(type);

		OID oidTypeDef = metamanager.createMetaTypeDef(name, localTypeName, isDistinct);

	}

	private void decodeMBView() throws RDMetaDecodeException, DatabaseException {
		OdraViewSchema vinfo = readView();	
		OID viewOID = metamanager.createMetaView(vinfo);

	}

	

	/**
	 * Reads a View metadata from a buffer and stores metada as ViewInfo object t
	 * 
	 * @return ViewInfo containing view metadata
	 * @throws RDMetaDecodeException
	 */
	private OdraViewSchema readView() throws RDMetaDecodeException, DatabaseException {
		

		// view name		
		String name = this.readString();
		OdraViewSchema viewSchema = new OdraViewSchema(name, new OdraProcedureSchema());
		ConfigServer.getLogWriter().getLogger().log(Level.FINEST, " reading View : " + name);

		// get VIRTUAL OBJECTS 
		if (buf.getInt() != MetaObjectKind.VIRTUAL_VARIABLE_OBJECT.kindAsInt()) throw new RDMetaDecodeException(
					"unknown structure argument,  expected VIRTUAL_VARIABLE_OBJECT");


		OdraVariableSchema vvari = this.readVirtualVariable();
		viewSchema.setVirtualObject(vvari);

		// get procedures
		int procsLen = buf.getInt();
		for (int i = 0; i < procsLen; i++) {
			if (buf.getInt() != MetaObjectKind.PROCEDURE_OBJECT.kindAsInt()) throw new RDMetaDecodeException(
						"unknown structure argument,  expected PROCEDURE_OBJECT");

			OdraProcedureSchema pi = readProcedure();
			viewSchema.addGenericProcedure(pi);
		}



		// get view 'static' fields (variables, procedures)
		int statFieldsLen = buf.getInt();
		for (int i = 0; i < statFieldsLen; i++) {
			int kind = buf.getInt();

			if (kind == MetaObjectKind.VARIABLE_OBJECT.kindAsInt()) viewSchema.addViewField(readVariable());
			else if (kind == MetaObjectKind.PROCEDURE_OBJECT.kindAsInt()) viewSchema.addViewField(readProcedure());
			else throw new RDMetaDecodeException("unknown view 'static' fields ->  " + kind);
		}

		// get subViews
		int subViewsLen = buf.getInt();
		for (int i = 0; i < subViewsLen; i++) {
			if (buf.getInt() != MetaObjectKind.VIEW_OBJECT.kindAsInt()) throw new RDMetaDecodeException(
						"unknown structure argument,  expected VIEW_OBJECT");

			OdraViewSchema subView = readView();
			viewSchema.addViewField(subView);
		}

		return viewSchema;
	}

	

	private OdraVariableSchema readVirtualVariable() throws RDMetaDecodeException, DatabaseException {
		OdraVariableSchema vari = this.readVariable();			
		return vari;
		
		
	}
	
	private void createMetaStructsSkeletons(int number) throws DatabaseException{
	    for(int i = 0; i < number; i++){
		String remotename = readString();
		OID metaStruct = this.metamanager.createMetaStruct(0);		
		this.metaStructs.put(remotename, metaStruct);
	    }
	}
	
	private String readString(){
	    int nameLen = buf.getInt();
	    byte[] bytea = new byte[nameLen];
		buf.get(bytea);
		return new String(bytea);
	}
	
	private String getLocalTypeName(String remotename)throws DatabaseException{
	    OID metastruct = this.metaStructs.get(remotename);
	    if(metastruct != null)
		return metastruct.getObjectName();
	    return remotename;
	}
}
