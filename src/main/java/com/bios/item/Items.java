package com.bios.item;

import com.bios.Mocha;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public class Items {
    public static final Item WAND_OF_RETURN = new WandOfReturn();

    public static void registerItems() {
        register("wand_of_return", WAND_OF_RETURN, ItemGroups.TOOLS);
    }

    private static void register(String name, Item item) {
        Registry.register(Registries.ITEM, Mocha.id(name), item);
    }

    private static void register(String name, Item item, RegistryKey<ItemGroup> itemGroup) {
        register(name, item);
        ItemGroupEvents.modifyEntriesEvent(itemGroup).register(content -> content.add(item));
    }
}
