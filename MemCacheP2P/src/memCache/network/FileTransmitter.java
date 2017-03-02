package memCache.network;

import memCache.ring.Reachable;

import java.io.*;
import java.net.Socket;
import java.util.List;


public class FileTransmitter implements Runnable {

    private Reachable responsible;

    private List<File> filesToTransfer;

    public FileTransmitter(Reachable responsible, List<File> filesToTransfer){
        this.responsible = responsible;
        this.filesToTransfer = filesToTransfer;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(responsible.getIp(), responsible.getFileReceivingPort());
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(filesToTransfer.size());
            for(File file : filesToTransfer){
                long length = file.length();
                dos.writeLong(length);
                String name = file.getName();
                dos.writeUTF(name);
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                int currentByte;
                while((currentByte = bis.read()) != -1){
                    bos.write(currentByte);
                }
                bis.close();
            }
            dos.close();
            socket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
