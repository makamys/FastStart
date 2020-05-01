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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import makamys.faststart.mixin.ITextureMap;
import makamys.faststart.mixin.MixinDefaultResourcePack;
import makamys.faststart.mixin.MixinFolderResourcePack;
import makamys.faststart.mixin.MixinTextureMap;

public class MixinConfigPlugin implements IMixinConfigPlugin {
	
	@Override
	public void onLoad(String mixinPackage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getRefMapperConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		Config.loadIfNotLoadedAlready();
		
		if(Arrays.asList(
				"makamys.faststart.mixin.ITextureMap",
				"makamys.faststart.mixin.MixinTextureMap"
				).contains(mixinClassName)){
			return Config.useThreadedTextureLoader;
		} else if(Arrays.asList(
				"makamys.faststart.mixin.MixinFolderResourcePack",
				"makamys.faststart.mixin.MixinDefaultResourcePack"
				).contains(mixinClassName)){
			return Config.useFolderTexturePackOptimization;
		} else {
			return true;
		}
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getMixins() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		// TODO Auto-generated method stub
		
	}

}
