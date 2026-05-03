package com.yourname.cardboost.player;

import com.yourname.cardboost.card.Card;
import com.yourname.cardboost.card.CardRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CardSlotManager {
    public static final int SLOT_COUNT = 3;
    private static final Map<UUID, CardSlotManager> DATA = new HashMap<>();

    private final String[] slots = new String[SLOT_COUNT];
    private final int[] cooldowns = new int[SLOT_COUNT];

    public static CardSlotManager get(Player player) {
        return DATA.computeIfAbsent(player.getUUID(), uuid -> new CardSlotManager());
    }

    public static void remove(UUID uuid) { DATA.remove(uuid); }

    public boolean activate(int slotIndex, Player player) {
        if (slotIndex < 0 || slotIndex >= SLOT_COUNT) return false;
        if (cooldowns[slotIndex] > 0) return false;
        if (slots[slotIndex] == null) return false;
        Card card = CardRegistry.getById(slots[slotIndex]);
        if (card == null) return false;
        card.onActivate(player);
        cooldowns[slotIndex] = card.getCooldownTicks();
        return true;
    }

    public void tick(Player player) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (cooldowns[i] > 0) cooldowns[i]--;
            if (slots[i] != null) {
                Card card = CardRegistry.getById(slots[i]);
                if (card != null) card.onTick(player);
            }
        }
    }

    public void setSlot(int index, String cardId) {
        if (index >= 0 && index < SLOT_COUNT) { slots[index] = cardId; cooldowns[index] = 0; }
    }

    public String getSlot(int index) { return slots[index]; }
    public int getCooldown(int index) { return cooldowns[index]; }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < SLOT_COUNT; i++) {
            tag.putString("slot_" + i, slots[i] != null ? slots[i] : "");
            tag.putInt("cd_" + i, cooldowns[i]);
        }
        return tag;
    }

    public void fromNbt(CompoundTag tag) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            // В 26.1 getString и getInt возвращают Optional
            String s = tag.getString("slot_" + i).orElse("");
            slots[i] = s.isEmpty() ? null : s;
            cooldowns[i] = tag.getInt("cd_" + i).orElse(0);
        }
    }
}
