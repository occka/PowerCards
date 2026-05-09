package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class TurmCard extends PowerCard {
    private static final Set<UUID> SLAMS = new HashSet<>();

    public TurmCard(Item.Properties properties) { super(properties); }
    @Override public Component getCardName() { return Component.translatable("item.powercaeds.turm_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.turm_card.desc"); }
    @Override public int getCooldownTicks() { return 30 * 20; }
    @Override public int getPrimaryColor() { return 0x050505; }
    @Override public int getSecondaryColor() { return 0x777777; }
    @Override public int getStructureChestLootWeight() { return 1; }
    @Override public boolean allowsDuplicateEquip() { return true; }
    @Override public int[] getSymbolPattern() { return new int[]{0b10101,0b01010,0b10101,0b01010,0b10101,0b01010}; }

    @Override
    public void applyEffect(Player player, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!player.onGround()) {
            SLAMS.add(player.getUUID());
            player.setDeltaMovement(0, -2.8, 0);
            player.hurtMarked = true;
            return;
        }

        Vec3 dir = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z);
        if (dir.lengthSqr() < 1.0E-4) dir = new Vec3(0, 0, 1);
        dir = dir.normalize();
        Vec3 start = player.position();
        for (int i = 1; i <= 10; i++) {
            Vec3 point = start.add(dir.scale(i));
            BlockPos center = BlockPos.containing(point.add(0, 1, 0));
            for (int x = -1; x <= 1; x++) for (int y = -1; y <= 1; y++) for (int z = -1; z <= 1; z++) {
                serverLevel.destroyBlock(center.offset(x, y, z), true, player);
            }
            damageAround(serverLevel, player, point, 3.0, 14.0F, dir.scale(1.6).add(0, 0.35, 0));
        }
        player.teleportTo(start.x + dir.x * 10, player.getY(), start.z + dir.z * 10);
    }

    public static void serverTick(MinecraftServer server) {
        Iterator<UUID> iterator = SLAMS.iterator();
        while (iterator.hasNext()) {
            UUID id = iterator.next();
            Player player = server.getPlayerList().getPlayer(id);
            if (player == null) {
                iterator.remove();
                continue;
            }
            if (player.onGround() && player.level() instanceof ServerLevel level) {
                level.explode(player, player.getX(), player.getY(), player.getZ(), 6.0F, Level.ExplosionInteraction.TNT);
                damageAround(level, player, player.position(), 6.0, 18.0F, new Vec3(0, 1.6, 0));
                player.fallDistance = 0;
                iterator.remove();
            }
        }
    }

    private static void damageAround(ServerLevel level, Player player, Vec3 center, double radius, float damage, Vec3 knockback) {
        AABB box = AABB.ofSize(center, radius * 2, radius * 2, radius * 2);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive())) {
            if (entity.distanceToSqr(center) > radius * radius) continue;
            entity.hurt(player.damageSources().playerAttack(player), damage);
            Vec3 away = entity.position().subtract(center).normalize().scale(knockback.length()).add(0, knockback.y, 0);
            entity.setDeltaMovement(away);
            entity.hurtMarked = true;
        }
    }
}
