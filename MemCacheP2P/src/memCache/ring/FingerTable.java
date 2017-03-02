package memCache.ring;

/**
 * Fingertable used in chord protocol
 */
public class FingerTable {
	
	private Reachable[] fingers;

	
	public FingerTable(int length){
		this.fingers = new Reachable[length];
	}

	
	public void put(Reachable finger , int i){
        synchronized(this.fingers) {
            this.fingers[i] = finger;
        }
	}

	public int length(){ return fingers.length; }
	
	@Override
	public String toString(){
		String res="";
		for(Reachable f:this.fingers){
			res+=f.toString();
		}
		return res;
	}

    /**
     * ------------Accessors - Mutators------------
     */
    public Reachable[] getFingers(){
        return this.fingers;
    }
}
