package memCache.network;

import common.Key;
import common.Request;
import memCache.config.Settings;
import memCache.fileUtils.Record;
import memCache.ring.Node;
import memCache.ring.Reachable;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;

/**
 * Class that handles all requests reaching a specific chord node (owner).
 * Based on the request it either sends the file to the initial client (if the node is responsible for it)
 * or forwards the request inside the ring according to the protocol until it meets the responsible node.
 */
public class RequestHandler implements Runnable {

    /** Node of the ring managing the RH **/
    private Node owner;

    /** Client connection **/
    private Socket connection;

    /** Client request **/
    private Request request;



	public RequestHandler(Node owner,Socket connection){
        this.owner = owner;
        this.connection = connection;
    }

    /**
     * Reading and handling the requests that meet the server running on the (owner) node of chord.
     */
    @Override
	public void run() {
		 try {
             ObjectInputStream ois = new ObjectInputStream(connection.getInputStream());
             this.request = (Request) ois.readObject();
             if (this.owner.getNodeKey().compareTo(this.owner.getPredecessor().getNodeKey())==1){
                 if (request.getFileKey().isBetween(this.owner.getPredecessor().getNodeKey(),this.owner.getNodeKey(), Key.ClBoundsComparison.UPPER)){
                     System.out.println("Searching for requested file...");
                     this.sendFile();
                 }
                 else {
                     Reachable responsibleNode = this.owner.findSuccessor(request.getFileKey());
                     this.forward(responsibleNode.getIp(),responsibleNode.getListeningPort());
                     System.out.println("Forwarded request to : "+responsibleNode.getIp()+":"+responsibleNode.getListeningPort());
                 }
             }
             else {
                 if (request.getFileKey().isBetween(this.owner.getNodeKey(),this.owner.getPredecessor().getNodeKey(), Key.ClBoundsComparison.UPPER)){
                     Reachable responsibleNode = this.owner.findSuccessor(request.getFileKey());
                     this.forward(responsibleNode.getIp(),responsibleNode.getListeningPort());
                     System.out.println("Forwarded request to : "+responsibleNode.getIp()+":"+responsibleNode.getListeningPort());
                 }
                 else {
                     System.out.println("Searching for requested file...");
                     this.sendFile();
                 }
             }
         }
         catch (Exception e) {
				e.printStackTrace();
			}
	}
	
    /**
     * Forwards the request of the file to the next selected node of the Ring.
     * @param ip
     * @param port
     */
	public void forward(InetAddress ip, int port){
		try {
			Socket fSocket = new Socket(ip,port);
			ObjectOutputStream oos = new ObjectOutputStream(fSocket.getOutputStream());
			oos.writeObject(this.request);
			oos.flush();
			oos.close();
			fSocket.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

    /**
     * Sends the file to the client that requested it.
     */
	public void sendFile(){
        String fileContent;
        try {
            //Cache Lookup
            if (this.owner.getCache().contains(request.getFileKey())) {
                System.out.println("File located on cache!");
                fileContent = this.owner.getCache().get(request.getFileKey()).getValue();
            }
            // Disk Lookup
            else if (this.owner.getFileManager().fileExists(request.getFileKey())) {
                System.out.println("File located on disk!");
                fileContent = this.owner.getFileManager().getFile(request.getFileKey());
            }
            // Issue request on Google API
            else {
                System.out.println("Not in possession of the file! Initiating request...");
                fileContent = getGoogleResponse();
                this.owner.getFileManager().storeToDisk(request.getFileKey(),fileContent);
                this.owner.getCache().store(new Key(request.getFileKey().toBigInt().toByteArray()),new Record<>(fileContent,2000));
            }
            // Finally send the content of the requested file
            if (fileContent!=null){
                System.out.println("File retrieved ok!");
                System.out.println("Sending file to : "+InetAddress.getByName(this.request.getIp().getHostAddress())+":"+request.getPort());
                Socket fSocket = new Socket(InetAddress.getByName(this.request.getIp().getHostAddress()), this.request.getPort());
                ObjectOutputStream oos = new ObjectOutputStream(fSocket.getOutputStream());
                oos.writeObject(fileContent);
                oos.flush();
                oos.close();
                fSocket.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
	}

    /**
     * Issues a request on Google Directions API for the desired file.
     * @return
     */
    private String getGoogleResponse(){
        try{
            String[] parameters = request.getFileName().split("_");
            String origin = "origin="+ parameters[0];
            String destination = "&destination="+ parameters[1];
            String mode = "&mode="+ parameters[2];
            String key = "&key="+ Settings.APP_KEY;
            String httpsURL = Settings.DIRECTIONS_API_LINK + origin + destination + mode + key;
            URL myurl = new URL(httpsURL);
            HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
            InputStream ins = con.getInputStream();
            InputStreamReader isr = new InputStreamReader(ins);
            BufferedReader in = new BufferedReader(isr);
            String inputLine;
            StringBuilder sb = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();
            isr.close();
            ins.close();
            return sb.toString();
        }
        catch (Exception e){
            return null;
        }
    }



    /**
     * ------------Accessors - Mutators------------
     */
    public Request getRequest(){ return this.request; }

    public void setRequest(Request request) {
        this.request = request;
    }

	
}
