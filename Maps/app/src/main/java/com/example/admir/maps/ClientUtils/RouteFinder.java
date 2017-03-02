package com.example.admir.maps.ClientUtils;

import android.os.AsyncTask;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import common.Hash;
import common.Key;
import common.Request;

/**
 * Created by admir on 15/2/2017.
 */

public final class RouteFinder extends AsyncTask<String, Void, String> {
    private String from;
    private String to;
    private String mode;
    private String myIp;
    private String destinationIp;
    private int destinationPort;
    private Socket clientConnection;
    private static ServerSocket serverSocket;
    public AsyncResponce delegate = null;

    public RouteFinder(String from,String to,String mode,String myIp,String destinationIp,int destinationPort) {
        super();
        this.from=from;
        this.to=to;
        this.mode=mode;
        this.myIp=myIp;
        this.destinationIp=destinationIp;
        this.destinationPort=destinationPort;
        // do stuff
    }

    @Override
    protected String doInBackground(String... params) {
            String fileName = from + "_" + to + "_" + mode;
            Hash.hash(fileName);
            Key fileKey = new Key(Hash.getHashedValue());
            try {
                Request request = new Request(InetAddress.getByName(myIp), 8082, fileName, fileKey);
                clientConnection = new Socket(InetAddress.getByName(destinationIp),destinationPort);
                ObjectOutputStream oos = new ObjectOutputStream(clientConnection.getOutputStream());
                oos.writeObject(request);
                oos.flush();
                oos.close();
                clientConnection.close();
                if(serverSocket!=null){
                    serverSocket.close();
                }
                serverSocket = new ServerSocket(8082);
                Socket serverConnection = serverSocket.accept();
                ObjectInputStream ois = new ObjectInputStream(serverConnection.getInputStream());
                String route = (String) ois.readObject();
                return route;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }


    }


    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }

@Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(Void... values) {}


}

