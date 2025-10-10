package com.skannamu.server.command;

import com.skannamu.server.TerminalCommands;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;

public class PwdCommand implements ICommand {

    @Override
    public String getName() {return "pwd";}
    @Override
    public String getUsage() {
        return "Usage: pwd\n" +
                "Prints the full path of the current working directory.";
    }
    @Override
    public String execute(ServerPlayerEntity player, List<String> optionsList, String remainingArgument) {
        String currentPath = TerminalCommands.getCurrentPlayerPath(player);
        return currentPath;
    }
}