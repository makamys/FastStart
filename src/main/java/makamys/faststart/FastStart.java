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

import java.io.File;
import java.io.IOException;

import net.minecraft.launchwrapper.Launch;

public class FastStart {
    
    public static FastStart instance = new FastStart();
    
    private ThreadedTextureLoader textureLoader;
    
    private CacheTransformer cacheTransformer;
    
    
    
    public void init(){
    	Config.loadIfNotLoadedAlready();
    	Persistence.loadIfNotLoadedAlready();
    	
    	if(Config.useThreadedTextureLoader) {
    			textureLoader = new ThreadedTextureLoader(
					Config.textureLoaderThreadCount != 0 ? Config.textureLoaderThreadCount
							: Runtime.getRuntime().availableProcessors());
    	}
        
    	if(Config.useCacheTransformer) {
    		cacheTransformer = CacheTransformer.register();
    	}
    	
    	Persistence.lastVersion = FastStartMod.VERSION;
    	Persistence.save();
    }
    
    public static File getDataFile(String name) {
    	return getDataFile(name, true);
    }
    
    public static File getDataFile(String name, boolean createIfNotExists) {
    	File myDir = new File(Launch.minecraftHome, "faststart");
    	if(!myDir.exists()) {
    		myDir.mkdir();
    	}
    	File dataFile = new File(myDir, name);
    	
    	if(createIfNotExists) {
	    	try {
				dataFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	return dataFile;
    }
    
    public ThreadedTextureLoader getTextureLoader() {
    	return textureLoader;
    }
}
