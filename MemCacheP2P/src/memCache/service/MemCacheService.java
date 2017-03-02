package memCache.service;

import common.Hash;
import common.Key;
import memCache.config.Settings;
import memCache.ring.Node;
import memCache.ring.Reachable;


import java.io.File;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Main class
 */
public class MemCacheService {


	public MemCacheService(){}


	public static void main(String[] args){
		MemCacheService memCacheService = new MemCacheService();
		memCacheService.start();
	}

	/**
	 * Initializes the node which creates a ring or enters an existing one
	 */
	public void start(){
		try {
			//-----Uncomment the 2 lines below if Node_Stub.class not present in memCache.ring----
			//String classpath = Node.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			//Runtime.getRuntime().exec("rmic memCache.ring.Node",null,new File(classpath));
			Node self = new Node(InetAddress.getLocalHost(), Settings.LISTENING_PORT);
			if (self.getIp().equals(Settings.GATE.getFirst())){
				self.join(self);
			}
			else {
				String gateInfo = Settings.GATE.getFirst().getHostAddress()+":"+Settings.GATE.getSecond();
				Hash.hash(gateInfo);
				Key gateKey = new Key(Hash.getHashedValue());
				System.out.println("Seeking remote registry at"+Settings.GATE.getFirst().getHostAddress()+":"+Registry.REGISTRY_PORT+" ...");
				Registry remoteRegistry = LocateRegistry.getRegistry(Settings.GATE.getFirst().getHostAddress(),Registry.REGISTRY_PORT);
				System.out.println("Registry found, locating remote object...");
				Reachable gate = (Reachable) remoteRegistry.lookup(gateKey.toHex());
				System.out.println("Remote Node: "+gate);
				self.join(gate);
				gate.notifyUpdates();
			}
			System.out.println(self);
			Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			registry.bind(self.getNodeKey().toHex(),self);
			System.setProperty("java.rmi.hostname",self.getIp().getHostAddress());
			self.getServer().start();
		}catch (Exception e){ e.getMessage();}
	}


}
