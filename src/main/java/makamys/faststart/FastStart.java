package makamys.faststart;

public class FastStart {
    
    public static FastStart instance = new FastStart();
    
    private ThreadedTextureLoader textureLoader;
    
    private CacheTransformer cacheTransformer;
    
    
    
    public void init(){
    	textureLoader = new ThreadedTextureLoader(4);
        
        cacheTransformer = CacheTransformer.register();
        //CacheManager.loadLCLCache();
        //new CacheManager.SaveThread().start();
    }
    
    public ThreadedTextureLoader getTextureLoader() {
    	return textureLoader;
    }
}
