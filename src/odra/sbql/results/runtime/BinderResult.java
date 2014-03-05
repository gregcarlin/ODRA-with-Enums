package odra.sbql.results.runtime;

public class BinderResult extends SingleResult {

	private String name;
	public Result value;
	
	public BinderResult(String name, Result value) {
		this.name = name;
		this.value = value;
	}
	
	public SingleResult[] fieldsToArray() {
		return new SingleResult[] { this };
	}
	
	public boolean equals(Object arg0) {
		if (arg0 instanceof BinderResult) {
			BinderResult r2 = (BinderResult) arg0; 
			if (!name.equals(r2.name))
				return false;
			return value.equals(r2.value);
		}
		return false;
	}

	public int hashCode() {
		int hash = name.hashCode();
		return ((hash << 5) + hash) + value.hashCode();
	}



	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		
		return this.name + "(" + this.value.toString() + ")";
	}

}
