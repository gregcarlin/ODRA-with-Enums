package odra.virtualnetwork.facade;

public interface ICMUHandler {
		
	//public void processMessage(int opcode);
	
	public void peerJoined(String peerName);
	public void peerContributedSchema(String peerName, String schema, String userName);
	public void removeContributedPeer(String peerName, String schema, String userName);
	
	public void peerChangedMetabase(String peerName, String schema, String userName);
	
	public void peerDisconected(String peerName);
}

