package com.storycraft.pvputil.command;

import net.minecraft.entity.player.EntityPlayer;

public interface ICommand {
    String[] getAliases();
    void onCommand(EntityPlayer player, String[] args);
}
