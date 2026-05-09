package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class TimeStopCard extends PowerCard {
    private static final Map<UUID, Long> FROZEN_PLAYERS = new HashMap<>();
    private static long unfreezeTick = 0;

    public TimeStopCard(Item.Properties properties) { super(properties); }
    @Override public Component getCardName() { return Component.translatable("item.powercaeds.timestop_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.timestop_card.desc"); }
    @Override public int getCooldownTicks() { return 180 * 20; }
    @Override public int getPrimaryColor() { return 0xD9A629; }
    @Override public int getSecondaryColor() { return 0x62D8FF; }
    @Override public int getStructureChestLootWeight() { return 0; }
    @Override public int[] getSymbolPattern() { return new int[]{0b11111,0b10001,0b01010,0b00100,0b01010,0b11111}; }

    @Override
    public void applyEffect(Player player, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        long end = serverLevel.getGameTime() + 6 * 20;
        unfreezeTick = Math.max(unfreezeTick, end);
        serverLevel.getServer().getCommands().performPrefixedCommand(serverLevel.getServer().createCommandSourceStack(), "tick freeze");
        for (Player target : serverLevel.players()) {
            if (target != player && target.distanceToSqr(player) <= 30 * 30) {
                FROZEN_PLAYERS.put(target.getUUID(), end);
            }
        }
    }

    public static void serverTick(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        if (unfreezeTick > 0 && now >= unfreezeTick) {
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "tick unfreeze");
            unfreezeTick = 0;
        }
        Iterator<Map.Entry<UUID, Long>> iterator = FROZEN_PLAYERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            if (now >= entry.getValue()) {
                iterator.remove();
                continue;
            }
            Player player = server.getPlayerList().getPlayer(entry.getKey());
            if (player != null) {
                player.setDeltaMovement(0, 0, 0);
                player.hurtMarked = true;
            }
        }
    }

    public static void registerEvents() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> !isFrozen(player.getUUID(), world.getGameTime()));
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
                isFrozen(player.getUUID(), world.getGameTime()) ? InteractionResult.FAIL : InteractionResult.PASS);
    }

    private static boolean isFrozen(UUID uuid, long now) {
        return FROZEN_PLAYERS.getOrDefault(uuid, 0L) > now;
    }
}
