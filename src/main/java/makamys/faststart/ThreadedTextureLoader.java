package makamys.faststart;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import java.util.Objects;

import makamys.faststart.mixin.ITextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

public class ThreadedTextureLoader {
	
	static class ResourceLoadJob {
		Optional<IResource> resource = Optional.empty();
		Optional<ResourceLocation> resourceLocation = Optional.empty();
		
		public ResourceLoadJob(IResource res) {
			this.resource = Optional.of(res);
		}
		
		public ResourceLoadJob(ResourceLocation resLoc) {
			this.resourceLocation = Optional.of(resLoc);
		}
		
		public static ResourceLoadJob of(Object object){
			if(object instanceof IResource) {
				return new ResourceLoadJob((IResource)object);
			} else if(object instanceof ResourceLocation) {
				return new ResourceLoadJob((ResourceLocation)object);
			} else {
				return null; // uh oh
			}
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ResourceLoadJob) {
				ResourceLoadJob o = (ResourceLoadJob)obj;
				return Objects.equals(resource, o.resource) && Objects.equals(resourceLocation, o.resourceLocation);
			} else {
				return false;
			}
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(resource, resourceLocation);
		}
	}
	
	List<TextureLoaderThread> threads = new ArrayList<>();
    protected LinkedBlockingQueue<ResourceLoadJob> queue = new LinkedBlockingQueue<>();
    protected ConcurrentHashMap<ResourceLocation, IResource> resMap = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<IResource, BufferedImage> map = new ConcurrentHashMap<>();
    
    protected IResource lastStreamedResource;
    public static final List<ResourceLoadJob> waitingOn = new ArrayList<>(1);
    
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
    
    public void addSpriteLoadJobs(Map mapRegisteredSprites, ITextureMap itx) {
        Iterator<Entry> iterator = itx.mapRegisteredSprites().entrySet().iterator();
        
        while(iterator.hasNext()) {
            try {
                Entry entry = iterator.next();
                
                ResourceLocation resLoc = new ResourceLocation((String)entry.getKey());
                resLoc = itx.callCompleteResourceLocation(resLoc, 0);
                
                // if we reuse resources, this happens:
                // Unexpected end of ZLIB input stream
                resMap.clear();
                
                if(!map.containsKey(resLoc)) {
                	queue.add(new ResourceLoadJob(resLoc));
                }
            } catch(Exception e) {}
        }
    }
    
    public BufferedImage fetchLastStreamedResource() {
        return fetchFromMap(map, lastStreamedResource);
    }
    
    public IResource fetchResource(ResourceLocation loc) {
    	return fetchFromMap(resMap, loc);
    }
    
    public <K, V> V fetchFromMap(Map<K, V> map, K key){
    	while(true) {
    		synchronized(waitingOn) {
    			if(map.containsKey(key)) {
    				break;
    			} else {
    				//System.out.println(lastStreamedResource + " hasn't been loaded yet, waiting...");
    	            waitingOn.add(ResourceLoadJob.of(key));
    	            if(waitingOn.size() > 1) throw new IllegalStateException();
    	            
	                queue.add(ResourceLoadJob.of(key));
	                try {
	                    waitingOn.wait();
	                } catch (InterruptedException e) {
	                    
	                }
    	            //System.out.println("Woke up on " + lastStreamedResource);
    			}
    		}
            
        }
        waitingOn.clear();
        //System.out.println("Returning " + lastStreamedResource + " fetched by thread");
        return map.get(key);
    }
}
