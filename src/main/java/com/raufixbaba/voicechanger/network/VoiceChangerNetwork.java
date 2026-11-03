package com.raufixbaba.voicechanger.network;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Where the packet meets the loader.
 *
 * <p>Both directions are registered from common code even though only one of
 * them is ever used on a given side. A payload registered on one side and not
 * the other is a payload neither side can use, so the registration cannot be
 * split by physical side even though the handling can: the client installs its
 * own handler through {@link #setClientReceiver}, and nothing here ever names a
 * client class.</p>
 */
public final class VoiceChangerNetwork {
    private static volatile Consumer<byte[]> clientReceiver;
    private static volatile BiConsumer<ServerPlayer, byte[]> serverReceiver;

    private VoiceChangerNetwork() {
    }

    /**
     * Declares the packet. Must run while the game is starting: Minecraft
     * settles which payload types exist before anything connects.
     */
    public static void initialize() {
        // Minecraft 26 renamed these two accessors; nothing else about the
        // Fabric networking API moved.
        PayloadTypeRegistry.playS2C().register(VoiceChangerPayload.TYPE, VoiceChangerPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(VoiceChangerPayload.TYPE, VoiceChangerPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(VoiceChangerPayload.TYPE, (payload, context) -> {
            byte[] data = payload.data();
            ServerPlayer player = context.player();
            // Arrives on a network thread; the policy it triggers touches
            // server state and the player list.
            context.server().execute(() -> deliverToServer(player, data));
        });
    }





    /** Installed by the client half once it is up. */
    public static void setClientReceiver(Consumer<byte[]> receiver) {
        clientReceiver = receiver;
    }

    /** Installed by the server half once it is up. */
    public static void setServerReceiver(BiConsumer<ServerPlayer, byte[]> receiver) {
        serverReceiver = receiver;
    }

    /** @return whether the packet was handed to the network layer */
    public static boolean sendToPlayer(ServerPlayer player, byte[] data) {
        if (!canSendTo(player)) {
            return false;
        }

        ServerPlayNetworking.send(player, new VoiceChangerPayload(data));
        return true;
    }

    /** Whether this player's client speaks our channel. */
    public static boolean canSendTo(ServerPlayer player) {
        return ServerPlayNetworking.canSend(player, VoiceChangerPayload.TYPE);
    }

    private static void deliverToClient(byte[] data) {
        Consumer<byte[]> receiver = clientReceiver;
        if (receiver != null) {
            receiver.accept(data);
        }
    }

    private static void deliverToServer(ServerPlayer player, byte[] data) {
        BiConsumer<ServerPlayer, byte[]> receiver = serverReceiver;
        if (receiver != null) {
            receiver.accept(player, data);
        }
    }
}
