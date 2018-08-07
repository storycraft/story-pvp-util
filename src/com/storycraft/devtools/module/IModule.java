package com.storycraft.devtools.module;

import com.storycraft.devtools.DevTools;

public interface IModule {
    void preInitialize();
    void initialize(DevTools mod);
    default void postInitialize() {

    }
}
