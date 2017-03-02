package common;

import java.io.Serializable;
import java.net.InetAddress;


public class Request implements Serializable{

    private static final long serialVersionUID = 9192068679042440338L;

    /** IP address of the client **/
    private InetAddress ip;

    /** Port to receive the requested file **/
    private int port;

    /** Requested file **/
    private String fileName;

    /** File hash key **/
    private Key fileKey;



    public Request(InetAddress ip, int port, String fileName, Key fileKey) {
        this.setIp(ip);
        this.setPort(port);
        this.setFileName(fileName);
        this.setFileKey(fileKey);
    }

    public Request(String fileName,int portToReceive) {
        this(null, portToReceive, fileName, null);
    }


    /**
     * ------------Accessors - Mutators------------
     */
    public InetAddress getIp() { return ip; }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Key getFileKey() {
        return fileKey;
    }

    public void setFileKey(Key fileKey) {
        this.fileKey = fileKey;
    }


}
