package odra.virtualnetwork.facade;

import java.net.URI;

public class Config {
	
	//default without jxta layer
	public static PEER_TYPE peerType = PEER_TYPE.PEER_NOTHING;
	public enum PEER_TYPE {
		PEER_NOTHING,
		PEER_CMU,
		PEER_ENDPOINT
	}
	
	//point to rendezvous peer, usually cmu (uri style)	
	public static String ipRandezVous = "tcp://10.10.249.245:9701";

	//authorization
	public static String repoGroup = "Odra";
	public static String repoIdentity = "client";
	public static String repoPasswd = "Odra";

	//jxta multicast ports range
	public static int jxtaTransportPortStart = 9701;
	public static int jxtaTransportPortEnd = 9799;

	//port on which transport application listen for database connection 
	public static int peerPort = 9552;
	
	//peer monitor 
	public static boolean peerMonitor = false;
	public static long socketKeepAliveTimeout = 50000;
	public static long peerMonitorSleepTime = 2000;
	
	//How often check new advs 
	public static long searchLoopTimeout = 1000;
	//search remote advs timeout  (searchAdvsTimeout / searchLoopTimeout = loops)
	public static long searchAdvsTimeout  = 10000;

	//platform configuration uri
	public static URI platformHome = URI.create("file:///tmp/odragrid"); 

}
