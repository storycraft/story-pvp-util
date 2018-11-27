package com.storycraft.pvputil.module;

import com.storycraft.pvputil.PvpUtil;
import com.storycraft.pvputil.module.chat.ChatOptimize;
import com.storycraft.pvputil.module.fullscreen.FullscreenCursorConfine;
import com.storycraft.pvputil.module.hitsound.PlayerHitSound;
import com.storycraft.pvputil.module.input.Rawinput;
import com.storycraft.pvputil.module.overlay.ComboCounter;
import com.storycraft.pvputil.module.render.ClientPlayerNameTag;
import com.storycraft.pvputil.module.render.DynamicBoundingBox;
import com.storycraft.pvputil.module.render.LabelBoxRenderToggle;
import com.storycraft.pvputil.module.server.ServerResourcePackBypass;
import com.storycraft.pvputil.module.screenshot.AsyncScreenshot;
import com.storycraft.pvputil.module.session.SessionRefresh;
import com.storycraft.pvputil.module.taboverlay.TabOptimize;
import com.storycraft.pvputil.util.Parallel;

import java.util.HashMap;
import java.util.Map;

public class ModuleManager {

    private Map<String, IModule> moduleMap;

    private PvpUtil mod;

    public ModuleManager(PvpUtil mod){
        this.mod = mod;
        this.moduleMap = new HashMap<>();

        addDefaultModule();
    }

    private void addDefaultModule() {
        addModule("async_screenshot", new AsyncScreenshot());

        addModule("hitsound", new PlayerHitSound());
        addModule("combo_counter", new ComboCounter());

        addModule("session_refresh", new SessionRefresh());

        addModule("fullscreen_cursor", new FullscreenCursorConfine());

        addModule("client_player_nametag", new ClientPlayerNameTag());
        addModule("label_box_render_toggle", new LabelBoxRenderToggle());
        addModule("dynamic_boundingbox", new DynamicBoundingBox());

        addModule("chat_optimize", new ChatOptimize());
        addModule("tab_optimize", new TabOptimize());

        addModule("rawinput", new Rawinput());

        addModule("server_resourcepacks_bypass", new ServerResourcePackBypass());
    }

    public PvpUtil getMod() {
        return mod;
    }

    public <T extends IModule>T getModule(String name){
        if (!contains(name))
            return null;

        return (T) moduleMap.get(name);
    }

    public void addModule(String name, IModule module){
        if (contains(name))
            getMod().getLogger().warning("module " + name + " is already loaded");

        if (getMod().isInited()) {
            module.preInitialize();
        }

        if (getMod().isLoaded()) {
            module.initialize(getMod());
        }

        if (getMod().isPostLoaded()) {
            module.postInitialize();
        }

        moduleMap.put(name, module);
    }

    public boolean contains(String name){
        return moduleMap.containsKey(name);
    }

    public void preInitialize() {
        Parallel.forEach(moduleMap.values(), (IModule module) -> {
            module.preInitialize();
        });
    }

    public void initialize(){
        for (IModule module : moduleMap.values()){
            module.initialize(getMod());
        }
    }

    public void postInitialize() {
        for (IModule module : moduleMap.values()){
            module.postInitialize();
        }
    }
}
