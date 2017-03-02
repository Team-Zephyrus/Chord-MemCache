package memCache.ring;

import common.Hash;
import common.Key;
import memCache.config.Settings;
import memCache.fileUtils.Cache;
import memCache.fileUtils.FileManager;
import memCache.fileUtils.Record;
import memCache.network.FileReceiver;
import memCache.network.FileTransmitter;
import memCache.network.Server;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;


public class Node extends UnicastRemoteObject implements Reachable{

    /** The ip address of the logged node **/
    private InetAddress ip;

    /** The port that the logged node listens to for requests**/
    private int listeningPort;

    /** The port to receive the files for which is responsible **/
    private int fileReceivingPort;

    /** Server that handles the incoming requests **/
    private Server server;

    /** Cache system of the node **/
    private Cache<Key, Record<String>> cache;

    /** Stores/Retrieves files stored in the node's disk **/
    private FileManager fileManager;

    /** Status of the node **/
    private boolean isAlive;

    /** Distinct id of the node inside the ring **/
    private	Key nodeKey;

    /** Predecessor node according to the key values **/
    private Reachable predecessor;

    /** Successor node according to the key values **/
    private Reachable successor;

    /** Shortcut nodes inside the ring **/
    private FingerTable fingerTable;

    /** Stabilizes node refreshing it's information **/
    private Stabilizer stabilizer;

    /** The timer used to invoke stabilization **/
    private Timer stabilizeTimer;




    public Node(InetAddress ip,int listeningPort) throws RemoteException {
        this.ip = ip;
        this.listeningPort = listeningPort;
        this.fileReceivingPort = Settings.FILE_RECEIVING_PORT;
        this.nodeKey = new Key(ip.getHostAddress() + ":" + listeningPort);
        this.server = new Server(this);
        this.fingerTable = new FingerTable(Hash.getKeyLength());
        this.cache = new Cache<>(10000, 1); //TODO: configure options (cacheSize,purgeFreq in minutes)
        this.fileManager = new FileManager();
        this.stabilizer = new Stabilizer(this);
        this.stabilizeTimer = new Timer();
        this.isAlive = true;

    }


    /**
     * ------------Methods used to initialize the node or balance the ring------------
     */

    /**
     * Join the ring asking the node n for the appropriate neighbours
     * @param n
     */
    public void join(Reachable n) throws RemoteException{
        if (this.equals(n)){
        	this.predecessor = this;
        	this.successor = this;
        	for(int i=0; i<this.fingerTable.length(); i++){
        		this.fingerTable.put(this, i);
        	}
        }
        else{
        	this.initFingerTable(n);
			this.updateOthers();
            this.successor.transferFilesTo(this);
        }
        this.stabilizeTimer.scheduleAtFixedRate(this.stabilizer,10000,5000); //TODO: config stabilization start time & period
    }

    /**
     * Leave gracefully the ring restoring the balance
     */
    public void leave() throws RemoteException{
        this.server.stop();
        this.stabilizeTimer.cancel(); //TODO: update fingers of others
        this.successor.setPredecessor(this.predecessor);
        this.predecessor.setSuccessor(this.successor);
        this.transferFilesTo(this.successor);
    }

    /**
     * Initialize the fingers/shortcuts to other nodes using node n.
     * @param n
     */
    public void initFingerTable(Reachable n){
    	try {
            	this.successor = n.findSuccessor(this.getNodeKey());
		this.fingerTable.put(this.successor,0);
		this.predecessor = this.successor.getPredecessor();
		this.successor.setPredecessor(this);
            	this.predecessor.setSuccessor(this);
		for(int i=1; i<this.fingerTable.length(); i++){
                Key nextFingerKey = this.successor(i);
                boolean isBetweenThisAndFinger = nextFingerKey.isBetween(this.nodeKey, this.fingerTable.getFingers()[i-1].getNodeKey(), Key.ClBoundsComparison.LOWER);
				if(isBetweenThisAndFinger){
					this.fingerTable.put(this.fingerTable.getFingers()[i-1], i);
				}
				else {
                    Reachable finger = n.findSuccessor(nextFingerKey);
                    if (!finger.equals(this)) {
                        this.fingerTable.put(finger, i);
                    }
                    else {
                        this.fingerTable.put(this.successor,i);
                    }
                }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * Update the entries of others inside the ring
     */
    public void updateOthers() throws RemoteException{
        this.predecessor.setFinger(this,0);
    	for(int i=0; i<this.fingerTable.length(); i++){
    		try {
    			Key predFingerKey = this.predecessor(i);
                	Reachable p;
			p = this.findPredecessor(predFingerKey);
                	if (!p.equals(this)){
                    		p.updateFingerTable(this,i);
               		 }
		     } catch (Exception e) {
				e.printStackTrace();
			}
    	}
    }
    
    public synchronized void updateFingerTable(Reachable s,int i) throws RemoteException {
        if (s.getNodeKey().isBetween(this.nodeKey, this.fingerTable.getFingers()[i].getNodeKey(), Key.ClBoundsComparison.LOWER)) {
            this.fingerTable.put(s, i);
            this.predecessor.updateFingerTable(s, i);
        }
    }

    /**
     * Periodically verify immediate successor
     */
    public void stabilize() throws RemoteException{
        Reachable x = this.successor.getPredecessor();
        if(x.getNodeKey().isBetween(this.nodeKey,this.successor.getNodeKey(), Key.ClBoundsComparison.NONE)){
            this.setSuccessor(x);
            this.successor.notify(this);
        }
    }

    /** Fix predecessor if notifier is closer than the current one **/
    public void notify(Reachable candidatePredecessor) throws RemoteException{
        if(this.predecessor==null || candidatePredecessor.getNodeKey().isBetween(this.predecessor.getNodeKey(),this.getNodeKey(), Key.ClBoundsComparison.NONE)){
            this.setPredecessor(candidatePredecessor);
        }
    }

    /**
     * Periodically refresh finger table entries
     */
    public void fixFingers() throws RemoteException{
        int i = (int)(this.fingerTable.length()*Math.random());
        if (i>0 && this.fingerTable.getFingers()[i] != null){
            this.fingerTable.put(this.findSuccessor(this.fingerTable.getFingers()[i].getNodeKey()),i);
        }
    }

    /**
     * Checks whether predecessor has failed
     */
    public void checkPredecessor() throws RemoteException {
        if (this.predecessor!= null && !this.predecessor.isAlive()) {
            this.predecessor = null;
        }
    }

    /**
     * Used by other nodes to identify status
     * @return
     */
    public boolean isAlive(){
        return this.isAlive;
    }

    /**
     * Transfer files to the responsible node (called upon node join/leave incidents)
     * @param responsible
     */
    public void transferFilesTo(Reachable responsible) throws RemoteException {
        System.out.println("Seeking for candidate files/key for transfer...");
        boolean joinIncident = responsible.equals(this.predecessor);
        boolean leaveIncident = !joinIncident && responsible.equals(this.successor);
        List<File> filesToTransfer = new ArrayList<>();
        if (joinIncident) {
            if (responsible.getPredecessor().getNodeKey().compareTo(responsible.getNodeKey()) == -1) {
                filesToTransfer = this.fileManager.getFilesInRange(responsible.getPredecessor().getNodeKey(), responsible.getNodeKey());
            } else {
                BigInteger minKeyInRing = BigInteger.valueOf(0);
                BigInteger maxKeyInRing = BigInteger.valueOf(2);
                maxKeyInRing = maxKeyInRing.pow(Hash.getKeyLength());
                filesToTransfer = this.fileManager.getFilesInRange(responsible.getPredecessor().getNodeKey(), new Key(maxKeyInRing.toByteArray()));
                filesToTransfer.addAll(this.fileManager.getFilesInRange(new Key(minKeyInRing.toByteArray()), responsible.getNodeKey()));
            }
        }
        else if (leaveIncident) {
            File filesDir = new File(Settings.FILES_DIR);
            File[] files = filesDir.listFiles();
            if (files != null) {
                filesToTransfer = Arrays.asList(files);
            }
        }
        try {
            if (!filesToTransfer.isEmpty()){
                System.out.println("Transferring files to : "+responsible.getIp()+":"+responsible.getListeningPort());
                responsible.awakeToReceive(this);
                Thread fileTransmission = new Thread(new FileTransmitter(responsible,filesToTransfer));
                fileTransmission.start();
                fileTransmission.join();
                this.fileManager.removeFiles(filesToTransfer);
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    /**
     * Used by other nodes to notify (this), so that it starts receiving
     * @param notifier
     */
    public void awakeToReceive(Reachable notifier){
        if (notifier.equals(this.predecessor) || notifier.equals(this.successor)){
            this.receiveFilesUnderResponsibility();
        }
    }

    /**
     * Receive the files that fall under responsibility -> within the range of (pred.Key-this.Key]
     */
    private void receiveFilesUnderResponsibility(){
        System.out.println("Receiving files under responsibility");
        Thread receiveService = new Thread(new FileReceiver(this.fileReceivingPort));
        receiveService.start();
        System.out.println("Files Received successfully!");
    }

    /**
     * ------------Auxiliary key calculation methods used for finger creation------------
     */

    /**
     * Calculates the ith entry of the fingerTable.
     * @param i entry number
     * @return appropriate key value.
     */
	private Key successor(int i) {
	BigInteger rterm = BigInteger.valueOf(2);
	rterm = rterm.pow(i);
	BigInteger fingerKey = this.nodeKey.toBigInt().add(rterm);
        BigInteger divisor = BigInteger.valueOf(2);
        divisor = divisor.pow(Hash.getKeyLength());
        fingerKey = fingerKey.mod(divisor);
	return new Key(fingerKey.toByteArray());
	}

    /**
     * Calculates the ith key of incoming neighbours to this node.
     * @param i entry number
     * @return appropriate key value.
     */
	private Key predecessor(int i) {
	BigInteger rterm = BigInteger.valueOf(2);
	rterm = rterm.pow(i);
	BigInteger fingerKey = this.nodeKey.toBigInt().subtract(rterm);
        if (fingerKey.compareTo(BigInteger.ZERO)== -1){
            BigInteger maxKeyVal = BigInteger.valueOf(2);
            maxKeyVal = maxKeyVal.pow(Hash.getKeyLength());
            fingerKey = maxKeyVal.add(fingerKey).add(BigInteger.ONE);
        }
	return new Key(fingerKey.toByteArray());
	}

    /**
     * ------------Methods that search inside the ring based on a given key------------
     */

    /**
     * Finds the logged successor of the given key
     * @param key given key
     * @return the successor
     */
	public Reachable findSuccessor(Key key) throws RemoteException{
        return findPredecessor(key).getSuccessor();
	}

    /**
     * Finds the logged predecessor of the given key
     * @param key given key
     * @return the successor
     */
	public Reachable findPredecessor(Key key) throws RemoteException {
        Reachable n = this;
        Reachable lastCandidatePred = this;
        while (!key.isBetween(n.getNodeKey(), n.getSuccessor().getNodeKey(), Key.ClBoundsComparison.UPPER)) {
            n = n.closestPrecedingFinger(key);
            //if we meet the same node twice then the new key doesn't belong to the current intervals
            //so it must either be the minimum or the maximum in the ring(either way it's pred is the current max key)
            if (n.equals(lastCandidatePred)) {
                return n.findMax();
            }
            lastCandidatePred = n;
        }
        return n;
    }

    /**
     * Returns the closest to the given key preceding node from the fingerTable.
     * @param key given key
     * @return closest predecessor
     */
	public Reachable closestPrecedingFinger(Key key) throws RemoteException{
		for(int i=this.fingerTable.length()-1; i>=0; i--){
            if(this.nodeKey.equals(this.nodeKey.min(this.fingerTable.getFingers()[i].getNodeKey()))){
                if(this.fingerTable.getFingers()[i].getNodeKey().isBetween(this.nodeKey,key, Key.ClBoundsComparison.NONE)){
                    return this.fingerTable.getFingers()[i];
                }
            }
            else {
                if (key.isBetween(this.fingerTable.getFingers()[i].getNodeKey(),this.nodeKey, Key.ClBoundsComparison.NONE)){
                    return this.fingerTable.getFingers()[i];
                }
            }
		}
        return this;
	}

    /**
     * Returns the node with the maximum key inside the ring.
     * @return
     */
    public Reachable findMax() throws RemoteException{
        for(int i=this.fingerTable.length()-1; i>=0; i--) {
            Reachable finger = this.fingerTable.getFingers()[i];
            if (!this.equals(finger)) {
                if (finger.getNodeKey().equals(finger.getNodeKey().max(this.nodeKey))) {
                    return finger.findMax();
                }
            }
        }
        return this;
    }

	/**
	 * ------------Accessors - Mutators------------
     */


    public InetAddress getIp(){ return this.ip; }

    public int getFileReceivingPort(){ return this.fileReceivingPort; }

    public int getListeningPort(){ return this.listeningPort; }

    public Server getServer(){ return server; }

    public Key getNodeKey(){ return this.nodeKey; }

    public Reachable getSuccessor(){ return successor; }

    public synchronized void setSuccessor(Reachable successor){ this.successor = successor; }

    public Reachable getPredecessor(){ return predecessor; }

    public synchronized void setPredecessor(Reachable predecessor){ this.predecessor = predecessor; }

    public synchronized void setFinger(Reachable finger,int i){
        if (finger.equals(this.successor)){
            this.fingerTable.put(finger,i);
        }
    }

    public Cache<Key, Record<String>> getCache() { return this.cache; }

    public FileManager getFileManager() { return this.fileManager; }

    public String printFingerTable() throws RemoteException{
        String result="\n\t=== FingerTable ===\n";
        int entry = 0;
        for(Reachable f: this.fingerTable.getFingers()){
            result+="\nEntry: "+entry+"-->"+f.getNodeKey().toBigInt();
            entry++;
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode *37 + this.listeningPort;
        hashCode = hashCode*23+ this.ip.hashCode();
        return hashCode;
    }


    @Override
    public String toString() {
        try {
            String res = "\nNode: "+this.ip.toString()+":"+this.listeningPort;
            res+= "\nkey:"+this.nodeKey.toBigInt();
            res+= "\nprd:"+this.predecessor.getNodeKey().toBigInt()+"\nsuc:"+this.successor.getNodeKey().toBigInt()+"\n";
            res+= this.printFingerTable();
            return res;
        }catch (RemoteException re){
            return re.getMessage();
        }
    }

    public void notifyUpdates() throws RemoteException{
        printUpdates();
    }

    private void printUpdates()throws RemoteException{
        BigInteger predKey = this.predecessor.getNodeKey().toBigInt();
        BigInteger succKey = this.successor.getNodeKey().toBigInt();
        System.out.println("\n UPDATES \nkey: "+this.nodeKey.toBigInt()+"\npred: "+predKey+"\nsucc: "+succKey);
    }

}
