package com.storycraft.pvputil.module.chat;

import com.storycraft.pvputil.PvpUtil;
import com.storycraft.pvputil.config.json.JsonConfigEntry;
import com.storycraft.pvputil.module.IModule;
import com.storycraft.pvputil.util.reflect.Reflect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.List;

public class ChatOptimize implements IModule {

    public static final String OPTION_CATEGORY = "chat";

    private static final Reflect.WrappedField<List<ChatLine>, GuiNewChat> field_146253_i;
    private static final Reflect.WrappedField<Integer, GuiNewChat> scrollPos;
    private static final Reflect.WrappedField<Boolean, GuiNewChat> isScrolled;

    private static final Reflect.WrappedField<GuiNewChat, GuiIngame> persistantChatGUI;
    private static final Reflect.WrappedField<String, GuiChat> defaultInputFieldText;

    static {
        field_146253_i = Reflect.getField(GuiNewChat.class, "field_146253_i", "field_146253_i");
        scrollPos = Reflect.getField(GuiNewChat.class, "scrollPos", "field_146250_j");
        isScrolled = Reflect.getField(GuiNewChat.class, "isScrolled", "field_146251_k");

        persistantChatGUI = Reflect.getField(GuiIngame.class, "persistantChatGUI", "field_73840_e");

        defaultInputFieldText = Reflect.getField(GuiChat.class, "defaultInputFieldText", "field_146409_v");
    }

    private PvpUtil mod;
    private Minecraft minecraft;

    private boolean isBackgroundEnabled;
    private boolean isShadowEnabled;
    private boolean isAlphaFakeDisabled;

    @Override
    public void preInitialize() {
        this.minecraft = Minecraft.getMinecraft();
    }

    @Override
    public void initialize(PvpUtil mod) {
        this.mod = mod;

        this.isBackgroundEnabled = isBackgroundEnabled();
        this.isShadowEnabled = isShadowEnabled();
        this.isAlphaFakeDisabled = isAlphaFakeDisabled();

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ChatRegister());
    }

    @SubscribeEvent
    public void onConfigUpdate(ConfigChangedEvent.PostConfigChangedEvent e){
        this.isBackgroundEnabled = isBackgroundEnabled();
        this.isShadowEnabled = isShadowEnabled();
        this.isAlphaFakeDisabled = isAlphaFakeDisabled();
    }


    @SubscribeEvent
    public void guiChat(GuiOpenEvent e) {
        if (e.gui instanceof GuiChat) {
            e.gui = new OptimizedChatGui(defaultInputFieldText.get((GuiChat) e.gui));
        }
    }

    public boolean isBackgroundEnabled() {
        if (!getModuleConfigEntry().contains("chat_background"))
            getModuleConfigEntry().set("chat_background", true);

        return getModuleConfigEntry().get("chat_background").getAsBoolean();
    }

    public boolean isShadowEnabled() {
        if (!getModuleConfigEntry().contains("draw_shadow_on_chat"))
            getModuleConfigEntry().set("draw_shadow_on_chat", true);

        return getModuleConfigEntry().get("draw_shadow_on_chat").getAsBoolean();
    }

    public boolean isAlphaFakeDisabled() {
        if (!getModuleConfigEntry().contains("follow_chat_opacity_settings"))
            getModuleConfigEntry().set("follow_chat_opacity_settings", false);

        return getModuleConfigEntry().get("follow_chat_opacity_settings").getAsBoolean();
    }

    public JsonConfigEntry getModuleConfigEntry(){
        if (!mod.getDefaultConfig().contains(OPTION_CATEGORY)) {
            mod.getDefaultConfig().set(OPTION_CATEGORY, new JsonConfigEntry());
        }

        return mod.getDefaultConfig().getObject(OPTION_CATEGORY);
    }

    public class ChatRegister {
        @SubscribeEvent
        public void guiChat(FMLNetworkEvent.ClientConnectedToServerEvent e) {
            persistantChatGUI.set(minecraft.ingameGUI, new OptimizedNewChatGui(minecraft));
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

    public class OptimizedChatGui extends GuiChat {

        public OptimizedChatGui(String defaultText){
            super(defaultText);
        }
    }

    public class OptimizedNewChatGui extends GuiNewChat {

        public OptimizedNewChatGui(Minecraft mcIn) {
            super(mcIn);
        }

        @Override
        public void drawChat(int p_146230_1_)
        {
            if (minecraft.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN)
            {
                int i = this.getLineCount();
                int scrollPos = ChatOptimize.scrollPos.get(this);
                boolean isScrolled = ChatOptimize.isScrolled.get(this);
                boolean flag = false;
                int j = 0;
                int k = field_146253_i.get(this).size();
                float f;
                if (isAlphaFakeDisabled){
                    f = minecraft.gameSettings.chatOpacity;
                }
                else {
                    f = minecraft.gameSettings.chatOpacity * 0.9F + 0.1F;
                }

                if (k > 0)
                {
                    if (this.getChatOpen())
                    {
                        flag = true;
                    }

                    float f1 = this.getChatScale();
                    int l = MathHelper.ceiling_float_int((float)this.getChatWidth() / f1);
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(2.0F, 20.0F, 0.0F);
                    GlStateManager.scale(f1, f1, 1.0F);

                    for (int i1 = 0; i1 + scrollPos < k && i1 < i; ++i1)
                    {
                        ChatLine chatline = field_146253_i.get(this).get(i1 + scrollPos);

                        if (chatline != null)
                        {
                            int j1 = p_146230_1_ - chatline.getUpdatedCounter();

                            if (j1 < 200 || flag)
                            {
                                double d0 = (double)j1 / 200.0D;
                                d0 = 1.0D - d0;
                                d0 = d0 * 10.0D;
                                d0 = MathHelper.clamp_double(d0, 0.0D, 1.0D);
                                d0 = d0 * d0;
                                int l1 = (int)(255.0D * d0);

                                if (flag)
                                {
                                    l1 = 255;
                                }

                                l1 = (int)((float)l1 * f);
                                ++j;

                                if (l1 > 3)
                                {
                                    int i2 = 0;
                                    int j2 = -i1 * 9;

                                    if (isBackgroundEnabled)
                                        drawRect(i2, j2 - 9, i2 + l + 4, j2, l1 / 2 << 24);
                                    String s = chatline.getChatComponent().getFormattedText();
                                    GlStateManager.enableBlend();
                                    minecraft.fontRendererObj.drawString(s, (float)i2, (float)(j2 - 8), 16777215 + (l1 << 24), isShadowEnabled);
                                    GlStateManager.disableAlpha();
                                    GlStateManager.disableBlend();
                                }
                            }
                        }
                    }

                    if (flag)
                    {
                        int k2 = minecraft.fontRendererObj.FONT_HEIGHT;
                        GlStateManager.translate(-3.0F, 0.0F, 0.0F);
                        int l2 = k * k2 + k;
                        int i3 = j * k2 + j;
                        int j3 = scrollPos * i3 / k;
                        int k1 = i3 * i3 / l2;

                        if (l2 != i3)
                        {
                            int k3 = j3 > 0 ? 170 : 96;
                            int l3 = isScrolled ? 13382451 : 3355562;

                            drawRect(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
                            drawRect(2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
                        }
                    }

                    GlStateManager.popMatrix();
                }
            }
        }
    }
}
