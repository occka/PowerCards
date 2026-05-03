package com.yourname.cardboost.card.impl;

import com.yourname.cardboost.card.Card;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class SpeedCard extends Card {
    public SpeedCard() {
        super("speed", 200, 0xFFD700, 0x1A1A1A);
    }

    @Override
    public void onActivate(Player player) {
        var effect = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.withDefaultNamespace("speed"));
        if (effect != null) player.addEffect(new MobEffectInstance(effect, 100, 1));
    }

    @Override
    public String getDescription() { return "Скорость II на 5 сек. КД: 10 сек."; }

    @Override
    public String getSymbol() { return "!"; }
}
