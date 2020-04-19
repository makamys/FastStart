package com.example.examplemod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public class FastStartPlugin implements IFMLLoadingPlugin {
    
    public FastStartPlugin(){
        FastStart.instance.init();
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
