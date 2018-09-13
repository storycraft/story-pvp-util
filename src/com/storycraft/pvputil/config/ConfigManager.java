package com.storycraft.pvputil.config;

import com.storycraft.pvputil.IStoryMod;
import com.storycraft.pvputil.storage.Storage;
import com.storycraft.pvputil.util.AsyncTask;
import com.storycraft.pvputil.util.Parallel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private Map<String, IConfigFile> configFileMap;

    private IStoryMod mod;

    public ConfigManager(IStoryMod mod) {
        this.mod = mod;

        this.configFileMap = new HashMap<>();
    }

    public AsyncTask<Void> addConfigFile(String name, IConfigFile configFile) {
        return new AsyncTask<>(new AsyncTask.AsyncCallable<Void>() {
            @Override
            public Void get() {
                if (hasConfigFile(name))
                    return null;

                try {
                    configFile.load(new ByteArrayInputStream(getDataStorage().getSync(name)));
                    getConfigFileMap().put(name, configFile);
                } catch (IOException e) {
                    getMod().getLogger().warning(name + " 을 로드 중 오류가 발생했습니다. " + e.getLocalizedMessage());
                }

                return null;
            }
        });
    }

    public void saveAll() {
        Parallel.forEach(getConfigFileMap().keySet(), new Parallel.Operation<String>() {
            @Override
            public void run(String name) {
                try {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();

                    IConfigFile configFile = getConfigFileMap().get(name);
                    configFile.save(output);

                    getDataStorage().saveSync(output.toByteArray(), name);
                } catch (IOException e) {
                    getMod().getLogger().warning(name + " 저장 중 오류가 발생 했습니다 " + e.getLocalizedMessage());
                }
            }
        });
    }

    public Storage<byte[]> getDataStorage(){
        return getMod().getModDataStorage();
    }

    public boolean hasConfigFile(String name) {
        return getConfigFileMap().containsKey(name);
    }

    public IConfigFile getConfigFile(String name) {
        return getConfigFileMap().get(name);
    }

    protected Map<String, IConfigFile> getConfigFileMap() {
        return configFileMap;
    }

    public IStoryMod getMod() {
        return mod;
    }
}
