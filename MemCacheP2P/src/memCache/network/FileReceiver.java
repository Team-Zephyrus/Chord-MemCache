package memCache.network;

import memCache.config.Settings;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class FileReceiver implements Runnable {

    private int fileReceivingPort;

    public FileReceiver(int fileReceivingPort){
        this.fileReceivingPort = fileReceivingPort;
    }

    @Override
    public void run() {
        try{
            ServerSocket serverSocket =  new ServerSocket(this.fileReceivingPort);
            Socket socket = serverSocket.accept();
            BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
            DataInputStream dis = new DataInputStream(bis);
            int filesCount = dis.readInt();
            List<File> files = new ArrayList<>(filesCount);
            for(int i = 0; i < filesCount; i++){
                long fileLength = dis.readLong();
                String fileName = dis.readUTF();
                String filePath = Settings.FILES_DIR + fileName;
                files.add(new File(filePath));
                FileOutputStream fos = new FileOutputStream(files.get(i));
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                for(int j = 0; j < fileLength; j++) {
                    bos.write(bis.read());
                }
                bos.close();
            }
            dis.close();
            serverSocket.close();
        }
        catch (Exception e){ e.printStackTrace(); }
    }
}
