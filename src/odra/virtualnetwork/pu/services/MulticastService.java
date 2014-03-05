package odra.virtualnetwork.pu.services;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Collection;
import java.util.HashMap;

import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaMulticastSocket;
import net.jxta.socket.JxtaServerSocket;
import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.links.LinkManager;
import odra.db.objects.data.DBLink;
import odra.db.objects.data.DBModule;
import odra.exceptions.rd.RDException;
import odra.network.transport.DBReply;
import odra.sbql.ast.ASTNode;
import odra.sbql.builder.BuilderUtils;
import odra.sbql.builder.LinkerException;
import odra.sbql.builder.ModuleConstructor;
import odra.sbql.builder.ModuleOrganizer;
import odra.sbql.results.runtime.StringResult;
import odra.system.Names;
import odra.system.config.ConfigServer;
import odra.virtualnetwork.base.Advertisements;

import org.apache.log4j.Logger;

public class MulticastService extends Thread {
	
	static Logger log = Logger.getLogger(MulticastService.class);
	
	private PeerGroup peerGroup = null;
	private PipeAdvertisement pipeAdv = null;

	public MulticastService(PeerGroup peerGroup, Advertisements advManager, PipeID mcast_id){
		this.setName("srvc-mc");
		
		this.peerGroup = peerGroup;
		
		advManager.initializeMulticastAdvertisements(peerGroup);
		
		advManager.saveAdvertisements();
		pipeAdv = (PipeAdvertisement) advManager.getAdvertisement(Advertisements.MULTICASTSOCKET_ADV);
		pipeAdv.setPipeID(mcast_id);
		
		try {
			peerGroup.getDiscoveryService().publish(pipeAdv);
			peerGroup.getDiscoveryService().publish(advManager.getAdvertisement(Advertisements.MULTICASTSERVICE_ADV));
			peerGroup.getDiscoveryService().remotePublish(pipeAdv);
			peerGroup.getDiscoveryService().remotePublish(advManager.getAdvertisement(Advertisements.MULTICASTSERVICE_ADV));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public JxtaMulticastSocket mulSocket = null;
	public void run(){
		log.info("Multicast Server Service starting");
		
		try {
			System.out.println(pipeAdv.toString());
			mulSocket = new JxtaMulticastSocket(peerGroup,pipeAdv);
			mulSocket.setSoTimeout(0);
		
			while (true){
				log.debug("Creating datagram and waiting for filling it");
				byte [] buffer = new byte[16384]; 
				DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
				mulSocket.receive(packet);				
				serv(packet.getData());
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private HashMap<String, DBLink> linkmap = new HashMap<String, DBLink>();
	public void serv(byte[] buff){
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buff));

			byte [] linkNameArr = null;
			byte [] peerNameArr = null;
			byte [] schemaArr = null;
			byte [] userNameArr = null;
			byte [] integrModuleNameArr = null;
			byte [] viewNameArr = null;
			byte [] viewDefArr = null;

			String linkName = null;
			String peerName = null;
			String schema = null;
			String userName = null;
			String integrModuleName = null;
			String viewDefName = null;
			String viewDefString = null;
			
			DBModule mod = null;
			ModuleConstructor modConstructor = null;
			OID oid = null;
			ModuleOrganizer modOrganizer = null;
			DBLink link = null;


			try {
				switch (dis.readInt()){
				case MulticastMessage.MC_PEERCONTRIBUTED:
					log.info("request for peer contribution event");
					linkNameArr = new byte[dis.readInt()];
					dis.read(linkNameArr);
					peerNameArr = new byte[dis.readInt()];
					dis.read(peerNameArr);
					schemaArr = new byte[dis.readInt()];
					dis.read(schemaArr);
					userNameArr = new byte[dis.readInt()];
					dis.read(userNameArr);
					integrModuleNameArr = new byte[dis.readInt()];
					dis.read(integrModuleNameArr);
					
					linkName = new String(linkNameArr);
					peerName = new String(peerNameArr);
					schema = new String(schemaArr);
					userName = new String(userNameArr);
					integrModuleName = new String(integrModuleNameArr);
					
					link = LinkManager.getInstance().createGridLink(
							linkName, Database.getModuleByName(integrModuleName), peerName, schema, "<hidden>");
					
					if (!linkmap.containsKey(linkName))linkmap.put(linkName, link);
					
					break;
					
				case MulticastMessage.MC_PEERREMOVED:
					log.info("request for peer removing event");
					linkNameArr = new byte[dis.readInt()];
					integrModuleNameArr = new byte[dis.readInt()];
					dis.read(integrModuleNameArr);
					
					linkName = new String(linkNameArr);
					integrModuleName = new String(integrModuleNameArr);
					
					LinkManager.getInstance().removeGridLink(linkName, integrModuleName);

					break;
					
				case MulticastMessage.MC_ADD_VIEW:
					log.info("request for add integration view");
					
					//turn off typechecker for a while due to transaction problem reasons 
					//ConfigServer.TYPECHECKING = false;
					
					viewNameArr = new byte[dis.readInt()];
					dis.read(viewNameArr);
					viewDefArr = new byte[dis.readInt()];
					dis.read(viewDefArr);
					integrModuleNameArr = new byte[dis.readInt()];
					dis.read(integrModuleNameArr);
					
					viewDefName = new String(viewNameArr);
					viewDefString = new String(viewDefArr);
					integrModuleName = new String(integrModuleNameArr);
					
					mod = Database.getModuleByName(integrModuleName);
					
					modConstructor = new ModuleConstructor(null);
					modConstructor.setConstructedModule(mod);

					try {
						ASTNode node = BuilderUtils.parseSBQL( integrModuleName, viewDefString);
						node.accept(modConstructor, null);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				
					//mod.setModuleLinked(false);
					//mod.setModuleCompiled(false);
					
					//ConfigServer.TYPECHECKING = true;
					
					break;

				case MulticastMessage.MC_REMOVE_VIEW:
					log.info("request for remove integration view");
					
					//turn off typechecker for a while due to transaction problem reasons 
					//ConfigServer.TYPECHECKING = false;
					
					viewNameArr = new byte[dis.readInt()];
					dis.read(viewNameArr);
					integrModuleNameArr = new byte[dis.readInt()];
					dis.read(integrModuleNameArr);
					
					viewDefName = new String(viewNameArr);
					integrModuleName = new String(integrModuleNameArr);
					
					mod = Database.getModuleByName(integrModuleName);
					
					//delete a view only if it exist in called module
					if (mod.findFirstByName(viewDefName, mod.getDatabaseEntry()) != null){					
						ModuleOrganizer org = new ModuleOrganizer(mod, false);
						org.deleteView(viewDefName);
					}
					
					modConstructor = new ModuleConstructor(null);
					modConstructor.setConstructedModule(mod);

					//mod.setModuleLinked(false);
					//mod.setModuleCompiled(false);
					
					//ConfigServer.TYPECHECKING = true;
					
					break;

				case MulticastMessage.MC_REFRESHLINK:
					linkNameArr = new byte[dis.readInt()];
					dis.read(linkNameArr);
					integrModuleNameArr = new byte[dis.readInt()];
					dis.read(integrModuleNameArr);
					
					mod = Database.getModuleByName(new String(integrModuleNameArr));
					oid  = mod.findFirstByName(new String(linkNameArr), mod.getDatabaseEntry());
					link = new DBLink(oid);

					LinkManager.getInstance().refreshLinkMetadata(link);
					
					break;
					
				case MulticastMessage.MC_COMPILEMODULE:
					log.info("request for compile module(s)");
					try {
						while (true){
							byte [] moduleNameArr = new byte[dis.readInt()];
							dis.read(moduleNameArr);
							mod  = Database.getModuleByName(new String(moduleNameArr));
							BuilderUtils.getModuleLinker().linkModule(mod);
							BuilderUtils.getModuleCompiler().compileModule(mod);
						}
					} catch(EOFException e){/* no more modules to compile */
					} catch(IOException e){ 
						e.printStackTrace();
					} catch(DatabaseException e){
						e.printStackTrace();
					} catch(LinkerException e){
						log.error(e);
						log.debug("trying to recover from LinkerException");						
						Collection<DBLink> coll = linkmap.values(); 
						for (DBLink l : coll){
							log.debug("refreshing metabase for link :" + l.getHost());
							LinkManager.getInstance().refreshLinkMetadata(l);
						}
							
						BuilderUtils.getModuleLinker().linkModule(mod);
						BuilderUtils.getModuleCompiler().compileModule(mod);
					}
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (DatabaseException e) {
				e.printStackTrace();
			} catch (RDException e) {
				e.printStackTrace();
			}
			
	}
}
