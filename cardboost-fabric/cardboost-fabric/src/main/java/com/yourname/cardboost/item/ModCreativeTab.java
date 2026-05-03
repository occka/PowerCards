package com.yourname.cardboost.item;

import com.yourname.cardboost.CardBoostMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ModCreativeTab {
    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(CardBoostMod.MOD_ID, "cards"),
            FabricItemGroup.builder()
                .title(Component.translatable("itemGroup.cardboost.cards"))
                .icon(() -> new ItemStack(ModItems.CARD_SPEED))
                .displayItems((params, output) -> {
                    output.accept(ModItems.CARD_SPEED);
                    output.accept(ModItems.CARD_SHIELD);
                })
                .build()
        );
    }
}
