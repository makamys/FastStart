package makamys.faststart;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;

import makamys.faststart.mixin.ITextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.util.ResourceLocation;

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
