package com.example.examplemod.mixin;

import java.io.File;
import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.resources.FolderResourcePack;

@Mixin(FolderResourcePack.class)
public abstract class MixinFolderResourcePack {
	
    /*@Inject(method = "hasResourceName(Ljava/lang/String;)B", at = @At("HEAD"))
    private void onHasResourceName(String str, CallbackInfo ci) {
        System.out.println("hasResourceName got called with " + str);
    }*/
    
    @Redirect(method = "hasResourceName(Ljava/lang/String;)Z", 
            at = @At(value = "INVOKE", target = "Ljava/io/File;isFile()Z"))
    public boolean redirectIsFile(File file) {
        System.out.println("Running isFile redirector (FolderResourcePack). file=" + file);
        return file.isFile();
    }
	
}
