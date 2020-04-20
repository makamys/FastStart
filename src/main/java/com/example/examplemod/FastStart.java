package com.example.examplemod;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;

import com.example.examplemod.mixin.ITextureMap;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class FastStart {
    
    public static FastStart instance = new FastStart();
    
    List<ImageLoaderThread> threads = new ArrayList<>();
    protected LinkedBlockingQueue<IResource> queue = new LinkedBlockingQueue<>();
    protected ConcurrentHashMap<IResource, BufferedImage> map = new ConcurrentHashMap<>();
    
    protected IResource lastStreamedResource;
    public static Optional<IResource> waitingOn = Optional.empty();
    
    static class ImageLoaderThread extends Thread {
        
        FastStart parent;
        
        public ImageLoaderThread(FastStart parent) {
            this.parent = parent;
        }
        
        @Override
        public void run() {
            while(true) {
                try {
                    if(parent.queue.isEmpty()) {
                        say("No more images to load left.");
                    }
                    IResource res = parent.queue.take();
                    say("Found a res: " + res);
                    
                    //Thread.sleep(10000);
                    
                    if(!parent.map.containsKey(res)) {
                    
                        BufferedImage img;
                        try {
                            img = ImageIO.read(res.getInputStream());
                        } catch (IOException e) {
                            img = null;
                            e.printStackTrace();
                        }
                        
                        parent.map.put(res, img);
                        
                        
                            if(parent.waitingOn.isPresent() && parent.waitingOn.get().equals(res)) {
                                synchronized(parent.waitingOn) {
                                parent.waitingOn.notify();
                            }
                        }
                    } else {
                        //System.out.println("meh, I already loaded " + res);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        
        void say(Object o) {
            //System.out.println("<" + Thread.currentThread().getName() + "> " + o);
        }
    }
    
    public void init(){
        int numThreads = 4;
        
        for(int i = 0; i < numThreads; i++) {
            threads.add(new ImageLoaderThread(this));
        }
        
        for(ImageLoaderThread t: threads) t.start();
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
