package odra.network.encoders.metabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBPrimitiveType;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBTypeDef;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MBView;
import odra.db.objects.meta.MBVirtualVariable;
import odra.db.objects.meta.MetaObjectKind;
import odra.exceptions.rd.RDNetworkException;
import odra.network.transport.AutoextendableBuffer;
import odra.system.config.ConfigServer;

/*	
 *	This class serializes Meta Data 
 *	
 *
 *	The format of message is as follows: 	
 *	- number of metadata entries 	(4 bytes ) 
 *	- metabase serial				(8 bytes )
 *
 *
 *	- metadata entry:
 *		- type of n entry			(4 bytes )
 *		- data for n entry			(n bytes )
 *		
 *
 * *************
 *	MBVariable entry:
 *	- meta type 		( 4 bytes ) 	
 *	- min 				( 4 bytes )
 *	- max 				( 4 bytes )
 *	- name length 		( 4 bytes )
 *	- name 				( n bytes )
 *	- type name length 	( 4 bytes )
 *	- type 				( n bytes )
 *  - ref				( 4 bytes )			
 * 	
 * *************
 * 	PrimitiveType entry:
 *	- meta type 		( 4 bytes ) 	
 *	- kind				( 4 bytes )
 * 
 * 
 * *************
 *	MBStruct entry:
 *	- meta type 		( 4 bytes ) 	
 *	- name length 		( 4 bytes )
 *	- name 				( n bytes )
 *
 *	- fields count			( 4 bytes )
 *	- field n as MBVariable ( n bytes )	
 *
 *
 * *************
 *	MBTypeDef entry:
 *	- meta type 		( 4 bytes ) 	
 *	- name length 		( 4 bytes )
 *	- name 				( n bytes )
 *	- type name length 	( 4 bytes )
 *	- type 				( n bytes )
 *	- isDistinct		( 1 byte)	
 *
 *
 * *************
 *	MBProcedure entry:
 *	- meta type 		( 4 bytes ) 
 *	- name length 		( 4 bytes )
 *	- name 				( n bytes )
 *	- min 				( 4 bytes )
 *	- max 				( 4 bytes )
 *	- refs 				( 4 bytes )
 *  - return type length	( 4 bytes )
 *  - return type name		( n bytes )
 *  
 *  - AST body size			( 4 bytes )
 *  - AST body 				( n bytes )	
 *	
 *	- args. count			( 4 bytes )
 *	- arg n as MBVariable	( n bytes )
 *	 
 *
 * *************
 *	MBViewDef entry:
 *	- meta type 		( 4 bytes ) 	
 *  - name length 		( 4 bytes )
 *  - name 				( n bytes )
 *  
 *  - generic procedure as MBVirtualObjectsProcedure 	(n bytes)
 *  
 *  - procedures count									( 4 bytes ) 
 *  - procedure n as MBProcedure						( n bytes )
 *  
 *  - virtual fields count								( 4 bytes ) 
 *  - virtual field n as MBVirtualObjectsProcedure		( n bytes )
 *  
 *  - view fields count									( 4 bytes ) 
 *  - type of n view field 								( 4 bytes )
 *  - data for n entry									( n bytes )
 *  
 *  - subviews count 									( 4 bytes ) 
 *  - subview n as  MBViewDef							( n bytes )
 *  
 *  
 *  *************
 *  MBVirtualObjectsProcedure entry:
 *  - meta type 		( 4 bytes ) 	
 *	- name length 		( 4 bytes )
 *	- name 				( n bytes )
 *	- min 				( 4 bytes )
 *	- max 				( 4 bytes )
 *	- refs 				( 4 bytes )
 *  - return type length	( 4 bytes )
 *  - return type name		( n bytes )
 *  
 *  - AST body size			( 4 bytes )
 *  - AST body 				( n bytes )	
 *  
 *	- view name length		( 4 bytes )		// view to which the procedure belongs
 *	- view name				( n bytes )
 *
 *	- args. count			( 4 bytes )
 *	- arg n as MBVariable	( n bytes )
 *  
 * @author murlewski
 */


public class MetaEncoder
{

	private AutoextendableBuffer buf = null;

	private int metaEntryNO = 0;
	private Map<String, OID> encoded = new HashMap<String, OID>();
	private List<String> structNames = new Vector<String>();

	public MetaEncoder()
	{

		this.buf = new AutoextendableBuffer();
	}

	/**
	 * This method serializes metadata into array of bytes
	 * 
	 * @param mod
	 *            DBModule containing metabase
	 * @param oidParent
	 *            OID to metabase entry
	 * 
	 * @return serialized metabase into array of bytes
	 * 
	 * @throws RDNetworkException
	 * @throws DatabaseException
	 */

	public byte[] encodeMeta(DBModule mod, OID oidParent) throws RDNetworkException
	{
	    AutoextendableBuffer finallBuffer = new AutoextendableBuffer();
		try
		{

			if (!oidParent.isComplexObject())
				assert false :"Parent object must be complex";

			// allocate space for no. of meta entries
//			buf.putInt(0);
//			buf.putLong( mod.getSerial() );
						
			for (OID c : oidParent.derefComplex())
			{
				if (c.isComplexObject())
				{
					encodeMetaEntry(c);
				}
				else if (c.isAggregateObject())
				{
					for (OID ac : c.derefComplex())
					{
						encodeMetaEntry(ac);
					}
				}
				else
					throw new RDNetworkException("not a complex/aggregate object " + c.getObjectName());
			}
			
			//encode types only if required by variables and procedures
			//
//			for(OID structid : this.requiredTypes){			    
//				this.encodeMetaEntry(structid, true);			    
//			}
			finallBuffer.putInt(this.metaEntryNO);
			finallBuffer.putLong( mod.getSerial() );
			finallBuffer.putInt(this.structNames.size());			
			for(String structname : this.structNames){
			    this.encodeString(structname, finallBuffer);
			}
			finallBuffer.put(this.buf.getBytes());
			// Insert at the beginning of buffer no. of objects
//			int pos = buf.position();
//			buf.rewind();
//			buf.putInt(this.metaEntryNO);
//			buf.position(pos);

		}
		catch (DatabaseException e)
		{
			ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Exception during metadata encoding", e);
			if (ConfigServer.DEBUG_EXCEPTIONS)
				e.printStackTrace();

			throw new RDNetworkException(e.getMessage());
		}		
		return finallBuffer.getBytes();

	}

	private void encodeMetaEntry(OID oidParent) throws RDNetworkException, DatabaseException
	{
		if(this.encoded.get(oidParent.getObjectName()) != null)
		    return;
		MetaObjectKind kind = new MBObject(oidParent).getObjectKind();
		switch (kind)
		{
			case VARIABLE_OBJECT:
				encodeMBVariable(oidParent);
				this.setEncoded(oidParent);
				break;
			case PRIMITIVE_TYPE_OBJECT:
				encodePrimitiveType(oidParent);
				this.setEncoded(oidParent);
				break;
			case STRUCT_OBJECT:
				encodeStruct_Object(oidParent);
				this.setEncoded(oidParent);
				break;
			case TYPEDEF_OBJECT:
				encodeTypeDef_Object(oidParent);
				this.setEncoded(oidParent);
				break;
			case PROCEDURE_OBJECT:
				encodeMBProcedure(oidParent);
				this.setEncoded(oidParent);
				break;
			case VIEW_OBJECT:
				encodeMBView(oidParent);
				this.setEncoded(oidParent);
				break;
			default:
				break;
		}
		
	}

	private void encodeMBProcedure(OID parent) throws DatabaseException, RDNetworkException
	{

		MBProcedure mproc = new MBProcedure(parent);

		buf.putInt(MetaObjectKind.PROCEDURE_OBJECT.kindAsInt());
		//encode procedure name
		this.encodeString(mproc.getName(), buf);


		buf.putInt(mproc.getMinCard());
		buf.putInt(mproc.getMaxCard());
		buf.putInt(mproc.getRefIndicator());
		//encode procedure type name
		this.encodeString(mproc.getTypeName(), buf);
		//if type is struct mark it as required
		this.enforceRequiredIfUnnamedStructureType(mproc.getType());

		
		buf.putInt( mproc.getAST().length );
		buf.put( mproc.getAST() );

		OID[] args = mproc.getArguments();

		buf.putInt(args.length);

		for (int i = 0; i < args.length; i++)
		{
			encodeMBVariable(args[i]);
		}
		
		
	}

	private void encodeMBVariable(OID parent) throws DatabaseException, RDNetworkException
	{
		MBVariable o = new MBVariable(parent);

		buf.putInt(MetaObjectKind.VARIABLE_OBJECT.kindAsInt());

		buf.putInt(o.getMinCard());
		buf.putInt(o.getMaxCard());
		//encode variable name
		this.encodeString(o.getName(), buf);


		//encode variable typename
		this.encodeString(o.getTypeName(), buf);
		this.enforceRequiredIfUnnamedStructureType(o.getType());

		buf.putInt(o.getRefIndicator());
		
		

	}

	private void encodePrimitiveType(OID parent) throws DatabaseException
	{
		MBPrimitiveType m = new MBPrimitiveType(parent);

		buf.putInt(MetaObjectKind.PRIMITIVE_TYPE_OBJECT.kindAsInt());
		buf.putInt(m.getTypeKind().kindAsInt());
	}

	private void encodeStruct_Object(OID parent) throws RDNetworkException, DatabaseException
	{
		MBStruct mbStruct = new MBStruct(parent);

		buf.putInt(MetaObjectKind.STRUCT_OBJECT.kindAsInt());

		//encode struct name
		this.encodeString(mbStruct.getName(), buf);


		OID[] filedOIDs = mbStruct.getFields();
		buf.putInt(filedOIDs.length);

		for (OID oid : filedOIDs)
		{
			encodeMBVariable(oid);
		}

	}

	private void encodeTypeDef_Object(OID parent) throws RDNetworkException, DatabaseException
	{
		MBTypeDef mbType = new MBTypeDef(parent);

		buf.putInt(MetaObjectKind.TYPEDEF_OBJECT.kindAsInt());

		//encode typedef  name
		this.encodeString(mbType.getName(), buf);


		// encode typedef type name
		this.encodeString(mbType.getTypeName(), buf);
		this.enforceRequiredIfUnnamedStructureType(mbType.getType());


		buf.put(mbType.isDistinct() ? (byte) 1 : (byte) 0);
		
		

	}

	private void encodeMBView(OID parent) throws RDNetworkException, DatabaseException
	{
		MBView mbv = new MBView(parent);
		buf.putInt(MetaObjectKind.VIEW_OBJECT.kindAsInt());

		//encode view name
		this.encodeString(mbv.getName(), buf);


		// encode virtual variable object
		encodeMBVirtualVariable(mbv.getVirtualObject());

		// encode GenProcsEntries
		OID[] prcs = mbv.getGenProcsEntry().derefComplex();
		buf.putInt(prcs.length);
		for (int j = 0; j < prcs.length; j++)
			encodeMBProcedure(prcs[j]);

		// encode virtual fields
//		OID[] vflds = mbv.getVirtualFieldsEntry().derefComplex();
//		buf.putInt(vflds.length);
//		for (int j = 0; j < vflds.length; j++)
//			encodeMBVirtualObjectsProcedure(vflds[j]);

		// encode view 'static' fields (variables, procedures)
		OID[] flds = mbv.getViewFieldsEntry().derefComplex();
		buf.putInt(flds.length);
		for (int j = 0; j < flds.length; j++)
		{
			if (MetaObjectKind.VARIABLE_OBJECT == new MBObject(flds[j]).getObjectKind())
				encodeMBVariable(flds[j]);
			else
				encodeMBProcedure(flds[j]);
		}

		// encode subviews
		OID[] sbvs = mbv.getSubViewsEntry().derefComplex();
		buf.putInt(sbvs.length);
		for (int j = 0; j < sbvs.length; j++)
			encodeMBView(sbvs[j]);

	}

	
	private void encodeMBVirtualVariable(OID parent) throws DatabaseException
	{
		MBVirtualVariable o = new MBVirtualVariable(parent);
		
		buf.putInt(MetaObjectKind.VIRTUAL_VARIABLE_OBJECT.kindAsInt());

		buf.putInt(o.getMinCard());
		buf.putInt(o.getMaxCard());
		
		//encode virtual variable name
		this.encodeString(o.getName(), buf);
		
		//encode virtual variable type name
		this.encodeString(o.getTypeName(), buf);
		this.enforceRequiredIfUnnamedStructureType(o.getType());
		
		buf.putInt(o.getRefIndicator());
		
		//encode virtual variable view name
	//	this.encodeString(o.getView().getObjectName(), buf);
		
		
		

	}
	
	private final void setEncoded(OID oid) throws DatabaseException{
	    this.encoded.put(oid.getObjectName(), oid);
	    this.metaEntryNO ++;
	}
	
	private final void enforceRequiredIfUnnamedStructureType(OID oid) throws DatabaseException{
	    String structname = oid.getObjectName(); 
	    if(structname.startsWith("$struct_")){
		if(!this.structNames.contains(structname)){
		    this.structNames.add(structname);
//		    this.requiredTypes.add(oid);
		}
	    }
	}
	
	private final void encodeString(String name, AutoextendableBuffer buffer){
	    
	    	byte[] barr = name.getBytes();		
	    	buffer.putInt(barr.length);
	    	buffer.put(barr);
	}
}
