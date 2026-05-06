package cards.modid.component;

import cards.modid.PowerCaeds;
import cards.modid.card.PowerCard;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Stores 3 card slots + cooldowns for a player.
 * Attached per-player via Fabric Attachment API (non-persistent; sync via network on join).
 *
 * NOTE: For full cross-session persistence, store NBT in player SavedData or
 * use the persistent() overload with a proper Codec once the API stabilises in 26.1.
 */
public class CardSlotsComponent {

    public static final int SLOT_COUNT = 3;

    public static final AttachmentType<CardSlotsComponent> TYPE =
            AttachmentRegistry.<CardSlotsComponent>builder()
                    .initializer(CardSlotsComponent::new)
                    .buildAndRegister(
                            Identifier.fromNamespaceAndPath(PowerCaeds.MOD_ID, "card_slots")
                    );

    // ── State ─────────────────────────────────────────────────────────────────
    private final ItemStack[] slots          = new ItemStack[SLOT_COUNT];
    private final int[]       cooldownRemaining = new int[SLOT_COUNT];

    public CardSlotsComponent() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            slots[i] = ItemStack.EMPTY;
            cooldownRemaining[i] = 0;
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────
    public ItemStack getCard(int slot) {
        return (slot < 0 || slot >= SLOT_COUNT) ? ItemStack.EMPTY : slots[slot];
    }
    public void setCard(int slot, ItemStack stack) {
        if (slot >= 0 && slot < SLOT_COUNT) slots[slot] = stack == null ? ItemStack.EMPTY : stack;
    }
    public boolean hasCard(int slot) {
        return !getCard(slot).isEmpty() && getCard(slot).getItem() instanceof PowerCard;
    }
    public PowerCard getCardItem(int slot) {
        return hasCard(slot) ? (PowerCard) slots[slot].getItem() : null;
    }

    // ── Cooldown ──────────────────────────────────────────────────────────────
    public int     getCooldown(int slot)             { return cooldownRemaining[slot]; }
    public void    setCooldown(int slot, int ticks)  { if (slot >= 0 && slot < SLOT_COUNT) cooldownRemaining[slot] = ticks; }
    public boolean isOnCooldown(int slot)            { return cooldownRemaining[slot] > 0; }

    public void tickCooldowns() {
        for (int i = 0; i < SLOT_COUNT; i++) if (cooldownRemaining[i] > 0) cooldownRemaining[i]--;
    }

    public float getCooldownProgress(int slot) {
        if (!hasCard(slot)) return 0f;
        int max = getCardItem(slot).getCooldownTicks();
        return max == 0 ? 0f : (float) cooldownRemaining[slot] / max;
    }
}
