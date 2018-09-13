package com.storycraft.pvputil.command;

import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CommandListener {
    private CommandManager manager;

    public CommandListener(CommandManager manager){
        this.manager = manager;
    }

    public CommandManager getManager() {
        return manager;
    }

    @SubscribeEvent
    public void onCommand(ServerChatEvent e){
        if (e.isCanceled() || !e.isCancelable() || e.message == null || !e.message.startsWith(CommandManager.COMMAND_PREFIX))
            return;

        e.setCanceled(true);

        String msg = e.message.substring(CommandManager.COMMAND_PREFIX.length());
        //PREFIX 제거 후 공백으로 나눔
        int spaceIndex = msg.indexOf(" ");
        String commandStr = msg.substring(CommandManager.COMMAND_PREFIX.length(), spaceIndex != -1 ? spaceIndex - 1 : msg.length());

        ICommand command = getManager().getCommand(commandStr);

        if (command == null){
            return;
        }

        String[] args = msg.substring(commandStr.length()).split(" ");

        command.onCommand(e.player, args);
    }
}
