package com.storycraft.pvputil.module.hitsound;

import com.storycraft.pvputil.PvpUtil;
import com.storycraft.pvputil.config.json.JsonConfigEntry;
import com.storycraft.pvputil.module.IModule;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PlayerHitSound implements IModule {

    public static final String OPTION_CATEGORY = "hitsound";

    private PvpUtil mod;

    private boolean soundEnabled;
    private boolean newDisabled;

    private ResourceLocation soundHitNormalLoc;
    private ResourceLocation soundHitClapLoc;
    private ResourceLocation soundHitFinishLoc;

    private SoundEvent soundHitNormal;
    private SoundEvent soundHitClap;
    private SoundEvent soundHitFinish;

    @Override
    public void preInitialize() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void initialize(PvpUtil mod) {
        this.mod = mod;

        this.soundEnabled = isModEnabled();
        this.newDisabled = isNewHitsoundDisabled();
    }

    @SubscribeEvent
    public void registerSound(RegistryEvent.Register<SoundEvent> e) {
        this.soundHitNormalLoc = new ResourceLocation("storycraft", "hitnormal");
        this.soundHitClapLoc = new ResourceLocation("storycraft", "hitclap");
        this.soundHitFinishLoc = new ResourceLocation("storycraft", "hitfinish");

        this.soundHitNormal = new SoundEvent(soundHitNormalLoc);
        this.soundHitClap = new SoundEvent(soundHitClapLoc);
        this.soundHitFinish = new SoundEvent(soundHitFinishLoc);

        soundHitNormal.setRegistryName("hitsound.normal");
        soundHitClap.setRegistryName("hitsound.clap");
        soundHitFinish.setRegistryName("hitsound.finish");

        e.getRegistry().registerAll(soundHitNormal, soundHitClap, soundHitFinish);
    }

    @SubscribeEvent
    public void onLeftInteract(AttackEntityEvent e){
        if (e.getEntityPlayer() == null || !e.getEntityPlayer().isUser() || !soundEnabled){
            return;
        }

        Entity target = e.getTarget();
        EntityPlayer attacker = e.getEntityPlayer();

        float f2 = attacker.getCooledAttackStrength(0.5F);
        boolean wtap = f2 > 0.9F;

        boolean crit = wtap && attacker.fallDistance > 0.0F && !attacker.onGround && !attacker.isOnLadder() && !attacker.isInWater() && !attacker.isPotionActive(MobEffects.BLINDNESS) && !attacker.isRiding() && !attacker.isSprinting();

        SoundEvent sound = soundHitNormal;

        if (attacker.isSprinting() && wtap) { //W tapping
            sound = soundHitClap;
        }
        
        if (crit) { //Crit
            sound = soundHitFinish;
        }

        attacker.getEntityWorld().playSound(attacker, target.posX, target.posY, target.posZ, sound, SoundCategory.PLAYERS, 1f, 1f);
    }

    @SubscribeEvent
    public void onNewHitSoundPlay(PlaySoundAtEntityEvent e) {
        if (!this.newDisabled || !e.getSound().getRegistryName().getResourcePath().startsWith("entity.player.attack")) {
            return;
        }

        e.setCanceled(true);
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.PostConfigChangedEvent e) {
        this.soundEnabled = isModEnabled();
        this.newDisabled = isNewHitsoundDisabled();
    }

    public boolean isModEnabled() {
        if (!getModuleConfigEntry().contains("sound_when_click_entity"))
            getModuleConfigEntry().set("sound_when_click_entity", true);

        return getModuleConfigEntry().get("sound_when_click_entity").getAsBoolean();
    }

    public boolean isNewHitsoundDisabled() {
        if (!getModuleConfigEntry().contains("disable_1_9_hitsound"))
            getModuleConfigEntry().set("disable_1_9_hitsound", false);

        return getModuleConfigEntry().get("disable_1_9_hitsound").getAsBoolean();
    }

    public JsonConfigEntry getModuleConfigEntry(){
        if (!mod.getDefaultConfig().contains(OPTION_CATEGORY)) {
            mod.getDefaultConfig().set(OPTION_CATEGORY, new JsonConfigEntry());
        }

        return mod.getDefaultConfig().getObject(OPTION_CATEGORY);
    }
}
