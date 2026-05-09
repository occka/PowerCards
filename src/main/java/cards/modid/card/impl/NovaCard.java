package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NovaCard extends PowerCard {
    private static final Map<UUID, PortalPair> PORTALS = new HashMap<>();
    private static final int PORTAL_LIFETIME_TICKS = 10 * 20;
    private static final int TELEPORT_LOCK_TICKS = 10;

    public NovaCard(Item.Properties properties) { super(properties); }
    @Override public Component getCardName() { return Component.translatable("item.powercaeds.nova_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.nova_card.desc"); }
    @Override public int getCooldownTicks() { return 25 * 20; }
    @Override public int getPrimaryColor() { return 0x59BFFF; }
    @Override public int getSecondaryColor() { return 0xFF8A2A; }
    @Override public int getStructureChestLootWeight() { return 1; }

    @Override
    public void applyEffect(Player player, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        Vec3 from = findSafePortalPos(player.level(), player.position().add(player.getLookAngle().scale(2.0)));
        HitResult hit = player.level().clip(new ClipContext(player.getEyePosition(), player.getEyePosition().add(player.getLookAngle().scale(500.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 target = hit.getLocation();
        Vec3 to = findSafePortalPos(player.level(), target);
        Vec3 right = getPortalRightVector(player);
        PORTALS.put(player.getUUID(), new PortalPair(from, to, right, player.getYRot(), player.level().getGameTime() + PORTAL_LIFETIME_TICKS));
        playPortalSound(serverLevel, from);
        playPortalSound(serverLevel, to);
    }

    @Override
    public void tickEffect(Player player, Level level, int slotIndex) {
        PortalPair pair = PORTALS.get(player.getUUID());
        if (pair == null) return;
        if (level.getGameTime() > pair.expireTick) { PORTALS.remove(player.getUUID()); return; }

        if (level instanceof ServerLevel serverLevel) {
            drawPortalFrame(serverLevel, pair.from, pair.right, true);
            drawPortalFrame(serverLevel, pair.to, pair.right, false);
        }

        long now = level.getGameTime();
        if (now < pair.nextTeleportTick) return;

        if (portalBox(pair.from).intersects(player.getBoundingBox())) {
            teleportThrough(player, level, pair, pair.to);
        } else if (portalBox(pair.to).intersects(player.getBoundingBox())) {
            teleportThrough(player, level, pair, pair.from);
        }
    }

    private static void teleportThrough(Player player, Level level, PortalPair pair, Vec3 destination) {
        player.teleportTo(destination.x, destination.y, destination.z);
        player.setYRot(pair.yaw);
        pair.nextTeleportTick = level.getGameTime() + TELEPORT_LOCK_TICKS;
        if (level instanceof ServerLevel serverLevel) {
            playPortalSound(serverLevel, destination);
        }
    }

    private static AABB portalBox(Vec3 pos) {
        return AABB.ofSize(pos.add(0, 1, 0), 1.2, 2.1, 1.2);
    }

    private static void drawPortalFrame(ServerLevel level, Vec3 pos, Vec3 rightVector, boolean blue) {
        double bottom = pos.y;
        double top = pos.y + 2.0;
        for (int i = 0; i <= 10; i++) {
            double t = i / 10.0;
            Vec3 horizontal = rightVector.scale(-0.55 + 1.1 * t);
            Vec3 left = pos.add(rightVector.scale(-0.55));
            Vec3 right = pos.add(rightVector.scale(0.55));
            spawnPortalParticle(level, pos.x + horizontal.x, bottom, pos.z + horizontal.z, blue);
            spawnPortalParticle(level, pos.x + horizontal.x, top, pos.z + horizontal.z, blue);
            spawnPortalParticle(level, left.x, bottom + (top - bottom) * t, left.z, blue);
            spawnPortalParticle(level, right.x, bottom + (top - bottom) * t, right.z, blue);
        }
    }

    private static void spawnPortalParticle(ServerLevel level, double x, double y, double z, boolean blue) {
        level.sendParticles(blue ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
                x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
    }

    private static void playPortalSound(ServerLevel level, Vec3 pos) {
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5F, 1.25F);
    }

    private static Vec3 findSafePortalPos(Level level, Vec3 desired) {
        BlockPos base = BlockPos.containing(desired);
        if (!level.getBlockState(base).isAir()) base = base.above();
        if (!level.getBlockState(base.above()).isAir()) base = base.above();
        return new Vec3(base.getX() + 0.5, base.getY(), base.getZ() + 0.5);
    }

    private static Vec3 getPortalRightVector(Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0, look.z);
        if (horizontal.lengthSqr() < 1.0E-4) {
            return new Vec3(1, 0, 0);
        }
        horizontal = horizontal.normalize();
        return new Vec3(-horizontal.z, 0, horizontal.x);
    }

    private static final class PortalPair {
        private final Vec3 from;
        private final Vec3 to;
        private final Vec3 right;
        private final float yaw;
        private final long expireTick;
        private long nextTeleportTick;

        private PortalPair(Vec3 from, Vec3 to, Vec3 right, float yaw, long expireTick) {
            this.from = from;
            this.to = to;
            this.right = right;
            this.yaw = yaw;
            this.expireTick = expireTick;
            this.nextTeleportTick = 0;
        }
    }
}
