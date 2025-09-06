package com.example;

import net.runelite.api.Client;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

public class CheapestFoodOverlay extends net.runelite.client.ui.overlay.OverlayPanel
{
    private final Client client;
    private final CheapestFoodPlugin plugin;
    private final ItemManager itemManager;

    @Inject
    public CheapestFoodOverlay(Client client, CheapestFoodPlugin plugin, ItemManager itemManager)
    {
        super(plugin);
        this.client = client;
        this.plugin = plugin;
        this.itemManager = itemManager;

        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.HIGH);
        setLayer(OverlayLayer.ABOVE_WIDGETS);

        panelComponent.setBackgroundColor(new Color(0, 0, 0, 150));
        panelComponent.setPreferredSize(new Dimension(220, 0));
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (client.getWidget(WidgetInfo.GRAND_EXCHANGE_WINDOW_CONTAINER) == null)
        {
            return null;
        }

        panelComponent.getChildren().clear();

        List<CheapestFoodPlugin.FoodPriceInfo> singleHealFoods = plugin.getCheapestFoodList().stream()
                .filter(f -> !f.doubleHeal)
                .collect(Collectors.toList());

        List<CheapestFoodPlugin.FoodPriceInfo> doubleHealFoods = plugin.getCheapestFoodList().stream()
                .filter(f -> f.doubleHeal)
                .collect(Collectors.toList());

        if (!singleHealFoods.isEmpty())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Single heal cheapest")
                    .leftColor(Color.CYAN)
                    .build());
            for (CheapestFoodPlugin.FoodPriceInfo food : singleHealFoods)
            {
                addFoodLine(food);
            }
        }

        if (!doubleHealFoods.isEmpty())
        {
            // Add a blank line as a separator
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(" ") // blank text
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Two-part heal cheapest")
                    .leftColor(Color.ORANGE)
                    .build());

            for (CheapestFoodPlugin.FoodPriceInfo food : doubleHealFoods)
            {
                addFoodLine(food);
            }
        }

        return super.render(graphics);
    }

    private void addFoodLine(CheapestFoodPlugin.FoodPriceInfo food)
    {
        BufferedImage icon = itemManager.getImage(food.itemId, 1, false);
        String name = itemManager.getItemComposition(food.itemId).getName();
        String rightText = food.price + " gp"; // Only show total cost

        panelComponent.getChildren().add(new ImageComponent(icon));
        panelComponent.getChildren().add(LineComponent.builder()
                .left(name + " (" + food.heal + "hp)")
                .leftColor(Color.WHITE)
                .right(rightText)
                .build());
    }
}
