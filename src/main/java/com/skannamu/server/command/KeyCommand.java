package com.skannamu.server.command;

import com.skannamu.server.ServerCommandProcessor;
import com.skannamu.server.TerminalCommands;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;
import java.util.UUID;

public class KeyCommand implements ICommand {

    @Override
    public String getName() {return "key";}
    @Override
    public String getUsage() {
        return "Usage: key <activation_code>\n" +
                "Enters the required activation code to enable full system access (Hacker Active status).";
    }
    @Override
    public String execute(ServerPlayerEntity player, List<String> optionsList, String remainingArgument) {
        if (remainingArgument.isBlank()) {
            return "Error: Usage: key <activation_code>. Type 'key -h' for help.";
        }
        UUID playerId = player.getUuid();
        String activationKey = TerminalCommands.ACTIVATION_KEY;
        if (activationKey == null || activationKey.isBlank()) {
            return "Error: System activation key is missing. Contact system administrator.";
        }
        if (remainingArgument.equals(activationKey)) {
            ServerCommandProcessor.PlayerState state = TerminalCommands.getPlayerState(playerId);
            if (state.isHackerActive()) {
                return "System access is already ACTIVE.";
            }
            state.setHackerActive(true);
            state.setActivationTime(System.currentTimeMillis());
            return "Key accepted. System access level upgraded to: ACTIVE.\n" +
                    "You may now use the company terminal and related blocks (e.g., standardBlock).";
        } else { return "Key rejected. Incorrect or expired code.";}

    }
}