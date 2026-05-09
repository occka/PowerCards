package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import cards.modid.component.CardSlotsComponent;
import cards.modid.network.SyncCardSlotsPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlazeCard extends PowerCard {
    private static final Map<UUID, FlightState> FLIGHTS = new HashMap<>();
    private static final int FLIGHT_TICKS = 5 * 60 * 20;
    private static final int COOLDOWN_TICKS = 30 * 20;
    private static final double SPEED = 1.25;

    public BlazeCard(Item.Properties properties) { super(properties); }

    @Override public Component getCardName() { return Component.translatable("item.powercaeds.blaze_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.blaze_card.desc"); }
    @Override public int getCooldownTicks() { return COOLDOWN_TICKS; }
    @Override public int getPrimaryColor() { return 0xB51E14; }
    @Override public int getSecondaryColor() { return 0xFF8A18; }
    @Override public int getStructureChestLootWeight() { return 2; }
    @Override public boolean usesDefaultCooldown() { return false; }

    @Override
    public int[] getSymbolPattern() {
        return new int[]{ 0b00100, 0b01110, 0b11111, 0b10101, 0b01110, 0b00100 };
    }

    @Override
    public void applyEffect(Player player, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        FLIGHTS.put(player.getUUID(), new FlightState(level.getGameTime(), level.getGameTime() + FLIGHT_TICKS));
        player.fallDistance = 0;
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    @Override
    public void tickEffect(Player player, Level level, int slotIndex) {
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, false, false, true));
        FlightState state = FLIGHTS.get(player.getUUID());
        if (state == null) return;

        long now = level.getGameTime();
        if (now >= state.expireTick || (now - state.startTick > 10 && player.onGround()) || player.isInWaterOrRain()) {
            endFlight(player, level, slotIndex);
            return;
        }

        Vec3 dir = player.getLookAngle().normalize();
        player.startFallFlying();
        player.setDeltaMovement(dir.scale(SPEED));
        player.hurtMarked = true;
        player.fallDistance = 0;

        if (level instanceof ServerLevel serverLevel) {
            Vec3 pos = player.position().add(0, player.getEyeHeight() * 0.5, 0).subtract(dir.scale(0.8));
            serverLevel.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 8, 0.25, 0.25, 0.25, 0.02);
            serverLevel.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 3, 0.2, 0.2, 0.2, 0.01);
        }
    }

    private static void endFlight(Player player, Level level, int slotIndex) {
        FLIGHTS.remove(player.getUUID());
        player.fallDistance = 0;
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20, 0, false, false, true));

        if (player instanceof ServerPlayer serverPlayer) {
            CardSlotsComponent comp = serverPlayer.getAttachedOrCreate(CardSlotsComponent.TYPE);
            comp.setCooldown(slotIndex, COOLDOWN_TICKS);
            SyncCardSlotsPacket.send(serverPlayer, comp);
        }
    }

    private record FlightState(long startTick, long expireTick) {}
}
