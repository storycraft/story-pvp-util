package com.storycraft.devtools.module.hitsound;

import com.storycraft.devtools.DevTools;
import com.storycraft.devtools.config.json.JsonConfigEntry;
import com.storycraft.devtools.module.IModule;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PlayerHitSound implements IModule {

    public static final String OPTION_CATEGORY = "hitsound";

    private DevTools mod;

    @Override
    public void preInitialize() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void initialize(DevTools mod) {
        this.mod = mod;

        isModEnabled();
    }

    @SubscribeEvent
    public void onLeftInteract(AttackEntityEvent e){
        if (e.entityLiving == null || !(e.entityPlayer instanceof EntityPlayerSP) || !isModEnabled()){
            return;
        }

        String sound = e.entityLiving.isEntityAlive() ? "storycraft:hitnormal" : "storycraft:hitfinish";
        e.entityPlayer.getEntityWorld().playSound(e.entityLiving.posX,e.entityLiving.posY,e.entityLiving.posZ, sound, 1, 1, false);
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
