package cards.modid.handler;

import cards.modid.component.CardSlotsComponent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class CardTickHandler {

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(CardTickHandler::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            CardSlotsComponent comp = player.getAttachedOrCreate(CardSlotsComponent.TYPE);
            comp.tickCooldowns();
            for (int i = 0; i < CardSlotsComponent.SLOT_COUNT; i++) {
                if (comp.hasCard(i)) {
                    comp.getCardItem(i).tickEffect(player, player.level(), i);
                }
            }
        }
    }
}
