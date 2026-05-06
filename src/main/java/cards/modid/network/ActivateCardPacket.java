package cards.modid.network;

import cards.modid.PowerCaeds;
import cards.modid.card.PowerCard;
import cards.modid.component.CardSlotsComponent;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record ActivateCardPacket(int slotIndex) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ActivateCardPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(PowerCaeds.MOD_ID, "activate_card"));

    public static final StreamCodec<FriendlyByteBuf, ActivateCardPacket> CODEC =
            StreamCodec.of((buf, p) -> buf.writeInt(p.slotIndex), buf -> new ActivateCardPacket(buf.readInt()));

    @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, ctx) -> {
            int slot = payload.slotIndex();
            ServerPlayer player = ctx.player();
            ctx.server().execute(() -> {
                CardSlotsComponent comp = player.getAttachedOrCreate(CardSlotsComponent.TYPE);
                if (!comp.hasCard(slot) || comp.isOnCooldown(slot)) return;
                PowerCard card = comp.getCardItem(slot);
                card.applyEffect(player, player.level());
                comp.setCooldown(slot, card.getCooldownTicks());
                SyncCardSlotsPacket.send(player, comp);
            });
        });
    }
}
