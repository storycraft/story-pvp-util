package com.storycraft.devtools.module.session;

import com.storycraft.devtools.DevTools;
import com.storycraft.devtools.module.IModule;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;


public class SessionRefresh implements IModule {

    private Minecraft minecraft;
    private DevTools mod;

    @Override
    public void preInitialize() {
        this.minecraft = Minecraft.getMinecraft();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void initialize(DevTools mod) {
        this.mod = mod;
    }

    @SubscribeEvent
    public void onDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {

    }
}

