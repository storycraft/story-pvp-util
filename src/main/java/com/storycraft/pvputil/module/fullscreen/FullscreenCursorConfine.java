package com.storycraft.pvputil.module.fullscreen;

import com.storycraft.pvputil.PvpUtil;
import com.storycraft.pvputil.config.json.JsonConfigEntry;
import com.storycraft.pvputil.module.IModule;
import com.storycraft.pvputil.util.reflect.Reflect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

public class FullscreenCursorConfine implements IModule {

    public static final String OPTION_CATEGORY = "fullscreen";

    private static final Reflect.WrappedField<Boolean, Minecraft> fullscreen;
    private static final Reflect.WrappedMethod<Void, Minecraft> updateDisplayMode;

    static {
        fullscreen = Reflect.getField(Minecraft.class, "fullscreen", "field_71431_Q");
        updateDisplayMode = Reflect.getMethod(Minecraft.class, "updateDisplayMode", "func_110441_Q");
    }

    private PvpUtil mod;
    private Minecraft minecraft;
    private KeyBinding fullscreenToggle;

    private int lastX;
    private int lastY;

    private boolean pressed;

    private boolean confineCursorToScreen;

    @Override
    public void preInitialize() {
        this.minecraft = Minecraft.getMinecraft();
        MinecraftForge.EVENT_BUS.register(this);
        this.lastX = 0;
        this.lastY = 0;
        this.fullscreenToggle = bindFullscreenKey();
        this.pressed = false;
    }

    @Override
    public void initialize(PvpUtil mod) {
        this.mod = mod;

        this.confineCursorToScreen = isModEnabled();
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.PostConfigChangedEvent e) {
        if (this.confineCursorToScreen != isModEnabled() && minecraft.isFullScreen()) {
            this.confineCursorToScreen = isModEnabled();
            minecraft.toggleFullscreen();

            toggleFullscreen();
        }
    }

    @Override
    public void postInitialize() {
        if (!isModEnabled() && minecraft.isFullScreen()) {
            minecraft.toggleFullscreen();

            toggleFullscreen();
        }
    }

    private KeyBinding bindFullscreenKey() {
        KeyBinding[] list = minecraft.gameSettings.keyBindings;
        KeyBinding target = minecraft.gameSettings.keyBindFullscreen;
        KeyBinding newKeybind = new KeyBinding(target.getKeyDescription(), target.getKeyCode(), target.getKeyCategory());

        for (int i = 0; i < list.length; i++) {
            KeyBinding binding = list[i];

            if (binding.getKeyDescription().equals(target.getKeyDescription())) {
                list[i] = newKeybind;
                target.setKeyCode(-1);
            }
        }
        return newKeybind;
    }


    public boolean isModEnabled() {
        if (!getModuleConfigEntry().contains("confine_cursor_on_fullscreen"))
            getModuleConfigEntry().set("confine_cursor_on_fullscreen", true);

        return getModuleConfigEntry().get("confine_cursor_on_fullscreen").getAsBoolean();
    }

    public void setModEnabled(boolean flag) {
        getModuleConfigEntry().set("confine_cursor_on_fullscreen", flag);
    }

    public JsonConfigEntry getModuleConfigEntry(){
        if (!mod.getDefaultConfig().contains(OPTION_CATEGORY)) {
            mod.getDefaultConfig().set(OPTION_CATEGORY, new JsonConfigEntry());
        }

        return mod.getDefaultConfig().getObject(OPTION_CATEGORY);
    }

    @SubscribeEvent
    public void onIngameKeyHit(InputEvent.KeyInputEvent e){
        if (Keyboard.getEventKey() == fullscreenToggle.getKeyCode()) {
            onFullscreenHit();
        }
    }

    @SubscribeEvent
    public void onGuiKeyHit(GuiScreenEvent.KeyboardInputEvent.Post e) {
        if (Keyboard.getEventKey() == fullscreenToggle.getKeyCode()) {
            onFullscreenHit();
        }
    }

    protected void onFullscreenHit() {
        if (!pressed) {
            pressed = true;
            toggleFullscreen();
        }
        else if (pressed) {
            pressed = false;
        }
    }

    private void toggleFullscreen() {
        if (!confineCursorToScreen && !minecraft.isFullScreen()){
            try {

                Display.setLocation(0, 0);
                System.setProperty("org.lwjgl.opengl.Window.undecorated","true");
                updateDisplayMode.invoke(minecraft);
                minecraft.gameSettings.fullScreen = true;
                fullscreen.set(minecraft, true);
                minecraft.resize(minecraft.displayWidth, minecraft.displayHeight);
                minecraft.updateDisplay();
            } catch (Exception ex) {
                mod.getLogger().warning("cannot switch to windowed fullscreen");

                setModEnabled(true);
                toggleFullscreen();
            }
        }
        else {
            System.setProperty("org.lwjgl.opengl.Window.undecorated","false");
            minecraft.toggleFullscreen();

            if (!minecraft.isFullScreen())
                Display.setLocation(lastX, lastY);
        }

        lastX = Display.getX();
        lastY = Display.getY();
    }
}
