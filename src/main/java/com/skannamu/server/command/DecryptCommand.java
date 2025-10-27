package com.skannamu.server.command;

import com.skannamu.server.TerminalCommands;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class DecryptCommand implements ICommand {

    private static final String MODE_BASE64 = "base64";
    private static final String MODE_ROT47 = "rot47";
    private static final String MODE_HEX = "hex";
    private static final String MODE_ASCII = "ascii";

    @Override
    public String getName() {
        return "decrypt";
    }

    @Override
    public String getUsage() {
        return "Usage: decrypt -m <mode> [string]\n" +
                "Decodes an encrypted string using the specified mode.\n\n" +
                "Available Modes (-m):\n" +
                "  base64   : Standard Base64 decoding.\n" +
                "  rot47    : ROT47 (Caesar Cipher variant) decoding.\n" +
                "  hex      : Hexadecimal string to ASCII decoding.\n" +
                "  ascii    : Space-separated ASCII decimal values to characters.";
    }

    @Override
    public String execute(ServerPlayerEntity player, List<String> optionsList, String remainingArgument) {
        if (remainingArgument.isBlank()) {
            return "Error: Decoding string cannot be blank.";
        }

        if (!optionsList.contains("m")) {
            return "Error: Missing decryption mode. Use '-m base64' or 'decrypt -h' for usage.";
        }

        String[] parts = remainingArgument.trim().split("\\s+", 2);
        String mode = parts[0].toLowerCase(); // 첫 번째 단어는 모드
        String dataString = parts.length > 1 ? parts[1].trim() : ""; // 두 번째 이후는 데이터 문자열
        if (dataString.isBlank()) {
            return "Error: Decryption mode specified, but data string is missing.";
        }
        try {
            switch (mode) {
                case MODE_BASE64:
                    return decodeBase64(dataString);
                case MODE_ROT47:
                    return decodeRot47(dataString);
                case MODE_HEX:
                    return decodeHex(dataString);
                case MODE_ASCII:
                    return decodeAscii(dataString);
                default:
                    return "Error: Unknown decryption mode '" + mode + "'. Type 'decrypt -h' for supported modes.";
            }
        } catch (IllegalArgumentException e) {
            return "Error: Decoding failed. Invalid format for mode '" + mode + "'.";
        } catch (Exception e) {
            return "Error: An unexpected error occurred during decryption.";
        }
    }
    private String decodeBase64(String encodedString) {
        String cleanString = encodedString.replaceAll("\\s", "");
        byte[] decodedBytes = Base64.getDecoder().decode(cleanString);
        return "Decryption successful (Base64):\n" + new String(decodedBytes);
    }
    private String decodeRot47(String encodedString) {
        StringBuilder decoded = new StringBuilder();
        for (char c : encodedString.toCharArray()) {
            if (c >= '!' && c <= '~') {
                decoded.append((char) ('!' + (c - '!' + 47) % 94));
            } else {
                decoded.append(c);
            }
        }
        return "Decryption successful (ROT47):\n" + decoded.toString();
    }

    private String decodeHex(String encodedString) {
        String hexString = encodedString.replaceAll("\\s", "");
        if (hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string length must be even.");
        }
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hexString.length(); i += 2) {
            String str = hexString.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return "Decryption successful (Hexadecimal):\n" + output.toString();
    }

    private String decodeAscii(String encodedString) {
        String[] asciiValues = encodedString.trim().split("\\s+");
        String decoded = java.util.Arrays.stream(asciiValues)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .mapToObj(i -> (char) i)
                .map(String::valueOf)
                .collect(Collectors.joining());

        return "Decryption successful (ASCII):\n" + decoded;
    }
}