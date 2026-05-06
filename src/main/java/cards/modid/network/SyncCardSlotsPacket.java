package cards.modid.network;

import cards.modid.PowerCaeds;
import cards.modid.component.CardSlotsComponent;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record SyncCardSlotsPacket(List<ItemStack> slots, List<Integer> cooldowns)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncCardSlotsPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(PowerCaeds.MOD_ID, "sync_card_slots"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCardSlotsPacket> CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeInt(p.slots().size());
                        for (ItemStack s : p.slots()) ItemStack.STREAM_CODEC.encode(buf, s);
                        for (int cd : p.cooldowns()) buf.writeInt(cd);
                    },
                    buf -> {
                        int n = buf.readInt();
                        List<ItemStack> slots = new ArrayList<>(n);
                        for (int i = 0; i < n; i++) slots.add(ItemStack.STREAM_CODEC.decode(buf));
                        List<Integer> cds = new ArrayList<>(n);
                        for (int i = 0; i < n; i++) cds.add(buf.readInt());
                        return new SyncCardSlotsPacket(slots, cds);
                    }
            );

    @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
    }

    public static SyncCardSlotsPacket from(CardSlotsComponent comp) {
        List<ItemStack> slots = new ArrayList<>();
        List<Integer> cds    = new ArrayList<>();
        for (int i = 0; i < CardSlotsComponent.SLOT_COUNT; i++) {
            slots.add(comp.getCard(i).copy());
            cds.add(comp.getCooldown(i));
        }
        return new SyncCardSlotsPacket(slots, cds);
    }

    public static void send(ServerPlayer player, CardSlotsComponent comp) {
        ServerPlayNetworking.send(player, from(comp));
    }
}
