package cards.modid.handler;

import cards.modid.card.PowerCard;
import cards.modid.card.impl.MidasCard;
import cards.modid.component.CardSlotsComponent;
import cards.modid.network.SyncCardSlotsPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class CardSlotHandler {

    public static boolean equipCard(ServerPlayer player, ItemStack stack, int slot) {
        if (slot < 0 || slot >= CardSlotsComponent.SLOT_COUNT) return false;
        if (stack.isEmpty() || !(stack.getItem() instanceof PowerCard)) return false;
        PowerCard card = (PowerCard) stack.getItem();
        CardSlotsComponent comp = player.getAttachedOrCreate(CardSlotsComponent.TYPE);
        if (comp.hasCard(slot) && comp.isOnCooldown(slot)) return false;
        if (!card.allowsDuplicateEquip()) {
            for (int i = 0; i < CardSlotsComponent.SLOT_COUNT; i++) {
                if (i != slot && comp.hasCard(i) && comp.getCardItem(i) == card) return false;
            }
        }
        if (comp.hasCard(slot)) unequipCard(player, slot);
        comp.setCard(slot, stack.copyWithCount(1));
        stack.shrink(1);
        sync(player, comp);
        return true;
    }

    public static ItemStack unequipCard(ServerPlayer player, int slot) {
        if (slot < 0 || slot >= CardSlotsComponent.SLOT_COUNT) return ItemStack.EMPTY;
        CardSlotsComponent comp = player.getAttachedOrCreate(CardSlotsComponent.TYPE);
        if (!comp.hasCard(slot)) return ItemStack.EMPTY;
        if (comp.isOnCooldown(slot)) return ItemStack.EMPTY;
        ItemStack card = comp.getCard(slot).copy();
        comp.setCard(slot, ItemStack.EMPTY);
        comp.setCooldown(slot, 0);

        if (card.getItem() instanceof MidasCard) {
            MidasCard.removeScale(player);
        }

        if (!player.getInventory().add(card)) player.drop(card, false);
        sync(player, comp);
        return card;
    }

    public static void sync(ServerPlayer player) {
        sync(player, player.getAttachedOrCreate(CardSlotsComponent.TYPE));
    }

    public static void sync(ServerPlayer player, CardSlotsComponent comp) {
        SyncCardSlotsPacket.send(player, comp);
    }
}
