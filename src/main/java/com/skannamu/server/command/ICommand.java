package com.skannamu.server.command;

import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;

public interface ICommand {

    String getName();

    String getUsage();

    /** * @param player 명령어를 실행한 플레이어 엔티티
     * @param optionsList TerminalCommands에서 분리된 옵션 문자열 목록 (예: ["a", "l"])
     * @param remainingArgument 옵션이 제거된 순수 인수 문자열 (예: "/etc/config")
     * @return 터미널에 출력될 결과 메시지
     */
    String execute(ServerPlayerEntity player, List<String> optionsList, String remainingArgument);
}