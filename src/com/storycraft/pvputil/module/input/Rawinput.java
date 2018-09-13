package com.storycraft.pvputil.module.input;

import com.storycraft.pvputil.PvpUtil;
import com.storycraft.pvputil.config.json.JsonConfigEntry;
import com.storycraft.pvputil.module.IModule;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

public class Rawinput implements IModule {

    public static final String OPTION_CATEGORY = "input";

    private PvpUtil mod;

    private volatile boolean enabled;

    @Override
    public void preInitialize() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void initialize(PvpUtil mod) {
        this.mod = mod;
        this.enabled = isModEnabled();

        if (enabled) {
            createInputThread();
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.PostConfigChangedEvent e){
        this.enabled = isModEnabled();

        if (enabled != isModEnabled()) {
            this.enabled = isModEnabled();

            if (enabled)
                createInputThread();
        }
    }

    protected void createInputThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(enabled && !Display.isCloseRequested() && Display.isCreated()) {
                    if ( Mouse.isCreated() ) {
                        Mouse.poll();
                        Mouse.updateCursor();
                    }

                    if ( Keyboard.isCreated() ) {
                        Keyboard.poll();
                    }

                    if ( Controllers.isCreated() ) {
                        Controllers.poll();
                    }
                }
            }
        });
    }

    public boolean isModEnabled() {
        if (!getModuleConfigEntry().contains("rawinput"))
            getModuleConfigEntry().set("rawinput", true);

        return getModuleConfigEntry().get("rawinput").getAsBoolean();
    }

    public void setModEnabled(boolean flag) {
        getModuleConfigEntry().set("rawinput", flag);
    }

    public JsonConfigEntry getModuleConfigEntry(){
        if (!mod.getDefaultConfig().contains(OPTION_CATEGORY)) {
            mod.getDefaultConfig().set(OPTION_CATEGORY, new JsonConfigEntry());
        }

        return mod.getDefaultConfig().getObject(OPTION_CATEGORY);
    }
}
