package cards.modid.handler;

import cards.modid.card.PowerCard;
import cards.modid.component.CardSlotsComponent;
import cards.modid.network.SyncCardSlotsPacket;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;

public class CardDeathHandler {

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (!(entity instanceof ServerPlayer player)) return true;

            CardSlotsComponent comp = player.getAttachedOrCreate(CardSlotsComponent.TYPE);
            for (int slot = 0; slot < CardSlotsComponent.SLOT_COUNT; slot++) {
                if (!comp.hasCard(slot) || comp.isOnCooldown(slot)) continue;

                PowerCard card = comp.getCardItem(slot);
                if (card.tryPreventDeath(player, player.level(), damageSource, damageAmount, slot)) {
                    comp.setCooldown(slot, card.getCooldownTicks());
                    SyncCardSlotsPacket.send(player, comp);
                    return false;
                }
            }

            return true;
        });
    }
}
