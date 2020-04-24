package makamys.faststart;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;

import makamys.faststart.mixin.ITextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class ThreadedTextureLoader {
	
	List<TextureLoaderThread> threads = new ArrayList<>();
    protected LinkedBlockingQueue<IResource> queue = new LinkedBlockingQueue<>();
    protected ConcurrentHashMap<IResource, BufferedImage> map = new ConcurrentHashMap<>();
    
    protected IResource lastStreamedResource;
    public static Optional<IResource> waitingOn = Optional.empty();
    
    public ThreadedTextureLoader(int numThreads) {
    	initThreads(numThreads);
    }
    
    private void initThreads(int numThreads) {
        for(int i = 0; i < numThreads; i++) {
            threads.add(new TextureLoaderThread(this, i));
        }
        
        for(TextureLoaderThread t: threads) t.start();
    }
	
    public void setLastStreamedResource(IResource res) {
        lastStreamedResource = res;
    }
    
    public void addSpriteLoadJobs(IResourceManager resman, Map mapRegisteredSprites, ITextureMap itx) {
        Iterator<Entry> iterator = itx.mapRegisteredSprites().entrySet().iterator();
        
        while(iterator.hasNext()) {
            try {
                Entry entry = iterator.next();
                
                ResourceLocation resLoc = new ResourceLocation((String)entry.getKey());
                resLoc = itx.callCompleteResourceLocation(resLoc, 0);
                
                IResource iresource = resman.getResource(resLoc);
                
                if(!map.containsKey(iresource)) {
                    addResourceLoadJob(iresource);
                }
            } catch(Exception e) {}
        }
    }
    
    private void addResourceLoadJob(IResource res) {
        queue.add(res);
    }
    
    public BufferedImage fetchLastStreamedResource() {
        while(!map.containsKey(lastStreamedResource)) {
            //System.out.println(lastStreamedResource + " hasn't been loaded yet, waiting...");
            waitingOn = Optional.of(lastStreamedResource);
            
            
            
            synchronized(waitingOn) {
                queue.add(lastStreamedResource);
                try {
                    waitingOn.wait();
                } catch (InterruptedException e) {
                    
                }
            }
            //System.out.println("Woke up on " + lastStreamedResource);
        }
        waitingOn = Optional.empty();
        //System.out.println("Returning " + lastStreamedResource + " fetched by thread");
        return map.get(lastStreamedResource);
    }
}
