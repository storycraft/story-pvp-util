package com.storycraft.devtools.module.render;

import com.storycraft.devtools.DevTools;
import com.storycraft.devtools.config.json.JsonConfigEntry;
import com.storycraft.devtools.module.IModule;
import com.storycraft.devtools.module.render.renderer.DynamicRenderManager;
import com.storycraft.devtools.util.reflect.Reflect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class DynamicBoundingBox implements IModule {

    public static final String OPTION_CATEGORY = "render";

    private static Reflect.WrappedField<RenderManager, Minecraft> renderManagerField;
    private static Reflect.WrappedField<RenderManager, RenderGlobal> renderManagerGlobalField;

    static {
        renderManagerField = Reflect.getField(Minecraft.class, "renderManager");
        renderManagerGlobalField = Reflect.getField(RenderGlobal.class, "renderManager");
    }

    private DevTools mod;

    private Minecraft minecraft;
    private DynamicRenderManager dynamicRenderManager;

    @Override
    public void preInitialize() {
        minecraft = Minecraft.getMinecraft();
    }

    @Override
    public void initialize(DevTools mod) {
        this.mod = mod;

        isAimHighlightEnabled();
        getBoundingBoxDistance();
        isHideRequired();
        isHideNearOrFar();
        isEyeSightDrawingEnabled();
        isEyePosDrawingEnabled();
        isProjectileBoundingBoxEnabled();
        isNonLivingBoundingBoxEnabled();
    }

    @Override
    public void postInitialize() {
        dynamicRenderManager = new DynamicRenderManager(minecraft.getTextureManager(), minecraft.getRenderItem(), this);
        renderManagerField.set(minecraft, dynamicRenderManager);
        renderManagerGlobalField.set(minecraft.renderGlobal, dynamicRenderManager);
    }

    public boolean isAimHighlightEnabled() {
        if (!getModuleConfigEntry().contains("render_boundingbox_red_when_aim"))
            getModuleConfigEntry().set("render_boundingbox_red_when_aim", false);

        return getModuleConfigEntry().get("render_boundingbox_red_when_aim").getAsBoolean();
    }

    public boolean isHideRequired() {
        if (!getModuleConfigEntry().contains("boundingbox_hide_by_distance"))
            getModuleConfigEntry().set("boundingbox_hide_by_distance", false);

        return getModuleConfigEntry().get("boundingbox_hide_by_distance").getAsBoolean();
    }

    public boolean isHideNearOrFar() {
        if (!getModuleConfigEntry().contains("boundingbox_hide_near_or_far"))
            getModuleConfigEntry().set("boundingbox_hide_near_or_far", false);

        return getModuleConfigEntry().get("boundingbox_hide_near_or_far").getAsBoolean();
    }

    public int getBoundingBoxDistance() {
        if (!getModuleConfigEntry().contains("boundingbox_render_distance"))
            getModuleConfigEntry().set("boundingbox_render_distance", 12);

        return getModuleConfigEntry().get("boundingbox_render_distance").getAsInt();
    }

    public boolean isEyeSightDrawingEnabled() {
        if (!getModuleConfigEntry().contains("boundingbox_render_eyesight"))
            getModuleConfigEntry().set("boundingbox_render_eyesight", true);

        return getModuleConfigEntry().get("boundingbox_render_eyesight").getAsBoolean();
    }

    public boolean isEyePosDrawingEnabled() {
        if (!getModuleConfigEntry().contains("boundingbox_render_eyePos"))
            getModuleConfigEntry().set("boundingbox_render_eyePos", true);

        return getModuleConfigEntry().get("boundingbox_render_eyePos").getAsBoolean();
    }

    public boolean isProjectileBoundingBoxEnabled() {
        if (!getModuleConfigEntry().contains("boundingbox_projectile"))
            getModuleConfigEntry().set("boundingbox_projectile", true);

        return getModuleConfigEntry().get("boundingbox_projectile").getAsBoolean();
    }

    public boolean isNonLivingBoundingBoxEnabled() {
        if (!getModuleConfigEntry().contains("boundingbox_not_living"))
            getModuleConfigEntry().set("boundingbox_not_living", true);

        return getModuleConfigEntry().get("boundingbox_not_living").getAsBoolean();
    }

    public JsonConfigEntry getModuleConfigEntry(){
        if (!mod.getDefaultConfig().contains(OPTION_CATEGORY)) {
            mod.getDefaultConfig().set(OPTION_CATEGORY, new JsonConfigEntry());
        }

        return mod.getDefaultConfig().getObject(OPTION_CATEGORY);
    }
}