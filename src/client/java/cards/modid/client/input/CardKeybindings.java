package cards.modid.client.input;

import cards.modid.PowerCaeds;
import cards.modid.client.screen.CardEquipScreen;
import cards.modid.network.ActivateCardPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/**
 * Registers keybindings for card slot activation and the card equip screen.
 *
 * Defaults:
 *   Slot 0 → Z
 *   Slot 1 → X
 *   Slot 2 → C
 *   Open card screen → G
 *
 * All rebindable in Options → Controls → Power Cards.
 */
public class CardKeybindings {

    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(PowerCaeds.MOD_ID, "power_cards"));

    public static final KeyMapping[] SLOT_KEYS = new KeyMapping[]{
            new KeyMapping("key.powercaeds.slot_0", InputConstants.Type.KEYSYM,
                    InputConstants.KEY_Z, CATEGORY),
            new KeyMapping("key.powercaeds.slot_1", InputConstants.Type.KEYSYM,
                    InputConstants.KEY_X, CATEGORY),
            new KeyMapping("key.powercaeds.slot_2", InputConstants.Type.KEYSYM,
                    InputConstants.KEY_C, CATEGORY),
    };

    public static final KeyMapping OPEN_SCREEN_KEY =
            new KeyMapping("key.powercaeds.open_screen", InputConstants.Type.KEYSYM,
                    InputConstants.KEY_G, CATEGORY);

    public static void register() {
        for (KeyMapping key : SLOT_KEYS) {
            KeyMappingHelper.registerKeyMapping(key);
        }
        KeyMappingHelper.registerKeyMapping(OPEN_SCREEN_KEY);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Activate card slots
            for (int i = 0; i < SLOT_KEYS.length; i++) {
                if (SLOT_KEYS[i].consumeClick()) {
                    ClientPlayNetworking.send(new ActivateCardPacket(i));
                }
            }

            // Open equip screen
            if (OPEN_SCREEN_KEY.consumeClick()) {
                Minecraft.getInstance().setScreen(new CardEquipScreen());
            }
        });
    }
}
