package memCache.ring;

import common.Key;

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI Interface used by the nodes of the ring to communicate with each other
 */
public interface Reachable extends Remote {


    Reachable findSuccessor(Key key)throws RemoteException ;

    Reachable findPredecessor(Key key)throws RemoteException;

    Reachable closestPrecedingFinger(Key key)throws RemoteException;

    void updateFingerTable(Reachable node, int i) throws RemoteException;

    Reachable findMax() throws RemoteException;

    void transferFilesTo(Reachable responsible) throws RemoteException;

    void awakeToReceive(Reachable notifier) throws RemoteException;

    void notify(Reachable candidatePredecessor) throws RemoteException;

    InetAddress getIp()throws RemoteException;

    int getListeningPort()throws RemoteException;

    int getFileReceivingPort()throws RemoteException;

    Key getNodeKey()throws RemoteException;

    Reachable getSuccessor()throws RemoteException;

    void setSuccessor(Reachable successor) throws RemoteException;

    Reachable getPredecessor()throws RemoteException;

    void setPredecessor(Reachable predecessor) throws RemoteException;

    void setFinger(Reachable finger, int index) throws RemoteException;

    boolean isAlive() throws RemoteException;

    void notifyUpdates() throws RemoteException;


}
