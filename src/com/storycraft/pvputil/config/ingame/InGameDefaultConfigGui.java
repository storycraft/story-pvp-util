package com.storycraft.pvputil.config.ingame;

import com.storycraft.pvputil.PvpUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;

public class InGameDefaultConfigGui extends GuiConfig {
    public InGameDefaultConfigGui(GuiScreen parentScreen) {
        super(parentScreen, PvpUtil.getDefaultGlobalConfig().getForgeConfigElement(), PvpUtil.getModMetadata().modId, false, false, "storycraft PvpUtil");
    }
}
