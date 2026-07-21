package com.rpicos.minememristors;

import com.rpicos.minememristors.item.ProbeItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;

public final class ModItems {
	public static final Item RESISTOR = registerBlockItem("resistor", ModBlocks.RESISTOR);
	public static final Item CAPACITOR = registerBlockItem("capacitor", ModBlocks.CAPACITOR);
	public static final Item INDUCTOR = registerBlockItem("inductor", ModBlocks.INDUCTOR);
	public static final Item MEMRISTOR = registerBlockItem("memristor", ModBlocks.MEMRISTOR);
	public static final Item POWER_SUPPLY = registerBlockItem("power_supply", ModBlocks.POWER_SUPPLY);
	public static final Item FUNCTION_GENERATOR = registerBlockItem("function_generator", ModBlocks.FUNCTION_GENERATOR);
	public static final Item WIRE = registerBlockItem("wire", ModBlocks.WIRE);
	public static final Item AMMETER = registerBlockItem("ammeter", ModBlocks.AMMETER);
	public static final Item VOLTAGE_MODULE = registerBlockItem("voltage_module", ModBlocks.VOLTAGE_MODULE);
	public static final Item FREQUENCY_MODULE = registerBlockItem("frequency_module", ModBlocks.FREQUENCY_MODULE);
	public static final Item GROUND = registerBlockItem("ground", ModBlocks.GROUND);

	public static final Item PROBE = registerItem("probe", ProbeItem::new);

	private static Item registerBlockItem(String path, Block block) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, MineMemristors.id(path));
		Item item = new BlockItem(block, new Item.Properties().setId(key));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	private static Item registerItem(String path, Function<Item.Properties, Item> factory) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, MineMemristors.id(path));
		Item item = factory.apply(new Item.Properties().setId(key));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	public static void init() {
	}
}
