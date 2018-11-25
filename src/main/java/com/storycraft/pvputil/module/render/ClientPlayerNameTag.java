package com.storycraft.pvputil.module.render;

import com.storycraft.pvputil.PvpUtil;
import com.storycraft.pvputil.config.json.JsonConfigEntry;
import com.storycraft.pvputil.module.IModule;
import com.storycraft.pvputil.util.reflect.Reflect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Timer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class ClientPlayerNameTag implements IModule {

    public static final String OPTION_CATEGORY = "render";

    private static Reflect.WrappedMethod<Void, RenderPlayer> renderEntityName;

    static {
        renderEntityName = Reflect.getMethod(RenderPlayer.class, "renderEntityName", "func_188296_a", AbstractClientPlayer.class, double.class, double.class, double.class, String.class, double.class);
    }

    private PvpUtil mod;
    private Minecraft minecraft;

    private boolean modEnabled;

    @Override
    public void preInitialize() {
        minecraft = Minecraft.getMinecraft();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void initialize(PvpUtil mod) {
        this.mod = mod;

        this.modEnabled = isModEnabled();
    }

    @SubscribeEvent
    public void onConfigUpdate(ConfigChangedEvent.OnConfigChangedEvent e) {
        this.modEnabled = isModEnabled();
    }

    @SubscribeEvent
    public void onPostRender(RenderPlayerEvent.Post e){
        if (e.getEntityPlayer().isUser() && modEnabled && minecraft.gameSettings.thirdPersonView != 0) {
            EntityPlayer entity = e.getEntityPlayer();

            double d0 = entity.getDistanceSq(e.getRenderer().getRenderManager().renderViewEntity);
            float f = entity.isSneaking() ? RenderPlayer.NAME_TAG_RANGE_SNEAK : RenderPlayer.NAME_TAG_RANGE;

            if (d0 < (double)(f * f))
            {
                String s = entity.getDisplayName().getFormattedText();
                GlStateManager.alphaFunc(516, 0.1F);
                renderEntityName.invoke(e.getRenderer(), ((EntityPlayerSP) entity), e.getX(), e.getY(), e.getZ(), s, d0);
            }
        }
    }

    public boolean isModEnabled() {
        if (!getModuleConfigEntry().contains("nametag_on_client_player"))
            getModuleConfigEntry().set("nametag_on_client_player", false);

        if (getModuleConfigEntry().get("nametag_on_client_player").getAsBoolean()) {
            return true;
        }

        return false;
    }

    public JsonConfigEntry getModuleConfigEntry(){
        if (!mod.getDefaultConfig().contains(OPTION_CATEGORY)) {
            mod.getDefaultConfig().set(OPTION_CATEGORY, new JsonConfigEntry());
        }

        return mod.getDefaultConfig().getObject(OPTION_CATEGORY);
    }
}
