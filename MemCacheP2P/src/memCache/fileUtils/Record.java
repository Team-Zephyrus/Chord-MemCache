package memCache.fileUtils;


public class Record <V> implements Cacheable {

    /** The total times that the record is accessed **/
    private int timesAccessed;

    /** Last time the record was accessed **/
    private long lastAccessed;

    /** Lifespan of the record **/
    private long timeToLive;

    /** The actual content **/
    private V value;


    public Record(V value, long timeToLive){
        this.value = value;
        this.timeToLive = timeToLive;
        this.timesAccessed = 1;
        this.lastAccessed = System.currentTimeMillis();
    }


    /**
     * Retrieves the content of the record.
     * @return
     */
    public V getValue(){
        this.timesAccessed++;
        return this.value;
    }


    /**
     * Determines if the record is stale.
     * @return
     */
    @Override
    public boolean isExpired() {
        return (System.currentTimeMillis()>lastAccessed+timeToLive*60);
    }

    @Override
    public String toString(){ return this.value.toString();}
}
