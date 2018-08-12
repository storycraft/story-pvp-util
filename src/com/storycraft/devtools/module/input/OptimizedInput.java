package com.storycraft.devtools.module.input;

import com.storycraft.devtools.DevTools;
import com.storycraft.devtools.config.json.JsonConfigEntry;
import com.storycraft.devtools.module.IModule;
import com.storycraft.devtools.util.AsyncTask;
import com.storycraft.devtools.util.reflect.Reflect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Timer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class OptimizedInput implements IModule {

    public static final String OPTION_CATEGORY = "render";

    private static Reflect.WrappedField<Boolean, Minecraft> running;
    private static Reflect.WrappedField<Timer, Minecraft> timer;

    static {
        running = Reflect.getField(Minecraft.class, "running", "field_71425_J");
        timer = Reflect.getField(Minecraft.class, "timer", "field_71428_T");
    }

    private Minecraft minecraft;
    private DevTools mod;

    private volatile boolean isEnabled;

    @Override
    public void preInitialize() {
        MinecraftForge.EVENT_BUS.register(this);
        this.minecraft = Minecraft.getMinecraft();
    }

    @Override
    public void initialize(DevTools mod) {
        this.mod = mod;

        this.isEnabled = isModEnabled();
        updateTimer();
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent e){
        this.isEnabled = isModEnabled();

        updateTimer();
    }

    private void updateTimer(){
        if (isEnabled) {
            timer.set(minecraft, new OptimizedTimer());
        }
        else{
            timer.set(minecraft, new Timer(20.0F));
        }
    }


    public boolean isModEnabled() {
        if (!getModuleConfigEntry().contains("fast_input_update"))
            getModuleConfigEntry().set("fast_input_update", true);

        if (getModuleConfigEntry().get("fast_input_update").getAsBoolean()) {
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

    public class OptimizedTimer extends Timer {

        public OptimizedTimer() {
            super(1000);
        }
    }
}
