package com.storycraft.devtools;

import com.storycraft.devtools.command.CommandManager;
import com.storycraft.devtools.config.ConfigManager;
import com.storycraft.devtools.config.json.JsonConfigFile;
import com.storycraft.devtools.module.ModuleManager;
import com.storycraft.devtools.registry.RegistryManager;
import com.storycraft.devtools.storage.ModDataStorage;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.logging.Logger;

@Mod(modid = "story-dev-tools", version = "0.1", clientSideOnly = true, guiFactory = "com.storycraft.devtools.config.ingame.InGameDefaultConfigFactory")
public class DevTools implements IStoryMod {

    private static DevTools instance;

    private static boolean isInited;
    private static boolean isLoaded;
    private static boolean isPostLoaded;
    private static ModMetadata metadata;
    private static JsonConfigFile defaultConfig;

    static {
        isInited = false;
        isLoaded = false;
        isPostLoaded = false;
    }

    public static boolean isModInited(){
        return isInited;
    }

    public static boolean isModLoaded(){
        return isLoaded;
    }

    public static DevTools getInstance(){
        return instance;
    }

    public static JsonConfigFile getDefaultGlobalConfig() {
        return defaultConfig;
    }

    public static ModMetadata getModMetadata() {
        return metadata;
    }

    private ConfigManager configManager;
    private RegistryManager registryManager;
    private CommandManager commandManager;

    private ModDataStorage modDataStorage;

    private Logger logger;
    private ModuleManager moduleManager;

    public DevTools() {
        this.instance = this;
        this.logger = Logger.getLogger("minecraft");
        this.moduleManager = new ModuleManager(this);
        this.registryManager = new RegistryManager(this);
        this.commandManager = new CommandManager(this);
    }

    @Mod.EventHandler
    public void preInitialize(FMLPreInitializationEvent e) {
        metadata = e.getModMetadata();
        isInited = true;
        this.modDataStorage = new ModDataStorage(this, e.getModConfigurationDirectory().toPath().resolve(getModName()));
        this.configManager = new ConfigManager(this);

        this.configManager.addConfigFile("config.json", defaultConfig = new JsonConfigFile()).getSync();

        getRegistryManager().preInitialize();
        getCommandManager().preInitialize();
        getModuleManager().preInitialize();
    }

    @Mod.EventHandler
    public void initialize(FMLInitializationEvent e) {
        isLoaded = true;

        getRegistryManager().initialize();
        getCommandManager().initialize();
        getModuleManager().initialize();
    }

    @Mod.EventHandler
    public void postInitialize(FMLLoadCompleteEvent e) {
        isPostLoaded = true;
        getModuleManager().postInitialize();
    }

    public JsonConfigFile getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public String getModId() {
        return getModMetadata().modId;
    }

    @Override
    public String getModName() {
        return getModMetadata().name;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public boolean isInited(){
        return isInited;
    }

    @Override
    public boolean isLoaded(){
        return isLoaded;
    }

    @Override
    public boolean isPostLoaded(){
        return isPostLoaded;
    }

    @Override
    public ModDataStorage getModDataStorage() {
        return modDataStorage;
    }

    @Override
    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public RegistryManager getRegistryManager() {
        return registryManager;
    }

    @Override
    public CommandManager getCommandManager() {
        return commandManager;
    }
}