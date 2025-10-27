package com.skannamu.server.command;

import com.skannamu.server.FilesystemService;
import com.skannamu.server.TerminalCommands;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;

public class MkdirCommand implements ICommand {

    @Override
    public String getName() {
        return "mkdir";
    }

    @Override
    public String getUsage() {
        return "Usage: mkdir <directory_path>\n" +
                "Creates a new, player-owned directory in the specified path.";
    }

    @Override
    public String execute(ServerPlayerEntity player, List<String> optionsList, String remainingArgument) {

        FilesystemService fileService = TerminalCommands.getFileService();
        if (fileService == null) {
            return "Error: File system service not initialized.";
        }

        if (remainingArgument.isBlank()) {
            return "Error: Usage: mkdir <directory_path>. Type 'mkdir -h' for help.";
        }

        String targetPath = TerminalCommands.getAbsolutePath(player, remainingArgument);
        targetPath = TerminalCommands.normalizePath(targetPath);

        String result = fileService.createDirectory(player.getUuid(), targetPath);

        if (result.startsWith("Error:")) {
            return result; // FilesystemService에서 반환된 에러 메시지 출력
        }

        return "";
    }
}