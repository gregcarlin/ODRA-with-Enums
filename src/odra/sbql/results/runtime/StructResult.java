package odra.sbql.results.runtime;

import java.util.Vector;

public class StructResult extends SingleResult {
    /*generics and foreach not used because of the compatibility with Java 1.4*/
//	private Vector<SingleResult> data = new Vector<SingleResult>();
	private Vector data = new Vector();
	
	public StructResult() {
	}
	
	public StructResult(SingleResult res) {
		data.addElement(res);
	}
	
	public StructResult(SingleResult res1, SingleResult res2) {
		data.addElement(res1);
		data.addElement(res2);
	}

	public void addField(SingleResult res) {
		if(res instanceof StructResult)
		{
		    this.data.addAll(((StructResult)res).data);
//			for(SingleResult sr: ((StructResult)res).fieldsToArray())
//				addField(sr);
		}else
		data.addElement(res);
	}
	
	public void addFieldAt(int i, SingleResult res) {
		if(res instanceof StructResult)
		{
		    this.data.addAll(i, ((StructResult)res).data);
//			for(SingleResult sr: ((StructResult)res).fieldsToArray())
//				addFieldAt(i++,sr);
		}else
		data.add(i, res);
	}
	
	public void removeField(SingleResult res) {
		data.removeElement(res);
	}
	
	public int fieldsCount() {
		return data.size();
	}
	
	public SingleResult fieldAt(int i) {
		return (SingleResult)data.elementAt(i);
	}
	
	public SingleResult[] fieldsToArray() {
		return (SingleResult[])data.toArray(new SingleResult[data.size()]);
	}
	
	public boolean equals(Object arg0) {
		if (arg0 instanceof StructResult) {
			SingleResult[] sres1 = fieldsToArray();
			SingleResult[] sres2 = ((StructResult) arg0).fieldsToArray();

			if (sres1.length != sres2.length)
				return false;

			for (int i = 0; i < sres1.length; i++) {
				if (!sres1[i].equals(sres2[i]))
					return false;
			}
			return true;
		}
		return false;
	}

	public int hashCode() {
		int hash = 0;
		for(int i = 0; i < data.size(); i++)
			hash = ((hash << 5) + hash) + data.get(i).hashCode();
		
		return hash;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("struct{");
		buff.append(data.get(0).toString());
		for(int i =1; i < this.data.size(); i++){
			buff.append(", ");
			buff.append(data.get(i).toString());			
		}
		buff.append("}");
		return buff.toString();
	}
	
}
