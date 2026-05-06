package cards.modid.client.screen;

import cards.modid.card.PowerCard;
import cards.modid.client.ClientCardState;
import cards.modid.component.CardSlotsComponent;
import cards.modid.network.EquipCardPacket;
import cards.modid.network.UnequipCardPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Card equip screen — opened with the configured keybind (default: G).
 *
 * Layout:
 *   ┌──────────────────────────────────────┐
 *   │  [ Card 1 ]  [ Card 2 ]  [ Card 3 ]  │  ← 3 card slots (top)
 *   │                                      │
 *   │  [ inv row 1 ] [ inv row 2 ] ...     │  ← player inventory (bottom)
 *   └──────────────────────────────────────┘
 *
 * Click a card slot → unequip (returns to inventory)
 * Click an inventory item that is a PowerCard → equip to first empty slot
 *   (or shift-click to pick a specific slot)
 */
public class CardEquipScreen extends Screen {

    // Slot display size
    private static final int SLOT_SIZE  = 32;
    private static final int SLOT_GAP   = 8;
    private static final int INV_SLOT   = 18; // vanilla slot size

    private int centerX, centerY;

    // Slot bounding boxes [slotIndex][0=x, 1=y]
    private final int[][] slotPos = new int[CardSlotsComponent.SLOT_COUNT][2];

    public CardEquipScreen() {
        super(Component.translatable("screen.powercaeds.card_equip"));
    }

    @Override
    protected void init() {
        centerX = width / 2;
        centerY = height / 2;

        // Position the 3 card slots centered at top
        int totalW = CardSlotsComponent.SLOT_COUNT * SLOT_SIZE
                   + (CardSlotsComponent.SLOT_COUNT - 1) * SLOT_GAP;
        int startX = centerX - totalW / 2;
        int startY = centerY - 80;

        for (int i = 0; i < CardSlotsComponent.SLOT_COUNT; i++) {
            slotPos[i][0] = startX + i * (SLOT_SIZE + SLOT_GAP);
            slotPos[i][1] = startY;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        // Background is extracted by Screen.extractRenderStateWithTooltipAndSubtitles before this method.

        // Title
        g.centeredText(font, title, centerX, centerY - 100, 0xFFFFFF);

        // Draw card slots
        for (int i = 0; i < CardSlotsComponent.SLOT_COUNT; i++) {
            renderCardSlot(g, i, mouseX, mouseY);
        }

        // Draw player inventory (9 hotbar + 27 main = 36 slots)
        renderInventory(g, mouseX, mouseY);

        // Tooltip
        for (int i = 0; i < CardSlotsComponent.SLOT_COUNT; i++) {
            if (isHoveringSlot(i, mouseX, mouseY) && ClientCardState.hasCard(i)) {
                g.setTooltipForNextFrame(font, ClientCardState.getCard(i), mouseX, mouseY);
            }
        }

        super.extractRenderState(g, mouseX, mouseY, delta);
    }

    private void renderCardSlot(GuiGraphicsExtractor g, int slot, int mouseX, int mouseY) {
        int x = slotPos[slot][0];
        int y = slotPos[slot][1];
        boolean hovering = isHoveringSlot(slot, mouseX, mouseY);

        // Slot background
        int bgColor = hovering ? 0xFF555555 : 0xFF333333;
        g.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, bgColor);

        // Border
        int borderColor = hovering ? 0xFFFFFFFF : 0xFF888888;
        g.fill(x, y,                   x + SLOT_SIZE, y + 1,          borderColor);
        g.fill(x, y + SLOT_SIZE - 1,   x + SLOT_SIZE, y + SLOT_SIZE,  borderColor);
        g.fill(x, y,                   x + 1,         y + SLOT_SIZE,  borderColor);
        g.fill(x + SLOT_SIZE - 1, y,   x + SLOT_SIZE, y + SLOT_SIZE,  borderColor);

        if (ClientCardState.hasCard(slot)) {
            // Draw the card item
            ItemStack stack = ClientCardState.getCard(slot);
            g.item(stack, x + 8, y + 8);

            // Cooldown overlay
            float cd = ClientCardState.getCooldownProgress(slot);
            if (cd > 0) {
                int overlayH = (int) (SLOT_SIZE * cd);
                g.fill(x + 1, y + SLOT_SIZE - overlayH - 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1,
                        0x99000000);
            }

            // Slot number label
            g.text(font, String.valueOf(slot + 1), x + 2, y + 2, 0xFFFFAA00);
        } else {
            // Empty slot label
            g.centeredText(font, String.valueOf(slot + 1),
                    x + SLOT_SIZE / 2, y + SLOT_SIZE / 2 - 4, 0xFF666666);
        }
    }

    private void renderInventory(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        assert minecraft != null;
        var inv = minecraft.player.getInventory();

        int invStartY = centerY - 20;
        int hotbarY   = centerY + 52;

        // Main inventory (rows 1-3, indices 9-35)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int invIndex = 9 + row * 9 + col;
                int x = centerX - 81 + col * INV_SLOT;
                int y = invStartY + row * INV_SLOT;
                renderInvSlot(g, inv.getItem(invIndex), x, y, invIndex, mouseX, mouseY);
            }
        }

        // Hotbar (indices 0-8)
        for (int col = 0; col < 9; col++) {
            int x = centerX - 81 + col * INV_SLOT;
            renderInvSlot(g, inv.getItem(col), x, hotbarY, col, mouseX, mouseY);
        }
    }

    private void renderInvSlot(GuiGraphicsExtractor g, ItemStack stack, int x, int y,
                                int invIndex, int mouseX, int mouseY) {
        boolean isPowerCard = !stack.isEmpty() && stack.getItem() instanceof PowerCard;
        boolean hovering = mouseX >= x && mouseX < x + INV_SLOT
                        && mouseY >= y && mouseY < y + INV_SLOT;

        // Highlight power cards
        int bg = isPowerCard ? 0xFF1A3A1A : 0xFF2A2A2A;
        if (hovering) bg = isPowerCard ? 0xFF2A5A2A : 0xFF3A3A3A;
        g.fill(x, y, x + INV_SLOT, y + INV_SLOT, bg);
        g.fill(x, y, x + INV_SLOT, y + 1, 0xFF555555);
        g.fill(x, y, x + 1, y + INV_SLOT, 0xFF555555);

        if (!stack.isEmpty()) {
            g.item(stack, x + 1, y + 1);
            g.itemDecorations(font, stack, x + 1, y + 1);
        }

        if (hovering && !stack.isEmpty()) {
            g.setTooltipForNextFrame(font, stack, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int mx = (int) event.x(), my = (int) event.y();

        // Click on a card slot
        for (int i = 0; i < CardSlotsComponent.SLOT_COUNT; i++) {
            if (isHoveringSlot(i, mx, my)) {
                if (ClientCardState.hasCard(i)) {
                    // Right-click or left-click → unequip
                    ClientPlayNetworking.send(new UnequipCardPacket(i));
                }
                return true;
            }
        }

        // Click on inventory slot — equip if it's a power card
        assert minecraft != null;
        var inv = minecraft.player.getInventory();
        int invIndex = getHoveredInvIndex(mx, my);
        if (invIndex >= 0) {
            ItemStack stack = inv.getItem(invIndex);
            if (!stack.isEmpty() && stack.getItem() instanceof PowerCard) {
                // Find first empty card slot
                int emptySlot = -1;
                for (int s = 0; s < CardSlotsComponent.SLOT_COUNT; s++) {
                    if (!ClientCardState.hasCard(s)) { emptySlot = s; break; }
                }
                if (emptySlot >= 0) {
                    ClientPlayNetworking.send(new EquipCardPacket(emptySlot, invIndex));
                }
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private boolean isHoveringSlot(int slot, int mouseX, int mouseY) {
        int x = slotPos[slot][0], y = slotPos[slot][1];
        return mouseX >= x && mouseX < x + SLOT_SIZE
            && mouseY >= y && mouseY < y + SLOT_SIZE;
    }

    private int getHoveredInvIndex(int mouseX, int mouseY) {
        int invStartY = centerY - 20;
        int hotbarY   = centerY + 52;

        // Main inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int x = centerX - 81 + col * INV_SLOT;
                int y = invStartY + row * INV_SLOT;
                if (mouseX >= x && mouseX < x + INV_SLOT && mouseY >= y && mouseY < y + INV_SLOT)
                    return 9 + row * 9 + col;
            }
        }
        // Hotbar
        for (int col = 0; col < 9; col++) {
            int x = centerX - 81 + col * INV_SLOT;
            if (mouseX >= x && mouseX < x + INV_SLOT && mouseY >= hotbarY && mouseY < hotbarY + INV_SLOT)
                return col;
        }
        return -1;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
