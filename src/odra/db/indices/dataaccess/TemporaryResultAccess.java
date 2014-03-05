package odra.db.indices.dataaccess;

import java.nio.ByteBuffer;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.recordtypes.MultiKeyRecordType;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.StructResult;
import odra.system.Sizes;

public class TemporaryResultAccess extends DataAccess {

	private BagResult bres;
	
	public TemporaryResultAccess(OID oid, BagResult bres) {
		super(oid);
		this.bres = bres;
	}
	
	public TemporaryResultAccess() {
		super();
	}

	@Override
	public Object key2keyValue(Object key) throws DatabaseException {
		return recordType.keyType.key2KeyValue(key);
	}

	@Override
	public Object record2nonkey(byte[] record) {
		return ((StructResult) bres.elementAt(ByteBuffer.wrap(record).getInt())).fieldAt(0);
	}

	@Override
	public Object nonkey2key(Object nonkey) throws DatabaseException {
		assert false:"unimlemented unused?";
		return null;
	}
	
	@Override
	public byte[] prepareRecordArray(Object key, Object nonkey)
			throws DatabaseException {
		return ByteBuffer.allocate(getNonkeyRecordSize()).putInt((Integer) nonkey).array();
	}

	@Override
	public Object record2keyValue(byte[] record) throws DatabaseException {
		StructResult stres = (StructResult) bres.elementAt(ByteBuffer.wrap(record).getInt());
		
		if (recordType instanceof MultiKeyRecordType) {
			StructResult keyres = new StructResult();
			for (int j = 1; j < stres.fieldsCount(); j++)
				keyres.addField(stres.fieldAt(j));
			return key2keyValue(keyres);			
		} 
			 	
		return key2keyValue(stres.fieldAt(1));		
	}
	
	@Override
	protected int getKindID() {
		return DataAccessKind.TEMPORARYRESULTACCESS_ID;
	}

	@Override
	public int getNonkeyRecordSize() {
		return Sizes.INTVAL_LEN;
	}

	@Override
	public Object getNotFoundValue() {
		return null;
	}

}
