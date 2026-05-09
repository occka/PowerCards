package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FreezeCard extends PowerCard {
    private static final List<StormState> STORMS = new ArrayList<>();
    private static final Map<UUID, Long> FROZEN_UNTIL = new HashMap<>();
    private static final int RADIUS = 15;
    private static final int STORM_TICKS = 8 * 20;

    public FreezeCard(Item.Properties properties) { super(properties); }

    @Override public Component getCardName() { return Component.translatable("item.powercaeds.freeze_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.freeze_card.desc"); }
    @Override public int getCooldownTicks() { return 100 * 20; }
    @Override public int getPrimaryColor() { return 0xDCEFFF; }
    @Override public int getSecondaryColor() { return 0x65AEEB; }
    @Override public int getStructureChestLootWeight() { return 1; }

    @Override
    public int[] getSymbolPattern() {
        return new int[]{ 0b10101, 0b01110, 0b11111, 0b01110, 0b10101, 0b00100 };
    }

    @Override
    public void applyEffect(Player player, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        HitResult hit = level.clip(new ClipContext(
                player.getEyePosition(),
                player.getEyePosition().add(player.getLookAngle().scale(60.0)),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));
        Vec3 center = hit.getLocation();
        drawSnowballTrail(serverLevel, player.getEyePosition(), center);
        STORMS.add(new StormState(serverLevel, center, player.getUUID(), level.getGameTime(), level.getGameTime() + STORM_TICKS));
        serverLevel.playSound(null, BlockPos.containing(center), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0F, 0.8F);
    }

    @Override
    public void tickEffect(Player player, Level level, int slotIndex) {
    }

    public static void serverTick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Long>> frozenIterator = FROZEN_UNTIL.entrySet().iterator();
        while (frozenIterator.hasNext()) {
            Map.Entry<UUID, Long> entry = frozenIterator.next();
            if (server.overworld().getGameTime() >= entry.getValue()) {
                frozenIterator.remove();
            }
        }

        Iterator<StormState> iterator = STORMS.iterator();
        while (iterator.hasNext()) {
            StormState storm = iterator.next();
            if (storm.level.getGameTime() >= storm.expireTick) {
                storm.discard();
                iterator.remove();
                continue;
            }
            storm.tick(storm.level.getGameTime());
        }
    }

    public static void registerFreezeEvents() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> !isFrozen(player.getUUID(), world.getGameTime()));
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
                isFrozen(player.getUUID(), world.getGameTime()) ? InteractionResult.FAIL : InteractionResult.PASS);
    }

    private static boolean isFrozen(UUID uuid, long gameTime) {
        return FROZEN_UNTIL.getOrDefault(uuid, 0L) > gameTime;
    }

    private static void drawSnowballTrail(ServerLevel level, Vec3 start, Vec3 end) {
        Vec3 delta = end.subtract(start);
        double length = delta.length();
        if (length < 0.01) return;
        Vec3 dir = delta.normalize();
        int steps = Math.max(6, (int) (length * 2.0));
        for (int i = 0; i <= steps; i++) {
            Vec3 point = start.add(dir.scale(length * i / steps));
            level.sendParticles(ParticleTypes.ITEM_SNOWBALL, point.x, point.y, point.z, 2, 0.03, 0.03, 0.03, 0.0);
        }
    }

    private static final class StormState {
        private final ServerLevel level;
        private final Vec3 center;
        private final UUID owner;
        private final long startTick;
        private final long expireTick;
        private final Display.TextDisplay label;

        private StormState(ServerLevel level, Vec3 center, UUID owner, long startTick, long expireTick) {
            this.level = level;
            this.center = center;
            this.owner = owner;
            this.startTick = startTick;
            this.expireTick = expireTick;
            this.label = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);
            this.label.setPos(center.x, center.y + 3.0, center.z);
            this.label.setCustomNameVisible(true);
            level.addFreshEntity(this.label);
        }

        private void tick(long now) {
            int seconds = Math.max(0, (int) Math.ceil((expireTick - now) / 20.0));
            label.setCustomName(Component.literal("Blizzard remain: " + seconds + " sec"));
            label.setPos(center.x, center.y + 3.0, center.z);
            level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 2, center.z, 80, RADIUS, 4, RADIUS, 0.05);
            drawBorder();

            if (now % 5 == 0) {
                placeSnow();
            }

            AABB area = AABB.ofSize(center, RADIUS * 2.0, RADIUS * 2.0, RADIUS * 2.0);
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
                if (entity.getUUID().equals(owner)) continue;
                if (entity.distanceToSqr(center) > RADIUS * RADIUS) continue;

                entity.setTicksFrozen(Math.max(entity.getTicksFrozen(), entity.getTicksRequiredToFreeze()));
                entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 4, false, false, true));
                entity.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 80, 4, false, false, true));
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 4, false, false, true));

                long age = now - startTick;
                if (age >= 3 * 20 && age <= 6 * 20) {
                    FROZEN_UNTIL.put(entity.getUUID(), now + 2);
                    entity.setDeltaMovement(0, 0, 0);
                    entity.hurtMarked = true;
                }
            }
        }

        private void placeSnow() {
            BlockPos base = BlockPos.containing(center);
            for (int x = -RADIUS; x <= RADIUS; x++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    if (x * x + z * z > RADIUS * RADIUS) continue;
                    BlockPos surface = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, base.offset(x, 0, z));
                    if (surface.getY() < level.getMinY() || surface.getY() > level.getMaxY()) continue;
                    if (level.getBlockState(surface).isAir() && Blocks.SNOW.defaultBlockState().canSurvive(level, surface)) {
                        level.setBlock(surface, Blocks.SNOW.defaultBlockState(), 3);
                    }
                }
            }
        }

        private void drawBorder() {
            for (int i = 0; i < 72; i++) {
                double angle = Math.PI * 2.0 * i / 72.0;
                double x = center.x + Math.cos(angle) * RADIUS;
                double z = center.z + Math.sin(angle) * RADIUS;
                level.sendParticles(ParticleTypes.SNOWFLAKE, x, center.y + 0.2, z, 2, 0.0, 0.8, 0.0, 0.0);
            }
        }

        private void discard() {
            label.discard();
        }
    }
}
