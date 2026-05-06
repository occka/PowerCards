package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class SpeedCard extends PowerCard {

    public SpeedCard(Item.Properties properties) { super(properties); }

    @Override public Component getCardName()        { return Component.translatable("item.powercaeds.speed_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.speed_card.desc"); }
    @Override public int getCooldownTicks()          { return 10 * 20; }
    @Override public int getPrimaryColor()           { return 0xFFCC00; }
    @Override public int getSecondaryColor()         { return 0x111111; }

    @Override
    public int[] getSymbolPattern() {
        return new int[]{ 0b00110, 0b00100, 0b01110, 0b00100, 0b01100, 0b01000 };
    }

    @Override
    public void applyEffect(Player player, Level level) {
        if (level.isClientSide()) return;
        // Lookup speed effect by registry key — avoids hardcoded field name issues
        Holder<MobEffect> speed = BuiltInRegistries.MOB_EFFECT
                .get(Identifier.withDefaultNamespace("speed"))
                .orElseThrow(() -> new IllegalStateException("Speed effect not found"));
        player.addEffect(new MobEffectInstance(speed, 5 * 20, 1, false, false, true));
    }
}
