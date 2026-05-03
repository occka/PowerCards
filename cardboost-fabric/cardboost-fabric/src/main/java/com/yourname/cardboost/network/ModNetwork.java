package com.yourname.cardboost.network;

import com.yourname.cardboost.CardBoostMod;
import com.yourname.cardboost.player.CardSlotManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class ModNetwork {

    public static void register() {
        PayloadTypeRegistry.playC2S().register(CardActivatePayload.TYPE, CardActivatePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(CardActivatePayload.TYPE,
            (payload, context) -> context.server().execute(() ->
                CardSlotManager.get(context.player()).activate(payload.slotIndex(), context.player())
            )
        );
    }

    public record CardActivatePayload(int slotIndex) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<CardActivatePayload> TYPE =
            new CustomPacketPayload.Type<>(
                ResourceLocation.fromNamespaceAndPath(CardBoostMod.MOD_ID, "card_activate"));

        public static final StreamCodec<RegistryFriendlyByteBuf, CardActivatePayload> CODEC =
            StreamCodec.of(
                (buf, p) -> buf.writeInt(p.slotIndex()),
                buf -> new CardActivatePayload(buf.readInt())
            );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
