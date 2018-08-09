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
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            File file = getFile(name);

            file.getParentFile().mkdirs();

            if (!file.exists()) {
                file.createNewFile();
            }

            try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file))) {
                byte[] readBuffer = new byte[2048];
                int readedLength;
                while ((readedLength = input.read(readBuffer, 0, readBuffer.length)) != -1) {
                    output.write(readBuffer, 0, readedLength);
                }
            }

            return output.toByteArray();
        }
    }
}
