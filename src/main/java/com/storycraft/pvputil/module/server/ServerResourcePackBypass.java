package com.storycraft.pvputil.module.server;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.storycraft.pvputil.PvpUtil;
import com.storycraft.pvputil.config.json.JsonConfigEntry;
import com.storycraft.pvputil.module.IModule;
import com.storycraft.pvputil.util.reflect.Reflect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenWorking;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.HttpUtil;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

public class ServerResourcePackBypass implements IModule {

    public static final String OPTION_CATEGORY = "server";

    private static Reflect.WrappedField<ResourcePackRepository, Minecraft> mcResourcePackRepository;
    private static Reflect.WrappedField<File, ResourcePackRepository> dirServerResourcepacks;
    private static Reflect.WrappedField<ReentrantLock, ResourcePackRepository> lock;
    private static Reflect.WrappedField<ListenableFuture<Object>, ResourcePackRepository> downloadingPacks;

    private static Reflect.WrappedMethod<Void, ResourcePackRepository> deleteOldServerResourcesPacks;

    private Minecraft minecraft;
    private PvpUtil mod;

    static {
        mcResourcePackRepository = Reflect.getField(Minecraft.class, "mcResourcePackRepository", "field_110448_aq");
        dirServerResourcepacks = Reflect.getField(ResourcePackRepository.class, "dirServerResourcepacks", "field_148534_e");
        lock = Reflect.getField(ResourcePackRepository.class, "lock", "field_177321_h");
        downloadingPacks = Reflect.getField(ResourcePackRepository.class, "downloadingPacks", "field_177322_i");

        deleteOldServerResourcesPacks = Reflect.getMethod(ResourcePackRepository.class, "deleteOldServerResourcesPacks", "func_183028_i");
    }

    @Override
    public void preInitialize() {
        this.minecraft = Minecraft.getMinecraft();
    }

    @Override
    public void initialize(PvpUtil mod) {
        this.mod = mod;

        isModEnabled();
    }

    @Override
    public void postInitialize() {
        ResourcePackRepository repo = minecraft.getResourcePackRepository();
        mcResourcePackRepository.set(minecraft, new CustomResourcePackRepository(repo.getDirResourcepacks(), dirServerResourcepacks.get(repo), minecraft.mcDefaultResourcePack, minecraft.getResourcePackRepository().rprMetadataSerializer, minecraft.gameSettings));
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

        private final Logger LOGGER = LogManager.getLogger();
        private final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
        
        public CustomResourcePackRepository(File dirResourcepacksIn, File dirServerResourcepacksIn, IResourcePack rprDefaultResourcePackIn, MetadataSerializer rprMetadataSerializerIn, GameSettings settings) {
            super(dirResourcepacksIn, dirServerResourcepacksIn, rprDefaultResourcePackIn, rprMetadataSerializerIn, settings);
        }

        @Override
        public ListenableFuture<Object> downloadResourcePack(String url, String hash)
        {
            String s = DigestUtils.sha1Hex(url);
            final String s1 = SHA1.matcher(hash).matches() ? hash : "";
            final File file1 = new File(ServerResourcePackBypass.dirServerResourcepacks.get(this), s);

            ReentrantLock lock = ServerResourcePackBypass.lock.get(this);
    
            try
            {
                this.clearResourcePack();
    
                if (file1.exists())
                {
                    if (this.checkHash(s1, file1))
                    {
                        ListenableFuture listenablefuture1 = this.setServerResourcePack(file1);
                        return listenablefuture1;
                    }
    
                    LOGGER.warn("Deleting file {}", (Object)file1);
                    FileUtils.deleteQuietly(file1);
                }
    
                deleteOldServerResourcesPacks.invoke(this);
                final GuiScreenWorking guiscreenworking = new GuiScreenWorking();
                Map<String, String> map = getDownloadHeaders();
                final Minecraft minecraft = Minecraft.getMinecraft();
                Futures.getUnchecked(minecraft.addScheduledTask(new Runnable()
                {
                    public void run()
                    {
                        minecraft.displayGuiScreen(guiscreenworking);
                    }
                }));
                final SettableFuture<Object> settablefuture = SettableFuture.<Object>create();

                ListenableFuture downloadingPacks = HttpUtil.downloadResourcePack(file1, url, map, 52428800, guiscreenworking, minecraft.getProxy());

                ServerResourcePackBypass.downloadingPacks.set(this, downloadingPacks);

                Futures.addCallback(downloadingPacks, new FutureCallback<Object>()
                {
                    public void onSuccess(@Nullable Object p_onSuccess_1_)
                    {
                        if (CustomResourcePackRepository.this.checkHash(s1, file1))
                        {
                            if (isModEnabled())
                                CustomResourcePackRepository.this.setServerResourcePack(file1);
                            settablefuture.set((Object)null);
                        }
                        else
                        {
                            LOGGER.warn("Deleting file {}", (Object)file1);
                            FileUtils.deleteQuietly(file1);
                        }
                    }
                    public void onFailure(Throwable p_onFailure_1_)
                    {
                        FileUtils.deleteQuietly(file1);
                        settablefuture.setException(p_onFailure_1_);
                    }
                });
                
                return downloadingPacks;
            }
            finally
            {
                lock.unlock();
            }
        }

        private boolean checkHash(String p_190113_1_, File p_190113_2_)
        {
            try
            {
                String s = DigestUtils.sha1Hex((InputStream)(new FileInputStream(p_190113_2_)));
    
                if (p_190113_1_.isEmpty())
                {
                    LOGGER.info("Found file {} without verification hash", (Object)p_190113_2_);
                    return true;
                }
    
                if (s.toLowerCase(java.util.Locale.ROOT).equals(p_190113_1_.toLowerCase(java.util.Locale.ROOT)))
                {
                    LOGGER.info("Found file {} matching requested hash {}", p_190113_2_, p_190113_1_);
                    return true;
                }
    
                LOGGER.warn("File {} had wrong hash (expected {}, found {}).", p_190113_2_, p_190113_1_, s);
            }
            catch (IOException ioexception)
            {
                LOGGER.warn("File {} couldn't be hashed.", p_190113_2_, ioexception);
            }
    
            return false;
        }
    }

}
