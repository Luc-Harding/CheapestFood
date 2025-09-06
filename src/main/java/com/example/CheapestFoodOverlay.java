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
        setPreferredSize(new Dimension(250, 200));
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (plugin.getCheapestFoodList().isEmpty())
        {
            System.out.println("[CheapestFoodOverlay] Food list is empty");
            return null;
        }

        panelComponent.getChildren().clear();

        for (CheapestFoodPlugin.FoodPriceInfo food : plugin.getCheapestFoodList())
        {
            String name = itemManager.getItemComposition(food.itemId).getName();
            BufferedImage icon = itemManager.getImage(food.itemId, 1, false);

            if (icon != null)
            {
                panelComponent.getChildren().add(new ImageComponent(icon));
            }

            String rightText = food.price + " gp";
            if (plugin.showCostPer10())
            {
                rightText += " (" + String.format("%.1f", food.costPer10) + " /10hp)";
            }

            panelComponent.getChildren().add(
                    LineComponent.builder()
                            .left(name + " (" + food.heal + "hp)")
                            .right(rightText)
                            .build()
            );

            System.out.println("[CheapestFoodOverlay] Rendered: " + name + " | " + rightText);
        }

        return super.render(graphics);
    }
}
