package com.skannamu.server.command;

import com.skannamu.server.TerminalCommands;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;

public class LsCommand implements ICommand {

    @Override
    public String getName() {return "ls";}

    @Override
    public String getUsage() {
        return "Usage: ls [directory_path]\n" +
                "Lists the contents of the specified directory. If no path is provided, lists the current directory.";
    }
    @Override
    public String execute(ServerPlayerEntity player, List<String> optionsList, String remainingArgument) {
        String targetPath;
        if (remainingArgument.isBlank()) {
            targetPath = TerminalCommands.getCurrentPlayerPath(player);
        } else {
            targetPath = TerminalCommands.getAbsolutePath(player, remainingArgument);
        }
        targetPath = TerminalCommands.normalizePath(targetPath);

        if (TerminalCommands.FAKE_DIRECTORIES.containsKey(targetPath)) {

            String contentsString = TerminalCommands.FAKE_DIRECTORIES.get(targetPath);

            StringBuilder output = new StringBuilder();
            output.append("Contents of ").append(targetPath).append(":\n");

            if (contentsString == null || contentsString.isBlank()) {
                return output.append("Empty directory.").toString();
            }

            String[] contents = contentsString.split("[\\s\\n]+");

            for (String item : contents) {
                if (!item.isBlank()) {
                    output.append("  ").append(item).append("\n");
                }
            }

            return output.toString().trim();

        } else {
            if (TerminalCommands.FAKE_FILESYSTEM.containsKey(targetPath)) {
                return "Error: '" + targetPath + "' is a file, not a directory.";
            }
            return "Error: Directory or path not found: " + remainingArgument;
        }
    }
}