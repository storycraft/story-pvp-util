package com.storycraft.devtools.module.render;

import com.storycraft.devtools.DevTools;
import com.storycraft.devtools.config.json.JsonConfigEntry;
import com.storycraft.devtools.module.IModule;
import com.storycraft.devtools.util.reflect.Reflect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Timer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.opengl.GL11;

public class ClientPlayerNameTag implements IModule {

    public static final String OPTION_CATEGORY = "render";

    private static Reflect.WrappedMethod<Void, RenderPlayer> renderOffsetLivingLabel;

    private static Reflect.WrappedField<Timer, Minecraft> timer;

    private static Reflect.WrappedField<Double, RenderManager> renderPosX;
    private static Reflect.WrappedField<Double, RenderManager> renderPosY;
    private static Reflect.WrappedField<Double, RenderManager> renderPosZ;

    static {
        renderOffsetLivingLabel = Reflect.getMethod(RenderPlayer.class, new String[]{"renderOffsetLivingLabel", "func_177069_a"}, AbstractClientPlayer.class, double.class, double.class, double.class, String.class, float.class, double.class);
        timer = Reflect.getField(Minecraft.class, "timer", "field_71428_T");

        renderPosX = Reflect.getField(RenderManager.class, "renderPosX", "field_78725_b");
        renderPosY = Reflect.getField(RenderManager.class, "renderPosY", "field_78726_c");
        renderPosZ = Reflect.getField(RenderManager.class, "renderPosZ", "field_78723_d");
    }

    private DevTools mod;
    private Minecraft minecraft;

    private boolean renderTag;

    @Override
    public void preInitialize() {
        minecraft = Minecraft.getMinecraft();
        renderTag = false;

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void initialize(DevTools mod) {
        this.mod = mod;

        isModEnabled();
    }

    @SubscribeEvent
    public void onPostRender(RenderPlayerEvent.Post e){
        if (isModEnabled() && needRenderTag() && e.entityPlayer.isUser()) {
            EntityPlayer entity = e.entityPlayer;

            double d0 = e.entityPlayer.lastTickPosX + (e.entityPlayer.posX - e.entityPlayer.lastTickPosX) * e.partialRenderTick;
            double d1 = e.entityPlayer.lastTickPosY + (e.entityPlayer.posY - e.entityPlayer.lastTickPosY) * e.partialRenderTick;
            double d2 = e.entityPlayer.lastTickPosZ + (e.entityPlayer.posZ - e.entityPlayer.lastTickPosZ) * e.partialRenderTick;

            double x = d0 - renderPosX.get(e.renderer.getRenderManager());
            double y = d1 - renderPosY.get(e.renderer.getRenderManager());
            double z = d2 - renderPosZ.get(e.renderer.getRenderManager());

            String s = entity.getDisplayName().getFormattedText();
            GlStateManager.alphaFunc(516, 0.1F);

            if (entity.isSneaking()) {
                FontRenderer fontrenderer = e.renderer.getFontRendererFromRenderManager();
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y + entity.height + 0.5F - (entity.isChild() ? entity.height / 2.0F : 0.0F), z);
                GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(-e.renderer.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(e.renderer.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
                GlStateManager.scale(-0.02666667F, -0.02666667F, 0.02666667F);
                GlStateManager.translate(0.0F, 9.374999F, 0.0F);
                GlStateManager.disableLighting();
                GlStateManager.depthMask(false);
                GlStateManager.enableBlend();
                GlStateManager.disableTexture2D();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                int i = fontrenderer.getStringWidth(s) / 2;
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                worldrenderer.pos((double) (-i - 1), -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                worldrenderer.pos((double) (-i - 1), 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                worldrenderer.pos((double) (i + 1), 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                worldrenderer.pos((double) (i + 1), -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                tessellator.draw();
                GlStateManager.enableTexture2D();
                GlStateManager.depthMask(true);
                fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, 0, 553648127);
                GlStateManager.enableLighting();
                GlStateManager.disableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.popMatrix();
            } else {
                renderOffsetLivingLabel.invoke(e.renderer, entity, x, y - (entity.isChild() ? (double) (entity.height / 2.0F) : 0.0D), z, s, 0.02666667F, 0);
            }
        }
    }

    public boolean needRenderTag() {
        return renderTag;
    }

    public boolean isModEnabled() {
        if (!getModuleConfigEntry().contains("nametag_on_client_player"))
            getModuleConfigEntry().set("nametag_on_client_player", false);

        if (getModuleConfigEntry().get("nametag_on_client_player").getAsBoolean()) {
            if (minecraft.gameSettings.thirdPersonView != 0) {
                renderTag = true;
            } else {
                renderTag = false;
            }

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
