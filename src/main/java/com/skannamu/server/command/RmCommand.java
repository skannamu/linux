package com.skannamu.server.command;

import com.skannamu.server.FilesystemService;
import com.skannamu.server.TerminalCommands;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;

public class RmCommand implements ICommand {

    @Override
    public String getName() {
        return "rm";
    }

    @Override
    public String getUsage() {
        return "Usage: rm <file_or_directory_path>\n" +
                "Deletes a player-owned file or an empty directory.\n" +
                "Note: Cannot delete system files or non-empty directories.";
    }

    @Override
    public String execute(ServerPlayerEntity player, List<String> optionsList, String remainingArgument) {

        FilesystemService fileService = TerminalCommands.getFileService();
        if (fileService == null) {
            return "Error: File system service not initialized.";
        }

        if (remainingArgument.isBlank()) {
            return "Error: Usage: rm <file_or_directory_path>. Type 'rm -h' for help.";
        }

        String targetPath = TerminalCommands.getAbsolutePath(player, remainingArgument);
        targetPath = TerminalCommands.normalizePath(targetPath);

        String result = fileService.deleteEntry(player.getUuid(), targetPath);

        if (result.startsWith("Error:")) {
            return result; // FilesystemService에서 반환된 에러 메시지 출력
        }

        return "";
    }
}