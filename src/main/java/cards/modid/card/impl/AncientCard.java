package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AncientCard extends PowerCard {

    public AncientCard(Item.Properties properties) { super(properties); }

    @Override public Component getCardName() { return Component.translatable("item.powercaeds.ancient_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.ancient_card.desc"); }
    @Override public int getCooldownTicks() { return 16 * 20; }
    @Override public int getPrimaryColor() { return 0x8FAF8F; }
    @Override public int getSecondaryColor() { return 0x5E6E5E; }
    @Override public int getStructureChestLootWeight() { return 1; }

    @Override
    public void applyEffect(Player player, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        Vec3 start = player.getEyePosition();
        Vec3 dir = player.getLookAngle().normalize();
        double length = 15.0;
        Vec3 end = start.add(dir.scale(length));

        for (int i = 1; i <= 30; i++) {
            Vec3 point = start.add(dir.scale(length * (i / 30.0)));
            serverLevel.sendParticles(ParticleTypes.ENCHANT, point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }

        AABB beamBox = new AABB(start, end).inflate(1.0);
        List<Entity> hits = serverLevel.getEntities(player, beamBox, e -> e instanceof LivingEntity && e.isAlive());
        for (Entity entity : hits) {
            if (!(entity instanceof LivingEntity living)) continue;
            Vec3 toTarget = living.getEyePosition().subtract(start);
            double along = toTarget.dot(dir);
            if (along < 0 || along > length) continue;
            Vec3 closest = start.add(dir.scale(along));
            if (living.getEyePosition().distanceTo(closest) > 1.0) continue;

            living.hurt(player.damageSources().magic(), 10.0F);
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 5 * 20, 1));
        }

        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.8F, 1.2F);
    }
}
