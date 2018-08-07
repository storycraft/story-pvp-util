package com.storycraft.devtools.config.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.storycraft.devtools.config.IConfigFile;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.ConfigGuiType;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonConfigFile extends JsonConfigEntry implements IConfigFile {

    private List<IConfigElement> lastestForgeConfig;

    public JsonConfigFile(){

    }

    @Override
    public void load(InputStream is) {
        try {
            setJsonObject(new JsonParser().parse(new InputStreamReader(is)).getAsJsonObject());
        } catch (Exception e) {
            //create new file when file is not exists
            setJsonObject(new JsonObject());
        }
    }

    @Override
    public void save(OutputStream os) throws IOException {
        try {
            os.write(getJsonObject().toString().trim().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<IConfigElement> getForgeConfigElement() {
        List<IConfigElement> list = lastestForgeConfig = new ArrayList<>();
        ConfigCategory category = toForgeConfigEntry("", getJsonObject(), null);

        for (ConfigCategory childCategory : category.getChildren()) {
            IConfigElement configElement = new ConfigElement(childCategory);

            list.add(configElement);
        }

        for (String propertyKey : category.getPropertyOrder()) {
            IConfigElement configElement = new ConfigElement(category.get(propertyKey));

            list.add(configElement);
        }

        return list;
    }

    @Override
    public boolean updateFromForgeConfig() {
        updateChild(this, lastestForgeConfig);

        return true;
    }

    private void updateChild(JsonConfigEntry entry, List<IConfigElement> list) {
        for (IConfigElement element : list) {
            if (element.isProperty()){
                String value = (String) element.get();
                if (element.getType() == ConfigGuiType.STRING)
                    entry.set(element.getName(), value);
                else if (element.getType() == ConfigGuiType.DOUBLE)
                    entry.set(element.getName(), Double.parseDouble(value));
                else if (element.getType() == ConfigGuiType.INTEGER)
                    entry.set(element.getName(), Integer.parseInt(value));
                else if (element.getType() == ConfigGuiType.BOOLEAN)
                    entry.set(element.getName(), Boolean.parseBoolean(value));
            }
            else {
                JsonConfigEntry childEntry;
                if (!entry.contains(element.getName()))
                    entry.set(element.getName(), childEntry = new JsonConfigEntry());
                else
                    childEntry = entry.getObject(element.getName());

                updateChild(childEntry, element.getChildElements());
            }
        }
    }

    private Property toForgeProperty(String name, JsonPrimitive primitive) {
        Property property = null;

        if (primitive.isNumber()) {
            double dVal = primitive.getAsDouble();
            double iVal = primitive.getAsInt();

            if (dVal != iVal)
                property = new Property(name, primitive.getAsString(), Property.Type.DOUBLE);
            else
                property = new Property(name, primitive.getAsString(), Property.Type.INTEGER);
        }
        else if (primitive.isBoolean()) {
            property = new Property(name, primitive.getAsString(), Property.Type.BOOLEAN);
        }
        else if (primitive.isString()) {
            property = new Property(name, primitive.getAsString(), Property.Type.STRING);
        }

        return property;
    }

    private ConfigCategory toForgeConfigEntry(String name, JsonObject object, ConfigCategory parent) {
        ConfigCategory category = new ConfigCategory(name, parent);

        for (Map.Entry<String, JsonElement> child : object.entrySet()) {
            String key = child.getKey();
            JsonElement element = child.getValue();

            if (element instanceof JsonPrimitive) {
                category.put(key, toForgeProperty(child.getKey(), (JsonPrimitive) element));
            }
            else if (element instanceof JsonObject) {
                toForgeConfigEntry(key, (JsonObject) element, category);
            }
        }

        return category;
    }
}
