package cards.modid;

import cards.modid.card.CardRegistry;
import cards.modid.card.impl.ForcewallCard;
import cards.modid.card.impl.FreezeCard;
import cards.modid.card.impl.ArtzCard;
import cards.modid.card.impl.InvisCard;
import cards.modid.card.impl.Md49Card;
import cards.modid.card.impl.TimeStopCard;
import cards.modid.handler.CardDeathHandler;
import cards.modid.handler.CardLootHandler;
import cards.modid.handler.CardTickHandler;
import cards.modid.handler.PlayerJoinHandler;
import cards.modid.network.ActivateCardPacket;
import cards.modid.network.EquipCardPacket;
import cards.modid.network.SyncCardSlotsPacket;
import cards.modid.network.UnequipCardPacket;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PowerCaeds implements ModInitializer {
    public static final String MOD_ID = "powercaeds";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[PowerCaeds] Initializing...");

        // 1. Items / cards
        CardRegistry.init();

        // 2. Chest loot injection
        CardLootHandler.register();

        // 3. Network packets
        SyncCardSlotsPacket.register();   // S2C
        ActivateCardPacket.register();    // C2S — activate card by keybind
        EquipCardPacket.register();       // C2S — equip card from inventory
        UnequipCardPacket.register();     // C2S — unequip card back to inventory

        // 4. Server-side event handlers
        ForcewallCard.registerProtectionEvent();
        FreezeCard.registerFreezeEvents();
        TimeStopCard.registerEvents();
        InvisCard.registerEvents();
        ArtzCard.registerEvents();
        Md49Card.registerEvents();
        CardTickHandler.register();
        CardDeathHandler.register();
        PlayerJoinHandler.register();

        LOGGER.info("[PowerCaeds] Ready! {} card(s) registered.", CardRegistry.getCount());
    }
}
