package com.rpicos.minememristors;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTab {
	public static final ResourceKey<CreativeModeTab> KEY =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, MineMemristors.id("components"));

	public static final CreativeModeTab TAB = Registry.register(
			BuiltInRegistries.CREATIVE_MODE_TAB,
			KEY,
			FabricCreativeModeTab.builder()
					.title(Component.translatable("itemGroup.minememristors.components"))
					.icon(() -> new ItemStack(ModItems.RESISTOR))
					.displayItems((parameters, output) -> {
						output.accept(ModItems.RESISTOR);
						output.accept(ModItems.CAPACITOR);
						output.accept(ModItems.INDUCTOR);
						output.accept(ModItems.MEMRISTOR);
						output.accept(ModItems.POWER_SUPPLY);
						output.accept(ModItems.FUNCTION_GENERATOR);
						output.accept(ModItems.WIRE);
						output.accept(ModItems.AMMETER);
						output.accept(ModItems.VOLTAGE_MODULE);
						output.accept(ModItems.FREQUENCY_MODULE);
						output.accept(ModItems.PROBE);
					})
					.build()
	);

	public static void init() {
	}
}
