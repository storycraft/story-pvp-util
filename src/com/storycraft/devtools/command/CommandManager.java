package com.storycraft.devtools.command;

import com.storycraft.devtools.IStoryMod;
import net.minecraftforge.common.MinecraftForge;

import java.util.*;

public class CommandManager {
    public static String COMMAND_PREFIX = "/";

    public static String UNKNOWN_COMMAND = "알 수 없는 커맨드 입니다";

    private IStoryMod mod;
    private CommandListener listener;

    private Map<String[], ICommand> commandMap = new HashMap<>();

    public CommandManager(IStoryMod mod){
        this.mod = mod;
        this.listener = new CommandListener(this);
    }

    public void preInitialize() {

    }

    public void initialize() {
        MinecraftForge.EVENT_BUS.register(listener);
    }

    public ICommand getCommand(String str){
        for (String[] aliases : getCommandMap().keySet()){
            for (String alias : aliases){
                if (str.equals(alias))
                    return getCommandMap().get(aliases);
            }
        }

        return null;
    }

    public void addCommand(ICommand command){
        getCommandMap().put(command.getAliases(), command);
    }

    public IStoryMod getMod() {
        return mod;
    }

    protected Map<String[], ICommand> getCommandMap() {
        return commandMap;
    }
}
