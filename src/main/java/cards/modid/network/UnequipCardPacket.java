package cards.modid.network;

import cards.modid.PowerCaeds;
import cards.modid.handler.CardSlotHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record UnequipCardPacket(int slotIndex) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UnequipCardPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(PowerCaeds.MOD_ID, "unequip_card"));

    public static final StreamCodec<FriendlyByteBuf, UnequipCardPacket> CODEC =
            StreamCodec.of((buf, p) -> buf.writeInt(p.slotIndex), buf -> new UnequipCardPacket(buf.readInt()));

    @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, ctx) -> {
            int slot = payload.slotIndex();
            ServerPlayer player = ctx.player();
            ctx.server().execute(() -> CardSlotHandler.unequipCard(player, slot));
        });
    }
}
