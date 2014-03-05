package odra.sbql.results.runtime;

public class IntegerResult extends ComparableResult {
	public int value;
	
	public IntegerResult(int value) {
		this.value = value;
	}
	
	public SingleResult[] fieldsToArray() {
		return new SingleResult[] { this };
	}
	
	public int compareTo(Object sres) {
		double dvalue;
		if(sres instanceof IntegerResult)
			dvalue = ((IntegerResult) sres).value;
		else 
			// FIXME: Remove comparing to DoubleResult (IntegerResult should be converted to DoubleResult before using CompareTo
			dvalue = ((DoubleResult) sres).value;
			//IntegerResult ires = (IntegerResult) sres;
	
		if (value < dvalue)
			return -1;
		else if (value > dvalue)
			return 1;
		else
			return 0;
	}
	
	public boolean equals(Object arg0) {
		if (arg0 instanceof IntegerResult) {
			return value == ((IntegerResult) arg0).value; 
		}
		
		return false;
	}

	public int hashCode() {
		return value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {		
		return Integer.toString(this.value);
	}
	
}
