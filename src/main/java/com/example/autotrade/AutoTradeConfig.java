package com.example.autotrade;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vòng lặp: IDLE (tìm villager gần nhất) -> MOVING (đi tới) -> WAITING_SCREEN (mở trade)
 * -> TRADING (bấm X / thực hiện trade) -> đóng GUI -> quay lại IDLE.
 *
 * LƯU Ý: phần di chuyển ở đây là "steer" đơn giản (xoay mặt + đẩy tới bằng Entity#move),
 * KHÔNG phải pathfinding thật sự. Player có thể bị kẹt ở vật cản, hàng rào, nước, hố sâu...
 * Nếu cần chính xác hơn, hãy tích hợp thư viện pathfinding (ví dụ Baritone API).
 */
public class AutoTradeManager {

	private enum State { IDLE, MOVING, WAITING_SCREEN, TRADING }

	private boolean enabled = false;
	private State state = State.IDLE;
	private Villager target;
	private int waitTicks = 0;
	private int tickCounter = 0;

	private static final double SEARCH_RADIUS = 24.0;
	private static final double REACH_DISTANCE = 3.0;
	private static final double MOVE_SPEED = 0.22;
	private static final int SCREEN_WAIT_TIMEOUT = 20; // ~1 giây
	private static final int COOLDOWN_TICKS = 100;      // ~5 giây trước khi quay lại trade cùng 1 villager

	private final Map<Integer, Integer> cooldown = new HashMap<>();

	public void toggle(Minecraft client) {
		enabled = !enabled;
		state = State.IDLE;
		target = null;
		if (client.player != null) {
			client.player.displayClientMessage(
					Component.literal("[AutoTrade] " + (enabled ? "BAT" : "TAT")), true);
		}
	}

	public void tick(Minecraft client) {
		tickCounter++;
		if (!enabled || client.player == null || client.level == null) {
			return;
		}

		LocalPlayer player = client.player;
		switch (state) {
			case IDLE -> handleIdle(client, player);
			case MOVING -> handleMoving(client, player);
			case WAITING_SCREEN -> handleWaitingScreen(client);
			case TRADING -> { /* thực hiện ngay trong tryTradeCurrentScreen() */ }
		}
	}

	private void handleIdle(Minecraft client, LocalPlayer player) {
		if (client.screen instanceof MerchantScreen) {
			state = State.TRADING;
			tryTradeCurrentScreen(client);
			return;
		}
		Villager found = findNearestVillager(client, player);
		if (found != null) {
			target = found;
			state = State.MOVING;
		}
	}

	private void handleMoving(Minecraft client, LocalPlayer player) {
		if (target == null || !target.isAlive() || player.distanceTo(target) > SEARCH_RADIUS * 1.5) {
			target = null;
			state = State.IDLE;
			return;
		}

		if (player.distanceTo(target) <= REACH_DISTANCE) {
			client.gameMode.interact(player, target, InteractionHand.MAIN_HAND);
			state = State.WAITING_SCREEN;
			waitTicks = 0;
			return;
		}

		steerTowards(player, target.position());
	}

	private void handleWaitingScreen(Minecraft client) {
		waitTicks++;
		if (client.screen instanceof MerchantScreen) {
			state = State.TRADING;
			tryTradeCurrentScreen(client);
			waitTicks = 0;
			return;
		}
		if (waitTicks > SCREEN_WAIT_TIMEOUT) {
			if (target != null) cooldown.put(target.getId(), tickCounter + COOLDOWN_TICKS);
			target = null;
			state = State.IDLE;
			waitTicks = 0;
		}
	}

	/** Gọi khi bấm phím X thủ công, hoặc tự động khi đang ở state TRADING. */
	public void tryTradeCurrentScreen(Minecraft client) {
		if (!(client.screen instanceof MerchantScreen merchantScreen)) return;
		MerchantMenu menu = merchantScreen.getMenu();

		if (menu.getOffers().isEmpty()) {
			if (enabled) closeAndFinish(client);
			return;
		}

		int offerIndex = 0;
		for (int i = 0; i < menu.getOffers().size(); i++) {
			if (!menu.getOffers().get(i).isOutOfStock()) {
				offerIndex = i;
				break;
			}
		}
		menu.setSelectionHint(offerIndex);

		// Slot 2 = slot kết quả của MerchantMenu. Shift-click (QUICK_MOVE) thực hiện trade
		// và đẩy thẳng vật phẩm vào túi đồ, giống hệt thao tác thủ công.
		client.gameMode.handleInventoryMouseClick(
				menu.containerId, 2, 0, ClickType.QUICK_MOVE, client.player);

		if (enabled && state == State.TRADING) {
			closeAndFinish(client);
		}
	}

	private void closeAndFinish(Minecraft client) {
		if (target != null) {
			cooldown.put(target.getId(), tickCounter + COOLDOWN_TICKS);
		}
		if (client.player != null) {
			client.player.closeContainer();
		}
		client.setScreen(null);
		target = null;
		state = State.IDLE;
	}

	private Villager findNearestVillager(Minecraft client, LocalPlayer player) {
		AABB box = player.getBoundingBox().inflate(SEARCH_RADIUS);
		List<Villager> nearby = client.level.getEntitiesOfClass(Villager.class, box,
				v -> v.isAlive() && isNotOnCooldown(v));

		Villager best = null;
		double bestDist = Double.MAX_VALUE;
		for (Villager v : nearby) {
			double d = player.distanceToSqr(v);
			if (d < bestDist) {
				bestDist = d;
				best = v;
			}
		}
		return best;
	}

	private boolean isNotOnCooldown(Entity e) {
		Integer until = cooldown.get(e.getId());
		return until == null || until <= tickCounter;
	}

	private void steerTowards(LocalPlayer player, Vec3 targetPos) {
		Vec3 from = player.position();
		Vec3 diff = new Vec3(targetPos.x - from.x, 0, targetPos.z - from.z);
		if (diff.lengthSqr() < 1.0e-4) return;
		Vec3 dir = diff.normalize();

		float yaw = (float) (Math.atan2(-dir.x, dir.z) * (180.0 / Math.PI));
		player.setYRot(yaw);
		player.setYHeadRot(yaw);

		Vec3 step = dir.scale(MOVE_SPEED);
		player.move(MoverType.SELF, new Vec3(step.x, 0, step.z));

		if (player.horizontalCollision && player.onGround()) {
			player.jumpFromGround();
		}
	}
}
