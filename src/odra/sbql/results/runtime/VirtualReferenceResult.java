package odra.sbql.results.runtime;

import odra.db.OID;

public class VirtualReferenceResult extends ReferenceResult {
	private Result seed;
	
	
	public VirtualReferenceResult(OID value, Result seed) {
		super(value);
		this.seed = seed;
	}
	
	

	/**
	 * @return the seed
	 */
	public Result getSeed() {
	    return seed;
	}

	public boolean equals(Object arg0) {
		if (arg0 instanceof VirtualReferenceResult) {
			VirtualReferenceResult sr2 = (VirtualReferenceResult) arg0;
			if(value.equals(sr2.value)){
				if(getSeed().elementsCount() == sr2.getSeed().elementsCount()){
					SingleResult[] seeds1 =  getSeed().elementsToArray();
					SingleResult[] seeds2 =  sr2.getSeed().elementsToArray();
					for(int i = 0; i < seeds1.length; i++){
						if(!(seeds1[i].equals(seeds2[i])))
							return false;
					}
					return true;
				}
			}
			return false;			
		}
		return false;
	}

	public int hashCode() {
		int hash = value.hashCode();
		SingleResult[] sinres = getSeed().elementsToArray();
		for(int i = 0; i < sinres.length; i++)
			hash = ((hash << 5) + hash) + sinres[i].hashCode();
		
		return hash;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.results.runtime.ReferenceResult#clone()
	 */
	public VirtualReferenceResult clone() throws CloneNotSupportedException {
	    VirtualReferenceResult res = new VirtualReferenceResult(this.value, this.getSeed());
	    res.parent = this.parent;
	    res.refFlag = this.refFlag;
	    return res;
	}
	
}
