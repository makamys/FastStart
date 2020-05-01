/**
 * Copyright (C) 2020 makamys
 *
 * This file is part of FastStart.
 *
 * FastStart is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FastStart is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FastStart.  If not, see <https://www.gnu.org/licenses/>.
 */

package makamys.faststart;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import makamys.faststart.ThreadedTextureLoader.Failable;
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
                
                IResource res;
                if(job.resource.isPresent()) {
                	res = job.resource.get();
                } else {
                	ResourceLocation resLoc = job.resourceLocation.get();
                	// reusing res may be a bad idea since the stream gets stale
                	boolean reuseRes = false;
                	if(reuseRes && parent.resMap.containsKey(resLoc)) {
                		Failable<IResource, IOException> resMaybe = parent.resMap.get(resLoc);
                		if(resMaybe.present()) {
                			res = resMaybe.get();
                		} else {
                			continue;
                		}
                	} else {
            			try {
            				res = Minecraft.getMinecraft().getResourceManager().getResource(resLoc);
            				parent.resMap.put(resLoc, Failable.of(res));
            			} catch (IOException e) {
            				say("hmm, couldn't load " + resLoc);
            				parent.resMap.put(resLoc, Failable.failed(e));
            				res = null;
            			}
            			notifyIfWaitingOn(resLoc);
                	}
                }
                
                if(res != null) {
                	if(!parent.map.containsKey(res)) {
	                
	                    BufferedImage img;
	                    try {
	                        img = ImageIO.read(res.getInputStream());
	                        parent.map.put(res, Failable.of(img));
	                    } catch (IOException e) {
	                        img = null;
	                        say("hmm, couldn't load image " + res);
	                        parent.map.put(res, Failable.failed(e));
	                    }
	                } else {
	                    //say("meh, I already loaded " + res);
	                }
                	notifyIfWaitingOn(res);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void notifyIfWaitingOn(Object o) {
    	synchronized(parent.waitingOn) {
    		if(!parent.waitingOn.isEmpty()) {
            	ResourceLoadJob job = parent.waitingOn.get(0);
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
