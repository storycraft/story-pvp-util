package com.storycraft.pvputil.module.session;

import com.storycraft.pvputil.PvpUtil;
import com.storycraft.pvputil.module.IModule;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;


public class SessionRefresh implements IModule {

    private Minecraft minecraft;
    private PvpUtil mod;

    @Override
    public void preInitialize() {
        this.minecraft = Minecraft.getMinecraft();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void initialize(PvpUtil mod) {
        this.mod = mod;
    }

    @SubscribeEvent
    public void onDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {

    }
}

