package odra.sbql.results.compiletime.util;

import odra.db.StdEnvironment;
import odra.sbql.results.compiletime.BinderSignature;
import odra.sbql.results.compiletime.ValueSignature;
import odra.sbql.stack.BindingInfo;

/**
 * @author janek
 * 
 */
public class ValueSignatureInfo extends SignatureInfo
{
	private String name;
	private ValueSignatureType sigType;
	private BindingInfo bindingInfo;

	public String getName()
	{
		return name;
	}

	public void setName(String sigName)
	{
		this.name = sigName;
	}

	public ValueSignatureType getSigType()
	{
		return sigType;
	}

	public void setSigType(ValueSignatureType sigType)
	{
		this.sigType = sigType;
	}

	public BindingInfo getBindingInfo()
	{
		return bindingInfo;
	}

	public void setBindingInfo(BindingInfo bindingInfo)
	{
		this.bindingInfo = bindingInfo;
	}

	@Override
	public BinderSignature getBinderSignature()
	{
		ValueSignature vs = null;

		switch (getSigType())
		{
			case BOOLEAN_TYPE:
				vs = new ValueSignature(StdEnvironment.getStdEnvironment().booleanType);
				break;
			case DATE_TYPE:
				vs = new ValueSignature(StdEnvironment.getStdEnvironment().dateType);
				break;
			case INTEGER_TYPE:
				vs = new ValueSignature(StdEnvironment.getStdEnvironment().integerType);
				break;
			case REAL_TYPE:
				vs = new ValueSignature(StdEnvironment.getStdEnvironment().realType);
				break;
			case STRING_TYPE:
				vs = new ValueSignature(StdEnvironment.getStdEnvironment().stringType);
				break;
		}

		BinderSignature bs = new BinderSignature(getName(), vs);

		return bs;
	}

}
