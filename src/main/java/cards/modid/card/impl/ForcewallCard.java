package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ForcewallCard extends PowerCard {
    private static final Map<UUID, DomeState> DOMES = new HashMap<>();
    private static final int DURATION_TICKS = 15 * 20;
    private static final int RADIUS = 6;

    public ForcewallCard(Item.Properties properties) { super(properties); }

    @Override public Component getCardName() { return Component.translatable("item.powercaeds.forcewall_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.forcewall_card.desc"); }
    @Override public int getCooldownTicks() { return 40 * 20; }
    @Override public int getPrimaryColor() { return 0xF6D84A; }
    @Override public int getSecondaryColor() { return 0xFFFFFF; }
    @Override public int getStructureChestLootWeight() { return 1; }

    @Override
    public int[] getSymbolPattern() {
        return new int[]{ 0b01110, 0b10001, 0b10101, 0b10101, 0b10001, 0b01110 };
    }

    @Override
    public void applyEffect(Player player, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        DomeState previous = DOMES.remove(player.getUUID());
        if (previous != null) previous.restore();

        DomeState dome = new DomeState(serverLevel, player.blockPosition(), level.getGameTime() + DURATION_TICKS);
        dome.place();
        DOMES.put(player.getUUID(), dome);
    }

    @Override
    public void tickEffect(Player player, Level level, int slotIndex) {
    }

    public static void serverTick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, DomeState>> iterator = DOMES.entrySet().iterator();
        while (iterator.hasNext()) {
            DomeState dome = iterator.next().getValue();
            if (dome.level.getGameTime() >= dome.expireTick) {
                dome.restore();
                iterator.remove();
                continue;
            }
            dome.applyEffects();
        }
    }

    public static void registerProtectionEvent() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> !isProtected(world, pos));
    }

    private static boolean isProtected(Level level, BlockPos pos) {
        for (DomeState dome : DOMES.values()) {
            if (dome.level == level && dome.blocks.containsKey(pos)) return true;
        }
        return false;
    }

    private static final class DomeState {
        private final ServerLevel level;
        private final BlockPos center;
        private final long expireTick;
        private final Map<BlockPos, BlockState> blocks = new HashMap<>();

        private DomeState(ServerLevel level, BlockPos center, long expireTick) {
            this.level = level;
            this.center = center;
            this.expireTick = expireTick;
        }

        private void place() {
            double radiusSq = RADIUS * RADIUS;
            double innerSq = (RADIUS - 1.1) * (RADIUS - 1.1);
            for (int x = -RADIUS; x <= RADIUS; x++) {
                for (int y = -RADIUS; y <= RADIUS; y++) {
                    for (int z = -RADIUS; z <= RADIUS; z++) {
                        double distSq = x * x + y * y + z * z;
                        if (distSq > radiusSq || distSq < innerSq) continue;
                        if (y < -RADIUS + 1) continue;
                        BlockPos pos = center.offset(x, y, z);
                        blocks.putIfAbsent(pos.immutable(), level.getBlockState(pos));
                        level.setBlock(pos, Blocks.YELLOW_STAINED_GLASS.defaultBlockState(), 3);
                    }
                }
            }

        }

        private void applyEffects() {
            AABB area = new AABB(center).inflate(RADIUS);
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 1, false, false, true));
                entity.addEffect(new MobEffectInstance(MobEffects.SATURATION, 40, 0, false, false, true));
            }
        }

        private void restore() {
            for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
                level.setBlock(entry.getKey(), entry.getValue(), 3);
            }
            blocks.clear();
        }
    }
}
