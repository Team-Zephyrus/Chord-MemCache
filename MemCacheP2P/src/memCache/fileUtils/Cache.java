package memCache.fileUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Cache <K,R extends Cacheable> {

    /** Records written in cache **/
    private Map<K,R> records;


    public Cache(int size,int purgeFreqMinutes){
        this.records = new HashMap<>(size);
        Thread purgeMechanism = new Thread(new PurgeMechanism(purgeFreqMinutes*60000));
        purgeMechanism.setDaemon(true);
        purgeMechanism.start();
    }


    /**
     * Stores a given record in the cache along with it's key.
     * @param key
     * @param record
     */
    public void store(K key,R record){
        synchronized (records){
            this.records.put(key,record);
        }
    }

    /**
     * Retreives the record with the specific key.
     * @param key
     * @return
     */
    public R get(K key){
        synchronized (records){
            return this.records.get(key);
        }
    }


    /**
     * Returns true if the given key is contained in the stored keys, false otherwise.
     * @param key
     * @return
     */
    public boolean contains(K key){
        synchronized (records){
            return (!this.records.isEmpty() && this.records.containsKey(key));
        }
    }

    /**
     * Purge mechanism of the cache.
     */
    private class PurgeMechanism implements Runnable{

        /** time interval between purges **/
        private long purgeInterval;

        private PurgeMechanism(long purgeInterval){ this.purgeInterval = purgeInterval; }

        @Override
        public void run() {
            while (true){
                try {
                    Thread.sleep(this.purgeInterval);
                    this.purge();
                    Thread.yield();
                }
                catch (InterruptedException ie){
                    ie.printStackTrace();
                }
            }
        }

        /**
         * Method that performs the purging of the cache
         */
        private void purge(){
            List<K> toBeDeleted = new ArrayList<>();
            synchronized (records){
                for (K key : records.keySet()){
                    if (records.get(key).isExpired()){
                        toBeDeleted.add(key);
                    }
                }
                for (K key : toBeDeleted){
                    records.remove(key);
                }
            }
        }
    }

}
