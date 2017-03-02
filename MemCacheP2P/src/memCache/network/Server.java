package memCache.network;


import memCache.ring.Node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;


public class Server {

    /** The node in which the server is running **/
    private Node owner;

    /** Socket running on the owner node**/
    private ServerSocket serverSocket;

    /** Client connection **/
    private Socket clientSocket;

    /** The port that the server listens to **/
    private int portToListen;

    /** Whether to stop the server **/
    private boolean stop;


    public Server(Node owner){
        try {
            this.owner = owner;
            this.portToListen = owner.getListeningPort();
            this.stop = false;
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Starts running the server on the specified port.
     */
    public void start(){
    	System.out.println("\nServer is on...waiting for requests on "+this.owner.getIp()+":"+this.owner.getListeningPort());
        try {
            serverSocket = new ServerSocket(portToListen);
            while (!stop) {
                clientSocket = serverSocket.accept();
                System.out.println("\nConnected with : "+clientSocket.getInetAddress()+" at "+new Date());
                Thread service = new Thread(new RequestHandler(owner, clientSocket));
                service.start();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            this.stop();
        }
    }

    /**
     * Terminates the server.
     */
    public void stop(){
        try {
            this.stop = true;
            this.serverSocket.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }



}
