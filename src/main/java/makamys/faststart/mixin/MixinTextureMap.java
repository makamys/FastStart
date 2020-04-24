package makamys.faststart.mixin;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import makamys.faststart.ExampleMod;
import makamys.faststart.FastStart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;

@Mixin(TextureMap.class)
public abstract class MixinTextureMap {
    @Shadow
    Map mapRegisteredSprites;
    
	/*@Redirect(method = "loadTextureAtlas(Lnet/minecraft/client/resources/IResourceManager;)V", 
            at = @At(value = "INVOKE", target = "Ljavax/imageio/ImageIO;(Ljava/io/File;)Ljava/awt/image/BufferedImage;"))
    public BufferedImage redirectImageIORead(File file) throws IOException {
	    //System.out.println("Running ImageIO.read redirector. file=" + file);
        return ImageIO.read(file);
    }
	
	@Redirect(method = "loadTextureAtlas(Lnet/minecraft/client/resources/IResourceManager;)V", 
            at = @At(value = "INVOKE", target = "Ljavax/imageio/ImageIO;(Ljavax/imageio/stream/ImageInputStream;)Ljava/awt/image/BufferedImage;"))
    public BufferedImage redirectImageIORead(ImageInputStream iis) throws IOException {
        //System.out.println("Running ImageIO.read redirector. iis=" + iis);
        return ImageIO.read(iis);
    }*/
	
	@Redirect(method = "loadTextureAtlas(Lnet/minecraft/client/resources/IResourceManager;)V", 
            at = @At(value = "INVOKE", target = "Ljavax/imageio/ImageIO;read(Ljava/io/InputStream;)Ljava/awt/image/BufferedImage;"))
    public BufferedImage redirectImageIORead(InputStream is) throws IOException {
        //System.out.println("Running ImageIO.read redirector. is=" + is);
        BufferedImage result = FastStart.instance.getTextureLoader().fetchLastStreamedResource();
        //System.out.println("ImageIO.read redirector returning " + result);
        return result;
        //return ImageIO.read(is);
    }
	
	/*@Redirect(method = "loadTextureAtlas(Lnet/minecraft/client/resources/IResourceManager;)V", 
            at = @At(value = "INVOKE", target = "Ljavax/imageio/ImageIO;(Ljava/net/URL;)Ljava/awt/image/BufferedImage;"))
    public BufferedImage redirectImageIORead(URL url) throws IOException {
        //System.out.println("Running ImageIO.read redirector. url=" + url);
        return ImageIO.read(url);
    }*/
	
   @Redirect(method = "loadTextureAtlas(Lnet/minecraft/client/resources/IResourceManager;)V", 
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/IResource;getInputStream()Ljava/io/InputStream;"))
    public InputStream redirectGetInputStream(IResource res) throws IOException {
        FastStart.instance.getTextureLoader().setLastStreamedResource(res);
        return res.getInputStream();
    }
	
    /*@Inject(method = "loadTextureAtlas(Lnet/minecraft/client/resources/IResourceManager;)V", at = @At("HEAD"))
    private void onConstructed(IResourceManager resman, CallbackInfo ci) {
        if(!skipFirst()) {
            FastStart.instance.addSpriteLoadJobs(resman, mapRegisteredSprites, ((ITextureMap)this));
        }
    }*/
    
    @Inject(method = "registerIcons()V", at = @At("RETURN"))
    private void afterRegisterIcons(CallbackInfo ci) {
        if(!skipFirst()) {
            FastStart.instance.getTextureLoader().addSpriteLoadJobs(Minecraft.getMinecraft().getResourceManager(), mapRegisteredSprites, ((ITextureMap)this));
        }
    }
    
    // Forge adds this, I think
    private boolean skipFirst() {
        try {
            Field skipFirstField = TextureMap.class.getDeclaredField("skipFirst");
            
            skipFirstField.setAccessible(true);
            
            return (boolean)skipFirstField.get(this);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
}
