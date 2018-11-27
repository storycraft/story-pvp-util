package com.storycraft.pvputil.module.overlay;

import java.util.ArrayList;
import java.util.List;

import com.storycraft.pvputil.PvpUtil;
import com.storycraft.pvputil.config.json.JsonConfigEntry;
import com.storycraft.pvputil.module.IModule;

import net.minecraft.client.Minecraft;
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

    private PvpUtil mod;

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

            int i;
            for (i = 0; i < list.length; i++) {
                ResourceLocation resource = list[0];
                minecraft.getTextureManager().bindTexture(resource);

                minecraft.ingameGUI.drawTexturedModalRect(LEFT_MARGIN + i * 30, height - BOTTOM_MARGIN - 40, 0, 0, 30, 40);
            }

            minecraft.getTextureManager().bindTexture(numberXTexture);
            minecraft.ingameGUI.drawTexturedModalRect(LEFT_MARGIN + i * 30, height - BOTTOM_MARGIN - 40, 0, 0, 30, 40);

            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
        }
    }

    protected ResourceLocation[] getRequiredTexture(int combo) {
        ResourceLocation[] list = new ResourceLocation[((int) Math.log10(combo)) + 1];

        for (int currentNumber = 0, i = 0; (currentNumber = (combo % 10)) > 0; combo /= 10, i++) {
            list[i] = numberTextureList.get(currentNumber);
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
