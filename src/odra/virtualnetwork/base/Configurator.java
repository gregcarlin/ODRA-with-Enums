package odra.virtualnetwork.base;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import odra.virtualnetwork.facade.Config;

import org.apache.log4j.Logger;

import net.jxta.ext.config.Profile;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkConfigurator;

public class Configurator {
	
	public static Logger log = Logger.getLogger(Configurator.class);
	
    public static NetworkConfigurator createConfigClient(String peerName, URI dirHome) {
    	File instanceHome = new File(dirHome);
        NetworkConfigurator config = new NetworkConfigurator();
        config.setHome(instanceHome);
        if (!config.exists()) {
            config.setPeerID(IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID));
            config.setName(peerName);
            //config.setMode(NetworkConfigurator.EDGE_NODE);
            try {            
            	config.addSeedRelay(URI.create(Config.ipRandezVous));
                config.addSeedRendezvous(URI.create(Config.ipRandezVous));
                config.setTcpEnabled(true);
                config.setTcpIncoming(true);
                config.setTcpOutgoing(true);
                config.setTcpPort(Config.jxtaTransportPortStart);
                config.setTcpStartPort(Config.jxtaTransportPortStart);
                config.setTcpEndPort(Config.jxtaTransportPortEnd);
            //    config.setUseMulticast(false);
                config.save();
            } catch (IOException io) {
                io.printStackTrace();
            }
        } else {
            try {
                File pc = new File(config.getHome(), "PlatformConfig");
                config.load(pc.toURI());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return config;
    }	
    
	public static NetworkConfigurator createConfigServerRandezVous(String dirHome, String name){
		log.info("CMU acts as a randezvous node");
    	File instanceHome = new File(dirHome);
        NetworkConfigurator config = new NetworkConfigurator();
        config.setHome(instanceHome);

        if (!config.exists()) {
            config.setPeerID(IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID));
            config.setName(name);
            config.setDescription("ODRA CMU");
            
            config.setMode(NetworkConfigurator.RDV_SERVER + NetworkConfigurator.RELAY_SERVER)  ;
            
            config.setUseOnlyRelaySeeds(true);
            config.setUseOnlyRelaySeeds(true);
            config.addSeedRelay(URI.create(Config.ipRandezVous));
            config.addSeedRendezvous(URI.create(Config.ipRandezVous));
            
            config.setTcpEnabled(true);
            config.setTcpIncoming(true);
            config.setTcpOutgoing(true);
            config.setUseMulticast(false);
            config.setTcpPort(9701);

            try {
                config.save();
            } catch (IOException io) {
                io.printStackTrace();
            }
        }else {
            try {
                File pc = new File(config.getHome(), "PlatformConfig");
                config.load(pc.toURI());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
       
        return config;		
	}
}
