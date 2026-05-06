package cards.modid.handler;

import cards.modid.component.CardSlotsComponent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

/**
 * Syncs card slot data to the client when a player joins or respawns.
 * Register in PowerCaeds.onInitialize().
 */
public class PlayerJoinHandler {

    public static void register() {
        // Sync on join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            CardSlotHandler.sync(handler.player);
        });
    }
}
