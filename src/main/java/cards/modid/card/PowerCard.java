package cards.modid.card;

import net.minecraft.network.chat.Component;
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

    public int[] getSymbolPattern() {
        return new int[]{ 0b00110, 0b00100, 0b01110, 0b00100, 0b01100, 0b01000 };
    }

    public abstract void applyEffect(Player player, Level level);
    public void tickEffect(Player player, Level level, int slotIndex) {}

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
