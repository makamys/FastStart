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

import net.minecraftforge.client.event.TextureStitchEvent;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;

@Mod(modid = FastStartMod.MODID, version = FastStartMod.VERSION)
public class FastStartMod
{
	public static FastStartMod INSTANCE;
	
    public static final String MODID = "faststart";
    public static final String VERSION = "0.0";
    public static final String MCVERSION = "1.7.10";
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        FMLCommonHandler.instance().bus().register(this);
        
        FastStartMod.INSTANCE = this;
    }

    
    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {

    }
    
    @SubscribeEvent
    public void onTextureStitchPre(TextureStitchEvent.Pre pre) {
    	FastStart.instance.getTextureLoader().onTextureStitchPre(pre.map);
    }
    
    @SubscribeEvent
    public void onTextureStitchPost(TextureStitchEvent.Post post) {
    	FastStart.instance.getTextureLoader().onTextureStitchPost(post.map);
    }
}
