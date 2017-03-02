package memCache.ring;

import java.rmi.RemoteException;
import java.util.TimerTask;

/**
 * Periodically refreshes entries to keep them valid
 */
public class Stabilizer extends TimerTask{

    private Node self;

    public Stabilizer(Node self){
        this.self = self;
    }

    @Override
    public void run() {
        try {
            self.checkPredecessor();
            self.stabilize();
            self.fixFingers();
        }catch (RemoteException re){
            re.getMessage();
        }
    }
}
