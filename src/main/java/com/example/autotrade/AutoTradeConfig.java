package com.example.autotrade;

/**
 * Simple in-memory config. Swap for a JSON file (e.g. via Gson) if you want it to persist
 * between game sessions.
 */
public class AutoTradeConfig {

    // Index of the trade slot (0-based, top to bottom in the trade GUI) to keep buying.
    // Change this to whichever trade you want automated (e.g. emerald -> item trades).
    public static int tradeSlotIndex = 0;

    // Delay (in client ticks) between each simulated click, to avoid inhumanly fast clicking.
    // 20 ticks = 1 second. Lower this at your own risk (more detectable).
    public static int ticksBetweenClicks = 4;
}

