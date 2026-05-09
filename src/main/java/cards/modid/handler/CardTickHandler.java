package cards.modid.handler;

import cards.modid.component.CardSlotsComponent;
import cards.modid.card.impl.ForcewallCard;
import cards.modid.card.impl.FreezeCard;
import cards.modid.card.impl.DeepCard;
import cards.modid.card.impl.MayhemCard;
import cards.modid.card.impl.TimeStopCard;
import cards.modid.card.impl.TurmCard;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class CardTickHandler {

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(CardTickHandler::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        ForcewallCard.serverTick(server);
        FreezeCard.serverTick(server);
        TimeStopCard.serverTick(server);
        MayhemCard.serverTick(server);
        TurmCard.serverTick(server);
        DeepCard.serverTick(server);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            CardSlotsComponent comp = player.getAttachedOrCreate(CardSlotsComponent.TYPE);
            comp.tickCooldowns();
            for (int i = 0; i < CardSlotsComponent.SLOT_COUNT; i++) {
                if (comp.hasCard(i)) {
                    comp.getCardItem(i).tickEffect(player, player.level(), i);
                }
            }

            player.level().getEntities(player, player.getBoundingBox().inflate(256), entity ->
                    entity.hasCustomName() && "powercaeds_ghast_card_fireball".equals(entity.getCustomName() != null ? entity.getCustomName().getString() : "") && entity.tickCount >= 10 * 20
            ).forEach(entity -> entity.discard());
        }
    }
}
