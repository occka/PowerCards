package cards.modid.card;

import cards.modid.PowerCaeds;
import cards.modid.card.impl.SpeedCard;
import net.fabricmc.fabric.api.itemgroup.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * HOW TO ADD A NEW CARD:
 *   1. Create YourCard extends PowerCard in cards.modid.card.impl
 *   2. Add: public static final YourCard YOUR_CARD = register("your_card", new YourCard(new Item.Properties()));
 *   3. Done.
 */
public class CardRegistry {

    // ── Cards ─────────────────────────────────────────────────────────────────
    public static final SpeedCard SPEED_CARD =
            register("speed_card", new SpeedCard(new Item.Properties()));

    // ── Internal ──────────────────────────────────────────────────────────────
    private static <T extends PowerCard> T register(String name, T card) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(PowerCaeds.MOD_ID, name));
        Registry.register(BuiltInRegistries.ITEM, key, card);
        return card;
    }

    public static void init() {
        // Add all cards to the TOOLS_AND_UTILITIES tab (no custom tab needed,
        // avoids FabricItemGroup API issues — easy to change later)
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(
                entries -> BuiltInRegistries.ITEM.stream()
                        .filter(i -> i instanceof PowerCard)
                        .map(ItemStack::new)
                        .forEach(entries::accept)
        );

        PowerCaeds.LOGGER.info("[PowerCaeds] CardRegistry initialized — {} card(s).", getCount());
    }

    public static long getCount() {
        return BuiltInRegistries.ITEM.stream().filter(i -> i instanceof PowerCard).count();
    }
}
