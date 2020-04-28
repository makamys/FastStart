package makamys.faststart;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

import java.lang.reflect.Field;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import makamys.faststart.mixin.ITextureMap;

@Mod(modid = ExampleMod.MODID, version = ExampleMod.VERSION)
public class ExampleMod
{
	public static ExampleMod INSTANCE;
	
    public static final String MODID = "examplemod";
    public static final String VERSION = "1.0";
    public static final String MCVERSION = "1.7.10";
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        FMLCommonHandler.instance().bus().register(this);
        
        ExampleMod.INSTANCE = this;
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
