package cards.modid.client;

import cards.modid.client.input.CardKeybindings;
import cards.modid.client.render.CardHudRenderer;
import cards.modid.network.SyncCardSlotsPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class PowerCaedsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CardKeybindings.register();
        CardHudRenderer.register();
        ClientPlayNetworking.registerGlobalReceiver(
                SyncCardSlotsPacket.TYPE,
                (payload, context) -> ClientCardState.update(payload)
        );
    }
}
