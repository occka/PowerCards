package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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

    public NovaCard(Item.Properties properties) { super(properties); }
    @Override public Component getCardName() { return Component.translatable("item.powercaeds.nova_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.nova_card.desc"); }
    @Override public int getCooldownTicks() { return 25 * 20; }
    @Override public int getPrimaryColor() { return 0x59BFFF; }
    @Override public int getSecondaryColor() { return 0xFF8A2A; }
    @Override public int getStructureChestLootWeight() { return 1; }

    @Override
    public void applyEffect(Player player, Level level) {
        if (!(level instanceof ServerLevel)) return;
        Vec3 from = findSafePortalPos(player.level(), player.position().add(player.getLookAngle().scale(2.0)));
        HitResult hit = player.level().clip(new ClipContext(player.getEyePosition(), player.getEyePosition().add(player.getLookAngle().scale(500.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 target = hit.getLocation();
        Vec3 to = findSafePortalPos(player.level(), target);
        PORTALS.put(player.getUUID(), new PortalPair(from, to, player.getYRot(), player.level().getGameTime() + 20 * 20));
    }

    @Override
    public void tickEffect(Player player, Level level, int slotIndex) {
        PortalPair pair = PORTALS.get(player.getUUID());
        if (pair == null) return;
        if (level.getGameTime() > pair.expireTick) { PORTALS.remove(player.getUUID()); return; }

        AABB entry = AABB.ofSize(pair.from.add(0, 1, 0), 1.2, 2.1, 0.6);
        if (entry.intersects(player.getBoundingBox())) {
            player.teleportTo(pair.to.x, pair.to.y, pair.to.z);
            player.setYRot(pair.yaw);
            PORTALS.remove(player.getUUID());
        }
    }

    private static Vec3 findSafePortalPos(Level level, Vec3 desired) {
        BlockPos base = BlockPos.containing(desired);
        if (!level.getBlockState(base).isAir()) base = base.above();
        if (!level.getBlockState(base.above()).isAir()) base = base.above();
        return new Vec3(base.getX() + 0.5, base.getY(), base.getZ() + 0.5);
    }

    private record PortalPair(Vec3 from, Vec3 to, float yaw, long expireTick) {}
}
