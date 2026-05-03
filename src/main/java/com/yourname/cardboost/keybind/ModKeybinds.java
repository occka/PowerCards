package com.yourname.cardboost.keybind;

import com.yourname.cardboost.network.ModNetwork;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {
    public static KeyMapping[] SLOT_KEYS = new KeyMapping[3];

    public static void register() {
        // В 26.1 категория теперь KeyMapping.Category, а не строка
        SLOT_KEYS[0] = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.cardboost.slot1", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.cardboost"));
        SLOT_KEYS[1] = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.cardboost.slot2", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, "key.categories.cardboost"));
        SLOT_KEYS[2] = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.cardboost.slot3", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, "key.categories.cardboost"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (int i = 0; i < SLOT_KEYS.length; i++) {
                if (SLOT_KEYS[i].consumeClick()) {
                    ClientPlayNetworking.send(ModNetwork.CARD_ACTIVATE, ModNetwork.createActivatePacket(i));
                }
            }
        });
    }
}
