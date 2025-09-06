package com.example;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.eventbus.Subscribe;

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

    // All food items and their total heal
    private final Map<Integer, Integer> FOOD_HEALS = Map.ofEntries(
            Map.entry(ItemID.PINEAPPLE_PIZZA, 22),
            Map.entry(ItemID.SUMMER_PIE, 22),
            Map.entry(ItemID.WILD_PIE, 22),
            Map.entry(ItemID.MANTA_RAY, 22),
            Map.entry(ItemID.TUNA_POTATO, 22),
            Map.entry(ItemID.DARK_CRAB, 22),
            Map.entry(ItemID.ANGLERFISH, 22)
    );

    private List<FoodPriceInfo> cheapestFoods = new ArrayList<>();

    @Override
    protected void startUp()
    {
        overlayManager.add(overlay);
        cheapestFoods.clear();
        System.out.println("[CheapestFoodPlugin] Started");
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        cheapestFoods.clear();
        System.out.println("[CheapestFoodPlugin] Stopped");
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
        Map<Integer, FoodPriceInfo> cheapestSingle = new HashMap<>();
        Map<Integer, FoodPriceInfo> cheapestDouble = new HashMap<>();

        for (var entry : FOOD_HEALS.entrySet())
        {
            int itemId = entry.getKey();
            int heal = entry.getValue();
            int price = itemManager.getItemPrice(itemId);

            if (price <= 0)
            {
                System.out.println("[CheapestFoodPlugin] Price unavailable for item " + itemId);
                continue;
            }

            boolean doubleHeal = isDoubleHeal(itemId);
            double costPer10 = price * 10.0 / heal;
            FoodPriceInfo info = new FoodPriceInfo(itemId, heal, price, costPer10, doubleHeal);

            if (doubleHeal)
            {
                FoodPriceInfo current = cheapestDouble.get(heal);
                if (current == null || costPer10 < current.costPer10)
                {
                    cheapestDouble.put(heal, info);
                    System.out.println("[CheapestFoodPlugin] Selected (double) " + itemId + " for heal " + heal + " with costPer10 " + costPer10);
                }
            }
            else
            {
                FoodPriceInfo current = cheapestSingle.get(heal);
                if (current == null || costPer10 < current.costPer10)
                {
                    cheapestSingle.put(heal, info);
                    System.out.println("[CheapestFoodPlugin] Selected (single) " + itemId + " for heal " + heal + " with costPer10 " + costPer10);
                }
            }
        }

        cheapestFoods = new ArrayList<>();
        cheapestFoods.addAll(cheapestSingle.values());
        cheapestFoods.addAll(cheapestDouble.values());
        cheapestFoods.sort(Comparator.comparingInt(f -> -f.heal));

        System.out.println("[CheapestFoodPlugin] Food list updated: " + cheapestFoods.size() + " items");
    }

    private boolean isDoubleHeal(int itemId)
    {
        // Pies and pizza are double-heal items
        return itemId == ItemID.PINEAPPLE_PIZZA
                || itemId == ItemID.SUMMER_PIE
                || itemId == ItemID.WILD_PIE;
    }

    List<FoodPriceInfo> getCheapestFoodList()
    {
        return cheapestFoods;
    }

    void openBuyOffer(int itemId)
    {
        System.out.println("[CheapestFoodPlugin] Open GE offer for item: " + itemId);
    }

    static class FoodPriceInfo
    {
        final int itemId;
        final int heal;
        final int price;
        final double costPer10;
        final boolean doubleHeal;

        FoodPriceInfo(int itemId, int heal, int price, double costPer10, boolean doubleHeal)
        {
            this.itemId = itemId;
            this.heal = heal;
            this.price = price;
            this.costPer10 = costPer10;
            this.doubleHeal = doubleHeal;
        }
    }
}
