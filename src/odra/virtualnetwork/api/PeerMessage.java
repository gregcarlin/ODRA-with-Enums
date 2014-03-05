package odra.virtualnetwork.api;

public class PeerMessage {
	
	//opcodes for peer app <--> LocalTransport communications
	public static final int REQUEST = 1;
	public static final int SETUP = 2;
	public static final int CMU_JOINPEER = 3;
}
