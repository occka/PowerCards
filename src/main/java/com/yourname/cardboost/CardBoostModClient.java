package com.yourname.cardboost;

import com.yourname.cardboost.hud.CardHudRenderer;
import com.yourname.cardboost.item.ModCreativeTab;
import com.yourname.cardboost.keybind.ModKeybinds;
import net.fabricmc.api.ClientModInitializer;

public class CardBoostModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModKeybinds.register();
        CardHudRenderer.register();
        ModCreativeTab.register();
    }
}
