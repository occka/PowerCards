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
import net.minecraft.world.item.ItemStack;

public record EquipCardPacket(int slotIndex, int inventoryIndex) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<EquipCardPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(PowerCaeds.MOD_ID, "equip_card"));

    public static final StreamCodec<FriendlyByteBuf, EquipCardPacket> CODEC =
            StreamCodec.of(
                    (buf, p) -> { buf.writeInt(p.slotIndex); buf.writeInt(p.inventoryIndex); },
                    buf -> new EquipCardPacket(buf.readInt(), buf.readInt()));

    @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, ctx) -> {
            int slot = payload.slotIndex();
            int invIdx = payload.inventoryIndex();
            ServerPlayer player = ctx.player();
            ctx.server().execute(() -> {
                ItemStack stack = player.getInventory().getItem(invIdx);
                CardSlotHandler.equipCard(player, stack, slot);
            });
        });
    }
}
