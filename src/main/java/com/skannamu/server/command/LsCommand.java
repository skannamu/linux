package com.skannamu.server.command;

import com.skannamu.server.FilesystemService;
import com.skannamu.server.TerminalCommands;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;

public class LsCommand implements ICommand {

    @Override
    public String getName() {
        return "ls";
    }

    @Override
    public String getUsage() {
        return "Usage: ls [directory_path]\n" +
                "Lists the contents of the specified directory. If no path is provided, lists the current directory.";
    }

    @Override
    public String execute(ServerPlayerEntity player, List<String> optionsList, String remainingArgument) {

        FilesystemService fileService = TerminalCommands.getFileService();
        if (fileService == null) {
            return "Error: File system service not initialized.";
        }

        // 1. 대상 경로 결정 및 정규화
        String targetPath;
        if (remainingArgument.isBlank()) {
            targetPath = TerminalCommands.getCurrentPlayerPath(player);
        } else {
            // 절대 경로를 얻습니다. (상대 경로인 경우 현재 경로를 기준으로 해석)
            targetPath = TerminalCommands.getAbsolutePath(player, remainingArgument);
        }

        // 2. FilesystemService를 통해 디렉토리 내용 조회
        // 이 메서드는 DB와 전역 시스템을 모두 검색하며, 오류 메시지를 포함한 결과를 반환합니다.
        String contentsResult = fileService.getDirectoryContents(player.getUuid(), targetPath);

        // 3. 결과 처리
        if (contentsResult.startsWith("Error:")) {
            return contentsResult; // FilesystemService에서 반환된 에러 메시지를 그대로 출력
        }

        StringBuilder output = new StringBuilder();
        output.append("Contents of ").append(targetPath).append(":\n");

        if (contentsResult.isBlank()) {
            return output.append("Empty directory.").toString();
        }

        // contentsResult는 이미 \n으로 구분된 목록이거나 공백입니다.
        String[] contents = contentsResult.split("[\\n]+");

        for (String item : contents) {
            if (!item.isBlank()) {
                // 4. (선택적 개선) 항목의 타입에 따라 * 표시 등을 추가할 수 있으나,
                // 현재는 간단히 항목 이름만 출력합니다. (e.g., if (fileService.checkPathType(...) == "dir") output.append("/");)
                output.append("  ").append(item).append("\n");
            }
        }

        return output.toString().trim();
    }
}