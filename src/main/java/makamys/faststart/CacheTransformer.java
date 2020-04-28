package makamys.faststart;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.Files;

import cpw.mods.fml.relauncher.ModListHelper;
import makamys.faststart.WrappedAddListenableMap.MapAddListener;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class CacheTransformer implements IClassTransformer, ListAddListener<IClassTransformer>, MapAddListener<String, Class<?>> {
	
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
	
	private AddListenableListView<IClassTransformer> wrappedTransformers;
	private WrappedAddListenableMap<String, Class<?>> wrappedCachedClasses;
	
	private Map<String, Optional<byte[]>> cache = new ConcurrentHashMap<>();
	private final static int QUEUE_SIZE = 512;
	Cache<String, byte[]> recentCache = CacheBuilder.newBuilder().maximumSize(QUEUE_SIZE).build();
	
	private SaveThread saveThread = new SaveThread(this);
	
	private Set<String> badTransformers = 
			new HashSet<>(Arrays.stream(Config.badTransformers.split(",")).collect(Collectors.toList()));
	
	public static final boolean DEBUG_PRINT = Boolean.parseBoolean(System.getProperty("cachetransformer.debug", "false"));
	
	private int lastSaveSize = 0;
	private BlockingQueue<String> dirtyClasses = new LinkedBlockingQueue<String>();
	
	public CacheTransformer(List<IClassTransformer> transformers, AddListenableListView<IClassTransformer> wrappedTransformers, WrappedAddListenableMap<String, Class<?>> wrappedCachedClasses) {
		logger.info("Initializing cache transformer");
		
		this.wrappedTransformers = wrappedTransformers;
		this.wrappedCachedClasses = wrappedCachedClasses;
		wrappedCachedClasses.addListener(this);
		
		if(isDevEnvironment() || modsChanged()) {
			clearCache(isDevEnvironment() ? "this is a dev environment." : "mods have changed.");
		} else {
			loadCache();
		}
		saveThread.start();
	}
	
	private static boolean isDevEnvironment() {
		try {
			return Launch.classLoader.getClassBytes("net.minecraft.world.World") != null;
		} catch (IOException e) {
			return false;
		}
	}
	
	private void clearCache(String reason) {
		logger.info("Rebuilding class cache, because " + reason);
		FastStart.getDataFile("classCache.dat").delete();
	}
	
	private List<File> findMods(){
		File modsDir = new File(Launch.minecraftHome, "mods");
		File versionedModsDir = new File(modsDir, ExampleMod.MCVERSION);
		
		List<File> mods = new ArrayList<File>();
		
		for(File dir : Arrays.asList(modsDir, versionedModsDir)) {
			if(dir.isDirectory()) {
				mods.addAll(Arrays.asList(
						modsDir.listFiles(x -> x.getName().endsWith(".jar") 
										|| x.getName().endsWith(".litemod"))));
			}
		}
		
		mods.addAll(ModListHelper.additionalMods.values());
		return mods;
	}
	
	private boolean modsChanged() {
		boolean changed = false;
		
		Persistence.loadIfNotLoadedAlready();
		
		Set<Pair<File, Long>> modFiles = findMods().stream()
				.map(f -> Pair.of(f, f.lastModified())).collect(Collectors.toSet());
		
		Set<Pair<File, Long>> previousModFiles = new HashSet<>();
		
		List<String> lines = Arrays.asList(Persistence.lastMods.split("\n"));
		File lastFile = null;
		for(String line : lines) {
			if(lastFile == null) {
				lastFile = new File(line);
			} else {
				previousModFiles.add(Pair.of(lastFile, Long.parseLong(line)));
				lastFile = null;
			}
		}
		
		changed = previousModFiles.size() != modFiles.size() ||
				!filesMatch(	previousModFiles.stream().sorted().iterator(), modFiles.stream().sorted().iterator());
		
		Persistence.lastMods = String.join("\n", modFiles.stream()
				.map(p -> p.getKey().getPath() + "\n" + p.getValue())
				.collect(Collectors.toList()));
		Persistence.save();
	
		
		return changed;
	}
	
	private boolean filesMatch(Iterator<Pair<File, Long>> aIt, Iterator<Pair<File, Long>> bIt) {
		while(aIt.hasNext()) {
			Pair<File, Long> a = aIt.next(), b = bIt.next();
			
			File fileA = a.getKey(), fileB = b.getKey();
			long dateA = a.getValue(), dateB = b.getValue();
			if(!fileA.equals(fileB) || (dateA != dateB && !Arrays.equals(calculateHash(fileA), calculateHash(fileB)))) {
				return false;
			}
		}
		return true;
	}
	
	private byte[] calculateHash(File f) {
		try(InputStream is = new BufferedInputStream(new FileInputStream(f))){
			return DigestUtils.md5(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[] {};
	}
	
	private void loadCache() {
		File inFile = FastStart.getDataFile("classCache.dat");
		
		if(inFile.exists()) {
			logger.info("Loading class cache.");
			cache.clear();
			
			boolean foundCorruption = false;
			
			try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(inFile)))){
				try {
					while(true) { // EOFException should break the loop
						String className = in.readUTF();
						int classLength = in.readInt();
						byte[] classData = new byte[classLength];
						int bytesRead = in.read(classData, 0, classLength);
						
						if(bytesRead == classLength) {
							cache.put(className, Optional.of(classData));
							
							superDebug("Loaded " + className);
						} else {
							logger.warn("Length of " + className + " doesn't match advertised length. Skipping.");
							foundCorruption = true;
						}
					}
				} catch(EOFException eof) {}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.info("Loaded " + cache.size() + " cached classes.");
			
			lastSaveSize = cache.size();
			
			if(foundCorruption) {
				logger.warn("There was data corruption present in the cache file. Doing full save to restore integrity.");
				saveCacheFully();
			}
		} else {
			logger.info("Couldn't find class cache file");
		}
	}
	
	private void saveCacheFully() {
		File outFile = FastStart.getDataFile("classCache.dat");
		File outFileTmp = FastStart.getDataFile("classCache.dat~");
		
		logger.info("Performing full save of class cache (size: " + cache.size() + ")");
		saveCacheChunk(cache.keySet(), outFileTmp, false);
		
		try {
			Files.move(outFileTmp, outFile);
		} catch (IOException e) {
			logger.error("Failed to finish saving class cache");
			e.printStackTrace();
		}
	}
	
	private void saveCache() {
		int saveSize = lastSaveSize;
		if(dirtyClasses.isEmpty()) {
			return; // don't save if the cache hasn't changed
		}
		
		File outFile = FastStart.getDataFile("classCache.dat");
		try {
            outFile.createNewFile();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
		
		List<String> classesToSave = new ArrayList<String>();
		dirtyClasses.drainTo(classesToSave);
		
		logger.info("Saving class cache (size: " + lastSaveSize + " -> " + cache.size() + " | +" + classesToSave.size() + ")");
		saveCacheChunk(classesToSave, outFile, true);
		
		lastSaveSize += classesToSave.size();
	}
	
	private void saveCacheChunk(Collection<String> classesToSave, File outFile, boolean append) {
		try(DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outFile, append)))){
			for(String name : classesToSave) {
				Optional<byte[]> data = cache.get(name);
		    	if(data != null && data.isPresent()) {
		    		out.writeUTF(name);
					out.writeInt(data.get().length);
					out.write(data.get());
		    	}
			}
			logger.info("Saved class cache");
		} catch (IOException e) {
			logger.info("Exception saving class cache");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String describeBytecode(byte[] basicClass) {
	    return basicClass == null ? "null" : String.format("length: %d, hash: %x", basicClass.length, basicClass.hashCode());
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		byte[] result = null;
		
	    superDebug(String.format("Starting loading class %s (%s) (%s)", name, transformedName, describeBytecode(basicClass)));
		
		try {
			if(cache.containsKey(transformedName)) {
				superDebug("Yay, we have it cached!");
				
				if(cache.get(transformedName).isPresent()) { // we still remember it
					result = cache.get(transformedName).get();
					
					// classes are only loaded once, so no need to keep it around in RAM
					cache.put(transformedName, Optional.empty());
					
					// but keep it around in case it's needed again by another transformer in the chain
					//recentCache.put(transformedName, result);
				} else { // we have forgotten it, hopefully it's still around in the recent queue
					result = recentCache.getIfPresent(transformedName);
					if(result == null) {
						logger.error("Couldn't find " + transformedName + " in cache. Is recent queue too small? (" + QUEUE_SIZE + ")");
					}
				}
			}
			if(result == null){
				// fall back to normal behavior..
			    for (final IClassTransformer transformer : wrappedTransformers.original) {
				    if(transformer == this) {
				        System.out.println("oops,");
				    }
				    
				    if(badTransformers.contains(transformer.getClass().getName())) {
				        wrappedTransformers.alt = null; // Hide from the view of conflicting transformers
				    }
				    
				    superDebug(String.format("Before transformer: %s (%s)", transformer.getClass().getName(), describeBytecode(basicClass)));
	                basicClass = transformer.transform(name, transformedName, basicClass);
	                superDebug(String.format("After transformer: %s (%s)", transformer.getClass().getName(), describeBytecode(basicClass)));
	                
	                if(wrappedTransformers.alt == null) {
	                    wrappedTransformers.alt = this; // reappear
	                }
	            }
			    if(basicClass != null) {
			        cache.put(transformedName, Optional.of(basicClass)); // then cache it
			        dirtyClasses.add(transformedName);
			    }
				result = basicClass;
			}
			if(result != null) {
				recentCache.put(transformedName, result);
			}
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
            
            WrappedAddListenableMap<String, Class<?>> wrappedCachedClasses = new WrappedAddListenableMap<String, Class<?>>(cachedClasses);
            cachedClassesField.set(lcl, wrappedCachedClasses);
            
            
            cacheTransformer = new CacheTransformer(transformers, listenableTransformers, wrappedCachedClasses);
            
            listenableTransformers.addListener(cacheTransformer);
            
            
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
		
	}

	@Override
	public void beforeIterator() {
		
	}
	
	@Override
	public boolean onPut(Map<String, Class<?>> delegateMap, String key, Class<?> value) {
		/* Explanation: When we're loading cached classes, mixin gives an error because it thinks it's going to
        have to run on already transformed classes. This doesn't actually happen, since CacheTransformer
        steals the transformation right from all other classes, including Mixin. But we need to bypass
        this error somehow, since it results in a crash. This is how we do it. (see MixinInfo.readTargets
        to see why it works)*/ 
     if(cache.keySet().contains(key)) {
         return false;
     } else {
         // For some reason mixin gives a different error if we always refuse to put, so we should only
         // do it when necessary.
         return true;
     }
	}

}
