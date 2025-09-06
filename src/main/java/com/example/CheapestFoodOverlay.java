package com.example;

import net.runelite.api.Client;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;

public class CheapestFoodOverlay extends OverlayPanel
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

        panelComponent.setBackgroundColor(new Color(0, 0, 0, 160));
        panelComponent.setGap(new Point(5, 3));
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        // Always render overlay (GE widget check optional)
        panelComponent.getChildren().clear();

        for (CheapestFoodPlugin.FoodPriceInfo food : plugin.getCheapestFoodList())
        {
            String name = itemManager.getItemComposition(food.itemId).getName();
            BufferedImage icon = itemManager.getImage(food.itemId, 1, false);

            if (icon != null)
            {
                panelComponent.getChildren().add(new ImageComponent(icon));
            }

            String leftText = name + " (" + food.heal + "hp)";
            String rightText = food.price + " gp";
            if (plugin.config.showCostPer10())
            {
                rightText += String.format(" (%.1f /10hp)", food.costPer10);
            }

            panelComponent.getChildren().add(
                    LineComponent.builder()
                            .left(leftText)
                            .right(rightText)
                            .build()
            );

            System.out.println("[CheapestFoodOverlay] Rendered: " + name + " | " + rightText);
        }

        return super.render(graphics);
    }
}
