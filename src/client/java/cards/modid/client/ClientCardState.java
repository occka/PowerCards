package cards.modid.client;

import cards.modid.card.PowerCard;
import cards.modid.component.CardSlotsComponent;
import cards.modid.network.SyncCardSlotsPacket;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side cache of the local player's card slots.
 * Updated by SyncCardSlotsPacket from the server.
 * Read by CardHudRenderer.
 */
public class ClientCardState {

    private static final List<ItemStack> slots = new ArrayList<>(CardSlotsComponent.SLOT_COUNT);
    private static final List<Integer> cooldowns = new ArrayList<>(CardSlotsComponent.SLOT_COUNT);

    static {
        for (int i = 0; i < CardSlotsComponent.SLOT_COUNT; i++) {
            slots.add(ItemStack.EMPTY);
            cooldowns.add(0);
        }
    }

    /** Called when a SyncCardSlotsPacket arrives. */
    public static void update(SyncCardSlotsPacket pkt) {
        for (int i = 0; i < CardSlotsComponent.SLOT_COUNT; i++) {
            slots.set(i, pkt.slots().get(i));
            cooldowns.set(i, pkt.cooldowns().get(i));
        }
    }

    public static ItemStack getCard(int slot) {
        return slots.get(slot);
    }

    public static boolean hasCard(int slot) {
        return !slots.get(slot).isEmpty() && slots.get(slot).getItem() instanceof PowerCard;
    }

    public static PowerCard getCardItem(int slot) {
        if (!hasCard(slot)) return null;
        return (PowerCard) slots.get(slot).getItem();
    }

    public static int getCooldown(int slot) {
        return cooldowns.get(slot);
    }

    /** Advances the client-side cooldown display between server sync packets. */
    public static void tickCooldowns() {
        for (int i = 0; i < CardSlotsComponent.SLOT_COUNT; i++) {
            int cooldown = cooldowns.get(i);
            if (cooldown > 0) {
                cooldowns.set(i, cooldown - 1);
            }
        }
    }

    public static boolean isOnCooldown(int slot) {
        return cooldowns.get(slot) > 0;
    }

    /**
     * Cooldown progress for HUD rendering.
     * 0.0 = ready, 1.0 = just activated
     */
    public static float getCooldownProgress(int slot) {
        if (!hasCard(slot)) return 0f;
        int maxCd = getCardItem(slot).getCooldownTicks();
        if (maxCd == 0) return 0f;
        return (float) cooldowns.get(slot) / maxCd;
    }
}
