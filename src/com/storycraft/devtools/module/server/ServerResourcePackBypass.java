package com.storycraft.devtools.module.server;

import com.google.common.util.concurrent.ListenableFuture;
import com.storycraft.devtools.DevTools;
import com.storycraft.devtools.config.json.JsonConfigEntry;
import com.storycraft.devtools.module.IModule;
import com.storycraft.devtools.util.reflect.Reflect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.HttpUtil;

import java.io.File;
import java.sql.Ref;
import java.util.concurrent.Callable;

public class ServerResourcePackBypass implements IModule {

    public static final String OPTION_CATEGORY = "server";

    private static final Reflect.WrappedField<IMetadataSerializer, Minecraft> metadataSerializer_;
    private static Reflect.WrappedField<ResourcePackRepository, Minecraft> mcResourcePackRepository;
    private static Reflect.WrappedField<File, ResourcePackRepository> dirServerResourcepacks;

    private Minecraft minecraft;
    private DevTools mod;

    static {
        mcResourcePackRepository = Reflect.getField(Minecraft.class, "mcResourcePackRepository", "field_110448_aq");
        metadataSerializer_ = Reflect.getField(Minecraft.class, "metadataSerializer_", "field_110452_an");
        dirServerResourcepacks = Reflect.getField(ResourcePackRepository.class, "dirServerResourcepacks", "field_148534_e");
    }

    @Override
    public void preInitialize() {
        this.minecraft = Minecraft.getMinecraft();
    }

    @Override
    public void initialize(DevTools mod) {
        this.mod = mod;

        isModEnabled();
    }

    @Override
    public void postInitialize() {
        ResourcePackRepository repo = minecraft.getResourcePackRepository();
        mcResourcePackRepository.set(minecraft, new CustomResourcePackRepository(repo.getDirResourcepacks(), dirServerResourcepacks.get(repo), minecraft.mcDefaultResourcePack, metadataSerializer_.get(minecraft), minecraft.gameSettings));
    }

    public boolean isModEnabled() {
        if (!getModuleConfigEntry().contains("bypass_resourcepack_download"))
            getModuleConfigEntry().set("bypass_resourcepack_download", false);

        return getModuleConfigEntry().get("bypass_resourcepack_download").getAsBoolean();
    }

    public JsonConfigEntry getModuleConfigEntry(){
        if (!mod.getDefaultConfig().contains(OPTION_CATEGORY)) {
            mod.getDefaultConfig().set(OPTION_CATEGORY, new JsonConfigEntry());
        }

        return mod.getDefaultConfig().getObject(OPTION_CATEGORY);
    }

    public class CustomResourcePackRepository extends ResourcePackRepository {

        public CustomResourcePackRepository(File dirResourcepacksIn, File dirServerResourcepacksIn, IResourcePack rprDefaultResourcePackIn, IMetadataSerializer rprMetadataSerializerIn, GameSettings settings) {
            super(dirResourcepacksIn, dirServerResourcepacksIn, rprDefaultResourcePackIn, rprMetadataSerializerIn, settings);
        }

        @Override
        public ListenableFuture<Object> downloadResourcePack(String url, String hash) {
            if (isModEnabled()) {
                //L
                return HttpUtil.field_180193_a.submit(new Callable<Object>() {
                    @Override
                    public Object call() {
                        return null;
                    }
                });
            }
            else {
                return super.downloadResourcePack(url, hash);
            }
        }
    }

}
