package com.yourname.cardboost;

import com.yourname.cardboost.card.CardRegistry;
import com.yourname.cardboost.item.ModItems;
import com.yourname.cardboost.network.ModNetwork;
import com.yourname.cardboost.player.CardSlotManager;
import com.yourname.cardboost.player.PlayerDataSaver;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class CardBoostMod implements ModInitializer {
    public static final String MOD_ID = "cardboost";

    @Override
    public void onInitialize() {
        CardRegistry.init();
        ModItems.register();
        ModNetwork.register();
        PlayerDataSaver.register();

        ServerTickEvents.END_SERVER_TICK.register(server ->
            server.getPlayerList().getPlayers().forEach(player ->
                CardSlotManager.get(player).tick(player)
            )
        );
    }
}
