/**
 * 
 */
package odra.sbql.ast.serializer;

import java.nio.ByteBuffer;

import odra.network.transport.AutoextendableBuffer;
import odra.sbql.ast.ASTNode;

/**
 * Util
 * 
 * @author Radek Adamus
 * @since 2008-04-25 last modified: 2008-04-25
 * @version 1.0
 */
public class SerializationUtil {

    public static void serializeString(AutoextendableBuffer buffer, String val) {
	byte[] bval = val.getBytes();
	buffer.putInt(bval.length);
	buffer.put(bval);
    }

    public static String deserializeString(ByteBuffer buffer) {
	int len = buffer.getInt();
	byte[] strarr = new byte[len];
	buffer.get(strarr);
	return new String(strarr);
    }

    public static void serializePosition(AutoextendableBuffer buffer, ASTNode node) {

	buffer.putInt(node.line);
	buffer.putInt(node.column);

    }

    public static void deserializePositionInfo(ASTNode node, ByteBuffer serast) {
	node.line = serast.getInt();
	node.column = serast.getInt();
    }
}
