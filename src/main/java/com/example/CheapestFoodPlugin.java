package com.example;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.*;

@PluginDescriptor(
        name = "Cheapest Food",
        description = "Shows cheapest food per heal value when GE is open",
        tags = {"food", "ge", "prices", "healing"}
)
public class CheapestFoodPlugin extends Plugin
{
    @Inject private Client client;
    @Inject private ItemManager itemManager;
    @Inject private CheapestFoodOverlay overlay;
    @Inject private OverlayManager overlayManager;
    @Inject CheapestFoodConfig config;

    private final Map<Integer, Integer> FOOD_HEALS = Map.of(
            ItemID.MANTA_RAY, 22,
            ItemID.DARK_CRAB, 22,
            ItemID.SEA_TURTLE, 21,
            ItemID.SHARK, 20,
            ItemID.MONKFISH, 16
    );

    private List<FoodPriceInfo> cheapestFoods = new ArrayList<>();

    @Override
    protected void startUp()
    {
        cheapestFoods.clear();
        overlayManager.add(overlay);
        System.out.println("[CheapestFoodPlugin] Plugin started");
    }

    @Override
    protected void shutDown()
    {
        cheapestFoods.clear();
        overlayManager.remove(overlay);
        System.out.println("[CheapestFoodPlugin] Plugin stopped");
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event)
    {
        if (event.getGroupId() == WidgetID.GRAND_EXCHANGE_GROUP_ID)
        {
            System.out.println("[CheapestFoodPlugin] GE opened, updating food list");
            updateCheapestFoods();
        }
    }

    private void updateCheapestFoods()
    {
        Map<Integer, FoodPriceInfo> cheapest = new HashMap<>();

        for (var entry : FOOD_HEALS.entrySet())
        {
            int itemId = entry.getKey();
            int heal = entry.getValue();

            if (heal < config.minHeal())
            {
                System.out.println("[CheapestFoodPlugin] Skipping " + itemId + " (heal " + heal + " < minHeal " + config.minHeal() + ")");
                continue;
            }

            int price = itemManager.getItemPrice(itemId);
            double costPer10 = price * 10.0 / heal;

            FoodPriceInfo current = cheapest.get(heal);
            if (current == null || costPer10 < current.costPer10)
            {
                cheapest.put(heal, new FoodPriceInfo(itemId, heal, price, costPer10));
                System.out.println("[CheapestFoodPlugin] Selected " + itemId + " for heal " + heal + " with costPer10 " + costPer10);
            }
        }

        cheapestFoods = new ArrayList<>(cheapest.values());
        cheapestFoods.sort(Comparator.comparingInt(FoodPriceInfo::getHeal).reversed());
        System.out.println("[CheapestFoodPlugin] Food list updated: " + cheapestFoods.size() + " items");
    }

    List<FoodPriceInfo> getCheapestFoodList()
    {
        return cheapestFoods;
    }

    boolean showCostPer10()
    {
        return config.showCostPer10();
    }

    void openBuyOffer(int itemId)
    {
        System.out.println("[CheapestFoodPlugin] Open GE offer for item: " + itemId);
    }

    @Provides
    CheapestFoodConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(CheapestFoodConfig.class);
    }

    static class FoodPriceInfo
    {
        final int itemId;
        final int heal;
        final int price;
        final double costPer10;

        FoodPriceInfo(int itemId, int heal, int price, double costPer10)
        {
            this.itemId = itemId;
            this.heal = heal;
            this.price = price;
            this.costPer10 = costPer10;
        }

        int getHeal() { return heal; }

        @Override
        public String toString()
        {
            return "FoodPriceInfo{" +
                    "itemId=" + itemId +
                    ", heal=" + heal +
                    ", price=" + price +
                    ", costPer10=" + costPer10 +
                    '}';
        }
    }
}
