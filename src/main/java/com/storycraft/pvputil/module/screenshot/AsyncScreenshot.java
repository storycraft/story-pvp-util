package com.storycraft.pvputil.module.screenshot;

import com.storycraft.pvputil.PvpUtil;
import com.storycraft.pvputil.config.json.JsonConfigEntry;
import com.storycraft.pvputil.module.IModule;
import com.storycraft.pvputil.util.AsyncTask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AsyncScreenshot implements IModule {

    public static final String OPTION_CATEGORY = "screenshot";

    private Minecraft minecraft;
    private PvpUtil mod;

    private boolean glWorking;
    private boolean asyncEnabled;

    private IntBuffer cachedPixelBuffer = null;
    private int[] cachedPixelValues = null;

    private boolean pressed;

    private KeyBinding hookedKeyBinding;

    @Override
    public void preInitialize() {
        this.minecraft = Minecraft.getMinecraft();

        MinecraftForge.EVENT_BUS.register(this);
        this.pressed = false;
        this.hookedKeyBinding = bindScreenshotKey();
    }

    @Override
    public void initialize(PvpUtil mod) {
        this.mod = mod;

        //update config before start
        this.glWorking = false;
        this.asyncEnabled = isModEnabled();
    }

    private KeyBinding bindScreenshotKey() {
        KeyBinding[] list = minecraft.gameSettings.keyBindings;
        KeyBinding target = minecraft.gameSettings.keyBindScreenshot;
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

    public File getScreenshotFolder(){
        return new File(minecraft.mcDataDir, "screenshots");
    }

    @SubscribeEvent
    public void onIngameKeyHit(InputEvent.KeyInputEvent e){
        if (Keyboard.getEventKey() == hookedKeyBinding.getKeyCode()) {
            onScreenshotHit();
        }
    }

    @SubscribeEvent
    public void onGuiKeyHit(GuiScreenEvent.KeyboardInputEvent.Post e) {
        if (Keyboard.getEventKey() == hookedKeyBinding.getKeyCode()) {
            onScreenshotHit();
        }
    }

    protected void onScreenshotHit() {
        if (!pressed) {
            pressed = true;
            onScreenshotPressed();
        }
        else if (pressed) {
            pressed = false;
        }
    }

    private void onScreenshotPressed() {
        if (glWorking)
            return;

        glWorking = true;

        if (asyncEnabled) {
            saveScreenshot(minecraft.displayWidth, minecraft.displayHeight, minecraft.getFramebuffer()).run();
        }
        else {
            saveScreenshot(minecraft.displayWidth, minecraft.displayHeight, minecraft.getFramebuffer()).run().join();
        }

        glWorking = false;
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.PostConfigChangedEvent e){
        this.asyncEnabled = isModEnabled();
    }

    public AsyncTask saveScreenshot(int width, int height, Framebuffer buffer) {
        File screenshotFolder = getScreenshotFolder();
        screenshotFolder.mkdirs();

        if (OpenGlHelper.isFramebufferEnabled()) {
            width = buffer.framebufferWidth;
            height = buffer.framebufferHeight;
        }

        int screenWidth = width;
        int screenHeight = height;

        int size = screenWidth * screenHeight;

        if (cachedPixelBuffer == null || cachedPixelValues.length != size) {
            cachedPixelBuffer = BufferUtils.createIntBuffer(size);
            cachedPixelValues = new int[size];
        }

        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        cachedPixelBuffer.clear();

        if (OpenGlHelper.isFramebufferEnabled()) {
            GlStateManager.bindTexture(buffer.framebufferTexture);
            GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, 32993, 33639, cachedPixelBuffer);
        } else {
            GL11.glReadPixels(0, 0, width, height, 32993, 33639, cachedPixelBuffer);
        }

        cachedPixelBuffer.get(cachedPixelValues);
        return new AsyncTask<>(new AsyncTask.AsyncCallable<Void>() {
            @Override
            public Void get() {
                File screenshotFile;

                TextureUtil.processPixelValues(cachedPixelValues, screenWidth, screenHeight);

                try {
                    BufferedImage bufferedimage = new BufferedImage(screenWidth, screenHeight, Image.SCALE_DEFAULT);

                    bufferedimage.setRGB(0, 0, screenWidth, screenHeight, cachedPixelValues, 0, screenWidth);

                    screenshotFile = getScreenshotFile(screenshotFolder, new Date());

                    ImageIO.write(bufferedimage, "png", screenshotFile);

                    ITextComponent textComponent = new TextComponentString(screenshotFile.getName());
                    textComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, screenshotFile.getAbsoluteFile().getAbsolutePath()));
                    textComponent.getStyle().setUnderlined(Boolean.valueOf(true));

                    minecraft.ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("screenshot.success", new Object[]{textComponent}));
                }
                catch (Exception e) {
                    mod.getLogger().warning("Couldn't save screenshot " + e.getLocalizedMessage());
                    minecraft.ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("screenshot.failure", new Object[]{e.getLocalizedMessage()}));
                }

                return null;
            }
        });
    }

    private static File getScreenshotFile(File screenshotFolder, Date date) {
        String s = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(date);
        File file;

        int i = 1;
        while ((file = new File(screenshotFolder, (s + (i == 1 ? "" : new StringBuilder().append("_").append(i).toString()) + ".png"))).exists()) {
            ++i;
        }

        return file;
    }

    public boolean isModEnabled() {
        if (!getModuleConfigEntry().contains("async_screenshot"))
            getModuleConfigEntry().set("async_screenshot", true);

        return getModuleConfigEntry().get("async_screenshot").getAsBoolean();
    }

    public JsonConfigEntry getModuleConfigEntry(){
        if (!mod.getDefaultConfig().contains(OPTION_CATEGORY)) {
            mod.getDefaultConfig().set(OPTION_CATEGORY, new JsonConfigEntry());
        }

        return mod.getDefaultConfig().getObject(OPTION_CATEGORY);
    }
}
