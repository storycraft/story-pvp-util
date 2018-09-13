package com.storycraft.pvputil.module.hitsound;

import com.storycraft.pvputil.PvpUtil;
import com.storycraft.pvputil.config.json.JsonConfigEntry;
import com.storycraft.pvputil.module.IModule;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PlayerHitSound implements IModule {

    public static final String OPTION_CATEGORY = "hitsound";

    private PvpUtil mod;

    private boolean soundEnabled;

    @Override
    public void preInitialize() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void initialize(PvpUtil mod) {
        this.mod = mod;

        this.soundEnabled = isModEnabled();
    }

    @SubscribeEvent
    public void onLeftInteract(AttackEntityEvent e){
        if (e.entityLiving == null || e.target instanceof EntityPlayerSP || !soundEnabled){
            return;
        }

        String sound = e.entityLiving.isEntityAlive() ? "storycraft:hitnormal" : "storycraft:hitfinish";
        e.entityPlayer.getEntityWorld().playSound(e.entityLiving.posX,e.entityLiving.posY,e.entityLiving.posZ, sound, 1, 1, false);
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
