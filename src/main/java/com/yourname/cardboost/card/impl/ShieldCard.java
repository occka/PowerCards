package com.yourname.cardboost.card.impl;

import com.yourname.cardboost.card.Card;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class ShieldCard extends Card {
    public ShieldCard() {
        super("shield", 300, 0xCC2222, 0xFFFFFF);
    }

    @Override
    public void onActivate(Player player) {
        var resistance = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.withDefaultNamespace("resistance"));
        var absorption = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.withDefaultNamespace("absorption"));
        if (resistance != null) player.addEffect(new MobEffectInstance(resistance, 100, 1));
        if (absorption != null) player.addEffect(new MobEffectInstance(absorption, 100, 0));
    }

    @Override
    public String getDescription() { return "Сопротивление II + Поглощение на 5 сек. КД: 15 сек."; }

    @Override
    public String getSymbol() { return "O"; }
}
