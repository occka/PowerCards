package cards.modid.handler;

import cards.modid.card.CardRegistry;
import cards.modid.card.PowerCard;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * Injects power cards into chest loot tables.
 *
 * Rarity is controlled per card by overriding
 * {@link PowerCard#getStructureChestLootWeight()}:
 *   0  = never appears in chests
 *   1  = very rare
 *   5  = default
 *   10 = more common
 */
public class CardLootHandler {

    /** Empty entry keeps cards rare while still checking every chest loot table. */
    private static final int EMPTY_WEIGHT = 100;

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!isChestLootTable(key.identifier().getPath())) return;

            LootPool.Builder pool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(EmptyLootItem.emptyItem().setWeight(EMPTY_WEIGHT));

            boolean hasAnyCard = false;
            for (PowerCard card : CardRegistry.getCards()) {
                int weight = card.getStructureChestLootWeight();
                if (weight <= 0) continue;

                pool.add(LootItem.lootTableItem(card).setWeight(weight));
                hasAnyCard = true;
            }

            if (hasAnyCard) {
                tableBuilder.withPool(pool);
            }
        });
    }

    private static boolean isChestLootTable(String path) {
        return path.startsWith("chests/");
    }
}
