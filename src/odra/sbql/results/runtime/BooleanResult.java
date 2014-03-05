package odra.sbql.results.runtime;

public class BooleanResult extends ComparableResult {
	public boolean value;

	public BooleanResult(boolean value) {
		this.value = value;
	}
	
	public SingleResult[] fieldsToArray() {
		return new SingleResult[] { this };
	}

	public int compareTo(Object sres) {
	    assert sres instanceof BooleanResult: "param instanceof BooleanResult";
		
		BooleanResult bres = (BooleanResult) sres;
	
		if (!value && bres.value)
			return -1;
		else if (value && !bres.value)
			return 1;
		else
			return 0;
	}
	
	public boolean equals(Object arg0) {
		if (arg0 instanceof BooleanResult) {
			return value == ((BooleanResult) arg0).value; 
		}
		return false;
	}

	public int hashCode() {
		return value?1:0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		
		return Boolean.toString(this.value);
	}
	
}
