package com.example;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CheapestFoodTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(CheapestFoodPlugin.class);
        RuneLite.main(args);
    }
}
