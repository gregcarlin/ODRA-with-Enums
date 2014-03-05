package odra.virtualnetwork;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.links.LinkManager;
import odra.db.objects.data.DBLink;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MetaBase;
import odra.exceptions.rd.RDException;
import odra.exceptions.rd.RDNetworkException;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.sbql.builder.BuilderUtils;
import odra.sessions.Session;
import odra.system.log.UniversalLogger;
import odra.virtualnetwork.api.IntegrViewGen;
import odra.virtualnetwork.api.IntegrViewGenConfig;
import odra.virtualnetwork.api.IntegrViewGenIO;
import odra.virtualnetwork.api.PeerMessage;
import odra.virtualnetwork.facade.ICMUHandler;
import odra.virtualnetwork.facade.Config;
import odra.virtualnetwork.cmu.CMUnit;
import odra.virtualnetwork.cmu.Network;
import odra.virtualnetwork.pu.Repository;
import odra.virtualnetwork.pu.RepositoryPeer;
import odra.virtualnetwork.pu.services.MulticastMessage;
import odra.virtualnetwork.pu.services.MulticastService;

public class CMUHandlerImpl implements ICMUHandler {
	static Logger log = Logger.getLogger(CMUHandlerImpl.class);
	
	private class Contrib{
		public String peerName;
		public String schema;
		public String userName;
		public String gridModule;
		
		public Contrib(String peerName, String schema, String userName, String gridModule){
			this.peerName = peerName;
			this.schema = schema;
			this.userName = userName;
			this.gridModule = gridModule;
		}
		
		public boolean equals(Contrib o){
			if ((peerName == o.peerName) && (schema == o.schema) && (userName==o.userName) && (gridModule == o.gridModule))
				return true;
			return false;
		}
		
		//<linkName, peerName, schema, userName>
		public String[] toStringList(){
			String [] list = {peerName + "_" + schema.replace('.', '_'), peerName, schema, userName, gridModule};
			return list ;
		}
	}
	
	private ArrayList<Contrib> contributions = new ArrayList<Contrib>();
	private DBModule parentmod;

	private Document xmlview = null;
	private Document currentView = null;
	private DBLink gridlink;
	private String gridlinkName = null;
	private String procObj = null;
	private String nameObj = null;
	private String viewDefString = null;
	private byte [] rawreq = null;
	DBRequest req = null;
	DBModule adminMod = null;
	DBModule theMod = null;
	private String integrModuleName = "admin.integr";
	private String globalModuleName = "admin.global";


	private IntegrViewGen viewGenerator = new IntegrViewGen();
	private IntegrViewGenConfig viewGeneratorConfig = new IntegrViewGenConfig();

	//parse common globalview
	private Document globalView = IntegrViewGenIO.parseFile(odra.virtualnetwork.api.IntegrViewGen.xmlGlobalViewSchemaFileName);
	private String globalViewString = IntegrViewGenIO.convertToString(globalView);

	//get viewgenerator config
	private List viewProperties = viewGeneratorConfig.getConfiguration();

	
	public CMUHandlerImpl() {
		try {
			parentmod =  Database.getModuleByName("admin");
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	
	}

	public void peerDisconected(String peerName){ 
		// TODO Auto-generated method stub
	}
	
	public void peerChangedMetabase(String peerName, String schema, String userName) {
		// TODO Ta funkcja wywolywana jest gdy peer zmieni metabaze!! (leniwe wywolanie)
	}

	public void peerContributedSchema(String peerName, String schema, String userName) {
		
		gridlinkName = peerName + "_" + schema.replace(".", "_");
		
		//add currently contributed peer to contributions' list
		Contrib contrib = new Contrib(peerName, schema, userName, integrModuleName);
		for(int co = 0; co < contributions.size() ; co++)
		{
			Contrib c = contributions.get(co);
			if(c.peerName.equals(contrib.peerName))
			{
				log.info("Can not contribute - peer " + peerName + " already exists in repository!");
					Thread th = Thread.currentThread();
					th.interrupt();
			}
		}
	
		if(contributions.size() >= 0) contributions.add(contrib);		


		//processing integr module add @cmu
		try {
			//check cmu if has he an admin.grid module...
			OID[] mods = Database.getModuleByName("admin").getSubmodules();
			boolean hasgridmod = false;
			for (int modsItem = 0; modsItem < mods.length; modsItem++)
			{
				if (mods[modsItem].getObjectName().equals("integr")) {
					hasgridmod = true;
					break;
				}
			}

			if (!hasgridmod) {
						//create locally grid cmu module with import from cmu local module
						adminMod = Database.getModuleByName("admin");
						theMod = new DBModule(adminMod.createSubmodule("integr", 0));
						theMod.addImport("admin.cmu");
				
						//compile grid module @cmu
						BuilderUtils.getModuleLinker().linkModule(theMod);
						BuilderUtils.getModuleCompiler().compileModule(theMod);
					}
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		//add currently contributed gridlink locally @cmu
		try {
		theMod = Database.getModuleByName(integrModuleName);
			gridlink = LinkManager.getInstance().createGridLink(gridlinkName, theMod, peerName, schema, "<hidden>");			
			LinkManager.getInstance().refreshLinkMetadata(gridlink);

		//compile grid module @cmu
		BuilderUtils.getModuleLinker().linkModule(theMod);
		BuilderUtils.getModuleCompiler().compileModule(theMod);

		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (RDException e) {
			e.printStackTrace();
		}

		
		//create at contributed peer an integr module
		req = new DBRequest(DBRequest.ADD_MODULE_RQST,
				new String[] { "module integr { import " + schema + "; }", "admin"} );
		try {
			rawreq = Repository.getInstance().encodeMessage(req);
			Repository.getInstance().getPeer(peerName).putRequest(rawreq, userName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//use broadcast for adding the new peer to existing contributors' peers including itself
		log.info("brodcasting MC_PEERCONTRIBUTED");
		try {
			for (Contrib contr : contributions){
				Network.getInstance().broadcast(new MulticastMessage().
						encodeMessage(MulticastMessage.MC_PEERCONTRIBUTED, contr.toStringList()));				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//View generator - adding or replacing a new peer into XML view
		//(integration), recurrent the process for each view from config
		//removes existing views form all peers 
		for (int v = 1; v <= viewGeneratorConfig.numberOfViews(viewProperties); v++)
		{
			Element curr = viewGeneratorConfig.getViewProperties(viewProperties, v);
			nameObj = curr.getAttributeValue("name");
			
			//remove current integration view through grid from all peers
			log.info("brodcasting MC_REMOVE_VIEW for view " + nameObj);
			try {
				Network.getInstance().broadcast(new MulticastMessage().
						encodeMessage(MulticastMessage.MC_REMOVE_VIEW, new String[] {nameObj, integrModuleName}));
	
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		//adding renewed views into all peers
		for (int v = 1; v <= viewGeneratorConfig.numberOfViews(viewProperties); v++)
		{
			Element curr = viewGeneratorConfig.getViewProperties(viewProperties, v);
			procObj = curr.getAttributeValue("proc");
			nameObj = curr.getAttributeValue("name");
			xmlview =  viewGenerator.addContributedSource(xmlview, procObj, gridlinkName, nameObj);
			
			currentView = viewGenerator.selectViewAsXMLDocument(xmlview, nameObj);
			viewDefString = IntegrViewGenIO.convertToString(currentView);
			
			//propagate current integration view through grid to all peers
			log.info("brodcasting MC_ADD_VIEW for view " + nameObj);
			try {
				Network.getInstance().broadcast(new MulticastMessage().
						encodeMessage(MulticastMessage.MC_ADD_VIEW, new String[] {nameObj, viewDefString, integrModuleName}));
	
			} catch (Exception e) {
				e.printStackTrace();
			}
		
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

/*
		//use broadcast to refresh link in all contributed peers
		try {
			log.info("brodcasting MC_REFRESHLINK");
			Network.getInstance().broadcast(new MulticastMessage().
					encodeMessage(MulticastMessage.MC_REFRESHLINK, new String[] {gridlinkName, integrModuleName}));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
*/		
		
		//use broadcast to compile an integr module
		try {
			log.info("brodcasting MC_COMPILEMODULE");
			Network.getInstance().broadcast(new MulticastMessage().
					encodeMessage(MulticastMessage.MC_COMPILEMODULE, new String[] {integrModuleName}));
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//create at contributed peer a global module
		req = new DBRequest(DBRequest.ADD_MODULE_RQST,
				new String[] { "module global { import admin.integr; }", "admin"} );
		try {
			rawreq = Repository.getInstance().encodeMessage(req);
			Repository.getInstance().getPeer(peerName).putRequest(rawreq, userName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//add a global view through grid into current peer
		try {
			rawreq = Repository.getInstance().encodeMessage(new DBRequest(
					DBRequest.EXECUTE_REMOTE_COMMAND_RQST, 
						new String[] {globalViewString}));
			
			Repository.getInstance().getPeer(peerName).putRequest(rawreq, userName);

		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//finally use broadcast to compile at current peer a global module
		try {
			log.info("brodcasting MC_COMPILEMODULE");
			Network.getInstance().broadcast(new MulticastMessage().
					encodeMessage(MulticastMessage.MC_COMPILEMODULE, new String[] {globalModuleName}));
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void removeContributedPeer(String peerName, String schema, String userName) {

		//remove currently requested peer from contributions' list
		Contrib contrib = new Contrib(peerName, schema, userName, integrModuleName );
		for(int co = 0; co < contributions.size() ; co++)
		{
			if(contributions.get(co).peerName.equals(contrib.peerName))
			{
				contributions.remove(co);
				break;
			}
		}
		
		String gridlinkName = peerName + "_" + schema.replace(".", "_");

		//View generator - removing or replacing a new peer into XML view
		//(integration), recurrent the process for each view from config
		//removes existing views form all peers 
		for (int v = 1; v <= viewGeneratorConfig.numberOfViews(viewProperties); v++)
		{
			Element curr = viewGeneratorConfig.getViewProperties(viewProperties, v);
			nameObj = curr.getAttributeValue("name");
			
			//if contributions > 0
			if (contributions.size() > 0)
			{
				//remove current integration view through grid from all peers
				log.info("brodcasting MC_REMOVE_VIEW for view " + nameObj);
				try {
					Network.getInstance().broadcast(new MulticastMessage().
							encodeMessage(MulticastMessage.MC_REMOVE_VIEW, new String[] {nameObj, integrModuleName}));
		
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		//View generator - removing requested peer from XML view
		//(integration), recurrent the process for each view from config
		for (int v = 1; v <= viewGeneratorConfig.numberOfViews(viewProperties); v++)
		{
			Element curr = viewGeneratorConfig.getViewProperties(viewProperties, v);
			procObj = curr.getAttributeValue("proc");
			nameObj = curr.getAttributeValue("name");
			xmlview =  viewGenerator.removeContributedSource(xmlview, procObj, gridlinkName, nameObj);
			
			currentView = viewGenerator.selectViewAsXMLDocument(xmlview, nameObj);
			viewDefString = IntegrViewGenIO.convertToString(currentView);

			//if contributions > 0
			if (contributions.size() > 0)
			{
				//propagate current integration view through grid to all peers
				log.info("brodcasting MC_ADD_VIEW for view " + nameObj);
				try {
					Network.getInstance().broadcast(new MulticastMessage().
							encodeMessage(MulticastMessage.MC_ADD_VIEW, new String[] {nameObj, viewDefString, integrModuleName}));
		
				} catch (Exception e) {
					e.printStackTrace();
				}
			
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		//if contributions > 0
		if (contributions.size() > 0)
		{
			//use broadcast to compile an integr module 
			try {
				log.info("brodcasting MC_COMPILEMODULE");
				Network.getInstance().broadcast(new MulticastMessage().
						encodeMessage(MulticastMessage.MC_COMPILEMODULE, new String[] {integrModuleName}));
				
			} catch (Exception e) {
				e.printStackTrace();
			}
	
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		//if contributions > 0
		if (contributions.size() > 0)
		{
			//use broadcast for removing requested gridlink from existing contributors'
			log.info("brodcasting MC_PEERREMOVED");
			try {
				for (Contrib contr : contributions){
					Network.getInstance().broadcast(new MulticastMessage().
							encodeMessage(MulticastMessage.MC_PEERREMOVED, contr.toStringList()));				
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		//remove an integr module on remote requested peer
		req = new DBRequest(DBRequest.REMOVE_MODULE_RQST, new String[] { integrModuleName } );
		try {
			rawreq = Repository.getInstance().encodeMessage(req);
			Repository.getInstance().getPeer(peerName).putRequest(rawreq, userName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//remove an global module on remote requested peer
		req = new DBRequest(DBRequest.REMOVE_MODULE_RQST, new String[] { globalModuleName } );
		try {
			rawreq = Repository.getInstance().encodeMessage(req);
			Repository.getInstance().getPeer(peerName).putRequest(rawreq, userName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//finally compile on remote requested peer an admin module
		req = new DBRequest(DBRequest.COMPILE_RQST, new String[] { "admin" } );
		try {
			rawreq = Repository.getInstance().encodeMessage(req);
			Repository.getInstance().getPeer(peerName).putRequest(rawreq, userName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//remove locally @cmu current peer's gridlink
		try {
			LinkManager.getInstance().removeGridLink(gridlinkName, integrModuleName);			

			//recompile integr module @cmu
			theMod = Database.getModuleByName(integrModuleName);
			BuilderUtils.getModuleLinker().linkModule(theMod);
			BuilderUtils.getModuleCompiler().compileModule(theMod);

		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (RDException e) {
			e.printStackTrace();
		}

		//remove locally @cmu integr module if contrib is empty
		if (contributions.size() == 0)
		{
			try {
				//create locally integr & global modules with imports from cmu local module
				DBModule adminMod = Database.getModuleByName("admin");
				new DBModule(adminMod.getSubmodule("integr")).deleteModule();
				new DBModule(adminMod.getSubmodule("global")).deleteModule();
				
				//compile grid module @cmu
				BuilderUtils.getModuleLinker().linkModule(adminMod);
				BuilderUtils.getModuleCompiler().compileModule(adminMod);
			}
			catch (DatabaseException e)
			{
				e.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void peerJoined(String peerName) {
		try {
			/*byte [] rawreq = Repository.getInstance().encodeMessage(new DBRequest(
					DBRequest.ADD_P2PLINK_RQST,					
					new String[] {"cmu_link", "admin.cmu", "<hidden>", Config.repoIdentity, "admin"}));
			
			//what user????
			Repository.getInstance().getPeer(peerName).putRequest(rawreq, "admin");*/
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
