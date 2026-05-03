package com.yourname.cardboost.network;

import com.yourname.cardboost.CardBoostMod;
import com.yourname.cardboost.player.CardSlotManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ModNetwork {
    public static final ResourceLocation CARD_ACTIVATE = ResourceLocation.fromNamespaceAndPath(CardBoostMod.MOD_ID, "card_activate");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(CARD_ACTIVATE,
            (server, player, handler, buf, responseSender) -> {
                int slotIndex = buf.readInt();
                server.execute(() -> CardSlotManager.get(player).activate(slotIndex, player));
            }
        );
    }

    public static FriendlyByteBuf createActivatePacket(int slotIndex) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(slotIndex);
        return buf;
    }
}
