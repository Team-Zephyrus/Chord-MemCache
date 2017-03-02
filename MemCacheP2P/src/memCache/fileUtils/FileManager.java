package memCache.fileUtils;

import common.Key;
import memCache.config.Settings;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class FileManager {


    public final String filesDirPath = Settings.FILES_DIR;

    private File requestedFile;

    public synchronized boolean storeToDisk(Key key,String content){
        try {
            this.requestedFile = new File(this.filesDirPath + key.toBigInt() + ".json");
            if (!this.requestedFile.exists()) {
                this.requestedFile.createNewFile();
                FileWriter fw = new FileWriter(this.requestedFile);
                fw.write(content);
                fw.flush();
                fw.close();
            }
            return true;
        }
        catch (Exception e){
            return false;
        }
    }


    public synchronized String getFile(Key key){
        this.requestedFile = new File(this.filesDirPath + key.toBigInt() + ".json");
        try {
            if (this.requestedFile.exists()){
                StringBuilder sb = new StringBuilder();
                FileReader fr = new FileReader(this.requestedFile);
                BufferedReader br = new BufferedReader(fr);
                String line;
                while ((line=br.readLine())!= null){
                    sb.append(line);
                }
                br.close();
                fr.close();
                return sb.toString();
            }
            else {
                throw new FileNotFoundException();
            }

        }
        catch (Exception e){
            return null;
        }
    }

    public synchronized List<File> getFilesInRange(Key low, Key high){
        List<File> matched = new ArrayList<>();
        File dir = new File(this.filesDirPath);
        for (File file : dir.listFiles()){
            String key = file.getName().replace(".json","");
            BigInteger keyBIval= new BigInteger(key);
            Key fileKey = new Key(keyBIval.toByteArray());
            if (fileKey.isBetween(low,high, Key.ClBoundsComparison.UPPER)){
                matched.add(file);
            }
        }
        return matched;
    }


    public synchronized boolean fileExists(Key key){
        this.requestedFile = new File(this.filesDirPath + key.toBigInt() + ".json");
        return this.requestedFile.exists();
    }

    public void removeFiles(List<File> filesToRemove){
        for (File file:filesToRemove){
            file.delete();
        }
    }


}
