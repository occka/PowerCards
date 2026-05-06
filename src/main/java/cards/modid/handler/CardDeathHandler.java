package cards.modid.handler;

import cards.modid.card.PowerCard;
import cards.modid.component.CardSlotsComponent;
import cards.modid.network.SyncCardSlotsPacket;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

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

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player) {
                dropEquippedCards(player);
            }
        });
    }

    private static void dropEquippedCards(ServerPlayer player) {
        CardSlotsComponent comp = player.getAttachedOrCreate(CardSlotsComponent.TYPE);
        boolean changed = false;

        for (int slot = 0; slot < CardSlotsComponent.SLOT_COUNT; slot++) {
            if (!comp.hasCard(slot)) continue;

            ItemStack card = comp.getCard(slot).copy();
            comp.setCard(slot, ItemStack.EMPTY);
            comp.setCooldown(slot, 0);
            player.drop(card, true);
            changed = true;
        }

        if (changed) {
            SyncCardSlotsPacket.send(player, comp);
        }
    }
}
