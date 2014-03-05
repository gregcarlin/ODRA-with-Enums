package odra.network.encoders.signatures;

import java.util.ArrayList;

import odra.network.transport.AutoextendableBuffer;
import odra.sbql.results.compiletime.util.SignatureKind;
import odra.sbql.results.compiletime.util.ValueSignatureInfo;

/**
 * SBQLSignatureEncoder encodes the signatures for dependent parameters.
 * 
 * Only Signatures of Value Types are being encoded.
 * 
 * 	The format of message is as follows:
 * 		- number of signatures entries 	( 4 bytes )
 * 
 * 		-signature n kind				( 4 bytes ) reserved only for further kinds						
 * 		
 * 		-signature n length				( 4 bytes )
 * 		-signature n name				( n bytes )
 * 		-signature n Value Type as int	( 4 bytes )
 * 		-signature n BindingInfo		( 4 bytes )
 *  	
 * 
 * @author janek
 * 
 */
public class SBQLPrimitiveSignatureEncoder
{
	private AutoextendableBuffer buf = new AutoextendableBuffer();

	public byte[] encode(ArrayList<ValueSignatureInfo> parmSignatures)
	{
		
		buf.putInt(parmSignatures.size());

		// encode Signatures
		for (ValueSignatureInfo sinfo : parmSignatures)
		{
			buf.putInt(SignatureKind.ValueSignature.kindAsInt());
			
			buf.putInt(sinfo.getName().getBytes().length);
			buf.put(sinfo.getName().getBytes());
			buf.putInt(sinfo.getSigType().kindAsInt());
			buf.putInt(sinfo.getBindingInfo().boundat);
		}

		return buf.getBytes();
	}
}
