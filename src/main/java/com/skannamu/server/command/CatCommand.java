package com.skannamu.server.command;

import com.skannamu.server.TerminalCommands;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;

public class CatCommand implements ICommand {

    @Override
    public String getName() { return "cat";}
    @Override
    public String getUsage() {
        return "Usage: cat <file_path>\n" +
                "Displays the content of the specified file.";
    }
    @Override
    public String execute(ServerPlayerEntity player, List<String> optionsList, String remainingArgument) {
        if (remainingArgument.isBlank()) {
            return "Error: Usage: cat <file_path>. Type 'cat -h' for help.";
        }
        String targetPath = TerminalCommands.getAbsolutePath(player, remainingArgument);

        if (TerminalCommands.FAKE_FILESYSTEM.containsKey(targetPath)) {

            if (TerminalCommands.FAKE_DIRECTORIES.containsKey(targetPath)) {
                return "Error: Cannot cat a directory: " + targetPath;
            }
            return TerminalCommands.FAKE_FILESYSTEM.get(targetPath);
        }
        return "Error: File not found or unreadable: " + remainingArgument;
    }
}