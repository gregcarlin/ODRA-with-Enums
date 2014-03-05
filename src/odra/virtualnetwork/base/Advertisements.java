package odra.virtualnetwork.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.activation.MimeType;

import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredTextDocument;
import net.jxta.document.XMLElement;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.ModuleClassAdvertisement;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.ModuleSpecAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

import org.apache.log4j.Logger;

/** This class is resposible creation and managinf of Advertisements
 */
public class Advertisements {
	static Logger log = Logger.getLogger(Advertisements.class);
	
	public static String MANAGMENTMODULE_ADV = "mgmtmodule.adv";
	public static String MANAGMENTSERVICE_ADV = "mgmtservice.adv";
	public static String MANAGMENTSOCKET_ADV = "mgmtsocket.adv";
	
	public static String DATABASEMODULE_ADV = "databasemodule.adv";
	public static String DATABASESERVICE_ADV = "databaseservice.adv";
	public static String DATABASESOCKET_ADV = "databasesocket.adv";
	
	public static String MULTICASTMODULE_ADV = "multicastmodule.adv";
	public static String MULTICASTSERVICE_ADV = "multicastservice.adv";
	public static String MULTICASTSOCKET_ADV = "multicastsocket.adv";
	
	public static String REPOSITORYGROUP_ADV = "repogroup.adv";

	private URI baseConfDir = null;
	/**
	 * @param baseConfDir Base platform configuration dir
	 */
	public Advertisements(URI baseConfDir){
		this.baseConfDir = baseConfDir;
	}
	
	private Map<String,Advertisement> advertisements = new HashMap<String,Advertisement>();
	
	public Advertisement getAdvertisement(String adv_name){
		return advertisements.get(adv_name);
	}
	
	/** Creates adverisements for Mangement Service for specified Peer Group
	 * @param PeerGroup
	 */
	public void initializeManagementAdverisements(PeerGroup pg){
		ModuleClassAdvertisement moduleAdv = null;
		ModuleSpecAdvertisement specAdv = null;
		PipeAdvertisement pipeAdv = null;
		
		File f;
		Advertisement adv;
		
		//we don't check if we already have them
		//probably we want to regenerate them
		
		//try to load them from the file - recovering previous confs / fastes tests
		
		//module advertisement
		f = new File(baseConfDir.getRawPath(), MANAGMENTMODULE_ADV);
		adv = getAdvertisementFromDisk(f);
		if (adv != null)
				moduleAdv = (ModuleClassAdvertisement) adv;
		else{
			log.info("Making advertisement " + MANAGMENTMODULE_ADV);
			moduleAdv = (ModuleClassAdvertisement)
           		AdvertisementFactory.newAdvertisement(
           				ModuleClassAdvertisement.getAdvertisementType());
			moduleAdv.setName("JXTAMOD:ODRAMANAGEMENTMODULE");
			moduleAdv.setDescription("ODRA distributed database control/connection module");
			moduleAdv.setModuleClassID(IDFactory.newModuleClassID());			
		}
		
		//service
		f = new File(baseConfDir.getRawPath(), MANAGMENTSERVICE_ADV);
		adv = getAdvertisementFromDisk(f);
		if (adv != null)
				specAdv =(ModuleSpecAdvertisement) adv;
		else {
			log.info("Making advertisement " + MANAGMENTSERVICE_ADV);
			specAdv =(ModuleSpecAdvertisement)
				AdvertisementFactory.newAdvertisement(
						ModuleSpecAdvertisement.getAdvertisementType());

			specAdv.setName("JXTASPEC:ODRAMANAGEMENTSERVICE");
			specAdv.setCreator(pg.getPeerName());
			specAdv.setVersion("Version 0.1");
			specAdv.setModuleSpecID(
				IDFactory.newModuleSpecID(
						moduleAdv.getModuleClassID()));	
		}
		
		//socket (pipe)
		f = new File(baseConfDir.getRawPath(), MANAGMENTSOCKET_ADV);
		adv = getAdvertisementFromDisk(f);
		if (adv!=null)
			pipeAdv =(PipeAdvertisement) adv;
		else {
			log.info("Making advertisement " + MANAGMENTSOCKET_ADV);
			pipeAdv = (PipeAdvertisement) 
           		AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
			pipeAdv.setPipeID(
					IDFactory.newPipeID(pg.getPeerGroupID()));
			specAdv.setPipeAdvertisement(pipeAdv);
			pipeAdv.setDescription("0.1");
			pipeAdv.setType("JxtaUnicast");
		}
			
		//put them to the map
		advertisements.put(MANAGMENTMODULE_ADV, moduleAdv);
		advertisements.put(MANAGMENTSERVICE_ADV, specAdv);
		advertisements.put(MANAGMENTSOCKET_ADV, pipeAdv);
	}
	
	/** Creates adverisements of Database Service for specified Peer Group
	 * @param PeerGroup
	 */
	public void initializeDatabaseAdverisements(PeerGroup pg){
		ModuleClassAdvertisement moduleAdv = null;
		ModuleSpecAdvertisement specAdv = null;
		PipeAdvertisement pipeAdv = null;
		
		File f;
		Advertisement adv;
		
		//we don't check if we already have them
		//probably we want to regenerate them
		
		//try to load them from the file - revovering previous confs / fastes tests
		
		//module advertisement
		f = new File(baseConfDir.getRawPath(), DATABASEMODULE_ADV);
		adv = getAdvertisementFromDisk(f);
		if (adv != null)
				moduleAdv = (ModuleClassAdvertisement) adv;
		else{
			log.info("Making advertisement " + DATABASEMODULE_ADV);
			moduleAdv = (ModuleClassAdvertisement)
           		AdvertisementFactory.newAdvertisement(
           				ModuleClassAdvertisement.getAdvertisementType());
			moduleAdv.setName("JXTAMOD:"+pg.getPeerName()+":DATABASEMODULE");
			moduleAdv.setDescription("gsfdhsfh");
			moduleAdv.setModuleClassID(IDFactory.newModuleClassID());			
		}
		
		//service
		f = new File(baseConfDir.getRawPath(), DATABASESERVICE_ADV);
		adv = getAdvertisementFromDisk(f);
		if (adv != null)
				specAdv =(ModuleSpecAdvertisement) adv;
		else {
			log.info("Making advertisement " + DATABASESERVICE_ADV);
			specAdv =(ModuleSpecAdvertisement)
				AdvertisementFactory.newAdvertisement(
						ModuleSpecAdvertisement.getAdvertisementType());

			specAdv.setName("JXTASPEC:"+pg.getPeerName()+":DATABASESERVICE");
			specAdv.setVersion("Version 0.1");
			specAdv.setModuleSpecID(
				IDFactory.newModuleSpecID(
						moduleAdv.getModuleClassID()));	
		}
		
		//socket (pipe)
		f = new File(baseConfDir.getRawPath(), DATABASESOCKET_ADV);
		adv = getAdvertisementFromDisk(f);
		if (adv!=null)
			pipeAdv =(PipeAdvertisement) adv;
		else {
			log.info("Making advertisement " + DATABASESOCKET_ADV);
			pipeAdv = (PipeAdvertisement) 
           		AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
			pipeAdv.setPipeID(
					IDFactory.newPipeID(pg.getPeerGroupID()));
			specAdv.setPipeAdvertisement(pipeAdv);
			pipeAdv.setDescription("0.1");
			pipeAdv.setType(PipeService.UnicastType);
		}
			
		//put them to the map
		advertisements.put(DATABASEMODULE_ADV, moduleAdv);
		advertisements.put(DATABASESERVICE_ADV, specAdv);
		advertisements.put(DATABASESOCKET_ADV, pipeAdv);
	}
	
	
	/** Creates adverisements of Multicast Service for specified Peer Group
	 * @param PeerGroup
	 */
	public void initializeMulticastAdvertisements(PeerGroup pg){
		ModuleClassAdvertisement moduleAdv = null;
		ModuleSpecAdvertisement specAdv = null;
		PipeAdvertisement pipeAdv = null;
		
		File f;
		Advertisement adv;
		
		//we don't check if we already have them
		//probably we want to regenerate them
		
		//try to load them from the file - revovering previous confs / fastes tests
		
		//module advertisement
		f = new File(baseConfDir.getRawPath(), MULTICASTMODULE_ADV);
		adv = getAdvertisementFromDisk(f);
		if (adv != null)
				moduleAdv = (ModuleClassAdvertisement) adv;
		else{
			log.info("Making advertisement " + MULTICASTMODULE_ADV);
			moduleAdv = (ModuleClassAdvertisement)
           		AdvertisementFactory.newAdvertisement(
           				ModuleClassAdvertisement.getAdvertisementType());
			moduleAdv.setName("JXTAMOD:"+pg.getPeerName()+":MULTICASTMODULE");
			moduleAdv.setDescription("Multicast Module for Odra distributed client");
			moduleAdv.setModuleClassID(IDFactory.newModuleClassID());			
		}
		
		//service
		f = new File(baseConfDir.getRawPath(), MULTICASTSERVICE_ADV);
		adv = getAdvertisementFromDisk(f);
		if (adv != null)
				specAdv =(ModuleSpecAdvertisement) adv;
		else {
			log.info("Making advertisement " + MULTICASTSERVICE_ADV);
			specAdv =(ModuleSpecAdvertisement)
				AdvertisementFactory.newAdvertisement(
						ModuleSpecAdvertisement.getAdvertisementType());

			specAdv.setName("JXTASPEC:"+pg.getPeerName()+":MULTICASTSERVICE");
			specAdv.setVersion("Version 0.1");
			specAdv.setModuleSpecID(
				IDFactory.newModuleSpecID(
						moduleAdv.getModuleClassID()));	
		}
		
		//socket (pipe)
		f = new File(baseConfDir.getRawPath(), MULTICASTSOCKET_ADV);
		adv = getAdvertisementFromDisk(f);
		if (adv!=null)
			pipeAdv =(PipeAdvertisement) adv;
		else {
			log.info("Making advertisement " + MULTICASTSOCKET_ADV);
			pipeAdv = (PipeAdvertisement) 
           		AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
			//set pipeID recived from cmu
			pipeAdv.setPipeID(IDFactory.newPipeID(pg.getPeerGroupID()));
			specAdv.setPipeAdvertisement(pipeAdv);
			pipeAdv.setDescription("0.1");
			pipeAdv.setType(PipeService.PropagateType);
		}
			
		//put them to the map
		advertisements.put(MULTICASTMODULE_ADV, moduleAdv);
		advertisements.put(MULTICASTSERVICE_ADV, specAdv);
		advertisements.put(MULTICASTSOCKET_ADV, pipeAdv);		
	}
	
	public PeerGroupAdvertisement initializePeerGroupAdvertisement(String repoName, PeerGroup parentGroup){
		File f;
		PeerGroupAdvertisement databaseGroupAdv = null;
		Advertisement adv = null;
		
		f = new File(baseConfDir.getRawPath(), REPOSITORYGROUP_ADV);
		adv = getAdvertisementFromDisk(f);
		if (adv != null)
			databaseGroupAdv = (PeerGroupAdvertisement) adv;
		else{
			log.info("Making group advertisement");
        	ModuleImplAdvertisement implAdv = null;
			try {
				implAdv = parentGroup.getAllPurposePeerGroupImplAdvertisement();
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			databaseGroupAdv = (PeerGroupAdvertisement) 
				AdvertisementFactory.newAdvertisement(PeerGroupAdvertisement.getAdvertisementType());
			databaseGroupAdv.setModuleSpecID(implAdv.getModuleSpecID());
			databaseGroupAdv.setPeerGroupID(IDFactory.newPeerGroupID());
			databaseGroupAdv.setName(repoName);
			//databaseGroupAdv.putServiceParam(parentGroup.getRendezVousService().getInterface().getImplAdvertisement().getID(),(Element) parentGroup.getRendezVousService().getInterface().getImplAdvertisement().getDocument(MimeMediaType.TEXT_DEFAULTENCODING) );
			databaseGroupAdv.setDescription("Distributed database group (ODRA)");

			try {
				ModuleImplAdvertisement pimpl = parentGroup.getAllPurposePeerGroupImplAdvertisement();
				parentGroup.newGroup(databaseGroupAdv.getPeerGroupID(),
						pimpl,
						databaseGroupAdv.getName(),
						databaseGroupAdv.getDescription());
			} catch (PeerGroupException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		advertisements.put(REPOSITORYGROUP_ADV, databaseGroupAdv);
		return databaseGroupAdv;
	}

	private static Advertisement getAdvertisementFromDisk(File f){
		if (f.exists()){
			log.info("Reading advertisement from file "+ f.getName());
			FileInputStream is;
			try {
				is = new FileInputStream(f);
				StructuredDocument doc = StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8, is);
				is.close();
				return AdvertisementFactory.newAdvertisement((XMLElement)doc);				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}            
		}
		return null;
	}	
	
	public void saveAdvertisements(){
		Iterator keys = advertisements.keySet().iterator();
		while (keys.hasNext()){
			String key = (String) keys.next();
			saveAdvertisementToDisk(
					new File(baseConfDir.getRawPath(), key),
					(Advertisement) advertisements.get(key));
		}
	}
	
	private void saveAdvertisementToDisk(File f, Advertisement adv){
		log.debug("Saving advetisement to disk: "+ f.getName());
        StructuredTextDocument doc = (StructuredTextDocument)
    		adv.getDocument(MimeMediaType.XMLUTF8);
		try {
			FileWriter fr = new FileWriter(f);
			doc.sendToWriter(fr);
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}	

	public static String debugAdv(Advertisement adv){
		StringWriter sw = new StringWriter();
		try {
			((StructuredTextDocument)adv.getDocument(new MimeMediaType("text/plain"))).sendToWriter(sw);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sw.toString();
	}
}
