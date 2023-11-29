package com.bios;

import com.bios.enchantment.Enchantments;
import com.bios.item.Items;
import com.bios.task.Tasks;
import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mocha implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("mocha");

	@Override
	public void onInitialize() {
		Enchantments.registerEnchantments();
		Items.registerItems();
		Tasks.registerEvents();
	}

	public static Identifier id(String path) {
		return Identifier.of("mocha", path);
	}
}