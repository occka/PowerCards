package com.yourname.cardboost.card;

import net.minecraft.world.entity.player.Player;

public abstract class Card {
    private final String id;
    private final int cooldownTicks;
    private final int primaryColor;
    private final int secondaryColor;

    protected Card(String id, int cooldownTicks, int primaryColor, int secondaryColor) {
        this.id = id;
        this.cooldownTicks = cooldownTicks;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }

    public abstract void onActivate(Player player);
    public abstract String getDescription();
    public void onTick(Player player) {}
    public String getSymbol() { return "?"; }

    public String getId() { return id; }
    public int getCooldownTicks() { return cooldownTicks; }
    public int getPrimaryColor() { return primaryColor; }
    public int getSecondaryColor() { return secondaryColor; }
}
