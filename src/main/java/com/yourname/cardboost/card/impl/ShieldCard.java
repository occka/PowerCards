package com.yourname.cardboost.card.impl;

import com.yourname.cardboost.card.Card;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class ShieldCard extends Card {
    public ShieldCard() {
        super("shield", 300, 0xCC2222, 0xFFFFFF);
    }

    @Override
    public void onActivate(Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 1));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 0));
    }

    @Override
    public String getDescription() { return "Сопротивление II + Поглощение на 5 сек. КД: 15 сек."; }

    @Override
    public String getSymbol() { return "O"; }
}
