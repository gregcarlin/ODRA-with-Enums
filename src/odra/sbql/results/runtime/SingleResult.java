package odra.sbql.results.runtime;


public abstract class SingleResult extends Result {
	public abstract SingleResult[] fieldsToArray();

	public int elementsCount() {
		return 1;
	}
	
	public SingleResult elementAt(int i) {
		assert i == 0: "Single result is only one element. You request is " + (i+1) +" element"; 

		return this;
	}
	
	public SingleResult[] elementsToArray() {
		return new SingleResult[] { this };
	}

	public boolean equals(Object arg0) {
		throw new RuntimeException("equality operation cannot be applied to " + arg0.getClass().getCanonicalName() + " type.");
	}

	public int hashCode() {
		throw new RuntimeException("hashing operation cannot be applied to " + getClass().getCanonicalName() + " type.");
	}

}
