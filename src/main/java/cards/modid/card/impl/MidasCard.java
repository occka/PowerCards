package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class MidasCard extends PowerCard {

    /** Scale multiplier applied to the player while the card is equipped */
    private static final double MIDAS_SCALE = 1.5;

    /**
     * Items that should NOT be replaced (power cards are already excluded
     * by the instanceof PowerCard guard in tickEffect).
     */
    private static final Map<Item, Item> GOLD_REPLACEMENTS = Map.ofEntries(
        // Armour
        Map.entry(Items.LEATHER_HELMET,       Items.GOLDEN_HELMET),
        Map.entry(Items.LEATHER_CHESTPLATE,   Items.GOLDEN_CHESTPLATE),
        Map.entry(Items.LEATHER_LEGGINGS,     Items.GOLDEN_LEGGINGS),
        Map.entry(Items.LEATHER_BOOTS,        Items.GOLDEN_BOOTS),
        Map.entry(Items.CHAINMAIL_HELMET,     Items.GOLDEN_HELMET),
        Map.entry(Items.CHAINMAIL_CHESTPLATE, Items.GOLDEN_CHESTPLATE),
        Map.entry(Items.CHAINMAIL_LEGGINGS,   Items.GOLDEN_LEGGINGS),
        Map.entry(Items.CHAINMAIL_BOOTS,      Items.GOLDEN_BOOTS),
        Map.entry(Items.IRON_HELMET,          Items.GOLDEN_HELMET),
        Map.entry(Items.IRON_CHESTPLATE,      Items.GOLDEN_CHESTPLATE),
        Map.entry(Items.IRON_LEGGINGS,        Items.GOLDEN_LEGGINGS),
        Map.entry(Items.IRON_BOOTS,           Items.GOLDEN_BOOTS),
        Map.entry(Items.DIAMOND_HELMET,       Items.GOLDEN_HELMET),
        Map.entry(Items.DIAMOND_CHESTPLATE,   Items.GOLDEN_CHESTPLATE),
        Map.entry(Items.DIAMOND_LEGGINGS,     Items.GOLDEN_LEGGINGS),
        Map.entry(Items.DIAMOND_BOOTS,        Items.GOLDEN_BOOTS),
        Map.entry(Items.NETHERITE_HELMET,     Items.GOLDEN_HELMET),
        Map.entry(Items.NETHERITE_CHESTPLATE, Items.GOLDEN_CHESTPLATE),
        Map.entry(Items.NETHERITE_LEGGINGS,   Items.GOLDEN_LEGGINGS),
        Map.entry(Items.NETHERITE_BOOTS,      Items.GOLDEN_BOOTS),
        // Weapons & tools
        Map.entry(Items.WOODEN_SWORD,   Items.GOLDEN_SWORD),
        Map.entry(Items.STONE_SWORD,    Items.GOLDEN_SWORD),
        Map.entry(Items.IRON_SWORD,     Items.GOLDEN_SWORD),
        Map.entry(Items.DIAMOND_SWORD,  Items.GOLDEN_SWORD),
        Map.entry(Items.NETHERITE_SWORD,Items.GOLDEN_SWORD),
        Map.entry(Items.WOODEN_PICKAXE, Items.GOLDEN_PICKAXE),
        Map.entry(Items.STONE_PICKAXE,  Items.GOLDEN_PICKAXE),
        Map.entry(Items.IRON_PICKAXE,   Items.GOLDEN_PICKAXE),
        Map.entry(Items.DIAMOND_PICKAXE,Items.GOLDEN_PICKAXE),
        Map.entry(Items.NETHERITE_PICKAXE, Items.GOLDEN_PICKAXE),
        Map.entry(Items.WOODEN_AXE,     Items.GOLDEN_AXE),
        Map.entry(Items.STONE_AXE,      Items.GOLDEN_AXE),
        Map.entry(Items.IRON_AXE,       Items.GOLDEN_AXE),
        Map.entry(Items.DIAMOND_AXE,    Items.GOLDEN_AXE),
        Map.entry(Items.NETHERITE_AXE,  Items.GOLDEN_AXE),
        Map.entry(Items.WOODEN_SHOVEL,  Items.GOLDEN_SHOVEL),
        Map.entry(Items.STONE_SHOVEL,   Items.GOLDEN_SHOVEL),
        Map.entry(Items.IRON_SHOVEL,    Items.GOLDEN_SHOVEL),
        Map.entry(Items.DIAMOND_SHOVEL, Items.GOLDEN_SHOVEL),
        Map.entry(Items.NETHERITE_SHOVEL, Items.GOLDEN_SHOVEL),
        Map.entry(Items.WOODEN_HOE,     Items.GOLDEN_HOE),
        Map.entry(Items.STONE_HOE,      Items.GOLDEN_HOE),
        Map.entry(Items.IRON_HOE,       Items.GOLDEN_HOE),
        Map.entry(Items.DIAMOND_HOE,    Items.GOLDEN_HOE),
        Map.entry(Items.NETHERITE_HOE,  Items.GOLDEN_HOE),
        // Raw blocks → gold block
        Map.entry(Items.STONE,          Items.GOLD_BLOCK),
        Map.entry(Items.COBBLESTONE,    Items.GOLD_BLOCK),
        Map.entry(Items.DIRT,           Items.GOLD_BLOCK),
        Map.entry(Items.GRASS_BLOCK,    Items.GOLD_BLOCK),
        Map.entry(Items.SAND,           Items.GOLD_BLOCK),
        Map.entry(Items.GRAVEL,         Items.GOLD_BLOCK),
        Map.entry(Items.OAK_LOG,        Items.GOLD_BLOCK),
        Map.entry(Items.SPRUCE_LOG,     Items.GOLD_BLOCK),
        Map.entry(Items.BIRCH_LOG,      Items.GOLD_BLOCK),
        Map.entry(Items.JUNGLE_LOG,     Items.GOLD_BLOCK),
        Map.entry(Items.ACACIA_LOG,     Items.GOLD_BLOCK),
        Map.entry(Items.DARK_OAK_LOG,   Items.GOLD_BLOCK),
        Map.entry(Items.IRON_INGOT,     Items.GOLD_INGOT),
        Map.entry(Items.IRON_BLOCK,     Items.GOLD_BLOCK),
        Map.entry(Items.DIAMOND,        Items.GOLD_INGOT),
        Map.entry(Items.DIAMOND_BLOCK,  Items.GOLD_BLOCK),
        Map.entry(Items.EMERALD,        Items.GOLD_INGOT),
        Map.entry(Items.EMERALD_BLOCK,  Items.GOLD_BLOCK)
    );

    public MidasCard(Item.Properties properties) { super(properties); }

    @Override public Component getCardName()        { return Component.translatable("item.powercaeds.midas_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.midas_card.desc"); }
    @Override public int getCooldownTicks()          { return 0; }   // passive — no activation
    @Override public int getPrimaryColor()           { return 0xFFD700; } // gold
    @Override public int getSecondaryColor()         { return 0xFFF176; } // light yellow
    @Override public int getStructureChestLootWeight() { return 1; }      // very rare

    @Override
    public int[] getSymbolPattern() {
        // Crown-like symbol
        return new int[]{
            0b10101,
            0b10101,
            0b11111,
            0b11111,
            0b01110,
            0b01110
        };
    }

    @Override public boolean canActivate() { return false; }

    // ── Scale: applied when the card is first ticked ─────────────────────────

    /** Applied once when the card slot becomes active (first tick). */
    private void applyScale(Player player) {
        AttributeInstance scaleAttr = player.getAttribute(Attributes.SCALE);
        if (scaleAttr == null) return;
        // Only override if not already at our value to avoid spamming
        if (Math.abs(scaleAttr.getBaseValue() - MIDAS_SCALE) > 0.001) {
            scaleAttr.setBaseValue(MIDAS_SCALE);
        }
    }

    /** Called when the card is unequipped / player dies — resets scale. */
    public static void removeScale(Player player) {
        AttributeInstance scaleAttr = player.getAttribute(Attributes.SCALE);
        if (scaleAttr != null) {
            scaleAttr.setBaseValue(1.0); // vanilla default
        }
    }

    // ── Passive tick ─────────────────────────────────────────────────────────

    @Override
    public void applyEffect(Player player, Level level) {
        // Not an active card — intentionally empty.
    }

    @Override
    public void tickEffect(Player player, Level level, int slotIndex) {
        if (level.isClientSide()) return;

        // Ensure scale is applied every tick (handles respawn, dimension change, etc.)
        applyScale(player);

        // Only run item/block conversion every 20 ticks (1 second) to avoid lag
        if (level.getGameTime() % 20 != 0) return;

        convertInventoryItems(player);
        convertBlocksUnderFeet(player, (ServerLevel) level);
    }

    // ── Item conversion ───────────────────────────────────────────────────────

    private void convertInventoryItems(Player player) {
        var inventory = player.getInventory();
        int size = inventory.getContainerSize();

        for (int i = 0; i < size; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) continue;
            Item item = stack.getItem();

            // Never convert other power cards
            if (item instanceof PowerCard) continue;
            // Never convert golden items (already gold)
            if (isAlreadyGold(item)) continue;

            Item replacement = getReplacementForItem(item);
            if (replacement != null) {
                int count = stack.getCount();
                inventory.setItem(i, new ItemStack(replacement, count));
            }
        }
    }

    private Item getReplacementForItem(Item item) {
        if (item == Items.APPLE) {
            return Items.GOLDEN_APPLE;
        }

        if (item == Items.CARROT) {
            return Items.GOLDEN_CARROT;
        }

        if (item.components().get(net.minecraft.core.component.DataComponents.FOOD) != null) {
            return Items.GOLD_INGOT;
        }

        return GOLD_REPLACEMENTS.get(item);
    }

    private boolean isAlreadyGold(Item item) {
        return item == Items.GOLDEN_SWORD   || item == Items.GOLDEN_PICKAXE
            || item == Items.GOLDEN_AXE     || item == Items.GOLDEN_SHOVEL
            || item == Items.GOLDEN_HOE     || item == Items.GOLDEN_HELMET
            || item == Items.GOLDEN_CHESTPLATE || item == Items.GOLDEN_LEGGINGS
            || item == Items.GOLDEN_BOOTS   || item == Items.GOLDEN_APPLE
            || item == Items.GOLDEN_CARROT  || item == Items.GOLD_INGOT
            || item == Items.GOLD_BLOCK     || item == Items.GOLD_NUGGET
            || item == Items.RAW_GOLD       || item == Items.RAW_GOLD_BLOCK;
    }

    // ── Block conversion ──────────────────────────────────────────────────────

    private void convertBlocksUnderFeet(Player player, ServerLevel level) {
        BlockPos feet = BlockPos.containing(player.getX(), player.getY() - 0.1, player.getZ());

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = feet.offset(dx, 0, dz);
                BlockState state = level.getBlockState(pos);

                // Never convert water/lava/air/bedrock
                if (state.isAir()) continue;
                if (state.is(Blocks.WATER) || state.is(Blocks.LAVA)) continue;
                if (state.is(Blocks.BEDROCK)) continue;
                // Don't touch already-gold blocks
                if (state.is(Blocks.GOLD_BLOCK) || state.is(Blocks.RAW_GOLD_BLOCK)) continue;
                level.setBlock(pos, Blocks.GOLD_BLOCK.defaultBlockState(), 3);
            }
        }
    }

    // ── Death hook: reset scale when the card drops ───────────────────────────

    @Override
    public boolean tryPreventDeath(Player player, Level level, DamageSource source, float amount, int slotIndex) {
        // We don't prevent death, but we reset scale on death
        removeScale(player);
        return false;
    }
}
