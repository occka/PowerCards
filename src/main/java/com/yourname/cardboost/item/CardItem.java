package com.yourname.cardboost.item;

import com.yourname.cardboost.card.Card;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class CardItem extends Item {
    private final Card card;

    public CardItem(Card card) {
        super(new Item.Properties().stacksTo(1));
        this.card = card;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.literal(card.getDescription()).withStyle(s -> s.withColor(0xAAAAAA)));
        lines.add(Component.literal("КД: " + (card.getCooldownTicks() / 20) + " сек.").withStyle(s -> s.withColor(0xFFD700)));
        lines.add(Component.literal("Привяжи клавишу в Настройки -> Управление").withStyle(s -> s.withColor(0x666666)));
    }

    public Card getCard() { return card; }
}
