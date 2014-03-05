package odra.sbql.results.runtime;

import odra.db.OID;

public class ReferenceResult extends SingleResult {
	public OID value;
	public boolean refFlag = false; //should deref be performed on this reference?
	public ReferenceResult parent; //the reference to the parent OID (== null for root objects)
	
	public ReferenceResult(OID value) {
		this.value = value;
	}
	public ReferenceResult(OID value, ReferenceResult parent) {
		this.value = value;
		this.parent = parent;
	}
	public SingleResult[] fieldsToArray() {
		return new SingleResult[] { this };
	}
	
	public boolean equals(Object arg0) {
		if (arg0 instanceof ReferenceResult) {
			return value.equals(((ReferenceResult) arg0).value); 
		}
		return false;
	}

	public int hashCode() {
		return value.hashCode();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public ReferenceResult clone() throws CloneNotSupportedException {
	    ReferenceResult res = new ReferenceResult(this.value, this.parent);
	    res.refFlag = this.refFlag;
	    return res;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		return "&" + this.value.toString();
	}
	
	
}
