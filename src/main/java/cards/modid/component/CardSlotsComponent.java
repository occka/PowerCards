package cards.modid.component;

import cards.modid.PowerCaeds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cards.modid.card.PowerCard;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores 3 card slots + cooldowns for a player.
 * Attached per-player via Fabric Attachment API and persisted with the player.
 */
public class CardSlotsComponent {

    public static final int SLOT_COUNT = 3;

    public static final Codec<CardSlotsComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("slots").forGetter(CardSlotsComponent::getSlotList),
            Codec.INT.listOf().fieldOf("cooldowns").forGetter(CardSlotsComponent::getCooldownList)
    ).apply(instance, CardSlotsComponent::fromLists));

    public static final AttachmentType<CardSlotsComponent> TYPE =
            AttachmentRegistry.<CardSlotsComponent>builder()
                    .initializer(CardSlotsComponent::new)
                    .persistent(CODEC)
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

    private static CardSlotsComponent fromLists(List<ItemStack> savedSlots, List<Integer> savedCooldowns) {
        CardSlotsComponent component = new CardSlotsComponent();
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (i < savedSlots.size()) {
                component.slots[i] = savedSlots.get(i).copy();
            }
            if (i < savedCooldowns.size()) {
                component.cooldownRemaining[i] = Math.max(0, savedCooldowns.get(i));
            }
        }
        return component;
    }

    private List<ItemStack> getSlotList() {
        List<ItemStack> savedSlots = new ArrayList<>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            savedSlots.add(slots[i].copy());
        }
        return savedSlots;
    }

    private List<Integer> getCooldownList() {
        List<Integer> savedCooldowns = new ArrayList<>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            savedCooldowns.add(cooldownRemaining[i]);
        }
        return savedCooldowns;
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
