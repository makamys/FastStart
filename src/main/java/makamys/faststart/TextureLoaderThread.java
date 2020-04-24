package makamys.faststart;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.minecraft.client.resources.IResource;

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
