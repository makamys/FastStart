package makamys.faststart;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import makamys.faststart.ThreadedTextureLoader.ResourceLoadJob;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

class TextureLoaderThread extends Thread {
    
	ThreadedTextureLoader parent;
    
    public TextureLoaderThread(ThreadedTextureLoader parent, int index) {
        this.parent = parent;
        setName("Texture loader thread #" + String.valueOf(index));
        setDaemon(false);
    }
    
    @Override
    public void run() {
        while(true) {
            try {
                if(parent.queue.isEmpty()) {
                    say("No more images to load left.");
                }
                ResourceLoadJob job = parent.queue.take();
                
                IResource res = job.resource.orElse(null);
                if(res == null) {
                	ResourceLocation resLoc = job.resourceLocation.get();
                	if(parent.resMap.containsKey(resLoc)) {
                		res = parent.resMap.get(resLoc);
                	} else {
            			try {
            				res = Minecraft.getMinecraft().getResourceManager().getResource(resLoc);
            				parent.resMap.put(resLoc, res);
            				
            				notifyIfWaitingOn(resLoc);
            			} catch (IOException e) {
            				// TODO Auto-generated catch block
            				e.printStackTrace();
            			}
                	}
                }
                
                if(!parent.map.containsKey(res)) {
                
                    BufferedImage img;
                    try {
                        img = ImageIO.read(res.getInputStream());
                    } catch (IOException e) {
                        img = null;
                        e.printStackTrace();
                    }
                    
                    if(img == null) {
                    	System.out.println("wait what");
                    }
                    parent.map.put(res, img);
                    
                    
                	notifyIfWaitingOn(res);
                } else {
                    //System.out.println("meh, I already loaded " + res);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void notifyIfWaitingOn(Object o) {
    	if(parent.waitingOn.isPresent()) {
            synchronized(parent.waitingOn) {
            	ResourceLoadJob job = parent.waitingOn.get();
            	if(o.equals(job.resource.orElse(null)) || o.equals(job.resourceLocation.orElse(null))) {
            		parent.waitingOn.notify();
            	}
            }
        }
    }
    
    void say(Object o) {
        //System.out.println("<" + Thread.currentThread().getName() + "> " + o);
    }
}
