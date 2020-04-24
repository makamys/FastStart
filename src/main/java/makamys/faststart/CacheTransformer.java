package makamys.faststart;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class CacheTransformer implements IClassTransformer, ListAddListener<IClassTransformer> {
	
	private static final Logger logger = LogManager.getLogger("faststart");
	
	// TODO can't this be compressed into a lambda?
	
	static class SaveThread extends Thread {
		
		private CacheTransformer cacheTransformer;
		
		private int saveInterval = 10000;
		
		public SaveThread(CacheTransformer ct) {
			this.cacheTransformer = ct;
			setName("CacheTransformer save thread");
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
				cacheTransformer.saveCache();
			}
		}
	}
	
	private List<IClassTransformer> transformers;
	private AddListenableListView<IClassTransformer> wrappedTransformers;
	private WrappedMap<String, Class<?>> wrappedCachedClasses;
	
	private List<IClassTransformer> proxiedTransformers = new ArrayList<>();
	
	private Map<String, byte[]> cache = new ConcurrentHashMap<>();
	
	private SaveThread saveThread = new SaveThread(this);
	
	private Set<String> badTransformers = new HashSet<>(Arrays.asList("org.spongepowered.asm.mixin.transformer.Proxy", "appeng.transformer.asm.ApiRepairer"));
	
	public static final boolean DEBUG_PRINT = Boolean.parseBoolean(System.getProperty("cachetransformer.debug", "false"));
	
	private int lastSaveSize = 0;
	
	public CacheTransformer(List<IClassTransformer> transformers, AddListenableListView<IClassTransformer> wrappedTransformers, WrappedMap<String, Class<?>> wrappedCachedClasses) {
		this.transformers = transformers;
		this.wrappedTransformers = wrappedTransformers;
		this.wrappedCachedClasses = wrappedCachedClasses;
		
		//eatThemAll();
		
		loadCache();
		saveThread.start();
	}
	
	private void eatThemAll() {
		Iterator<IClassTransformer> it = transformers.iterator();
		while(it.hasNext()) {
			IClassTransformer t = it.next();
			if(!(t instanceof CacheTransformer)) {
				proxiedTransformers.add(t);
				it.remove();
			}
		}
	}
	
	private void loadCache() {
		File inFile = new File(Launch.minecraftHome, "classCache.dat");
		
		if(inFile.exists()) {
			//synchronized(cache) {
				logger.info("Loading class cache.");
				cache.clear();
				try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(inFile)))){
					
					try {
    					while(true) { // EOFException should break the loop
    						String className = in.readUTF();
    						int classLength = in.readInt();
    						byte[] classData = new byte[classLength];
    						in.read(classData, 0, classLength);
    						
    						cache.put(className, classData);
    						wrappedCachedClasses.setBlackList(cache.keySet());
    						
    						superDebug("Loaded " + className);
    					}
					} catch(EOFException eof) {}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				logger.info("Loaded " + cache.size() + " cached classes.");
				
				lastSaveSize = cache.size();
			//}
		} else {
			logger.info("Couldn't find class cache file");
		}
	}
	
	private void saveCache() {
		int size = cache.size();
		if(size == lastSaveSize) {
			return; // don't save if the cache hasn't changed
		}
		
		File outFile = new File(Launch.minecraftHome, "classCache.dat");
		try {
            outFile.createNewFile();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
		
		//synchronized(cache) {
			logger.info("Saving class cache (size changed from " + lastSaveSize + " to " + size + ")");
			try{
			    FileOutputStream out0 = new FileOutputStream(outFile);
			    //BufferedOutputStream out1 = new BufferedOutputStream(out0);
			    DataOutputStream out = new DataOutputStream(out0);
				for(Entry<String, byte[]> entry : cache.entrySet()) {
				    if(entry.getValue() == null) {
				        System.out.println("wtf, " + entry + " is null");
				    } else {
    					out.writeUTF(entry.getKey());
    					out.writeInt(entry.getValue().length);
    					out.write(entry.getValue());
				    }
				}
				logger.info("Saved class cache");
			} catch (IOException e) {
				logger.info("Exception saving class cache");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		//}
		
		lastSaveSize = size;
	}
	
	private String describeBytecode(byte[] basicClass) {
	    return basicClass == null ? "null" : String.format("length: %d, hash: %x", basicClass.length, basicClass.hashCode());
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		byte[] result = null;
		
	    superDebug(String.format("Starting loading class %s (%s) (%s)", name, transformedName, describeBytecode(basicClass)));
		
		//eatThemAll();
		
		try {
    		//synchronized(cache) {
    		    //System.out.println("time to get ");
    			if(cache.containsKey(transformedName)) {
    				result = cache.get(transformedName); // yay, we have it cached
    				superDebug("Yay, we have it cached!");
    			} else {
    				// fall back to normal behavior..
    				//transformers.clear();
    				//transformers.addAll(proxiedTransformers);
    			    for (final IClassTransformer transformer : wrappedTransformers.original) {
    				//for (final IClassTransformer transformer : transformers) {
    				//for (final IClassTransformer transformer : proxiedTransformers) { 
    				    if(transformer == this) {
    				        System.out.println("oops,");
    				    }
    				    
    				    if(badTransformers.contains(transformer.getClass().getName())) {
    				        wrappedTransformers.alt = null; // HIDE!
    				    }
    				    
    				    superDebug(String.format("Before transformer: %s (%s)", transformer.getClass().getName(), describeBytecode(basicClass)));
    	                basicClass = transformer.transform(name, transformedName, basicClass);
    	                superDebug(String.format("After transformer: %s (%s)", transformer.getClass().getName(), describeBytecode(basicClass)));
    	                
    	                if(wrappedTransformers.alt == null) {
    	                    wrappedTransformers.alt = this; // reappear
    	                }
    	            }
    			    if(basicClass != null) {
    			        cache.put(transformedName, basicClass); // then cache it
    			    }
    				//transformers.clear();
    				//transformers.add(0, this);
    				result = basicClass;
    			}
    		//}
		} catch(Exception e) {
		    e.printStackTrace();
		}

		wrappedTransformers.alt = this;
	    superDebug(String.format("Finished loading class %s (%s) (%s)", name, transformedName, describeBytecode(basicClass)));
		return result;
	}
	    
	private void superDebug(String msg) {
	    if(DEBUG_PRINT) {
	        logger.trace(msg);
	    }
	}
	
	public static CacheTransformer register() {
		CacheTransformer cacheTransformer = null;
    	try {
            LaunchClassLoader lcl = (LaunchClassLoader)Launch.classLoader;
            
            
            Field transformersField = LaunchClassLoader.class.getDeclaredField("transformers");
            transformersField.setAccessible(true);
            List<IClassTransformer> transformers = (List<IClassTransformer>)transformersField.get(lcl);
            
            AddListenableListView<IClassTransformer> listenableTransformers = 
            		new AddListenableListView<IClassTransformer>(transformers);
            
            transformersField.set(lcl, listenableTransformers);

            
            Field cachedClassesField = LaunchClassLoader.class.getDeclaredField("cachedClasses");
            cachedClassesField.setAccessible(true);
            Map<String, Class<?>> cachedClasses = (Map<String, Class<?>>)cachedClassesField.get(lcl);
            //cachedClasses.clear(); // gotta do this to make Mixin happy
            
            WrappedMap<String, Class<?>> wrappedCachedClasses = new WrappedMap<String, Class<?>>(cachedClasses);
            cachedClassesField.set(lcl, wrappedCachedClasses);
            
            
            cacheTransformer = new CacheTransformer(transformers, listenableTransformers, wrappedCachedClasses);
            
            
            listenableTransformers.addListener(cacheTransformer);
            
            
            //transformers.add(0, (IClassTransformer)cacheTransformer);
            listenableTransformers.alt = cacheTransformer;
            
            System.out.println("Finished initializing cache transformer");
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            System.out.println("Exception registering cache transformer.");
            e.printStackTrace();
        }
    	return cacheTransformer;
	}

	@Override
	public void onElementAdded(int index, IClassTransformer addedElement) {
		//eatThemAll();
	}

	@Override
	public void beforeIterator() {
		//eatThemAll();
	}

}
