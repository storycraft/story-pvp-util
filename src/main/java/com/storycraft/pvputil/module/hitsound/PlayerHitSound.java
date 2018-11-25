package com.storycraft.pvputil.module.hitsound;

import com.storycraft.pvputil.PvpUtil;
import com.storycraft.pvputil.config.json.JsonConfigEntry;
import com.storycraft.pvputil.module.IModule;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PlayerHitSound implements IModule {

    public static final String OPTION_CATEGORY = "hitsound";

    private PvpUtil mod;

    private boolean soundEnabled;

    private ResourceLocation soundHitNormalLoc;
    private ResourceLocation soundHitFinishLoc;

    @Override
    public void preInitialize() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void initialize(PvpUtil mod) {
        this.mod = mod;

        this.soundHitNormalLoc = new ResourceLocation("storycraft", "hitnormal");
        this.soundHitFinishLoc = new ResourceLocation("storycraft", "hitfinish");

        this.soundEnabled = isModEnabled();
    }

    @SubscribeEvent
    public void onLeftInteract(AttackEntityEvent e){
        if (e.getEntityLiving() == null || e.getTarget() instanceof EntityPlayerSP || !soundEnabled){
            return;
        }

        SoundEvent sound = new SoundEvent(e.getEntityLiving().isEntityAlive() ? this.soundHitNormalLoc : this.soundHitFinishLoc);
        e.getEntityLiving().getEntityWorld().playSound(e.getEntityPlayer(), e.getEntityLiving().posX, e.getEntityLiving().posY, e.getEntityLiving().posZ, sound, SoundCategory.PLAYERS, 1f, 1f);
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.PostConfigChangedEvent e) {
        this.soundEnabled = isModEnabled();
    }

    public boolean isModEnabled() {
        if (!getModuleConfigEntry().contains("sound_when_click_entity"))
            getModuleConfigEntry().set("sound_when_click_entity", true);

        return getModuleConfigEntry().get("sound_when_click_entity").getAsBoolean();
    }

    public JsonConfigEntry getModuleConfigEntry(){
        if (!mod.getDefaultConfig().contains(OPTION_CATEGORY)) {
            mod.getDefaultConfig().set(OPTION_CATEGORY, new JsonConfigEntry());
        }

        return mod.getDefaultConfig().getObject(OPTION_CATEGORY);
    }
}
