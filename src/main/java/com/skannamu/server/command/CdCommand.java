package com.skannamu.server.command;

import com.skannamu.server.ServerCommandProcessor;
import com.skannamu.server.TerminalCommands;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;

public class CdCommand implements ICommand {

    @Override
    public String getName() {return "cd";}
    @Override
    public String getUsage() {
        return "Usage: cd <directory_path>\n" +
                "Changes the current working directory.\n" +
                "Special argument:\n" +
                "  ..  : Moves up to the parent directory.";
    }

    @Override
    public String execute(ServerPlayerEntity player, List<String> optionsList, String remainingArgument) {
        if (remainingArgument.isBlank()) {
            return "Error: Usage: cd <directory_path>. Type 'cd -h' for help.";
        }

        ServerCommandProcessor.PlayerState state = TerminalCommands.getPlayerState(player.getUuid());
        String currentPath = state.getCurrentPath();
        String targetPath;

        if (remainingArgument.equals("..")) {
            if (currentPath.equals("/")) {
                return "Cannot move up from root directory: " + currentPath;
            }

            String path = currentPath;
            int lastSlash = path.lastIndexOf('/');
            targetPath = (lastSlash <= 0) ? "/" : path.substring(0, lastSlash);

        }
        else { targetPath = TerminalCommands.getAbsolutePath(player, remainingArgument); }

        targetPath = TerminalCommands.normalizePath(targetPath);

        if (TerminalCommands.FAKE_DIRECTORIES.containsKey(targetPath)) {
            state.setCurrentPath(targetPath);
            return "Directory changed to: " + targetPath;
        } else {
            if (TerminalCommands.FAKE_FILESYSTEM.containsKey(targetPath)) {
                return "Error: Cannot change directory to a file: " + remainingArgument;
            }
            return "Error: Directory not found: " + remainingArgument;
        }
    }
}