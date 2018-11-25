package com.storycraft.pvputil.module;

import com.storycraft.pvputil.PvpUtil;

public interface IModule {
    void preInitialize();
    void initialize(PvpUtil mod);
    default void postInitialize() {

    }
}
