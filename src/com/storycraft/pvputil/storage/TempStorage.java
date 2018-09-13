package com.storycraft.pvputil.storage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class TempStorage extends Storage<byte[]> {

    private Path tempPath;

    public TempStorage(){
        this(UUID.randomUUID().toString());
    }

    public TempStorage(String prefix){
        try {
            this.tempPath = Files.createTempDirectory(prefix);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected File getFile(String name){
        return tempPath.resolve(name).toFile();
    }

    public Path getPath() {
        return tempPath;
    }

    @Override
    public boolean saveSync(byte[] data, String name) throws IOException {
        File file = getFile(name);
        BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(file));

        writer.write(data);
        writer.close();

        return true;
    }

    @Override
    public byte[] getSync(String name) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        File file = getFile(name);
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
