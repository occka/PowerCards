package cards.modid.client.render;

import cards.modid.card.PowerCard;
import cards.modid.client.ClientCardState;
import cards.modid.component.CardSlotsComponent;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudRenderPhase;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import cards.modid.PowerCaeds;

/**
 * Renders 3 power card slots on the right side of the HUD.
 * Colors and pixel-art symbol come from each PowerCard's methods.
 */
public class CardHudRenderer {

    private static final int CARD_W  = 20;
    private static final int CARD_H  = 28;
    private static final int SCALE   = 2;
    private static final int GAP     = 4;
    private static final int MARGIN_RIGHT  = 8;
    private static final int MARGIN_BOTTOM = 60;
    private static final int BAR_H   = 3;

    public static void register() {
        HudElementRegistry.attachToElement(
                net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.HOTBAR,
                HudRenderPhase.AFTER,
                Identifier.fromNamespaceAndPath(PowerCaeds.MOD_ID, "card_hud"),
                CardHudRenderer::render
        );
    }

    private static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int totalH = CardSlotsComponent.SLOT_COUNT * (CARD_H * SCALE + BAR_H + GAP);
        int startY = screenH - MARGIN_BOTTOM - totalH;

        for (int i = 0; i < CardSlotsComponent.SLOT_COUNT; i++) {
            int x = screenW - MARGIN_RIGHT - CARD_W * SCALE;
            int y = startY + i * (CARD_H * SCALE + BAR_H + GAP);
            if (ClientCardState.hasCard(i)) {
                drawCard(graphics, x, y, i, ClientCardState.getCardItem(i));
            } else {
                drawEmptySlot(graphics, x, y);
            }
        }
    }

    private static void drawCard(GuiGraphics g, int x, int y, int slot, PowerCard card) {
        int primary   = card.getPrimaryColor();
        int secondary = card.getSecondaryColor();
        int w = CARD_W * SCALE;
        int h = CARD_H * SCALE;
        int s = SCALE;

        g.fill(x, y, x + w, y + h, 0xFF000000 | primary);
        g.fill(x,         y,             x + w, y + s,     0xFF000000 | secondary);
        g.fill(x,         y + h - s,     x + w, y + h,     0xFF000000 | secondary);
        g.fill(x,         y,             x + s, y + h,     0xFF000000 | secondary);
        g.fill(x + w - s, y,             x + w, y + h,     0xFF000000 | secondary);

        drawCardSymbol(g, x, y, secondary, card);

        float cdProgress = ClientCardState.getCooldownProgress(slot);
        if (cdProgress > 0f) {
            int overlayH = (int) (h * cdProgress);
            g.fill(x + s, y + h - overlayH, x + w - s, y + h - s, 0xAA000000);
        }

        drawCooldownBar(g, x, y + h + 1, w, slot, card);
    }

    private static void drawCardSymbol(GuiGraphics g, int cardX, int cardY, int secondary, PowerCard card) {
        int ox = cardX + 3 * SCALE;
        int oy = cardY + 3 * SCALE;
        int color = 0xFF000000 | secondary;
        int[] pattern = card.getSymbolPattern();
        for (int row = 0; row < pattern.length && row < 10; row++) {
            for (int col = 0; col < 5; col++) {
                if ((pattern[row] & (1 << (4 - col))) != 0) {
                    int px = ox + col * SCALE;
                    int py = oy + row * SCALE;
                    g.fill(px, py, px + SCALE, py + SCALE, color);
                }
            }
        }
    }

    private static void drawCooldownBar(GuiGraphics g, int x, int y, int w, int slot, PowerCard card) {
        float progress = ClientCardState.getCooldownProgress(slot);
        g.fill(x, y, x + w, y + BAR_H, 0xFF333333);
        int filled = (int) (w * (1f - progress));
        int barColor = progress > 0f ? 0xFFCC2222 : 0xFF22CC44;
        if (filled > 0) g.fill(x, y, x + filled, y + BAR_H, 0xFF000000 | barColor);
    }

    private static void drawEmptySlot(GuiGraphics g, int x, int y) {
        int w = CARD_W * SCALE, h = CARD_H * SCALE, s = SCALE;
        g.fill(x,         y,         x + w, y + s,     0x44FFFFFF);
        g.fill(x,         y + h - s, x + w, y + h,     0x44FFFFFF);
        g.fill(x,         y,         x + s, y + h,     0x44FFFFFF);
        g.fill(x + w - s, y,         x + w, y + h,     0x44FFFFFF);
    }
}
