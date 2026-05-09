package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class InvisCard extends PowerCard {
    private static final Set<UUID> ACTIVE = new HashSet<>();

    public InvisCard(Item.Properties properties) { super(properties); }
    @Override public Component getCardName() { return Component.translatable("item.powercaeds.invis_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.invis_card.desc"); }
    @Override public int getCooldownTicks() { return 120 * 20; }
    @Override public int getPrimaryColor() { return 0xF8F8F8; }
    @Override public int getSecondaryColor() { return 0x111111; }
    @Override public int getStructureChestLootWeight() { return 2; }
    @Override public int[] getSymbolPattern() { return new int[]{0b11111,0b10001,0b10101,0b10001,0b11111,0b00000}; }

    @Override
    public void applyEffect(Player player, Level level) {
        ACTIVE.add(player.getUUID());
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 10 * 60 * 20, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 10 * 60 * 20, 1, false, false, true));
    }

    public static void registerEvents() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            cancel(player);
            return InteractionResult.PASS;
        });
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            cancel(player);
            return true;
        });
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            cancel(player);
            return InteractionResult.PASS;
        });
    }

    private static void cancel(Player player) {
        if (!ACTIVE.remove(player.getUUID())) return;
        player.removeEffect(MobEffects.INVISIBILITY);
        player.removeEffect(MobEffects.RESISTANCE);
    }
}
