package com.storycraft.devtools.module;

import com.storycraft.devtools.DevTools;
import com.storycraft.devtools.module.fullscreen.FullscreenCursorConfine;
import com.storycraft.devtools.module.hitsound.PlayerHitSound;
import com.storycraft.devtools.module.render.ClientPlayerNameTag;
import com.storycraft.devtools.module.render.DynamicBoundingBox;
import com.storycraft.devtools.module.render.LabelBoxRenderToggle;
import com.storycraft.devtools.module.resourcepack.ServerResourcePackBypass;
import com.storycraft.devtools.module.screenshot.AsyncScreenshot;
import com.storycraft.devtools.module.session.SessionRefresh;
import com.storycraft.devtools.util.Parallel;

import java.util.HashMap;
import java.util.Map;

public class ModuleManager {

    private Map<String, IModule> moduleMap;

    private DevTools mod;

    public ModuleManager(DevTools mod){
        this.mod = mod;
        this.moduleMap = new HashMap<>();

        addDefaultModule();
    }

    private void addDefaultModule() {
        addModule("async_screenshot", new AsyncScreenshot());

        addModule("hitsound", new PlayerHitSound());

        addModule("session_refresh", new SessionRefresh());

        addModule("fullscreen_cursor", new FullscreenCursorConfine());

        addModule("client_player_nametag", new ClientPlayerNameTag());
        addModule("label_box_render_toggle", new LabelBoxRenderToggle());
        addModule("dynamic_boundingbox", new DynamicBoundingBox());

        addModule("server_resourcepacks_bypass", new ServerResourcePackBypass());
    }

    public DevTools getMod() {
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

        if (getMod().isInited())
            module.preInitialize();

        if (getMod().isInited())
            module.initialize(getMod());


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
