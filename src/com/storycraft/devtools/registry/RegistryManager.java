package com.storycraft.devtools.registry;

import com.storycraft.devtools.IStoryMod;
import net.minecraftforge.client.event.sound.SoundEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.Map;

public class RegistryManager {

    public static final String NAMESPACE = "storycraft";

    private Map<String, SoundEvent> soundMap;

    private IStoryMod mod;

    public RegistryManager(IStoryMod mod){
        this.mod = mod;
    }

    public void preInitialize() {

    }

    public void initialize(){
        MinecraftForge.EVENT_BUS.register(this);
    }
}
