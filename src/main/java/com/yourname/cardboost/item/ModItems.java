package com.yourname.cardboost.item;

import com.yourname.cardboost.CardBoostMod;
import com.yourname.cardboost.card.CardRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ModItems {
    public static Item CARD_SPEED;
    public static Item CARD_SHIELD;

    public static void register() {
        CARD_SPEED = Registry.register(BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath(CardBoostMod.MOD_ID, "card_speed"),
            new CardItem(CardRegistry.getById("speed")));

        CARD_SHIELD = Registry.register(BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath(CardBoostMod.MOD_ID, "card_shield"),
            new CardItem(CardRegistry.getById("shield")));
    }
}
