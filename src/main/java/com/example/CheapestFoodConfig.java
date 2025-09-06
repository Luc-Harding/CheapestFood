package com.example;

import net.runelite.client.config.*;

@ConfigGroup("cheapestfood")
public interface CheapestFoodConfig extends Config
{
    @ConfigItem(
            keyName = "minHeal",
            name = "Minimum heal amount",
            description = "Show only foods that heal at least this amount"
    )
    default int minHeal()
    {
        return 0;
    }

    @ConfigItem(
            keyName = "showCostPer10",
            name = "Show cost per 10hp",
            description = "Display gp cost per 10 hp healed"
    )
    default boolean showCostPer10()
    {
        return true;
    }
}
