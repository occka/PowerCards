package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DashCard extends PowerCard {

    /**
     * Импульс в блоках/тик.
     * getLookAngle() возвращает единичный вектор во всех направлениях,
     * включая вверх/вниз — рывок работает по взгляду в любую сторону.
     */
    private static final double DASH_POWER      = 3.0;
    private static final int    SLOW_FALL_TICKS = 6 * 20;

    public DashCard(Item.Properties properties) { super(properties); }

    @Override public Component getCardName()        { return Component.translatable("item.powercaeds.dash_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.dash_card.desc"); }
    @Override public int getCooldownTicks()          { return 8 * 20; }
    @Override public int getPrimaryColor()           { return 0x7FD8FF; }
    @Override public int getSecondaryColor()         { return 0x1A5FCC; }
    @Override public int getStructureChestLootWeight() { return 5; }
    @Override public boolean allowsDuplicateEquip() { return true; }

    @Override
    public int[] getSymbolPattern() {
        return new int[]{
            0b00000,
            0b00100,
            0b01110,
            0b11111,
            0b01110,
            0b00100
        };
    }

    @Override
    public void applyEffect(Player player, Level level) {
        if (level.isClientSide()) return;

        // Единичный вектор взгляда * сила = импульс рывка
        Vec3 impulse = player.getLookAngle().scale(DASH_POWER);
        player.setDeltaMovement(impulse);

        // Сообщаем серверу что скорость изменилась — клиент получит сразу
        player.hurtMarked = true;

        // Сброс падения чтобы не получить урон в начале рывка
        player.fallDistance = 0f;

        // Облака в точке рывка
        if (level instanceof ServerLevel serverLevel) {
            Vec3 pos = player.position().add(0, player.getEyeHeight() * 0.5, 0);
            serverLevel.sendParticles(
                    ParticleTypes.CLOUD,
                    pos.x, pos.y, pos.z,
                    24,
                    0.35, 0.35, 0.35,
                    0.04
            );
        }

        // Плавное падение на 3 секунды после рывка
        player.addEffect(new MobEffectInstance(
                MobEffects.SLOW_FALLING, SLOW_FALL_TICKS, 0, false, false, true));
    }
}
