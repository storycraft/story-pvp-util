package com.storycraft.devtools.storage;

import com.storycraft.devtools.IStoryMod;

import java.io.*;
import java.nio.file.Path;

public class ModDataStorage extends Storage<byte[]> {

    private IStoryMod mod;
    private Path dataPath;

    public ModDataStorage(IStoryMod mod, Path dataPath){
        this.mod = mod;
        this.dataPath = dataPath;
    }

    public IStoryMod getMod() {
        return mod;
    }

    public Path getDataPath(){
        return dataPath;
    }

    protected File getFile(String name){
        return getDataPath().resolve(name).toFile();
    }

    @Override
    public boolean saveSync(byte[] data, String name) throws IOException {
        BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(getFile(name)));

        writer.write(data);
        writer.close();

        return true;
    }

    @Override
    public byte[] getSync(String name) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        File file = getFile(name);

        if (!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            file.createNewFile();
        }

        BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));

        byte[] readBuffer = new byte[2048];
        while (input.read(readBuffer, 0, readBuffer.length) != -1) {
            output.write(readBuffer);
        }

        input.close();
        output.close();

        return output.toByteArray();
    }
}
