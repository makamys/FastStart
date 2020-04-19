package com.example.examplemod.mixin;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.example.examplemod.ExampleMod;

import net.minecraft.client.renderer.texture.TextureMap;

@Mixin(TextureMap.class)
public abstract class MixinTextureMap {
    
	@Redirect(method = "loadTextureAtlas(Lnet/minecraft/client/resources/IResourceManager;)V", 
            at = @At(value = "INVOKE", target = "Ljavax/imageio/ImageIO;(Ljava/io/File;)Ljava/awt/image/BufferedImage;"))
    public BufferedImage redirectImageIORead(File file) throws IOException {
	    System.out.println("Running ImageIO.read redirector. file=" + file);
        return ImageIO.read(file);
    }
	
	@Redirect(method = "loadTextureAtlas(Lnet/minecraft/client/resources/IResourceManager;)V", 
            at = @At(value = "INVOKE", target = "Ljavax/imageio/ImageIO;(Ljavax/imageio/stream/ImageInputStream;)Ljava/awt/image/BufferedImage;"))
    public BufferedImage redirectImageIORead(ImageInputStream iis) throws IOException {
        System.out.println("Running ImageIO.read redirector. iis=" + iis);
        return ImageIO.read(iis);
    }
	
	@Redirect(method = "loadTextureAtlas(Lnet/minecraft/client/resources/IResourceManager;)V", 
            at = @At(value = "INVOKE", target = "Ljavax/imageio/ImageIO;(Ljava/io/InputStream;)Ljava/awt/image/BufferedImage;"))
    public BufferedImage redirectImageIORead(InputStream is) throws IOException {
        System.out.println("Running ImageIO.read redirector. is=" + is);
        return ImageIO.read(is);
    }
	
	@Redirect(method = "loadTextureAtlas(Lnet/minecraft/client/resources/IResourceManager;)V", 
            at = @At(value = "INVOKE", target = "Ljavax/imageio/ImageIO;(Ljava/net/URL;)Ljava/awt/image/BufferedImage;"))
    public BufferedImage redirectImageIORead(URL url) throws IOException {
        System.out.println("Running ImageIO.read redirector. url=" + url);
        return ImageIO.read(url);
    }
}
