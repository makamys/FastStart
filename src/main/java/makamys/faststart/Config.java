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

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Configuration;

public class Config {
	
	private static Configuration cfg;

	public static boolean useThreadedTextureLoader;
	public static boolean useCacheTransformer;
	public static boolean useFolderTexturePackOptimization;

	public static int textureLoaderThreadCount;

	public static String badTransformers;
	public static String badClasses;
	public static String modFilesToIgnore;
	public static int recentCacheSize;
	public static int verbosity;
	
	public static void loadIfNotLoadedAlready() {
		if(cfg != null) return;
		
		File cfgFile = new File(Launch.minecraftHome, "config/faststart.cfg");
		Configuration cfg = new Configuration(cfgFile);
		
		cfg.load();
		
		cfg.setCategoryComment("-features", "Enable or disable features");
		useThreadedTextureLoader = cfg.getBoolean("useThreadedTextureLoader", "-features", true, 
				"Use multi-threaded texture loading when stitching textures?");
		useCacheTransformer = cfg.getBoolean("useCacheTransformer", "-features", true, 
				"Use caching class transformer?");
		useFolderTexturePackOptimization = cfg.getBoolean("useFolderTexturePackOptimization", "-features", true, 
				"Use the optimization that speeds up loading folder resource packs?");
		
		cfg.setCategoryComment("threadedTextureLoader", 
				"Options for the threaded texture loader. (only appliable if it's enabled)");
		textureLoaderThreadCount = cfg.getInt("textureLoaderThreadCount", "threadedTextureLoader", 0, 0, Integer.MAX_VALUE,
				"How many threads to use for loading textures? (0: auto (all cores))");
		
		cfg.setCategoryComment("cacheTransformer", 
				"Options for the caching class transformer. (only appliable if it's enabled)");
		badTransformers = cfg.getString("badTransformers", "cacheTransformer",
				"org.spongepowered.asm.mixin.transformer.Proxy,appeng.transformer.asm.ApiRepairer,com.mumfrey.liteloader.transformers.ClassOverlayTransformer+",
				"Comma-separated list of transformers for which the view of the transformer chain should be restored.\n" + 
				"\n" + 
				"The caching class transformer replaces the transformer chain with just itself.\n" + 
				"This creates conflicts with certain other transformers which also access the transformer chain,\n" +
				"which can result in the game crashing.\n" +
				"To solve this, our transformer will restore the view of the transformer chain while these transformers are running.\n" + 
				"\n" + 
				"How to find bad transformers? If you see another transformer's name in your crash log,\n" +
				"or see its name in one of the iterator stack traces printed in debug mode,\n" +
				"adding it to this list may solve the problem.\n");
		badClasses = cfg.getString("badClasses", "cacheTransformer", "net.eq2online.macros.permissions.MacroModPermissions", 
				"Sometimes caching classes can cause problems. Classes in this list will not be cached.\n");
		modFilesToIgnore = cfg.getString("modFilesToIgnore", "cacheTransformer", "CMD files.jar", 
				"Comma-separated list of mod files to ignore modifications of when deciding if a cache rebuild\n" +
				"should be triggered.\n" +
				"If your cache keeps getting rebuilt even though you haven't changed any mods, look for deranged\n" +
				"mod files and add them to this list.");
		recentCacheSize = cfg.getInt("recentCacheSize", "cacheTransformer", 512, -1, Integer.MAX_VALUE, 
				"Cached class bytecode is removed from memory after being used, but the most recent N are kept around\n" +
				"because the same class is often transformed more than once. This option sets the value of that N.\n" +
				"(Set to -1 to keep class bytecode in RAM forever)");
		verbosity = cfg.getInt("verbosity", "cacheTransformer", 1, 0, 2,
				"0: Only print the essential messages.\n" +
				"1: Print when the cache gets saved.\n" +
				"2: Debug mode. Turn this on to log a bunch of stuff that can help find the cause of a crash.");
		
		if(cfg.hasChanged()) {
			cfg.save();
		}
	}
	
}
