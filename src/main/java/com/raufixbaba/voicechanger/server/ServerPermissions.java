package com.raufixbaba.voicechanger.server;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.commands.Commands;

/**
 * Who may change their voice, and who may run the command.
 *
 * <p>NeoForge ships a permission API that any permission manager plugs into, so
 * there both questions are real permission nodes. Fabric has no equivalent in
 * the loader or in Fabric API, and the community library that fills the gap is
 * not a dependency this mod takes, so there the command falls back to the
 * vanilla operator level and per-player control is the mute list instead.</p>
 */
public final class ServerPermissions {


    private ServerPermissions() {
    }


    /** Whether this player may change their voice at all. */
    public static boolean canUse(ServerPlayer player) {
        // No permission API on Fabric, so nobody is refused on this ground and
        // the mute list is what a moderator uses instead.
        return true;
    }

    /** Whether this source may run the moderation command. */
    public static boolean canCommand(CommandSourceStack source) {
        return isOperator(source);
    }

    /**
     * The vanilla moderator level, however the running version spells it.
     * Minecraft 1.21.11 replaced numeric permission levels with a permission
     * set, so there is no one spelling that works across the supported range.
     */
    private static boolean isOperator(CommandSourceStack source) {
        return Commands.<CommandSourceStack>hasPermission(Commands.LEVEL_GAMEMASTERS).test(source);
    }
}
