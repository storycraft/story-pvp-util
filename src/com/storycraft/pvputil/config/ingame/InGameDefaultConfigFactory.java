package com.storycraft.pvputil.config.ingame;

import com.storycraft.pvputil.PvpUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Set;

public class InGameDefaultConfigFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraftInstance) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return InGameDefaultConfigGui.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent e) {
        if (!PvpUtil.getModMetadata().modId.equalsIgnoreCase(e.modID) || e.getResult() == Event.Result.DENY || e.isCanceled())
            return;

        PvpUtil.getDefaultGlobalConfig().updateFromForgeConfig();
        PvpUtil.getInstance().getConfigManager().saveAll();
    }
}
