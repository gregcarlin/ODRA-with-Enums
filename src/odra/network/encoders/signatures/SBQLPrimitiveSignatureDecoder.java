package odra.network.encoders.signatures;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import odra.exceptions.rd.RDNetworkException;
import odra.sbql.results.compiletime.util.SignatureInfo;
import odra.sbql.results.compiletime.util.SignatureKind;
import odra.sbql.results.compiletime.util.ValueSignatureInfo;
import odra.sbql.results.compiletime.util.ValueSignatureType;
import odra.sbql.stack.BindingInfo;

/**
 * Decodes the Signatures for dependent parameters
 * 
 * 
 * @author janek
 * 
 */
public class SBQLPrimitiveSignatureDecoder
{
	private ByteBuffer buf;

	public ArrayList<SignatureInfo> decode(byte[] data) throws RDNetworkException
	{
		ArrayList<SignatureInfo> parmSignatures = new ArrayList<SignatureInfo>();
		buf = ByteBuffer.wrap(data);

		int count = buf.getInt();

		for (int i = 0; i < count; i++)
		{
			switch (SignatureKind.getForInteger(buf.getInt()))
			{
				case ValueSignature:
					parmSignatures.add(decodeValueSignature());
					break;
				default:
					throw new RDNetworkException("unsupported signature kind");
			}

		}

		return parmSignatures;
	}

	private ValueSignatureInfo decodeValueSignature()
	{
		int nameLen = buf.getInt();
		byte[] bytea = new byte[nameLen];
		buf.get(bytea);
		String pName = new String(bytea);

		int pType = buf.getInt();

		ValueSignatureInfo vInfo = new ValueSignatureInfo();
		vInfo.setName(pName);

		vInfo.setSigType(ValueSignatureType.getForInteger(pType));

		vInfo.setBindingInfo(new BindingInfo(buf.getInt()));

		return vInfo;
	}
}
