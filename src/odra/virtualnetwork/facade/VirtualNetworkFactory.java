package odra.virtualnetwork.facade;

import odra.virtualnetwork.api.IRequestHandler;

public class VirtualNetworkFactory {
	
	private static IPeerUnit peerUnit = null;
	private static ICMUUnit cmuUnit = null;
	private static IRequestHandler requestHandler = null;
	private static ICMUHandler cmuHandler = null;
	
	public static IPeerUnit createPeerUnit(){
		if (peerUnit != null) return peerUnit;
		
		try {
			peerUnit = (IPeerUnit) Class.forName("odra.virtualnetwork.pu.ClientUnit").newInstance();
		} catch (Exception e) {	}
		
		return peerUnit;
	}
	
	public static ICMUUnit createCMUUnit(){
		if (cmuUnit != null) return cmuUnit;
		
		try {
			cmuUnit  = (ICMUUnit) Class.forName("odra.virtualnetwork.cmu.CMUnit").newInstance();
		} catch (Exception e) { } 
		return cmuUnit;
	}
	
	public static IRequestHandler createRequestHandler(){
		if (requestHandler != null) return requestHandler;
		
		try {
			requestHandler = (IRequestHandler) Class.forName("odra.virtualnetowrk.RequestHandlerImpl").newInstance();
		} catch (Exception e) { }
 		
		return requestHandler;
	}
	
	public static ICMUHandler createCMUHandler(){
		if (cmuHandler != null) return cmuHandler;
		
		try {
			cmuHandler = (ICMUHandler) Class.forName("odra.virtualnetwork.CMUHandlerImpl").newInstance();
		} catch (Exception e) { }
		
		return cmuHandler;
	}
}
