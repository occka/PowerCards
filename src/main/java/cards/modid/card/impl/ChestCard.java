package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class ChestCard extends PowerCard {
    public ChestCard(Item.Properties properties) { super(properties); }

    @Override public Component getCardName() { return Component.translatable("item.powercaeds.chest_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.chest_card.desc"); }
    @Override public int getCooldownTicks() { return 5 * 20; }
    @Override public int getPrimaryColor() { return 0x111018; }
    @Override public int getSecondaryColor() { return 0x8B35D8; }
    @Override public int getStructureChestLootWeight() { return 5; }

    @Override
    public int[] getSymbolPattern() {
        return new int[]{ 0b11111, 0b10001, 0b10101, 0b10001, 0b11111, 0b00000 };
    }

    @Override
    public void applyEffect(Player player, Level level) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        serverPlayer.openMenu(new SimpleMenuProvider(
                (id, inventory, p) -> ChestMenu.threeRows(id, inventory, player.getEnderChestInventory()),
                Component.translatable("container.enderchest")
        ));
    }
}
