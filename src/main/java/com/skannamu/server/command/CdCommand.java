package com.skannamu.server.command;

import com.skannamu.server.FilesystemService;
import com.skannamu.server.ServerCommandProcessor;
import com.skannamu.server.TerminalCommands;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;

public class CdCommand implements ICommand {

    @Override
    public String getName() {
        return "cd";
    }

    @Override
    public String getUsage() {
        return "Usage: cd <directory_path>\n" +
                "Changes the current working directory.\n" +
                "Special argument:\n" +
                "  ..  : Moves up to the parent directory.";
    }

    @Override
    public String execute(ServerPlayerEntity player, List<String> optionsList, String remainingArgument) {

        FilesystemService fileService = TerminalCommands.getFileService();
        if (fileService == null) {
            return "Error: File system service not initialized.";
        }

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

            int lastSlash = currentPath.lastIndexOf('/');
            targetPath = (lastSlash <= 0) ? "/" : currentPath.substring(0, lastSlash);

        }
        else {
            targetPath = TerminalCommands.getAbsolutePath(player, remainingArgument);
        }

        targetPath = TerminalCommands.normalizePath(targetPath);

        String pathType = fileService.checkPathType(player.getUuid(), targetPath);

        if (pathType.equals("dir")) {
            // 1. 서버의 PlayerState에 경로를 저장합니다.
            state.setCurrentPath(targetPath);

            // 2. 클라이언트에게 새로운 경로를 전달하여 프롬프트를 업데이트하도록 합니다.
            TerminalCommands.sendCwdUpdate(player, targetPath);

            // 3. 화면에는 아무것도 출력하지 않습니다.
            return "";

        }

        else if (pathType.equals("file")) {
            return "Error: Cannot change directory to a file: " + remainingArgument;
        }

        else {
            return "Error: Directory not found: " + remainingArgument;
        }
    }
}