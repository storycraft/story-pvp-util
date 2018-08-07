package com.storycraft.devtools.config.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.storycraft.devtools.config.IConfigEntry;

public class JsonConfigEntry implements IConfigEntry<JsonConfigEntry> {

    private JsonObject jsonObject;

    public JsonConfigEntry() {
        this.jsonObject = new JsonObject();
    }

    public JsonConfigEntry(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }

    protected void setJsonObject(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    @Override
    public void set(String key, JsonConfigEntry value) {
        getJsonObject().add(key, value.getJsonObject());
    }

    @Override
    public void set(String key, byte value) {
        getJsonObject().addProperty(key, value);
    }

    @Override
    public void set(String key, int value) {
        getJsonObject().addProperty(key, value);
    }

    @Override
    public void set(String key, short value) {
        getJsonObject().addProperty(key, value);
    }

    @Override
    public void set(String key, long value) {
        getJsonObject().addProperty(key, value);
    }

    @Override
    public void set(String key, double value) {
        getJsonObject().addProperty(key, value);
    }

    @Override
    public void set(String key, float value) {
        getJsonObject().addProperty(key, value);
    }

    @Override
    public void set(String key, boolean value) {
        getJsonObject().addProperty(key, value);
    }

    @Override
    public void set(String key, String value) {
        getJsonObject().addProperty(key, value);
    }

    @Override
    public void set(String key, Object value) {
        set(key, value.toString());
    }

    @Override
    public boolean contains(String key) {
        return getJsonObject().has(key);
    }

    @Override
    public JsonElement get(String key) {
        return getJsonObject().get(key);
    }

    @Override
    public JsonConfigEntry getObject(String key) {
        return new JsonConfigEntry(get(key).getAsJsonObject());
    }
}
