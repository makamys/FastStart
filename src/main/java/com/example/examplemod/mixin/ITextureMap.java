package com.example.examplemod.mixin;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorldAccess;

@Mixin(TextureMap.class)
public interface ITextureMap {
    @Accessor("mapRegisteredSprites")
    Map mapRegisteredSprites();
    
    @Accessor("skipFirst")
    boolean skipFirst();
    
    @Invoker
    public ResourceLocation callCompleteResourceLocation(ResourceLocation p_147634_1_, int p_147634_2_);
}