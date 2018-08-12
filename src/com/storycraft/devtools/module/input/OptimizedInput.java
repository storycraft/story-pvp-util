package com.storycraft.devtools.module.input;

import com.storycraft.devtools.DevTools;
import com.storycraft.devtools.config.json.JsonConfigEntry;
import com.storycraft.devtools.module.IModule;
import com.storycraft.devtools.util.reflect.Reflect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class OptimizedInput implements IModule {

    public static final String OPTION_CATEGORY = "update";

    private static Reflect.WrappedField<Integer, Minecraft> rightClickDelayTimer;
    private static Reflect.WrappedField<NetworkManager, Minecraft> myNetworkManager;

    private static Reflect.WrappedMethod<Void, Minecraft> clickMouse;
    private static Reflect.WrappedMethod<Void, Minecraft> rightClickMouse;
    private static Reflect.WrappedMethod<Void, Minecraft> middleClickMouse;
    private static Reflect.WrappedMethod<Void, Minecraft> sendClickBlockToController;

    static {
        myNetworkManager = Reflect.getField(Minecraft.class, "myNetworkManager", "field_71453_ak");
        rightClickDelayTimer = Reflect.getField(Minecraft.class, "rightClickDelayTimer", "field_71467_ac");

        clickMouse = Reflect.getMethod(Minecraft.class, new String[]{"clickMouse", "func_147116_af"});
        rightClickMouse = Reflect.getMethod(Minecraft.class, new String[]{"rightClickMouse", "func_147121_ag"});
        middleClickMouse = Reflect.getMethod(Minecraft.class, new String[]{"middleClickMouse", "func_147112_ai"});
        sendClickBlockToController = Reflect.getMethod(Minecraft.class, new String[]{"sendClickBlockToController", "func_147115_a"}, boolean.class);
    }

    private Minecraft minecraft;
    private DevTools mod;

    private boolean updateInput;
    private boolean isEnabled;

    @Override
    public void preInitialize() {
        this.minecraft = Minecraft.getMinecraft();
    }

    @Override
    public void initialize(DevTools mod) {
        this.mod = mod;

        MinecraftForge.EVENT_BUS.register(this);

        this.isEnabled = isModEnabled();
        this.updateInput = true;
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent e){
        this.isEnabled = isModEnabled();
    }

    @SubscribeEvent
    public void onUpdateTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START || !isEnabled)
            return;

        try {
            updateInput();
        } catch (IOException e1) {

        }
    }

    private void updateInput() throws IOException {
        if (this.updateInput) {
            minecraft.mcProfiler.endStartSection("mouse");

            while (Mouse.next()) {
                if (net.minecraftforge.client.ForgeHooksClient.postMouseEvent()) continue;

                int i = Mouse.getEventButton();
                KeyBinding.setKeyBindState(i - 100, Mouse.getEventButtonState());

                if (Mouse.getEventButtonState()) {
                    if (minecraft.thePlayer.isSpectator() && i == 2) {
                        minecraft.ingameGUI.getSpectatorGui().func_175261_b();
                    } else {
                        KeyBinding.onTick(i - 100);
                    }
                }

                int j = Mouse.getEventDWheel();

                if (j != 0) {
                    if (minecraft.thePlayer.isSpectator()) {
                        j = j < 0 ? -1 : 1;

                        if (minecraft.ingameGUI.getSpectatorGui().func_175262_a()) {
                            minecraft.ingameGUI.getSpectatorGui().func_175259_b(-j);
                        } else {
                            float f = MathHelper.clamp_float(minecraft.thePlayer.capabilities.getFlySpeed() + (float) j * 0.005F, 0.0F, 0.2F);
                            minecraft.thePlayer.capabilities.setFlySpeed(f);
                        }
                    } else {
                        minecraft.thePlayer.inventory.changeCurrentItem(j);
                    }
                }

                if (minecraft.currentScreen == null) {
                    if (!minecraft.inGameHasFocus && Mouse.getEventButtonState()) {
                        minecraft.setIngameFocus();
                    }
                } else if (minecraft.currentScreen != null) {
                    minecraft.currentScreen.handleMouseInput();
                }

                net.minecraftforge.fml.common.FMLCommonHandler.instance().fireMouseInput();
            }

            minecraft.mcProfiler.endStartSection("keyboard");

            while (Keyboard.next()) {
                int k = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
                KeyBinding.setKeyBindState(k, Keyboard.getEventKeyState());

                if (Keyboard.getEventKeyState()) {
                    KeyBinding.onTick(k);
                }

                minecraft.dispatchKeypresses();

                if (Keyboard.getEventKeyState()) {
                    if (k == 62 && minecraft.entityRenderer != null) {
                        minecraft.entityRenderer.switchUseShader();
                    }

                    if (minecraft.currentScreen != null) {
                        minecraft.currentScreen.handleKeyboardInput();
                    } else {
                        if (k == 1) {
                            minecraft.displayInGameMenu();
                        }

                        if (k == 32 && Keyboard.isKeyDown(61) && minecraft.ingameGUI != null) {
                            minecraft.ingameGUI.getChatGUI().clearChatMessages();
                        }

                        if (k == 31 && Keyboard.isKeyDown(61)) {
                            minecraft.refreshResources();
                        }

                        if (k == 17 && Keyboard.isKeyDown(61)) {
                            ;
                        }

                        if (k == 18 && Keyboard.isKeyDown(61)) {
                            ;
                        }

                        if (k == 47 && Keyboard.isKeyDown(61)) {
                            ;
                        }

                        if (k == 38 && Keyboard.isKeyDown(61)) {
                            ;
                        }

                        if (k == 22 && Keyboard.isKeyDown(61)) {
                            ;
                        }

                        if (k == 20 && Keyboard.isKeyDown(61)) {
                            minecraft.refreshResources();
                        }

                        if (k == 33 && Keyboard.isKeyDown(61)) {
                            minecraft.gameSettings.setOptionValue(GameSettings.Options.RENDER_DISTANCE, GuiScreen.isShiftKeyDown() ? -1 : 1);
                        }

                        if (k == 30 && Keyboard.isKeyDown(61)) {
                            minecraft.renderGlobal.loadRenderers();
                        }

                        if (k == 35 && Keyboard.isKeyDown(61)) {
                            minecraft.gameSettings.advancedItemTooltips = !minecraft.gameSettings.advancedItemTooltips;
                            minecraft.gameSettings.saveOptions();
                        }

                        if (k == 48 && Keyboard.isKeyDown(61)) {
                            minecraft.getRenderManager().setDebugBoundingBox(!minecraft.getRenderManager().isDebugBoundingBox());
                        }

                        if (k == 25 && Keyboard.isKeyDown(61)) {
                            minecraft.gameSettings.pauseOnLostFocus = !minecraft.gameSettings.pauseOnLostFocus;
                            minecraft.gameSettings.saveOptions();
                        }

                        if (k == 59) {
                            minecraft.gameSettings.hideGUI = !minecraft.gameSettings.hideGUI;
                        }

                        if (k == 61) {
                            minecraft.gameSettings.showDebugInfo = !minecraft.gameSettings.showDebugInfo;
                            minecraft.gameSettings.showDebugProfilerChart = GuiScreen.isShiftKeyDown();
                            minecraft.gameSettings.showLagometer = GuiScreen.isAltKeyDown();
                        }

                        if (minecraft.gameSettings.keyBindTogglePerspective.isPressed()) {
                            ++minecraft.gameSettings.thirdPersonView;

                            if (minecraft.gameSettings.thirdPersonView > 2) {
                                minecraft.gameSettings.thirdPersonView = 0;
                            }

                            if (minecraft.gameSettings.thirdPersonView == 0) {
                                minecraft.entityRenderer.loadEntityShader(minecraft.getRenderViewEntity());
                            } else if (minecraft.gameSettings.thirdPersonView == 1) {
                                minecraft.entityRenderer.loadEntityShader((Entity) null);
                            }

                            minecraft.renderGlobal.setDisplayListEntitiesDirty();
                        }

                        if (minecraft.gameSettings.keyBindSmoothCamera.isPressed()) {
                            minecraft.gameSettings.smoothCamera = !minecraft.gameSettings.smoothCamera;
                        }
                    }

                }
                net.minecraftforge.fml.common.FMLCommonHandler.instance().fireKeyInput();
            }

            for (int l = 0; l < 9; ++l) {
                if (minecraft.gameSettings.keyBindsHotbar[l].isPressed()) {
                    if (minecraft.thePlayer.isSpectator()) {
                        minecraft.ingameGUI.getSpectatorGui().func_175260_a(l);
                    } else {
                        minecraft.thePlayer.inventory.currentItem = l;
                    }
                }
            }

            boolean flag = minecraft.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN;

            while (minecraft.gameSettings.keyBindInventory.isPressed()) {
                if (minecraft.playerController.isRidingHorse()) {
                    minecraft.thePlayer.sendHorseInventory();
                } else {
                    minecraft.getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                    minecraft.displayGuiScreen(new GuiInventory(minecraft.thePlayer));
                }
            }

            while (minecraft.gameSettings.keyBindDrop.isPressed()) {
                if (!minecraft.thePlayer.isSpectator()) {
                    minecraft.thePlayer.dropOneItem(GuiScreen.isCtrlKeyDown());
                }
            }

            while (minecraft.gameSettings.keyBindChat.isPressed() && flag) {
                minecraft.displayGuiScreen(new GuiChat());
            }

            if (minecraft.currentScreen == null && minecraft.gameSettings.keyBindCommand.isPressed() && flag) {
                minecraft.displayGuiScreen(new GuiChat("/"));
            }

            if (minecraft.thePlayer.isUsingItem()) {
                if (!minecraft.gameSettings.keyBindUseItem.isKeyDown()) {
                    minecraft.playerController.onStoppedUsingItem(minecraft.thePlayer);
                }

            }

            while (minecraft.gameSettings.keyBindAttack.isPressed()) {
                clickMouse.invoke(minecraft);
            }

            while (minecraft.gameSettings.keyBindUseItem.isPressed()) {
                rightClickMouse.invoke(minecraft);
            }

            while (minecraft.gameSettings.keyBindPickBlock.isPressed()) {
                middleClickMouse.invoke(minecraft);
            }

            if (minecraft.gameSettings.keyBindUseItem.isKeyDown() && rightClickDelayTimer.get(minecraft) == 0 && !minecraft.thePlayer.isUsingItem()) {
                rightClickMouse.invoke(minecraft);
            }

            sendClickBlockToController.invoke(minecraft, minecraft.currentScreen == null && minecraft.gameSettings.keyBindAttack.isKeyDown() && minecraft.inGameHasFocus);
        }

        NetworkManager networkManager = myNetworkManager.get(minecraft);

        if (minecraft.theWorld == null && networkManager != null)
        {
            minecraft.mcProfiler.endStartSection("pendingConnection");
            networkManager.processReceivedPackets();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiOpen(GuiOpenEvent e){
        if (!isEnabled)
            return;

        if (e.gui != null) {
            if (!e.gui.allowUserInput) {
                this.updateInput = false;
                return;
            }
            else {
                e.gui.allowUserInput = false;
            }
        }

       this.updateInput = true;
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
}
