package com.yourname.cardboost.player;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class PlayerDataSaver {
    private static final String NBT_KEY = "cardboost_card_slots";

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            CompoundTag persistent = player.getPersistentData();
            if (persistent.contains(NBT_KEY)) {
                CardSlotManager.get(player).fromNbt(persistent.getCompound(NBT_KEY).orElse(new CompoundTag()));
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.player;
            player.getPersistentData().put(NBT_KEY, CardSlotManager.get(player).toNbt());
            CardSlotManager.remove(player.getUUID());
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            CardSlotManager oldData = CardSlotManager.get(oldPlayer);
            CardSlotManager newData = CardSlotManager.get(newPlayer);
            newData.fromNbt(oldData.toNbt());
        });
    }
}
