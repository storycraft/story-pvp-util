package com.storycraft.pvputil.module.overlay;

import java.util.ArrayList;
import java.util.List;

import com.storycraft.pvputil.PvpUtil;
import com.storycraft.pvputil.config.json.JsonConfigEntry;
import com.storycraft.pvputil.module.IModule;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;

public class ComboCounter implements IModule {

    public static final String OPTION_CATEGORY = "overlay";

    private static final int LEFT_MARGIN = 10;
    private static final int BOTTOM_MARGIN = 10;

    private static final float POPOUT_SCALE = 1.6f;
    private static final float POPOUT_SMALL_SCALE = 1.1f;

    private static final float POPOUT_DURATION = 150f;

    private PvpUtil mod;
    private Minecraft minecraft;

    private List<ResourceLocation> numberTextureList;
    private ResourceLocation numberXTexture;

    private volatile boolean enabled;
    private volatile boolean soundEnabled;

    private long lastComboChange;
    private int lastCombo;
    private int currentCombo;

    private ResourceLocation soundComboBreak;

    @Override
    public void preInitialize() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void initialize(PvpUtil mod) {
        this.mod = mod;
        this.minecraft = Minecraft.getMinecraft();
        this.enabled = isModEnabled();
        this.soundEnabled = isSoundEnabled();
        this.currentCombo = 0;

        this.soundComboBreak = new ResourceLocation(PvpUtil.getModMetadata().modId, "combo.break");

        initNumberTexture();
    }

    public int getCombo() {
        return currentCombo;
    }

    public int getLastCombo() {
        return lastCombo;
    }

    public long getLastComboChange() {
        return lastComboChange;
    }

    public void setCombo(int combo) {
        this.lastCombo = combo;
        this.currentCombo = combo;
        this.lastComboChange = System.currentTimeMillis();
    }

    protected void initNumberTexture() {
        Minecraft minecraft = Minecraft.getMinecraft();

        this.numberTextureList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            ResourceLocation resource = new ResourceLocation(PvpUtil.getModMetadata().modId, "overlay/score-" + i + ".png");
            numberTextureList.add(resource);

            minecraft.getTextureManager().loadTexture(resource, new SimpleTexture(resource));
        }

        this.numberXTexture = new ResourceLocation(PvpUtil.getModMetadata().modId, "overlay/score-x.png");
        minecraft.getTextureManager().loadTexture(numberXTexture, new SimpleTexture(numberXTexture));
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.PostConfigChangedEvent e){
        this.enabled = isModEnabled();
        this.soundEnabled = isSoundEnabled();
    }

    @SubscribeEvent
    public void onWorldChange(PlayerChangedDimensionEvent e) {
        if (e.player.isUser()) {
            setCombo(0);
        }
    }

    @SubscribeEvent
    public void onDamage(LivingAttackEvent e) {
        if (e.entityLiving == null || !enabled) {
            return;
        }

        if (e.entityLiving instanceof EntityPlayer && ((EntityPlayer) e.entityLiving).isUser()) {
            setCombo(0);

            if (soundEnabled) {
                e.entityLiving.getEntityWorld().playSound(e.entityLiving.posX, e.entityLiving.posY, e.entityLiving.posZ, soundComboBreak.toString(), 1f, 1f, false);
            }
        }
    }
    
    @SubscribeEvent
    public void onHit(AttackEntityEvent e) {
        if (e.entityPlayer == null || !e.entityPlayer.isUser() || !enabled){
            return;
        }
        
        setCombo(getCombo() + 1);
    }

    @SubscribeEvent
    public void onScreenDraw(RenderGameOverlayEvent.Post e) {
        if (e.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS && enabled && currentCombo > 0) {
            Minecraft minecraft = Minecraft.getMinecraft();

            ResourceLocation[] list = getRequiredTexture(currentCombo);

            ScaledResolution scaledresolution = new ScaledResolution(minecraft);
            int width = scaledresolution.getScaledWidth();
            int height = scaledresolution.getScaledHeight();

            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();

            long timeFromLastHit = (System.currentTimeMillis() - getLastComboChange());
            boolean drawScaleOverlay = timeFromLastHit <= POPOUT_DURATION;
            boolean overlayScale = timeFromLastHit >= 150 && (timeFromLastHit - 150) <= POPOUT_DURATION;
            float scaleOverlayProgress = 0;
            float overlayProgress = 1;

            if (drawScaleOverlay)
                scaleOverlayProgress = timeFromLastHit / POPOUT_DURATION;

            if (overlayScale)
                overlayProgress = (timeFromLastHit - 150) / POPOUT_DURATION;

            int i;
            for (i = 0; i < list.length; i++) {
                ResourceLocation resource = list[i];

                drawComboText(i, width, height, resource, drawScaleOverlay, scaleOverlayProgress, overlayProgress);
            }

            drawComboText(i, width, height, numberXTexture, drawScaleOverlay, scaleOverlayProgress, overlayProgress);

            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
        }
    }

    private void drawComboText(int index, int screenWidth, int screenHeight, ResourceLocation resource, boolean drawScaleOverlay, float scaleOverlayProgress, float overlayProgress) {
        minecraft.getTextureManager().bindTexture(resource);

        if (drawScaleOverlay) {
            GlStateManager.color(1, 1, 1, (1 - scaleOverlayProgress) * 0.75f);
            Gui.drawModalRectWithCustomSizedTexture(LEFT_MARGIN + index * 30, screenHeight - BOTTOM_MARGIN - 40, 0, 0, (int) (30 * ((1 - scaleOverlayProgress) * POPOUT_SCALE)), (int) (40 * ((1 - scaleOverlayProgress) * POPOUT_SCALE)), 40, 30);
        }

        GlStateManager.color(1, 1, 1);

        Gui.drawModalRectWithCustomSizedTexture(LEFT_MARGIN + index * 30, screenHeight - BOTTOM_MARGIN - 40, 0, 0, (int) (30 * (overlayProgress * POPOUT_SMALL_SCALE)), (int) (40 * (overlayProgress * POPOUT_SMALL_SCALE)), 40, 30);
    }

    protected ResourceLocation[] getRequiredTexture(int combo) {
        int size = ((int) Math.log10(combo)) + 1;
        ResourceLocation[] list = new ResourceLocation[size];

        for (int i = 0; i < size; i++) {
            list[i] = numberTextureList.get(combo % 10);
            combo /= 10;
        }
        return list;
    }

    public boolean isModEnabled() {
        if (!getModuleConfigEntry().contains("combo_counter"))
            getModuleConfigEntry().set("combo_counter", false);

        return getModuleConfigEntry().get("combo_counter").getAsBoolean();
    }

    public boolean isSoundEnabled() {
        if (!getModuleConfigEntry().contains("combobreak_sound"))
            getModuleConfigEntry().set("combobreak_sound", false);

        return getModuleConfigEntry().get("combobreak_sound").getAsBoolean();
    }

    public JsonConfigEntry getModuleConfigEntry(){
        if (!mod.getDefaultConfig().contains(OPTION_CATEGORY)) {
            mod.getDefaultConfig().set(OPTION_CATEGORY, new JsonConfigEntry());
        }

        return mod.getDefaultConfig().getObject(OPTION_CATEGORY);
    }
}
