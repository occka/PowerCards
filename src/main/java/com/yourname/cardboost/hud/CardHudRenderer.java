package com.yourname.cardboost.hud;

import com.yourname.cardboost.card.Card;
import com.yourname.cardboost.card.CardRegistry;
import com.yourname.cardboost.keybind.ModKeybinds;
import com.yourname.cardboost.player.CardSlotManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class CardHudRenderer {
    private static final int CARD_W = 24;
    private static final int CARD_H = 36;
    private static final int GAP = 6;

    public static void register() {
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> render(graphics));
    }

    private static void render(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        CardSlotManager manager = CardSlotManager.get(mc.player);
        Font font = mc.font;
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        int startX = screenW - CARD_W - 10;
        int startY = screenH / 2 - (CardSlotManager.SLOT_COUNT * (CARD_H + GAP)) / 2;

        for (int i = 0; i < CardSlotManager.SLOT_COUNT; i++) {
            int x = startX;
            int y = startY + i * (CARD_H + GAP);
            String cardId = manager.getSlot(i);
            int cd = manager.getCooldown(i);

            if (cardId != null) {
                Card card = CardRegistry.getById(cardId);
                if (card != null) drawCard(graphics, font, x, y, card, cd);
            } else {
                drawEmptySlot(graphics, x, y);
            }

            String keyHint = "[" + ModKeybinds.SLOT_KEYS[i].getTranslatedKeyMessage().getString() + "]";
            graphics.drawString(font, keyHint, x + 1, y + CARD_H + 2, 0x888888, false);
        }
    }

    private static void drawCard(GuiGraphics g, Font font, int x, int y, Card card, int cd) {
        int bg = 0xFF000000 | card.getPrimaryColor();
        int border = 0xFF000000 | card.getSecondaryColor();

        g.fill(x + 1, y + 1, x + CARD_W - 1, y + CARD_H - 1, bg);
        g.fill(x, y, x + CARD_W, y + 2, border);
        g.fill(x, y + CARD_H - 2, x + CARD_W, y + CARD_H, border);
        g.fill(x, y, x + 2, y + CARD_H, border);
        g.fill(x + CARD_W - 2, y, x + CARD_W, y + CARD_H, border);

        String sym = card.getSymbol();
        g.drawString(font, sym, x + CARD_W / 2 - font.width(sym) / 2, y + CARD_H / 2 - 4, 0xFFFFFF, false);

        if (cd > 0) {
            g.fill(x + 2, y + 2, x + CARD_W - 2, y + CARD_H - 2, 0xBB000000);
            String cdStr = String.valueOf((cd + 19) / 20);
            g.drawString(font, cdStr, x + CARD_W / 2 - font.width(cdStr) / 2, y + CARD_H / 2 - 4, 0xFF4444, false);
        }
    }

    private static void drawEmptySlot(GuiGraphics g, int x, int y) {
        g.fill(x + 1, y + 1, x + CARD_W - 1, y + CARD_H - 1, 0x33FFFFFF);
        g.fill(x, y, x + CARD_W, y + 2, 0x55888888);
        g.fill(x, y + CARD_H - 2, x + CARD_W, y + CARD_H, 0x55888888);
        g.fill(x, y, x + 2, y + CARD_H, 0x55888888);
        g.fill(x + CARD_W - 2, y, x + CARD_W, y + CARD_H, 0x55888888);
    }
}
