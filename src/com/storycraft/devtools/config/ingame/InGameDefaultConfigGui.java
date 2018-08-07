package com.storycraft.devtools.config.ingame;

import com.storycraft.devtools.DevTools;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;

public class InGameDefaultConfigGui extends GuiConfig {
    public InGameDefaultConfigGui(GuiScreen parentScreen) {
        super(parentScreen, DevTools.getDefaultGlobalConfig().getForgeConfigElement(), DevTools.getModMetadata().modId, false, false, "storycraft DevTools");
    }
}
