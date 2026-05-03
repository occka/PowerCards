package com.yourname.cardboost.player;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

public class PlayerDataSaver {
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            CardSlotManager.get(player);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.player;
            CardSlotManager.remove(player.getUUID());
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            CardSlotManager oldData = CardSlotManager.get(oldPlayer);
            CardSlotManager newData = CardSlotManager.get(newPlayer);
            newData.fromNbt(oldData.toNbt());
        });
    }
}
