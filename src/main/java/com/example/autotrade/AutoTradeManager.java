package com.example.autotrade;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.village.TradeOffer;

import java.util.List;

/**
 * On-demand auto trade:
 *   - Press the bound key (default X) while looking at a villager within reach -> opens the
 *     trade GUI and auto-clicks the configured trade slot until it can't be used anymore
 *     (out of stock / player can't afford it), then closes the GUI automatically.
 *   - No automatic movement: you walk up and aim at the villager yourself, same as trading
 *     normally, the mod just automates the "open + repeat click + close" part.
 */
public class AutoTradeManager {

    private static boolean trading = false;
    private static int tickCounter = 0;

    /** Called once when the toggle key is pressed. */
    public static void activate() {
        if (trading) {
            // Already mid-trade; ignore extra presses.
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (client.world == null || player == null || client.interactionManager == null) return;

        VillagerEntity villager = getTargetedVillager(client);
        if (villager == null) {
            player.sendMessage(net.minecraft.text.Text.literal(
                    "[AutoTrade] Không có villager nào trong tầm ngắm."), true);
            return;
        }

        client.interactionManager.interactEntity(player, villager, Hand.MAIN_HAND);
        trading = true;
        tickCounter = 0;
    }

    /** Call once per client tick regardless of state. */
    public static void tick() {
        if (!trading) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            trading = false;
            return;
        }

        if (!(client.currentScreen instanceof MerchantScreen merchantScreen)) {
            // GUI closed some other way (e.g. player pressed Escape) -> stop.
            trading = false;
            return;
        }

        handleTradeScreen(client, merchantScreen);
    }

    /** Looks for the villager entity currently under the player's crosshair, within reach. */
    private static VillagerEntity getTargetedVillager(MinecraftClient client) {
        HitResult hit = client.crosshairTarget;
        if (hit instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            if (entity instanceof VillagerEntity villager) {
                return villager;
            }
        }
        return null;
    }

    private static void handleTradeScreen(MinecraftClient client, MerchantScreen screen) {
        tickCounter++;
        if (tickCounter < AutoTradeConfig.ticksBetweenClicks) {
            return;
        }
        tickCounter = 0;

        MerchantScreenHandler handler = screen.getScreenHandler();
        List<TradeOffer> offers = handler.getRecipes();

        int slot = AutoTradeConfig.tradeSlotIndex;
        if (slot < 0 || slot >= offers.size()) {
            closeAndStop(client);
            return;
        }

        TradeOffer offer = offers.get(slot);
        if (offer.isDisabled()) {
            // Out of stock / player can't afford it -> done, close automatically.
            closeAndStop(client);
            return;
        }

        // Select the trade slot, then simulate clicking the merchant's output slot to execute it.
        handler.setRecipeIndex(slot);
        // Slot index 2 = the merchant's result/output slot (0 = input 1, 1 = input 2, 2 = output).
        client.interactionManager.clickSlot(handler.syncId, 2, 0, SlotActionType.PICKUP, client.player);
        // Put the traded item back into the player's inventory instead of leaving it on cursor.
        client.interactionManager.clickSlot(handler.syncId, 2, 0, SlotActionType.PICKUP, client.player);
    }

    private static void closeAndStop(MinecraftClient client) {
        client.player.closeHandledScreen();
        trading = false;
    }
}
