package com.storycraft.devtools.module.fullscreen;

import com.storycraft.devtools.DevTools;
import com.storycraft.devtools.config.json.JsonConfigEntry;
import com.storycraft.devtools.module.IModule;
import com.storycraft.devtools.util.reflect.Reflect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

public class FullscreenCursorConfine implements IModule {

    public static final String OPTION_CATEGORY = "fullscreen";

    private static final Reflect.WrappedField<Boolean, Minecraft> fullscreen;
    private static final Reflect.WrappedMethod<Void, Minecraft> updateDisplayMode;

    static {
        fullscreen = Reflect.getField(Minecraft.class, "fullscreen");
        updateDisplayMode = Reflect.getMethod(Minecraft.class, "updateDisplayMode");
    }

    private DevTools mod;
    private Minecraft minecraft;
    private KeyBinding fullscreenToggle;

    @Override
    public void preInitialize() {
        this.minecraft = Minecraft.getMinecraft();
        MinecraftForge.EVENT_BUS.register(this);
        this.fullscreenToggle = bindFullscreenKey();
    }

    @Override
    public void initialize(DevTools mod) {
        this.mod = mod;

        isModEnabled();
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

    public JsonConfigEntry getModuleConfigEntry(){
        if (!mod.getDefaultConfig().contains(OPTION_CATEGORY)) {
            mod.getDefaultConfig().set(OPTION_CATEGORY, new JsonConfigEntry());
        }

        return mod.getDefaultConfig().getObject(OPTION_CATEGORY);
    }

    @SubscribeEvent
    public void onFullscreenSwitch(InputEvent.KeyInputEvent e){
        if (fullscreenToggle.isPressed()){
            boolean isFullscreen = minecraft.isFullScreen();

            if (!isModEnabled()){
                if (minecraft.isFullScreen()){
                    try {
                        Display.setLocation(0, 0);
                        updateDisplayMode.invoke(minecraft);
                        fullscreen.set(minecraft, minecraft.gameSettings.fullScreen = !isFullscreen);
                        System.setProperty("org.lwjgl.opengl.Window.undecorated","true");
                        minecraft.updateDisplay();
                    } catch (Exception ex) {
                        mod.getLogger().warning("cannot switch to windowed fullscreen");

                        getModuleConfigEntry().set("confine_cursor_on_fullscreen", true);
                        onFullscreenSwitch(e);
                    }
                }
                else {
                    minecraft.toggleFullscreen();
                    System.setProperty("org.lwjgl.opengl.Window.undecorated","false");
                }

            }
            else {
                minecraft.toggleFullscreen();
                System.setProperty("org.lwjgl.opengl.Window.undecorated","false");
            }
        }
    }
}
