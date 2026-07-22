package com.rpicos.circuitcraft;

import com.rpicos.circuitcraft.block.AmmeterBlock;
import com.rpicos.circuitcraft.block.CapacitorBlock;
import com.rpicos.circuitcraft.block.DiodeBlock;
import com.rpicos.circuitcraft.block.FrequencyModuleBlock;
import com.rpicos.circuitcraft.block.FunctionGeneratorBlock;
import com.rpicos.circuitcraft.block.GroundBlock;
import com.rpicos.circuitcraft.block.InductorBlock;
import com.rpicos.circuitcraft.block.MemristorBlock;
import com.rpicos.circuitcraft.block.OpAmpBlock;
import com.rpicos.circuitcraft.block.PowerSupplyBlock;
import com.rpicos.circuitcraft.block.ResistorBlock;
import com.rpicos.circuitcraft.block.VoltageModuleBlock;
import com.rpicos.circuitcraft.block.WireBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public final class ModBlocks {
	public static final Block RESISTOR = register("resistor", ResistorBlock::new, componentProperties());
	public static final Block CAPACITOR = register("capacitor", CapacitorBlock::new, componentProperties());
	public static final Block INDUCTOR = register("inductor", InductorBlock::new, componentProperties());
	public static final Block MEMRISTOR = register("memristor", MemristorBlock::new, componentProperties());
	public static final Block POWER_SUPPLY = register("power_supply", PowerSupplyBlock::new, componentProperties());
	public static final Block FUNCTION_GENERATOR = register("function_generator", FunctionGeneratorBlock::new, componentProperties());
	public static final Block WIRE = register("wire", WireBlock::new, componentProperties());
	public static final Block AMMETER = register("ammeter", AmmeterBlock::new, componentProperties());
	public static final Block VOLTAGE_MODULE = register("voltage_module", VoltageModuleBlock::new, componentProperties());
	public static final Block FREQUENCY_MODULE = register("frequency_module", FrequencyModuleBlock::new, componentProperties());
	public static final Block GROUND = register("ground", GroundBlock::new, componentProperties());
	public static final Block DIODE = register("diode", DiodeBlock::new, componentProperties());
	public static final Block OP_AMP = register("op_amp", OpAmpBlock::new, componentProperties());

	private static BlockBehaviour.Properties componentProperties() {
		return BlockBehaviour.Properties.of().strength(1.0F).sound(SoundType.METAL);
	}

	private static <T extends Block> T register(String path, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, CircuitCraft.id(path));
		T block = factory.apply(properties.setId(key));
		return Registry.register(BuiltInRegistries.BLOCK, key, block);
	}

	public static void init() {
	}
}
