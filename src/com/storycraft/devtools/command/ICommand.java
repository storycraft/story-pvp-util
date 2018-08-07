package com.storycraft.devtools.command;

import net.minecraft.entity.player.EntityPlayer;

public interface ICommand {
    String[] getAliases();
    void onCommand(EntityPlayer player, String[] args);
}
