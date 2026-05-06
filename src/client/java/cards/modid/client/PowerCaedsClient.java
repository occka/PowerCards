package cards.modid.client;

import cards.modid.client.input.CardKeybindings;
import cards.modid.client.render.CardHudRenderer;
import cards.modid.network.SyncCardSlotsPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class PowerCaedsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CardKeybindings.register();
        CardHudRenderer.register();
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientCardState.tickCooldowns());
        ClientPlayNetworking.registerGlobalReceiver(
                SyncCardSlotsPacket.TYPE,
                (payload, context) -> ClientCardState.update(payload)
        );
    }
}
