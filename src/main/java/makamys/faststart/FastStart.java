package makamys.faststart;

import java.io.File;
import java.io.IOException;

import net.minecraft.launchwrapper.Launch;

public class FastStart {
    
    public static FastStart instance = new FastStart();
    
    private ThreadedTextureLoader textureLoader;
    
    private CacheTransformer cacheTransformer;
    
    
    
    public void init(){
    	Config.loadIfNotLoadedAlready();
    	
    	textureLoader = new ThreadedTextureLoader(
    			Config.textureLoaderThreadCount != 0 ? Config.textureLoaderThreadCount
    					: Runtime.getRuntime().availableProcessors());
        
    	if(Config.useCacheTransformer) {
    		cacheTransformer = CacheTransformer.register();
    	}
    }
    
    public static File getDataFile(String name) {
    	File myDir = new File(Launch.minecraftHome, "faststart");
    	if(!myDir.exists()) {
    		myDir.mkdir();
    	}
    	File dataFile = new File(myDir, name);
    	try {
			dataFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return dataFile;
    }
    
    public ThreadedTextureLoader getTextureLoader() {
    	return textureLoader;
    }
}
