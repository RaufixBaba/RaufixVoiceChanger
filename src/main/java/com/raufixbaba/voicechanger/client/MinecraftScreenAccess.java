package com.raufixbaba.voicechanger.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class MinecraftScreenAccess {
    private MinecraftScreenAccess() {
    }

    public static Screen current(Minecraft client) {
        return client == null ? null : client.screen;
    }

    public static void show(Minecraft client, Screen screen) {
        if (client != null) {
            client.setScreen(screen);
        }
    }
}
