package com.bios.enchantment;

import com.bios.Mocha;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class Enchantments {
    public static CascadeEnchantment CASCADE = new CascadeEnchantment();

    public static void registerEnchantments() {
        register("cascade", CASCADE);
    }

    private static void register(String name, MochaEnchantment enchantment) {
        Registry.register(Registries.ENCHANTMENT, Mocha.id(name), enchantment);
        enchantment.registerCallbacks();
    }
}
