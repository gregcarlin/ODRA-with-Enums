package odra.db.indices;

import java.util.Arrays;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.dataaccess.DataAccess;
import odra.db.indices.dataaccess.DBObjectDirectKeyAccess;
import odra.db.indices.dataaccess.DBObjectToKeyAccess;
import odra.db.indices.recordtypes.*;
import odra.db.indices.structures.LinearHashingMap;
import odra.db.indices.keytypes.*;
import odra.db.objects.data.DBIndex;
import odra.db.objects.data.DBModule;
import odra.sbql.ast.ASTNode;
import odra.sbql.results.runtime.*;

/**
 * This class is responsible for creating index in dynamic mode (typechecking off).
 * This is done by introducing proper DBIndex structure in module.<br>
 * Indices created in dynamic mode cannot be updated or used by index optimizer
 * so are not very important from pragmatic point of view.
 * 
 * @author tkowals
 * @version 1.0
 */
class DynamicGenerateIndex {

	protected static IndexManager idxMgr;

	/**
	 * @param idxMgr index register object
	 * @param node AST of index creating query
	 * @param mod index home module
	 * @param idxname name of an index
	 * @param prog index creating query
	 * @param rParams parameters indicating record types for each key (dense, range or enum)
	 * @return OID of DBIndex object
	 * @throws Exception
	 */
	public static OID createIndex(IndexManager idxMgr, ASTNode node, DBModule mod, String idxname, String prog, String []rParams) throws Exception {

		SafeGenerateIndex.idxMgr = idxMgr;
		DynamicGenerateIndex.idxMgr = idxMgr;  
		
	    BagResult bres = SafeGenerateIndex.getQueryResult(node, mod);
	
		DataAccess dataAccess = getDataAccessKind(bres, node, mod);
	
		RecordType recordType = getRecordTypeKindID(bres);  
		
		boolean[] obligatory = new boolean[recordType.keyCount()];
		Arrays.fill(obligatory, true);
		
		recordType = SafeGenerateIndex.updateRecordTypes(rParams, obligatory, recordType, bres);
			
		OID idxoid = mod.createLinearHashingIndex(idxname, false, recordType, 
				dataAccess, prog, 
				idxMgr.generator.getCode().getByteCode(),
				idxMgr.generator.getConstantPool().getAsBytes());
		LinearHashingMap lhm = (LinearHashingMap) new DBIndex(idxoid).getIndex();
		lhm.initialize(LHBUCKETSCOUNT, LHBUCKETCAPACITY, LHPERSPLITLOAD, LHPERMERGELOAD);
		
		SafeGenerateIndex.insertResult(bres, idxoid);
	
		return idxoid;
	
	}
	
	private static RecordType getRecordTypeKindID(BagResult bres) throws DatabaseException {
	
		StructResult stres;
		SingleResult res;
		ReferenceResult rres;						
		
		stres = ((StructResult) bres.elementAt(0));
	
		int keyCount = stres.fieldsCount() - 1;
		
		RecordType[] recordType = new RecordType[keyCount];		
		for (int j = 0; j < keyCount; j++) {				
			res = stres.fieldAt(j + 1);
			if (res instanceof ReferenceResult) {
				rres = (ReferenceResult) res;
				if (rres.value.isIntegerObject())
					recordType[j] = new SimpleRecordType(new IntegerKeyType());
				else if (rres.value.isDoubleObject())
					recordType[j] = new SimpleRecordType(new DoubleKeyType());
				else if (rres.value.isStringObject())
					recordType[j] = new SimpleRecordType(new StringKeyType());
				else if (rres.value.isBooleanObject())
					recordType[j] = new BooleanEnumRecordType(true);
				else if (rres.value.isDateObject())
					recordType[j] = new SimpleRecordType(new DateKeyType());
				else if (rres.value.isReferenceObject())
					recordType[j] = new SimpleRecordType(new ReferenceKeyType());
				else throw new IndicesException("Key value type not supported for indexing!", null);				
			} 
			else if (res instanceof IntegerResult)  
				recordType[j] = new SimpleRecordType(new IntegerKeyType());
			else if (res instanceof DoubleResult)
				recordType[j] = new SimpleRecordType(new DoubleKeyType());
			else if (res instanceof StringResult)
				recordType[j] = new SimpleRecordType(new StringKeyType());
			else if (res instanceof BooleanResult)
				recordType[j] = new BooleanEnumRecordType(true);
			else if (res instanceof DateResult)
				recordType[j] = new SimpleRecordType(new DateKeyType());
			else throw new IndicesException("Key value type not supported for indexing!", null);			
		}
	
		if (keyCount > 1)
			return new MultiKeyRecordType(recordType);
		
		return recordType[0];
		
	}

	private static DataAccess getDataAccessKind(BagResult bres, ASTNode node, DBModule mod) throws DatabaseException {
	
		if (!(bres.elementAt(0) instanceof StructResult)) 
			throw new IndicesException("Query should return nonkey and key values", null);
			
		StructResult stres = ((StructResult) bres.elementAt(0));
	
		int keyCount = stres.fieldsCount() - 1;
		
		if (keyCount < 1)
			throw new IndicesException("Query should return nonkey and key values", null);
		
		if (!(stres.fieldAt(0) instanceof ReferenceResult))
			throw new IndicesException("Nonkey value must be reference", null);
		
		if ((keyCount ==  1) && (stres.fieldAt(1) instanceof ReferenceResult)) {			
				
			for(int i = 1; i < bres.elementsCount(); i++) {
				stres = ((StructResult) bres.elementAt(i));
				if (stres.fieldsCount() != 2)
					throw new IndicesException("Number of keys is not constant", null);
							
				if (!(stres.fieldAt(0) instanceof ReferenceResult))
					throw new IndicesException("Nonkey value must be reference", null);
				
				if (!(stres.fieldAt(1) instanceof ReferenceResult))
					throw new IndicesException("Key value must be reference", null);							
			}		

			byte[][] n2kBytecode = SafeGenerateIndex.getN2KBytecode(node, mod);
			
			return new DBObjectDirectKeyAccess(n2kBytecode[0], n2kBytecode[1]);
		} 
		
		for(int i = 1; i < bres.elementsCount(); i++) {
			stres = ((StructResult) bres.elementAt(i));
						
			if (!(stres.fieldAt(0) instanceof ReferenceResult))
				throw new IndicesException("Nonkey value must be reference", null);
						
			if (keyCount != stres.fieldsCount() - 1)
				throw new IndicesException("Number of keys is not constant", null);											
		}
		
		byte[][] n2kBytecode = SafeGenerateIndex.getN2KBytecode(node, mod);
		
		return new DBObjectToKeyAccess(n2kBytecode[0], n2kBytecode[1]);
		
	}
	
	public static final int LHBUCKETSCOUNT = 13,
							LHBUCKETCAPACITY = 5, 
							LHPERSPLITLOAD = 75, 
							LHPERMERGELOAD = 65;

	
}
