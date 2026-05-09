package cards.modid.card.impl;

import cards.modid.component.CardSlotsComponent;
import cards.modid.card.PowerCard;
import cards.modid.network.SyncCardSlotsPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class GlockCard extends PowerCard {
    private static final String CHARGES_KEY = "glock_charges";
    private static final int MAX_CHARGES = 8;
    private static final int RELOAD_TICKS = 30 * 20;
    private static final double RANGE = 50.0;

    public GlockCard(Item.Properties properties) { super(properties); }
    @Override public Component getCardName() { return Component.translatable("item.powercaeds.glock_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.glock_card.desc"); }
    @Override public int getCooldownTicks() { return RELOAD_TICKS; }
    @Override public int getPrimaryColor() { return 0x111111; }
    @Override public int getSecondaryColor() { return 0xB8860B; }
    @Override public int getStructureChestLootWeight() { return 2; }
    @Override public boolean usesDefaultCooldown() { return false; }

    @Override public boolean isBarVisible(ItemStack stack) { return true; }
    @Override public int getBarWidth(ItemStack stack) { return Math.round(13.0F * getCharges(stack) / MAX_CHARGES); }
    @Override public int getBarColor(ItemStack stack) { return 0x111111; }

    @Override
    public void applyEffect(Player player, Level level) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer sp)) return;
        CardSlotsComponent comp = sp.getAttachedOrCreate(CardSlotsComponent.TYPE);

        for (int slot = 0; slot < CardSlotsComponent.SLOT_COUNT; slot++) {
            if (!comp.hasCard(slot) || comp.getCardItem(slot) != this) continue;
            ItemStack stack = comp.getCard(slot);
            int charges = getCharges(stack);
            if (charges <= 0) return;

            shootSingleTarget(player, level);
            setCharges(stack, charges - 1);
            if (charges - 1 <= 0) {
                comp.setCooldown(slot, RELOAD_TICKS);
            }
            comp.setCard(slot, stack);
            SyncCardSlotsPacket.send(sp, comp);
            return;
        }
    }

    @Override
    public void tickEffect(Player player, Level level, int slotIndex) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer sp)) return;

        CardSlotsComponent comp = sp.getAttachedOrCreate(CardSlotsComponent.TYPE);
        if (comp.isOnCooldown(slotIndex)) return;

        ItemStack stack = comp.getCard(slotIndex);
        if (!stack.isEmpty() && getCharges(stack) <= 0) {
            setCharges(stack, MAX_CHARGES);
            comp.setCard(slotIndex, stack);
            SyncCardSlotsPacket.send(sp, comp);
        }
    }

    private void shootSingleTarget(Player player, Level level) {
        Vec3 start = player.getEyePosition();
        Vec3 dir = player.getLookAngle().normalize();
        Vec3 end = start.add(dir.scale(RANGE));

        Entity hit = null;
        double best = Double.MAX_VALUE;
        for (Entity entity : level.getEntities(player, new AABB(start, end).inflate(1.0), e -> e instanceof LivingEntity && e.isAlive())) {
            Vec3 center = entity.getBoundingBox().getCenter();
            double t = center.subtract(start).dot(dir);
            if (t < 0 || t > RANGE) continue;
            Vec3 closest = start.add(dir.scale(t));
            if (center.distanceTo(closest) > 1.0) continue;
            if (t < best) { best = t; hit = entity; }
        }

        Vec3 visualEnd = hit == null ? end : start.add(dir.scale(best));
        spawnShotVisual(player, level, start, dir, visualEnd);

        if (hit instanceof LivingEntity living) {
            living.hurt(player.damageSources().playerAttack(player), 4.0F);
        }
    }

    private void spawnShotVisual(Player player, Level level, Vec3 start, Vec3 dir, Vec3 end) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        double length = start.distanceTo(end);
        int steps = Math.max(4, (int) (length * 3.0));
        for (int i = 0; i <= steps; i++) {
            Vec3 point = start.add(dir.scale(length * i / steps));
            serverLevel.sendParticles(ParticleTypes.CRIT, point.x, point.y, point.z, 1, 0.01, 0.01, 0.01, 0.0);
        }
        serverLevel.sendParticles(ParticleTypes.SMOKE, end.x, end.y, end.z, 6, 0.08, 0.08, 0.08, 0.01);
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 0.7F, 1.8F);
    }

    private static int getCharges(ItemStack stack) {
        int value = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY)
                .copyTag().getInt(CHARGES_KEY).orElse(MAX_CHARGES);
        return Math.clamp(value, 0, MAX_CHARGES);
    }

    private static void setCharges(ItemStack stack, int charges) {
        var tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        tag.putInt(CHARGES_KEY, Math.max(0, charges));
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
    }
}
