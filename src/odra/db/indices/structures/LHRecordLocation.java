package odra.db.indices.structures;

import odra.db.indices.updating.IndexRecordLocation;

public class LHRecordLocation implements IndexRecordLocation {
	
	public int bucketNumber;
	public byte[] nonkeyArray;
	
	public LHRecordLocation(int bucketNumber, byte[] nonkeyArray) {
		super();
		this.bucketNumber = bucketNumber;
		this.nonkeyArray = nonkeyArray;
	}
	
}
