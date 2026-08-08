package com.example.autotrade;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class AutoTradeClient implements ClientModInitializer {

	// Phím X: bấm 1 lần để trade ngay offer hiện đang mở (nếu có màn hình Merchant)
	public static KeyMapping tradeOnceKey;

	// Phím mặc định: RIGHT_BRACKET ( ] ) để BẬT/TẮT vòng lặp tự động tìm dân làng + trade
	public static KeyMapping toggleAutoKey;

	private static final AutoTradeManager MANAGER = new AutoTradeManager();

	@Override
	public void onInitializeClient() {
		tradeOnceKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.autotrade.trade_once",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_X,
				"category.autotrade"
		));

		toggleAutoKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.autotrade.toggle_auto",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_BRACKET,
				"category.autotrade"
		));

		ClientTickEvents.END_CLIENT_TICK.register(this::onTick);

		AutoTradeMod.LOGGER.info("[AutoTrade] Client da san sang. Phim X = trade 1 lan, phim ] = bat/tat auto.");
	}

	private void onTick(Minecraft client) {
		if (client.player == null || client.level == null) {
			return;
		}

		// Bấm X thủ công -> trade ngay lập tức nếu đang mở màn hình Merchant
		while (tradeOnceKey.consumeClick()) {
			MANAGER.tryTradeCurrentScreen(client);
		}

		// Bật/tắt chế độ auto (tự tìm dân làng -> đi tới -> mở trade -> bấm X -> đóng -> lặp)
		while (toggleAutoKey.consumeClick()) {
			MANAGER.toggle(client);
		}

		MANAGER.tick(client);
	}
}
