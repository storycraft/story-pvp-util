package com.storycraft.pvputil;

import com.storycraft.pvputil.command.CommandManager;
import com.storycraft.pvputil.config.ConfigManager;
import com.storycraft.pvputil.config.IConfigFile;
import com.storycraft.pvputil.registry.RegistryManager;
import com.storycraft.pvputil.storage.Storage;

import java.util.logging.Logger;

public interface IStoryMod {

    String getModId();
    String getModName();

    Logger getLogger();

    Storage<byte[]> getModDataStorage();
    ConfigManager getConfigManager();
    RegistryManager getRegistryManager();
    CommandManager getCommandManager();

    IConfigFile getDefaultConfig();

    boolean isInited();
    boolean isLoaded();
    boolean isPostLoaded();
}
