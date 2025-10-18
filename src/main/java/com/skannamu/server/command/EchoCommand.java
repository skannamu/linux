package com.skannamu.server.command;

import com.skannamu.server.TerminalCommands;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;

public class EchoCommand implements ICommand {

    @Override
    public String getName() {
        return "echo";
    }

    @Override
    public String getUsage() {
        return "Usage: echo [OPTIONS] [STRING] [OP] [FILE_PATH]\n" +
                "Displays a line of text. Supports redirection.\n" +
                "  -h: Display this help message.\n" +
                "  -n: Do not output the trailing newline.\n" + // 리눅스 echo의 일반적인 옵션도 추가 가능
                "  > : Overwrite the file with STRING.\n" +
                "  >>: Append STRING to the end of the file.";
    }

    @Override
    public String execute(ServerPlayerEntity player, List<String> optionsList, String remainingArgument) {
        if (optionsList.contains("-h")) {
            return getUsage();
        }

        CommandParsingResult result = parseEchoArguments(remainingArgument);

        if (result.operator == null) {
            return result.textToEcho.trim();
        }

        if (result.filePath == null || result.filePath.isBlank()) {
            return "Error: Missing file path for redirection operator '" + result.operator + "'.";
        }

        String currentDirectory = TerminalCommands.getPlayerState(player.getUuid()).getCurrentPath();

        String message = TerminalCommands.getFileService().redirectOutput(
                currentDirectory,
                result.filePath,
                result.textToEcho.trim(),
                result.operator
        );

        return message;
    }

    private CommandParsingResult parseEchoArguments(String input) {
        String textToEcho = input;
        String operator = null;
        String filePath = null;
        int appendIndex = input.lastIndexOf(">>");
        if (appendIndex != -1) {
            operator = ">>";
            textToEcho = input.substring(0, appendIndex).trim();
            filePath = input.substring(appendIndex + 2).trim();
        }
        // > (overwrite) 체크
        else {
            int overwriteIndex = input.lastIndexOf(">");
            if (overwriteIndex != -1) {
                operator = ">";
                textToEcho = input.substring(0, overwriteIndex).trim();
                filePath = input.substring(overwriteIndex + 1).trim();
            }
        }

        if (textToEcho.startsWith("\"") && textToEcho.endsWith("\"")) {
            textToEcho = textToEcho.substring(1, textToEcho.length() - 1);
        }

        return new CommandParsingResult(textToEcho, operator, filePath);
    }

    // 파싱 결과를 담을 간단한 내부 클래스
    private static class CommandParsingResult {
        String textToEcho;
        String operator; // ">" or ">>" or null
        String filePath;

        public CommandParsingResult(String text, String op, String path) {
            this.textToEcho = text;
            this.operator = op;
            this.filePath = path;
        }
    }
}