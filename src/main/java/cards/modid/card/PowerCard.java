package cards.modid.card;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public abstract class PowerCard extends Item {

    public PowerCard(Properties properties) {
        super(properties.stacksTo(1));
    }

    public abstract Component getCardName();
    public abstract Component getCardDescription();
    public abstract int getCooldownTicks();
    public abstract int getPrimaryColor();
    public abstract int getSecondaryColor();

    /**
     * Relative chance for this card to appear in structure/chest loot.
     * Higher values make the card more common; return 0 or less to disable chest loot for a card.
     */
    public int getStructureChestLootWeight() { return 5; }

    public int[] getSymbolPattern() {
        return new int[]{ 0b00110, 0b00100, 0b01110, 0b00100, 0b01100, 0b01000 };
    }

    public boolean canActivate() { return true; }

    public abstract void applyEffect(Player player, Level level);
    public void tickEffect(Player player, Level level, int slotIndex) {}

    /**
     * Called before a player carrying this card would die.
     * Return true to cancel death and put the card on cooldown.
     */
    public boolean tryPreventDeath(Player player, Level level, DamageSource source, float amount, int slotIndex) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                TooltipDisplay display, Consumer<Component> sink, TooltipFlag flag) {
        sink.accept(getCardDescription());
        sink.accept(Component.translatable("item.powercaeds.card.cooldown", getCooldownTicks() / 20));
        super.appendHoverText(stack, context, display, sink, flag);
    }

    @Override
    public Component getName(ItemStack stack) {
        return getCardName();
    }
}
