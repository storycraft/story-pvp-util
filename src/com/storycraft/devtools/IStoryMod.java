package com.storycraft.devtools;

import com.storycraft.devtools.command.CommandManager;
import com.storycraft.devtools.config.ConfigManager;
import com.storycraft.devtools.config.IConfigFile;
import com.storycraft.devtools.registry.RegistryManager;
import com.storycraft.devtools.storage.Storage;

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
}
