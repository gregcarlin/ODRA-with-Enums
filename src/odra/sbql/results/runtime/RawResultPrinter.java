package odra.sbql.results.runtime;

import java.io.UnsupportedEncodingException;

public class RawResultPrinter {
	/** output encoding name */
	private String outputEncoding = "UTF-8";
	
	public String print(Result res) throws UnsupportedEncodingException {
		return removeLastEol(new String(print(res, "").getBytes(outputEncoding), outputEncoding));
	}

	private String print(Result res, String indend) {
		StringBuffer str = new StringBuffer();
		
		if (res instanceof BagResult) {
			SingleResult[] resarr = res.elementsToArray();
			
			str.append(indend).append("bag {\n");

			for (int i = 0; i < resarr.length; i++)
				str.append(print(resarr[i], " " + indend));

			str.append(indend).append("}\n");
		}
		else if (res instanceof IntegerResult) 
			str.append(indend + ((IntegerResult) res).value + "\n");
		else if (res instanceof StringResult) 
			str.append(indend + "\"" + ((StringResult) res).value + "\"\n");
		else if (res instanceof DoubleResult)
			str.append(indend + ((DoubleResult) res).value + "\n");
		else if (res instanceof BooleanResult)
			str.append(indend + ((BooleanResult) res).value + "\n");
		else if (res instanceof DateResult)
			str.append(indend + ((DateResult) res).format() + "\n");
		else if (res instanceof StructResult) {
			SingleResult[] resarr = ((StructResult)res).fieldsToArray();
			
			str.append(indend + "struct {\n");
			
			for (int i = 0; i < resarr.length; i++)
				str.append(print(resarr[i], " " + indend));

			str.append(indend + "}\n");
		}
		else if (res instanceof BinderResult) {
			BinderResult bres = (BinderResult) res;

			String resprnt;
			
			if (outOfLinePrinting(bres)) {
//				indend += "";
				resprnt = "\n" + print(bres.value, " " + indend);

				str.append(indend + bres.getName() + "(" + resprnt + indend + ")\n");
			}
			else {
				resprnt = print(bres.value, "");

				if (resprnt.split("\n").length < 2)
					resprnt = removeNewLines(resprnt);
			
				str.append(indend + bres.getName() + "(" + resprnt + ")\n");
			}
		}
		else if (res instanceof ReferenceResult)
			str.append(indend + "&" + ((ReferenceResult) res).value.toString() + "\n");
		else if (res instanceof RemoteReferenceResult)
		{
			RemoteReferenceResult remote = (RemoteReferenceResult)res;
			str.append(indend + "&" + remote.id + "@" + remote.host + "/" + remote.schema + "\n");
		}
		else
			assert false;

		return str.toString();
	}

	private boolean outOfLinePrinting(BinderResult bres) {
		boolean outofline = false;
		
		outofline = bres.value instanceof StructResult || bres.value instanceof BagResult;

		if (bres.value instanceof BinderResult) {
			BinderResult bres2 = (BinderResult) bres.value;

			return outOfLinePrinting(bres2);
		}

		return outofline;
	}

	private final String removeLastEol(String str) {
		int idx = str.lastIndexOf('\n');
		
		if (idx < 0)
			return str;
		
		return str.substring(0, idx);	
	}
	
	private final String removeNewLines(String str) {
		return str.replaceAll("\n", "");
	}
	
	/**
	 * Sets the output encoding.
	 * 
	 * @param outputEncoding encoding name
	 */
	public void setOutputEncoding(String outputEncoding)
	{
		this.outputEncoding = outputEncoding;
	}
}
