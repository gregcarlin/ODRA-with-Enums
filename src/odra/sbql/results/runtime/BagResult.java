package odra.sbql.results.runtime;

public class BagResult extends CollectionResult {

	/* (non-Javadoc)
	 * @see odra.sbql.results.runtime.CollectionResult#toString()
	 */
	public String toString() {
		
		return "bag(" + super.toString() + ")";
	}

}
