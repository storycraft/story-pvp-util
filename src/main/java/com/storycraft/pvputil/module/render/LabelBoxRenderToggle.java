package com.storycraft.pvputil.module.render;

import com.storycraft.pvputil.PvpUtil;
import com.storycraft.pvputil.config.json.JsonConfigEntry;
import com.storycraft.pvputil.module.IModule;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

public class LabelBoxRenderToggle implements IModule {

    public static final String OPTION_CATEGORY = "render";

    private PvpUtil mod;

    private Minecraft minecraft;

    @Override
    public void preInitialize() {
        minecraft = Minecraft.getMinecraft();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void initialize(PvpUtil mod) {
        this.mod = mod;

        isModEnabled();
    }

    public boolean isModEnabled() {
        if (!getModuleConfigEntry().contains("render_box_on_label"))
            getModuleConfigEntry().set("render_box_on_label", true);

        return getModuleConfigEntry().get("render_box_on_label").getAsBoolean();
    }

    public JsonConfigEntry getModuleConfigEntry(){
        if (!mod.getDefaultConfig().contains(OPTION_CATEGORY)) {
            mod.getDefaultConfig().set(OPTION_CATEGORY, new JsonConfigEntry());
        }

        return mod.getDefaultConfig().getObject(OPTION_CATEGORY);
    }
}

