package com.example.autotrade;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AutoTradeClient implements ClientModInitializer {

    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autotrade.activate",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "category.autotrade"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                AutoTradeManager.activate();
            }
            AutoTradeManager.tick();
        });
    }
}
