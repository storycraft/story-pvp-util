package com.storycraft.pvputil.config;

import net.minecraftforge.fml.client.config.IConfigElement;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface IConfigFile {
    void load(InputStream is) throws IOException;
    void save(OutputStream os) throws IOException;

    List<IConfigElement> getForgeConfigElement();
    boolean updateFromForgeConfig();
}
