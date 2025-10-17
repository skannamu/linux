package com.skannamu.client;

import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;

public class ClientHackingState {

    private static boolean isTerminalHacked = false;
    public static boolean isTerminalHacked() {
        return isTerminalHacked;
    }
    public static void setHacked(boolean hacked) {
        if (isTerminalHacked == hacked) return;
        isTerminalHacked = hacked;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            if (hacked) {
                client.player.sendMessage(Text.literal("❗️ SYSTEM ALERT: Terminal access denied. EMP in effect."), true);
            } else {
                client.player.sendMessage(Text.literal("✅ Terminal system online. EMP effect dissipated."), true);
            }
        }
    }
}