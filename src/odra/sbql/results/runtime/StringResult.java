package odra.sbql.results.runtime;

import java.text.Collator;
import java.util.Locale;

public class StringResult extends ComparableResult {
	public String value;
	
	public StringResult(String value) {
		this.value = value;
	}
	
	public SingleResult[] fieldsToArray() {
		return new SingleResult[] { this };
	}
	
	public int compareTo(Object sres) {
	    assert sres instanceof StringResult: "param instanceof StringResult";
		StringResult stres = (StringResult) sres;
	
		return Collator.getInstance(Locale.getDefault()).compare(value, stres.value);
	}
	
	public boolean equals(Object arg0) {
		if (arg0 instanceof StringResult) {
			return value.equals(((StringResult) arg0).value); 
		}
		
		return false;
	}

	public int hashCode() {
		return value.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.value;
	}
	
}
