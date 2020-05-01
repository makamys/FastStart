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

package makamys.faststart.mixin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.util.ResourceLocation;

@Mixin(DefaultResourcePack.class)
public class MixinDefaultResourcePack {
	//public InputStream func_152780_c(ResourceLocation p_152780_1_) throws IOException
    @Redirect(method = "func_152780_c(Lnet/minecraft/util/ResourceLocation;)Ljava/io/InputStream;", 
            at = @At(value = "INVOKE", target = "Ljava/io/File;isFile()Z"))
    public boolean redirectIsFile(File file) throws IOException {
        //System.out.println("Running isFile redirector (DefaultResourcePack). file=" + file);
        return file.isFile();
    }
	
}
