package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.fish.Cod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DeepCard extends PowerCard {
    private static final String TORPEDO_NAME = "powercaeds_deep_torpedo";

    public DeepCard(Item.Properties properties) { super(properties); }
    @Override public Component getCardName() { return Component.translatable("item.powercaeds.deep_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.deep_card.desc"); }
    @Override public int getCooldownTicks() { return 20 * 20; }
    @Override public int getPrimaryColor() { return 0x0B6F7A; }
    @Override public int getSecondaryColor() { return 0x38D6A4; }
    @Override public int getStructureChestLootWeight() { return 2; }
    @Override public boolean allowsDuplicateEquip() { return true; }
    @Override public int[] getSymbolPattern() { return new int[]{0b00110,0b01111,0b11110,0b01111,0b00110,0b00000}; }

    @Override
    public void applyEffect(Player player, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        Vec3 look = player.getLookAngle().normalize();
        Vec3 right = new Vec3(-look.z, 0, look.x);
        if (right.lengthSqr() < 1.0E-4) right = new Vec3(1, 0, 0);
        right = right.normalize();

        spawnFish(serverLevel, player.position().add(right.scale(1.4)), player.isInWater() ? look.scale(1.4) : Vec3.ZERO);
        spawnFish(serverLevel, player.position().add(right.scale(-1.4)), player.isInWater() ? look.scale(1.4) : Vec3.ZERO);
    }

    @Override
    public void tickEffect(Player player, Level level, int slotIndex) {
        if (player.isInWater()) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 60, 0, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60, 0, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 60, 0, false, false, true));
        }
    }

    public static void serverTick(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (!entity.hasCustomName() || entity.getCustomName() == null || !TORPEDO_NAME.equals(entity.getCustomName().getString())) continue;
                if (entity.tickCount > 100 || entity.horizontalCollision || entity.verticalCollision) {
                    explode(level, entity);
                    continue;
                }
                for (LivingEntity hit : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(1.0), e -> e != entity && e.isAlive())) {
                    explode(level, entity);
                    break;
                }
            }
        }
    }

    private static void spawnFish(ServerLevel level, Vec3 pos, Vec3 velocity) {
        Cod fish = new Cod(EntityType.COD, level);
        fish.setPos(pos.x, pos.y + 1.0, pos.z);
        fish.setDeltaMovement(velocity);
        if (velocity.lengthSqr() > 0) {
            fish.setCustomName(Component.literal(TORPEDO_NAME));
            fish.setCustomNameVisible(false);
            fish.setNoGravity(true);
        }
        level.addFreshEntity(fish);
    }

    private static void explode(ServerLevel level, Entity entity) {
        level.explode(entity, entity.getX(), entity.getY(), entity.getZ(), 7.0F, Level.ExplosionInteraction.TNT);
        AABB area = entity.getBoundingBox().inflate(7.0);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (living.distanceToSqr(entity.position()) <= 49.0) {
                living.hurt(living.damageSources().explosion(entity, null), 18.0F);
            }
        }
        entity.discard();
    }
}
