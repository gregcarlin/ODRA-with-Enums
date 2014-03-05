package odra.sbql.results.runtime;

public class DoubleResult extends ComparableResult {
	public double value;
	
	public DoubleResult(double value) {
		this.value = value;
	}
	
	public SingleResult[] fieldsToArray() {
		return new SingleResult[] { this };
	}

	public int compareTo(Object sres) {
		double dvalue;
		if(sres instanceof DoubleResult)
			dvalue = ((DoubleResult) sres).value;
		else 		
			// FIXME: Remove comparing to IntegerResult (IntegerResult should be converted to DoubleResult before using CompareTo
			dvalue = ((IntegerResult) sres).value;
		
		if (value < dvalue)
			return -1;
		else if (value > dvalue)
			return 1;
		else
			return 0;
	}
	
	public boolean equals(Object arg0) {
		if (arg0 instanceof DoubleResult) {
			return value == ((DoubleResult) arg0).value; 
		}
		
		return false;
	}

	public int hashCode() {
		return Double.valueOf(value).hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {

		return Double.toString(this.value);
	}
	
}
