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
	public static int recentCacheSize;
	
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
				"org.spongepowered.asm.mixin.transformer.Proxy,appeng.transformer.asm.ApiRepairer",
				"Comma-separated list of transformers for which the view of the transformer chain should be restored.\n" + 
				"\n" + 
				"The caching class transformer replaces the transformer chain with just itself.\n" + 
				"This creates conflicts with certain other transformers which also access the transformer chain.\n" + 
				"To solve this, our transformer will restore the view of the transformer chain while these transformers are running.\n" + 
				"\n" + 
				"If you see another transformer's name in your crash log, adding it to this list may solve the problem.");
		badClasses = cfg.getString("badClasses", "cacheTransformer", "net.eq2online.macros.permissions.MacroModPermissions", 
				"Sometimes caching classes can cause problems. Classes in this list will not be cached.");
		recentCacheSize = cfg.getInt("recentCacheSize", "cacheTransformer", 512, -1, Integer.MAX_VALUE, 
				"Cached class bytecode is removed from memory after being used, but the most recent N are kept around\n" +
				"because the same class is often transformed more than once. This option sets the value of that N.\n" +
				"(Set to -1 to keep class bytecode in RAM forever)");
		
		if(cfg.hasChanged()) {
			cfg.save();
		}
	}
	
}
