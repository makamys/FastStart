package makamys.faststart.mixin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import makamys.faststart.FastStart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FolderResourcePack;

@Mixin(FolderResourcePack.class)
public abstract class MixinFolderResourcePack {
	
	HashSet<String> filePaths = new HashSet<String>();
	
	@Inject(method = "<init>*", at = @At("RETURN"))
    private void afterConstructor(File folder, CallbackInfo ci) {
		explore(folder, folder.getPath());
    }
	
	private void explore(File folder, String path) {
		for(File f: folder.listFiles()) {
			String myPath = (path.isEmpty() ? "" : path + "/") + f.getName();
			filePaths.add(myPath);
			if(f.isDirectory()) {
				explore(f, myPath);
			}
		}
	}
    
    @Redirect(method = "hasResourceName(Ljava/lang/String;)Z", 
            at = @At(value = "INVOKE", target = "Ljava/io/File;isFile()Z"))
    public boolean redirectIsFile(File file) {
        //System.out.println("Running isFile redirector (FolderResourcePack). file=" + file);
    	//try {
			return filePaths.contains(file.getPath());
		/*} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return file.isFile(); // oops, fall back on isFile()
		}*/
        
    }
	
}
