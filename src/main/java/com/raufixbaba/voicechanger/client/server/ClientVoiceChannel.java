package com.raufixbaba.voicechanger.client.server;

import java.util.function.Consumer;
import com.raufixbaba.voicechanger.RaufixVoiceChanger;
import com.raufixbaba.voicechanger.network.VoiceChangerNetwork;
import com.raufixbaba.voicechanger.network.VoiceChangerPayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/** The client end of the channel the server sends its policy over. */
public final class ClientVoiceChannel {
    private ClientVoiceChannel() {
    }

    /**
     * Starts listening. The packet type itself is declared by
     * {@link VoiceChangerNetwork}, from common code, because a type registered
     * on one side only is one neither side can use.
     */
    public static void initialize(Consumer<byte[]> receiver) {
        ClientPlayNetworking.registerGlobalReceiver(VoiceChangerPayload.TYPE, (payload, context) -> {
            byte[] data = payload.data();
            // Arrives on a network thread; everything it touches is client state.
            context.client().execute(() -> receiver.accept(data));
        });
    }

    /** Whether there is a server on the other end that speaks this channel. */
    public static boolean isConnected() {
        return ClientPlayNetworking.canSend(VoiceChangerPayload.TYPE);
    }

    /** @return whether the message was handed to the network layer */
    public static boolean send(byte[] payload) {
        if (!isConnected()) {
            return false;
        }

        try {
            ClientPlayNetworking.send(new VoiceChangerPayload(payload));
            return true;
        } catch (RuntimeException exception) {
            // The connection can drop between the check and the send.
            RaufixVoiceChanger.LOGGER.debug("Could not send on the voice changer channel", exception);
            return false;
        }
    }

}
