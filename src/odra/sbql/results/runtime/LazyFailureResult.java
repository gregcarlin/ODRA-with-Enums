package odra.sbql.results.runtime;

import odra.sbql.results.AbstractQueryResult;

/**
 * @author tkowalski
 *
 */
public final class LazyFailureResult extends SingleResult {
	
	Exception e;

	public LazyFailureResult(Exception e) {
		super();
		this.e = e;
	}

	public Exception getException() {
		return e;
	}

	@Override
	public SingleResult[] fieldsToArray() {
		return null;
	}

		
}
