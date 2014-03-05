package odra.sbql.results.runtime;

import java.util.Vector;


public abstract class CollectionResult extends Result {
    /*generics and foreach not used because of the compatibility with Java 1.4*/
    //private Vector<SingleResult> data = new Vector<SingleResult>();
	private Vector data = new Vector();
	
	public void addElement(SingleResult res) {
		data.addElement(res);
	}
	
	public void addAll(Result res){
	    SingleResult[] sres = res.elementsToArray();
		for(int i = 0; i < sres.length; i++)
			data.add(sres[i]);
	}
	
	public void removeElement(SingleResult res) {
		data.removeElement(res);
	}
	
	public void removeAll() {
		data.clear();
	}
	
	public int elementsCount() {
		return data.size();
	}

	public SingleResult elementAt(int i) {
		return (SingleResult)data.elementAt(i);
	}
	
	public SingleResult[] elementsToArray() {
		return (SingleResult[])data.toArray(new SingleResult[data.size()]);
	}
	
	public boolean equals(Object arg0) {
		assert false:"collections comparison unimplemented";
		return false;
	}

	public int hashCode() {
		assert false:"collections comparison unimplemented";
		return 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {	
		StringBuffer buff = new StringBuffer();
		for(int i = 0 ; i < this.data.size(); i++){
			buff.append(data.get(i).toString());
			buff.append(" ");
		}
		return buff.toString();
	}
	
}
