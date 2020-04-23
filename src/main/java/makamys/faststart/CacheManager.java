package makamys.faststart;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class CacheManager {
    
    static void loadLCLCache() {
        System.out.println("Loading LaunchClassLoader cache.");
        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream("testCache.dat")))){
            
            LaunchClassLoader lcl = (LaunchClassLoader)Launch.classLoader;
            
            Field cacheField = LaunchClassLoader.class.getDeclaredField("cachedClasses");
            
            cacheField.setAccessible(true);
            
            Map<String, Class<?>> cachedClasses = (Map<String, Class<?>>)cacheField.get(lcl);
            
            System.out.println("There are " + cachedClasses.size() + " cached classes.");
            
            //FileOutputStream out0 = new FileOutputStream(outFile);
            //BufferedOutputStream out1 = new BufferedOutputStream(out0);
            
            int count = 0;
            try {
                while(true) {
                    String key = (String)in.readObject();
                    Class<?> value = (Class<?>)in.readObject();
                    
                    cachedClasses.put(key, value);
                    System.out.println("Read " + key);
                    count++;
                }
            } catch(EOFException e) {}
            
            
            System.out.println("Loaded " + count + " cached classes");
            System.out.println("There are now " + cachedClasses.size() + " cached classes.");
        } catch (Exception e) {
            System.out.println("Exception loading LaunchClassLoader cache.");
            e.printStackTrace();
        }
    }
    
    protected static void saveLCLCache() {
        
        System.out.println("Saving cached classes");
        try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("testCache.dat")))){
            
            LaunchClassLoader lcl = (LaunchClassLoader)Launch.classLoader;
            
            Field cacheField = LaunchClassLoader.class.getDeclaredField("cachedClasses");
            
            cacheField.setAccessible(true);
            
            Map<String, Class<?>> cachedClasses = (Map<String, Class<?>>)cacheField.get(lcl);
            System.out.println("There are " + cachedClasses.size() + " cached classes.");
            
            //FileOutputStream out0 = new FileOutputStream(outFile);
            //BufferedOutputStream out1 = new BufferedOutputStream(out0);
            
            for(Entry<String, Class<?>> entry: cachedClasses.entrySet()) {
                try {
                    String key = entry.getKey();
                    Class<?> value = entry.getValue();
                    
                    
                    try {
                        System.out.println("getting fields of " + key);
                        Field[] fields = value.getDeclaredFields();System.out.println("got fields of " + key);
                        //System.out.println("got fields of " + key);
                        
                        out.writeObject(key);
                        out.writeObject(value);
                        System.out.println("Wrote " + key);
                    } catch(NoClassDefFoundError e) {
                        //System.out.println("oops");
                        e.printStackTrace();
                        //System.out.println("whatever.");
                    }
                    
                    
                    
                    //if(key.startsWith("net.minecraft.")) {
                        
                    //}
                } catch(Exception e) {
                    System.out.println("hmm, failed to save " + entry.getKey());
                    e.printStackTrace();
                }
            }
            System.out.println("Saved cached classes");
        } catch (Exception e) {
            System.out.println("Exception hacking LaunchClassLoader cache.");
            e.printStackTrace();
        }
    }
    
    static class SaveThread extends Thread {
        
        private int saveInterval = 15000;
        
        public SaveThread() {
            setName("FastStart save thread");
            setDaemon(false);
        }
        
        @Override
        public void run() {
            while(true) {
                try {
                    Thread.sleep(saveInterval);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //saveLCLCache();
            }
        }
    }
}
