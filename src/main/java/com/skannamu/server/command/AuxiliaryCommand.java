package com.skannamu.server.command;

// import com.skannamu.init.ModItems; // 💡 EMP 모듈 체크를 제거했으므로 더 이상 필요하지 않을 수 있습니다.
import com.skannamu.server.ServerCommandProcessor;
import com.skannamu.server.TerminalCommands;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;

public class AuxiliaryCommand implements ICommand {

    @Override
    public String getName() {
        return "auxiliary";
    }
    @Override
    public String getUsage() {
        return "Usage: auxiliary -m <module>\n" +
                "  -m emp: 광역 전자기 펄스(EMP)를 설정하고 발동합니다.";
    }
    @Override
    public String execute(ServerPlayerEntity player, List<String> options, String argument) {

        ServerCommandProcessor.PlayerState state = TerminalCommands.getPlayerState(player.getUuid());

        if (!options.contains("m")) {
            return "Error: Missing required option '-m <module>'. Use -h for help.";
        }
        String moduleName = argument.trim().split("\\s+", 2)[0].trim().toLowerCase();

        if (moduleName.equals("emp")) {

            state.setCurrentCommandState(ServerCommandProcessor.PlayerState.CommandState.EMP_RANGE_PROMPT);
            state.setEmpRange(0);
            state.setEmpDuration(0);

            return "EMP module loaded. Enter range (5-30 blocks): >> set range <value>";

        } else {
            return "Error: Unknown module '" + moduleName + "'. Currently only 'emp' is supported.";
        }
    }
}