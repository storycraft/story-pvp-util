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
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

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

    private boolean sprint;
    private float lastSprint;

    @Override
    public void preInitialize() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void initialize(PvpUtil mod) {
        this.mod = mod;

        this.soundEnabled = isModEnabled();
        this.newDisabled = isNewHitsoundDisabled();

        this.lastSprint = 0;
        this.sprint = false;
    }

    @SubscribeEvent
    public void registerSound(RegistryEvent.Register<SoundEvent> e) {
        this.soundHitNormalLoc = new ResourceLocation(PvpUtil.getModMetadata().modId, "hitsound.normal");
        this.soundHitClapLoc = new ResourceLocation(PvpUtil.getModMetadata().modId, "hitsound.clap");
        this.soundHitFinishLoc = new ResourceLocation(PvpUtil.getModMetadata().modId, "hitsound.finish");

        this.soundHitNormal = new SoundEvent(soundHitNormalLoc);
        this.soundHitClap = new SoundEvent(soundHitClapLoc);
        this.soundHitFinish = new SoundEvent(soundHitFinishLoc);

        soundHitNormal.setRegistryName("hitsound.normal");
        soundHitClap.setRegistryName("hitsound.clap");
        soundHitFinish.setRegistryName("hitsound.finish");
    }

    @SubscribeEvent
    public void capturePlayerSprint(PlayerTickEvent e) {
        if (e.player.isUser()) {
            if (sprint != e.player.isSprinting()) {
                if (!sprint) {
                    lastSprint = System.currentTimeMillis();
                }

                sprint = e.player.isSprinting();
            }
        }
    }

    @SubscribeEvent
    public void onLeftInteract(AttackEntityEvent e){
        if (e.getEntityPlayer() == null || !e.getEntityPlayer().isUser() || !soundEnabled){
            return;
        }

        Entity target = e.getTarget();
        EntityPlayer attacker = e.getEntityPlayer();

        float f2 = attacker.getCooledAttackStrength(0.5F);
        boolean power = f2 > 0.9F;

        boolean crit = attacker.fallDistance > 0.0F && !attacker.onGround && !attacker.isOnLadder() && !attacker.isInWater() && !attacker.isPotionActive(MobEffects.BLINDNESS) && !attacker.isRiding() && !attacker.isSprinting();

        World world = attacker.getEntityWorld();

        world.playSound(attacker, target.posX, target.posY, target.posZ, soundHitNormal, SoundCategory.PLAYERS, 1f, 1f);

        if (power) {
            if (attacker.isSprinting() && (System.currentTimeMillis() - lastSprint) < 1000) { //W tap
                world.playSound(attacker, target.posX, target.posY, target.posZ, soundHitClap, SoundCategory.PLAYERS, 1f, 1f);
            }
        
            if (crit) { //Crit
                world.playSound(attacker, target.posX, target.posY, target.posZ, soundHitFinish, SoundCategory.PLAYERS, 1f, 1f);
            }
        }
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
