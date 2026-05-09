package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import cards.modid.component.CardSlotsComponent;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ArtzCard extends PowerCard {
    private static boolean applyingExtraDamage = false;

    public ArtzCard(Item.Properties properties) { super(properties); }
    @Override public Component getCardName() { return Component.translatable("item.powercaeds.artz_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.artz_card.desc"); }
    @Override public int getCooldownTicks() { return 0; }
    @Override public int getPrimaryColor() { return 0x22B8A8; }
    @Override public int getSecondaryColor() { return 0xD9C8A4; }
    @Override public int getStructureChestLootWeight() { return 2; }
    @Override public boolean canActivate() { return false; }
    @Override public int[] getSymbolPattern() { return new int[]{0b10001,0b01010,0b00100,0b01110,0b10101,0b10001}; }

    @Override
    public void applyEffect(Player player, Level level) {
    }

    @Override
    public void tickEffect(Player player, Level level, int slotIndex) {
        player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, 2, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 60, 4, false, false, true));
        player.fallDistance = 0;
        if (player.horizontalCollision && !player.onGround()) {
            player.setDeltaMovement(player.getDeltaMovement().x, 0.22, player.getDeltaMovement().z);
            player.hurtMarked = true;
        }
        if (level instanceof ServerLevel serverLevel) {
            Vec3 back = player.getLookAngle().scale(-0.7);
            for (int i = 0; i < 4; i++) {
                double side = i < 2 ? -0.45 : 0.45;
                double height = i % 2 == 0 ? 0.2 : 1.6;
                serverLevel.sendParticles(ParticleTypes.SQUID_INK,
                        player.getX() + back.x + side, player.getY() + height, player.getZ() + back.z,
                        2, 0.08, 0.08, 0.08, 0.0);
            }
        }
    }

    public static void registerEvents() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (applyingExtraDamage || !(source.getEntity() instanceof Player player) || !hasArtz(player)) return;
            applyingExtraDamage = true;
            entity.hurt(player.damageSources().playerAttack(player), baseDamageTaken);
            applyingExtraDamage = false;
        });
    }

    private static boolean hasArtz(Player player) {
        CardSlotsComponent comp = player.getAttachedOrCreate(CardSlotsComponent.TYPE);
        for (int i = 0; i < CardSlotsComponent.SLOT_COUNT; i++) {
            if (comp.hasCard(i) && comp.getCardItem(i) instanceof ArtzCard) return true;
        }
        return false;
    }
}
