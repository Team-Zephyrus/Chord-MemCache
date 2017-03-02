package memCache.config;

import common.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Properties;

/**
 * Configuration Settings of the node.
 */
public class Settings {

    private static Properties PROPERTIES;

    public static InetAddress IP ;

    public final static int LISTENING_PORT;

    public final static int FILE_RECEIVING_PORT;

    private static ServerSocket reservedFPort;

    public final static String FILES_DIR;

    public final static String UPDATE_LINK;

    public final static String DIRECTIONS_API_LINK;

    public final static String APP_KEY;

    public static Pair<InetAddress,Integer> GATE;

    public static String OS;


    static {
        PROPERTIES = load();
        IP = initializeIP();
        LISTENING_PORT = 8080;
        FILE_RECEIVING_PORT = initializeFileReceivingPort();
        FILES_DIR = initializeFilesDir();
        UPDATE_LINK = initializeUpdateLink();
        DIRECTIONS_API_LINK = initializeDirectionsAPILink();
        APP_KEY = initializeAppKey();
        GATE = initializeGate();
        OS = System.getProperty("os.name").toLowerCase();
    }

    public static Properties load(){
        try {
            Properties properties = new Properties();
            File configurationFile = new File(new File(".").getCanonicalPath()+"/src/memCache/config.xml");
            FileInputStream fis = new FileInputStream(configurationFile);
            properties.loadFromXML(fis);
            fis.close();
            return properties;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    private static int initializeFileReceivingPort(){
        try {
            reservedFPort = new ServerSocket(0);
            int fileReceivePort = reservedFPort.getLocalPort();
            reservedFPort.close();
            return fileReceivePort;
        }catch (Exception e){
            return 0;
        }
    }

    private static String initializeFilesDir(){
        try {
            return System.getProperty("user.dir")+"/src/memCache"+ PROPERTIES.getProperty("FILES_DIR");
        }catch (Exception e){
            return null;
        }
    }

    private static String initializeUpdateLink(){
        try {
            return PROPERTIES.getProperty("UPDATE_LINK");
        }catch (Exception e){
            return null;
        }
    }

    private static String initializeDirectionsAPILink(){
        try {
            return PROPERTIES.getProperty("DIRECTIONS_API_LINK");
        }catch (Exception e){
            return null;
        }
    }

    private static String initializeAppKey(){
        try {
            return PROPERTIES.getProperty("APP_KEY");
        }catch (Exception e){
            return null;
        }
    }

    private static InetAddress initializeIP(){
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com/");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            return InetAddress.getByName(in.readLine());
        }catch (Exception e){
            return null;
        }
    }

    private static Pair<InetAddress,Integer> initializeGate(){
        try {
            String[] gate = PROPERTIES.getProperty("GATE").split(":");
            return new Pair<>(InetAddress.getByName(gate[0]),Integer.parseInt(gate[1]));
        }catch (Exception e){
            return null;
        }
    }

}
