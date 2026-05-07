package cards.modid.card.impl;

import cards.modid.component.CardSlotsComponent;
import cards.modid.card.PowerCard;
import cards.modid.network.SyncCardSlotsPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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

    public GlockCard(Item.Properties properties) { super(properties); }
    @Override public Component getCardName() { return Component.translatable("item.powercaeds.glock_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.glock_card.desc"); }
    @Override public int getCooldownTicks() { return 0; }
    @Override public int getPrimaryColor() { return 0x111111; }
    @Override public int getSecondaryColor() { return 0xB8860B; }
    @Override public int getStructureChestLootWeight() { return 2; }

    @Override public boolean isBarVisible(ItemStack stack) { return true; }
    @Override public int getBarWidth(ItemStack stack) { return Math.max(1, Math.round(13.0F * getCharges(stack) / MAX_CHARGES)); }
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
                comp.setCooldown(slot, 30 * 20);
                setCharges(stack, MAX_CHARGES);
            }
            comp.setCard(slot, stack);
            SyncCardSlotsPacket.send(sp, comp);
            return;
        }
    }

    private void shootSingleTarget(Player player, Level level) {
        Vec3 start = player.getEyePosition();
        Vec3 dir = player.getLookAngle().normalize();
        Vec3 end = start.add(dir.scale(50.0));

        Entity hit = null;
        double best = Double.MAX_VALUE;
        for (Entity entity : level.getEntities(player, new AABB(start, end).inflate(1.0), e -> e instanceof LivingEntity && e.isAlive())) {
            Vec3 center = entity.getBoundingBox().getCenter();
            double t = center.subtract(start).dot(dir);
            if (t < 0 || t > 50.0) continue;
            Vec3 closest = start.add(dir.scale(t));
            if (center.distanceTo(closest) > 1.0) continue;
            if (t < best) { best = t; hit = entity; }
        }
        if (hit instanceof LivingEntity living) {
            living.hurt(player.damageSources().playerAttack(player), 4.0F);
        }
    }

    private static int getCharges(ItemStack stack) {
        int value = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY)
                .copyTag().getInt(CHARGES_KEY).orElse(MAX_CHARGES);
        return value <= 0 ? MAX_CHARGES : Math.min(MAX_CHARGES, value);
    }

    private static void setCharges(ItemStack stack, int charges) {
        var tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        tag.putInt(CHARGES_KEY, Math.max(0, charges));
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
    }
}
