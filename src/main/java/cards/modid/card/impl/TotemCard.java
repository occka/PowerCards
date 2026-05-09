package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class TotemCard extends PowerCard {

    public TotemCard(Item.Properties properties) { super(properties); }

    @Override public Component getCardName()        { return Component.translatable("item.powercaeds.totem_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.totem_card.desc"); }
    @Override public int getCooldownTicks()          { return 5 * 60 * 20; }
    @Override public int getPrimaryColor()           { return 0x2ECC40; }
    @Override public int getSecondaryColor()         { return 0xFFD83D; }
    @Override public int getStructureChestLootWeight() { return 1; }
    @Override public boolean allowsDuplicateEquip() { return true; }

    @Override
    public int[] getSymbolPattern() {
        return new int[]{ 0b01110, 0b10101, 0b11111, 0b01110, 0b00100, 0b01110 };
    }

    @Override public boolean canActivate() { return false; }

    @Override
    public void applyEffect(Player player, Level level) {
        // Passive card: effect is triggered by tryPreventDeath.
    }

    @Override
    public boolean tryPreventDeath(Player player, Level level, DamageSource source, float amount, int slotIndex) {
        if (level.isClientSide()) return false;

        player.setHealth(1.0F);
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(effect("regeneration"), 45 * 20, 1));
        player.addEffect(new MobEffectInstance(effect("absorption"), 5 * 20, 1));
        player.addEffect(new MobEffectInstance(effect("fire_resistance"), 40 * 20, 0));

        level.broadcastEntityEvent(player, (byte) 35);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private static Holder<MobEffect> effect(String name) {
        return BuiltInRegistries.MOB_EFFECT
                .get(Identifier.withDefaultNamespace(name))
                .orElseThrow(() -> new IllegalStateException(name + " effect not found"));
    }
}
