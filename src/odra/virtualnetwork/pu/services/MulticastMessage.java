package odra.virtualnetwork.pu.services;

import odra.network.transport.AutoextendableBuffer;
													//  |
public class MulticastMessage {						// VVV  sending list (elements in that order) 
	public static final int MC_PEERJOINED = 1;		//
	public static final int MC_PEERREMOVED = 2; 	//
	public static final int MC_PEERCONTRIBUTED = 3; // {gridlinkName, peerName, schema, userName, gridModuleName}  notifies about peer incomings
	public static final int MC_ADD_VIEW = 4;	// {viewName, viewDefString, gridModuleName}
	public static final int MC_REMOVE_VIEW = 5;	// {viewName, gridModuleName)
	public static final int MC_COMPILEMODULE = 6;	// {module1, module2,...} compile all of grid modules on connected peers
	public static final int MC_REFRESHLINK = 7;     // {gridlinkName, gridModuleName}
	AutoextendableBuffer buffer = new AutoextendableBuffer();
	public byte [] encodeMessage(int type, String [] args){
		buffer.flip();
		buffer.putInt(type);
		for (String arg : args){
			buffer.putInt(arg.getBytes().length);
			buffer.put(arg.getBytes());
		}		
		
		return buffer.getBytes();
	}
	
	
	
}
