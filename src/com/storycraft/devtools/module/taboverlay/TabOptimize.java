package com.storycraft.devtools.module.taboverlay;

import com.storycraft.devtools.DevTools;
import com.storycraft.devtools.config.json.JsonConfigEntry;
import com.storycraft.devtools.module.IModule;
import com.storycraft.devtools.module.chat.ChatOptimize;
import com.storycraft.devtools.util.reflect.Reflect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.opengl.GL11;

public class TabOptimize implements IModule {

    public static final String OPTION_CATEGORY = "tab_overlay";

    private static final Reflect.WrappedField<GuiPlayerTabOverlay, GuiIngame> overlayPlayerList;

    static {
        overlayPlayerList = Reflect.getField(GuiIngame.class, "overlayPlayerList", "field_175196_v");
    }

    private DevTools mod;
    private Minecraft minecraft;

    private boolean isNumberMode;

    @Override
    public void preInitialize() {
        this.minecraft = Minecraft.getMinecraft();
    }

    @Override
    public void initialize(DevTools mod) {
        this.mod = mod;

        this.isNumberMode = isNumberMode();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void guiTabOverlay(FMLNetworkEvent.ClientConnectedToServerEvent e) {
        overlayPlayerList.set(minecraft.ingameGUI, new OptimizedTabOverlay(minecraft, minecraft.ingameGUI));
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent e){
        this.isNumberMode = isNumberMode();
    }

    public boolean isNumberMode() {
        if (!getModuleConfigEntry().contains("show_ping_as_number"))
            getModuleConfigEntry().set("show_ping_as_number", false);

        return getModuleConfigEntry().get("show_ping_as_number").getAsBoolean();
    }

    public JsonConfigEntry getModuleConfigEntry(){
        if (!mod.getDefaultConfig().contains(OPTION_CATEGORY)) {
            mod.getDefaultConfig().set(OPTION_CATEGORY, new JsonConfigEntry());
        }

        return mod.getDefaultConfig().getObject(OPTION_CATEGORY);
    }

    public class OptimizedTabOverlay extends GuiPlayerTabOverlay {
        public OptimizedTabOverlay(Minecraft mcIn, GuiIngame guiIngameIn) {
            super(mcIn, guiIngameIn);
        }

        protected void drawPing(int p_175245_1_, int p_175245_2_, int p_175245_3_, NetworkPlayerInfo networkPlayerInfoIn)
        {
            if (isNumberMode) {
                int ping = networkPlayerInfoIn.getResponseTime();
                int colour = ping > 500 ? 11141120 : (ping > 300 ? 11184640 : (ping > 200 ? 11193344 : (ping > 135 ? 2128640 : (ping > 70 ? 39168 : (ping > 0 ? 47872 : 11141120)))));
                if (ping > 0 && ping < 10000) {
                    GL11.glPushMatrix();
                    GL11.glScalef(0.5f, 0.5f, 0.5f);
                    int x = p_175245_2_ + p_175245_1_ - (minecraft.fontRendererObj.getStringWidth("" + ping + "") >> 1) - 2;
                    int y = p_175245_3_ + (minecraft.fontRendererObj.FONT_HEIGHT >> 2);
                    minecraft.fontRendererObj.drawString("" + ping + "", (float)(2 * x), (float)(2 * y), colour, true);
                    GL11.glScalef(2.0f, 2.0f, 2.0f);
                    GL11.glPopMatrix();
                }
            }
            else {
                super.drawPing(p_175245_1_, p_175245_2_, p_175245_3_, networkPlayerInfoIn);
            }
        }
    }
}
